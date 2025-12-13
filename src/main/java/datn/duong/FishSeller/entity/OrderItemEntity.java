package datn.duong.FishSeller.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "orderItem")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private ProductEntity product;

    private Integer quantity;
    private Double priceAtOrder; // Quan trọng: Giá tại thời điểm mua
}
