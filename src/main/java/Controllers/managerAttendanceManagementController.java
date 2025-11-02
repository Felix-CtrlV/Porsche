package Controllers;

import Database.DatabaseConnectionManager;
import Utils.Session;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.List;
import java.util.ArrayList;

public class managerAttendanceManagementController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(managerAttendanceManagementController.class);

    // Dashboard controller reference for notifications
    private managerDashboardController dashboardController;

    public void setDashboardController(managerDashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    @FXML private ComboBox<String> staffCombo;
    @FXML private Button dateRangeBtn;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Button addAbsentBtn;

    @FXML private TableView<AttendanceRecord> attendanceTable;
    @FXML private TableColumn<AttendanceRecord, String> staffNameCol;
    @FXML private TableColumn<AttendanceRecord, String> dateCol;
    @FXML private TableColumn<AttendanceRecord, String> checkInCol;
    @FXML private TableColumn<AttendanceRecord, String> checkOutCol;
    @FXML private TableColumn<AttendanceRecord, String> hoursWorkedCol;
    @FXML private TableColumn<AttendanceRecord, String> remarksCol;

    @FXML private Label recordCountLabel;
    @FXML private Label presentTodayLabel;
    @FXML private Label absentTodayLabel;
    @FXML private Label lateTodayLabel;
    @FXML private Label totalStaffLabel;

    // Date Range Picker elements
    @FXML private Pane dateRangeBackdrop;
    @FXML private VBox dateRangePanel;
    @FXML private VBox dateRangePickerPanel;
    @FXML private VBox calendarGrid;
    @FXML private Label monthYearLabel;
    @FXML private Button prevMonthBtn;
    @FXML private Button nextMonthBtn;
    @FXML private Label selectedDateLabel;
    @FXML private Button applyDateRangeBtn;
    @FXML private Button cancelDateRangeBtn;
    @FXML private Label closeDateRangeBtn;

    // Store selected date range and calendar state
    private LocalDate selectedStartDate;
    private LocalDate selectedEndDate;
    private YearMonth currentMonth;
    private LocalDate tempStartDate;
    private LocalDate tempEndDate;


    private ObservableList<AttendanceRecord> attendanceData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupComboBoxes();
        loadInitialData();
        setupRealTimeListeners();
        updateSummaryStats();
    }

    private void setupTableColumns() {
        staffNameCol.setCellValueFactory(new PropertyValueFactory<>("staffName"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        checkInCol.setCellValueFactory(new PropertyValueFactory<>("checkIn"));
        checkOutCol.setCellValueFactory(new PropertyValueFactory<>("checkOut"));
        hoursWorkedCol.setCellValueFactory(new PropertyValueFactory<>("hoursWorked"));
        remarksCol.setCellValueFactory(new PropertyValueFactory<>("remarks"));

        attendanceTable.setItems(attendanceData);
        
        // Apply styling to make headers visible and professional
        attendanceTable.setStyle(attendanceTable.getStyle() + 
            "; -fx-table-header-border-color: #dee2e6" +
            "; -fx-table-header-border-width: 0 0 2 0" +
            "; -fx-control-inner-background: white" +
            "; -fx-table-column-border-insets: 0" +
            "; -fx-table-header-border-insets: 0");
            
        // Set header styling for each column
        staffNameCol.setStyle("-fx-alignment: CENTER-LEFT; -fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #495057;");
        dateCol.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #495057;");
        checkInCol.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #495057;");
        checkOutCol.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #495057;");
        hoursWorkedCol.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #495057;");
        remarksCol.setStyle("-fx-alignment: CENTER-LEFT; -fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #495057;");
    }

    private void setupComboBoxes() {
        // Setup status combo
        statusCombo.setItems(FXCollections.observableArrayList(
                "All", "Present", "On Time", "Late", "Early Leave", "Absent"
        ));
        statusCombo.setValue("All");

        // Load staff members
        loadStaffMembers();
    }

    private void loadStaffMembers() {
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
            Session session = Session.getInstance();
            int currentUserId = session.getUserid();
            String userRole = session.getRole();

            String query;
            PreparedStatement stmt;

            if ("admin".equals(userRole)) {
                // Admin sees all staff (not managers)
                query = "SELECT DISTINCT user_name FROM user_info WHERE user_role = 'staff' AND user_status = TRUE ORDER BY user_name";
                stmt = conn.prepareStatement(query);
            } else if ("manager".equals(userRole)) {
                // Manager sees only their direct staff (not themselves)
                query = "SELECT DISTINCT u.user_name FROM user_info u " +
                        "JOIN user_workinfo uw ON u.user_id = uw.user_id " +
                        "WHERE u.user_status = TRUE AND uw.manager = ? AND u.user_role = 'staff' " +
                        "ORDER BY u.user_name";
                stmt = conn.prepareStatement(query);
                stmt.setInt(1, currentUserId);
            } else {
                // Staff sees only themselves
                query = "SELECT user_name FROM user_info WHERE user_id = ?";
                stmt = conn.prepareStatement(query);
                stmt.setInt(1, currentUserId);
            }

            ResultSet rs = stmt.executeQuery();

            ObservableList<String> staffList = FXCollections.observableArrayList();
            staffList.add("All Staff");

            while (rs.next()) {
                staffList.add(rs.getString("user_name"));
            }

            staffCombo.setItems(staffList);
            staffCombo.setValue("All Staff");

        } catch (SQLException e) {
            logger.error("Error loading staff members", e);
        }
    }

    private void loadInitialData() {
        // Initialize date range to current day
        LocalDate today = LocalDate.now();
        selectedStartDate = today; // Current day
        selectedEndDate = today;   // Current day (single day selection)
        currentMonth = YearMonth.now();
        
        // Initialize temp dates for calendar
        tempStartDate = selectedStartDate;
        tempEndDate = selectedEndDate;
        
        updateDateRangeButton();
        loadAttendanceData();
        updateSummaryStats();
    }

    private void setupRealTimeListeners() {
        // Add listeners for real-time updates
        staffCombo.setOnAction(e -> loadAttendanceData());
        statusCombo.setOnAction(e -> loadAttendanceData());
    }

    @FXML
    private void onAddAbsent() {
        showAddAbsentPanel();
    }

    @FXML
    private void showDateRangePicker() {
        // Initialize calendar with current selection
        tempStartDate = selectedStartDate;
        tempEndDate = selectedEndDate;
        buildCalendar();
        updateSelectedDateDisplay();
        
        // Show backdrop and dialog
        dateRangeBackdrop.setVisible(true);
        dateRangePanel.setVisible(true);
        
        // Auto-scroll to make date picker fully visible (with small delay for rendering)
        javafx.application.Platform.runLater(() -> {
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(100));
            pause.setOnFinished(e -> scrollToDatePicker());
            pause.play();
        });
    }
    
    private void scrollToDatePicker() {
        // Find ScrollPane within the modal dialog and scroll to center
        javafx.application.Platform.runLater(() -> {
            try {
                // First, look for ScrollPane within the current scene (modal dialog)
                javafx.scene.Scene scene = dateRangeBtn.getScene();
                if (scene != null) {
                    javafx.scene.control.ScrollPane scrollPane = findScrollPane(scene.getRoot());
                    if (scrollPane != null) {
                        scrollPane.setVvalue(0.5);
                        logger.debug("Successfully scrolled modal dialog ScrollPane");
                        return;
                    }
                }
                
                // If no ScrollPane in modal, try to find the parent window's ScrollPane
                javafx.stage.Window currentWindow = dateRangeBtn.getScene().getWindow();
                if (currentWindow instanceof javafx.stage.Stage) {
                    javafx.stage.Stage currentStage = (javafx.stage.Stage) currentWindow;
                    javafx.stage.Stage ownerStage = (javafx.stage.Stage) currentStage.getOwner();
                    
                    if (ownerStage != null && ownerStage.getScene() != null) {
                        javafx.scene.control.ScrollPane parentScrollPane = findScrollPane(ownerStage.getScene().getRoot());
                        if (parentScrollPane != null) {
                            parentScrollPane.setVvalue(0.5);
                            logger.debug("Successfully scrolled parent window ScrollPane");
                            return;
                        }
                    }
                }
                
                logger.debug("No ScrollPane found - modal dialog may not need scrolling");
                
            } catch (Exception e) {
                logger.debug("Could not auto-scroll in modal dialog", e);
            }
        });
    }
    
    private void findAndScrollAlternative() {
        try {
            // Alternative approach: traverse up the scene graph to find ScrollPane
            javafx.scene.Node current = dateRangeBtn;
            while (current != null) {
                if (current instanceof javafx.scene.control.ScrollPane) {
                    javafx.scene.control.ScrollPane scrollPane = (javafx.scene.control.ScrollPane) current;
                    scrollPane.setVvalue(0.5);
                    logger.debug("Found ScrollPane via traversal");
                    return;
                }
                current = current.getParent();
            }
            
            // Last resort: find all ScrollPanes in the scene
            javafx.scene.Scene scene = dateRangeBtn.getScene();
            if (scene != null) {
                findAllScrollPanes(scene.getRoot()).forEach(sp -> {
                    sp.setVvalue(0.5);
                    logger.debug("Applied scroll to found ScrollPane");
                });
            }
        } catch (Exception e) {
            logger.debug("Alternative scroll approach failed", e);
        }
    }
    
    private java.util.List<javafx.scene.control.ScrollPane> findAllScrollPanes(javafx.scene.Node node) {
        java.util.List<javafx.scene.control.ScrollPane> scrollPanes = new java.util.ArrayList<>();
        
        if (node instanceof javafx.scene.control.ScrollPane) {
            scrollPanes.add((javafx.scene.control.ScrollPane) node);
        }
        
        if (node instanceof javafx.scene.Parent) {
            javafx.scene.Parent parent = (javafx.scene.Parent) node;
            for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
                scrollPanes.addAll(findAllScrollPanes(child));
            }
        }
        
        return scrollPanes;
    }
    
    private javafx.scene.control.ScrollPane findScrollPane(javafx.scene.Node node) {
        if (node instanceof javafx.scene.control.ScrollPane) {
            return (javafx.scene.control.ScrollPane) node;
        }
        
        if (node instanceof javafx.scene.Parent) {
            javafx.scene.Parent parent = (javafx.scene.Parent) node;
            for (javafx.scene.Node child : parent.getChildrenUnmodifiable()) {
                javafx.scene.control.ScrollPane result = findScrollPane(child);
                if (result != null) {
                    return result;
                }
            }
        }
        
        return null;
    }

    @FXML
    private void closeDateRangePicker() {
        dateRangeBackdrop.setVisible(false);
        dateRangePanel.setVisible(false);
    }

    @FXML
    private void applyDateRange() {
        if (tempStartDate != null) {
            selectedStartDate = tempStartDate;
            selectedEndDate = tempEndDate != null ? tempEndDate : tempStartDate;
            updateDateRangeButton();
            loadAttendanceData();
        }
        closeDateRangePicker();
    }

    @FXML
    private void previousMonth() {
        currentMonth = currentMonth.minusMonths(1);
        buildCalendar();
    }

    @FXML
    private void nextMonth() {
        currentMonth = currentMonth.plusMonths(1);
        buildCalendar();
    }

    private void updateDateRangeButton() {
        if (selectedStartDate != null && selectedEndDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
            if (selectedStartDate.equals(selectedEndDate)) {
                dateRangeBtn.setText(selectedStartDate.format(formatter));
            } else {
                dateRangeBtn.setText(selectedStartDate.format(formatter) + " - " + selectedEndDate.format(formatter));
            }
        } else {
            dateRangeBtn.setText("Select date range...");
        }
    }

    private void buildCalendar() {
        calendarGrid.getChildren().clear();

        // Update month/year label
        String monthYear = currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + currentMonth.getYear();
        monthYearLabel.setText(monthYear);

        YearMonth yearMonth = YearMonth.of(currentMonth.getYear(), currentMonth.getMonth());
        LocalDate firstOfMonth = yearMonth.atDay(1);
        int daysInMonth = yearMonth.lengthOfMonth();
        int startDayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7; // Sunday = 0

        // Create calendar rows
        int day = 1;
        for (int week = 0; week < 6; week++) {
            HBox weekRow = new HBox();
            weekRow.setSpacing(0);
            weekRow.setAlignment(Pos.CENTER);

            for (int dayOfWeek = 0; dayOfWeek < 7; dayOfWeek++) {
                Label dayLabel = new Label();
                dayLabel.setPrefWidth(35);
                dayLabel.setPrefHeight(35);
                dayLabel.setAlignment(Pos.CENTER);
                dayLabel.getStyleClass().add("dayLabel");

                if (week == 0 && dayOfWeek < startDayOfWeek) {
                    // Empty cell for days before month starts
                    dayLabel.setText("");
                    dayLabel.setDisable(true); // Or add "disabledStyle" class if you define it
                } else if (day <= daysInMonth) {
                    dayLabel.setText(String.valueOf(day));
                    LocalDate cellDate = currentMonth.atDay(day);

                    // Style the day based on selection state (now just sets classes)
                    styleDayCell(dayLabel, cellDate);

                    // Add click handler
                    dayLabel.setOnMouseClicked(e -> handleDateClick(cellDate));

                    day++;
                } else {
                    // Empty cell for days after month ends
                    dayLabel.setText("");
                    dayLabel.setDisable(true); // Or add "disabledStyle" class
                }

                weekRow.getChildren().add(dayLabel);
            }

            calendarGrid.getChildren().add(weekRow);

            if (day > daysInMonth) break;
        }
    }

    private void styleDayCell(Label dayLabel, LocalDate date) {
        // Clear existing style classes (except base "dayLabel")
        dayLabel.getStyleClass().removeAll("normalStyle", "selectedStyle", "betweenStyle", "singleDateStyle");

        // No inline styles needed—CSS handles everything now
        if (tempStartDate != null && tempEndDate != null) {
            // Range selection
            if (date.equals(tempStartDate) || date.equals(tempEndDate)) {
                // Start or end date
                dayLabel.getStyleClass().add("selectedStyle");
            } else if (date.isAfter(tempStartDate) && date.isBefore(tempEndDate)) {
                // In between dates
                dayLabel.getStyleClass().add("betweenStyle");
            } else {
                // Normal day
                dayLabel.getStyleClass().add("normalStyle");
            }
        } else if (tempStartDate != null && date.equals(tempStartDate)) {
            // Single date selected
            dayLabel.getStyleClass().add("singleDateStyle");
        } else {
            // Normal day
            dayLabel.getStyleClass().add("normalStyle");
        }
        System.out.println("Classes for " + date + ": " + dayLabel.getStyleClass());
    }
    private void handleDateClick(LocalDate clickedDate) {
        if (tempStartDate == null) {
            // First click - set start date
            tempStartDate = clickedDate;
            tempEndDate = null;
        } else if (tempStartDate.equals(clickedDate)) {
            // Clicking the same date twice - set as end date (single day range)
            tempEndDate = clickedDate;
        } else if (tempEndDate == null) {
            // Second click on different date - set as end date
            if (clickedDate.isBefore(tempStartDate)) {
                // Swap if clicked date is before start date
                tempEndDate = tempStartDate;
                tempStartDate = clickedDate;
            } else {
                tempEndDate = clickedDate;
            }
        } else {
            // Third click - start new selection
            tempStartDate = clickedDate;
            tempEndDate = null;
        }

        buildCalendar(); // Refresh calendar to show selection
        updateSelectedDateDisplay(); // Update the date display
    }

    private void updateSelectedDateDisplay() {
        if (selectedDateLabel != null) {
            if (tempStartDate == null) {
                selectedDateLabel.setText("No dates selected");
            } else if (tempEndDate == null || tempStartDate.equals(tempEndDate)) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
                selectedDateLabel.setText("Selected: " + tempStartDate.format(formatter));
            } else {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
                selectedDateLabel.setText("Selected: " + tempStartDate.format(formatter) + " - " + tempEndDate.format(formatter));
            }
        }
    }




    private void loadAttendanceData() {
        attendanceData.clear();

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
            StringBuilder query = new StringBuilder(
                    "SELECT u.user_name, DATE(ua.check_in) as attendance_date, " +
                            "TIME(ua.check_in) as check_in_time, TIME(ua.check_out) as check_out_time, " +
                            "CASE " +
                            "  WHEN ua.check_out IS NOT NULL THEN " +
                            "    CONCAT(FLOOR(TIMESTAMPDIFF(MINUTE, ua.check_in, ua.check_out) / 60), 'h ', " +
                            "           MOD(TIMESTAMPDIFF(MINUTE, ua.check_in, ua.check_out), 60), 'm') " +
                            "  ELSE '-' " +
                            "END as hours_worked, " +
                            "CASE " +
                            "  WHEN ua.reason LIKE 'Absent%' THEN 'Absent' " +
                            "  WHEN ua.check_in IS NULL THEN 'Absent' " +
                            "  WHEN TIME(ua.check_in) > '08:00:00' THEN 'Late' " +
                            "  WHEN ua.check_out IS NOT NULL AND TIME(ua.check_out) < '17:00:00' THEN 'Early Leave' " +
                            "  WHEN ua.check_in IS NOT NULL THEN 'On Time' " +
                            "  ELSE 'Present' " +
                            "END as status, " +
                            "ua.reason as reason " +
                            "FROM user_info u " +
                            "LEFT JOIN user_attendance ua ON u.user_id = ua.user_id " +
                            "LEFT JOIN user_workinfo uw ON u.user_id = uw.user_id " +
                            "WHERE u.user_status = TRUE"
            );

            // Add role-based filtering
            Session session = Session.getInstance();
            int currentUserId = session.getUserid();
            String userRole = session.getRole();

            if ("admin".equals(userRole)) {
                // Admin sees all staff
                query.append(" AND u.user_role = 'staff'");
            } else if ("manager".equals(userRole)) {
                // Manager sees only their direct staff
                query.append(" AND uw.manager = ").append(currentUserId).append(" AND u.user_role = 'staff'");
            } else {
                // Staff sees only themselves
                query.append(" AND u.user_id = ").append(currentUserId);
            }

            // Add date filters
            if (selectedStartDate != null) {
                query.append(" AND DATE(ua.check_in) >= ?");
            }
            if (selectedEndDate != null) {
                query.append(" AND DATE(ua.check_in) <= ?");
            }

            // Add staff filter
            if (staffCombo.getValue() != null && !staffCombo.getValue().equals("All Staff")) {
                query.append(" AND u.user_name = ?");
            }

            // Add status filter - we'll filter this in Java since status is calculated

            query.append(" ORDER BY attendance_date DESC, u.user_name");

            PreparedStatement stmt = conn.prepareStatement(query.toString());
            int paramIndex = 1;

            if (selectedStartDate != null) {
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(selectedStartDate));
            }
            if (selectedEndDate != null) {
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(selectedEndDate));
            }
            if (staffCombo.getValue() != null && !staffCombo.getValue().equals("All Staff")) {
                stmt.setString(paramIndex++, staffCombo.getValue());
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String status = rs.getString("status");

                // Apply status filter
                String selectedStatus = statusCombo.getValue();
                if (selectedStatus != null && !selectedStatus.equals("All")) {
                    if (selectedStatus.equals("Present")) {
                        // Present includes On Time, Late, and Early Leave (anyone who showed up)
                        if (!status.equals("On Time") && !status.equals("Late") && !status.equals("Early Leave")) {
                            continue;
                        }
                    } else if (!status.equals(selectedStatus)) {
                        continue;
                    }
                }

                AttendanceRecord record = new AttendanceRecord(
                        rs.getString("user_name"),
                        rs.getDate("attendance_date") != null ?
                                rs.getDate("attendance_date").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-",
                        rs.getTime("check_in_time") != null ? rs.getTime("check_in_time").toString() : "-",
                        rs.getTime("check_out_time") != null ? rs.getTime("check_out_time").toString() : "-",
                        rs.getString("hours_worked"),
                        rs.getString("reason") != null ? rs.getString("reason") : ""
                );
                attendanceData.add(record);
            }

            recordCountLabel.setText(attendanceData.size() + " records");
            updateSummaryStats();

        } catch (SQLException e) {
            logger.error("Error loading attendance data", e);
        }
    }

    private void updateSummaryStats() {
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
            LocalDate today = LocalDate.now();

            Session session = Session.getInstance();
            int currentUserId = session.getUserid();
            String userRole = session.getRole();

            String roleFilter;
            if ("admin".equals(userRole)) {
                roleFilter = "AND u.user_role = 'staff' AND u.user_status = TRUE";
            } else if ("manager".equals(userRole)) {
                roleFilter = "AND uw.manager = " + currentUserId + " AND u.user_role = 'staff' AND u.user_status = TRUE";
            } else {
                roleFilter = "AND u.user_id = " + currentUserId + " AND u.user_status = TRUE";
            }

            // Present today (checked in and on time)
            String presentQuery = "SELECT COUNT(DISTINCT u.user_id) FROM user_info u " +
                    "JOIN user_attendance ua ON u.user_id = ua.user_id " +
                    "LEFT JOIN user_workinfo uw ON u.user_id = uw.user_id " +
                    "WHERE DATE(ua.check_in) = ? AND TIME(ua.check_in) <= '09:30:00' " + roleFilter;
            PreparedStatement presentStmt = conn.prepareStatement(presentQuery);
            presentStmt.setDate(1, java.sql.Date.valueOf(today));
            ResultSet presentRs = presentStmt.executeQuery();
            if (presentRs.next()) {
                presentTodayLabel.setText(String.valueOf(presentRs.getInt(1)));
            }

            // Late today (checked in after 9:30 AM)
            String lateQuery = "SELECT COUNT(DISTINCT u.user_id) FROM user_info u " +
                    "JOIN user_attendance ua ON u.user_id = ua.user_id " +
                    "LEFT JOIN user_workinfo uw ON u.user_id = uw.user_id " +
                    "WHERE DATE(ua.check_in) = ? AND TIME(ua.check_in) > '09:30:00' " + roleFilter;
            PreparedStatement lateStmt = conn.prepareStatement(lateQuery);
            lateStmt.setDate(1, java.sql.Date.valueOf(today));
            ResultSet lateRs = lateStmt.executeQuery();
            if (lateRs.next()) {
                lateTodayLabel.setText(String.valueOf(lateRs.getInt(1)));
            }

            // Total staff
            String totalQuery;
            PreparedStatement totalStmt;
            if ("admin".equals(userRole)) {
                totalQuery = "SELECT COUNT(*) FROM user_info WHERE user_role = 'staff' AND user_status = TRUE";
                totalStmt = conn.prepareStatement(totalQuery);
            } else if ("manager".equals(userRole)) {
                totalQuery = "SELECT COUNT(*) FROM user_info u JOIN user_workinfo uw ON u.user_id = uw.user_id " +
                        "WHERE uw.manager = ? AND u.user_role = 'staff' AND u.user_status = TRUE";
                totalStmt = conn.prepareStatement(totalQuery);
                totalStmt.setInt(1, currentUserId);
            } else {
                totalQuery = "SELECT 1"; // Staff only sees themselves
                totalStmt = conn.prepareStatement(totalQuery);
            }

            ResultSet totalRs = totalStmt.executeQuery();
            if (totalRs.next()) {
                int totalStaff = totalRs.getInt(1);
                totalStaffLabel.setText(String.valueOf(totalStaff));

                // Calculate absent (total - present - late)
                int present = Integer.parseInt(presentTodayLabel.getText());
                int late = Integer.parseInt(lateTodayLabel.getText());
                int absent = totalStaff - present - late;
                absentTodayLabel.setText(String.valueOf(Math.max(0, absent)));
            }

        } catch (SQLException e) {
            logger.error("Error updating summary stats", e);
        }
    }

    private void showAddAbsentPanel() {
        Session session = Session.getInstance();
        String userRole = session.getRole();

        // Only managers and admins can add absent records
        if (!"manager".equals(userRole) && !"admin".equals(userRole)) {
            showAlert("Access Denied", "Only managers can add absent records.", Alert.AlertType.WARNING);
            return;
        }

        try {
            // Load the new dialog FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/addAbsentDialog.fxml"));
            VBox dialogRoot = loader.load();

            // Get the controller and set up callback
            AddAbsentDialogController dialogController = loader.getController();
            dialogController.setOnSaveCallback((Void) -> {
                // Refresh data when record is saved
                loadAttendanceData();
                updateSummaryStats();
            });

            // Create new stage for the dialog
            Stage dialogStage = new Stage();
            dialogStage.initStyle(StageStyle.UNDECORATED);
            dialogStage.initModality(Modality.APPLICATION_MODAL);

            // Get the current stage to set as owner
            Stage currentStage = (Stage) addAbsentBtn.getScene().getWindow();
            dialogStage.initOwner(currentStage);

            // Create scene and set it to the stage
            Scene scene = new Scene(dialogRoot);

            // Add stylesheet to the scene (adjust path as needed, e.g., "/styles/dialog.css")
            if(managerDashboardController.isDarkMode){
                scene.getStylesheets().clear();
                scene.getStylesheets().add(getClass().getResource("/CSS/manager_dark_mode.css").toExternalForm());

            }else {
                scene.getStylesheets().clear();
                scene.getStylesheets().add(getClass().getResource("/CSS/manager_light_mode.css").toExternalForm());
            }
            // Alternatively, add a style class to the root VBox for targeted styling
            dialogRoot.getStyleClass().add("absent-dialog");

            dialogStage.setScene(scene);

            // Make the dialog non-resizable and center it
            dialogStage.setResizable(false);
            dialogStage.centerOnScreen();

            // Show the dialog
            dialogStage.showAndWait();

        } catch (Exception e) {
            logger.error("Error opening add absent dialog", e);
            showAlert("Error", "Failed to open add absent dialog: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void exportToCSV(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            // Write header
            writer.append("Staff Name,Date,Check In,Check Out,Hours Worked,Remarks\n");

            // Write data
            for (AttendanceRecord record : attendanceData) {
                writer.append(record.getStaffName()).append(",");
                writer.append(record.getDate()).append(",");
                writer.append(record.getCheckIn()).append(",");
                writer.append(record.getCheckOut()).append(",");
                writer.append(record.getHoursWorked()).append(",");
                writer.append(record.getRemarks()).append("\n");
            }

            System.out.println("Attendance data exported successfully to: " + file.getAbsolutePath());

        } catch (IOException e) {
            logger.error("Error exporting attendance data", e);
        }
    }

    // Inner class for attendance record data model
    public static class AttendanceRecord {
        private final SimpleStringProperty staffName;
        private final SimpleStringProperty date;
        private final SimpleStringProperty checkIn;
        private final SimpleStringProperty checkOut;
        private final SimpleStringProperty hoursWorked;
        private final SimpleStringProperty remarks;

        public AttendanceRecord(String staffName, String date, String checkIn, String checkOut,
                                String hoursWorked, String remarks) {
            this.staffName = new SimpleStringProperty(staffName);
            this.date = new SimpleStringProperty(date);
            this.checkIn = new SimpleStringProperty(checkIn);
            this.checkOut = new SimpleStringProperty(checkOut);
            this.hoursWorked = new SimpleStringProperty(hoursWorked);
            this.remarks = new SimpleStringProperty(remarks);
        }

        public String getStaffName() { return staffName.get(); }
        public String getDate() { return date.get(); }
        public String getCheckIn() { return checkIn.get(); }
        public String getCheckOut() { return checkOut.get(); }
        public String getHoursWorked() { return hoursWorked.get(); }
        public String getRemarks() { return remarks.get(); }
    }
}
