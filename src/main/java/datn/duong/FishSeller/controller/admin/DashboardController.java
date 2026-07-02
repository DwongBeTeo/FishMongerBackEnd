package datn.duong.FishSeller.controller.admin;

import java.io.ByteArrayInputStream;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import datn.duong.FishSeller.dto.dashboard.DashboardStatisticsDTO;
import datn.duong.FishSeller.dto.dashboard.VoucherStatsDTO;
import datn.duong.FishSeller.service.DashboardService;
import datn.duong.FishSeller.service.ExcelService;
import datn.duong.FishSeller.service.VoucherService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final VoucherService voucherService;
    private final ExcelService excelService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatisticsDTO> getStats() {
        return ResponseEntity.ok(dashboardService.getDashboardStats());
    }

    // GET /admin/vouchers/{code}/stats
    @GetMapping("/{code}/stats")
    public ResponseEntity<VoucherStatsDTO> getVoucherStats(@PathVariable String code) {
        return ResponseEntity.ok(voucherService.getVoucherStats(code));
    }

    // API Xuất Excel
    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportExcel() {
        // 1. Lấy dữ liệu mới nhất
        DashboardStatisticsDTO stats = dashboardService.getDashboardStats();

        // 2. Tạo file excel
        ByteArrayInputStream in = excelService.exportDashboardToExcel(stats);

        // 3. Trả về file
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=bao_cao_doanh_thu.xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
}