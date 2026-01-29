package com.internship.rblp.models.enums;

public enum AuditAction {
    SIGNUP("User Signup"),
    LOGIN("User Login"),
    LOGOUT("User Logout"),
    SUBMIT_KYC("KYC Submit"),
    APPROVE_KYC("KYC Approval"),
    REJECT_KYC("KYC Rejection"),
    FETCH_KYC_STATUS("Fetch KYC Status"),
    UPDATE_PROFILE("User Profile Updated"),
    FETCH_KYC_ALL("Fetch KYC Request List"),
    FETCH_KYC_ONE("Fetch KYC Details"),
    TOGGLE_USER("User Status Toggle"),
    BULK_UPLOAD("Bulk Upload"),
    FETCH_USER_LIST("Fetch User List"),
    BULK_UPLOAD_STATUS("Fetch Bulk Status"),
    BULK_UPLOAD_ERRORS("Fetch Bulk Errors"),
    SAVE_FILE("File Saved to local");

    private final String description;
    AuditAction(String description) {
        this.description = description;
    }
    public String getDescription(){
        return description;
    }
}
