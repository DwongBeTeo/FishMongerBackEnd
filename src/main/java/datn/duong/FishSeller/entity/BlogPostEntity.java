package datn.duong.FishSeller.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "blog_posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BlogPostEntity extends BaseEntity {

    @Column(nullable = false, length = 500)
    private String title; // Tiêu đề bài viết

    @Column(unique = true, nullable = false)
    private String slug; // URL (VD: top-5-loai-ca-rong)

    // Ảnh đại diện (Thumbnail) hiển thị ở danh sách bên ngoài (Hình 1)
    @Column(name = "thumbnail_url")
    private String thumbnail; 

    // Mô tả ngắn (Sapô) hiển thị dưới tiêu đề ở danh sách (Hình 1)
    @Column(columnDefinition = "TEXT", name = "short_description")
    private String shortDescription;

    // Nội dung chi tiết (HTML) chứa chữ và các ảnh con xen kẽ (Hình 2)
    @Lob // Báo hiệu đây là dữ liệu lớn
    @Column(columnDefinition = "LONGTEXT") // MySQL dùng LONGTEXT để lưu bài dài
    private String content;

    // --- Thông tin Audit (Người tạo/Sửa) ---
    // Bạn nên lưu ID của User, sau này join bảng User để lấy tên
    @Column(name = "created_by")
    private Long createdBy; 

    @Column(name = "updated_by")
    private Long updatedBy;
    
    // Tên người duyệt bài (nếu cần như trong Excel)
    @Column(name = "approver_by")
    private Long approverBy;

    // --- Thống kê ---
    @Builder.Default
    @Column(name = "view_count")
    private Integer viewCount = 0; // Đếm lượt xem

    // --- SEO Fields ---
    @Column(name = "meta_title")
    private String metaTitle;

    @Column(name = "meta_keyword")
    private String metaKeyword;

    @Column(name = "meta_description")
    private String metaDescription;

    // --- Soft Delete & Status ---
    @Builder.Default
    @Column(name = "is_deleted")
    private Boolean isDeleted = false; // Xóa mềm

    @Builder.Default
    @Column(name = "is_active") // Hiển thị lên trang chủ hay không
    private Boolean isActive = true;
    
    @Builder.Default
    @Column(name = "is_home") // Có hiện ở mục Hot trang chủ không (cột IS_HOME trong excel)
    private Boolean isHome = false;

    // --- Quan hệ ---
    @ManyToOne
    @JoinColumn(name = "category_id")
    private BlogCategoryEntity category;
}