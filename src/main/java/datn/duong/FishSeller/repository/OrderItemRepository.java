package datn.duong.FishSeller.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import datn.duong.FishSeller.entity.OrderItemEntity;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {
//làm câu truy vấn lấy giá từ trường priceAtOrder không bao giờ lấy giá trong bảng product
}
