package datn.duong.FishSeller.dto;

import lombok.Data;

// DTO nhận yêu cầu đặt hàng (Checkout)
@Data
public class OrderCreationRequest {
    private String shippingAddress;
    private String phoneNumber;
    // Không cần list sản phẩm, vì sẽ lấy từ Cart của user hiện tại
}
