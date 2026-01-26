package datn.duong.FishSeller.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import datn.duong.FishSeller.dto.dashboard.DailyRevenueDTO;
import datn.duong.FishSeller.entity.AppointmentEntity;
import datn.duong.FishSeller.enums.AppointmentStatus;

@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentEntity, Long> {

    // 1. USER: Lấy lịch sử đặt của chính mình
    Page<AppointmentEntity> findByUserId(Long userId, Pageable pageable);

    // 2. EMPLOYEE: Lấy lịch làm việc của mình theo ngày (để hiển thị lên App nhân viên)
    List<AppointmentEntity> findByEmployeeIdAndAppointmentDate(Long employeeId, LocalDate date);

    // 3. EMPLOYEE: Lấy lịch sử làm việc (phân trang)
    Page<AppointmentEntity> findByEmployeeId(Long employeeId, Pageable pageable);

    // 4. ADMIN: Lấy tất cả hoặc lọc theo Status
    Page<AppointmentEntity> findByStatus(AppointmentStatus status, Pageable pageable);

    // 5. QUAN TRỌNG: Check trùng lịch nhân viên
    // Logic: Có lịch nào của nhân viên này, vào ngày này, mà khoảng thời gian bị chồng chéo không?
    // (StartA < EndB) AND (EndA > StartB) là công thức check giao nhau
    @Query("SELECT COUNT(a) > 0 FROM AppointmentEntity a " +
           "WHERE a.employee.id = :employeeId " +
           "AND a.appointmentDate = :date " +
           "AND a.status NOT IN ('CANCELLED', 'REJECTED') " +
           "AND (a.appointmentTime < :endTime " +
           "AND a.expectedEndTime > :startTime)")
    boolean existsByEmployeeAndDateTimeOverlap(
            @Param("employeeId") Long employeeId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    // HÀM TÌM KIẾM NÂNG CAO CHO ADMIN
    // Logic: Nếu keyword null thì lấy hết. Nếu có keyword thì tìm trong Tên khách OR SĐT OR Tên NV OR ID
    @Query("SELECT a FROM AppointmentEntity a " +
           "LEFT JOIN a.user u " +
           "LEFT JOIN a.employee e " +
           "LEFT JOIN a.serviceType s " +
           "WHERE (:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "a.phoneNumber LIKE CONCAT('%', :keyword, '%') OR " + // Tìm theo SĐT trên đơn hàng
           "(e IS NOT NULL AND LOWER(e.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))) OR " +
           "LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "CAST(a.id AS string) LIKE CONCAT('%', :keyword, '%')) " +
           "AND (:status IS NULL OR a.status = :status) " +
           "ORDER BY a.appointmentDate DESC, a.appointmentTime DESC")
    Page<AppointmentEntity> searchAppointments(
            @Param("keyword") String keyword,
            @Param("status") AppointmentStatus status,
            Pageable pageable
    );

    // Các query thống kê của AppointmentRepository dành cho Admindoashboard
    // 1. Tổng tiền dịch vụ
    @Query("SELECT SUM(a.priceAtBooking) FROM AppointmentEntity a " +
           "WHERE a.status = 'COMPLETED' AND a.appointmentDate BETWEEN :start AND :end")
    Double sumRevenueByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);

    // 2. Đếm số lượng
    @Query("SELECT COUNT(a) FROM AppointmentEntity a " +
           "WHERE a.status = 'COMPLETED' AND a.appointmentDate BETWEEN :start AND :end")
    Long countAppointmentsByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);

    // 3. Doanh thu từng ngày (FIX: Sửa đường dẫn package datn.duong...)
    @Query(value = "SELECT DATE(a.appointment_date) as date, SUM(a.price_at_booking) as revenue " +
                   "FROM appointments a " +
                   "WHERE a.status = 'COMPLETED' AND a.appointment_date BETWEEN :start AND :end " +
                   "GROUP BY DATE(a.appointment_date) " +
                   "ORDER BY DATE(a.appointment_date) ASC", nativeQuery = true)
    List<Object[]> getDailyRevenue(@Param("start") LocalDate start, @Param("end") LocalDate end);
    // end admindashboard
}
