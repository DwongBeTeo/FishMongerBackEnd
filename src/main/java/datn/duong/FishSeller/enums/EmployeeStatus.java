package datn.duong.FishSeller.enums;

public enum EmployeeStatus {
    ACTIVE("Đang làm việc"),
    INACTIVE("Đã nghỉ việc"),
    ON_LEAVE("Đang nghỉ phép");

    private final String displayName;

    EmployeeStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
