package datn.duong.FishSeller.entity;

import java.time.LocalDate;

import datn.duong.FishSeller.enums.DiscountType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "vouchers")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherEntity extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String code; // VD: SALE50, TET2025

    @Column(nullable = false)
    private String description; // VD: Giảm 50k cho đơn từ 200k

    // Loại giảm giá: PERCENTAGE (Theo %) hoặc FIXED_AMOUNT (Trừ tiền cứng)
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", length = 20, nullable = false)
    private DiscountType discountType; 

    private Double discountValue; // Giá trị (VD: 10 (nếu là %) hoặc 50000 (nếu là tiền))

    private Double maxDiscountAmount; // Giảm tối đa (VD: giảm 10% nhưng tối đa 100k)
    
    private Double minOrderValue; // Đơn tối thiểu để dùng (VD: 200k)

    private Integer quantity; // Số lượng mã (VD: 100 mã)
    
    private LocalDate startDate;
    private LocalDate endDate;
    
    private Boolean isActive;
}