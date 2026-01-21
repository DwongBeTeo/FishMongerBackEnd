package datn.duong.FishSeller.dto;

import datn.duong.FishSeller.enums.EmployeeStatus;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class EmployeeDTO extends BaseDTO {

    private String fullName;
    private String phoneNumber;

    private EmployeeStatus status; // ACTIVE, INACTIVE...

    // Thông tin tài khoản liên kết (User)
    private Long userId;     // Để Admin biết liên kết với User ID nào
    private String username; // Để hiển thị tên đăng nhập
}