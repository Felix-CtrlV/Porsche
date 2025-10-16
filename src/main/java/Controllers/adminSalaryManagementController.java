package Controllers;

import Utils.MonthEndSalaryService;
import Utils.SalaryScheduler;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class adminSalaryManagementController {
    private static final Logger logger = LoggerFactory.getLogger(adminSalaryManagementController.class);
    
    // Reference to parent dashboard controller
    private adminDashboardController dashboardController;
    
    @FXML
    private Label schedulerStatusLabel;
    
    @FXML
    private Button startSchedulerBtn;
    
    @FXML
    private Button stopSchedulerBtn;
    
    @FXML
    private Button manualProcessBtn;
    
    public void setDashboardController(adminDashboardController controller) {
        this.dashboardController = controller;
    }
    
    @FXML
    public void initialize() {
        updateSchedulerStatus();
        
        // Start scheduler automatically when view loads
        if (!SalaryScheduler.isRunning()) {
            SalaryScheduler.start();
            updateSchedulerStatus();
        }
    }
    
    @FXML
    private void onStartScheduler() {
        try {
            SalaryScheduler.start();
            updateSchedulerStatus();
            showToast("Scheduler Started", "The automatic salary scheduler has been started successfully.", "success");
        } catch (Exception e) {
            logger.error("Failed to start scheduler", e);
            showToast("Error", "Failed to start the scheduler: " + e.getMessage(), "error");
        }
    }
    
    @FXML
    private void onStopScheduler() {
        if (dashboardController != null) {
            dashboardController.showConfirmDialog(
                "Stop Automatic Scheduler",
                "Are you sure you want to stop the automatic salary scheduler? Salaries will not be processed automatically at month-end.",
                "⚠",
                () -> {
                    try {
                        SalaryScheduler.stop();
                        updateSchedulerStatus();
                        showToast("Scheduler Stopped", "The automatic salary scheduler has been stopped.", "info");
                    } catch (Exception e) {
                        logger.error("Failed to stop scheduler", e);
                        showToast("Error", "Failed to stop the scheduler: " + e.getMessage(), "error");
                    }
                }
            );
        }
    }
    
    @FXML
    private void onManualProcess() {
        if (dashboardController != null) {
            dashboardController.showConfirmDialog(
                "Process Salaries Manually",
                "This will:\n• Send email notifications to ALL employees\n• Reset ALL bonuses to $0\n\nAre you sure you want to continue?",
                "⚠",
                () -> {
                    manualProcessBtn.setDisable(true);
                    manualProcessBtn.setText("Processing...");
                    
                    // Run in background thread
                    new Thread(() -> {
                        try {
                            MonthEndSalaryService.processMonthEndSalary();
                            
                            Platform.runLater(() -> {
                                manualProcessBtn.setDisable(false);
                                manualProcessBtn.setText("Process Now");
                                showToast("Processing Complete", "Salary processing completed successfully. Check logs for details.", "success");
                            });
                            
                        } catch (Exception e) {
                            logger.error("Manual processing failed", e);
                            Platform.runLater(() -> {
                                manualProcessBtn.setDisable(false);
                                manualProcessBtn.setText("Process Now");
                                showToast("Processing Failed", "Failed to process salaries: " + e.getMessage(), "error");
                            });
                        }
                    }).start();
                }
            );
        }
    }
    
    private void updateSchedulerStatus() {
        if (SalaryScheduler.isRunning()) {
            schedulerStatusLabel.setText("✓ Running");
            schedulerStatusLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #10b981;");
            startSchedulerBtn.setDisable(true);
            stopSchedulerBtn.setDisable(false);
        } else {
            schedulerStatusLabel.setText("✕ Stopped");
            schedulerStatusLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #ef4444;");
            startSchedulerBtn.setDisable(false);
            stopSchedulerBtn.setDisable(true);
        }
    }
    
    private void showToast(String title, String message, String type) {
        if (dashboardController != null) {
            dashboardController.showToast(title, message, type);
        }
    }
}
