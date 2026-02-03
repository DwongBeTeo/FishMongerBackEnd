package datn.duong.FishSeller.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import datn.duong.FishSeller.entity.BlogCategoryEntity;

public interface BlogCategoryRepository extends JpaRepository<BlogCategoryEntity, Long>{
    // Hàm này dành cho USER: Chỉ lấy danh mục Đang hiện và Chưa xóa
    List<BlogCategoryEntity> findAllByIsActiveTrueAndIsDeletedFalse();

    // Ham này dành cho ADMIN: Lấy tất cả danh mục chưa xóa
    List<BlogCategoryEntity> findAllByIsDeletedFalse();

    boolean existsBySlug(String slug);

    // Tìm danh mục theo slug (thường dùng cho phía người dùng hiển thị bài viết theo danh mục)
    Optional<BlogCategoryEntity> findBySlugAndIsActiveTrue(String slug);

    // Lấy các danh mục đã xóa mềm (soft delete)
    List<BlogCategoryEntity> findAllByIsDeletedTrue();
}
