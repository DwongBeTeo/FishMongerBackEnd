package datn.duong.FishSeller.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class CategoryDTO extends BaseDTO {
    // private Long id;
    private String name;
    private String description;
    private Long parentId;
    private String parentName;
    private Boolean isDeleted;
    private Long productCount;
    private List<CategoryDTO> children;
    private String type;
    private String slug;
    private String metaTitle;
    private String metaKeyword;
    // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    // private LocalDateTime createdDate;

    // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    // private LocalDateTime updatedDate;
}