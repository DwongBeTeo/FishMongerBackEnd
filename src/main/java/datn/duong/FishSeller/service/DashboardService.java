package datn.duong.FishSeller.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import datn.duong.FishSeller.dto.dashboard.DailyRevenueDTO;
import datn.duong.FishSeller.dto.dashboard.DashboardStatisticsDTO;
import datn.duong.FishSeller.repository.AppointmentRepository;
import datn.duong.FishSeller.repository.OrderRepository;
import datn.duong.FishSeller.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrderRepository orderRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    public DashboardStatisticsDTO getDashboardStats() {
        // 1. Xác định khoảng thời gian: Đầu tháng đến Cuối tháng này
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDate lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth());

        // Convert sang LocalDateTime cho Order (vì Order dùng LocalDateTime)
        LocalDateTime startDateTime = firstDayOfMonth.atStartOfDay();
        LocalDateTime endDateTime = lastDayOfMonth.atTime(23, 59, 59);

        // 2. Lấy số liệu tổng quan
        Double orderRevenue = orderRepository.sumRevenueByDateRange(startDateTime, endDateTime);
        Double apptRevenue = appointmentRepository.sumRevenueByDateRange(firstDayOfMonth, lastDayOfMonth);
        
        // Handle null (nếu tháng này chưa có đơn nào)
        orderRevenue = (orderRevenue != null) ? orderRevenue : 0.0;
        apptRevenue = (apptRevenue != null) ? apptRevenue : 0.0;

        Long totalOrders = orderRepository.countOrdersByDateRange(startDateTime, endDateTime);
        Long totalAppts = appointmentRepository.countAppointmentsByDateRange(firstDayOfMonth, lastDayOfMonth);
        Long totalUsers = userRepository.count(); // Tổng số khách hàng toàn hệ thống

        // 3. Lấy dữ liệu biểu đồ (SỬA ĐOẠN NÀY)
        List<Object[]> orderRaw = orderRepository.getDailyRevenue(startDateTime, endDateTime);
        List<Object[]> apptRaw = appointmentRepository.getDailyRevenue(firstDayOfMonth, lastDayOfMonth);

        // Convert Object[] -> DailyRevenueDTO
        List<DailyRevenueDTO> orderDaily = mapToDTO(orderRaw);
        List<DailyRevenueDTO> apptDaily = mapToDTO(apptRaw);

        // 4. Gộp dữ liệu biểu đồ (Order + Appointment = Total Daily)
        Map<LocalDate, Double> revenueMap = new TreeMap<>(); // TreeMap để tự sắp xếp theo ngày

        // Khởi tạo các ngày trong tháng bằng 0 (để biểu đồ liền mạch không bị đứt đoạn)
        for (LocalDate date = firstDayOfMonth; !date.isAfter(today); date = date.plusDays(1)) {
            revenueMap.put(date, 0.0);
        }

        // Cộng dồn doanh thu Order
        for (DailyRevenueDTO item : orderDaily) {
            revenueMap.merge(item.getDate(), item.getRevenue(), Double::sum);
        }

        // Cộng dồn doanh thu Appointment
        for (DailyRevenueDTO item : apptDaily) {
            revenueMap.merge(item.getDate(), item.getRevenue(), Double::sum);
        }

        // Chuyển Map thành List
        List<DailyRevenueDTO> finalDailyStats = revenueMap.entrySet().stream()
                .map(entry -> new DailyRevenueDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        // 5. Build DTO trả về
        return DashboardStatisticsDTO.builder()
                .totalRevenueThisMonth(orderRevenue + apptRevenue)
                .totalOrdersThisMonth(totalOrders)
                .totalAppointmentsThisMonth(totalAppts)
                .totalCustomers(totalUsers)
                .dailyRevenues(finalDailyStats)
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