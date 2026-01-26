package datn.duong.FishSeller.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import datn.duong.FishSeller.entity.AddressEntity;

@Repository
public interface AddressRepository extends JpaRepository<AddressEntity, Long> {
    List<AddressEntity> findByUserId(Long userId);

    // Tìm địa chỉ mặc định của user
    Optional<AddressEntity> findByUserIdAndIsDefaultTrue(Long userId);

    // Reset tất cả địa chỉ của user về false trước khi set cái mới làm default
    @Modifying
    @Query("UPDATE AddressEntity a SET a.isDefault = false WHERE a.user.id = :userId")
    void resetDefaultByUserId(@Param("userId") Long userId);
}