package datn.duong.FishSeller.enums;

public enum DiscountType {
    PERCENTAGE("Giảm theo phần trăm (%)"),
    FIXED_AMOUNT("Giảm số tiền cố định (VND)");

    private final String displayName;

    DiscountType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}