package datn.duong.FishSeller.entity;

import datn.duong.FishSeller.enums.AppointmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "appointments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id") // Khách hàng đặt
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "service_type_id")
    private ServiceTypeEntity serviceType;

    @ManyToOne
    @JoinColumn(name = "employee_id") // Nhân viên thực hiện (Optional)
    private EmployeeEntity employee;

    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String address; // Địa chỉ đến làm dịch vụ
    private String note;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;
}
