package com.aptech.projectmgmt.util;

import com.aptech.projectmgmt.model.Account;
import com.aptech.projectmgmt.model.Staff;
import com.aptech.projectmgmt.model.Student;

import java.util.ArrayList;
import java.util.List;

public class SessionManager {

    private static SessionManager instance;

    private Account currentAccount;
    private Student currentStudent;
    private Staff currentStaff;
    private String currentScreenFxmlPath;
    private String currentScreenController;
    private final List<String> recentScreenFxmlPaths = new ArrayList<>();

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public Account getCurrentAccount() { return currentAccount; }
    public void setCurrentAccount(Account currentAccount) { this.currentAccount = currentAccount; }

    public Student getCurrentStudent() { return currentStudent; }
    public void setCurrentStudent(Student currentStudent) { this.currentStudent = currentStudent; }

    public Staff getCurrentStaff() { return currentStaff; }
    public void setCurrentStaff(Staff currentStaff) { this.currentStaff = currentStaff; }

    public String getCurrentScreenFxmlPath() { return currentScreenFxmlPath; }
    public void setCurrentScreenFxmlPath(String currentScreenFxmlPath) { this.currentScreenFxmlPath = currentScreenFxmlPath; }

    public String getCurrentScreenController() { return currentScreenController; }
    public void setCurrentScreenController(String currentScreenController) { this.currentScreenController = currentScreenController; }

    public List<String> getRecentScreenFxmlPaths() {
        return List.copyOf(recentScreenFxmlPaths);
    }

    public void pushRecentScreenFxmlPath(String fxmlPath) {
        if (fxmlPath == null || fxmlPath.isBlank()) {
            return;
        }
        recentScreenFxmlPaths.remove(fxmlPath);
        recentScreenFxmlPaths.add(0, fxmlPath);
        while (recentScreenFxmlPaths.size() > 6) {
            recentScreenFxmlPaths.remove(recentScreenFxmlPaths.size() - 1);
        }
    }

    public void clearSession() {
        currentAccount = null;
        currentStudent = null;
        currentStaff = null;
        currentScreenFxmlPath = null;
        currentScreenController = null;
        recentScreenFxmlPaths.clear();
    }
}
