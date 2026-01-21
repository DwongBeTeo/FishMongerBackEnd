package datn.duong.FishSeller.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import datn.duong.FishSeller.dto.ServiceTypeDTO;
import datn.duong.FishSeller.service.ServiceTypeService;

@RestController
@RequestMapping("/service-types")
@RequiredArgsConstructor
public class ServiceTypeController {

    private final ServiceTypeService serviceTypeService;

    // 1. Xem danh sách dịch vụ (Active only)
    // URL: GET /service-types
    @GetMapping
    public ResponseEntity<Page<ServiceTypeDTO>> getAllActive(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        // SECURITY CHECK: không cho phép lấy quá 10 sản phẩm(tránh bị tấn công thông qua postman)
        if (size > 10) {
            size = 10;
        }
        return ResponseEntity.ok(serviceTypeService.getAllActiveServiceTypes(page, size));
    }

    // 2. Tìm kiếm dịch vụ (Active only)
    // URL: GET /service-types/search?keyword=be ca
    @GetMapping("/search")
    public ResponseEntity<Page<ServiceTypeDTO>> searchActive(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(serviceTypeService.searchActiveServiceTypes(keyword, page, size));
    }

    // 3. Xem chi tiết 1 dịch vụ
    // URL: GET /service-types/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ServiceTypeDTO> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(serviceTypeService.getServiceTypeDetail(id));
    }
}