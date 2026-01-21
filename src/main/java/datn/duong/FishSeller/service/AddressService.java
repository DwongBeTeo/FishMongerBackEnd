package datn.duong.FishSeller.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import datn.duong.FishSeller.dto.AddressDTO;
import datn.duong.FishSeller.entity.AddressEntity;
import datn.duong.FishSeller.entity.UserEntity;
import datn.duong.FishSeller.repository.AddressRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AddressService {
    private final AddressRepository addressRepository;
    private final UserService userService;

    // --------------------------
    //  Logic cho user
    // --------------------------

    // Lấy danh sách địa chỉ của user đang đăng nhập
    public List<AddressDTO> myAddresses() {
        UserEntity user = userService.getCurrentProfile();
        return addressRepository.findByUserId(user.getId()).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    // Thêm địa chỉ mới
    public AddressDTO addAddress(AddressDTO dto) {
        UserEntity user = userService.getCurrentProfile();
        
        // Validate số điện thoại (Logic dùng chung)
        if (!isValidPhoneNumber(dto.getPhoneNumber())) {
            throw new RuntimeException("Số điện thoại không hợp lệ (Phải có 10 số, bắt đầu bằng số 0)");
        }

        AddressEntity entity = AddressEntity.builder()
                .user(user)
                .recipientName(dto.getRecipientName())
                .phoneNumber(dto.getPhoneNumber())
                .detailedAddress(dto.getDetailedAddress())
                .isDefault(dto.isDefault())
                .build();
        
        // Nếu user chưa có địa chỉ nào, cái đầu tiên sẽ là default
        List<AddressEntity> existing = addressRepository.findByUserId(user.getId());
        if (existing.isEmpty()) entity.setDefault(true);

        return toDTO(addressRepository.save(entity));
    }

    // 4. Sửa địa chỉ
    @Transactional
    public AddressDTO updateAddress(Long addressId, AddressDTO dto) {
        // Lấy User hiện tại
        UserEntity user = userService.getCurrentProfile();

        // Tìm địa chỉ trong DB
        AddressEntity address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ với ID: " + addressId));

        // --- BẢO MẬT: Kiểm tra quyền sở hữu ---
        if (!address.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền sửa địa chỉ này!");
        }

        // --- Validate lại số điện thoại nếu có thay đổi ---
        if (dto.getPhoneNumber() != null && !isValidPhoneNumber(dto.getPhoneNumber())) {
            throw new RuntimeException("Số điện thoại không hợp lệ");
        }

        // Cập nhật thông tin
        // Chỉ cập nhật những trường client gửi lên (hoặc cập nhật hết tùy logic frontend)
        address.setRecipientName(dto.getRecipientName());
        address.setPhoneNumber(dto.getPhoneNumber());
        address.setDetailedAddress(dto.getDetailedAddress());
        
        // Logic xử lý địa chỉ mặc định (Nếu user set cái này là default)
        if (dto.isDefault() && !address.isDefault()) {
            // Cần bỏ default của các địa chỉ khác (nếu muốn chỉ có 1 default)
            // (Phần này nâng cao, tạm thời set thẳng vào)
            address.setDefault(true);
        } else {
            address.setDefault(dto.isDefault());
        }

        return toDTO(addressRepository.save(address));
    }

    public void deleteAddress(Long id) {
        UserEntity user = userService.getCurrentProfile();
        AddressEntity address = addressRepository.findById(id)
             .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ"));
             
        // Bảo mật: Chỉ xóa được địa chỉ của chính mình
        if(!address.getUser().getId().equals(user.getId())){
             throw new RuntimeException("Không có quyền xóa");
        }
        addressRepository.delete(address);
    }

    // ------------------------
    // --- LOGIC CHO ADMIN ---
    // ------------------------
    public List<AddressDTO> getAddressesByUserId(Long userId) {
        // Admin được quyền xem hết
        return addressRepository.findByUserId(userId).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    // Helper convert
    private AddressDTO toDTO(AddressEntity e) {
        return AddressDTO.builder()
                .id(e.getId())
                .recipientName(e.getRecipientName())
                .phoneNumber(e.getPhoneNumber())
                .detailedAddress(e.getDetailedAddress())
                .isDefault(e.isDefault())
                .build();
    }

    // --- HÀM VALIDATE SỐ ĐIỆN THOẠI (BACKEND) ---
    public static boolean isValidPhoneNumber(String phone) {
        // Regex: Bắt đầu bằng 0, theo sau là 9 chữ số bất kỳ
        return phone != null && phone.matches("^0\\d{9}$");
    }
}