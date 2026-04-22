package com.aptech.projectmgmt.model.dashboard;

import java.util.List;

public record StaffDashboardData(
        List<MetricCard> metrics,
        List<ChartPoint> studentsByClass,
        List<ChartPoint> projectsByStatus,
        List<ChartPoint> completedTasksByMonth,
        List<ChartPoint> overdueTasksByMonth
) {
}
