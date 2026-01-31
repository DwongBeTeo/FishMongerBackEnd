package datn.duong.FishSeller.controller.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import datn.duong.FishSeller.dto.dashboard.DashboardStatisticsDTO;
import datn.duong.FishSeller.dto.dashboard.VoucherStatsDTO;
import datn.duong.FishSeller.service.DashboardService;
import datn.duong.FishSeller.service.VoucherService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final VoucherService voucherService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatisticsDTO> getStats() {
        return ResponseEntity.ok(dashboardService.getDashboardStats());
    }

    // GET /admin/vouchers/{code}/stats
    @GetMapping("/{code}/stats")
    public ResponseEntity<VoucherStatsDTO> getVoucherStats(@PathVariable String code) {
        return ResponseEntity.ok(voucherService.getVoucherStats(code));
    }
}