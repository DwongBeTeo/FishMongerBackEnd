package datn.duong.FishSeller.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

import datn.duong.FishSeller.dto.OrderDTO;

// DTO tổng hợp cho Dashboard
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatisticsDTO {
    private Double totalRevenueThisMonth; // Tổng doanh thu tháng này
    private Long totalOrdersThisMonth;    // Tổng số đơn hàng tháng này
    private Long totalAppointmentsThisMonth; // Tổng số lịch hẹn hoàn thành tháng này
    private Long totalCustomers;          // Tổng số khách hàng
    private List<DailyRevenueDTO> dailyRevenues; // List dữ liệu cho biểu đồ

    // 1. Tăng trưởng so với tháng trước
    private Double totalRevenueLastMonth;
    private Double growthRate; // Phần trăm tăng trưởng (VD: 15.5%)

    // 2. Biểu đồ tròn trạng thái đơn hàng
    private Map<String, Long> orderStatusCounts;

    // 3. Top sản phẩm bán chạy
    private List<TopProductDTO> topProducts;

    // 4. Đơn hàng gần đây
    private List<OrderDTO> recentOrders;
}