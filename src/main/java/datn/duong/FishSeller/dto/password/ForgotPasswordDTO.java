package datn.duong.FishSeller.dto.password;

import lombok.Data;

// Bước 1: Gửi yêu cầu lấy lại pass
@Data
public class ForgotPasswordDTO {
    private String email;
}
