package com.aptech.projectmgmt.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;

import java.util.Optional;

public class AlertUtil {

    public static void showError(String message) {
        Alert alert = buildAlert(Alert.AlertType.ERROR, "Loi", message);
        alert.showAndWait();
    }

    public static void showSuccess(String message) {
        Alert alert = buildAlert(Alert.AlertType.INFORMATION, "Thanh cong", message);
        alert.showAndWait();
    }

    public static boolean showConfirm(String message) {
        Alert alert = buildAlert(Alert.AlertType.CONFIRMATION, "Xac nhan", message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private static Alert buildAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);

        String finalMessage = (message == null || message.trim().isEmpty())
                ? "Thao tac thanh cong"
                : message;

        Label contentLabel = new Label(finalMessage);
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(420);
        contentLabel.setMinHeight(Region.USE_PREF_SIZE);
        contentLabel.setFont(Font.font(14));
        contentLabel.setStyle(
                "-fx-text-fill: #111111;" +
                "-fx-background-color: white;" +
                "-fx-padding: 12;" +
                "-fx-opacity: 1;"
        );

        alert.getDialogPane().setContent(contentLabel);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setPrefWidth(460);
        alert.getDialogPane().setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #d0d0d0;"
        );

        // bỏ stylesheet ngoài nếu có đè màu chữ
        alert.getDialogPane().getStylesheets().clear();

        return alert;
    }
}