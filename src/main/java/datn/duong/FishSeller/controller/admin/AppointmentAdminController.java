package datn.duong.FishSeller.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import datn.duong.FishSeller.dto.AppointmentDTO;
import datn.duong.FishSeller.enums.AppointmentStatus;
import datn.duong.FishSeller.service.AppointmentService;

@RestController
@RequestMapping("/admin/appointments")
@RequiredArgsConstructor
public class AppointmentAdminController {

    private final AppointmentService appointmentService;

    // 1. Xem tất cả đơn đặt lịch (Quản lý)
    // URL: GET /admin/appointments?keyword=0988&status=PENDING&page=0
    @GetMapping
    public ResponseEntity<Page<AppointmentDTO>> getAllAppointments(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) AppointmentStatus status, // Enum
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        // SECURITY CHECK: không cho phép lấy quá 10 sản phẩm(tránh bị tấn công thông qua postman)\
        if (size > 10) {
            size = 10;
        }
        return ResponseEntity.ok(appointmentService.getAllForAdmin(keyword, status, page, size));
    }

    // 2. Phân công nhân viên (Assign)
    // URL: PUT /admin/appointments/{id}/assign?employeeId=5
    @PutMapping("/{id}/assign")
    public ResponseEntity<AppointmentDTO> assignEmployee(
            @PathVariable Long id,
            @RequestParam Long employeeId
    ) {
        return ResponseEntity.ok(appointmentService.assignEmployee(id, employeeId));
    }

    // 3. Cập nhật trạng thái (VD: Hoàn thành, Đang làm)
    // URL: PATCH /admin/appointments/{id}/status?status=COMPLETED
    @PatchMapping("/{id}/status")
    public ResponseEntity<AppointmentDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam AppointmentStatus status
    ) {
        return ResponseEntity.ok(appointmentService.updateStatusByAdmin(id, status));
    }

    // 4. Admin chủ động hủy lịch
    // URL: PATCH /admin/appointments/{id}/cancel
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<AppointmentDTO> cancelAppointment(
            @PathVariable Long id,
            @RequestParam(required = false) String reason
    ) {
        return ResponseEntity.ok(appointmentService.cancelByAdmin(id, reason));
    }

    // 5.: DUYỆT HOẶC TỪ CHỐI YÊU CẦU HỦY TỪ KHÁCH HÀNG
    // URL: PUT /admin/appointments/{id}/review-cancel?approve=true&reason=...
    @PutMapping("/{id}/review-cancel")
    public ResponseEntity<AppointmentDTO> reviewCancellation(
            @PathVariable Long id,
            @RequestParam boolean approve,
            @RequestParam(required = false) String reason
    ) {
        return ResponseEntity.ok(appointmentService.handleCancellationReview(id, approve, reason));
    }
}