package datn.duong.FishSeller.enums;

public enum OrderStatus {
    // Định nghĩa các trạng thái kèm mô tả tiếng Việt
    PENDING("Chờ xác nhận"),      // Khách mới đặt, chưa ai xử lý
    PREPARING("Đang chuẩn bị"),   // Shop đang đóng gói/bắt cá
    SHIPPING("Đang giao hàng"),   // Đã giao cho shipper
    COMPLETED("Đã hoàn thành"),   // Khách đã nhận và thanh toán
    CANCELLED("Đã hủy");        // Khách hoặc Shop hủy đơn

    // Biến lưu giá trị hiển thị
    private final String displayName;

    // Constructor (Bắt buộc phải là private hoặc package-private trong Enum)
    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    // Getter để lấy tên tiếng Việt ra dùng
    public String getDisplayName() {
        return displayName;
    }
}
