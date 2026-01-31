package datn.duong.FishSeller.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import datn.duong.FishSeller.entity.VoucherEntity;

@Repository
public interface VoucherRepository extends JpaRepository<VoucherEntity, Long> {
    
    // Tìm voucher theo mã và đang active
    Optional<VoucherEntity> findByCodeAndIsActiveTrue(String code);
    
    // Check trùng mã (dùng cho Admin khi tạo mới)
    boolean existsByCode(String code);

    // Admin: Tìm kiếm
    Page<VoucherEntity> findByCodeContainingIgnoreCase(String keyword, Pageable pageable);

    // User: Lấy danh sách voucher còn hạn sử dụng, còn số lượng và đang active
    @Query("SELECT v FROM VoucherEntity v WHERE v.isActive = true " +
           "AND v.startDate <= CURRENT_DATE AND v.endDate >= CURRENT_DATE " +
           "AND v.quantity > 0")
    List<VoucherEntity> findAllAvailableVouchers();
}