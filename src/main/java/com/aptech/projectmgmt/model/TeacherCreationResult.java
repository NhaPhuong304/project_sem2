package com.aptech.projectmgmt.model;

public class TeacherCreationResult {

    private final String username;
    private final String temporaryPassword;
    private final boolean notificationEmailSent;

    public TeacherCreationResult(String username, String temporaryPassword, boolean notificationEmailSent) {
        this.username = username;
        this.temporaryPassword = temporaryPassword;
        this.notificationEmailSent = notificationEmailSent;
    }

    public String getUsername() {
        return username;
    }

    public String getTemporaryPassword() {
        return temporaryPassword;
    }

    public boolean isNotificationEmailSent() {
        return notificationEmailSent;
    }
}
