package datn.duong.FishSeller.entity;

import datn.duong.FishSeller.enums.AppointmentStatus;
import datn.duong.FishSeller.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "appointments")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder // <--- Class con dùng SuperBuilder
public class AppointmentEntity extends BaseEntity{
    // ID kế thừa từ cha

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude // <--- Quan trọng: Cắt đứt User để tránh User in ra List Appointment -> Lặp vô tận
    private UserEntity user; 

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_type_id", nullable = false)
    @ToString.Exclude 
    private ServiceTypeEntity serviceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id") 
    @ToString.Exclude
    private EmployeeEntity employee;

    // Thông tin
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private LocalTime expectedEndTime; 
    private String address; 
    private String note;
    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;     
    private Double priceAtBooking;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status = AppointmentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID; // Sử dụng Enum PaymentStatus bạn đã gửi

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;
    
    // 4. FEEDBACK
    private Integer rating;
    @Column(name = "feedback_comment", columnDefinition = "TEXT")
    private String feedbackComment;
}
