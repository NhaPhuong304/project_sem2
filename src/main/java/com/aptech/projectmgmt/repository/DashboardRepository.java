package com.aptech.projectmgmt.repository;

import com.aptech.projectmgmt.model.dashboard.AdminDashboardData;
import com.aptech.projectmgmt.model.dashboard.ChartPoint;
import com.aptech.projectmgmt.model.dashboard.MetricCard;
import com.aptech.projectmgmt.model.dashboard.StaffDashboardData;
import com.aptech.projectmgmt.model.dashboard.StudentDashboardData;
import com.aptech.projectmgmt.model.dashboard.TeacherDashboardData;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardRepository extends BaseRepository {

    public StaffDashboardData getStaffDashboardData(int staffId) throws SQLException {
        List<MetricCard> metrics = List.of(
                new MetricCard("Managed classes", String.valueOf(queryCount(
                        "SELECT COUNT(*) FROM Class WHERE ManagerID = ?", staffId)), "Classes owned by this staff account"),
                new MetricCard("Students", String.valueOf(queryCount(
                        "SELECT COUNT(*) FROM Student s INNER JOIN Class c ON c.ClassID = s.ClassID WHERE c.ManagerID = ?", staffId)),
                        "Students across managed classes"),
                new MetricCard("Active projects", String.valueOf(queryCount(
                        "SELECT COUNT(*) FROM Project p INNER JOIN ProjectGroup pg ON pg.GroupID = p.GroupID "
                                + "INNER JOIN Class c ON c.ClassID = pg.ClassID WHERE c.ManagerID = ? AND p.Status = 1", staffId)),
                        "Projects currently in progress"),
                new MetricCard("Open submissions", String.valueOf(queryCount(
                        "SELECT CASE WHEN OBJECT_ID('SubmissionRequest', 'U') IS NULL THEN 0 ELSE "
                                + "(SELECT COUNT(*) FROM SubmissionRequest WHERE CreatedByStaffID = ? AND Status = 1) END", staffId)),
                        "Submission requests still open")
        );

        return new StaffDashboardData(
                metrics,
                queryPoints("SELECT c.ClassName, COUNT(s.StudentID) "
                                + "FROM Class c LEFT JOIN Student s ON s.ClassID = c.ClassID "
                                + "WHERE c.ManagerID = ? GROUP BY c.ClassName ORDER BY c.ClassName", staffId),
                queryPoints("SELECT CASE p.Status WHEN 1 THEN 'Active' WHEN 2 THEN 'Completed' ELSE 'Other' END, COUNT(*) "
                                + "FROM Project p INNER JOIN ProjectGroup pg ON pg.GroupID = p.GroupID "
                                + "INNER JOIN Class c ON c.ClassID = pg.ClassID "
                                + "WHERE c.ManagerID = ? "
                                + "GROUP BY p.Status ORDER BY p.Status", staffId),
                queryMonthlyPoints("SELECT CONVERT(char(7), t.ActualEndDate, 120), COUNT(*) "
                                + "FROM Task t INNER JOIN ProjectGroup pg ON pg.GroupID = t.GroupID "
                                + "INNER JOIN Class c ON c.ClassID = pg.ClassID "
                                + "WHERE c.ManagerID = ? AND t.ActualEndDate IS NOT NULL "
                                + "AND t.ActualEndDate >= DATEADD(MONTH, -5, GETDATE()) "
                                + "GROUP BY CONVERT(char(7), t.ActualEndDate, 120) "
                                + "ORDER BY CONVERT(char(7), t.ActualEndDate, 120)", 6, staffId),
                queryMonthlyPoints("SELECT CONVERT(char(7), t.EstimatedEndDate, 120), COUNT(*) "
                                + "FROM Task t INNER JOIN ProjectGroup pg ON pg.GroupID = t.GroupID "
                                + "INNER JOIN Class c ON c.ClassID = pg.ClassID "
                                + "WHERE c.ManagerID = ? "
                                + "AND (t.IsLate = 1 OR (t.Status <> 5 AND t.EstimatedEndDate < GETDATE())) "
                                + "AND t.EstimatedEndDate >= DATEADD(MONTH, -5, GETDATE()) "
                                + "GROUP BY CONVERT(char(7), t.EstimatedEndDate, 120) "
                                + "ORDER BY CONVERT(char(7), t.EstimatedEndDate, 120)", 6, staffId)
        );
    }

    public AdminDashboardData getAdminDashboardData() throws SQLException {
        List<MetricCard> metrics = List.of(
                new MetricCard("Accounts", String.valueOf(queryCount("SELECT COUNT(*) FROM Account")), "All platform accounts"),
                new MetricCard("Staff", String.valueOf(queryCount("SELECT COUNT(*) FROM Account WHERE Role = 4")), "Operations staff"),
                new MetricCard("Teachers", String.valueOf(queryCount("SELECT COUNT(*) FROM Account WHERE Role = 3")), "Academic supervisors"),
                new MetricCard("Students", String.valueOf(queryCount("SELECT COUNT(*) FROM Account WHERE Role = 2")), "Learners in the system")
        );

        return new AdminDashboardData(
                metrics,
                queryPoints("SELECT CASE Role WHEN 1 THEN 'Admin' WHEN 2 THEN 'Student' WHEN 3 THEN 'Teacher' WHEN 4 THEN 'Staff' END, COUNT(*) "
                                + "FROM Account GROUP BY Role ORDER BY Role"),
                queryPoints("SELECT s.FullName, COUNT(c.ClassID) "
                                + "FROM Staff s LEFT JOIN Class c ON c.ManagerID = s.StaffID "
                                + "INNER JOIN Account a ON a.AccountID = s.AccountID "
                                + "WHERE a.Role = 4 GROUP BY s.FullName ORDER BY s.FullName"),
                queryPoints("SELECT s.FullName, COUNT(p.ProjectID) "
                                + "FROM Staff s LEFT JOIN Project p ON p.AdvisorID = s.StaffID "
                                + "INNER JOIN Account a ON a.AccountID = s.AccountID "
                                + "WHERE a.Role = 3 GROUP BY s.FullName ORDER BY s.FullName"),
                queryMonthlyPoints("SELECT CONVERT(char(7), CreatedAt, 120), COUNT(*) "
                                + "FROM Account WHERE CreatedAt >= DATEADD(MONTH, -5, GETDATE()) "
                                + "GROUP BY CONVERT(char(7), CreatedAt, 120) "
                                + "ORDER BY CONVERT(char(7), CreatedAt, 120)", 6)
        );
    }

    public TeacherDashboardData getTeacherDashboardData(int teacherStaffId) throws SQLException {
        List<MetricCard> metrics = List.of(
                new MetricCard("Assigned classes", String.valueOf(queryCount(
                        "SELECT COUNT(DISTINCT pg.ClassID) FROM Project p INNER JOIN ProjectGroup pg ON pg.GroupID = p.GroupID "
                                + "WHERE p.AdvisorID = ?", teacherStaffId)), "Classes with supervised projects"),
                new MetricCard("Supervised projects", String.valueOf(queryCount(
                        "SELECT COUNT(*) FROM Project WHERE AdvisorID = ?", teacherStaffId)), "Projects under supervision"),
                new MetricCard("Review queue", String.valueOf(queryCount(
                        "SELECT COUNT(*) FROM Task t INNER JOIN Project p ON p.GroupID = t.GroupID "
                                + "WHERE p.AdvisorID = ? AND t.Status IN (3, 4)", teacherStaffId)), "Tasks waiting for review or revision"),
                new MetricCard("Finished tasks", String.valueOf(queryCount(
                        "SELECT COUNT(*) FROM Task t INNER JOIN Project p ON p.GroupID = t.GroupID "
                                + "WHERE p.AdvisorID = ? AND t.Status = 5", teacherStaffId)), "Tasks marked completed")
        );

        return new TeacherDashboardData(
                metrics,
                queryPoints("SELECT c.ClassName, COUNT(p.ProjectID) "
                                + "FROM Project p INNER JOIN ProjectGroup pg ON pg.GroupID = p.GroupID "
                                + "INNER JOIN Class c ON c.ClassID = pg.ClassID "
                                + "WHERE p.AdvisorID = ? GROUP BY c.ClassName ORDER BY c.ClassName", teacherStaffId),
                queryPoints("SELECT CASE t.Status WHEN 1 THEN 'Pending' WHEN 2 THEN 'In progress' WHEN 3 THEN 'Reviewing' "
                                + "WHEN 4 THEN 'Revising' WHEN 5 THEN 'Completed' ELSE 'Other' END, COUNT(*) "
                                + "FROM Task t INNER JOIN Project p ON p.GroupID = t.GroupID "
                                + "WHERE p.AdvisorID = ? GROUP BY t.Status ORDER BY t.Status", teacherStaffId),
                queryMonthlyPoints("SELECT CONVERT(char(7), t.ActualEndDate, 120), COUNT(*) "
                                + "FROM Task t INNER JOIN Project p ON p.GroupID = t.GroupID "
                                + "WHERE p.AdvisorID = ? AND t.ActualEndDate IS NOT NULL "
                                + "AND t.ActualEndDate >= DATEADD(MONTH, -5, GETDATE()) "
                                + "GROUP BY CONVERT(char(7), t.ActualEndDate, 120) "
                                + "ORDER BY CONVERT(char(7), t.ActualEndDate, 120)", 6, teacherStaffId)
        );
    }

    public StudentDashboardData getStudentDashboardData(int studentId) throws SQLException {
        List<MetricCard> metrics = List.of(
                new MetricCard("My projects", String.valueOf(queryCount(
                        "SELECT COUNT(DISTINCT GroupID) FROM GroupMember WHERE StudentID = ? AND Status = 1", studentId)),
                        "Active groups you belong to"),
                new MetricCard("My tasks", String.valueOf(queryCount(
                        "SELECT COUNT(*) FROM Task WHERE AssignedTo = ? OR ReviewedBy = ? OR CreatedBy = ?", studentId, studentId, studentId)),
                        "Tasks assigned, reviewed, or created by you"),
                new MetricCard("Overdue tasks", String.valueOf(queryCount(
                        "SELECT COUNT(*) FROM Task WHERE (AssignedTo = ? OR ReviewedBy = ? OR CreatedBy = ?) "
                                + "AND (IsLate = 1 OR (Status <> 5 AND EstimatedEndDate < GETDATE()))", studentId, studentId, studentId)),
                        "Work items needing attention"),
                new MetricCard("Pending submissions", String.valueOf(queryCount(
                        "SELECT CASE WHEN OBJECT_ID('SubmissionTarget', 'U') IS NULL THEN 0 ELSE "
                                + "(SELECT COUNT(*) FROM SubmissionTarget WHERE LeaderStudentID = ? AND Status IN (1, 2)) END", studentId)),
                        "Submission targets still open")
        );

        return new StudentDashboardData(
                metrics,
                queryPoints("SELECT CASE Status WHEN 1 THEN 'Pending' WHEN 2 THEN 'In progress' WHEN 3 THEN 'Reviewing' "
                                + "WHEN 4 THEN 'Revising' WHEN 5 THEN 'Completed' ELSE 'Other' END, COUNT(*) "
                                + "FROM Task WHERE AssignedTo = ? OR ReviewedBy = ? OR CreatedBy = ? "
                                + "GROUP BY Status ORDER BY Status", studentId, studentId, studentId),
                queryMonthlyPoints("SELECT CONVERT(char(7), EstimatedEndDate, 120), COUNT(*) "
                                + "FROM Task WHERE (AssignedTo = ? OR ReviewedBy = ? OR CreatedBy = ?) "
                                + "AND EstimatedEndDate >= DATEADD(MONTH, -5, GETDATE()) "
                                + "GROUP BY CONVERT(char(7), EstimatedEndDate, 120) "
                                + "ORDER BY CONVERT(char(7), EstimatedEndDate, 120)", 6, studentId, studentId, studentId)
        );
    }

    private int queryCount(String sql, Object... params) throws SQLException {
        return executeQuery(sql, rs -> rs.next() ? rs.getInt(1) : 0, params);
    }

    private List<ChartPoint> queryPoints(String sql, Object... params) throws SQLException {
        return executeQuery(sql, rs -> {
            List<ChartPoint> result = new ArrayList<>();
            while (rs.next()) {
                result.add(new ChartPoint(rs.getString(1), rs.getInt(2)));
            }
            return result;
        }, params);
    }

    private List<ChartPoint> queryMonthlyPoints(String sql, int months, Object... params) throws SQLException {
        List<ChartPoint> rawPoints = queryPoints(sql, params);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        Map<String, Integer> pointMap = new LinkedHashMap<>();
        LocalDate startMonth = LocalDate.now().withDayOfMonth(1).minusMonths(months - 1L);

        for (int i = 0; i < months; i++) {
            String key = startMonth.plusMonths(i).format(formatter);
            pointMap.put(key, 0);
        }

        for (ChartPoint point : rawPoints) {
            if (point.label() != null && pointMap.containsKey(point.label())) {
                pointMap.put(point.label(), point.value());
            }
        }

        return pointMap.entrySet().stream()
                .map(entry -> new ChartPoint(entry.getKey(), entry.getValue()))
                .toList();
    }
}
