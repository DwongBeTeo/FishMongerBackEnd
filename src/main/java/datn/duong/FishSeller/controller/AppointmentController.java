package datn.duong.FishSeller.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import datn.duong.FishSeller.dto.AppointmentDTO;
import datn.duong.FishSeller.dto.FeedbackRequest;
import datn.duong.FishSeller.service.AppointmentService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    // 1. Đặt lịch mới
    // URL: POST /appointments
    @PostMapping
    public ResponseEntity<AppointmentDTO> createBooking(@RequestBody AppointmentDTO dto) {
        // Frontend chỉ cần gửi: serviceTypeId, appointmentDate, appointmentTime, address, note
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.createBooking(dto));
    }

    // 2. Lấy lịch sử đặt của TÔI
    // URL: GET /appointments?page=0&size=10
    @GetMapping
    public ResponseEntity<Page<AppointmentDTO>> getMyAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(appointmentService.getMyAppointments(page, size));
    }

    // 3. Xem chi tiết 1 đơn (Phải là của mình mới xem được)
    // URL: GET /appointments/{id}
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentDTO> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getMyAppointmentDetail(id));
    }

    // 4. Hủy lịch (Chỉ được hủy khi còn PENDING/CONFIRMED)
    // URL: PATCH /appointments/{id}/cancel
    @PatchMapping("/{id}/request-cancel")
    public ResponseEntity<String> requestCancel(
            @PathVariable Long id, 
            @RequestParam String reason
    ) {
        appointmentService.requestCancelBooking(id, reason);
        return ResponseEntity.ok("Yêu cầu hủy của bạn đã được gửi và đang chờ Admin phê duyệt.");
    }

    // 5. Gửi đánh giá (Feedback)
    // URL: POST /appointments/{id}/feedback
    @PostMapping("/{id}/feedback")
    public ResponseEntity<String> submitFeedback(
            @PathVariable Long id,
            @RequestBody @Valid FeedbackRequest request
    ) {
        appointmentService.submitFeedback(id, request.getRating(), request.getComment());
        return ResponseEntity.ok("Cảm ơn quý khách đã đánh giá dịch vụ!");
    }
}
