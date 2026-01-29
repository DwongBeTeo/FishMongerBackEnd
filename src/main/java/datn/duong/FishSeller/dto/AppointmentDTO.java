package datn.duong.FishSeller.dto;

import java.time.*;

import com.fasterxml.jackson.annotation.JsonFormat;

import datn.duong.FishSeller.enums.AppointmentStatus;
import datn.duong.FishSeller.enums.PaymentStatus;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class AppointmentDTO extends BaseDTO {

    private Long addressId; // ID từ bảng user_addresses

    // --- 1. THÔNG TIN KHÁCH HÀNG ---
    private Long userId;            // Input: Dùng khi tạo đơn
    private String userFullName;    // Output: Hiển thị tên khách
    private String username;
    // private String userPhoneNumber; // Output: SĐT khách
    private String phoneNumber; // Input (Khách nhập) & Output (Hiển thị)
    private String email;

    // --- 2. THÔNG TIN DỊCH VỤ ---
    private Long serviceTypeId;     // Input
    private String serviceTypeName; // Output
    private String serviceImage;    // Output: Hiển thị ảnh dịch vụ

    // --- 3. THÔNG TIN NHÂN VIÊN ---
    private Long employeeId;        // Input: Admin gán nhân viên
    private String employeeName;    // Output: Hiển thị tên nhân viên

    // --- 4. THỜI GIAN & ĐỊA ĐIỂM ---
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate appointmentDate;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime appointmentTime;
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime expectedEndTime; // Backend tự tính toán trả về

    private String address;
    private String note;

    // --- 5. TÀI CHÍNH & TRẠNG THÁI ---
    private Double priceAtBooking; // Giá chốt tại thời điểm đặt
    
    private AppointmentStatus status;       // PENDING, CONFIRMED...
    private PaymentStatus paymentStatus;    // UNPAID, PAID...
    
    private String cancellationReason;

    // --- 6. ĐÁNH GIÁ (Feedback) ---
    private Integer rating;
    private String feedbackComment;
}