package datn.duong.FishSeller.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import datn.duong.FishSeller.dto.ServiceTypeDTO;
import datn.duong.FishSeller.service.ServiceTypeService;

@RestController
@RequestMapping("/admin/service-types")
@RequiredArgsConstructor
public class ServiceTypeAdminController {

    private final ServiceTypeService serviceTypeService;

    // 1. Lấy danh sách quản lý (Có tìm kiếm + Phân trang)
    // URL: GET /admin/service-types?keyword=vệ sinh&page=0&size=10
    @GetMapping
    public ResponseEntity<Page<ServiceTypeDTO>> getAllForAdmin(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        // SECURITY CHECK: không cho phép lấy quá 10 sản phẩm(tránh bị tấn công thông qua postman)
        if (size > 5) {
            size = 5;
        }
        return ResponseEntity.ok(serviceTypeService.getAllServiceTypesForAdmin(keyword, page, size));
    }

    // 2. Tạo dịch vụ mới
    // URL: POST /admin/service-types
    @PostMapping
    public ResponseEntity<ServiceTypeDTO> createServiceType(@RequestBody ServiceTypeDTO dto) {
        ServiceTypeDTO created = serviceTypeService.createServiceType(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // 3. Cập nhật dịch vụ
    // URL: PUT /admin/service-types/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ServiceTypeDTO> updateServiceType(
            @PathVariable Long id,
            @RequestBody ServiceTypeDTO dto
    ) {
        return ResponseEntity.ok(serviceTypeService.updateServiceType(id, dto));
    }

    // 4. Ẩn/Hiện dịch vụ (Xóa mềm)
    // URL: PATCH /admin/service-types/{id}/toggle
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<Void> toggleStatus(@PathVariable Long id) {
        serviceTypeService.toggleServiceStatus(id);
        return ResponseEntity.noContent().build();
    }
}
