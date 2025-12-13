package datn.duong.FishSeller.entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "roles") // Tên bảng trong DB
@Data // Lombok sinh Getter, Setter, toString...
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Tự tăng (Auto Increment)
    private Long id;

    @Column(nullable = false, unique = true) // Không được null, tên Role phải duy nhất
    private String name;

    // Quan hệ 1-N: Một Role có nhiều User
    // mappedBy = "role" nghĩa là biến "role" bên class User nắm giữ khóa ngoại
//    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
//    private List<User> users;
}
