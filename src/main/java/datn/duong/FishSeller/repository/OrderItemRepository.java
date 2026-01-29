package datn.duong.FishSeller.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import datn.duong.FishSeller.dto.dashboard.TopProductDTO;
import datn.duong.FishSeller.entity.OrderItemEntity;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {
//làm câu truy vấn lấy giá từ trường priceAtOrder không bao giờ lấy giá trong bảng product
    // Lấy Top sản phẩm bán chạy (Chỉ tính đơn đã hoàn thành để chính xác)
    @Query("SELECT new datn.duong.FishSeller.dto.dashboard.TopProductDTO(" +
           "  i.product.id, " +
           "  i.product.name, " +
           "  i.product.imageUrl, " +
           "  SUM(i.quantity), " +
           "  SUM(i.priceAtOrder * i.quantity) " +
           ") " +
           "FROM OrderItemEntity i " +
           "WHERE i.order.status = 'COMPLETED' " +
           "GROUP BY i.product.id, i.product.name, i.product.imageUrl " +
           "ORDER BY SUM(i.quantity) DESC")
    List<TopProductDTO> findTopSellingProducts(Pageable pageable);
}
