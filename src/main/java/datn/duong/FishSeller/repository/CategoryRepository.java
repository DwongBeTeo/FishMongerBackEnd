package datn.duong.FishSeller.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import datn.duong.FishSeller.entity.CategoryEntity;
import jakarta.transaction.Transactional;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    // select * from tbl_categories where name = ?
    Optional<CategoryEntity> findByName(String name);

    // select count(*) from tbl_categories where name = ?
    Boolean existsByName(String name);

    // select * from tbl_categories where id = ?
    Optional<CategoryEntity> findById(Long id);

    List<CategoryEntity> findByParentIsNotNullAndType(String type);

    // Hàm lấy tất cả cho Admin (Bỏ qua @Where/Soft Delete bằng nativeQuery)
    @Query(value = "SELECT * FROM categories ORDER BY id DESC", nativeQuery = true)
    List<CategoryEntity> findAllForAdminRaw();

    // Hàm tìm kiếm cho Admin (Tìm cả trong những cái đã xóa)
    @Query(value = "SELECT * FROM categories WHERE name LIKE %?1% ORDER BY id DESC", nativeQuery = true)
    List<CategoryEntity> searchForAdminRaw(String keyword);

    // THÊM MỚI: Tìm kiếm theo tên (không phân biệt hoa thường)
    List<CategoryEntity> findByNameContainingIgnoreCase(String name);
    
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, Long id); // Dùng khi Update (trùng slug nhưng không phải chính nó)

    // --- THÊM MỚI ĐỂ XỬ LÝ RESTORE ---
    
    // THÊM MỚI: Tìm kiếm ID bất kể trạng thái is_deleted (Dùng Native Query để bỏ qua @Where)
    @Query(value = "SELECT * FROM categories WHERE id = :id", nativeQuery = true)
    Optional<CategoryEntity> findByIdIncludingDeleted(@Param("id") Long id);

    // // 1. Hàm này update trực tiếp vào DB, bỏ qua @Where của Hibernate
    // @Modifying
    // @Transactional
    // @Query(value = "UPDATE categories SET is_deleted = false WHERE id = :id", nativeQuery = true)
    // void restoreCategoryById(Long id);

    // // 2. Hàm này kiểm tra tồn tại kể cả khi đã bị xóa mềm (để validate)
    // @Query(value = "SELECT count(*) FROM categories WHERE id = :id", nativeQuery = true)
    // boolean existsByIdInDatabase(Long id);
}
