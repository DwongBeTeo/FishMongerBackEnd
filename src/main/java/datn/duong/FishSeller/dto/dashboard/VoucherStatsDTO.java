package datn.duong.FishSeller.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VoucherStatsDTO {
    private String voucherCode;
    private Long totalUsed;         // Số lần đã sử dụng (đơn thành công)
    private Double totalRevenue;    // Tổng doanh thu thực tế (Final Amount)
    private Double totalDiscount;   // Tổng số tiền shop đã giảm cho khách
}