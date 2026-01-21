package datn.duong.FishSeller.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import datn.duong.FishSeller.entity.AddressEntity;

public interface AddressRepository extends JpaRepository<AddressEntity, Long> {
    List<AddressEntity> findByUserId(Long userId);
}