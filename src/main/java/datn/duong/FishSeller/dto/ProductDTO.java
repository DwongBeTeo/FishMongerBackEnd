package datn.duong.FishSeller.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ProductDTO extends BaseDTO {
    // private Long id;
    private String name;
    private Double price;
    private Integer stockQuantity;
    private String description;
    private String imageUrl;
    private String status;
    private String slug;
    private String metaTitle;
    private String metaKeyword;

    // Chỉ cần ID của danh mục để xử lý logic
    private Long categoryId; 
    
    // Thêm tên danh mục để hiển thị ra Frontend cho tiện (Optional)
    private String categoryName;
}
