package datn.duong.FishSeller.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import datn.duong.FishSeller.dto.OrderDTO;
import datn.duong.FishSeller.dto.dashboard.DailyRevenueDTO;
import datn.duong.FishSeller.dto.dashboard.DashboardStatisticsDTO;
import datn.duong.FishSeller.dto.dashboard.TopProductDTO;
import datn.duong.FishSeller.repository.AppointmentRepository;
import datn.duong.FishSeller.repository.OrderItemRepository;
import datn.duong.FishSeller.repository.OrderRepository;
import datn.duong.FishSeller.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrderRepository orderRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderService orderService;

    public DashboardStatisticsDTO getDashboardStats() {
        // 1. Chuẩn bị khung thời gian (Time Range)
        LocalDate today = LocalDate.now();
        
        // Tháng này (This Month)
        LocalDate startMonthDate = today.withDayOfMonth(1);
        LocalDate endMonthDate = today.withDayOfMonth(today.lengthOfMonth());
        LocalDateTime startMonthTime = startMonthDate.atStartOfDay();
        LocalDateTime endMonthTime = endMonthDate.atTime(23, 59, 59);

        // Tháng trước (Last Month)
        LocalDate startLastMonthDate = startMonthDate.minusMonths(1);
        LocalDate endLastMonthDate = endMonthDate.minusMonths(1);
        LocalDateTime startLastMonthTime = startLastMonthDate.atStartOfDay();
        LocalDateTime endLastMonthTime = endLastMonthDate.atTime(23, 59, 59);

        // 2. Tính toán doanh thu
        Double revenueThisMonth = calculateTotalRevenue(startMonthTime, endMonthTime, startMonthDate, endMonthDate);
        Double revenueLastMonth = calculateTotalRevenue(startLastMonthTime, endLastMonthTime, startLastMonthDate, endLastMonthDate);

        // 3. Tính % Tăng trưởng (Growth Rate)
        Double growthRate = calculateGrowthRate(revenueThisMonth, revenueLastMonth);

        // 4. Lấy các chỉ số đếm (Counts)
        Long totalOrders = orderRepository.countOrdersByDateRange(startMonthTime, endMonthTime);
        Long totalAppts = appointmentRepository.countAppointmentsByDateRange(startMonthDate, endMonthDate);
        Long totalUsers = userRepository.count();

        // 5. Xử lý dữ liệu biểu đồ (Daily Chart)
        List<DailyRevenueDTO> dailyStats = getDailyRevenueChart(startMonthTime, endMonthTime, startMonthDate, endMonthDate);

        // 6. Lấy các dữ liệu phụ (Pie Chart, Top Products, Recent Orders)
        Map<String, Long> orderStatusCounts = getOrderStatusCounts();
        List<TopProductDTO> topProducts = orderItemRepository.findTopSellingProducts(PageRequest.of(0, 5));
        List<OrderDTO> recentOrders = getRecentOrders();

        // 7. Đóng gói kết quả (Builder)
        return DashboardStatisticsDTO.builder()
                .totalRevenueThisMonth(revenueThisMonth)
                .totalRevenueLastMonth(revenueLastMonth)
                .growthRate(growthRate)
                .totalOrdersThisMonth(totalOrders)
                .totalAppointmentsThisMonth(totalAppts)
                .totalCustomers(totalUsers)
                .dailyRevenues(dailyStats)
                .orderStatusCounts(orderStatusCounts)
                .topProducts(topProducts)
                .recentOrders(recentOrders)
                .build();
    }

    // =========================================================================
    // PRIVATE HELPER METHODS
    // =========================================================================

    /**
     * Tính tổng doanh thu (Order + Appointment) trong 1 khoảng thời gian
     */
    private Double calculateTotalRevenue(LocalDateTime startTime, LocalDateTime endTime, LocalDate startDate, LocalDate endDate) {
        Double orderRev = orderRepository.sumRevenueByDateRange(startTime, endTime);
        Double apptRev = appointmentRepository.sumRevenueByDateRange(startDate, endDate);
        
        return (orderRev != null ? orderRev : 0.0) + (apptRev != null ? apptRev : 0.0);
    }

    /**
     * Logic tính phần trăm tăng trưởng
     */
    private Double calculateGrowthRate(Double current, Double previous) {
        if (previous > 0) {
            double rate = ((current - previous) / previous) * 100;
            return Math.round(rate * 100.0) / 100.0; // Làm tròn 2 số thập phân
        }
        return current > 0 ? 100.0 : 0.0;
    }

    /**
     * Xử lý dữ liệu biểu đồ doanh thu theo ngày
     */
    private List<DailyRevenueDTO> getDailyRevenueChart(LocalDateTime startTime, LocalDateTime endTime, LocalDate startDate, LocalDate endDate) {
        // Lấy dữ liệu thô từ DB
        List<DailyRevenueDTO> orderDaily = mapToDTO(orderRepository.getDailyRevenue(startTime, endTime));
        List<DailyRevenueDTO> apptDaily = mapToDTO(appointmentRepository.getDailyRevenue(startDate, endDate));

        // Merge dữ liệu vào Map để cộng dồn theo ngày
        Map<LocalDate, Double> revenueMap = new TreeMap<>();
        
        // Khởi tạo tất cả các ngày trong tháng bằng 0 (để biểu đồ không bị gãy khúc)
        for (LocalDate date = startDate; !date.isAfter(LocalDate.now()) && !date.isAfter(endDate); date = date.plusDays(1)) {
            revenueMap.put(date, 0.0);
        }

        // Cộng dồn doanh thu
        for (DailyRevenueDTO item : orderDaily) revenueMap.merge(item.getDate(), item.getRevenue(), Double::sum);
        for (DailyRevenueDTO item : apptDaily) revenueMap.merge(item.getDate(), item.getRevenue(), Double::sum);

        return revenueMap.entrySet().stream()
                .map(entry -> new DailyRevenueDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Map dữ liệu biểu đồ tròn
     */
    private Map<String, Long> getOrderStatusCounts() {
        List<Object[]> counts = orderRepository.countOrdersByStatus();
        Map<String, Long> map = new HashMap<>();
        if (counts != null) {
            for (Object[] row : counts) {
                map.put(row[0].toString(), (Long) row[1]);
            }
        }
        return map;
    }

    /**
     * Lấy danh sách đơn hàng gần đây
     */
    private List<OrderDTO> getRecentOrders() {
        return orderRepository.findAll(PageRequest.of(0, 5, Sort.by("orderDate").descending()))
                .stream()
                .map(orderService::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Helper chuyển đổi dữ liệu thô từ Native Query sang DTO
     */
    private List<DailyRevenueDTO> mapToDTO(List<Object[]> rawData) {
        if (rawData == null) return new ArrayList<>();
        return rawData.stream().map(row -> {
            java.sql.Date sqlDate = (java.sql.Date) row[0];
            Double revenue = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
            return new DailyRevenueDTO(sqlDate.toLocalDate(), revenue);
        }).collect(Collectors.toList());
    }
}