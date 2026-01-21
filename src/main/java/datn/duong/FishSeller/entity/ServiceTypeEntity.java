package datn.duong.FishSeller.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "serviceTypes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ServiceTypeEntity extends BaseEntity {
    // ID, createdDate, updatedDate được kế thừa

    private String name; // VD: Vệ sinh hồ
    private Double price;
    private String description;
    private String imageUrl;
    
    @Column(nullable = false)
    private Integer estimatedDuration; // Thời gian ước tính (phút)
    
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true; // Mặc định là True
}
