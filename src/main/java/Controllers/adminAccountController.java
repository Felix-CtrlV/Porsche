package Controllers;

import DAO.AdminAccountDAO;
import Model.user;
import Utils.ThreadPoolManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.animation.TranslateTransition;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.File;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

public class adminAccountController {
    private static final Logger logger = LoggerFactory.getLogger(adminAccountController.class);

    // ---------- Staff Info ----------
    @FXML
    private Label StaffListTitleLabel, StaffAddressLabel, StaffDOBLabel, StaffEmailLabel, StaffNameLabel, StaffPhoneLabel, StaffReasonLabel, addUser;
    @FXML
    private ImageView StaffImage;
    @FXML
    private HBox terminationReasonBox;
    @FXML
    private TextField StaffSearchText;
    @FXML
    private VBox staffListContainer;
    @FXML
    private Button ActiveInactiveSwitchbtn, SearchNamebtn;
    @FXML
    private ComboBox<String> roleCombo;

    // ---------- Chart ----------
    @FXML
    private AreaChart<String, Number> lineChart;

    // ---------- Staff Table (for Managers) ----------
    @FXML
    private TableView<user> staffTableView;
    @FXML
    private TableColumn<user, Integer> staffIdCol;
    @FXML
    private TableColumn<user, String> staffNameCol;
    @FXML
    private TableColumn<user, String> staffPhoneCol;
    @FXML
    private TableColumn<user, String> staffEmailCol;
    @FXML
    private TableColumn<user, String> staffStatusCol;

    // ---------- Management Pane ----------
    @FXML
    private VBox managementPane;
    @FXML
    private Button manageEmployeeBtn, closeManagementBtn, updateSalaryBtn, applyBonusBtn, terminateEmployeeBtn;
    @FXML
    private TextField salaryField;
    @FXML
    private TextArea terminationReasonField;
    @FXML
    private Label carTargetLabel, partTargetLabel, carBonusLabel, partBonusLabel, totalBonusLabel, bonusBreakdownLabel;

    // ---------- Month / Year ----------
    @FXML
    private ChoiceBox<String> monthBox;
    @FXML
    private ChoiceBox<Integer> yearBox;
    @FXML
    private Button NextMonthbtn, PreviousMonthbtn, NextYearbtn, PreviousYearbtn;

    // ---------- Target / Attendance (Placeholders) ----------
    @FXML
    private Circle attendanceCircle, attendanceBackCircle, carCircle, partCircle;
    @FXML
    private Text attendancePercent, targetCar, targetPart;
    @FXML
    private Label targetCarMessagelbl, targetPartMessagelbl, targetOverCar, targetOverPart, DueDateLabel;
    @FXML
    private VBox targetlayer;

    // ---------- Variables ----------
    private boolean showActive = true;
    private final AdminAccountDAO dao;
    private final ObservableList<user> staffList = FXCollections.observableArrayList();
    private int selectedStaffId = 0;
    private final ContextMenu searchMenu = new ContextMenu();
    private final LocalDate today = LocalDate.now();
    private int currentMonth, currentYear;
    private LocalDate currentStaffStartDate;
    private LocalDate currentStaffEndDate;
    private boolean isManagementPaneOpen = false;

    public adminAccountController() {
        try {
            dao = new AdminAccountDAO();
        } catch (Exception e) {
            logger.error("Failed to initialize AdminAccountDAO", e);
            throw new RuntimeException("Failed to initialize controller", e);
        }
    }

    @FXML
    public void initialize() {
        currentMonth = today.getMonthValue();
        currentYear = today.getYear();

        // Initialize role combo
        roleCombo.setItems(FXCollections.observableArrayList("Manager", "Staff"));
        roleCombo.setValue("Manager");
        roleCombo.valueProperty().addListener((obs, o, n) -> loadStaffCardsAsync(showActive));

        // Setup addUser click handler
        if (addUser != null) {
            addUser.setOnMouseClicked(this::openUserRegistration);
        }

        // Setup search, month/year, chart
        setupSearch();
        setupMonthYear();
        setupEmptyChart();
        setupStaffTable();

        StaffListTitleLabel.setText("List (Active)");
        loadStaffCardsAsync(showActive);
    }
    
