package datn.duong.FishSeller.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import datn.duong.FishSeller.entity.OrderEntity;

public interface OrderRepository extends JpaRepository<OrderEntity, Long>{
    List<OrderEntity> findByUserId(Long userId); // Lịch sử mua hàng
}
