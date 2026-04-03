package com.aptech.projectmgmt.model;

public class Staff {

    private int staffId;
    private String fullName;
    private String email;
    private int accountId;
    private String username;
    private String photoUrl;
    private boolean active;

    public Staff() {}

    public int getStaffId() { return staffId; }
    public void setStaffId(int staffId) { this.staffId = staffId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
