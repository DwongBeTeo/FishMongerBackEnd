package datn.duong.FishSeller.controller.admin;

import datn.duong.FishSeller.dto.AddressDTO;
import datn.duong.FishSeller.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("admin/addresses")
@RequiredArgsConstructor
public class AddressAdminController {

    private final AddressService addressService;

    // 1. Admin xem danh sách địa chỉ của User cụ thể (theo userId)
    // GET: /api/v1.0/admin/addresses/user/{userId}
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AddressDTO>> getUserAddresses(@PathVariable Long userId) {
        return ResponseEntity.ok(addressService.getAddressesByUserId(userId));
    }
}