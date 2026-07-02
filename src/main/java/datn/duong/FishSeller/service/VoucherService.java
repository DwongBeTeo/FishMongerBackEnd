package datn.duong.FishSeller.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import datn.duong.FishSeller.dto.dashboard.VoucherStatsDTO;
import datn.duong.FishSeller.entity.VoucherEntity;
import datn.duong.FishSeller.enums.DiscountType;
import datn.duong.FishSeller.repository.OrderRepository;
import datn.duong.FishSeller.repository.VoucherRepository;
import datn.duong.FishSeller.util.VoucherUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    // USER / GUEST METHODS (Khách hàng)
    // Lấy danh sách voucher "ngon" đang có để hiển thị ở trang chủ hoặc trang Cart
    public List<VoucherEntity> getAvailableVouchersForUser() {
        return voucherRepository.findAllAvailableVouchers();
    }
    
    // API Check trước xem giảm được bao nhiêu tiền (Preview)
    // Frontend gọi cái này khi khách vừa nhập mã xong bấm "Áp dụng" nhưng chưa "Đặt hàng"
    public Double previewDiscount(String code, Double tempTotalAmount) {
        return calculateDiscount(code, tempTotalAmount);
    }

    // =============================
    // ADMIN

    // thống kê doanh thu của voucher(hàm nay năm trong DashboardAdminController)
    public VoucherStatsDTO getVoucherStats(String code) {
        VoucherStatsDTO stats = orderRepository.getVoucherStatistics(code);
        if (stats == null) {
            // Nếu chưa có đơn nào dùng mã này thì trả về 0 hết
            return new VoucherStatsDTO(code, 0L, 0.0, 0.0);
        }
        return stats;
    }

    // Lấy tất cả (có phân trang & search)
    public Page<VoucherEntity> getAllVouchersForAdmin(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        if (keyword != null && !keyword.isEmpty()) {
            return voucherRepository.findByCodeContainingIgnoreCase(keyword, pageable);
        }
        return voucherRepository.findAll(pageable);
    }

    @Transactional
    public VoucherEntity createVoucher(VoucherEntity voucher) {
        System.out.println("========== DEBUG CREATE VOUCHER ==========");
        System.out.println("1. Bắt đầu tạo voucher...");

        // LOGIC 1: Xử lý Mã Voucher
        if (voucher.getCode() == null || voucher.getCode().trim().isEmpty()) {
            String generatedCode = "";
            do {
                generatedCode = VoucherUtils.generateRandomCode(8);
            } while (voucherRepository.existsByCode(generatedCode));
            voucher.setCode(generatedCode);
            System.out.println("2. Sinh mã tự động: " + voucher.getCode());
        } else {
            if (voucherRepository.existsByCode(voucher.getCode())) {
                throw new RuntimeException("Mã voucher '" + voucher.getCode() + "' đã tồn tại!");
            }
            voucher.setCode(voucher.getCode().toUpperCase().trim());
            System.out.println("2. Dùng mã tự nhập: " + voucher.getCode());
        }

        if(voucher.getIsActive() == null) voucher.setIsActive(true);
        
        System.out.println("3. Tiến hành lưu DB...");
        VoucherEntity savedVoucher = voucherRepository.save(voucher);
        System.out.println("4. Đã lưu DB thành công. ID: " + savedVoucher.getId() + " | Active: " + savedVoucher.getIsActive());

        // KHOANH VÙNG ĐIỀU KIỆN GỬI REAL-TIME
        boolean isNotExpired = savedVoucher.getEndDate() == null || !LocalDate.now().isAfter(savedVoucher.getEndDate());
        System.out.println("5. Kiểm tra điều kiện gửi WS -> isNotExpired: " + isNotExpired + " | isActive: " + savedVoucher.getIsActive());

        if (savedVoucher.getIsActive() && isNotExpired) {
            System.out.println("6. THỎA MÃN ĐIỀU KIỆN -> ĐANG GỬI TIN NHẮN TỚI KÊNH: /topic/public/vouchers");
            try {
                notificationService.sendPublicNotification("/vouchers", savedVoucher);
                System.out.println("7. GỬI TIN NHẮN WEBSOCKET THÀNH CÔNG!");
            } catch (Exception e) {
                System.out.println("❌ LỖI KHI GỬI WEBSOCKET: " + e.getMessage());
            }
        } else {
            System.out.println("6. BỎ QUA GỬI WEBSOCKET (Do chưa thỏa mãn điều kiện Active/Hạn sử dụng)");
        }
        
        System.out.println("==========================================");
        return savedVoucher;
    }

    // Sửa lại hàm updateVoucher trong VoucherService.java
    @Transactional
    public VoucherEntity updateVoucher(Long id, VoucherEntity dto) {
        System.out.println("========== DEBUG UPDATE VOUCHER ==========");
        VoucherEntity existing = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));

        existing.setDescription(dto.getDescription());
        existing.setDiscountType(dto.getDiscountType());
        existing.setDiscountValue(dto.getDiscountValue());
        existing.setMaxDiscountAmount(dto.getMaxDiscountAmount());
        existing.setMinOrderValue(dto.getMinOrderValue());
        existing.setQuantity(dto.getQuantity());
        existing.setStartDate(dto.getStartDate());
        existing.setEndDate(dto.getEndDate());
        existing.setIsActive(dto.getIsActive());

        VoucherEntity savedVoucher = voucherRepository.save(existing);

        // LOGIC BẮN WEBSOCKET TƯƠNG TỰ CREATE
        boolean isNotExpired = savedVoucher.getEndDate() == null || !LocalDate.now().isAfter(savedVoucher.getEndDate());
        if (savedVoucher.getIsActive() && isNotExpired) {
            System.out.println("-> Đã Cập nhật Voucher hợp lệ, đang gửi tới kênh: /topic/public/vouchers");
            try {
                notificationService.sendPublicNotification("/vouchers", savedVoucher);
            } catch (Exception e) {
                System.out.println("❌ LỖI GỬI WS: " + e.getMessage());
            }
        }
        
        return savedVoucher;
    }

    // Xóa mềm (Khóa/Vô hiệu hóa Voucher)
    @Transactional
    public void deleteVoucher(Long id) {
        System.out.println("========== DEBUG DELETE VOUCHER ==========");
        VoucherEntity voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));
        
        // 1. Soft Delete (Ẩn đi)
        voucher.setIsActive(false); 
        VoucherEntity savedVoucher = voucherRepository.save(voucher);

        // 2. Bắn thông báo Real-time để Frontend gỡ voucher này khỏi màn hình User
        System.out.println("-> Đã khóa Voucher ID: " + id + ", đang gửi tới kênh: /topic/public/vouchers để cập nhật UI");
        try {
            // Gửi toàn bộ object (lúc này isActive = false). Frontend tự bắt logic để ẩn đi.
            notificationService.sendPublicNotification("/vouchers", savedVoucher);
        } catch (Exception e) {
            System.out.println("❌ LỖI GỬI WS (DELETE): " + e.getMessage());
        }
        System.out.println("==========================================");
    }

    // Khôi phục (Mở khóa Voucher)
    @Transactional
    public void restoreVoucher(Long id) {
        System.out.println("========== DEBUG RESTORE VOUCHER ==========");
        VoucherEntity voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));
        
        // 1. Logic khôi phục
        voucher.setIsActive(true);
        VoucherEntity savedVoucher = voucherRepository.save(voucher);

        // 2. Logic bắn WebSocket (Chỉ gửi nếu voucher này chưa hết hạn)
        boolean isNotExpired = savedVoucher.getEndDate() == null || !LocalDate.now().isAfter(savedVoucher.getEndDate());
        if (isNotExpired) {
            System.out.println("-> Đã khôi phục Voucher ID: " + id + " hợp lệ, đang gửi tới kênh: /topic/public/vouchers");
            try {
                notificationService.sendPublicNotification("/vouchers", savedVoucher);
            } catch (Exception e) {
                System.out.println("❌ LỖI GỬI WS (RESTORE): " + e.getMessage());
            }
        } else {
            System.out.println("-> Voucher đã khôi phục nhưng ĐÃ HẾT HẠN, bỏ qua gửi WebSocket cho User.");
        }
        System.out.println("===========================================");
    }

    // =============================
    // HELPER METHODS
    // Hàm tính toán số tiền giảm giá
    public Double calculateDiscount(String code, Double orderTotalAmount) {
        code = code.toUpperCase();
        // 1. Tìm voucher
        VoucherEntity voucher = voucherRepository.findByCodeAndIsActiveTrue(code)
                .orElseThrow(() -> new RuntimeException("Mã giảm giá không tồn tại hoặc đã bị khóa"));

        // 2. Validate ngày
        LocalDate now = LocalDate.now();
        if (now.isBefore(voucher.getStartDate()) || now.isAfter(voucher.getEndDate())) {
            throw new RuntimeException("Mã giảm giá chưa đến hạn hoặc đã hết hạn");
        }

        // 3. Validate số lượng
        if (voucher.getQuantity() <= 0) {
            throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng");
        }

        // 4. Validate giá trị đơn tối thiểu
        if (orderTotalAmount < voucher.getMinOrderValue()) {
            throw new RuntimeException("Đơn hàng chưa đạt giá trị tối thiểu: " + voucher.getMinOrderValue());
        }

        // 5. Tính toán tiền giảm
        double discount = 0;
        if (voucher.getDiscountType() == DiscountType.FIXED_AMOUNT) {
            discount = voucher.getDiscountValue();
        } else {
            // Tính theo %
            discount = orderTotalAmount * (voucher.getDiscountValue() / 100);
            // Kiểm tra giảm tối đa (nếu có cấu hình)
            if (voucher.getMaxDiscountAmount() != null && discount > voucher.getMaxDiscountAmount()) {
                discount = voucher.getMaxDiscountAmount();
            }
        }

        return discount;
    }

    // Hàm trừ số lượng voucher sau khi đặt hàng thành công
    @Transactional
    public void decreaseQuantity(String code) {
        VoucherEntity voucher = voucherRepository.findByCodeAndIsActiveTrue(code).orElse(null);
        if (voucher != null && voucher.getQuantity() > 0) {
            voucher.setQuantity(voucher.getQuantity() - 1);
            voucherRepository.save(voucher);
        }
    }
}