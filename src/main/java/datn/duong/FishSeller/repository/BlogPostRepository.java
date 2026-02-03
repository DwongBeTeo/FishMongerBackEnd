package datn.duong.FishSeller.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import datn.duong.FishSeller.entity.BlogPostEntity;

public interface BlogPostRepository extends JpaRepository<BlogPostEntity, Long> {
    
    // --- Các hàm phục vụ cho BlogCategoryService ---

    // 1. Kiểm tra xem có bài viết nào đang HOẠT ĐỘNG thuộc danh mục này không (dùng để chặn ẩn danh mục)
    boolean existsByCategoryIdAndIsActiveTrue(Long categoryId);

    // 2. Kiểm tra xem có bất kỳ bài viết nào thuộc danh mục không (dùng để chặn xóa danh mục)
    boolean existsByCategoryId(Long categoryId);

    // --- Public Queries ---
    
    // Tìm bài viết theo danh mục (chỉ lấy bài đang hoạt động và chưa xóa)
    Page<BlogPostEntity> findAllByCategoryIdAndIsActiveTrueAndIsDeletedFalse(Long categoryId, Pageable pageable);

    // Tìm tất cả bài viết public (đang hoạt động và chưa xóa)
    Page<BlogPostEntity> findAllByIsActiveTrueAndIsDeletedFalse(Pageable pageable);

    // Tìm chi tiết bài viết qua Slug và đang hoạt động
    Optional<BlogPostEntity> findBySlugAndIsActiveTrue(String slug);

    // Lấy top 5 bài viết nổi bật hiển thị ở trang chủ
    List<BlogPostEntity> findTop5ByIsHomeTrueAndIsActiveTrueAndIsDeletedFalseOrderByCreatedDateDesc();


    // --- Admin Queries ---

    // Kiểm tra slug đã tồn tại chưa (để xử lý trùng lặp khi tạo mới)
    boolean existsBySlug(String slug);

    // Lấy tất cả bài viết chưa xóa (bao gồm cả bài ẩn/Active=false) cho Admin quản lý
    Page<BlogPostEntity> findAllByIsDeletedFalse(Pageable pageable);

    // Lấy danh sách bài viết trong "Thùng rác"
    Page<BlogPostEntity> findAllByIsDeletedTrue(Pageable pageable);
}