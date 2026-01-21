package datn.duong.FishSeller.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import datn.duong.FishSeller.dto.dashboard.DashboardStatisticsDTO;
import datn.duong.FishSeller.service.DashboardService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatisticsDTO> getStats() {
        return ResponseEntity.ok(dashboardService.getDashboardStats());
    }
}