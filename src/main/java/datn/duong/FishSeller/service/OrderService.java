package datn.duong.FishSeller.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
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
import datn.duong.FishSeller.entity.UserEntity;
import datn.duong.FishSeller.enums.OrderStatus;
import datn.duong.FishSeller.enums.PaymentStatus;
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
    private final UserService userService;
    private final EmailService emailService;

    // ========================================================================
    // PHẦN DÀNH CHO ADMIN
    // ========================================================================

    // 1. ADMIN: Lấy tất cả đơn hàng trong hệ thống
    public List<OrderDTO> getAllOrders(String keyword) {
        List<OrderEntity> orders;
        if (keyword != null && !keyword.trim().isEmpty()) {
            orders = orderRepository.findByUser_EmailContainingIgnoreCaseOrderByOrderDateDesc(keyword.trim());
        }else{
            // findAll() lấy tất cả không phân biệt user
            orders = orderRepository.findAll(Sort.by(Sort.Direction.DESC, "orderDate"));
        }
        
        return orders.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // 2. ADMIN: Cập nhật trạng thái đơn hàng (Duyệt đơn, Giao hàng, Hủy đơn...)
    // có thể cưỡng chế hủy đơn hàng nếu hết hàng
    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, OrderStatus newStatus) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.COMPLETED) {
            throw new RuntimeException("Không thể thay đổi trạng thái đơn hàng đã Hoàn thành hoặc đã Hủy.");
        }

        // LOGIC 1: Nếu Admin chuyển sang trạng thái CANCELLED (Hủy)
        // -> Phải hoàn lại tồn kho (Giống hệt logic user tự hủy)
        if (newStatus == OrderStatus.CANCELLED) {
            restoreStock(order);
            order.setCancellationRequested(false); // Reset cờ yêu cầu (nếu có) vì đơn đã hủy rồi
            // Gửi email thông báo
            sendNotificationToUser(
                order.getUser(), 
                "Thông báo hủy đơn hàng #" + order.getId(),
                "Đơn hàng <b>#" + order.getId() + "</b> đã bị hủy bởi Admin vì lý do hết hàng hoặc sự cố vận hành."
            );
        }
        // LOGIC 3: tư động cập nhật trạng thái thanh toán Thông báo khi đơn hàng Giao thành công 
        if (newStatus == OrderStatus.COMPLETED) {
            if(order.getPaymentStatus()==PaymentStatus.UNPAID){
                order.setPaymentStatus(PaymentStatus.PAID);
            }

             sendNotificationToUser(
                order.getUser(),
                "Đơn hàng #" + order.getId() + " đã hoàn thành",
                "Cảm ơn bạn đã mua sắm tại Cá Cảnh Shop. Đơn hàng <b>#" + order.getId() + "</b> đã được giao thành công."
            );
        }

        order.setStatus(newStatus);
        OrderEntity savedOrder = orderRepository.save(order);
        return toDTO(savedOrder);
    }

    // 3. ADMIN:XỬ LÝ YÊU CẦU HỦY (DUYỆT HOẶC TỪ CHỐI)
    @Transactional
    public OrderDTO handleCancellationRequest(Long orderId, boolean approve, String reason) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.isCancellationRequested()) {
            throw new RuntimeException("Đơn hàng này không có yêu cầu hủy từ khách.");
        }

        if (approve) {
            // ADMIN ĐỒNG Ý HỦY
            restoreStock(order); // Hoàn kho
            order.setStatus(OrderStatus.CANCELLED);
            // Gửi mail đồng ý
            sendNotificationToUser(
                order.getUser(),
                "Xác nhận hủy đơn hàng #" + order.getId(),
                "Yêu cầu hủy đơn hàng <b>#" + order.getId() + "</b> của bạn đã được CHẤP NHẬN."
            );
        } else {
            // B. ADMIN TỪ CHỐI
            String msg = "Yêu cầu hủy đơn hàng <b>#" + order.getId() + "</b> đã bị TỪ CHỐI.";
            if (reason != null && !reason.isEmpty()) {
                msg += "<br/><b>Lý do:</b> " + reason;
            }
            // Gửi mail từ chối kèm lý do
            sendNotificationToUser(
                order.getUser(),
                "Yêu cầu hủy đơn hàng #" + order.getId() + " bị từ chối",
                msg
            );
        }

        // Dù đồng ý hay từ chối thì cũng reset cờ này về false (đã xử lý xong)
        order.setCancellationRequested(false); 
        
        return toDTO(orderRepository.save(order));
    }

    // 4: ADMIN: xem chi tiết đơn hàng
    public OrderDTO getOrderByIdForAdmin(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return toDTO(order);
    }

    // ========================================================================
    // PHẦN DÀNH CHO USER
    // ========================================================================
    // 1. Chức năng ĐẶT HÀNG (Checkout)
    @Transactional // Quan trọng: Nếu lỗi ở bất kỳ bước nào, rollback toàn bộ
    public OrderDTO placeOrder(OrderCreationRequest request) {
        // Lấy user hien tai
        UserEntity user = userService.getCurrentProfile();

        // --- 1. VALIDATE PHONE NUMBER (MỚI) ---
        if (!AddressService.isValidPhoneNumber(request.getPhoneNumber())) {
            throw new RuntimeException("Số điện thoại nhận hàng không hợp lệ!");
        }

        // B1: Lấy giỏ hàng của User
        CartEntity cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // B2: Tạo OrderEntity (chưa có items)
        OrderEntity newOrder = OrderEntity.builder()
                .user(user)
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PENDING) // Mặc định là Chờ xử lý
                .shippingAddress(request.getShippingAddress())
                .phoneNumber(request.getPhoneNumber())
                .paymentMethod(request.getPaymentMethod()) 
                // Mặc định mới đặt là Chưa thanh toán (kể cả Banking cũng cần chờ check)
                .paymentStatus(PaymentStatus.UNPAID)
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

    // 2. HỦY ĐƠN HÀNG
    @Transactional // Rất quan trọng: Đảm bảo cả việc đổi trạng thái và cộng kho cùng thành công hoặc cùng thất bại
    public OrderDTO requestCancelOrder(Long orderId) {
        // Lưu ý nhỏ: Nên kiểm tra xem order này có phải của user đang đăng nhập không
       // để tránh ông A hủy đơn của ông B.
       UserEntity currentUser = userService.getCurrentProfile();
        // 2.1. Tìm đơn hàng
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // 2.2. Kiểm tra trạng thái hợp lệ để hủy
        // Ví dụ: Chỉ cho hủy khi đơn hàng đang Chờ xử lý.
        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền để hủy đơn hàng này!");
        }

        //Case1: pending cho phép hủy luôn 
        // if (order.getStatus() == OrderStatus.PENDING) {
        //     restoreStock(order);
        //     order.setStatus(OrderStatus.CANCELLED);
        //     return toDTO(orderRepository.save(order));
        // }

        // Case2: Đang chuẩn bị hoặc đang giao -> Gửi yêu cầu hủy
        if(order.getStatus() == OrderStatus.PREPARING || order.getStatus() == OrderStatus.SHIPPING || order.getStatus() == OrderStatus.PENDING) {
            order.setCancellationRequested(true);// Bật cờ
            return toDTO(orderRepository.save(order));
        }
        restoreStock(order);

        //Case 3: Đã xong hoặc đã hủy -> Lỗi
        throw new RuntimeException("Không thể hủy đơn hàng đã hoàn thành hoặc đã bị hủy.");
    }

    // 3. XEM DANH SÁCH ĐƠN HÀNG CỦA TÔI
    public List<OrderDTO> getMyOrders() {
        UserEntity currentUser = userService.getCurrentProfile();
        List<OrderEntity> orders = orderRepository.findByUserId(currentUser.getId());
        
        return orders.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // 4. XEM CHI TIẾT 1 ĐƠN HÀNG
    public OrderDTO getOrderById(Long orderId) {
        UserEntity currentUser = userService.getCurrentProfile();
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Bảo mật: Chỉ xem được đơn của chính mình
        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Access denied");
        }

        return toDTO(order);
    }

    // --- MAPPING METHODS ---

    // Hàm phụ trợ: Hoàn lại số lượng tồn kho
    private void restoreStock(OrderEntity order) {
        List<OrderItemEntity> orderItems = order.getOrderItems();
        for (OrderItemEntity item : orderItems) {
            ProductEntity product = item.getProduct();
            
            // Tính toán số lượng mới
            int currentStock = product.getStockQuantity();
            int quantityToRestore = item.getQuantity();
            
            // Cập nhật và lưu
            product.setStockQuantity(currentStock + quantityToRestore);
            productRepository.save(product);
        }
    }

    // --- HÀM GỬI EMAIL THỰC TẾ ---
    private void sendNotificationToUser(UserEntity user, String subject, String messageContent) {
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            return; // Bỏ qua nếu user không có email
        }

        // Tạo nội dung HTML đơn giản
        String htmlBody = "" +
                "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #ddd; border-radius: 5px;'>" +
                "   <h2 style='color: #0891b2;'>Cá Cảnh Shop Thông Báo</h2>" +
                "   <p>Xin chào <b>" + user.getUsername() + "</b>,</p>" +
                "   <p>" + messageContent + "</p>" +
                "   <hr style='border: 0; border-top: 1px solid #eee;' />" +
                "   <p style='font-size: 12px; color: #777;'>Đây là email tự động, vui lòng không trả lời email này.</p>" +
                "</div>";

        // QUAN TRỌNG: Chạy Async để không làm Admin phải chờ gửi mail xong mới thấy phản hồi
        CompletableFuture.runAsync(() -> {
            try {
                emailService.sendEmail(user.getEmail(), subject, htmlBody);
            } catch (Exception e) {
                // Chỉ log lỗi, không throw exception làm ảnh hưởng luồng chính
                System.err.println("Lỗi gửi email background: " + e.getMessage());
            }
        });
    }

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
                .userEmail(entity.getUser().getEmail())
                .orderDate(entity.getOrderDate())
                .status(entity.getStatus().name())
                .totalAmount(entity.getTotalAmount())
                .shippingAddress(entity.getShippingAddress())
                .phoneNumber(entity.getPhoneNumber())
                .paymentMethod(entity.getPaymentMethod() != null ? entity.getPaymentMethod().name() : null)
                .paymentStatus(entity.getPaymentStatus() != null ? entity.getPaymentStatus().name() : null)
                .orderItems(itemDTOs)
                .cancellationRequested(entity.isCancellationRequested())
                .build();
    }
    
    // Về hàm toEntity: 
    // Đối với Order, chúng ta KHÔNG BAO GIỜ convert trực tiếp từ 1 cục DTO sang Entity 
    // để lưu vào DB (trừ khi import dữ liệu cũ). 
    // Order phải được sinh ra từ logic checkout (như hàm placeOrder ở trên) để đảm bảo tính đúng đắn của giá tiền và tồn kho.
}
