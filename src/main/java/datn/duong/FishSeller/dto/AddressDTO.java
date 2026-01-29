package datn.duong.FishSeller.dto;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Data
@Builder
public class AddressDTO {
    private Long id;
    private String recipientName;
    private String phoneNumber;
    private String detailedAddress;
    
    @JsonProperty("isDefault")
    private boolean isDefault;
}