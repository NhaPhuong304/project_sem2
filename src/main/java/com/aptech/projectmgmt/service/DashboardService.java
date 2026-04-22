package com.aptech.projectmgmt.service;

import com.aptech.projectmgmt.model.dashboard.AdminDashboardData;
import com.aptech.projectmgmt.model.dashboard.StaffDashboardData;
import com.aptech.projectmgmt.model.dashboard.StudentDashboardData;
import com.aptech.projectmgmt.model.dashboard.TeacherDashboardData;
import com.aptech.projectmgmt.repository.DashboardRepository;

public class DashboardService {
    private final DashboardRepository dashboardRepository = new DashboardRepository();

    public StaffDashboardData getStaffDashboardData(int staffId) {
        try {
            return dashboardRepository.getStaffDashboardData(staffId);
        } catch (Exception e) {
            throw new RuntimeException("Unable to load staff dashboard data", e);
        }
    }

    public AdminDashboardData getAdminDashboardData() {
        try {
            return dashboardRepository.getAdminDashboardData();
        } catch (Exception e) {
            throw new RuntimeException("Unable to load admin dashboard data", e);
        }
    }

    public TeacherDashboardData getTeacherDashboardData(int teacherStaffId) {
        try {
            return dashboardRepository.getTeacherDashboardData(teacherStaffId);
        } catch (Exception e) {
            throw new RuntimeException("Unable to load teacher dashboard data", e);
        }
    }

    public StudentDashboardData getStudentDashboardData(int studentId) {
        try {
            return dashboardRepository.getStudentDashboardData(studentId);
        } catch (Exception e) {
            throw new RuntimeException("Unable to load student dashboard data", e);
        }
    }
}
