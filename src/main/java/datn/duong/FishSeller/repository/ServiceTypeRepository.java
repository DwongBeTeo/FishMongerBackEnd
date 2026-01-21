package datn.duong.FishSeller.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import datn.duong.FishSeller.entity.ServiceTypeEntity;

public interface ServiceTypeRepository extends JpaRepository<ServiceTypeEntity, Long> {
    // 1. Kiểm tra trùng tên (Dùng khi tạo mới)
    boolean existsByName(String name);

    // 2. Dành cho USER/GUEST: Chỉ lấy các dịch vụ đang hoạt động (isActive = true)
    Page<ServiceTypeEntity> findAllByIsActiveTrue(Pageable pageable);

    // 3. Dành cho USER/GUEST: Tìm kiếm theo tên nhưng chỉ trong các dịch vụ Active
    Page<ServiceTypeEntity> findByNameContainingIgnoreCaseAndIsActiveTrue(String name, Pageable pageable);

    // 4. Dành cho ADMIN: Tìm kiếm tất cả (kể cả đã ẩn)
    Page<ServiceTypeEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
