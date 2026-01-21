package datn.duong.FishSeller.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
}