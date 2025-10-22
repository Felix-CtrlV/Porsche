package Controllers;

import Database.DatabaseConnectionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
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
    @FXML private Button filterBtn;
    @FXML private Button clearBtn;
    @FXML private Button exportBtn;

    @FXML private TableView<AttendanceRecord> attendanceTable;
    @FXML private TableColumn<AttendanceRecord, String> staffNameCol;
    @FXML private TableColumn<AttendanceRecord, String> dateCol;
    @FXML private TableColumn<AttendanceRecord, String> checkInCol;
    @FXML private TableColumn<AttendanceRecord, String> checkOutCol;
    @FXML private TableColumn<AttendanceRecord, String> hoursWorkedCol;
    @FXML private TableColumn<AttendanceRecord, String> statusCol;
    @FXML private TableColumn<AttendanceRecord, String> remarksCol;
    @FXML private TableColumn<AttendanceRecord, String> actionsCol;

    @FXML private Label recordCountLabel;
    @FXML private Label presentTodayLabel;
    @FXML private Label absentTodayLabel;
    @FXML private Label lateTodayLabel;
    @FXML private Label totalStaffLabel;

    private ObservableList<AttendanceRecord> attendanceData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        setupComboBoxes();
        loadInitialData();
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

        // Setup actions column with buttons
        actionsCol.setCellFactory(column -> new TableCell<AttendanceRecord, String>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");

            {
                editBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 10; -fx-padding: 2 8; -fx-background-radius: 4;");
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 10; -fx-padding: 2 8; -fx-background-radius: 4;");

                editBtn.setOnAction(e -> {
                    AttendanceRecord record = getTableView().getItems().get(getIndex());
                    editAttendanceRecord(record);
                });

                deleteBtn.setOnAction(e -> {
                    AttendanceRecord record = getTableView().getItems().get(getIndex());
                    deleteAttendanceRecord(record);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(5);
                    buttons.getChildren().addAll(editBtn, deleteBtn);
                    setGraphic(buttons);
                }
            }
        });

        attendanceTable.setItems(attendanceData);
    }

    private void setupComboBoxes() {
        // Setup status combo
        statusCombo.setItems(FXCollections.observableArrayList(
                "All", "Present", "Absent", "Late", "Half Day", "On Leave"
        ));
        statusCombo.setValue("All");

        // Load staff members
        loadStaffMembers();
    }

    private void loadStaffMembers() {
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
            String query = "SELECT DISTINCT CONCAT(firstName, ' ', lastName) as fullName FROM users WHERE role = 'staff' AND status = 'active'";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            ObservableList<String> staffList = FXCollections.observableArrayList();
            staffList.add("All Staff");

            while (rs.next()) {
                staffList.add(rs.getString("fullName"));
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

    @FXML
    private void onFilterAttendance() {
        loadAttendanceData();
    }

    @FXML
    private void onClearFilter() {
        staffCombo.setValue("All Staff");
        statusCombo.setValue("All");
        LocalDate now = LocalDate.now();
        fromDatePicker.setValue(now.withDayOfMonth(1));
        toDatePicker.setValue(now);
        loadAttendanceData();
    }

    @FXML
    private void onExportAttendance() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Attendance Data");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        Stage stage = (Stage) exportBtn.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            exportToCSV(file);
        }
    }

    private void loadAttendanceData() {
        attendanceData.clear();

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection()) {
            StringBuilder query = new StringBuilder(
                    "SELECT u.firstName, u.lastName, a.date, a.checkIn, a.checkOut, " +
                            "a.hoursWorked, a.status, a.remarks " +
                            "FROM attendance a " +
                            "JOIN users u ON a.userId = u.id " +
                            "WHERE u.role = 'staff' AND u.status = 'active'"
            );

            // Add date filters
            if (fromDatePicker.getValue() != null) {
                query.append(" AND a.date >= ?");
            }
            if (toDatePicker.getValue() != null) {
                query.append(" AND a.date <= ?");
            }

            // Add staff filter
            if (staffCombo.getValue() != null && !staffCombo.getValue().equals("All Staff")) {
                query.append(" AND CONCAT(u.firstName, ' ', u.lastName) = ?");
            }

            // Add status filter
            if (statusCombo.getValue() != null && !statusCombo.getValue().equals("All")) {
                query.append(" AND a.status = ?");
            }

            query.append(" ORDER BY a.date DESC, u.firstName");

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
            if (statusCombo.getValue() != null && !statusCombo.getValue().equals("All")) {
                stmt.setString(paramIndex++, statusCombo.getValue());
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                AttendanceRecord record = new AttendanceRecord(
                        rs.getString("firstName") + " " + rs.getString("lastName"),
                        rs.getDate("date").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        rs.getTime("checkIn") != null ? rs.getTime("checkIn").toString() : "-",
                        rs.getTime("checkOut") != null ? rs.getTime("checkOut").toString() : "-",
                        rs.getString("hoursWorked") != null ? rs.getString("hoursWorked") : "-",
                        rs.getString("status"),
                        rs.getString("remarks") != null ? rs.getString("remarks") : ""
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

            // Present today
            String presentQuery = "SELECT COUNT(*) FROM attendance a JOIN users u ON a.userId = u.id " +
                    "WHERE a.date = ? AND a.status = 'Present' AND u.role = 'staff' AND u.status = 'active'";
            PreparedStatement presentStmt = conn.prepareStatement(presentQuery);
            presentStmt.setDate(1, java.sql.Date.valueOf(today));
            ResultSet presentRs = presentStmt.executeQuery();
            if (presentRs.next()) {
                presentTodayLabel.setText(String.valueOf(presentRs.getInt(1)));
            }

            // Absent today
            String absentQuery = "SELECT COUNT(*) FROM attendance a JOIN users u ON a.userId = u.id " +
                    "WHERE a.date = ? AND a.status = 'Absent' AND u.role = 'staff' AND u.status = 'active'";
            PreparedStatement absentStmt = conn.prepareStatement(absentQuery);
            absentStmt.setDate(1, java.sql.Date.valueOf(today));
            ResultSet absentRs = absentStmt.executeQuery();
            if (absentRs.next()) {
                absentTodayLabel.setText(String.valueOf(absentRs.getInt(1)));
            }

            // Late today
            String lateQuery = "SELECT COUNT(*) FROM attendance a JOIN users u ON a.userId = u.id " +
                    "WHERE a.date = ? AND a.status = 'Late' AND u.role = 'staff' AND u.status = 'active'";
            PreparedStatement lateStmt = conn.prepareStatement(lateQuery);
            lateStmt.setDate(1, java.sql.Date.valueOf(today));
            ResultSet lateRs = lateStmt.executeQuery();
            if (lateRs.next()) {
                lateTodayLabel.setText(String.valueOf(lateRs.getInt(1)));
            }

            // Total staff
            String totalQuery = "SELECT COUNT(*) FROM users WHERE role = 'staff' AND status = 'active'";
            PreparedStatement totalStmt = conn.prepareStatement(totalQuery);
            ResultSet totalRs = totalStmt.executeQuery();
            if (totalRs.next()) {
                totalStaffLabel.setText(String.valueOf(totalRs.getInt(1)));
            }

        } catch (SQLException e) {
            logger.error("Error updating summary stats", e);
        }
    }

    private void editAttendanceRecord(AttendanceRecord record) {
        // TODO: Implement edit functionality
        System.out.println("Edit record: " + record.getStaffName());
    }

    private void deleteAttendanceRecord(AttendanceRecord record) {
        // TODO: Implement delete functionality
        System.out.println("Delete record: " + record.getStaffName());
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
