package datn.duong.FishSeller.enums;

public enum PaymentMethod {
    COD("Thanh toán khi nhận hàng"),
    BANKING("Chuyển khoản ngân hàng");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
