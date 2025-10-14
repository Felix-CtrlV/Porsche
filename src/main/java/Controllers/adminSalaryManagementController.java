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
    
    @FXML
    private Label schedulerStatusLabel;
    
    @FXML
    private Button startSchedulerBtn;
    
    @FXML
    private Button stopSchedulerBtn;
    
    @FXML
    private Button manualProcessBtn;
    
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
            showAlert(Alert.AlertType.INFORMATION, "Scheduler Started", 
                     "The automatic salary scheduler has been started successfully.");
        } catch (Exception e) {
            logger.error("Failed to start scheduler", e);
            showAlert(Alert.AlertType.ERROR, "Error", 
                     "Failed to start the scheduler: " + e.getMessage());
        }
    }
    
    @FXML
    private void onStopScheduler() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Stop");
        confirm.setHeaderText("Stop Automatic Scheduler");
        confirm.setContentText("Are you sure you want to stop the automatic salary scheduler? " +
                              "Salaries will not be processed automatically at month-end.");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    SalaryScheduler.stop();
                    updateSchedulerStatus();
                    showAlert(Alert.AlertType.INFORMATION, "Scheduler Stopped", 
                             "The automatic salary scheduler has been stopped.");
                } catch (Exception e) {
                    logger.error("Failed to stop scheduler", e);
                    showAlert(Alert.AlertType.ERROR, "Error", 
                             "Failed to stop the scheduler: " + e.getMessage());
                }
            }
        });
    }
    
    @FXML
    private void onManualProcess() {
        Alert confirm = new Alert(Alert.AlertType.WARNING);
        confirm.setTitle("Confirm Manual Processing");
        confirm.setHeaderText("Process Salaries Manually");
        confirm.setContentText("This will:\n" +
                              "• Send email notifications to ALL employees\n" +
                              "• Reset ALL bonuses to $0\n\n" +
                              "Are you sure you want to continue?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                manualProcessBtn.setDisable(true);
                manualProcessBtn.setText("Processing...");
                
                // Run in background thread
                new Thread(() -> {
                    try {
                        MonthEndSalaryService.processMonthEndSalary();
                        
                        Platform.runLater(() -> {
                            manualProcessBtn.setDisable(false);
                            manualProcessBtn.setText("Process Now");
                            showAlert(Alert.AlertType.INFORMATION, "Processing Complete", 
                                     "Salary processing completed successfully. Check logs for details.");
                        });
                        
                    } catch (Exception e) {
                        logger.error("Manual processing failed", e);
                        Platform.runLater(() -> {
                            manualProcessBtn.setDisable(false);
                            manualProcessBtn.setText("Process Now");
                            showAlert(Alert.AlertType.ERROR, "Processing Failed", 
                                     "Failed to process salaries: " + e.getMessage());
                        });
                    }
                }).start();
            }
        });
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
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
