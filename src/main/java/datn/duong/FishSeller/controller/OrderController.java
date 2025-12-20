package datn.duong.FishSeller.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import datn.duong.FishSeller.dto.OrderCreationRequest;
import datn.duong.FishSeller.dto.OrderDTO;
import datn.duong.FishSeller.service.OrderService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    // 1. Đặt hàng (Checkout)
    // POST: /orders/checkout
    @PostMapping("/checkout")
    public ResponseEntity<OrderDTO> placeOrder(@RequestBody OrderCreationRequest request) {
        return ResponseEntity.ok(orderService.placeOrder(request));
    }

    // 2. Xem danh sách đơn hàng của tôi
    // GET: /orders
    @GetMapping
    public ResponseEntity<List<OrderDTO>> getMyOrders() {
        return ResponseEntity.ok(orderService.getMyOrders());
    }

    // 3. Xem chi tiết 1 đơn hàng
    // GET: /orders/{orderId}
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    // 4. Hủy đơn hàng
    // PUT: /orders/{orderId}/cancel
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDTO> reviewCancellation(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.requestCancelOrder(orderId));
    }
}
