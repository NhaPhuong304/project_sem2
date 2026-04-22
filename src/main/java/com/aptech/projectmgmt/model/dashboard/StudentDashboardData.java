package com.aptech.projectmgmt.model.dashboard;

import java.util.List;

public record StudentDashboardData(
        List<MetricCard> metrics,
        List<ChartPoint> tasksByStatus,
        List<ChartPoint> tasksByMonth
) {
}
