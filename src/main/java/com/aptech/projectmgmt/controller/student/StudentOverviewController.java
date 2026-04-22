package com.aptech.projectmgmt.controller.student;

import com.aptech.projectmgmt.model.Student;
import com.aptech.projectmgmt.model.dashboard.ChartPoint;
import com.aptech.projectmgmt.model.dashboard.MetricCard;
import com.aptech.projectmgmt.model.dashboard.StudentDashboardData;
import com.aptech.projectmgmt.service.DashboardService;
import com.aptech.projectmgmt.util.AlertUtil;
import com.aptech.projectmgmt.util.DashboardChartHelper;
import com.aptech.projectmgmt.util.SessionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class StudentOverviewController {
    @FXML private HBox metricContainer;
    @FXML private PieChart taskStatusChart;
    @FXML private LineChart<String, Number> scheduleChart;

    private final DashboardService dashboardService = new DashboardService();

    @FXML
    public void initialize() {
        Student currentStudent = SessionManager.getInstance().getCurrentStudent();
        if (currentStudent == null) {
            AlertUtil.showError("No student session found for the dashboard.");
            return;
        }
        loadDashboard(currentStudent.getStudentId());
    }

    private void loadDashboard(int studentId) {
        Task<StudentDashboardData> task = new Task<>() {
            @Override
            protected StudentDashboardData call() {
                return dashboardService.getStudentDashboardData(studentId);
            }
        };

        task.setOnSucceeded(event -> Platform.runLater(() -> render(task.getValue())));
        task.setOnFailed(event -> Platform.runLater(() -> {
            Throwable ex = task.getException();
            AlertUtil.showError("Unable to load student overview: " + (ex != null ? ex.getMessage() : ""));
        }));
        new Thread(task, "student-dashboard-overview").start();
    }

    private void render(StudentDashboardData data) {
        metricContainer.getChildren().setAll(data.metrics().stream().map(this::createMetricCard).toList());
        taskStatusChart.setData(FXCollections.observableArrayList(
                data.tasksByStatus().stream()
                        .map(point -> new PieChart.Data(point.label(), point.value()))
                        .toList()
        ));
        DashboardChartHelper.configureChart(taskStatusChart, "My task status");
        DashboardChartHelper.installPieLabels(taskStatusChart);
        configureLineChart(scheduleChart, "Upcoming task schedule", "Task schedule", "Month", "Tasks", data.tasksByMonth());
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
}
