package datn.duong.FishSeller.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String recipientName; // Tên người nhận (VD: Anh A - Nhà riêng)
    private String phoneNumber;
    
    @Column(columnDefinition = "TEXT")
    private String detailedAddress; // Địa chỉ cụ thể

    @Column(columnDefinition = "boolean default false")
    private boolean isDefault; // Đánh dấu địa chỉ mặc định

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private UserEntity user;
}