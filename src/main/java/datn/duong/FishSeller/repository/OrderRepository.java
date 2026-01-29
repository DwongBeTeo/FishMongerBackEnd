package datn.duong.FishSeller.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import datn.duong.FishSeller.dto.dashboard.DailyRevenueDTO;
import datn.duong.FishSeller.entity.OrderEntity;

public interface OrderRepository extends JpaRepository<OrderEntity, Long>{
    List<OrderEntity> findByUserId(Long userId); // Lịch sử mua hàng

    // Tìm đơn hàng của user có email chứa từ khóa (IgnoreCase: không phân biệt hoa thường)
    // Sắp xếp ngày mới nhất lên đầu
    List<OrderEntity> findByUser_EmailContainingIgnoreCaseOrderByOrderDateDesc(String email);

    // các query thống kê 
    // 1. Tính tổng doanh thu đơn hàng trong khoảng thời gian (Chỉ tính đơn COMPLETED)
    @Query("SELECT SUM(o.totalAmount) FROM OrderEntity o " +
           "WHERE o.status = 'COMPLETED' AND o.orderDate BETWEEN :start AND :end")
    Double sumRevenueByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 2. Đếm số đơn hàng hoàn thành
    @Query("SELECT COUNT(o) FROM OrderEntity o " +
           "WHERE o.status = 'COMPLETED' AND o.orderDate BETWEEN :start AND :end")
    Long countOrdersByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 3. Thống kê doanh thu theo từng ngày (Cho biểu đồ)
    @Query(value = "SELECT DATE(o.order_date) as date, SUM(o.total_amount) as revenue " +
                   "FROM orders o " +
                   "WHERE o.status = 'COMPLETED' AND o.order_date BETWEEN :start AND :end " +
                   "GROUP BY DATE(o.order_date) " +
                   "ORDER BY DATE(o.order_date) ASC", nativeQuery = true)
    List<Object[]> getDailyRevenue(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 4. Thống kê số lượng đơn theo trạng thái (Cho Pie Chart)
    // Kết quả trả về dạng: [ ["PENDING", 5], ["COMPLETED", 10] ]
    @Query("SELECT o.status, COUNT(o) FROM OrderEntity o GROUP BY o.status")
    List<Object[]> countOrdersByStatus();
}
