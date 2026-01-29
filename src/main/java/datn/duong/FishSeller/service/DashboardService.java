package datn.duong.FishSeller.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final OrderItemRepository orderItemRepository; // Để lấy Top sản phẩm
    private final OrderService orderService; // Để dùng hàm toDTO cho Recent Orders
    public DashboardStatisticsDTO getDashboardStats() {
        // =========================================================================
        // 1. XÁC ĐỊNH THỜI GIAN (THÁNG NÀY & THÁNG TRƯỚC)
        // =========================================================================
        LocalDate today = LocalDate.now();
        
        // Tháng này
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDate lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth());
        LocalDateTime startDateTime = firstDayOfMonth.atStartOfDay();
        LocalDateTime endDateTime = lastDayOfMonth.atTime(23, 59, 59);

        // Tháng trước (Để tính tăng trưởng) -> MỚI THÊM
        LocalDate firstDayLastMonth = firstDayOfMonth.minusMonths(1);
        LocalDate lastDayLastMonth = lastDayOfMonth.minusMonths(1);
        LocalDateTime startLastMonth = firstDayLastMonth.atStartOfDay();
        LocalDateTime endLastMonth = lastDayLastMonth.atTime(23, 59, 59);

        // =========================================================================
        // 2. SỐ LIỆU TỔNG QUAN (DOANH THU & SỐ LƯỢNG)
        // =========================================================================
        
        // --- A. Doanh thu Tháng Này (Logic cũ của bạn) ---
        Double orderRevenue = orderRepository.sumRevenueByDateRange(startDateTime, endDateTime);
        Double apptRevenue = appointmentRepository.sumRevenueByDateRange(firstDayOfMonth, lastDayOfMonth);
        
        orderRevenue = (orderRevenue != null) ? orderRevenue : 0.0;
        apptRevenue = (apptRevenue != null) ? apptRevenue : 0.0;
        Double totalRevenueThisMonth = orderRevenue + apptRevenue;

        // --- B. Doanh thu Tháng Trước (Logic MỚI để tính Growth Rate) ---
        Double orderRevLast = orderRepository.sumRevenueByDateRange(startLastMonth, endLastMonth);
        Double apptRevLast = appointmentRepository.sumRevenueByDateRange(firstDayLastMonth, lastDayLastMonth);
        
        double totalRevenueLastMonth = (orderRevLast != null ? orderRevLast : 0.0) + 
                                       (apptRevLast != null ? apptRevLast : 0.0);

        // --- C. Tính % Tăng trưởng ---
        double growthRate = 0.0;
        if (totalRevenueLastMonth > 0) {
            growthRate = ((totalRevenueThisMonth - totalRevenueLastMonth) / totalRevenueLastMonth) * 100;
        } else if (totalRevenueThisMonth > 0) {
            growthRate = 100.0; // Tăng trưởng tuyệt đối từ 0
        }

        // --- D. Các chỉ số đếm (Logic cũ của bạn) ---
        Long totalOrders = orderRepository.countOrdersByDateRange(startDateTime, endDateTime);
        Long totalAppts = appointmentRepository.countAppointmentsByDateRange(firstDayOfMonth, lastDayOfMonth);
        Long totalUsers = userRepository.count();

        // =========================================================================
        // 3. BIỂU ĐỒ DOANH THU THEO NGÀY (Logic cũ của bạn - Giữ nguyên)
        // =========================================================================
        List<Object[]> orderRaw = orderRepository.getDailyRevenue(startDateTime, endDateTime);
        List<Object[]> apptRaw = appointmentRepository.getDailyRevenue(firstDayOfMonth, lastDayOfMonth);

        List<DailyRevenueDTO> orderDaily = mapToDTO(orderRaw);
        List<DailyRevenueDTO> apptDaily = mapToDTO(apptRaw);

        Map<LocalDate, Double> revenueMap = new TreeMap<>();
        for (LocalDate date = firstDayOfMonth; !date.isAfter(today); date = date.plusDays(1)) {
            revenueMap.put(date, 0.0);
        }
        for (DailyRevenueDTO item : orderDaily) revenueMap.merge(item.getDate(), item.getRevenue(), Double::sum);
        for (DailyRevenueDTO item : apptDaily) revenueMap.merge(item.getDate(), item.getRevenue(), Double::sum);

        List<DailyRevenueDTO> finalDailyStats = revenueMap.entrySet().stream()
                .map(entry -> new DailyRevenueDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        // =========================================================================
        // 4. CÁC TÍNH NĂNG MỚI (PIE CHART, TOP PRODUCTS, RECENT ORDERS)
        // =========================================================================

        // --- E. Pie Chart (Trạng thái đơn hàng) ---
        List<Object[]> statusCounts = orderRepository.countOrdersByStatus();
        Map<String, Long> statusMap = new HashMap<>();
        if (statusCounts != null) {
            for (Object[] row : statusCounts) {
                statusMap.put(row[0].toString(), (Long) row[1]);
            }
        }

        // --- F. Top Sản phẩm bán chạy (Top 5) ---
        List<TopProductDTO> topProducts = orderItemRepository.findTopSellingProducts(PageRequest.of(0, 5));

        // --- G. Đơn hàng gần đây (5 đơn mới nhất) ---
        // Sử dụng orderService.toDTO để convert Entity sang DTO cho đầy đủ thông tin
        List<OrderDTO> recentOrders = orderRepository.findAll(
                PageRequest.of(0, 5, Sort.by("orderDate").descending())
        ).stream().map(orderService::toDTO).collect(Collectors.toList());

        // =========================================================================
        // 5. BUILD KẾT QUẢ TRẢ VỀ
        // =========================================================================
        return DashboardStatisticsDTO.builder()
                // Chỉ số cũ
                .totalRevenueThisMonth(totalRevenueThisMonth)
                .totalOrdersThisMonth(totalOrders)
                .totalAppointmentsThisMonth(totalAppts)
                .totalCustomers(totalUsers)
                .dailyRevenues(finalDailyStats)
                
                // Chỉ số MỚI
                .totalRevenueLastMonth(totalRevenueLastMonth)
                .growthRate(Math.round(growthRate * 100.0) / 100.0) // Làm tròn 2 số thập phân
                .orderStatusCounts(statusMap)
                .topProducts(topProducts)
                .recentOrders(recentOrders)
                .build();
    }
    // --- HÀM HELPER ĐỂ MAP DỮ LIỆU TỪ NATIVE QUERY ---
    private List<DailyRevenueDTO> mapToDTO(List<Object[]> rawData) {
        return rawData.stream().map(row -> {
            // MySQL Native Query trả về Date dưới dạng java.sql.Date
            java.sql.Date sqlDate = (java.sql.Date) row[0];
            LocalDate date = sqlDate.toLocalDate();
            
            // Sum trả về Double hoặc BigDecimal tùy DB, ép kiểu an toàn
            Double revenue = 0.0;
            if (row[1] != null) {
                revenue = ((Number) row[1]).doubleValue();
            }
            
            return new DailyRevenueDTO(date, revenue);
        }).collect(Collectors.toList());
    }
}