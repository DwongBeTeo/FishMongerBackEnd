package datn.duong.FishSeller.controller.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import datn.duong.FishSeller.entity.VoucherEntity;
import datn.duong.FishSeller.enums.DiscountType;
import datn.duong.FishSeller.service.VoucherService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/vouchers")
@RequiredArgsConstructor
public class VoucherAdminController {
    
    private final VoucherService voucherService;

    // 1. Lấy danh sách (Search + Phân trang)
    @GetMapping
    public ResponseEntity<Page<VoucherEntity>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(voucherService.getAllVouchersForAdmin(keyword, page, size));
    }

    // 2. Tạo mới
    @PostMapping
    public ResponseEntity<VoucherEntity> create(@RequestBody VoucherEntity voucher) {
        return ResponseEntity.ok(voucherService.createVoucher(voucher));
    }

    // 3. Cập nhật
    @PutMapping("/{id}")
    public ResponseEntity<VoucherEntity> update(@PathVariable Long id, @RequestBody VoucherEntity voucher) {
        return ResponseEntity.ok(voucherService.updateVoucher(id, voucher));
    }

    // 4. Xóa (Soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        voucherService.deleteVoucher(id);
        return ResponseEntity.noContent().build();
    }

    // 5. Khôi phục voucher
    // PUT: /admin/vouchers/{id}/restore
    @PutMapping("/{id}/restore")
    public ResponseEntity<String> restoreVoucher(@PathVariable Long id) {
        voucherService.restoreVoucher(id);
        return ResponseEntity.ok("Khôi phục voucher thành công!");
    }

    // 6. Helper Dropdown Enum
    @GetMapping("/discount-types")
    public ResponseEntity<List<Map<String, String>>> getDiscountTypes() {
        List<Map<String, String>> list = new ArrayList<>();
        for (DiscountType type : DiscountType.values()) {
            Map<String, String> item = new HashMap<>();
            item.put("key", type.name());
            item.put("name", type.getDisplayName());
            list.add(item);
        }
        return ResponseEntity.ok(list);
    }
}