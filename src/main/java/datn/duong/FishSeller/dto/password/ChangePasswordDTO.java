package datn.duong.FishSeller.dto.password;

import lombok.Data;

// Dùng cho người đã đăng nhập
@Data
public class ChangePasswordDTO {
    private String currentPassword;
    private String newPassword; 
    private String confirmPassword;
}