package datn.duong.FishSeller.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ServiceTypeDTO extends BaseDTO {

    private String name;       // VD: Vệ sinh bể cá
    private Double price;      // VD: 200000
    private String description;
    private String imageUrl;
    
    private Integer estimatedDuration; // Thời gian ước tính (phút)
    
    private Boolean isActive;
}