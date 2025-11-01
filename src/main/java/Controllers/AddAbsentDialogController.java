package Controllers;

import Database.DatabaseConnectionManager;
import Utils.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class AddAbsentDialogController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(AddAbsentDialogController.class);

    @FXML private ComboBox<String> staffComboDialog;
    @FXML private TextField reasonField;
    @FXML private Label closeDialogBtn;
    @FXML private Button cancelBtn;
    @FXML private Button saveBtn;

    // Calendar elements
    @FXML private VBox dateRangePickerPanel;
    @FXML private VBox calendarGrid;
    @FXML private Label monthYearLabel;
    @FXML private Button prevMonthBtn;
    @FXML private Button nextMonthBtn;
    @FXML private Label selectedDateLabel;

    // Store selected date range and calendar state
    private LocalDate selectedStartDate;
    private LocalDate selectedEndDate;
    private YearMonth currentMonth;
    private LocalDate tempStartDate;
    private LocalDate tempEndDate;

    // Callback to refresh main window data
    private Consumer<Void> onSaveCallback;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadStaffMembers();
        initializeCalendar();
    }

    public void setOnSaveCallback(Consumer<Void> callback) {
        this.onSaveCallback = callback;
    }

    private void loadStaffMembers() {
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
            logger.error("Error loading staff members", e);
        }
    }

    private void initializeCalendar() {
        currentMonth = YearMonth.now();
        tempStartDate = null;
        tempEndDate = null;
        buildCalendar();
        updateSelectedDateDisplay();
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

    private void buildCalendar() {
        calendarGrid.getChildren().clear();

        // Update month/year label
        String monthYear = currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + currentMonth.getYear();
        monthYearLabel.setText(monthYear);

        LocalDate firstOfMonth = currentMonth.atDay(1);
        int daysInMonth = currentMonth.lengthOfMonth();
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
                dayLabel.getStyleClass().addAll("dayLabel","dayLabelAbsent");

                if (week == 0 && dayOfWeek < startDayOfWeek) {
                    // Empty cell for days before month starts
                    dayLabel.setText("");
                    dayLabel.setStyle("-fx-cursor: default;");
                } else if (day <= daysInMonth) {
                    dayLabel.setText(String.valueOf(day));
                    LocalDate cellDate = currentMonth.atDay(day);

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
        // Clear existing style classes (except base "dayLabel")
        dayLabel.getStyleClass().removeAll("normalStyle", "selectedAbsentStyle", "betweenAbsentStyle", "singleDateAbsentStyle");

        // No inline styles needed—CSS handles everything now
        if (tempStartDate != null && tempEndDate != null) {
            // Range selection
            if (date.equals(tempStartDate) || date.equals(tempEndDate)) {
                // Start or end date
                dayLabel.getStyleClass().add("selectedAbsentStyle");
            } else if (date.isAfter(tempStartDate) && date.isBefore(tempEndDate)) {
                // In between dates
                dayLabel.getStyleClass().add("betweenAbsentStyle");
            } else {
                // Normal day
                dayLabel.getStyleClass().add("normalStyle");
            }
        } else if (tempStartDate != null && date.equals(tempStartDate)) {
            // Single date selected
            dayLabel.getStyleClass().add("singleDateAbsentStyle");
        } else {
            // Normal day
            dayLabel.getStyleClass().add("normalStyle");
        }
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

    @FXML
    private void closeDialog() {
        Stage stage = (Stage) closeDialogBtn.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void saveAbsentRecord() {
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

        if (addAbsentRecord(selectedStaff, tempStartDate, endDate, reason)) {
            // Notify parent window to refresh data
            if (onSaveCallback != null) {
                onSaveCallback.accept(null);
            }
            closeDialog();
        }
    }

    private boolean addAbsentRecord(String staffName, LocalDate fromDate, LocalDate toDate, String reason) {
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
            // Get staff user_id
            String getUserIdQuery = "SELECT user_id FROM user_info WHERE user_name = ?";
            PreparedStatement getUserIdStmt = conn.prepareStatement(getUserIdQuery);
            getUserIdStmt.setString(1, staffName);
            ResultSet rs = getUserIdStmt.executeQuery();

            if (!rs.next()) {
                showAlert("Error", "Staff member not found.", Alert.AlertType.ERROR);
                return false;
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
                return true;
            } else {
                showAlert("No Records Added", "Records already exist for the selected date range.", Alert.AlertType.WARNING);
                return false;
            }

        } catch (SQLException e) {
            logger.error("Error adding absent record", e);
            showAlert("Database Error", "Failed to add absent record: " + e.getMessage(), Alert.AlertType.ERROR);
            return false;
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
