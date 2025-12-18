package datn.duong.FishSeller.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import datn.duong.FishSeller.entity.CartEntity;

@Repository
public interface CartRepository extends JpaRepository<CartEntity, Long> {
    // Tìm giỏ hàng theo userId
    Optional<CartEntity> findByUserId(Long userId);
}
