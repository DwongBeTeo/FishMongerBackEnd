package datn.duong.FishSeller.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import datn.duong.FishSeller.dto.ProductDTO;
import datn.duong.FishSeller.service.ProductService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;

    // 1. Xem danh sách sản phẩm (Mặc định trang 0, 10 sản phẩm/trang)
    // URL: GET /api/v1.0/products?page=0&size=10
    @GetMapping
    public ResponseEntity<Page<ProductDTO>> getAllActiveProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(productService.getAllActiveProducts(page, size));
    }

    // 2. Xem chi tiết 1 sản phẩm
    // URL: GET /api/v1.0/products/5
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductDetail(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductDetail(id));
    }

    // 3. Tìm kiếm sản phẩm
    // URL: GET /api/v1.0/products/search?keyword=koi&page=0&size=10
    @GetMapping("/search")
    public ResponseEntity<Page<ProductDTO>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(productService.searchActiveProducts(keyword, page, size));
    }

    // 4. Lọc sản phẩm theo Danh Mục
    // URL: GET /api/v1.0/products/category/2?page=0&size=10
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductDTO>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(productService.getActiveProductsByCategory(categoryId, page, size));
    }

    // 5. Lọc theo khoảng giá
    // URL: GET /api/v1.0/products/filter?min=100000&max=500000
    @GetMapping("/filter")
    public ResponseEntity<Page<ProductDTO>> filterByPrice(
            @RequestParam Double min,
            @RequestParam Double max,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(productService.filterActiveProductsByPrice(min, max, page, size));
    }
}
