package datn.duong.FishSeller.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private Long id;
    private String name;
    private Double price;
    private Integer stockQuantity;
    private String description;
    private String imageUrl;
    private String status;

    // Chỉ cần ID của danh mục để xử lý logic
    private Long categoryId; 
    
    // Thêm tên danh mục để hiển thị ra Frontend cho tiện (Optional)
    private String categoryName;
}
