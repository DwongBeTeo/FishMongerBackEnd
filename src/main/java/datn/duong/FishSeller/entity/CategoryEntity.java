package datn.duong.FishSeller.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

@SuppressWarnings("deprecation")
@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE categories SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class CategoryEntity extends BaseEntity {
    // @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    // private Long id;
    private String name;
    private String description;

    @OneToMany(mappedBy = "category")
    private List<ProductEntity> products;

    private Boolean isDeleted = false;

    @ManyToOne
    @JoinColumn(name = "parent_id", nullable = true)
    private CategoryEntity parent;

    @OneToMany(mappedBy = "parent", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private List<CategoryEntity> children;

    // // 1. Tự động lấy thời gian lúc Insert
    // // updatable = false: Để khi update danh mục, ngày tạo không bị đổi
    // @CreationTimestamp
    // @Column(name = "created_date", updatable = false)
    // private LocalDateTime createdDate;

    // // 2. Tự động lấy thời gian mỗi khi có lệnh Update
    // @UpdateTimestamp
    // @Column(name = "updated_date")
    // private LocalDateTime updatedDate;
}
