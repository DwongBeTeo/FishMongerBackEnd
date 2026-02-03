package datn.duong.FishSeller.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "users")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
// ⚠️ Lưu ý quan trọng: Nếu trong UserEntity có quan hệ
// @OneToMany (ví dụ List Orders),
// bạn bắt buộc phải dùng @ToString.Exclude trên trường đó,
// nếu không sẽ bị lỗi StackOverflowError (tràn bộ nhớ) khi in log.
public class UserEntity extends BaseEntity {

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password; // Lưu chuỗi hash

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "full_name")
    private String fullName;

    private String phoneNumber;

    private String address;
    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "is_active")
    private Boolean isActive;
    private String activationToken;

    @PrePersist
    public void prePersist() {
        if (this.isActive == null) {
            isActive = false;
        }
    }

    // --- Mối quan hệ N-1 với Role ---
    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false) // Tên cột khóa ngoại trong DB sẽ là role_id
    private RoleEntity role;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @ToString.Exclude // <--- KHÔNG CÓ CÁI NÀY LÀ BỊ TRÀN BỘ NHỚ NGAY
    @JsonIgnore 
    // Nếu không có dòng này, User chứa Appointment, Appointment chứa User -> Lặp vô tận -> Treo API.
    @Builder.Default // Nếu dùng @SuperBuilder/Builder thì nên có dòng này để khởi tạo list rỗng
    private List<AppointmentEntity> appointments = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AddressEntity> addresses;
}
