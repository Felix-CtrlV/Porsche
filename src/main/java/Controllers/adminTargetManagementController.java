package Controllers;

import Database.DatabaseConnectionManager;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

public class adminTargetManagementController {
    private static final Logger logger = LoggerFactory.getLogger(adminTargetManagementController.class);
    
    // UI Panes
    @FXML private VBox setTargetPane;
    @FXML private VBox progressPane;
    
    // Set Target Form Controls
    @FXML private ComboBox<String> managerCombo;
    @FXML private ComboBox<String> monthCombo;
    @FXML private Spinner<Integer> yearSpinner;
    @FXML private Spinner<Integer> carTargetSpinner;
    @FXML private Spinner<Integer> partTargetSpinner;
    
    // Progress View Controls
    @FXML private Label managerNameLabel;
    @FXML private Label periodLabel;
    @FXML private Circle carProgressCircle;
    @FXML private Circle partProgressCircle;
    @FXML private Label carProgressLabel;
    @FXML private Label partProgressLabel;
    @FXML private Label carAchieveLabel;
    @FXML private Label partAchieveLabel;
    
    // Data Storage
    private final Map<String, Integer> managerMap = new HashMap<>();
    private int currentManagerId = 0;
    private int currentMonth = 0;
    private int currentYear = 0;
    
    // Parent controller reference
    private adminDashboardController dashboardController;
    
    public void setDashboardController(adminDashboardController controller) {
        this.dashboardController = controller;
    }
    
    @FXML
    public void initialize() {
        setupMonthCombo();
        setupSpinners();
        loadManagers();
        checkExistingTarget();
    }
    
    private void setupMonthCombo() {
        String[] months = {"January", "February", "March", "April", "May", "June",
                          "July", "August", "September", "October", "November", "December"};
        monthCombo.getItems().addAll(months);
        
        // Set current month as default
        LocalDate now = LocalDate.now();
        monthCombo.setValue(months[now.getMonthValue() - 1]);
    }
    
    private void setupSpinners() {
        // Year spinner
        int currentYear = LocalDate.now().getYear();
        SpinnerValueFactory<Integer> yearFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(
            currentYear, currentYear + 5, currentYear);
        yearSpinner.setValueFactory(yearFactory);
        
        // Car target spinner
        SpinnerValueFactory<Integer> carFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(
            1, 1000, 10);
        carTargetSpinner.setValueFactory(carFactory);
        
        // Part target spinner
        SpinnerValueFactory<Integer> partFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(
            1, 10000, 50);
        partTargetSpinner.setValueFactory(partFactory);
    }
    
