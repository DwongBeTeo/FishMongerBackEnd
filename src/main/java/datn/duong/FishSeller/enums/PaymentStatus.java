package datn.duong.FishSeller.enums;

public enum PaymentStatus {
    UNPAID("Chưa thanh toán"),
    PAID("Đã thanh toán"),
    REFUNDED("Đã hoàn tiền"); // Thêm cái này phòng khi hủy đơn đã thanh toán

    private final String displayName;

    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
