package datn.duong.FishSeller.service;

import java.text.Normalizer;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import datn.duong.FishSeller.dto.ProductDTO;
import datn.duong.FishSeller.entity.CategoryEntity;
import datn.duong.FishSeller.entity.ProductEntity;
import datn.duong.FishSeller.repository.CategoryRepository;
import datn.duong.FishSeller.repository.ProductRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    private final CategoryRepository categoryRepository;

    // =========================================================================
    // PHẦN 1: PUBLIC METHODS (Dành cho Guest & User - Chỉ hiện AVAILABLE)
    // =========================================================================

    // 1. Lấy tất cả sản phẩm đang bán (Phân trang)
    public Page<ProductDTO> getAllActiveProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        // Luôn truyền cứng status là "AVAILABLE" để khách không thấy hàng ẩn
        return productRepository.findByStatus("AVAILABLE", pageable)
                .map(this::toDTO);
    }

    // 2. Tìm kiếm sản phẩm theo tên (Search bar)
    public Page<ProductDTO> searchActiveProducts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByNameContainingIgnoreCaseAndStatus(keyword, "AVAILABLE", pageable)
                .map(this::toDTO);
    }

    // 3. Lấy sản phẩm theo Danh Mục (Khi user click vào menu Category)
    public Page<ProductDTO> getActiveProductsByCategory(Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByCategoryIdAndStatus(categoryId, "AVAILABLE", pageable)
                .map(this::toDTO);
    }

    // 4. Lọc theo khoảng giá (Filter)
    public Page<ProductDTO> filterActiveProductsByPrice(Double min, Double max, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByPriceBetweenAndStatus(min, max, "AVAILABLE", pageable)
                .map(this::toDTO);
    }

    // 5. Xem chi tiết 1 sản phẩm
    public ProductDTO getProductDetailByStatus(Long id, String status) {
        ProductEntity product = productRepository.findByIdAndStatus(id, "AVAILABLE")
                .orElseThrow(() -> new RuntimeException("Product not available"));
        return toDTO(product);
    }

    // [MỚI] 6. Xem chi tiết theo SLUG (Dùng cho trang chi tiết sản phẩm chuẩn SEO)
    public ProductDTO getProductDetailBySlug(String slug) {
        ProductEntity product = productRepository.findBySlugAndStatus(slug, "AVAILABLE")
                .orElseThrow(() -> new RuntimeException("Product not found (Slug: " + slug + ")"));
        return toDTO(product);
    }

    // =========================================================================
    // PHẦN 2: ADMIN METHODS (Quản lý - Xem hết, Thêm, Sửa, Xóa)
    // =========================================================================

    // 1. Lấy tất cả sản phẩm (Kể cả ẩn/hết hàng) để Admin quản lý
    public Page<ProductDTO> getAllProductsForAdmin(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findAll(pageable)
                .map(this::toDTO);
    }

    // 2. Tạo sản phẩm mới
    public ProductDTO createProduct(ProductDTO productDTO) {
        // 1. Xử lý Slug
        String slug = productDTO.getSlug();
        if (slug == null || slug.trim().isEmpty()) {
            slug = toSlug(productDTO.getName());
            System.out.println("Debuf: đã tự sinh Slug:" + slug);
        }else{
            System.out.println("DEBUG: Dùng slug người dùng gửi: " + slug);
        }
        // Check trùng slug
        if (productRepository.existsBySlug(slug)) {
            throw new RuntimeException("Slug '" + slug + "' đã tồn tại. Vui lòng đổi tên hoặc sửa slug.");
        }

        ProductEntity product = toEntity(productDTO);
        product.setSlug(slug);
        
        System.out.println("DEBUG: Slug trong Entity trước khi save: " + product.getSlug());

        // Mặc định khi tạo mới nếu không gửi status thì set là AVAILABLE
        if (product.getStatus() == null) {
            product.setStatus("AVAILABLE");
        }
        ProductEntity savedProduct = productRepository.save(product);
        return toDTO(savedProduct);
    }

    // 3. Cập nhật sản phẩm
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        ProductEntity existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found or not accessible"));

        // Update the product fields
        existingProduct.setName(productDTO.getName());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setStockQuantity(productDTO.getStockQuantity());
        existingProduct.setDescription(productDTO.getDescription());
        existingProduct.setImageUrl(productDTO.getImageUrl());

        // Update the status if provided
        existingProduct.setMetaTitle(productDTO.getMetaTitle());
        existingProduct.setMetaKeyword(productDTO.getMetaKeyword());

        // Update Slug logic
        String newSlug = productDTO.getSlug();
        if (newSlug == null || newSlug.trim().isEmpty()) {
            newSlug = toSlug(productDTO.getName());
        }
        // Nếu slug thay đổi thì mới check trùng
        if (!newSlug.equals(existingProduct.getSlug())) {
            if (productRepository.existsBySlugAndIdNot(newSlug, id)) {
                throw new RuntimeException("Slug '" + newSlug + "' đã tồn tại ở sản phẩm khác.");
            }
            existingProduct.setSlug(newSlug);
        }

        // Update the category if provided
        if (productDTO.getCategoryId() != null) {
            CategoryEntity category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category Id not found"));
            existingProduct.setCategory(category);
        }

        // Save the updated product
        ProductEntity updatedProduct = productRepository.save(existingProduct);
        return toDTO(updatedProduct);
    }

    // 4. Xóa sản phẩm
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found");
        }
        productRepository.deleteById(id);
    }

    // =========================================================================
    // PHẦN 3: HELPER METHODS (MAPPING)
    // =========================================================================

    // Hàm tạo Slug từ tên
    private String toSlug(String input) {
        if (input == null) return "";
        String nowhitespace = input.trim().replaceAll("\\s+", "-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("").toLowerCase();
        
    }
    public ProductDTO toDTO(ProductEntity entity) {
        return ProductDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .price(entity.getPrice())
                .stockQuantity(entity.getStockQuantity())
                .description(entity.getDescription())
                .imageUrl(entity.getImageUrl())
                .status(entity.getStatus())
                .slug(entity.getSlug())
                .metaTitle(entity.getMetaTitle())
                .metaKeyword(entity.getMetaKeyword())
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId() : null)
                .categoryName(entity.getCategory() != null ? entity.getCategory().getName() : null) // Tiện cho Frontend hiển thị
                .createdDate(entity.getCreatedDate())
                .updatedDate(entity.getUpdatedDate())
                .build();
    }

    public ProductEntity toEntity(ProductDTO dto) {
        CategoryEntity category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + dto.getCategoryId()));

        return ProductEntity.builder()
                .name(dto.getName())
                .price(dto.getPrice())
                .stockQuantity(dto.getStockQuantity())
                .description(dto.getDescription())
                .imageUrl(dto.getImageUrl())
                .status(dto.getStatus())
                // slug set riêngowr logic create/update
                .metaTitle(dto.getMetaTitle())
                .metaKeyword(dto.getMetaKeyword())
                .category(category)
                .build();
    }
}
