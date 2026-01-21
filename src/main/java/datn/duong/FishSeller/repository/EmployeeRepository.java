package datn.duong.FishSeller.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import datn.duong.FishSeller.entity.EmployeeEntity;
import datn.duong.FishSeller.enums.EmployeeStatus;

public interface EmployeeRepository extends JpaRepository<EmployeeEntity, Long> {
    // 1. Tìm nhân viên theo User ID (Dùng để check trùng & lấy profile)
    Optional<EmployeeEntity> findByUserId(Long userId);

    // 2. Check xem User này đã là nhân viên chưa (Trả về true/false nhanh hơn tìm entity)
    boolean existsByUserId(Long userId);

    // Hàm này để lấy list cho dropdown
    List<EmployeeEntity> findByStatus(EmployeeStatus status);

    // 3. Tìm kiếm đa năng (Tên OR SĐT) cho Admin
    @Query("SELECT e FROM EmployeeEntity e " +
           "WHERE (:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(e.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "e.phoneNumber LIKE CONCAT('%', :keyword, '%') OR " +
           "LOWER(e.user.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:status IS NULL OR e.status = :status)")
    Page<EmployeeEntity> searchEmployees(
            @Param("keyword") String keyword, 
            @Param("status") EmployeeStatus status, 
            Pageable pageable
    );
}
