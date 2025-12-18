package datn.duong.FishSeller.dto;

import java.util.List;

import lombok.*;

// 2. DTO hiển thị cả giỏ hàng
@Data
@Builder
public class CartDTO {
    private Long id;            // ID của Cart
    private Long userId;
    private List<CartItemDTO> items;
    private Double totalAmount; // Tổng tiền cả giỏ hàng
}