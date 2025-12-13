package datn.duong.FishSeller.controller.admin;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import datn.duong.FishSeller.dto.ProductDTO;
import datn.duong.FishSeller.service.ProductService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class ProductAdminController {

    private final ProductService productService;

    // 1. Lấy tất cả sản phẩm (Cho trang quản lý admin - xem cả hàng ẩn)
    // URL: GET /api/v1.0/admin/products
    @GetMapping
    public ResponseEntity<Page<ProductDTO>> getAllProductsForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(productService.getAllProductsForAdmin(page, size));
    }

    // 2. Thêm mới sản phẩm
    // URL: POST /api/v1.0/admin/products
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct( @RequestBody ProductDTO productDTO) {
        ProductDTO newProduct = productService.createProduct(productDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newProduct);
    }

    // 3. Cập nhật sản phẩm (Sửa giá, tồn kho, ẩn/hiện...)
    // URL: PUT /api/v1.0/admin/products/5
    @PutMapping("/{productId}")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long ProductId,
            @RequestBody ProductDTO productDTO
    ) {
        ProductDTO updatedProduct = productService.updateProduct(ProductId, productDTO);
        return ResponseEntity.ok(updatedProduct);
    }

    // 4. Xóa sản phẩm
    // URL: DELETE /api/v1.0/admin/products/5
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
