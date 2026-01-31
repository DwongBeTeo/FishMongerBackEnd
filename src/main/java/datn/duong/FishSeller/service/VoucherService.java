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

    // Tạo mới
    @Transactional
    public VoucherEntity createVoucher(VoucherEntity voucher) {
        // LOGIC 1: Xử lý Mã Voucher
        if (voucher.getCode() == null || voucher.getCode().trim().isEmpty()) {
            // Trường hợp Admin muốn tự sinh mã (VD: Mã dài 8 ký tự)
            String generatedCode = "";
            // Vòng lặp do-while để đảm bảo sinh ra mã chưa từng tồn tại (tránh trùng lặp)
            do {
                generatedCode = VoucherUtils.generateRandomCode(8);
            } while (voucherRepository.existsByCode(generatedCode));
            
            voucher.setCode(generatedCode);
        } else {
            // Trường hợp Admin tự nhập (VD: SALE50)
            // Kiểm tra trùng
            if (voucherRepository.existsByCode(voucher.getCode())) {
                throw new RuntimeException("Mã voucher '" + voucher.getCode() + "' đã tồn tại!");
            }
            // Luôn convert sang chữ hoa để tránh user nhập sale50 bị lỗi
            voucher.setCode(voucher.getCode().toUpperCase().trim());
        }

        // Logic 2: Default Active
        if(voucher.getIsActive() == null) voucher.setIsActive(true);
        
        return voucherRepository.save(voucher);
    }

    // Cập nhật
    @Transactional
    public VoucherEntity updateVoucher(Long id, VoucherEntity dto) {
        VoucherEntity existing = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));

        // Chỉ cho phép update các thông tin cấu hình, KHÔNG cho sửa Code để đảm bảo toàn vẹn dữ liệu cũ
        existing.setDescription(dto.getDescription());
        existing.setDiscountType(dto.getDiscountType());
        existing.setDiscountValue(dto.getDiscountValue());
        existing.setMaxDiscountAmount(dto.getMaxDiscountAmount());
        existing.setMinOrderValue(dto.getMinOrderValue());
        existing.setQuantity(dto.getQuantity());
        existing.setStartDate(dto.getStartDate());
        existing.setEndDate(dto.getEndDate());
        existing.setIsActive(dto.getIsActive());

        return voucherRepository.save(existing);
    }

    // Xóa mềm (Toggle Active)
    @Transactional
    public void deleteVoucher(Long id) {
        VoucherEntity voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));
        // Ở đây tôi chọn Soft Delete (Ẩn đi) để giữ lịch sử đơn hàng
        voucher.setIsActive(false); 
        voucherRepository.save(voucher);
    }

    // khôi phục
    @Transactional
    public void restoreVoucher(Long id) {
        VoucherEntity voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));
        
        // Logic khôi phục
        voucher.setIsActive(true);
        voucherRepository.save(voucher);
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