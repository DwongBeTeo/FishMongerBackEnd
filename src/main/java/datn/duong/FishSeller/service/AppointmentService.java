package datn.duong.FishSeller.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import datn.duong.FishSeller.dto.AppointmentDTO;
import datn.duong.FishSeller.entity.AppointmentEntity;
import datn.duong.FishSeller.entity.EmployeeEntity;
import datn.duong.FishSeller.entity.ServiceTypeEntity;
import datn.duong.FishSeller.entity.UserEntity;
import datn.duong.FishSeller.enums.AppointmentStatus;
import datn.duong.FishSeller.enums.PaymentStatus;
import datn.duong.FishSeller.repository.AppointmentRepository;
import datn.duong.FishSeller.repository.ServiceTypeRepository;
import datn.duong.FishSeller.repository.EmployeeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ServiceTypeRepository serviceTypeRepository;
    private final EmployeeRepository employeeRepository;
    private final UserService userService; // Để lấy User hiện tại

    // =========================================================================
    // PHẦN 1: USER METHODS (Khách hàng)
    // =========================================================================

    // 1. Đặt lịch mới
    @Transactional
    public AppointmentDTO createBooking(AppointmentDTO dto) {
        // A. Lấy User hiện tại (Bảo mật: Luôn dùng user từ token, ko dùng user từ dto gửi lên)
        UserEntity currentUser = userService.getCurrentProfile();

        // B. Validate Dịch vụ
        ServiceTypeEntity service = serviceTypeRepository.findById(dto.getServiceTypeId())
                .orElseThrow(() -> new RuntimeException("Dịch vụ không tồn tại"));
        
        if (!service.getIsActive()) {
            throw new RuntimeException("Dịch vụ này đang tạm ngưng phục vụ");
        }

        // C. Validate Thời gian (Không được đặt quá khứ)
        if (dto.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Không thể đặt lịch trong quá khứ");
        }
        if (dto.getAppointmentDate().isEqual(LocalDate.now()) && dto.getAppointmentTime().isBefore(LocalTime.now())) {
            throw new RuntimeException("Giờ đặt phải sau giờ hiện tại");
        }

        // D. Tính toán giờ kết thúc
        LocalTime startTime = dto.getAppointmentTime();
        LocalTime endTime = startTime.plusMinutes(service.getEstimatedDuration());

        // E LOGIC XỬ LÝ SỐ ĐIỆN THOẠI ---
        String contactPhone = dto.getPhoneNumber();
        
        // Nếu khách KHÔNG nhập SĐT riêng -> Lấy SĐT trong hồ sơ User
        if (contactPhone == null || contactPhone.trim().isEmpty()) {
            contactPhone = currentUser.getPhoneNumber();
        }
        
        // Nếu hồ sơ cũng không có SĐT -> Báo lỗi bắt buộc nhập
        if (contactPhone == null || contactPhone.trim().isEmpty()) {
            throw new RuntimeException("Vui lòng cung cấp số điện thoại liên hệ!");
        }

        // F. Tạo Entity
        AppointmentEntity appointment = AppointmentEntity.builder()
                .user(currentUser) // Gán cứng User hiện tại
                .serviceType(service)
                .phoneNumber(contactPhone)
                .appointmentDate(dto.getAppointmentDate())
                .appointmentTime(startTime)
                .expectedEndTime(endTime)
                .address(dto.getAddress())
                .note(dto.getNote())
                .priceAtBooking(service.getPrice()) // Snapshot giá tại thời điểm đặt
                .status(AppointmentStatus.PENDING)  // Mới đặt là Chờ xác nhận
                .paymentStatus(PaymentStatus.UNPAID)
                .employee(null) // Chưa phân công
                .build();

        return toDTO(appointmentRepository.save(appointment));
    }

    // 2. User Hủy lịch
    @Transactional
    public void cancelBookingByUser(Long appointmentId, String reason) {
        UserEntity currentUser = userService.getCurrentProfile();
        AppointmentEntity appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Lịch hẹn không tồn tại"));

        // BẢO MẬT: Check xem lịch này có phải của ông user đang login không?
        if (!appointment.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền hủy lịch hẹn của người khác!");
        }

        // Chỉ cho hủy khi còn PENDING hoặc CONFIRMED
        if (appointment.getStatus() != AppointmentStatus.PENDING && appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new RuntimeException("Không thể hủy khi đơn đang thực hiện hoặc đã hoàn thành");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancellationReason("Khách hủy: " + reason);
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
                // .userPhoneNumber(entity.getUser().getPhoneNumber())
                .phoneNumber(entity.getPhoneNumber())
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