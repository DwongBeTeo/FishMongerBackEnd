package datn.duong.FishSeller.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "employee")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String phoneNumber;

    // Nếu nhân viên cũng là User để đăng nhập
    @OneToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
}
