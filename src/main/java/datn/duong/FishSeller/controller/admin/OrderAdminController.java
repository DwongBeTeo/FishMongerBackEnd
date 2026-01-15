package datn.duong.FishSeller.controller.admin;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import datn.duong.FishSeller.dto.OrderDTO;
import datn.duong.FishSeller.enums.OrderStatus;
import datn.duong.FishSeller.service.OrderService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/order")
@RequiredArgsConstructor
public class OrderAdminController {

    private final OrderService orderService;

    // 1. Xem tất cả đơn hàng
    // GET:
    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrders(
        @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(orderService.getAllOrders(keyword));
    }
 
    // 2. Admin cập nhật trạng thái (Dùng để DUYỆT hoặc HỦY đơn khách)
    // PUT: /{orderId}/status?status=CANCELLED
    // status có thể là: SHIPPING, COMPLETED, CANCELLED
    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long orderId, 
            @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status));
    }

    // 3.Admin Duyệt/Từ chối yêu cầu hủy
    // PUT: /api/orders/admin/{orderId}/review-cancel?approve=true
    // Hoặc: /api/admin/orders/{orderId}/review-cancel?approve=false&reason=Hang da giao cho shipper
    @PutMapping("{orderId}/review-cancel")
    public ResponseEntity<OrderDTO> reviewCancellation(
            @PathVariable Long orderId,
            @RequestParam boolean approve,
            @RequestParam(required = false) String reason // Lý do từ chối (nếu có)
        ) {
        return ResponseEntity.ok(orderService.handleCancellationRequest(orderId, approve, reason));
    }

    // 4. Xem chi tiết đơn hàng (Dành cho Admin)
    // GET: /api/v1.0/admin/orders/{orderId}
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrderByIdForAdmin(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderByIdForAdmin(orderId));
    }
}
