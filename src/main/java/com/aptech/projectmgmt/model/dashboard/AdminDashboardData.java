package com.aptech.projectmgmt.model.dashboard;

import java.util.List;

public record AdminDashboardData(
        List<MetricCard> metrics,
        List<ChartPoint> accountsByRole,
        List<ChartPoint> classesByManager,
        List<ChartPoint> projectsByAdvisor,
        List<ChartPoint> accountsCreatedByMonth
) {
}
