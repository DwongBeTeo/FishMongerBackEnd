package datn.duong.FishSeller.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BlogPostDTO extends BaseDTO {

    // --- Thông tin chính ---
    private String title;             // Tiêu đề bài viết
    private String slug;              // URL (VD: cach-nuoi-ca-rong)
    private String thumbnail;         // Link ảnh đại diện (Hình 1)
    private String shortDescription;  // Mô tả ngắn (Sapo) (Hình 1)
    
    // Nội dung chi tiết (HTML) chứa cả chữ và các ảnh con (Hình 2)
    private String content;           

    // --- Thông tin phân loại & Tác giả ---
    private Long categoryId;          // INPUT: ID danh mục khi tạo/sửa
    private String categoryName;      // OUTPUT: Tên danh mục để hiển thị lên UI
    
    private Long createdBy;           // ID người tạo
    private String authorName;        // OUTPUT: Tên người tạo (map từ bảng User sang)

    // --- Chỉ số & Trạng thái ---
    private Integer viewCount;        // Số lượt xem
    private Boolean isHome;           // Có hiện ở trang chủ không?
    private Boolean isActive;         // Có đang hiển thị không?

    // --- SEO Fields ---
    private String metaTitle;
    private String metaKeyword;
    private String metaDescription;
}