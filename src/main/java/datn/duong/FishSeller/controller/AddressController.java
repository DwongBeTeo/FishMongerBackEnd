package datn.duong.FishSeller.controller;

import datn.duong.FishSeller.dto.AddressDTO;
import datn.duong.FishSeller.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
public class AddressController {
    
    private final AddressService addressService;

    // 1. Lấy danh sách địa chỉ của TÔI
    // GET: /api/v1.0/addresses
    @GetMapping
    public ResponseEntity<List<AddressDTO>> getMyAddresses() {
        return ResponseEntity.ok(addressService.myAddresses());
    }

    // 2. Thêm địa chỉ mới cho TÔI
    // POST: /api/v1.0/addresses
    @PostMapping
    public ResponseEntity<AddressDTO> addAddress(@RequestBody AddressDTO addressDTO) {
        return ResponseEntity.ok(addressService.addAddress(addressDTO));
    }

    // 3. Xóa địa chỉ (Nếu cần)
    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long addressId) {
        addressService.deleteAddress(addressId);
        return ResponseEntity.ok().build();
    }

    // 4. Sửa địa chỉ
    // PUT: /api/v1.0/addresses/{addressId}
    @PutMapping("/{addressId}")
    public ResponseEntity<AddressDTO> updateAddress(
            @PathVariable Long addressId, 
            @RequestBody AddressDTO addressDTO) {
        return ResponseEntity.ok(addressService.updateAddress(addressId, addressDTO));
    }
}