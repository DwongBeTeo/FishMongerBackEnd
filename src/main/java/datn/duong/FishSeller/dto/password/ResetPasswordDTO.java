package datn.duong.FishSeller.dto.password;

import lombok.Data;

// Bước 2: Nhập otp và pass mới
@Data
public class ResetPasswordDTO {
    private String email;
    private String otp;
    private String newPassword;
}