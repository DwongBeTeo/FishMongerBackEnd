package datn.duong.FishSeller.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import datn.duong.FishSeller.dto.AuthDTO;
import datn.duong.FishSeller.dto.UserDTO;
import datn.duong.FishSeller.dto.password.ChangePasswordDTO;
import datn.duong.FishSeller.dto.password.ForgotPasswordDTO;
import datn.duong.FishSeller.dto.password.ResetPasswordDTO;
import datn.duong.FishSeller.service.UserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
// @RequestMapping("")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerProfile(@RequestBody UserDTO userDTO ){
        try {
            //Chuyển UserDTO thành UserEntity (bằng phương thức toEntity).
            //Trả về UserDTO (được gán vào registeredProfile).
            UserDTO registeredProfile = userService.registerUser(userDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredProfile);
            
        } catch (Exception e) {
            // TODO: handle exception
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/activate")
    public ResponseEntity<String> activateUser(@RequestParam String token){
        boolean isActivated = userService.activateUser(token);
        if (isActivated){
            return ResponseEntity.ok("Profile activated successfully, Please comeback your website");
        }else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Activation token not found or already used");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String,Object>> login(@RequestBody AuthDTO authDTO) {
        try {
            if(!userService.isAccountActive(authDTO.getEmail())){

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "message", "Account is not active. Please activate your account first"
                ));
            }
            Map<String, Object> response = userService.authenticateAndGenerateToken(authDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/test")
    public String test() {
        return "test successful";
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getPublicProfile () {
        UserDTO userDTO =  userService.getPublicProfile(null);
        return ResponseEntity.ok(userDTO);
    }

    // API UPDATE PROFILE
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UserDTO userDTO) {
        try {
            // Gọi service để update (Email và ngày tạo sẽ không bị thay đổi do logic trong service)
            UserDTO updatedProfile = userService.updateUserProfile(userDTO);
            return ResponseEntity.ok(updatedProfile);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    // --- 1. API ĐỔI MẬT KHẨU (Yêu cầu Login - Có Header Token) ---
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordDTO request) {
        try {
            userService.changePassword(request);
            return ResponseEntity.ok("Đổi mật khẩu thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // --- 2. API QUÊN MẬT KHẨU (Public - Không cần Login) ---
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordDTO request) {
        try {
            userService.requestPasswordReset(request.getEmail());
            return ResponseEntity.ok("Link đặt lại mật khẩu đã được gửi vào email của bạn");
        } catch (Exception e) {
            // Lưu ý: Về mặt bảo mật, đôi khi người ta luôn trả về OK dù email không tồn tại 
            // để tránh hacker dò email. Nhưng với đồ án, trả về lỗi để dễ debug cũng được.
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // --- 3. API RESET MẬT KHẨU (Public - Không cần Login)
    // Body: { "email": "a@gmail.com", "otp": "123456", "newPassword": "newPass" }
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDTO request) {
        try {
            userService.resetPassword(request);
            return ResponseEntity.ok("Đặt lại mật khẩu thành công. Vui lòng đăng nhập lại.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
