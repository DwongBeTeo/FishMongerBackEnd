package datn.duong.FishSeller.repository;

import org.springframework.data.domain.Pageable;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import datn.duong.FishSeller.entity.ProductEntity;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    // PHẦN 1: DÀNH CHO GUEST/USER (Chỉ hiện Available)

    // find by id and status
    Optional<ProductEntity> findByIdAndStatus(Long id, String status);
    // 1. Lấy tất cả sản phẩm đang bán (Có phân trang)
    Page<ProductEntity> findByStatus(String status, Pageable pageable);

    // 2. Lấy sản phẩm theo Danh Mục + Đang bán
    // (Khách bấm vào danh mục "Cá Cảnh", chỉ hiện con nào đang bán)
    Page<ProductEntity> findByCategoryIdAndStatus(Long categoryId, String status, Pageable pageable);

    // 3. Tìm kiếm theo tên + Đang bán
    // (Khách search "Cá Koi", không được hiện sản phẩm đã ẩn)
    // ContainingIgnoreCase: Tìm gần đúng và không phân biệt hoa thường
    Page<ProductEntity> findByNameContainingIgnoreCaseAndStatus(String name, String status, Pageable pageable);

    // 4. (Nâng cao) Lọc theo khoảng giá + Đang bán
    // VD: Tìm cá từ 100k đến 500k
    Page<ProductEntity> findByPriceBetweenAndStatus(Double minPrice, Double maxPrice, String status, Pageable pageable);

    // PHẦN 2: DÀNH CHO ADMIN (Hiện tất cả, kể cả ẩn)
    // 5. Tìm kiếm cho Admin (Admin cần thấy cả sản phẩm đã ẩn để quản lý)
    Page<ProductEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // 6. find all
    Page<ProductEntity> findAll(Pageable pageable);

    // 7. find by id
    Optional <ProductEntity> findById(Long id);

    // 8. Hàm này xử lý tất cả các bộ lọc của Admin cùng lúc
    @Query("SELECT p FROM ProductEntity p WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:status IS NULL OR :status = '' OR p.status = :status) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<ProductEntity> searchAndFilterAdmin(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("status") String status,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable
    );

    // --- THÊM MỚI ---
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, Long id); // Check trùng khi update
    Optional<ProductEntity> findBySlugAndStatus(String slug, String status); // Tìm theo slug cho User
}
