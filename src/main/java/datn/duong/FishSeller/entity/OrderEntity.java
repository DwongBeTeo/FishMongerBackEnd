package datn.duong.FishSeller.entity;

import datn.duong.FishSeller.enums.OrderStatus;
import datn.duong.FishSeller.enums.PaymentMethod;
import datn.duong.FishSeller.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime orderDate;
    private Double totalAmount;
    private String shippingAddress;
    
    @Column(nullable = false)
    private String phoneNumber; // Nên lưu thêm sđt giao hàng phòng khi user đổi số

    @Enumerated(EnumType.STRING)
    private OrderStatus status;// ENUM: PENDING, SHIPPING, COMPLETED, CANCELLED

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method") // Nên đặt tên rõ ràng trong DB
    private PaymentMethod paymentMethod; // COD, BANKING

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus; // UNPAID, PAID

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItemEntity> orderItems;

    // MỚI: Cờ đánh dấu user yêu cầu hủy
    @Column(columnDefinition = "boolean default false")
    private boolean cancellationRequested;
}