    private void openUserRegistration(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/adminUserRegister.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setTitle("Register New User");
            stage.initStyle(StageStyle.DECORATED);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.showAndWait();
            
            // Reload staff list after registration window closes
            loadStaffCardsAsync(showActive);
        } catch (IOException e) {
            logger.error("Failed to open user registration window", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Failed to open registration window");
            alert.show();
        }
    }

    // ---------- Chart ----------
    private void setupEmptyChart() {
        if (lineChart == null)
            return;
        lineChart.getData().clear();
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        s.setName("Weekly Sales");
        for (int i = 1; i <= 4; i++)
            s.getData().add(new XYChart.Data<>("Week " + i, 0));
        lineChart.getData().add(s);
    }

    // ---------- Staff Table Setup ----------
    private void setupStaffTable() {
        if (staffTableView == null)
            return;
        
        staffIdCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getId()));
        staffNameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUsername()));
        staffPhoneCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPhone()));
        staffEmailCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));
        staffStatusCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIs_active()));
    }

    private void loadWeeklySalesAsync(int staffId, int month, int year) {
        if (staffId == 0)
            return;

        Task<List<Double>> task = new Task<>() {
            @Override
            protected List<Double> call() throws Exception {
                return dao.getWeeklySales(staffId, month, year);
            }
        };

        task.setOnSucceeded(e -> updateChart(task.getValue()));
        task.setOnFailed(e -> {
            logger.error("Failed to load weekly sales", task.getException());
            Platform.runLater(() -> setupEmptyChart());
        });
        ThreadPoolManager.getInstance().execute(task);
    }

    private void updateChart(List<Double> weeklySales) {
        if (lineChart == null || weeklySales == null)
            return;

        lineChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Weekly Sales");

        for (int i = 0; i < 4; i++)
            series.getData().add(new XYChart.Data<>("Week " + (i + 1), weeklySales.get(i)));

        lineChart.getData().add(series);
    }

    // ---------- Load Staff Under Manager ----------
    private void loadStaffUnderManagerAsync(int managerId) {
        if (managerId == 0)
            return;

        Task<List<user>> task = new Task<>() {
            @Override
            protected List<user> call() throws Exception {
                return dao.getStaffUnderManager(managerId, "Active");
            }
        };

        task.setOnSucceeded(e -> updateStaffTable(task.getValue()));
        task.setOnFailed(e -> {
            logger.error("Failed to load staff under manager", task.getException());
            Platform.runLater(() -> staffTableView.getItems().clear());
        });
        ThreadPoolManager.getInstance().execute(task);
    }

    private void updateStaffTable(List<user> staffList) {
        if (staffTableView == null || staffList == null)
            return;

        staffTableView.getItems().clear();
        staffTableView.getItems().addAll(staffList);
    }

    // ---------- Attendance ----------
    private void loadAttendanceAsync(int staffId, int month, int year) {
        if (staffId == 0)
            return;

        Task<Double> task = new Task<>() {
            @Override
            protected Double call() throws Exception {
                return dao.getMonthlyAttendance(staffId, month, year);
            }
        };

        task.setOnSucceeded(e -> updateAttendance(task.getValue()));
        task.setOnFailed(e -> {
            logger.error("Failed to load attendance", task.getException());
            Platform.runLater(() -> updateAttendance(0.0));
        });
        ThreadPoolManager.getInstance().execute(task);
    }

    private void updateAttendance(double percent) {
        if (attendancePercent == null || attendanceCircle == null)
            return;

        attendancePercent.setText(String.format("%.0f%%", percent));
        
        // Update circle stroke dash to show percentage
        double radius = 100;
        double circumference = 2 * Math.PI * radius;
        double dashLength = (percent / 100.0) * circumference;
        attendanceCircle.getStrokeDashArray().setAll(dashLength, circumference);
        attendanceCircle.setVisible(true);
    }

    // ---------- Targets ----------
    private void loadTargetsAsync(int staffId, int month, int year) {
        if (staffId == 0)
            return;

        Task<int[][]> task = new Task<>() {
            @Override
            protected int[][] call() throws Exception {
                return dao.getTargetData(staffId, month, year);
            }
        };

        task.setOnSucceeded(e -> {
            int[][] data = task.getValue();
            updateTargets(data[0], data[1]);
        });
        task.setOnFailed(e -> {
            logger.error("Failed to load targets", task.getException());
            Platform.runLater(() -> updateTargets(new int[]{0, 0}, new int[]{0, 0}));
        });
        ThreadPoolManager.getInstance().execute(task);
    }

    private void updateTargets(int[] carData, int[] partData) {
        if (targetCar == null || targetPart == null)
            return;

        // Car targets
        int carAchieved = carData[0];
        int carTarget = carData[1];
        targetCar.setText(carAchieved + "/" + carTarget);
        
        if (carTarget > 0) {
            int carOver = carAchieved - carTarget;
            targetOverCar.setText((carOver >= 0 ? "+" : "") + carOver);
            targetOverCar.setStyle("-fx-text-fill: " + (carOver >= 0 ? "#10b981" : "#ef4444") + "; -fx-font-weight: bold; -fx-font-size: 12;");
            targetCarMessagelbl.setText(carOver >= 0 ? "Target Achieved! 🎉" : "Missed the target");
            targetCarMessagelbl.setStyle("-fx-text-fill: " + (carOver >= 0 ? "#10b981" : "#ef4444") + "; -fx-font-weight: bold; -fx-font-size: 12;");
            
            double carPercent = Math.min(100.0, (carAchieved * 100.0) / carTarget);
            updateCircleProgress(carCircle, carPercent);
        } else {
            targetOverCar.setText("+0");
            targetCarMessagelbl.setText("No target set");
            updateCircleProgress(carCircle, 0);
        }

        // Part targets
        int partAchieved = partData[0];
        int partTarget = partData[1];
        targetPart.setText(partAchieved + "/" + partTarget);
        
        if (partTarget > 0) {
            int partOver = partAchieved - partTarget;
            targetOverPart.setText((partOver >= 0 ? "+" : "") + partOver);
            targetOverPart.setStyle("-fx-text-fill: " + (partOver >= 0 ? "#10b981" : "#ef4444") + "; -fx-font-weight: bold; -fx-font-size: 12;");
            targetPartMessagelbl.setText(partOver >= 0 ? "Target Achieved! 🎉" : "Missed the target");
            targetPartMessagelbl.setStyle("-fx-text-fill: " + (partOver >= 0 ? "#10b981" : "#ef4444") + "; -fx-font-weight: bold; -fx-font-size: 12;");
            
            double partPercent = Math.min(100.0, (partAchieved * 100.0) / partTarget);
            updateCircleProgress(partCircle, partPercent);
        } else {
            targetOverPart.setText("+0");
            targetPartMessagelbl.setText("No target set");
            updateCircleProgress(partCircle, 0);
        }
    }

    private void updateCircleProgress(Circle circle, double percent) {
        if (circle == null)
            return;
        
        double radius = circle.getRadius();
        double circumference = 2 * Math.PI * radius;
        double dashLength = (percent / 100.0) * circumference;
        circle.getStrokeDashArray().setAll(dashLength, circumference);
        circle.setRotate(-90); // Start from top
    }

    private void clearAttendanceAndTargets() {
        if (attendancePercent != null)
            attendancePercent.setText("0%");
        if (attendanceCircle != null)
            attendanceCircle.setVisible(false);
        if (targetCar != null)
            targetCar.setText("0/0");
        if (targetPart != null)
            targetPart.setText("0/0");
        if (targetOverCar != null)
            targetOverCar.setText("+0");
        if (targetOverPart != null)
            targetOverPart.setText("+0");
        if (targetCarMessagelbl != null)
            targetCarMessagelbl.setText("No data");
        if (targetPartMessagelbl != null)
            targetPartMessagelbl.setText("No data");
    }

    // ---------- Load Staff ----------
    private void loadStaffCardsAsync(boolean active) {
        Task<List<user>> task = new Task<>() {
            @Override
            protected List<user> call() throws Exception {
                return dao.getStaffCards(active ? "Active" : "Inactive", roleCombo.getValue());
            }
        };

        task.setOnSucceeded(e -> {
            staffList.setAll(task.getValue());
            populateStaffCards();
        });

        task.setOnFailed(e -> {
            logger.error("Failed to load staff cards", task.getException());
            Platform.runLater(() -> staffListContainer.getChildren().clear());
        });
        ThreadPoolManager.getInstance().execute(task);
    }

    private void populateStaffCards() {
        staffListContainer.getChildren().clear();

        for (user staff : staffList) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/userCards.fxml"));
                Node card = loader.load();
                if (loader.getController() instanceof cardController cc)
                    cc.setData(staff.getId(), staff.getUsername(), staff.getIs_active());

                card.setUserData(staff.getId());
                card.setOnMouseClicked(e -> {
                    showStaffDetails(staff);
                    loadDataForSelectedUser(staff.getId());
                });

                staffListContainer.getChildren().add(card);
            } catch (IOException ex) {
                logger.error("Failed to load staff card for user ID: " + staff.getId(), ex);
            }
        }

        if (!staffList.isEmpty()) {
            showStaffDetails(staffList.get(0));
            loadDataForSelectedUser(staffList.get(0).getId());
        } else {
            selectedStaffId = 0;
            setupEmptyChart();
            clearAttendanceAndTargets();
        }
    }

    private void loadDataForSelectedUser(int userId) {
        String selectedRole = roleCombo.getValue();
        
        if ("Manager".equals(selectedRole)) {
            // For managers, show staff table instead of weekly sales chart
            lineChart.setVisible(false);
            staffTableView.setVisible(true);
            loadStaffUnderManagerAsync(userId);
        } else {
            // For staff, show weekly sales chart
            lineChart.setVisible(true);
            staffTableView.setVisible(false);
            loadWeeklySalesAsync(userId, currentMonth, currentYear);
        }
        
        // Load attendance and targets for both roles
        loadAttendanceAsync(userId, currentMonth, currentYear);
        loadTargetsAsync(userId, currentMonth, currentYear);
    }

    private void showStaffDetails(user staff) {
        if (staff == null)
            return;

        // Close management pane if open and switching to different employee
        if (isManagementPaneOpen && selectedStaffId != staff.getId()) {
            closeManagementPaneWithAnimation();
        }

        StaffNameLabel.setText(Optional.ofNullable(staff.getUsername()).orElse(""));
        StaffPhoneLabel.setText(Optional.ofNullable(staff.getPhone()).orElse(""));
        StaffEmailLabel.setText(Optional.ofNullable(staff.getEmail()).orElse(""));
        StaffAddressLabel.setText(Optional.ofNullable(staff.getAddress()).orElse(""));
        StaffDOBLabel.setText(staff.getDob() != null ?
                staff.getDob().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) : "");
        
        // Show/hide termination reason for inactive employees
        boolean isInactive = "Inactive".equalsIgnoreCase(staff.getIs_active());
        if (terminationReasonBox != null) {
            terminationReasonBox.setVisible(isInactive);
            terminationReasonBox.setManaged(isInactive);
            if (isInactive && StaffReasonLabel != null) {
                StaffReasonLabel.setText(Optional.ofNullable(staff.getReason()).orElse("No reason provided"));
            }
        }
        
        // Hide manage button for inactive employees
        if (manageEmployeeBtn != null) {
            manageEmployeeBtn.setVisible(!isInactive);
            manageEmployeeBtn.setManaged(!isInactive);
        }

        try {
            if (staff.getImagePath() != null && !staff.getImagePath().isBlank()) {
                String path = staff.getImagePath();
                String uri = path.startsWith("http://") || path.startsWith("https://") || path.startsWith("file:")
                        ? path
                        : new File(path).toURI().toString();
                StaffImage.setImage(new Image(uri, true));
            }
        } catch (Exception e) {
            logger.warn("Failed to load image for staff ID: " + staff.getId(), e);
        }

        selectedStaffId = staff.getId();
        highlightCard(staff.getId());
        updateMonthYearOptions(staff);
    }

    private void highlightCard(int id) {
        staffListContainer.getChildren().forEach(node ->
                node.setStyle("-fx-border-color: transparent;"));

        staffListContainer.getChildren().stream()
                .filter(n -> Objects.equals(n.getUserData(), id))
                .findFirst()
                .ifPresent(n -> n.setStyle("-fx-background-color: #e8f0fe; -fx-border-color: #1a73e8; -fx-border-radius: 8;"));
    }

    // ---------- Search ----------
    private void setupSearch() {
        StaffSearchText.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER)
                searchStaff();
        });
        SearchNamebtn.setOnMouseClicked(e -> searchStaff());

        StaffSearchText.textProperty().addListener((obs, o, text) -> {
            if (text == null || text.isEmpty()) {
                searchMenu.hide();
                return;
            }

            List<MenuItem> matches = new ArrayList<>();
            String lower = text.toLowerCase();

            for (user s : staffList) {
                if (String.valueOf(s.getId()).contains(lower) || s.getUsername().toLowerCase().contains(lower)) {
                    MenuItem item = new MenuItem(s.getId() + " - " + s.getUsername());
                    item.setOnAction(ev -> {
                        StaffSearchText.setText(s.getUsername());
                        searchMenu.hide();
                        showStaffDetails(s);
                        loadDataForSelectedUser(s.getId());
                    });
                    matches.add(item);
                }
            }

            if (!matches.isEmpty()) {
                searchMenu.getItems().setAll(matches);
                searchMenu.show(StaffSearchText, Side.BOTTOM, 0, 0);
            } else
                searchMenu.hide();
        });

        StaffSearchText.focusedProperty().addListener((obs, o, focus) -> {
            if (!focus)
                searchMenu.hide();
        });
    }

    private void searchStaff() {
        String text = StaffSearchText.getText().trim();
        if (text.isEmpty())
            return;

        staffList.stream()
                .filter(s -> s.getUsername().equalsIgnoreCase(text) || String.valueOf(s.getId()).equals(text))
                .findFirst()
                .ifPresent(s -> {
                    showStaffDetails(s);
                    loadDataForSelectedUser(s.getId());
                });
    }

    // ---------- Month/Year ----------
    private boolean updatingDateBox = false;

    private void setupMonthYear() {
        monthBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (updatingDateBox || newVal == null)
                return;
            int selectedMonth = Month.valueOf(newVal.toUpperCase(Locale.ENGLISH)).getValue();
            if (selectedMonth != currentMonth) {
                currentMonth = selectedMonth;
                updateDateControls();
                loadDataForSelectedUser(selectedStaffId);
            }
        });

        yearBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (updatingDateBox || newVal == null)
                return;
            if (newVal != currentYear) {
                currentYear = newVal;
                updateMonthBox();
                updateDateControls();
                loadDataForSelectedUser(selectedStaffId);
            }
        });
    }

    private void updateMonthYearOptions(user staff) {
        updatingDateBox = true;

        currentStaffStartDate = Optional.ofNullable(staff.getStart_date()).orElse(today);
        currentStaffEndDate = Optional.ofNullable(staff.getEnd_date()).orElse(today);

        yearBox.getItems().clear();
        for (int y = currentStaffStartDate.getYear(); y <= currentStaffEndDate.getYear(); y++)
            yearBox.getItems().add(y);
        if (!yearBox.getItems().contains(currentYear))
            currentYear = currentStaffStartDate.getYear();
        yearBox.setValue(currentYear);

        updateMonthBox();
        updateDateControls();
        updatingDateBox = false;
    }

    private void updateMonthBox() {
        updatingDateBox = true;

        List<String> months = new ArrayList<>();

        // Limit months based on staff start/end dates for the current year
        int startMonth = (currentYear == currentStaffStartDate.getYear()) ? currentStaffStartDate.getMonthValue() : 1;
        int endMonth = (currentYear == currentStaffEndDate.getYear()) ? currentStaffEndDate.getMonthValue() : 12;

        for (int i = startMonth; i <= endMonth; i++)
            months.add(Month.of(i).getDisplayName(TextStyle.SHORT, Locale.ENGLISH));

        monthBox.setItems(FXCollections.observableArrayList(months));
        
        // Ensure current month is within valid range
        if (currentMonth < startMonth || currentMonth > endMonth)
            currentMonth = startMonth;
            
        monthBox.setValue(Month.of(currentMonth).getDisplayName(TextStyle.SHORT, Locale.ENGLISH));

        updatingDateBox = false;
    }

    private void updateDateControls() {
        if (NextMonthbtn == null || NextYearbtn == null || PreviousMonthbtn == null || PreviousYearbtn == null)
            return;
        
        // Use end date for boundary checking (for inactive employees, this is their termination date)
        LocalDate endBoundary = (currentStaffEndDate != null) ? currentStaffEndDate : today;
        
        // Disable next buttons if at end boundary month/year
        boolean isEndYear = currentYear == endBoundary.getYear();
        boolean isEndMonth = isEndYear && currentMonth == endBoundary.getMonthValue();
        NextMonthbtn.setDisable(isEndMonth);
        NextYearbtn.setDisable(isEndYear);
        
        // Hide next buttons if at end boundary month/year
        NextMonthbtn.setVisible(!isEndMonth);
        NextYearbtn.setVisible(!isEndYear);
        
        // Disable/hide previous buttons if at staff start date
        if (currentStaffStartDate != null) {
            boolean isStartYear = currentYear == currentStaffStartDate.getYear();
            boolean isStartMonth = isStartYear && currentMonth == currentStaffStartDate.getMonthValue();
            
            PreviousMonthbtn.setDisable(isStartMonth);
            PreviousYearbtn.setDisable(isStartYear);
            PreviousMonthbtn.setVisible(!isStartMonth);
            PreviousYearbtn.setVisible(!isStartYear);
        } else {
            PreviousMonthbtn.setDisable(false);
            PreviousYearbtn.setDisable(false);
            PreviousMonthbtn.setVisible(true);
            PreviousYearbtn.setVisible(true);
        }
    }

    @FXML
    private void nextMonthClick(MouseEvent e) {
        if (currentMonth == 12) {
            currentMonth = 1;
            currentYear++;
            yearBox.setValue(currentYear);
        } else
            currentMonth++;
        updateMonthBox();
        updateDateControls();
        loadDataForSelectedUser(selectedStaffId);
    }

    @FXML
    private void prevMonthClick(MouseEvent e) {
        if (currentMonth == 1) {
            currentMonth = 12;
            currentYear--;
            yearBox.setValue(currentYear);
        } else
            currentMonth--;
        updateMonthBox();
        updateDateControls();
        loadDataForSelectedUser(selectedStaffId);
    }

    @FXML
    private void nextYearClick(MouseEvent e) {
        currentYear++;
        yearBox.setValue(currentYear);
        updateMonthBox();
        updateDateControls();
        loadDataForSelectedUser(selectedStaffId);
    }

    @FXML
    private void prevYearClick(MouseEvent e) {
        currentYear--;
        yearBox.setValue(currentYear);
        updateMonthBox();
        updateDateControls();
        loadDataForSelectedUser(selectedStaffId);
    }

    boolean cardtype = true;

    @FXML
    void SwitchMouseClick(MouseEvent event) {
        cardtype = !cardtype;
        StaffListTitleLabel.setText(cardtype ? "List (Active)" : "List (InActive)");
        loadStaffCardsAsync(cardtype);
    }

    // ---------- Management Pane Methods ----------
    @FXML
    private void onManageEmployeeClick() {
        if (selectedStaffId == 0) {
            showAlert(Alert.AlertType.WARNING, "No Employee Selected", "Please select an employee to manage.");
            return;
        }
        
        // Toggle the pane
        if (isManagementPaneOpen) {
            closeManagementPaneWithAnimation();
        } else {
            openManagementPaneWithAnimation();
            loadManagementData();
        }
    }

    @FXML
    private void onCloseManagementPane() {
        closeManagementPaneWithAnimation();
    }

    private void openManagementPaneWithAnimation() {
        isManagementPaneOpen = true;
        
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), managementPane);
        slideIn.setFromX(450);  // Start off-screen to the right
        slideIn.setToX(0);      // End at normal position
        slideIn.play();
    }

    private void closeManagementPaneWithAnimation() {
        if (!isManagementPaneOpen) return;
        
        isManagementPaneOpen = false;
        terminationReasonField.clear();
        
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), managementPane);
        slideOut.setFromX(0);    // Start at normal position
        slideOut.setToX(450);    // End off-screen to the right
        slideOut.play();
    }

    private void loadManagementData() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Load current salary
                double salary = dao.getCurrentSalary(selectedStaffId);
                
                // Get target data for bonus calculation
                int[][] targetData = dao.getTargetData(selectedStaffId, currentMonth, currentYear);
                int carAchieved = targetData[0][0];
                int carTarget = targetData[0][1];
                int partAchieved = targetData[1][0];
                int partTarget = targetData[1][1];
                
                Platform.runLater(() -> {
                    // Update salary field
                    salaryField.setText(String.format("%.2f", salary));
                    
                    // Calculate and display bonus
                    calculateAndDisplayBonus(carAchieved, carTarget, partAchieved, partTarget, salary);
                });
                
                return null;
            }
        };
        
        task.setOnFailed(e -> {
            logger.error("Failed to load management data", task.getException());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load employee data.");
        });
        
        ThreadPoolManager.getInstance().execute(task);
    }

    private void calculateAndDisplayBonus(int carAchieved, int carTarget, int partAchieved, int partTarget, double salary) {
        // Update target labels
        carTargetLabel.setText(carAchieved + "/" + carTarget);
        partTargetLabel.setText(partAchieved + "/" + partTarget);
        
        // Calculate car bonus (2% per car over target)
        int carsOverTarget = Math.max(0, carAchieved - carTarget);
        double carBonusPercent = carsOverTarget * 2.0;
        double carBonusAmount = (salary * carBonusPercent) / 100.0;
        
        // Calculate part bonus (1% per part over target)
        int partsOverTarget = Math.max(0, partAchieved - partTarget);
        double partBonusPercent = partsOverTarget * 1.0;
        double partBonusAmount = (salary * partBonusPercent) / 100.0;
        
        // Total bonus
        double totalBonus = carBonusAmount + partBonusAmount;
        
        // Update UI
        carBonusLabel.setText(String.format("+%.1f%%", carBonusPercent));
        carBonusLabel.setStyle(carBonusPercent > 0 ? "-fx-text-fill: #10b981; -fx-font-weight: bold;" : "-fx-text-fill: #64748b; -fx-font-weight: bold;");
        
        partBonusLabel.setText(String.format("+%.1f%%", partBonusPercent));
        partBonusLabel.setStyle(partBonusPercent > 0 ? "-fx-text-fill: #10b981; -fx-font-weight: bold;" : "-fx-text-fill: #64748b; -fx-font-weight: bold;");
        
        totalBonusLabel.setText(String.format("$ %.2f", totalBonus));
        
        // Breakdown text
        if (totalBonus > 0) {
            StringBuilder breakdown = new StringBuilder();
            if (carsOverTarget > 0) {
                breakdown.append(String.format("Cars: %d over target × 2%% = $ %.2f", carsOverTarget, carBonusAmount));
            }
            if (partsOverTarget > 0) {
                if (breakdown.length() > 0) breakdown.append("\n");
                breakdown.append(String.format("Parts: %d over target × 1%% = $ %.2f", partsOverTarget, partBonusAmount));
            }
            bonusBreakdownLabel.setText(breakdown.toString());
        } else {
            bonusBreakdownLabel.setText("No bonus earned this month. Target not exceeded.");
        }
    }

    @FXML
    private void onUpdateSalary() {
        if (selectedStaffId == 0) {
            showAlert(Alert.AlertType.WARNING, "No Employee Selected", "Please select an employee first.");
            return;
        }
        
        try {
            double newSalary = Double.parseDouble(salaryField.getText().trim());
            
            if (newSalary <= 0) {
                showAlert(Alert.AlertType.WARNING, "Invalid Salary", "Salary must be greater than 0.");
                return;
            }
            
            // Confirm update
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Salary Update");
            confirm.setHeaderText("Update Employee Salary");
            confirm.setContentText(String.format("Are you sure you want to update the salary to $ %.2f?", newSalary));
            
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    updateSalaryAsync(newSalary);
                }
            });
            
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid salary amount.");
        }
    }

    private void updateSalaryAsync(double newSalary) {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return dao.updateSalary(selectedStaffId, newSalary);
            }
        };
        
        task.setOnSucceeded(e -> {
            if (task.getValue()) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Salary updated successfully!");
                loadManagementData(); // Refresh bonus calculation
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update salary.");
            }
        });
        
        task.setOnFailed(e -> {
            logger.error("Failed to update salary", task.getException());
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while updating salary.");
        });
        
        ThreadPoolManager.getInstance().execute(task);
    }

    @FXML
    private void onApplyBonus() {
        if (selectedStaffId == 0) {
            showAlert(Alert.AlertType.WARNING, "No Employee Selected", "Please select an employee first.");
            return;
        }
        
        try {
            String bonusText = totalBonusLabel.getText().replace("$", "").trim();
            double bonusAmount = Double.parseDouble(bonusText);
            
            if (bonusAmount <= 0) {
                showAlert(Alert.AlertType.WARNING, "No Bonus", "There is no bonus to apply for this employee.");
                return;
            }
            
            // Confirm apply
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Bonus Application");
            confirm.setHeaderText("Apply Employee Bonus");
            confirm.setContentText(String.format("Are you sure you want to apply a bonus of $ %.2f?", bonusAmount));
            
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    applyBonusAsync(bonusAmount);
                }
            });
            
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Bonus", "Unable to parse bonus amount.");
        }
    }

    private void applyBonusAsync(double bonusAmount) {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return dao.applyBonus(selectedStaffId, bonusAmount);
            }
        };
        
        task.setOnSucceeded(e -> {
            if (task.getValue()) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Bonus applied successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to apply bonus.");
            }
        });
        
        task.setOnFailed(e -> {
            logger.error("Failed to apply bonus", task.getException());
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while applying bonus.");
        });
        
        ThreadPoolManager.getInstance().execute(task);
    }

    @FXML
    private void onTerminateEmployee() {
        if (selectedStaffId == 0) {
            showAlert(Alert.AlertType.WARNING, "No Employee Selected", "Please select an employee first.");
            return;
        }
        
        String reason = terminationReasonField.getText().trim();
        
        if (reason.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Reason Required", "Please provide a reason for termination.");
            return;
        }
        
        // Confirm termination
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Employee Termination");
        confirm.setHeaderText("⚠ WARNING: This action cannot be undone!");
        confirm.setContentText("Are you sure you want to terminate this employee?\n\nReason: " + reason);
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                terminateEmployeeAsync(reason);
            }
        });
    }

    private void terminateEmployeeAsync(String reason) {
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return dao.terminateEmployee(selectedStaffId, reason);
            }
        };
        
        task.setOnSucceeded(e -> {
            if (task.getValue()) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Employee has been terminated successfully.");
                closeManagementPaneWithAnimation();
                // Reload staff list
                loadStaffCardsAsync(showActive);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to terminate employee.");
            }
        });
        
        task.setOnFailed(e -> {
            logger.error("Failed to terminate employee", task.getException());
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred while terminating employee.");
        });
        
        ThreadPoolManager.getInstance().execute(task);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.show();
        });
    }

}