    private void loadManagers() {
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
            String query = "SELECT user_id, user_name FROM user_info WHERE user_role = 'Manager' AND user_status = 1 ORDER BY user_name";
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            
            managerCombo.getItems().clear();
            managerMap.clear();
            
            while (rs.next()) {
                int userId = rs.getInt("user_id");
                String userName = rs.getString("user_name");
                managerCombo.getItems().add(userName);
                managerMap.put(userName, userId);
            }
            
        } catch (SQLException e) {
            logger.error("Failed to load managers", e);
            showToast("Error", "Failed to load managers: " + e.getMessage(), "error");
        }
    }
    
    private void checkExistingTarget() {
        // Set current month and year
        LocalDate now = LocalDate.now();
        currentMonth = now.getMonthValue();
        currentYear = now.getYear();
        
        // When manager is selected, check if they have a target for the selected period
        managerCombo.setOnAction(e -> onManagerSelected());
        monthCombo.setOnAction(e -> onManagerSelected());
        yearSpinner.valueProperty().addListener((obs, oldVal, newVal) -> onManagerSelected());
    }
    
    private void onManagerSelected() {
        String selectedManager = managerCombo.getValue();
        String selectedMonth = monthCombo.getValue();
        Integer selectedYear = yearSpinner.getValue();
        
        if (selectedManager != null && selectedMonth != null && selectedYear != null) {
            int managerId = managerMap.get(selectedManager);
            int month = monthCombo.getItems().indexOf(selectedMonth) + 1;
            
            // Check if target exists for this manager/period
            checkManagerTarget(managerId, month, selectedYear);
        }
    }
    
    private void checkManagerTarget(int managerId, int month, int year) {
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
            // Call targetViewChart stored procedure
            String callProc = "{CALL targetViewChart(?, ?, ?)}";
            CallableStatement cs = conn.prepareCall(callProc);
            cs.setInt(1, managerId);
            cs.setInt(2, month);
            cs.setInt(3, year);
            
            ResultSet rs = cs.executeQuery();
            
            if (rs.next()) {
                int targetCar = rs.getInt("target_car");
                int targetPart = rs.getInt("target_part");
                int achieveCar = rs.getInt("achieve_car");
                int achievePart = rs.getInt("achieve_part");
                double carPercentage = rs.getDouble("car_achievement_percentage");
                double partPercentage = rs.getDouble("part_achievement_percentage");
                String userName = rs.getString("user_name");
                String status = rs.getString("achievement_status");
                
                // Check if target exists (target values > 0)
                if (targetCar > 0 || targetPart > 0) {
                    currentManagerId = managerId;
                    showProgressView(managerId, userName, targetCar, targetPart, 
                                   achieveCar, achievePart, carPercentage, partPercentage, 
                                   month, year, status);
                } else {
                    // No target set, stay on set target pane
                    showSetTargetPane();
                }
            } else {
                // No data found, stay on set target pane
                showSetTargetPane();
            }
            
        } catch (SQLException e) {
            logger.error("Failed to check manager target", e);
            showToast("Error", "Failed to load target data: " + e.getMessage(), "error");
        }
    }
    
    
    @FXML
    private void onSetTarget() {
        String selectedManager = managerCombo.getValue();
        String selectedMonth = monthCombo.getValue();
        Integer year = yearSpinner.getValue();
        Integer carTarget = carTargetSpinner.getValue();
        Integer partTarget = partTargetSpinner.getValue();
        
        // Validation
        if (selectedManager == null || selectedMonth == null) {
            showToast("Validation Error", "Please select a manager and month.", "error");
            return;
        }
        
        if (carTarget == null || carTarget <= 0) {
            showToast("Validation Error", "Car target must be greater than 0.", "error");
            return;
        }
        
        if (partTarget == null || partTarget <= 0) {
            showToast("Validation Error", "Part target must be greater than 0.", "error");
            return;
        }
        
        // Show confirmation dialog with details
        showTargetConfirmation(selectedManager, selectedMonth, year, carTarget, partTarget);
    }
    
    private void showTargetConfirmation(String manager, String month, int year, int carTarget, int partTarget) {
        // Create details node
        VBox detailsBox = new VBox(12);
        detailsBox.setStyle("-fx-alignment: center;");
        
        // Info card
        VBox infoCard = new VBox(10);
        infoCard.setStyle("-fx-background-color: #f8fafc; -fx-padding: 15; -fx-background-radius: 8;");
        
        // Manager
        HBox managerRow = new HBox(10);
        managerRow.setStyle("-fx-alignment: center-left;");
        Label managerIcon = new Label("👤");
        managerIcon.setStyle("-fx-font-size: 16;");
        Label managerLabel = new Label("Manager:");
        managerLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #64748b; -fx-font-weight: 600;");
        Label managerValue = new Label(manager);
        managerValue.setStyle("-fx-font-size: 13; -fx-text-fill: #1e293b; -fx-font-weight: bold;");
        managerRow.getChildren().addAll(managerIcon, managerLabel, managerValue);
        
        // Period
        HBox periodRow = new HBox(10);
        periodRow.setStyle("-fx-alignment: center-left;");
        Label periodIcon = new Label("📅");
        periodIcon.setStyle("-fx-font-size: 16;");
        Label periodLabel = new Label("Period:");
        periodLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #64748b; -fx-font-weight: 600;");
        Label periodValue = new Label(month + " " + year);
        periodValue.setStyle("-fx-font-size: 13; -fx-text-fill: #1e293b; -fx-font-weight: bold;");
        periodRow.getChildren().addAll(periodIcon, periodLabel, periodValue);
        
        infoCard.getChildren().addAll(managerRow, periodRow);
        
        // Targets card
        HBox targetsCard = new HBox(10);
        targetsCard.setStyle("-fx-alignment: center;");
        
        // Car target
        VBox carBox = new VBox(8);
        carBox.setStyle("-fx-background-color: #eff6ff; -fx-padding: 12; -fx-background-radius: 8; -fx-alignment: center;");
        Label carIcon = new Label("🚗");
        carIcon.setStyle("-fx-font-size: 20;");
        Label carLabel = new Label("Car Target");
        carLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #3b82f6; -fx-font-weight: 600;");
        Label carValue = new Label(String.valueOf(carTarget));
        carValue.setStyle("-fx-font-size: 18; -fx-text-fill: #1e293b; -fx-font-weight: bold;");
        carBox.getChildren().addAll(carIcon, carLabel, carValue);
        
        // Part target
        VBox partBox = new VBox(8);
        partBox.setStyle("-fx-background-color: #f0fdf4; -fx-padding: 12; -fx-background-radius: 8; -fx-alignment: center;");
        Label partIcon = new Label("🔧");
        partIcon.setStyle("-fx-font-size: 20;");
        Label partLabel = new Label("Part Target");
        partLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #10b981; -fx-font-weight: 600;");
        Label partValue = new Label(String.valueOf(partTarget));
        partValue.setStyle("-fx-font-size: 18; -fx-text-fill: #1e293b; -fx-font-weight: bold;");
        partBox.getChildren().addAll(partIcon, partLabel, partValue);
        
        targetsCard.getChildren().addAll(carBox, partBox);
        
        detailsBox.getChildren().addAll(infoCard, targetsCard);
        
        // Show confirmation dialog
        if (dashboardController != null) {
            dashboardController.showConfirmDialogWithDetails(
                "Confirm Target Setting",
                "🎯",
                detailsBox,
                () -> executeSetTarget(manager, month, year, carTarget, partTarget)
            );
        }
    }
    
    private void executeSetTarget(String selectedManager, String selectedMonth, int year, int carTarget, int partTarget) {
        int managerId = managerMap.get(selectedManager);
        int month = monthCombo.getItems().indexOf(selectedMonth) + 1;
        
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
            // Call the setTarget stored procedure
            String callProc = "{CALL setTarget(?, ?, ?, ?, ?)}";
            CallableStatement cs = conn.prepareCall(callProc);
            cs.setInt(1, managerId);
            cs.setInt(2, month);
            cs.setInt(3, year);
            cs.setInt(4, carTarget);
            cs.setInt(5, partTarget);
            
            // Execute the procedure
            boolean hasResults = cs.execute();
            
            // Get the result message from the procedure
            String resultMessage = null;
            if (hasResults) {
                ResultSet rs = cs.getResultSet();
                if (rs.next()) {
                    resultMessage = rs.getString("result");
                    logger.info("Procedure result: {}", resultMessage);
                }
            }
            
            // Show success message and auto-close confirmation
            showToast("Success", "Target set successfully!", "success");
            logger.info("Target set for manager {} ({}/{}): cars-{}, parts-{}", 
                       managerId, month, year, carTarget, partTarget);
            
            // Switch to progress view
            currentManagerId = managerId;
            currentMonth = month;
            currentYear = year;
            checkManagerTarget(managerId, month, year);
            
        } catch (SQLException e) {
            logger.error("Failed to set target", e);
            showToast("Error", "Failed to set target: " + e.getMessage(), "error");
        }
    }
    
    private void showProgressView(int managerId, String userName, int carTarget, int partTarget,
                                   int carAchieve, int partAchieve, double carPercent, double partPercent,
                                   int month, int year, String status) {
        
        // Update header labels
        managerNameLabel.setText(userName);
        
        String[] months = {"January", "February", "March", "April", "May", "June",
                          "July", "August", "September", "October", "November", "December"};
        periodLabel.setText(months[month - 1] + " " + year);
        
        // Update car progress
        if (carTarget > 0) {
            int excess = carAchieve - carTarget;
            carProgressLabel.setText(carPercent >= 100 ? "100%" : String.format("%.1f%%", carPercent));
            carAchieveLabel.setText(Math.min(carAchieve, carTarget) + " / " + carTarget + (excess > 0 ? " (+" + excess + ")" : ""));
        } else {
            carProgressLabel.setText("0%");
            carAchieveLabel.setText("0 / 0");
        }
        
        // Update part progress
        if (partTarget > 0) {
            int excess = partAchieve - partTarget;
            partProgressLabel.setText(partPercent >= 100 ? "100%" : String.format("%.1f%%", partPercent));
            partAchieveLabel.setText(Math.min(partAchieve, partTarget) + " / " + partTarget + (excess > 0 ? " (+" + excess + ")" : ""));
        } else {
            partProgressLabel.setText("0%");
            partAchieveLabel.setText("0 / 0");
        }
        
        // Animate progress circles (cap at 100% for visual)
        animateProgress(carProgressCircle, Math.min(carPercent, 100));
        animateProgress(partProgressCircle, Math.min(partPercent, 100));
        
        // Log status
        logger.info("Target status for {}: {}", userName, status);
        
        // Show progress pane
        setTargetPane.setVisible(false);
        progressPane.setVisible(true);
    }
    
    private void animateProgress(Circle circle, double percent) {
        double circumference = 2 * Math.PI * 60; // radius = 60
        double offset = circumference - (circumference * percent / 100);
        
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(circle.strokeDashOffsetProperty(), circumference)),
            new KeyFrame(Duration.seconds(1.5), new KeyValue(circle.strokeDashOffsetProperty(), offset, Interpolator.EASE_OUT))
        );
        timeline.play();
    }
    
    private void showSetTargetPane() {
        progressPane.setVisible(false);
        setTargetPane.setVisible(true);
    }
    
    @FXML
    private void onSetNewTarget() {
        showSetTargetPane();
    }
    
    @FXML
    private void onRefresh() {
        if (currentManagerId > 0) {
            checkManagerTarget(currentManagerId, currentMonth, currentYear);
        }
    }

    @FXML
    private void onBack() {
        // Switch back to set target pane from progress view
        progressPane.setVisible(false);
        setTargetPane.setVisible(true);
    }

    @FXML
    private void onCancel() {
        // Switch back to set target pane
        progressPane.setVisible(false);
        setTargetPane.setVisible(true);
        
        // Reset form
        managerCombo.getSelectionModel().clearSelection();
        managerCombo.setValue(null);
        monthCombo.getSelectionModel().clearSelection();
        carTargetSpinner.getValueFactory().setValue(10);
        partTargetSpinner.getValueFactory().setValue(50);
    }
    
    private void showToast(String title, String message, String type) {
        if (dashboardController != null) {
            dashboardController.showToast(title, message, type);
        }
    }
}
