package com.aptech.projectmgmt.model.dashboard;

import java.util.List;

public record TeacherDashboardData(
        List<MetricCard> metrics,
        List<ChartPoint> projectsByClass,
        List<ChartPoint> tasksByStatus,
        List<ChartPoint> completedTasksByMonth
) {
}
