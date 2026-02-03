package datn.duong.FishSeller.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BlogCategoryDTO extends BaseDTO {

    // Không cần id, createdDate, updatedDate vì đã có trong BaseDTO

    private String name;        // Tên danh mục (Required)
    private String slug;        // URL thân thiện (Backend tự sinh hoặc Frontend gửi)
    private String description; // Mô tả ngắn

    // --- SEO Fields ---
    private String metaTitle;
    private String metaKeyword;
    private String metaDescription;

    private Boolean isActive;   // Trạng thái hiển thị
    
    // (Optional) Số lượng bài viết trong danh mục này (để hiển thị thống kê)
    private Integer postCount;
}