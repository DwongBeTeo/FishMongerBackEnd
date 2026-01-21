package datn.duong.FishSeller.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import datn.duong.FishSeller.dto.ServiceTypeDTO;
import datn.duong.FishSeller.entity.ServiceTypeEntity;
import datn.duong.FishSeller.entity.UserEntity;
import datn.duong.FishSeller.repository.ServiceTypeRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceTypeService {

    private final ServiceTypeRepository serviceTypeRepository;
    private final UserService userService; // Để lấy thông tin người đang thao tác (nếu cần)

    // =========================================================================
    // PHẦN 1: PUBLIC / USER METHODS (Xem, Tìm kiếm - Chỉ hiện Active)
    // =========================================================================

    // 1. Lấy danh sách dịch vụ (Dành cho trang chủ/đặt lịch)
    @Transactional(readOnly = true)
    public Page<ServiceTypeDTO
    > getAllActiveServiceTypes(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("price").ascending()); // Sắp xếp giá tăng dần
        return serviceTypeRepository.findAllByIsActiveTrue(pageable)
                .map(this::toDTO);
    }

    // 2. Tìm kiếm dịch vụ (Dành cho User)
    @Transactional(readOnly = true)
    public Page<ServiceTypeDTO> searchActiveServiceTypes(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return serviceTypeRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(keyword, pageable)
                .map(this::toDTO);
    }

    // 3. Xem chi tiết 1 dịch vụ (Để xem mô tả kỹ hơn trước khi đặt)
    @Transactional(readOnly = true)
    public ServiceTypeDTO getServiceTypeDetail(Long id) {
        ServiceTypeEntity service = serviceTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dịch vụ không tồn tại!"));

        // Nếu dịch vụ bị ẩn mà User thường cố truy cập -> Chặn
        if (!service.getIsActive()) {
            // Có thể check role ở đây nếu muốn Admin vẫn xem được
            throw new RuntimeException("Dịch vụ này hiện đang tạm ngưng phục vụ.");
        }
        return toDTO(service);
    }

    // =========================================================================
    // PHẦN 2: ADMIN METHODS (Thêm, Sửa, Ẩn/Hiện)
    // =========================================================================

    // 4. Lấy tất cả (kể cả ẩn) để quản lý
    @Transactional(readOnly = true)
    public Page<ServiceTypeDTO> getAllServiceTypesForAdmin(String keyword, int page, int size) {
        // Có thể thêm dòng này để đảm bảo chỉ Admin mới gọi được (hoặc chặn ở Controller)
        UserEntity currentUser = userService.getCurrentProfile(); 
        // if (!currentUser.getRole().getName().equals("ADMIN")) throw ...
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        
        if (keyword != null && !keyword.isEmpty()) {
            return serviceTypeRepository.findByNameContainingIgnoreCase(keyword, pageable).map(this::toDTO);
        }
        return serviceTypeRepository.findAll(pageable).map(this::toDTO);
    }

    // 5. Tạo dịch vụ mới
    @Transactional
    public ServiceTypeDTO createServiceType(ServiceTypeDTO dto) {
        if (serviceTypeRepository.existsByName(dto.getName())) {
            throw new RuntimeException("Tên dịch vụ đã tồn tại: " + dto.getName());
        }

        ServiceTypeEntity entity = toEntity(dto);
        // Mặc định khi tạo mới là Active
        if (entity.getIsActive() == null) {
            entity.setIsActive(true);
        }

        return toDTO(serviceTypeRepository.save(entity));
    }

    // 6. Cập nhật dịch vụ
    @Transactional
    public ServiceTypeDTO updateServiceType(Long id, ServiceTypeDTO dto) {
        ServiceTypeEntity existingService = serviceTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dịch vụ id: " + id));

        existingService.setName(dto.getName());
        existingService.setPrice(dto.getPrice());
        existingService.setDescription(dto.getDescription());
        existingService.setImageUrl(dto.getImageUrl());
        existingService.setEstimatedDuration(dto.getEstimatedDuration());
        
        // Admin có thể chủ động update trạng thái active/inactive
        if (dto.getIsActive() != null) {
            existingService.setIsActive(dto.getIsActive());
        }

        return toDTO(serviceTypeRepository.save(existingService));
    }

    // 7. Xóa mềm (Chuyển Active -> Inactive)
    // mặc định ban đầu isActive là true    
    @Transactional
    public void toggleServiceStatus(Long id) {
        ServiceTypeEntity service = serviceTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dịch vụ"));
        
        // Đảo ngược trạng thái
        service.setIsActive(!service.getIsActive());
        serviceTypeRepository.save(service);
    }

    // =========================================================================
    // MAPPING METHODS (Manual Mapping)
    // =========================================================================

    public ServiceTypeDTO toDTO(ServiceTypeEntity entity) {
        if (entity == null) return null;
        return ServiceTypeDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .price(entity.getPrice())
                .description(entity.getDescription())
                .imageUrl(entity.getImageUrl())
                .estimatedDuration(entity.getEstimatedDuration())
                .isActive(entity.getIsActive())
                .createdDate(entity.getCreatedDate())
                .updatedDate(entity.getUpdatedDate())
                .build();
    }

    public ServiceTypeEntity toEntity(ServiceTypeDTO dto) {
        if (dto == null) return null;
        return ServiceTypeEntity.builder()
                .name(dto.getName())
                .price(dto.getPrice())
                .description(dto.getDescription())
                .imageUrl(dto.getImageUrl())
                .estimatedDuration(dto.getEstimatedDuration())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();
    }
}
