package com.aptech.projectmgmt.util;

import com.aptech.projectmgmt.model.Account;
import com.aptech.projectmgmt.model.Staff;
import com.aptech.projectmgmt.model.Student;

public class SessionManager {

    private static SessionManager instance;

    private Account currentAccount;
    private Student currentStudent;
    private Staff currentStaff;

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

    public void clearSession() {
        currentAccount = null;
        currentStudent = null;
        currentStaff = null;
    }
}
