package datn.duong.FishSeller.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import datn.duong.FishSeller.entity.CategoryEntity;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    // select * from tbl_categories where name = ?
    Optional<CategoryEntity> findByName(String name);

    // select count(*) from tbl_categories where name = ?
    Boolean existsByName(String name);

    // select * from tbl_categories where id = ?
    Optional<CategoryEntity> findById(Long id);

    List<CategoryEntity> findByParentIsNull();
    
    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, Long id); // Dùng khi Update (trùng slug nhưng không phải chính nó)
}
