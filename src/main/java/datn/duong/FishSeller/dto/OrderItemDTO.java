package datn.duong.FishSeller.dto;

import lombok.*;

// DTO chi tiết từng món trong đơn (Có thể dùng chung hoặc tách riêng với CartItemDTO)
@Data
@Builder
public class OrderItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
    private Integer quantity;
    private Double price; // Giá lúc mua
}
