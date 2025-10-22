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
import javafx.geometry.Insets;
import javafx.animation.TranslateTransition;
import javafx.animation.Interpolator;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.ParallelTransition;
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

public class managerAttendanceManagementController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(managerAttendanceManagementController.class);

    @FXML private ComboBox<String> staffCombo;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Button addAbsentBtn;

    @FXML private TableView<AttendanceRecord> attendanceTable;
    @FXML private TableColumn<AttendanceRecord, String> staffNameCol;
    @FXML private TableColumn<AttendanceRecord, String> dateCol;
    @FXML private TableColumn<AttendanceRecord, String> checkInCol;
    @FXML private TableColumn<AttendanceRecord, String> checkOutCol;
    @FXML private TableColumn<AttendanceRecord, String> hoursWorkedCol;
    @FXML private TableColumn<AttendanceRecord, String> statusCol;
    @FXML private TableColumn<AttendanceRecord, String> remarksCol;

    @FXML private Label recordCountLabel;
    @FXML private Label presentTodayLabel;
    @FXML private Label absentTodayLabel;
    @FXML private Label lateTodayLabel;
    @FXML private Label totalStaffLabel;

    // Add Absent Panel elements
    @FXML private VBox addAbsentPanel;
    @FXML private ComboBox<String> staffComboDialog;
    @FXML private TextField reasonField;
    @FXML private Button saveAbsentBtn;
    @FXML private Button cancelAbsentBtn;
    @FXML private Label closeAbsentPanelBtn;

    // Date Range Picker elements
    @FXML private HBox dateRangeContainer;
    @FXML private Label dateRangeLabel;
    @FXML private VBox dateRangePickerPanel;
    @FXML private VBox calendarGrid;
    @FXML private Label monthYearLabel;
    @FXML private Button prevMonthBtn;
    @FXML private Button nextMonthBtn;
    @FXML private Button applyDateRangeBtn;
    @FXML private Button cancelDateRangeBtn;
    @FXML private VBox dateRangeFieldContainer;
    @FXML private Pane absentDialogBackdrop;
    @FXML private Label selectedDateLabel;

    // Store selected date range and calendar state
    private LocalDate selectedStartDate;
    private LocalDate selectedEndDate;
    private LocalDate currentMonth;
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
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        remarksCol.setCellValueFactory(new PropertyValueFactory<>("remarks"));

        attendanceTable.setItems(attendanceData);
    }

    private void setupComboBoxes() {
        // Setup status combo
        statusCombo.setItems(FXCollections.observableArrayList(
                "All", "Present", "Absent", "Late", "On Leave"
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
        // Set default date range (current month)
        LocalDate now = LocalDate.now();
        fromDatePicker.setValue(now.withDayOfMonth(1));
        toDatePicker.setValue(now);

        loadAttendanceData();
    }

    private void setupRealTimeListeners() {
        // Add listeners for real-time updates
        staffCombo.setOnAction(e -> loadAttendanceData());
        statusCombo.setOnAction(e -> loadAttendanceData());
        fromDatePicker.setOnAction(e -> loadAttendanceData());
        toDatePicker.setOnAction(e -> loadAttendanceData());
    }

    @FXML
    private void onAddAbsent() {
        showAddAbsentPanel();
    }

    @FXML
    private void closeAbsentPanel() {
        hideAddAbsentPanel();
    }

    @FXML
    private void saveAbsentRecord() {
        handleSaveAbsentRecord();
    }


    @FXML
    public void previousMonth() {
        currentMonth = currentMonth.minusMonths(1);
        buildCalendar();
    }

    @FXML
    public void nextMonth() {
        currentMonth = currentMonth.plusMonths(1);
        buildCalendar();
    }

    // Temporary methods to prevent FXML loading errors (calendar is now always visible)
    @FXML
    public void showDateRangePicker() {
        // Do nothing - calendar is now always visible in the dialog
    }

    @FXML
    public void cancelDateRange() {
        // Do nothing - no separate date picker to cancel
    }

    @FXML
    public void applyDateRange() {
        // Do nothing - dates are selected directly on the always-visible calendar
    }

    @FXML
    public void closeCalendarOnBackdrop() {
        // Do nothing - no separate calendar popup to close
    }


    private void updateDateRangeLabel() {
        if (selectedStartDate != null && selectedEndDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
            if (selectedStartDate.equals(selectedEndDate)) {
                dateRangeLabel.setText(selectedStartDate.format(formatter));
            } else {
                dateRangeLabel.setText(selectedStartDate.format(formatter) + " - " + selectedEndDate.format(formatter));
            }
        } else {
            dateRangeLabel.setText("Select date range...");
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
            weekRow.setAlignment(javafx.geometry.Pos.CENTER);

            for (int dayOfWeek = 0; dayOfWeek < 7; dayOfWeek++) {
                Label dayLabel = new Label();
                dayLabel.setPrefWidth(35);
                dayLabel.setPrefHeight(35);
                dayLabel.setAlignment(javafx.geometry.Pos.CENTER);
                dayLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #374151; -fx-cursor: hand;");

                if (week == 0 && dayOfWeek < startDayOfWeek) {
                    // Empty cell for days before month starts
                    dayLabel.setText("");
                    dayLabel.setStyle("-fx-cursor: default;");
                } else if (day <= daysInMonth) {
                    dayLabel.setText(String.valueOf(day));
                    LocalDate cellDate = currentMonth.withDayOfMonth(day);

                    // Style the day based on selection state
                    styleDayCell(dayLabel, cellDate);

                    // Add click handler
                    dayLabel.setOnMouseClicked(e -> handleDateClick(cellDate));

                    day++;
                } else {
                    // Empty cell for days after month ends
                    dayLabel.setText("");
                    dayLabel.setStyle("-fx-cursor: default;");
                }

                weekRow.getChildren().add(dayLabel);
            }

            calendarGrid.getChildren().add(weekRow);

            if (day > daysInMonth) break;
        }
    }

    private void styleDayCell(Label dayLabel, LocalDate date) {
        String baseStyle = "-fx-font-size: 12; -fx-cursor: hand; -fx-background-radius: 18; -fx-alignment: center;";

        if (tempStartDate != null && tempEndDate != null) {
            // Range selection
            if (date.equals(tempStartDate) || date.equals(tempEndDate)) {
                // Start or end date - red circle
                dayLabel.setStyle(baseStyle + "-fx-background-color: #ef4444; -fx-text-fill: white;");
            } else if (date.isAfter(tempStartDate) && date.isBefore(tempEndDate)) {
                // In between dates - gradient background like your image
                dayLabel.setStyle(baseStyle + "-fx-background-color: linear-gradient(to right, #667eea, #764ba2); -fx-text-fill: white; -fx-background-radius: 20;");
            } else {
                // Normal day
                dayLabel.setStyle(baseStyle + "-fx-text-fill: #374151;");
            }
        } else if (tempStartDate != null && date.equals(tempStartDate)) {
            // Single date selected - red circle
            dayLabel.setStyle(baseStyle + "-fx-background-color: #ef4444; -fx-text-fill: white;");
        } else {
            // Normal day
            dayLabel.setStyle(baseStyle + "-fx-text-fill: #374151;");
        }

        // Add hover effect
        dayLabel.setOnMouseEntered(e -> {
            if (!dayLabel.getStyle().contains("#ef4444") && !dayLabel.getStyle().contains("linear-gradient")) {
                dayLabel.setStyle(dayLabel.getStyle() + "-fx-background-color: #f3f4f6;");
            }
        });

        dayLabel.setOnMouseExited(e -> {
            styleDayCell(dayLabel, date); // Restore original style
        });
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
                            "  WHEN ua.check_in IS NULL THEN 'Absent' " +
                            "  WHEN TIME(ua.check_in) > '09:30:00' THEN 'Late' " +
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
            if (fromDatePicker.getValue() != null) {
                query.append(" AND DATE(ua.check_in) >= ?");
            }
            if (toDatePicker.getValue() != null) {
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

            if (fromDatePicker.getValue() != null) {
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(fromDatePicker.getValue()));
            }
            if (toDatePicker.getValue() != null) {
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(toDatePicker.getValue()));
            }
            if (staffCombo.getValue() != null && !staffCombo.getValue().equals("All Staff")) {
                stmt.setString(paramIndex++, staffCombo.getValue());
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String status = rs.getString("status");

                // Apply status filter
                if (statusCombo.getValue() != null && !statusCombo.getValue().equals("All") &&
                        !status.equals(statusCombo.getValue())) {
                    continue;
                }

                AttendanceRecord record = new AttendanceRecord(
                        rs.getString("user_name"),
                        rs.getDate("attendance_date") != null ?
                                rs.getDate("attendance_date").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-",
                        rs.getTime("check_in_time") != null ? rs.getTime("check_in_time").toString() : "-",
                        rs.getTime("check_out_time") != null ? rs.getTime("check_out_time").toString() : "-",
                        rs.getString("hours_worked"),
                        status,
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

        // Load staff members and set default values
        loadStaffForDialog(staffComboDialog);
        tempStartDate = null;
        tempEndDate = null;
        reasonField.clear();

        // Initialize calendar
        currentMonth = LocalDate.now().withDayOfMonth(1);
        buildCalendar();
        updateSelectedDateDisplay();

        // Show backdrop and center popup
        if (absentDialogBackdrop != null) {
            absentDialogBackdrop.setVisible(true);
        }
        addAbsentPanel.setVisible(true);

        // Ensure center positioning
        addAbsentPanel.setTranslateX(0);
        addAbsentPanel.setTranslateY(0);

        // Set initial animation state
        addAbsentPanel.setOpacity(0);
        addAbsentPanel.setScaleX(0.8);
        addAbsentPanel.setScaleY(0.8);

        // Animate popup appearance
        javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(Duration.millis(200), addAbsentPanel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        javafx.animation.ScaleTransition scaleIn = new javafx.animation.ScaleTransition(Duration.millis(200), addAbsentPanel);
        scaleIn.setFromX(0.8);
        scaleIn.setFromY(0.8);
        scaleIn.setToX(1.0);
        scaleIn.setToY(1.0);

        javafx.animation.ParallelTransition showPopup = new javafx.animation.ParallelTransition(fadeIn, scaleIn);
        showPopup.setInterpolator(Interpolator.EASE_OUT);
        showPopup.play();
    }

    private void hideAddAbsentPanel() {
        // Animate popup disappearance
        javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(Duration.millis(150), addAbsentPanel);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        javafx.animation.ScaleTransition scaleOut = new javafx.animation.ScaleTransition(Duration.millis(150), addAbsentPanel);
        scaleOut.setFromX(1.0);
        scaleOut.setFromY(1.0);
        scaleOut.setToX(0.8);
        scaleOut.setToY(0.8);

        javafx.animation.ParallelTransition hidePopup = new javafx.animation.ParallelTransition(fadeOut, scaleOut);
        hidePopup.setInterpolator(Interpolator.EASE_IN);
        hidePopup.setOnFinished(e -> {
            addAbsentPanel.setVisible(false);
            if (absentDialogBackdrop != null) {
                absentDialogBackdrop.setVisible(false);
            }
            // Clear form
            staffComboDialog.setValue(null);
            tempStartDate = null;
            tempEndDate = null;
            reasonField.clear();
            // Reset popup transform
            addAbsentPanel.setOpacity(1);
            addAbsentPanel.setScaleX(1);
            addAbsentPanel.setScaleY(1);
        });
        hidePopup.play();
    }

    private void handleSaveAbsentRecord() {
        String selectedStaff = staffComboDialog.getValue();
        String reason = reasonField.getText().trim();

        if (selectedStaff == null || tempStartDate == null || reason.isEmpty()) {
            showAlert("Invalid Input", "Please fill all fields and select a date range.", Alert.AlertType.ERROR);
            return;
        }

        LocalDate endDate = tempEndDate != null ? tempEndDate : tempStartDate;

        if (tempStartDate.isAfter(endDate)) {
            showAlert("Invalid Date Range", "Start date cannot be after end date.", Alert.AlertType.ERROR);
            return;
        }

        addAbsentRecord(selectedStaff, tempStartDate, endDate, reason);
        hideAddAbsentPanel();
    }

    private void loadStaffForDialog(ComboBox<String> staffComboDialog) {
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
            Session session = Session.getInstance();
            int currentUserId = session.getUserid();
            String userRole = session.getRole();

            String query;
            PreparedStatement stmt;

            if ("admin".equals(userRole)) {
                query = "SELECT DISTINCT user_name FROM user_info WHERE user_role = 'staff' AND user_status = TRUE ORDER BY user_name";
                stmt = conn.prepareStatement(query);
            } else {
                query = "SELECT DISTINCT u.user_name FROM user_info u " +
                        "JOIN user_workinfo uw ON u.user_id = uw.user_id " +
                        "WHERE u.user_status = TRUE AND uw.manager = ? AND u.user_role = 'staff' " +
                        "ORDER BY u.user_name";
                stmt = conn.prepareStatement(query);
                stmt.setInt(1, currentUserId);
            }

            ResultSet rs = stmt.executeQuery();
            ObservableList<String> staffList = FXCollections.observableArrayList();

            while (rs.next()) {
                staffList.add(rs.getString("user_name"));
            }

            staffComboDialog.setItems(staffList);

        } catch (SQLException e) {
            logger.error("Error loading staff for dialog", e);
        }
    }

    private void addAbsentRecord(String staffName, LocalDate fromDate, LocalDate toDate, String reason) {
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
            // Get staff user_id
            String getUserIdQuery = "SELECT user_id FROM user_info WHERE user_name = ?";
            PreparedStatement getUserIdStmt = conn.prepareStatement(getUserIdQuery);
            getUserIdStmt.setString(1, staffName);
            ResultSet rs = getUserIdStmt.executeQuery();

            if (!rs.next()) {
                showAlert("Error", "Staff member not found.", Alert.AlertType.ERROR);
                return;
            }

            int staffUserId = rs.getInt("user_id");

            // Insert absent records for each day in the range
            String insertQuery = "INSERT INTO user_attendance (user_id, check_in, check_out, reason) VALUES (?, ?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertQuery);

            LocalDate currentDate = fromDate;
            int recordsAdded = 0;

            while (!currentDate.isAfter(toDate)) {
                // Check if record already exists for this date
                String checkQuery = "SELECT COUNT(*) FROM user_attendance WHERE user_id = ? AND DATE(check_in) = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.setInt(1, staffUserId);
                checkStmt.setDate(2, java.sql.Date.valueOf(currentDate));
                ResultSet checkRs = checkStmt.executeQuery();

                if (checkRs.next() && checkRs.getInt(1) == 0) {
                    // No record exists, add absent record
                    java.sql.Timestamp absentTimestamp = java.sql.Timestamp.valueOf(currentDate.atStartOfDay());

                    insertStmt.setInt(1, staffUserId);
                    insertStmt.setTimestamp(2, absentTimestamp);
                    insertStmt.setTimestamp(3, absentTimestamp);
                    insertStmt.setString(4, "Absent - " + reason);
                    insertStmt.executeUpdate();
                    recordsAdded++;
                }

                currentDate = currentDate.plusDays(1);
            }

            if (recordsAdded > 0) {
                showAlert("Success", recordsAdded + " absent record(s) added successfully.", Alert.AlertType.INFORMATION);
                loadAttendanceData(); // Refresh the table
                updateSummaryStats(); // Update summary
            } else {
                showAlert("No Records Added", "Records already exist for the selected date range.", Alert.AlertType.WARNING);
            }

        } catch (SQLException e) {
            logger.error("Error adding absent record", e);
            showAlert("Database Error", "Failed to add absent record: " + e.getMessage(), Alert.AlertType.ERROR);
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
            writer.append("Staff Name,Date,Check In,Check Out,Hours Worked,Status,Remarks\n");

            // Write data
            for (AttendanceRecord record : attendanceData) {
                writer.append(record.getStaffName()).append(",");
                writer.append(record.getDate()).append(",");
                writer.append(record.getCheckIn()).append(",");
                writer.append(record.getCheckOut()).append(",");
                writer.append(record.getHoursWorked()).append(",");
                writer.append(record.getStatus()).append(",");
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
        private final SimpleStringProperty status;
        private final SimpleStringProperty remarks;

        public AttendanceRecord(String staffName, String date, String checkIn, String checkOut,
                                String hoursWorked, String status, String remarks) {
            this.staffName = new SimpleStringProperty(staffName);
            this.date = new SimpleStringProperty(date);
            this.checkIn = new SimpleStringProperty(checkIn);
            this.checkOut = new SimpleStringProperty(checkOut);
            this.hoursWorked = new SimpleStringProperty(hoursWorked);
            this.status = new SimpleStringProperty(status);
            this.remarks = new SimpleStringProperty(remarks);
        }

        public String getStaffName() { return staffName.get(); }
        public String getDate() { return date.get(); }
        public String getCheckIn() { return checkIn.get(); }
        public String getCheckOut() { return checkOut.get(); }
        public String getHoursWorked() { return hoursWorked.get(); }
        public String getStatus() { return status.get(); }
        public String getRemarks() { return remarks.get(); }
    }
}
