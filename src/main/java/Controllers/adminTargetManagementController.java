package Controllers;

import Database.DatabaseConnectionManager;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class adminTargetManagementController {
    private static final Logger logger = LoggerFactory.getLogger(adminTargetManagementController.class);
    
    @FXML private VBox setTargetPane;
    @FXML private VBox progressPane;
    
    // Set Target Controls
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
    
    private Map<String, Integer> managerMap = new HashMap<>();
    private int currentManagerId = 0;
    private int currentMonth = 0;
    private int currentYear = 0;
    
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
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load managers: " + e.getMessage());
        }
    }
    
    private void checkExistingTarget() {
        // Check if there's a target for current month
        LocalDate now = LocalDate.now();
        currentMonth = now.getMonthValue();
        currentYear = now.getYear();
        
        // For now, show set target pane by default
        // When manager is selected, we'll check if they have a target
        managerCombo.setOnAction(e -> {
            String selectedManager = managerCombo.getValue();
            if (selectedManager != null) {
                int managerId = managerMap.get(selectedManager);
                checkManagerTarget(managerId, currentMonth, currentYear);
            }
        });
    }
    
    private void checkManagerTarget(int managerId, int month, int year) {
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
            String query = """
                SELECT target 
                FROM user_target 
                WHERE user_id = ? 
                AND MONTH(effective_date) = ? 
                AND YEAR(effective_date) = ?
                ORDER BY effective_date DESC 
                LIMIT 1
            """;
            
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, managerId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                // Target exists, calculate achievements from actual sales
                String target = rs.getString("target");
                String achieve = calculateAchievements(managerId, month, year);
                
                currentManagerId = managerId;
                showProgressView(managerId, target, achieve, month, year);
            } else {
                // No target, stay on set target pane
                showSetTargetPane();
            }
            
        } catch (SQLException e) {
            logger.error("Failed to check manager target", e);
        }
    }
    
    private String calculateAchievements(int managerId, int month, int year) {
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
            // Count car sales for the manager in the given month/year
            String carQuery = """
                SELECT COALESCE(COUNT(*), 0) as car_count
                FROM orders o
                JOIN order_details od ON o.order_id = od.order_id
                WHERE o.user_id = ?
                AND MONTH(o.order_date) = ?
                AND YEAR(o.order_date) = ?
                AND od.car_id IS NOT NULL
            """;
            
            PreparedStatement carPs = conn.prepareStatement(carQuery);
            carPs.setInt(1, managerId);
            carPs.setInt(2, month);
            carPs.setInt(3, year);
            ResultSet carRs = carPs.executeQuery();
            
            int carCount = 0;
            if (carRs.next()) {
                carCount = carRs.getInt("car_count");
            }
            
            logger.info("Car count for manager {} in {}/{}: {}", managerId, month, year, carCount);
            
            // Count part sales for the manager in the given month/year
            String partQuery = """
                SELECT COALESCE(SUM(od.qty), 0) as part_count
                FROM orders o
                JOIN order_details od ON o.order_id = od.order_id
                WHERE o.user_id = ?
                AND MONTH(o.order_date) = ?
                AND YEAR(o.order_date) = ?
                AND od.part_id IS NOT NULL
            """;
            
            PreparedStatement partPs = conn.prepareStatement(partQuery);
            partPs.setInt(1, managerId);
            partPs.setInt(2, month);
            partPs.setInt(3, year);
            ResultSet partRs = partPs.executeQuery();
            
            int partCount = 0;
            if (partRs.next()) {
                partCount = partRs.getInt("part_count");
            }
            
            logger.info("Part count for manager {} in {}/{}: {}", managerId, month, year, partCount);
            
            return "cars-" + carCount + ",parts-" + partCount;
            
        } catch (SQLException e) {
            logger.error("Failed to calculate achievements", e);
            return "cars-0,parts-0";
        }
    }
    
    @FXML
    private void onSetTarget() {
        String selectedManager = managerCombo.getValue();
        String selectedMonth = monthCombo.getValue();
        Integer year = yearSpinner.getValue();
        Integer carTarget = carTargetSpinner.getValue();
        Integer partTarget = partTargetSpinner.getValue();
        
        if (selectedManager == null || selectedMonth == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please select a manager and month.");
            return;
        }
        
        int managerId = managerMap.get(selectedManager);
        int month = monthCombo.getItems().indexOf(selectedMonth) + 1;
        String targetString = "cars-" + carTarget + ",parts-" + partTarget;
        
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
            // Check if target already exists for this manager/month/year
            String checkQuery = """
                SELECT target_id 
                FROM user_target 
                WHERE user_id = ? 
                AND MONTH(effective_date) = ? 
                AND YEAR(effective_date) = ?
            """;
            
            PreparedStatement checkPs = conn.prepareStatement(checkQuery);
            checkPs.setInt(1, managerId);
            checkPs.setInt(2, month);
            checkPs.setInt(3, year);
            ResultSet checkRs = checkPs.executeQuery();
            
            if (checkRs.next()) {
                // Target exists, update it
                int targetId = checkRs.getInt("target_id");
                String updateQuery = """
                    UPDATE user_target 
                    SET target = ? 
                    WHERE target_id = ?
                """;
                
                PreparedStatement updatePs = conn.prepareStatement(updateQuery);
                updatePs.setString(1, targetString);
                updatePs.setInt(2, targetId);
                updatePs.executeUpdate();
                
                logger.info("Target updated for manager {} - {}", managerId, targetString);
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                         "Target updated successfully for " + selectedManager + "!\n\n" +
                         "Cars: " + carTarget + "\nParts: " + partTarget);
            } else {
                // Target doesn't exist, create new one
                String insertQuery = """
                    INSERT INTO user_target (user_id, effective_date, target) 
                    VALUES (?, ?, ?)
                """;
                
                PreparedStatement insertPs = conn.prepareStatement(insertQuery);
                insertPs.setInt(1, managerId);
                insertPs.setDate(2, java.sql.Date.valueOf(year + "-" + String.format("%02d", month) + "-01"));
                insertPs.setString(3, targetString);
                insertPs.executeUpdate();
                
                logger.info("Target created for manager {} - {}", managerId, targetString);
                showAlert(Alert.AlertType.INFORMATION, "Success", 
                         "Target set successfully for " + selectedManager + "!\n\n" +
                         "Cars: " + carTarget + "\nParts: " + partTarget);
            }
            
            // Switch to progress view
            currentManagerId = managerId;
            currentMonth = month;
            currentYear = year;
            checkManagerTarget(managerId, month, year);
            
        } catch (SQLException e) {
            logger.error("Failed to set target", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to set target: " + e.getMessage());
        }
    }
    
    private void showProgressView(int managerId, String target, String achieve, int month, int year) {
        // Parse target: "cars-10,parts-50"
        Map<String, Integer> targetMap = parseTargetString(target);
        Map<String, Integer> achieveMap = parseTargetString(achieve);
        
        int carTarget = targetMap.getOrDefault("cars", 0);
        int partTarget = targetMap.getOrDefault("parts", 0);
        int carAchieve = achieveMap.getOrDefault("cars", 0);
        int partAchieve = achieveMap.getOrDefault("parts", 0);
        
        // Calculate percentages (cap at 100% for circle animation)
        double carPercent = carTarget > 0 ? (carAchieve * 100.0 / carTarget) : 0;
        double partPercent = partTarget > 0 ? (partAchieve * 100.0 / partTarget) : 0;
        
        // Update labels
        String managerName = getManagerName(managerId);
        managerNameLabel.setText(managerName);
        
        String[] months = {"January", "February", "March", "April", "May", "June",
                          "July", "August", "September", "October", "November", "December"};
        periodLabel.setText(months[month - 1] + " " + year);
        
        // Update progress labels with excess indicator
        if (carPercent >= 100) {
            int excess = carAchieve - carTarget;
            carProgressLabel.setText("100%");
            if (excess > 0) {
                carAchieveLabel.setText(carAchieve + " / " + carTarget + " (+" + excess + ")");
            } else {
                carAchieveLabel.setText(carAchieve + " / " + carTarget);
            }
        } else {
            carProgressLabel.setText(String.format("%.0f%%", carPercent));
            carAchieveLabel.setText(carAchieve + " / " + carTarget);
        }
        
        if (partPercent >= 100) {
            int excess = partAchieve - partTarget;
            partProgressLabel.setText("100%");
            if (excess > 0) {
                partAchieveLabel.setText(partAchieve + " / " + partTarget + " (+" + excess + ")");
            } else {
                partAchieveLabel.setText(partAchieve + " / " + partTarget);
            }
        } else {
            partProgressLabel.setText(String.format("%.0f%%", partPercent));
            partAchieveLabel.setText(partAchieve + " / " + partTarget);
        }
        
        // Animate progress circles (cap at 100%)
        animateProgress(carProgressCircle, Math.min(carPercent, 100));
        animateProgress(partProgressCircle, Math.min(partPercent, 100));
        
        // Show progress pane
        setTargetPane.setVisible(false);
        progressPane.setVisible(true);
    }
    
    private void animateProgress(Circle circle, double percent) {
        double circumference = 2 * Math.PI * 65; // radius = 65
        double offset = circumference - (circumference * percent / 100);
        
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(circle.strokeDashOffsetProperty(), circumference)),
            new KeyFrame(Duration.seconds(1.2), new KeyValue(circle.strokeDashOffsetProperty(), offset))
        );
        timeline.play();
    }
    
    private Map<String, Integer> parseTargetString(String targetStr) {
        Map<String, Integer> map = new HashMap<>();
        if (targetStr == null || targetStr.isEmpty()) return map;
        
        String[] parts = targetStr.split(",");
        for (String part : parts) {
            String[] kv = part.split("-");
            if (kv.length == 2) {
                map.put(kv[0].trim(), Integer.parseInt(kv[1].trim()));
            }
        }
        return map;
    }
    
    private String getManagerName(int managerId) {
        for (Map.Entry<String, Integer> entry : managerMap.entrySet()) {
            if (entry.getValue() == managerId) {
                return entry.getKey();
            }
        }
        return "Manager";
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
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
