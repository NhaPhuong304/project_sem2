package com.aptech.projectmgmt.controller.staff;

import com.aptech.projectmgmt.model.Staff;
import com.aptech.projectmgmt.model.dashboard.ChartPoint;
import com.aptech.projectmgmt.model.dashboard.MetricCard;
import com.aptech.projectmgmt.model.dashboard.StaffDashboardData;
import com.aptech.projectmgmt.service.DashboardService;
import com.aptech.projectmgmt.util.AlertUtil;
import com.aptech.projectmgmt.util.DashboardChartHelper;
import com.aptech.projectmgmt.util.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class StaffOverviewController {
    @FXML private HBox metricContainer;
    @FXML private BarChart<String, Number> studentsByClassChart;
    @FXML private PieChart projectStatusChart;
    @FXML private LineChart<String, Number> completedTasksChart;
    @FXML private AreaChart<String, Number> overdueTasksChart;

    private final DashboardService dashboardService = new DashboardService();

    @FXML
    public void initialize() {
        Staff currentStaff = SessionManager.getInstance().getCurrentStaff();
        if (currentStaff == null) {
            AlertUtil.showError("No staff session found for the dashboard.");
            return;
        }
        loadDashboard(currentStaff.getStaffId());
    }

    private void loadDashboard(int staffId) {
        Task<StaffDashboardData> task = new Task<>() {
            @Override
            protected StaffDashboardData call() {
                return dashboardService.getStaffDashboardData(staffId);
            }
        };

        task.setOnSucceeded(event -> Platform.runLater(() -> render(task.getValue())));
        task.setOnFailed(event -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            AlertUtil.showError("Unable to load staff overview: " + (ex != null ? ex.getMessage() : ""));
        }));
        new Thread(task, "staff-dashboard-overview").start();
    }

    private void render(StaffDashboardData data) {
        metricContainer.getChildren().setAll(data.metrics().stream().map(this::createMetricCard).toList());
        configureBarChart(studentsByClassChart, "Students by class", "Students", "Class", "Students", data.studentsByClass());
        projectStatusChart.setData(FXCollections.observableArrayList(
                data.projectsByStatus().stream()
                        .map(point -> new PieChart.Data(point.label(), point.value()))
                        .toList()
        ));
        DashboardChartHelper.configureChart(projectStatusChart, "Projects by status");
        DashboardChartHelper.installPieLabels(projectStatusChart);
        configureLineChart(completedTasksChart, "Completed tasks trend", "Completed tasks", "Month", "Tasks", data.completedTasksByMonth());
        configureAreaChart(overdueTasksChart, "Overdue task pressure", "Overdue tasks", "Month", "Tasks", data.overdueTasksByMonth());
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

    private void configureLineChart(LineChart<String, Number> chart, String title, String seriesName, String xAxisLabel, String yAxisLabel, java.util.List<ChartPoint> points) {
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
