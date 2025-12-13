package datn.duong.FishSeller.enums;

public enum AppointmentStatus{
    PENDING("Chờ xác nhận"),      // Khách mới đặt lịch
    CONFIRMED("Đã xác nhận"),     // Shop đã gọi điện chốt lịch
    IN_PROCESS("Đang thực hiện"), // Nhân viên đang đến nhà làm dịch vụ
    COMPLETED("Đã hoàn thành"),   // Dịch vụ xong, khách đã trả tiền
    CANCELLED("Đã hủy");          // Lịch bị hủy

    private final String displayName;

    AppointmentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
