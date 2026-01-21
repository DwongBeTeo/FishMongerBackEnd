package datn.duong.FishSeller.entity;

import datn.duong.FishSeller.enums.EmployeeStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "employee")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class EmployeeEntity extends BaseEntity {
    // ID, createdDate, updatedDate được kế thừa

    private String fullName;
    private String phoneNumber;

   @Enumerated(EnumType.STRING)
    @Builder.Default // Mặc định nhân viên mới tạo là đang làm việc
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    @OneToOne
    @JoinColumn(name = "user_id")
    @ToString.Exclude // <--- NGĂN CHẶN LỖI: Cắt vòng lặp toString sang User
    private UserEntity user;
}
