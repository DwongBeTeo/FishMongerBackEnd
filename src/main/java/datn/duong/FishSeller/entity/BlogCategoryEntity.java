package datn.duong.FishSeller.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "blog_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BlogCategoryEntity extends BaseEntity {

    @Column(nullable = false)
    private String name; // Tên danh mục (VD: Kiến thức nuôi cá, Sự kiện)

    @Column(unique = true, nullable = false)
    private String slug; // URL thân thiện (VD: kien-thuc-nuoi-ca)

    @Column(columnDefinition = "TEXT")
    private String description; // Mô tả ngắn về danh mục

    // --- SEO Fields ---
    @Column(name = "meta_title")
    private String metaTitle;

    @Column(name = "meta_keyword")
    private String metaKeyword;

    @Column(name = "meta_description")
    private String metaDescription;

    // --- Soft Delete & Status ---
    @Builder.Default
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    // Quan hệ 1-N với bài viết
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @Builder.Default
    private List<BlogPostEntity> posts = new ArrayList<>();
}