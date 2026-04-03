package com.aptech.projectmgmt.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

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
        Label contentLabel = new Label(message != null ? message : "");
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(420);
        alert.getDialogPane().setContent(contentLabel);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setPrefWidth(460);
        return alert;
    }
}
