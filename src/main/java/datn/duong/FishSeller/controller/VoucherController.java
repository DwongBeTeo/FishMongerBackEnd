package datn.duong.FishSeller.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import datn.duong.FishSeller.entity.VoucherEntity;
import datn.duong.FishSeller.service.VoucherService;
import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    // 1. Lấy danh sách Voucher đang diễn ra (để hiển thị banner: "Lấy mã ngay")
    @GetMapping("/available")
    public ResponseEntity<List<VoucherEntity>> getAvailableVouchers() {
        return ResponseEntity.ok(voucherService.getAvailableVouchersForUser());
    }

    // 2. Kiểm tra thử mã giảm giá (Preview)
    // URL: /vouchers/preview?code=SALE50&totalAmount=500000
    @GetMapping("/preview")
    public ResponseEntity<?> previewVoucher(
            @RequestParam String code,
            @RequestParam Double totalAmount
    ) {
        try {
            Double discount = voucherService.previewDiscount(code, totalAmount);
            // Trả về số tiền được giảm để frontend hiện thị
            return ResponseEntity.ok(Map.of(
                "valid", true,
                "discountAmount", discount,
                "message", "Áp dụng mã thành công!"
            ));
        } catch (RuntimeException e) {
            // Trả về lỗi để frontend hiện thị đỏ
            return ResponseEntity.badRequest().body(Map.of(
                "valid", false,
                "discountAmount", 0,
                "message", e.getMessage()
            ));
        }
    }
}