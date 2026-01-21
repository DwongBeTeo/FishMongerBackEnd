package datn.duong.FishSeller.controller.admin;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import datn.duong.FishSeller.dto.UserDTO;
import datn.duong.FishSeller.service.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("admin/users") // Endpoint dành cho Admin quản lý User
@RequiredArgsConstructor
public class UserAdminController {

    private final UserService userService;

    // 1. Lấy danh sách user chưa phải là nhân viên
    // URL: GET /api/v1.0/admin/users/available
    @GetMapping("/available")
    public ResponseEntity<List<UserDTO>> getAvailableUsers() {
        return ResponseEntity.ok(userService.getAvailableUsersForEmployee());
    }
}
