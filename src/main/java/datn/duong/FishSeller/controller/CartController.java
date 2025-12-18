package datn.duong.FishSeller.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import datn.duong.FishSeller.dto.AddToCartRequest;
import datn.duong.FishSeller.dto.CartDTO;
import datn.duong.FishSeller.service.CartService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    // URL: http://localhost:8080/api/v1.0/cart
    @GetMapping
    public ResponseEntity<CartDTO> getMyCart() {
        return ResponseEntity.ok(cartService.getMyCart());
    }

    // ========================================================================
    // 2. THÊM SẢN PHẨM VÀO GIỎ
    // URL: http://localhost:8080/api/v1.0/admin/cart
    // {
    //    "productId": 1,
    //    "quantity": 2
    // }
    @PostMapping
    public ResponseEntity<CartDTO> addToCart(@RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(cartService.addToCart(request));
    }

    // 3. CẬP NHẬT SỐ LƯỢNG (Tăng/Giảm)
    // URL: http://localhost:8080/api/cart/items/{cartItemId}?quantity=5
    // Ví dụ: http://localhost:8080/api/cart/items/10?quantity=5
    // Note: cartItemId là ID của dòng trong giỏ hàng, không phải ID sản phẩm
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartDTO> updateItemQuantity(
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.updateItemQuantity(cartItemId, quantity));
    }

    // 4. XÓA 1 SẢN PHẨM KHỎI GIỎ
    // URL: http://localhost:8080/api/cart/items/{cartItemId}
    // Ví dụ: http://localhost:8080/api/cart/items/10
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<CartDTO> removeItem(@PathVariable Long cartItemId) {
        return ResponseEntity.ok(cartService.removeFromCart(cartItemId));
    }

    // 5. XÓA SẠCH GIỎ HÀNG
    // URL: http://localhost:8080/api/cart
    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart();
        return ResponseEntity.noContent().build();
    }
}
