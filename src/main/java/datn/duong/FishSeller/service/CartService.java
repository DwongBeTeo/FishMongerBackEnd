package datn.duong.FishSeller.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import datn.duong.FishSeller.dto.AddToCartRequest;
import datn.duong.FishSeller.dto.CartDTO;
import datn.duong.FishSeller.dto.CartItemDTO;
import datn.duong.FishSeller.entity.CartEntity;
import datn.duong.FishSeller.entity.CartItemEntity;
import datn.duong.FishSeller.entity.ProductEntity;
import datn.duong.FishSeller.entity.UserEntity;
import datn.duong.FishSeller.repository.CartItemRepository;
import datn.duong.FishSeller.repository.CartRepository;
import datn.duong.FishSeller.repository.ProductRepository;
import datn.duong.FishSeller.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final UserService userService;

    // Lấy giỏ hàng theo userId
    // @Transactional
    // public CartDTO getCartByUserId(Long userId) {
    //     CartEntity cart = getOrCreateCart(userId);
    //     return toDTO(cart);
    // }

    // 1. Lấy giỏ hàng của user hiện tại
    @Transactional(readOnly = true) // Tối ưu hiệu suất cho việc đọc dữ liệu
    public CartDTO getMyCart() {
        // Lấy user đang đăng nhập
        UserEntity currentUser = userService.getCurrentProfile();
        
        // Lấy hoặc tạo giỏ hàng nếu chưa có
        CartEntity cart = getOrCreateCart(currentUser);
        
        // Chuyển sang DTO và trả về
        return toDTO(cart);
    }

    // Thêm sản phẩm vào giỏ hàng
    public CartDTO addToCart(AddToCartRequest request) {
        UserEntity currentUser = userService.getCurrentProfile();
        CartEntity cart = getOrCreateCart(currentUser);
        ProductEntity product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        //1. Kiểm tra xem sản phẩm đã có trong giỏ chưa
        Optional<CartItemEntity> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        // 2. Tính toán tổng số lượng dự kiến
        int currentQuantityInCart = existingItemOpt.map(CartItemEntity::getQuantity).orElse(0);
        int quantityToAdd = request.getQuantity();
        int newQuantity = currentQuantityInCart + quantityToAdd;

        // 3. CHECK TỒN KHO (Logic thêm mới)
        if (newQuantity > product.getStockQuantity()) {
            throw new RuntimeException("Số lượng sản phẩm trong kho không đủ. Chỉ còn: " + product.getStockQuantity());
        }

        if (existingItemOpt.isPresent()) {
        // Cập nhật số lượng
        existingItemOpt.get().setQuantity(newQuantity);
        } else {
            // Tạo mới item
            CartItemEntity newItem = new CartItemEntity();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantityToAdd);
            cart.getItems().add(newItem);
        }

        // Save cart (cascade sẽ tự save items)
        CartEntity savedCart = cartRepository.save(cart);
        return toDTO(savedCart);
    }

    // update số lượng sản phẩm trong giỏ hàng
    @Transactional
    public CartDTO updateItemQuantity(Long cartItemId, Integer quantity) {
        // Lấy user hiện tại từ Security
        UserEntity user = userService.getCurrentProfile();
        CartEntity cart = getOrCreateCart(user);

        CartItemEntity item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found in cart"));

        if (quantity <= 0) {
            // Nếu số lượng <= 0 thì xóa luôn item đó
            cart.getItems().remove(item);
            cartItemRepository.delete(item); // Xóa cứng để đảm bảo DB đồng bộ
        } else {
            // QUAN TRỌNG: Kiểm tra tồn kho trước khi cập nhật
            if (quantity > item.getProduct().getStockQuantity()) {
                throw new RuntimeException("Sản phẩm không đủ hàng. Kho chỉ còn: " 
                                           + item.getProduct().getStockQuantity());
            }
            item.setQuantity(quantity);
        }

        CartEntity savedCart = cartRepository.save(cart);
        return toDTO(savedCart);
    }

    @Transactional
    public CartDTO removeFromCart(Long cartItemId) {
        UserEntity user = userService.getCurrentProfile();
        CartEntity cart = getOrCreateCart(user);

        // Dùng removeIf để xóa item khỏi list
        boolean removed = cart.getItems().removeIf(item -> item.getId().equals(cartItemId));

        if (!removed) {
            throw new RuntimeException("Item not found in cart to remove");
        }

        // Vì orphanRemoval=true, khi save Cart, JPA tự động xóa record bên bảng cart_items
        CartEntity savedCart = cartRepository.save(cart);
        return toDTO(savedCart);
    }

    // Xóa sản phẩm khỏi giỏ hàng
    @Transactional
    public void clearCart() {
        UserEntity user = userService.getCurrentProfile();
        CartEntity cart = getOrCreateCart(user);
        
        cart.getItems().clear(); // Xóa hết list
        cartRepository.save(cart); // JPA tự delete hết trong DB
    }

    // --- Private Helper Methods ---

    // Hàm lấy giỏ hàng, nếu chưa có thì tạo mới
    private CartEntity getOrCreateCart(UserEntity user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    System.out.println("--- ĐANG TẠO GIỎ HÀNG MỚI CHO USER: " + user.getUsername() + " ---");
                    CartEntity newCart = new CartEntity();
                    newCart.setUser(user);
                    newCart.setItems(new ArrayList<>());
                    return cartRepository.save(newCart);
                });
    }

    // --- MAPPING METHODS (Convert Data) ---

    // 1. Chuyển từ Entity sang DTO (Trả về Client)
    private CartDTO toDTO(CartEntity entity) {
        if (entity == null) return null;

        // Convert list items và tính tổng tiền
        List<CartItemDTO> itemDTOs = entity.getItems().stream()
                .map(this::toItemDTO)
                .collect(Collectors.toList());

        double totalAmount = itemDTOs.stream()
                .mapToDouble(CartItemDTO::getSubTotal)
                .sum();

        return CartDTO.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .items(itemDTOs)
                .totalAmount(totalAmount)
                .build();
    }

    // Helper convert từng item
    private CartItemDTO toItemDTO(CartItemEntity entity) {
        return CartItemDTO.builder()
                .id(entity.getId())
                .productId(entity.getProduct().getId())
                .productName(entity.getProduct().getName())
                .productImage(entity.getProduct().getImageUrl())
                .price(entity.getProduct().getPrice())
                .quantity(entity.getQuantity())
                .subTotal(entity.getProduct().getPrice() * entity.getQuantity())
                // Trả về tồn kho để FE giới hạn số lượng mua
                .productStock(entity.getProduct().getStockQuantity()) 
                .build();
    }

    // 2. Chuyển từ DTO sang Entity (Để lưu DB)
    // Lưu ý: Với Cart, thường chúng ta không map nguyên cục CartDTO to sang Entity
    // mà chỉ map từng phần nhỏ từ request (như AddToCartRequest) vào Entity có sẵn.
    // Tuy nhiên, đây là ví dụ nếu bạn muốn convert ngược một CartItemRequest thành CartItemEntity mới.
    
    // Ví dụ method này dùng để tạo mới Entity từ request add
    private CartItemEntity toEntity(AddToCartRequest request, CartEntity cart, ProductEntity product) {
        CartItemEntity entity = new CartItemEntity();
        entity.setCart(cart);
        entity.setProduct(product);
        entity.setQuantity(request.getQuantity());
        return entity;
    }
}
