package datn.duong.FishSeller.entity;

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

    // @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    // private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password; // Lưu chuỗi hash

    @Column(nullable = false)
    private String email;

    @Column() // Mapping tên biến camelCase sang snake_case trong DB
    private String fullName;

    private String phoneNumber;

    private String address;

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

}
