package com.aptech.projectmgmt.controller.admin;

import com.aptech.projectmgmt.model.dashboard.AdminDashboardData;
import com.aptech.projectmgmt.model.dashboard.ChartPoint;
import com.aptech.projectmgmt.model.dashboard.MetricCard;
import com.aptech.projectmgmt.service.DashboardService;
import com.aptech.projectmgmt.util.AlertUtil;
import com.aptech.projectmgmt.util.DashboardChartHelper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class AdminOverviewController {
    @FXML private HBox metricContainer;
    @FXML private PieChart accountRoleChart;
    @FXML private BarChart<String, Number> classesByManagerChart;
    @FXML private BarChart<String, Number> projectsByAdvisorChart;
    @FXML private AreaChart<String, Number> accountGrowthChart;

    private final DashboardService dashboardService = new DashboardService();

    @FXML
    public void initialize() {
        loadDashboard();
    }

    private void loadDashboard() {
        Task<AdminDashboardData> task = new Task<>() {
            @Override
            protected AdminDashboardData call() {
                return dashboardService.getAdminDashboardData();
            }
        };

        task.setOnSucceeded(event -> Platform.runLater(() -> render(task.getValue())));
        task.setOnFailed(event -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            AlertUtil.showError("Unable to load admin overview: " + (ex != null ? ex.getMessage() : ""));
        }));
        new Thread(task, "admin-dashboard-overview").start();
    }

    private void render(AdminDashboardData data) {
        metricContainer.getChildren().setAll(data.metrics().stream().map(this::createMetricCard).toList());
        accountRoleChart.setData(FXCollections.observableArrayList(
                data.accountsByRole().stream()
                        .map(point -> new PieChart.Data(point.label(), point.value()))
                        .toList()
        ));
        DashboardChartHelper.configureChart(accountRoleChart, "Accounts by role");
        DashboardChartHelper.installPieLabels(accountRoleChart);
        configureBarChart(classesByManagerChart, "Classes by manager", "Classes", "Manager", "Classes", compactLabels(data.classesByManager()));
        configureBarChart(projectsByAdvisorChart, "Projects by teacher", "Projects", "Teacher", "Projects", compactLabels(data.projectsByAdvisor()));
        configureAreaChart(accountGrowthChart, "Recent account growth", "New accounts", "Month", "Accounts", data.accountsCreatedByMonth());
    }

    private VBox createMetricCard(MetricCard metric) {
        Label titleLabel = new Label(metric.title());
        titleLabel.getStyleClass().add("overview-metric-title");

        Label valueLabel = new Label(metric.value());
        valueLabel.getStyleClass().add("overview-metric-value");

        Label captionLabel = new Label(metric.caption());
        captionLabel.getStyleClass().add("overview-metric-caption");
        captionLabel.setWrapText(true);

        VBox box = new VBox(6, titleLabel, valueLabel, captionLabel);
        box.getStyleClass().add("overview-metric-card");
        HBox.setHgrow(box, Priority.ALWAYS);
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    private java.util.List<ChartPoint> compactLabels(java.util.List<ChartPoint> points) {
        return points.stream()
                .map(point -> new ChartPoint(compactPersonName(point.label()), point.value()))
                .toList();
    }

    private String compactPersonName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "";
        }

        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0];
        }
        if (parts.length == 2) {
            return fullName;
        }
        return parts[parts.length - 2] + " " + parts[parts.length - 1];
    }

    private void configureBarChart(BarChart<String, Number> chart, String title, String seriesName, String xAxisLabel, String yAxisLabel, java.util.List<ChartPoint> points) {
        chart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(seriesName);
        points.forEach(point -> series.getData().add(new XYChart.Data<>(point.label(), point.value())));
        chart.getData().add(series);
        DashboardChartHelper.configureChart(chart, title);
        DashboardChartHelper.configureAxis(chart.getXAxis(), xAxisLabel);
        DashboardChartHelper.configureAxis(chart.getYAxis(), yAxisLabel);
        DashboardChartHelper.configureNumberAxis((NumberAxis) chart.getYAxis(), points.stream().map(ChartPoint::value).toList());
        DashboardChartHelper.installValueLabels(chart);
    }

    private void configureAreaChart(AreaChart<String, Number> chart, String title, String seriesName, String xAxisLabel, String yAxisLabel, java.util.List<ChartPoint> points) {
        chart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(seriesName);
        points.forEach(point -> series.getData().add(new XYChart.Data<>(point.label(), point.value())));
        chart.getData().add(series);
        DashboardChartHelper.configureChart(chart, title);
        DashboardChartHelper.configureAxis(chart.getXAxis(), xAxisLabel);
        DashboardChartHelper.configureAxis(chart.getYAxis(), yAxisLabel);
        DashboardChartHelper.configureNumberAxis((NumberAxis) chart.getYAxis(), points.stream().map(ChartPoint::value).toList());
        DashboardChartHelper.installValueLabels(chart);
    }
}
