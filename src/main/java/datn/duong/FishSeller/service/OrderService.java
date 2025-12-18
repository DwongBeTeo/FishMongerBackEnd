package datn.duong.FishSeller.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import datn.duong.FishSeller.dto.OrderCreationRequest;
import datn.duong.FishSeller.dto.OrderDTO;
import datn.duong.FishSeller.dto.OrderItemDTO;
import datn.duong.FishSeller.entity.CartEntity;
import datn.duong.FishSeller.entity.CartItemEntity;
import datn.duong.FishSeller.entity.OrderEntity;
import datn.duong.FishSeller.entity.OrderItemEntity;
import datn.duong.FishSeller.entity.ProductEntity;
import datn.duong.FishSeller.enums.OrderStatus;
import datn.duong.FishSeller.repository.CartRepository;
import datn.duong.FishSeller.repository.OrderRepository;
import datn.duong.FishSeller.repository.ProductRepository;
import datn.duong.FishSeller.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository; // Cần để trừ tồn kho
    private final UserRepository userRepository;


    // 1. Chức năng ĐẶT HÀNG (Checkout)
    @Transactional // Quan trọng: Nếu lỗi ở bất kỳ bước nào, rollback toàn bộ
    public OrderDTO placeOrder(Long userId, OrderCreationRequest request) {
        // B1: Lấy giỏ hàng của User
        CartEntity cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // B2: Tạo OrderEntity (chưa có items)
        OrderEntity newOrder = OrderEntity.builder()
                .user(cart.getUser())
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PENDING) // Mặc định là Chờ xử lý
                .shippingAddress(request.getShippingAddress())
                .phoneNumber(request.getPhoneNumber())
                .totalAmount(0.0) // Sẽ tính lại bên dưới
                .build();

        // B3: Duyệt qua CartItems để tạo OrderItems
        List<OrderItemEntity> orderItems = new ArrayList<>();
        double totalAmount = 0;

        for (CartItemEntity cartItem : cart.getItems()) {
            ProductEntity product = cartItem.getProduct();

            // Kiểm tra tồn kho
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Product " + product.getName() + " is out of stock");
            }

            // Trừ tồn kho
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            // Tạo OrderItem
            OrderItemEntity orderItem = OrderItemEntity.builder()
                    .order(newOrder)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .priceAtOrder(product.getPrice()) // LẤY GIÁ HIỆN TẠI CỦA PRODUCT
                    .build();

            orderItems.add(orderItem);
            totalAmount += (product.getPrice() * cartItem.getQuantity());
        }

        // B4: Hoàn thiện Order
        newOrder.setOrderItems(orderItems);
        newOrder.setTotalAmount(totalAmount);
        
        OrderEntity savedOrder = orderRepository.save(newOrder);

        // B5: Xóa sạch giỏ hàng sau khi đặt thành công
        cart.getItems().clear();
        cartRepository.save(cart);

        // B6: Trả về DTO
        return toDTO(savedOrder);
    }

    @Transactional // Rất quan trọng: Đảm bảo cả việc đổi trạng thái và cộng kho cùng thành công hoặc cùng thất bại
    public OrderDTO cancelOrder(Long orderId) {
        // 1. Tìm đơn hàng
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // 2. Kiểm tra trạng thái hợp lệ để hủy
        // Ví dụ: Chỉ cho hủy khi đơn hàng đang Chờ xử lý.
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Không thể hủy đơn hàng đã được duyệt hoặc đang giao.");
        }

        // 3. Hoàn lại tồn kho (Logic quan trọng nhất)
        List<OrderItemEntity> orderItems = order.getOrderItems();
        for (OrderItemEntity item : orderItems) {
            ProductEntity product = item.getProduct();
            
            // Cộng lại số lượng đã mua vào kho
            int currentStock = product.getStockQuantity();
            int quantityToRestore = item.getQuantity();
            product.setStockQuantity(currentStock + quantityToRestore);

            // Lưu lại Product (Nếu dùng Transactional, Hibernate sẽ tự dirty check, 
            // nhưng gọi save rõ ràng cũng tốt để dễ debug)
            productRepository.save(product);
        }

        // 4. Cập nhật trạng thái đơn hàng
        order.setStatus(OrderStatus.CANCELLED);
        OrderEntity cancelledOrder = orderRepository.save(order);

        return toDTO(cancelledOrder);
    }

    // --- MAPPING METHODS ---

    // Chuyển từ Entity sang DTO
    public OrderDTO toDTO(OrderEntity entity) {
        if (entity == null) return null;

        List<OrderItemDTO> itemDTOs = entity.getOrderItems().stream()
                .map(item -> OrderItemDTO.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .productImage(item.getProduct().getImageUrl())
                        .quantity(item.getQuantity())
                        .price(item.getPriceAtOrder()) // Giá lúc mua
                        .build())
                .collect(Collectors.toList());

        return OrderDTO.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .orderDate(entity.getOrderDate())
                .status(entity.getStatus().name())
                .totalAmount(entity.getTotalAmount())
                .shippingAddress(entity.getShippingAddress())
                .phoneNumber(entity.getPhoneNumber())
                .orderItems(itemDTOs)
                .build();
    }
    
    // Về hàm toEntity: 
    // Đối với Order, chúng ta KHÔNG BAO GIỜ convert trực tiếp từ 1 cục DTO sang Entity 
    // để lưu vào DB (trừ khi import dữ liệu cũ). 
    // Order phải được sinh ra từ logic checkout (như hàm placeOrder ở trên) để đảm bảo tính đúng đắn của giá tiền và tồn kho.
}
