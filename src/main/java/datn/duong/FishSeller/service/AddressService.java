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
    @Transactional
    public AddressDTO addAddress(AddressDTO dto) {
        UserEntity user = userService.getCurrentProfile();
        
        // 1. Kiểm tra xem đây có phải địa chỉ đầu tiên không
        List<AddressEntity> existing = addressRepository.findByUserId(user.getId());
        boolean isFirstAddress = existing.isEmpty();

        // 2. Nếu là default HOẶC là địa chỉ đầu tiên -> Reset những cái cũ
        if (dto.isDefault() || isFirstAddress) {
            addressRepository.resetDefaultByUserId(user.getId());
        }

        AddressEntity entity = AddressEntity.builder()
                .user(user)
                .recipientName(dto.getRecipientName())
                .phoneNumber(dto.getPhoneNumber())
                .detailedAddress(dto.getDetailedAddress())
                .isDefault(dto.isDefault() || isFirstAddress) // Tự động set true nếu là cái đầu tiên
                .build();
        
        return toDTO(addressRepository.save(entity));
    }

    // 4. Sửa địa chỉ
    @Transactional
    public AddressDTO updateAddress(Long addressId, AddressDTO dto) {
        UserEntity user = userService.getCurrentProfile();

        AddressEntity address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ với ID: " + addressId));

        // --- GIỮ NGUYÊN BẢO MẬT ---
        if (!address.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền sửa địa chỉ này!");
        }

        // --- GIỮ NGUYÊN VALIDATE SĐT ---
        if (dto.getPhoneNumber() != null && !isValidPhoneNumber(dto.getPhoneNumber())) {
            throw new RuntimeException("Số điện thoại không hợp lệ");
        }

        // Cập nhật thông tin cơ bản
        address.setRecipientName(dto.getRecipientName());
        address.setPhoneNumber(dto.getPhoneNumber());
        address.setDetailedAddress(dto.getDetailedAddress());
        
        // --- SỬA LOGIC DEFAULT ĐỂ CHẠY ĐÚNG ---
        if (dto.isDefault() && !address.isDefault()) {
            // 1. Reset tất cả các địa chỉ khác của user này về false
            addressRepository.resetDefaultByUserId(user.getId());
            
            // 2. Set địa chỉ hiện tại là mặc định
            address.setDefault(true);
            
            // 3. Flush để đảm bảo lệnh reset chạy xong trước khi transaction kết thúc
            addressRepository.saveAndFlush(address);
        } else {
            // Giữ nguyên logic set theo dto nếu không phải trường hợp chuyển đổi mặc định mới
            address.setDefault(dto.isDefault());
            addressRepository.save(address);
        }

        return toDTO(address); 
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