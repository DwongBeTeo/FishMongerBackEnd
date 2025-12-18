package datn.duong.FishSeller.dto;

import lombok.*;
// 1. DTO hiển thị từng món trong giỏ
@Data
@Builder
public class CartItemDTO {
    private Long id;            // ID của CartItem
    private Long productId;
    private String productName;
    private String productImage;
    private Double price;       // Giá đơn vị tại thời điểm xem
    private Integer quantity;
    private Double subTotal;    // Thành tiền (price * quantity)
    private Integer productStock; // <--- Thêm trường này nếu muốn FE hiển thị "Còn 5 sản phẩm"
}