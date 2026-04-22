package com.aptech.projectmgmt.util;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.Chart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

import java.util.List;

public final class DashboardChartHelper {
    private DashboardChartHelper() {
    }

    public static void configureChart(Chart chart, String title) {
        chart.setTitle(title);
        chart.setAnimated(false);
        chart.setLegendVisible(false);
    }

    public static void configureAxis(Axis<?> axis, String label) {
        if (axis != null) {
            axis.setLabel(label);
            axis.setAnimated(false);
            axis.setTickLabelsVisible(true);
            axis.setTickMarkVisible(true);
            axis.setTickLabelFill(Color.web("#334155"));
            axis.setStyle("-fx-font-size: 11px; -fx-font-weight: 700;");
            if (axis instanceof NumberAxis numberAxis) {
                numberAxis.setForceZeroInRange(true);
                numberAxis.setMinorTickVisible(false);
                numberAxis.setAutoRanging(true);
                numberAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(numberAxis, null, null) {
                    @Override
                    public String toString(Number object) {
                        return String.valueOf(object.intValue());
                    }
                });
            }
        }
    }

    public static void configureNumberAxis(NumberAxis axis, List<? extends Number> values) {
        if (axis == null) {
            return;
        }

        int max = values.stream().mapToInt(Number::intValue).max().orElse(0);
        axis.setAutoRanging(false);
        axis.setLowerBound(0);
        axis.setUpperBound(Math.max(1, max + 1));
        axis.setTickUnit(Math.max(1, Math.ceil((max + 1) / 5.0)));
    }

    public static <X, Y extends Number> void installValueLabels(XYChart<X, Y> chart) {
        for (XYChart.Series<X, Y> series : chart.getData()) {
            for (XYChart.Data<X, Y> data : series.getData()) {
                attachPointLabel(data);
            }
        }
    }

    public static void installPieLabels(PieChart chart) {
        for (PieChart.Data slice : chart.getData()) {
            String label = slice.getName() + " (" + (int) Math.round(slice.getPieValue()) + ")";
            slice.setName(label);
        }
    }

    private static <X, Y extends Number> void attachPointLabel(XYChart.Data<X, Y> data) {
        if (data.getNode() != null) {
            addLabelToNode(data);
            return;
        }
        data.nodeProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) {
                addLabelToNode(data);
            }
        });
    }

    private static <X, Y extends Number> void addLabelToNode(XYChart.Data<X, Y> data) {
        Platform.runLater(() -> {
            Node node = data.getNode();
            if (node == null || node.getParent() == null || !(node.getParent() instanceof javafx.scene.layout.Pane pane)) {
                return;
            }

            if (node.getProperties().containsKey("chart-label")) {
                return;
            }

            Label label = new Label(String.valueOf(data.getYValue().intValue()));
            label.getStyleClass().add("chart-value-label");
            label.setManaged(false);
            label.setMouseTransparent(true);
            pane.getChildren().add(label);
            node.getProperties().put("chart-label", label);

            Runnable relocate = () -> {
                double x = node.getBoundsInParent().getMinX() + (node.getBoundsInParent().getWidth() / 2.0) - (label.prefWidth(-1) / 2.0);
                double y = node.getBoundsInParent().getMinY() - 20;
                label.relocate(x, y);
            };

            node.boundsInParentProperty().addListener((obs, oldBounds, newBounds) -> relocate.run());
            label.widthProperty().addListener((obs, oldWidth, newWidth) -> relocate.run());
            relocate.run();
        });
    }
}
