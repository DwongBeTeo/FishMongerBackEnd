package datn.duong.FishSeller.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ProductEntity extends BaseEntity {
    // @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    // private Long id;

    private String name;
    private String description;
    private Double price;
    private Integer stockQuantity;

    @Column(name = "status", nullable = false)
    private String status;
    private String imageUrl;
    // @Column(name = "type",nullable = false)
    // private String type;

    @Column(name = "slug", unique = true) // Slug nên là duy nhất để tạo link
    private String slug;

    @Column(name = "meta_title")
    private String metaTitle;

    @Column(name = "meta_keyword")
    private String metaKeyword;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;
}
