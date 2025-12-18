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
    private LocalDateTime orderDate;
    private Double totalAmount;
    private String status;
    private String shippingAddress;
    private String phoneNumber;
    private List<OrderItemDTO> orderItems;
}
