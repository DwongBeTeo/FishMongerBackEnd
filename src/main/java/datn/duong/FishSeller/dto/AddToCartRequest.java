package datn.duong.FishSeller.dto;

import lombok.Data;

// 3. DTO nhận dữ liệu từ Client (Thêm vào giỏ)
@Data
public class AddToCartRequest {
    private Long productId;
    private Integer quantity;
}
