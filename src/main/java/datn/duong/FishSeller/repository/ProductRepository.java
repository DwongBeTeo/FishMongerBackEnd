package datn.duong.FishSeller.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import datn.duong.FishSeller.entity.ProductEntity;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    // Tìm kiếm sản phẩm theo tên hoặc category
    List<ProductEntity> findByNameContaining(String name);
    List<ProductEntity> findByCategoryId(Long categoryId);
}
