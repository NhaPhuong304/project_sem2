package com.aptech.projectmgmt.util;

public final class NotificationUtil {

    public static final String REMINDER_PREFIX = "SYSTEM_REMINDER|";

    private NotificationUtil() {
    }

    public static String buildReminderContent(String content) {
        return REMINDER_PREFIX + content;
    }

    public static String stripSystemPrefix(String content) {
        if (content == null) {
            return null;
        }
        if (content.startsWith(REMINDER_PREFIX)) {
            return content.substring(REMINDER_PREFIX.length());
        }
        return content;
    }
}
