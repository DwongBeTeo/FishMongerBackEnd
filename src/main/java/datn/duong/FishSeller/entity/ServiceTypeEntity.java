package datn.duong.FishSeller.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "serviceTypes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // VD: Vệ sinh hồ
    private Double price;
    private String description;
}
