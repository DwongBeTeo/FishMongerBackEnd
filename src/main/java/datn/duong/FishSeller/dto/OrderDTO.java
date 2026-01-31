package datn.duong.FishSeller.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.*;

// DTO trả về cho Client xem lịch sử
@Data
@Builder
public class OrderDTO {
    private Long id;
    private Long userId;
    private String userEmail;
    private LocalDateTime orderDate;
    private Double totalAmount;
    private String status;
    private String shippingAddress;
    private String phoneNumber;
    private List<OrderItemDTO> orderItems;
    private boolean cancellationRequested; // Để FE hiển thị thông báo "Đang chờ admin duyệt hủy"
    private String paymentMethod;
    private String paymentStatus;
    private Double discountAmount; // Tiền giảm
    private Double finalAmount;    // Tiền khách phải trả
    private String voucherCode;     // Mã voucher đã dùng
}
