package datn.duong.FishSeller.entity;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
@Entity
@Table(name = "password_reset_token")
@Data
@AllArgsConstructor
@Builder
public class PasswordResetTokenEntity {
  
    private static final int EXPIRATION = 10; // Mã hết hạn sau 10 phút

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token; // Mã validate (VD: UUID hoặc 6 số ngẫu nhiên)

    @OneToOne(targetEntity = UserEntity.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private UserEntity user; // Liên kết với bảng User

    private Date expiryDate;

    // Constructor để tự động tính thời gian hết hạn khi tạo token
    public PasswordResetTokenEntity(UserEntity user, String token) {
        this.user = user;
        this.token = token;
        this.expiryDate = calculateExpiryDate(EXPIRATION);
    }
    
    public PasswordResetTokenEntity() {}

    // Hàm tính thời gian hết hạn
    private Date calculateExpiryDate(int expiryTimeInMinutes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Timestamp(cal.getTime().getTime()));
        cal.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Date(cal.getTime().getTime());
    }
}