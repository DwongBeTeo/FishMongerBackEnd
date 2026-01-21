package datn.duong.FishSeller.dto.dashboard;

import java.time.LocalDate;

import lombok.*;

// DTO chi tiết doanh thu từng ngày (Dùng cho LineChart)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyRevenueDTO {
    private LocalDate date;
    private Double revenue;
}