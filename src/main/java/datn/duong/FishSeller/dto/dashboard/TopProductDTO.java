package datn.duong.FishSeller.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopProductDTO {
    private Long id;
    private String name;
    private String imageUrl;
    private Long totalSold; // Tổng số lượng đã bán
    private Double totalRevenue; // Tổng doanh thu từ sản phẩm này (tùy chọn)
}