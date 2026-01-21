package datn.duong.FishSeller.dto;
import lombok.*;

@Data
@Builder
public class AddressDTO {
    private Long id;
    private String recipientName;
    private String phoneNumber;
    private String detailedAddress;
    private boolean isDefault;
}