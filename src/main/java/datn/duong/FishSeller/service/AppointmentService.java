package datn.duong.FishSeller.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import datn.duong.FishSeller.dto.AppointmentDTO;
import datn.duong.FishSeller.entity.AddressEntity;
import datn.duong.FishSeller.entity.AppointmentEntity;
import datn.duong.FishSeller.entity.EmployeeEntity;
import datn.duong.FishSeller.entity.ServiceTypeEntity;
import datn.duong.FishSeller.entity.UserEntity;
import datn.duong.FishSeller.enums.AppointmentStatus;
import datn.duong.FishSeller.enums.PaymentStatus;
import datn.duong.FishSeller.repository.AddressRepository;
import datn.duong.FishSeller.repository.AppointmentRepository;
import datn.duong.FishSeller.repository.ServiceTypeRepository;
import datn.duong.FishSeller.repository.EmployeeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ServiceTypeRepository serviceTypeRepository;
    private final EmployeeRepository employeeRepository;
    private final AddressRepository addressRepository;
    private final UserService userService;
    private final EmailService emailService;

    // =========================================================================
    // PHẦN 1: USER METHODS (Khách hàng)
    // =========================================================================

    @Transactional
    public AppointmentDTO createBooking(AppointmentDTO dto) {
        // A. Lấy User hiện tại
        UserEntity currentUser = userService.getCurrentProfile();

        // B. Validate Dịch vụ
        ServiceTypeEntity service = serviceTypeRepository.findById(dto.getServiceTypeId())
                .orElseThrow(() -> new RuntimeException("Dịch vụ không tồn tại"));

        if (!service.getIsActive()) {
            throw new RuntimeException("Dịch vụ này đang tạm ngưng phục vụ");
        }

        // C. Logic xác định Thông tin liên hệ (Snapshot)
        String finalAddress = dto.getAddress();
        String finalPhone = dto.getPhoneNumber();
        String recipientName = ""; // Tên người nhận lấy từ sổ địa chỉ

        // Ưu tiên 1: Nếu khách chọn địa chỉ từ danh sách (AddressId)
        if (dto.getAddressId() != null) {
            AddressEntity addr = addressRepository.findById(dto.getAddressId())
                    .orElseThrow(() -> new RuntimeException("Địa chỉ không hợp lệ"));
            finalAddress = addr.getDetailedAddress();
            finalPhone = addr.getPhoneNumber();
            recipientName = addr.getRecipientName(); // Lấy tên người nhận từ đây
        } 
        // Ưu tiên 2: Nếu không có Id, kiểm tra xem khách có nhập tay không. 
        // Nếu trống cả hai, lấy địa chỉ mặc định của User
        else if (finalAddress == null || finalAddress.trim().isEmpty()) {
            AddressEntity defaultAddr = addressRepository.findByUserIdAndIsDefaultTrue(currentUser.getId())
                    .orElse(null);
            if (defaultAddr != null) {
                finalAddress = defaultAddr.getDetailedAddress();
                finalPhone = defaultAddr.getPhoneNumber();
                recipientName = defaultAddr.getRecipientName();
            }
        }

        // Ưu tiên 3: Trường hợp cuối cùng nếu vẫn chưa có SĐT -> Lấy SĐT chính của User
        if (finalPhone == null || finalPhone.trim().isEmpty()) {
            finalPhone = currentUser.getPhoneNumber();
        }

        // Kiểm tra bắt buộc phải có địa chỉ
        if (finalAddress == null || finalAddress.trim().isEmpty()) {
            throw new RuntimeException("Vui lòng cung cấp địa chỉ thực hiện dịch vụ!");
        }

        // D. Validate Thời gian
        if (dto.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Không thể đặt lịch trong quá khứ");
        }
        if (dto.getAppointmentDate().isEqual(LocalDate.now()) && dto.getAppointmentTime().isBefore(LocalTime.now())) {
            throw new RuntimeException("Giờ đặt phải sau giờ hiện tại");
        }

        // E. Tính toán giờ kết thúc
        LocalTime startTime = dto.getAppointmentTime();
        LocalTime endTime = startTime.plusMinutes(service.getEstimatedDuration());

        // F. Gộp Ghi chú (Note) kèm tên người nhận để Admin dễ quan sát
        String finalNote = dto.getNote();
        if (recipientName != null && !recipientName.isEmpty()) {
            finalNote = "[Người nhận: " + recipientName + "] " + (finalNote != null ? finalNote : "");
        }

        // G. Tạo Entity
        AppointmentEntity appointment = AppointmentEntity.builder()
                .user(currentUser)
                .serviceType(service)
                .phoneNumber(finalPhone) // SĐT đã chốt qua các bước ưu tiên
                .appointmentDate(dto.getAppointmentDate())
                .appointmentTime(startTime)
                .expectedEndTime(endTime)
                .address(finalAddress) // Địa chỉ đã chốt
                .note(finalNote)      // Ghi chú đã gộp tên người nhận
                .priceAtBooking(service.getPrice())
                .status(AppointmentStatus.PENDING)
                .paymentStatus(PaymentStatus.UNPAID)
                .build();

        return toDTO(appointmentRepository.save(appointment));
    }
    // 2. User gửi yêu cầu Hủy lịch
    @Transactional
    public void requestCancelBooking(Long appointmentId, String reason) {
        UserEntity currentUser = userService.getCurrentProfile();
        AppointmentEntity appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Lịch hẹn không tồn tại"));

        // BẢO MẬT: Check quyền sở hữu đơn hàng
        if (!appointment.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền yêu cầu hủy lịch hẹn của người khác!");
        }

        // Kiểm tra trạng thái: Chỉ cho phép yêu cầu hủy khi chưa thực hiện
        if (appointment.getStatus() != AppointmentStatus.PENDING && 
            appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new RuntimeException("Đơn hàng đã vào giai đoạn không thể yêu cầu hủy (Đang thực hiện hoặc đã xong).");
        }

        // Cập nhật trạng thái chờ duyệt
        appointment.setStatus(AppointmentStatus.CANCEL_REQUESTED);
        appointment.setCancellationReason("Khách yêu cầu hủy: " + reason);
        
        appointmentRepository.save(appointment);
    }

    // 3. Xem lịch sử của TÔI
    @Transactional(readOnly = true)
    public Page<AppointmentDTO> getMyAppointments(int page, int size) {
        UserEntity currentUser = userService.getCurrentProfile();
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        
        // Chỉ lấy theo ID của người đang đăng nhập
        return appointmentRepository.findByUserId(currentUser.getId(), pageable)
                .map(this::toDTO);
    }
    
    // 4. Xem chi tiết 1 đơn (Của tôi)
    @Transactional(readOnly = true)
    public AppointmentDTO getMyAppointmentDetail(Long id) {
        UserEntity currentUser = userService.getCurrentProfile();
        AppointmentEntity appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lịch hẹn không tồn tại"));

        // BẢO MẬT: Check chủ sở hữu
        if (!appointment.getUser().getId().equals(currentUser.getId())) {
             throw new RuntimeException("Access Denied: Đây không phải đơn hàng của bạn.");
        }
        
        return toDTO(appointment);
    }

    // 5. Đánh giá đơn hàng của chính mình
    @Transactional
    public void submitFeedback(Long appointmentId, Integer rating, String comment) {
        UserEntity currentUser = userService.getCurrentProfile();
        AppointmentEntity appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        // Check quyền sở hữu
        if (!appointment.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền đánh giá đơn này");
        }

        // Check trạng thái
        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new RuntimeException("Chỉ được đánh giá khi dịch vụ đã hoàn thành");
        }
        
        // Check đã đánh giá chưa
        if (appointment.getRating() != null) {
            throw new RuntimeException("Bạn đã đánh giá đơn này rồi!");
        }

        appointment.setRating(rating);
        appointment.setFeedbackComment(comment);
        appointmentRepository.save(appointment);
    }

    // =========================================================================
    // PHẦN 2: ADMIN METHODS (Quản lý & Phân công)
    // =========================================================================

    // 5. Admin Phân công nhân viên (Assign)
    @Transactional
    public AppointmentDTO assignEmployee(Long appointmentId, Long employeeId) {
        AppointmentEntity appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Lịch hẹn không tồn tại"));

        EmployeeEntity employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));

        // Check xem nhân viên có rảnh vào giờ đó không?
        boolean isBusy = appointmentRepository.existsByEmployeeAndDateTimeOverlap(
                employeeId,
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime(),
                appointment.getExpectedEndTime()
        );

        if (isBusy) {
            throw new RuntimeException("Nhân viên " + employee.getFullName() + " đã bị trùng lịch vào khung giờ này!");
        }

        appointment.setEmployee(employee);
        appointment.setStatus(AppointmentStatus.CONFIRMED); // Chuyển sang đã xác nhận
        return toDTO(appointmentRepository.save(appointment));
    }
    
    // 6. Admin cập nhật trạng thái (Ví dụ: Đã xong, Đã hủy)
    @Transactional
    public AppointmentDTO updateStatusByAdmin(Long id, AppointmentStatus status) {
        AppointmentEntity appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));
        
        appointment.setStatus(status);
        if (status == AppointmentStatus.COMPLETED) {
            appointment.setPaymentStatus(PaymentStatus.PAID); // Giả sử xong là thanh toán luôn
        }
        return toDTO(appointmentRepository.save(appointment));
    }

    // 7. Admin lấy danh sách (Có tìm kiếm + Lọc status)
    @Transactional(readOnly = true)
    public Page<AppointmentDTO> getAllForAdmin(String keyword, AppointmentStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        
        // Gọi hàm search vừa viết bên Repository
        return appointmentRepository.searchAppointments(keyword, status, pageable)
                .map(this::toDTO);
    }

    // 8. Duyệt hủy theo yêu cầu của user
    @Transactional
    public AppointmentDTO handleCancellationReview(Long id, boolean approve, String adminReason) {
        AppointmentEntity appt = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lịch hẹn không tồn tại"));

        // Kiểm tra: Phải đúng là đang ở trạng thái khách yêu cầu hủy mới xử lý được
        if (appt.getStatus() != AppointmentStatus.CANCEL_REQUESTED) {
            throw new RuntimeException("Lịch hẹn này không nằm trong danh sách chờ duyệt hủy.");
        }

        if (approve) {
            // ĐỒNG Ý HỦY
            appt.setStatus(AppointmentStatus.CANCELLED);
            appt.setCancellationReason(appt.getCancellationReason() + " | [Admin duyệt]: Đồng ý hủy.");
            
            sendAppointmentCancellationEmail(appt, 
                "Lịch hẹn #" + id + " đã được hủy thành công", 
                "Yêu cầu hủy lịch hẹn của bạn đã được Admin <b>CHẤP NHẬN</b>.");
        } else {
            // TỪ CHỐI HỦY -> Quay lại trạng thái trước đó (mặc định cho về CONFIRMED)
            appt.setStatus(AppointmentStatus.CONFIRMED);
            String rejectMsg = "Yêu cầu hủy lịch hẹn của bạn đã bị <b>TỪ CHỐI</b>.";
            if (adminReason != null) rejectMsg += "<br/><b>Lý do từ chối:</b> " + adminReason;
            
            sendAppointmentCancellationEmail(appt, 
                "Yêu cầu hủy lịch hẹn #" + id + " bị từ chối", 
                rejectMsg);
        }
        return toDTO(appointmentRepository.save(appt));
    }

    //  9. ADMIN CHỦ ĐỘNG HỦY LỊCH
    @Transactional
    public AppointmentDTO cancelByAdmin(Long id, String reason) {
        AppointmentEntity appt = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lịch hẹn không tồn tại"));

        if (appt.getStatus() == AppointmentStatus.COMPLETED) {
            throw new RuntimeException("Không thể hủy lịch hẹn đã hoàn thành!");
        }

        appt.setStatus(AppointmentStatus.CANCELLED);
        String finalReason = (reason != null && !reason.isEmpty()) ? reason : "Shop bận đột xuất hoặc có sự cố vận hành.";
        appt.setCancellationReason("[ADMIN CHỦ ĐỘNG HỦY]: " + finalReason);
        // Nếu đơn hàng đã thanh toán trước đó (nếu có logic hoàn tiền thì thêm ở đây)
        // appointment.setPaymentStatus(PaymentStatus.REFUNDED); 
        
        sendAppointmentCancellationEmail(appt, 
            "Thông báo hủy lịch hẹn #" + id, 
            "Chúng tôi rất tiếc phải thông báo lịch hẹn của bạn đã bị hủy bởi Admin.<br/><b>Lý do:</b> " + finalReason);

        return toDTO(appointmentRepository.save(appt));
    }

    // =========================================================================
    // HÀM GỬI EMAIL THÔNG BÁO DÙNG CHUNG
    // =========================================================================
    private void sendAppointmentCancellationEmail(AppointmentEntity appt, String subject, String content) {
        if (appt.getUser().getEmail() == null) return;

        String finalContent = (content != null) ? content : "Thông báo mới về lịch hẹn của bạn.";

        String htmlBody = String.format(
            "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #e2e8f0; border-radius: 8px;'>" +
            "  <h2 style='color: #0891b2;'>Thông báo Lịch hẹn #%d</h2>" +
            "  <p>Xin chào <b>%s</b>,</p>" +
            "  <p>%s</p>" +
            "  <div style='background-color: #f8fafc; padding: 15px; border-radius: 5px; margin: 15px 0;'>" +
            "    <p style='margin: 0;'><b>Dịch vụ:</b> %s</p>" +
            "    <p style='margin: 5px 0;'><b>Thời gian:</b> %s ngày %s</p>" +
            "    <p style='margin: 0;'><b>Địa chỉ:</b> %s</p>" +
            "  </div>" +
            "  <p style='color: #64748b; font-size: 12px;'>Đây là email tự động từ hệ thống Cá Cảnh Shop.</p>" +
            "</div>",
            appt.getId(),
            appt.getUser().getUsername(),
            finalContent,
            appt.getServiceType().getName(),
            appt.getAppointmentTime(),
            appt.getAppointmentDate(),
            appt.getAddress()
        );

        CompletableFuture.runAsync(() -> {
            try {
                emailService.sendEmail(appt.getUser().getEmail(), subject, htmlBody);
            } catch (Exception e) {
                System.err.println("Lỗi gửi email lịch hẹn: " + e.getMessage());
            }
        });
    }

    // =========================================================================
    // MAPPING
    // =========================================================================

    public AppointmentDTO toDTO(AppointmentEntity entity) {
        if (entity == null) return null;
        
        return AppointmentDTO.builder()
                .id(entity.getId())
                .createdDate(entity.getCreatedDate())
                .updatedDate(entity.getUpdatedDate())
                // User info
                .userId(entity.getUser().getId())
                .userFullName(entity.getUser().getFullName())
                .username(entity.getUser().getUsername())
                .phoneNumber(entity.getPhoneNumber())
                .email(entity.getUser().getEmail())
                // Service info
                .serviceTypeId(entity.getServiceType().getId())
                .serviceTypeName(entity.getServiceType().getName())
                .serviceImage(entity.getServiceType().getImageUrl())
                // Employee info (Check null)
                .employeeId(entity.getEmployee() != null ? entity.getEmployee().getId() : null)
                .employeeName(entity.getEmployee() != null ? entity.getEmployee().getFullName() : "Chưa phân công")
                // Main info
                .appointmentDate(entity.getAppointmentDate())
                .appointmentTime(entity.getAppointmentTime())
                .expectedEndTime(entity.getExpectedEndTime())
                .address(entity.getAddress())
                .note(entity.getNote())
                .priceAtBooking(entity.getPriceAtBooking())
                .status(entity.getStatus())
                .paymentStatus(entity.getPaymentStatus())
                .cancellationReason(entity.getCancellationReason())
                .rating(entity.getRating())
                .feedbackComment(entity.getFeedbackComment())
                .build();
    }
    
    // Hàm toEntity thường ít dùng cho Appointment vì logic tạo phức tạp, 
    // ta thường set thủ công trong hàm createBooking như trên.
}