package com.aptech.projectmgmt;

import com.aptech.projectmgmt.config.DatabaseConfig;
import com.aptech.projectmgmt.controller.SplashController;
import com.aptech.projectmgmt.service.TaskService;
import com.aptech.projectmgmt.util.SceneManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main extends Application {

    private ScheduledExecutorService scheduler;

    @Override
    public void start(Stage primaryStage) throws Exception {
        startScheduler();

        FXMLLoader loader = new FXMLLoader(getClass().getResource(SceneManager.SPLASH));
        Parent root = loader.load();
        SplashController controller = loader.getController();
        Scene scene = new Scene(root);
        String css = getClass().getResource("/css/style.css") != null
                ? getClass().getResource("/css/style.css").toExternalForm() : null;
        if (css != null) scene.getStylesheets().add(css);
        primaryStage.setTitle("Project Management System - Aptech");
        primaryStage.setResizable(true);
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.show();
        primaryStage.centerOnScreen();
        controller.initData(primaryStage);
    }

    private void startScheduler() {
        TaskService taskService = new TaskService();
        scheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, "task-background-scheduler");
            t.setDaemon(true);
            return t;
        });

        // Tự động thu hồi task quá hạn: quét mỗi 5 phút
        scheduler.scheduleAtFixedRate(() -> {
            try {
                taskService.resetOverdueTasks();
            } catch (Exception e) {
                System.err.println("[Scheduler] Reset Error: " + e.getMessage());
            }
        }, 0, 5, TimeUnit.MINUTES);

        // Gửi email nhắc nhở: quét 1 giờ 1 lần
        scheduler.scheduleAtFixedRate(() -> {
            try {
                taskService.processEmailReminders();
                System.out.println("[Scheduler] Sent hourly email reminders.");
            } catch (Exception e) {
                System.err.println("[Scheduler] Email Error: " + e.getMessage());
            }
        }, 0, 1, TimeUnit.HOURS);
    }

    @Override
    public void stop() throws Exception {
        if (scheduler != null) {
            scheduler.shutdown();
        }
        DatabaseConfig.close();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
