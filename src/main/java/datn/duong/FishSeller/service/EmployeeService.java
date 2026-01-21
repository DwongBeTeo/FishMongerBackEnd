package datn.duong.FishSeller.service;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import datn.duong.FishSeller.dto.EmployeeDTO;
import datn.duong.FishSeller.entity.EmployeeEntity;
import datn.duong.FishSeller.entity.RoleEntity;
import datn.duong.FishSeller.entity.UserEntity;
import datn.duong.FishSeller.enums.EmployeeStatus;
import datn.duong.FishSeller.repository.EmployeeRepository;
import datn.duong.FishSeller.repository.RoleRepository;
import datn.duong.FishSeller.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository; // Cần để update Role cho User
    private final UserService userService; // Lấy current user

    // =========================================================================
    // ADMIN METHODS
    // =========================================================================

    // 1. Lấy danh sách nhân viên (Search + Filter)
    @Transactional(readOnly = true)
    public Page<EmployeeDTO> getAllEmployees(String keyword, EmployeeStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return employeeRepository.searchEmployees(keyword, status, pageable)
                .map(this::toDTO);
    }

    // 2. Tạo nhân viên mới (Liên kết User -> Employee)
    @Transactional
    public EmployeeDTO createEmployee(EmployeeDTO dto) {
        // A. Validate User
        UserEntity user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User ID không tồn tại"));

        // B. CHECK TRÙNG (Quan trọng nhất): User này đã là nhân viên chưa?
        if (employeeRepository.existsByUserId(dto.getUserId())) {
            throw new RuntimeException("User " + user.getUsername() + " đã là nhân viên rồi, không thể tạo thêm hồ sơ!");
        }

        // C. Tạo Entity
        EmployeeEntity employee = toEntity(dto);
        employee.setUser(user);
        
        // Mặc định trạng thái là ACTIVE nếu không gửi
        if (employee.getStatus() == null) {
            employee.setStatus(EmployeeStatus.ACTIVE);
        }

        // D. Tự động update Role của User thành EMPLOYEE (nếu đang là USER thường)
        if (!user.getRole().getName().equals("ADMIN")) {
            RoleEntity employeeRole = roleRepository.findByName("EMPLOYEE")
                    .orElseThrow(() -> new RuntimeException("Role EMPLOYEE chưa được tạo trong DB"));
            user.setRole(employeeRole);
            userRepository.save(user); // Lưu lại role mới
        }

        return toDTO(employeeRepository.save(employee));
    }

    // 3. Cập nhật thông tin nhân viên
    @Transactional
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO dto) {
        EmployeeEntity employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));

        employee.setFullName(dto.getFullName());
        employee.setPhoneNumber(dto.getPhoneNumber());
        employee.setStatus(dto.getStatus());

        // Lưu ý: Thường ta KHÔNG cho phép đổi User ID của nhân viên sau khi đã tạo
        // để tránh lỗi dữ liệu lịch sử. Nên không set user ở đây.

        return toDTO(employeeRepository.save(employee));
    }

    // Lấy danh sách nhân viên ACTIVE để gán việc
    public List<EmployeeDTO> getActiveEmployees() {
        return employeeRepository.findByStatus(EmployeeStatus.ACTIVE)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }
    
    // =========================================================================
    // USER/EMPLOYEE METHODS
    // =========================================================================
    
    // 4. Xem hồ sơ làm việc của chính mình
    @Transactional(readOnly = true)
    public EmployeeDTO getMyProfile() {
        UserEntity currentUser = userService.getCurrentProfile();
        
        EmployeeEntity employee = employeeRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Bạn chưa được thiết lập hồ sơ nhân viên!"));
                
        return toDTO(employee);
    }

    // =========================================================================
    // MAPPING
    // =========================================================================

    public EmployeeDTO toDTO(EmployeeEntity entity) {
        if (entity == null) return null;
        return EmployeeDTO.builder()
                .id(entity.getId())
                .createdDate(entity.getCreatedDate())
                .updatedDate(entity.getUpdatedDate())
                
                .fullName(entity.getFullName())
                .phoneNumber(entity.getPhoneNumber())
                .status(entity.getStatus())
                
                // Map thông tin User liên kết
                .userId(entity.getUser().getId())
                .username(entity.getUser().getUsername())
                .build();
    }

    public EmployeeEntity toEntity(EmployeeDTO dto) {
        if (dto == null) return null;
        return EmployeeEntity.builder()
                .fullName(dto.getFullName())
                .phoneNumber(dto.getPhoneNumber())
                .status(dto.getStatus())
                // User sẽ được set riêng trong hàm create
                .build();
    }
}