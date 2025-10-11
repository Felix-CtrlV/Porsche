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
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
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
    private Label StaffListTitleLabel, StaffAddressLabel, StaffDOBLabel, StaffEmailLabel, StaffNameLabel, StaffPhoneLabel;
    @FXML
    private ImageView StaffImage;
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

        // Setup search, month/year, chart
        setupSearch();
        setupMonthYear();
        setupEmptyChart();

        StaffListTitleLabel.setText("List (Active)");
        loadStaffCardsAsync(showActive);
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
                    loadWeeklySalesAsync(staff.getId(), currentMonth, currentYear);
                    loadAttendanceAsync(staff.getId(), currentMonth, currentYear);
                    loadTargetsAsync(staff.getId(), currentMonth, currentYear);
                });

                staffListContainer.getChildren().add(card);
            } catch (IOException ex) {
                logger.error("Failed to load staff card for user ID: " + staff.getId(), ex);
            }
        }

        if (!staffList.isEmpty()) {
            showStaffDetails(staffList.get(0));
            loadWeeklySalesAsync(staffList.get(0).getId(), currentMonth, currentYear);
            loadAttendanceAsync(staffList.get(0).getId(), currentMonth, currentYear);
            loadTargetsAsync(staffList.get(0).getId(), currentMonth, currentYear);
        } else {
            selectedStaffId = 0;
            setupEmptyChart();
            clearAttendanceAndTargets();
        }
    }

    private void showStaffDetails(user staff) {
        if (staff == null)
            return;

        StaffNameLabel.setText(Optional.ofNullable(staff.getUsername()).orElse(""));
        StaffPhoneLabel.setText(Optional.ofNullable(staff.getPhone()).orElse(""));
        StaffEmailLabel.setText(Optional.ofNullable(staff.getEmail()).orElse(""));
        StaffAddressLabel.setText(Optional.ofNullable(staff.getAddress()).orElse(""));
        StaffDOBLabel.setText(staff.getDob() != null ?
                staff.getDob().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) : "");

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
                        loadWeeklySalesAsync(s.getId(), currentMonth, currentYear);
                        loadAttendanceAsync(s.getId(), currentMonth, currentYear);
                        loadTargetsAsync(s.getId(), currentMonth, currentYear);
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
                    loadWeeklySalesAsync(s.getId(), currentMonth, currentYear);
                    loadAttendanceAsync(s.getId(), currentMonth, currentYear);
                    loadTargetsAsync(s.getId(), currentMonth, currentYear);
                });
    }

    // ---------- Month/Year ----------
    private boolean updatingDateBox = false;

    private void setupMonthYear() {
        monthBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (updatingDateBox || newVal == null)
                return;
            // Parse short month name (Jan, Feb, etc.) to month number
            int selectedMonth = -1;
            for (int i = 1; i <= 12; i++) {
                if (Month.of(i).getDisplayName(TextStyle.SHORT, Locale.ENGLISH).equals(newVal)) {
                    selectedMonth = i;
                    break;
                }
            }
            if (selectedMonth != -1 && selectedMonth != currentMonth) {
                currentMonth = selectedMonth;
                updateDateControls();
                loadWeeklySalesAsync(selectedStaffId, currentMonth, currentYear);
                loadAttendanceAsync(selectedStaffId, currentMonth, currentYear);
                loadTargetsAsync(selectedStaffId, currentMonth, currentYear);
            }
        });

        yearBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (updatingDateBox || newVal == null)
                return;
            if (newVal != currentYear) {
                currentYear = newVal;
                updateMonthBox();
                updateDateControls();
                loadWeeklySalesAsync(selectedStaffId, currentMonth, currentYear);
                loadAttendanceAsync(selectedStaffId, currentMonth, currentYear);
                loadTargetsAsync(selectedStaffId, currentMonth, currentYear);
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

        // Limit months to current month if viewing current year
        int maxMonth = (currentYear == today.getYear()) ? today.getMonthValue() : 12;
        
        for (int i = 1; i <= maxMonth; i++)
            months.add(Month.of(i).getDisplayName(TextStyle.SHORT, Locale.ENGLISH));

        monthBox.setItems(FXCollections.observableArrayList(months));
        
        // If current month exceeds max available, reset to max
        if (currentMonth > maxMonth)
            currentMonth = maxMonth;
        if (currentMonth < 1)
            currentMonth = 1;
            
        monthBox.setValue(Month.of(currentMonth).getDisplayName(TextStyle.SHORT, Locale.ENGLISH));

        updatingDateBox = false;
    }

    private void updateDateControls() {
        if (NextMonthbtn == null || NextYearbtn == null || PreviousMonthbtn == null || PreviousYearbtn == null)
            return;
        
        // Disable next buttons if at current month/year
        boolean isCurrentYear = currentYear == today.getYear();
        boolean isCurrentMonth = isCurrentYear && currentMonth == today.getMonthValue();
        NextMonthbtn.setDisable(isCurrentMonth);
        NextYearbtn.setDisable(isCurrentYear);
        
        // Hide next buttons if at current month/year
        NextMonthbtn.setVisible(!isCurrentMonth);
        NextYearbtn.setVisible(!isCurrentYear);
        
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
        loadWeeklySalesAsync(selectedStaffId, currentMonth, currentYear);
        loadAttendanceAsync(selectedStaffId, currentMonth, currentYear);
        loadTargetsAsync(selectedStaffId, currentMonth, currentYear);
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
        loadWeeklySalesAsync(selectedStaffId, currentMonth, currentYear);
        loadAttendanceAsync(selectedStaffId, currentMonth, currentYear);
        loadTargetsAsync(selectedStaffId, currentMonth, currentYear);
    }

    @FXML
    private void nextYearClick(MouseEvent e) {
        currentYear++;
        yearBox.setValue(currentYear);
        updateMonthBox();
        updateDateControls();
        loadWeeklySalesAsync(selectedStaffId, currentMonth, currentYear);
        loadAttendanceAsync(selectedStaffId, currentMonth, currentYear);
        loadTargetsAsync(selectedStaffId, currentMonth, currentYear);
    }

    @FXML
    private void prevYearClick(MouseEvent e) {
        currentYear--;
        yearBox.setValue(currentYear);
        updateMonthBox();
        updateDateControls();
        loadWeeklySalesAsync(selectedStaffId, currentMonth, currentYear);
        loadAttendanceAsync(selectedStaffId, currentMonth, currentYear);
        loadTargetsAsync(selectedStaffId, currentMonth, currentYear);
    }

    boolean cardtype = true;

    @FXML
    void SwitchMouseClick(MouseEvent event) {
        cardtype = !cardtype;
        StaffListTitleLabel.setText(cardtype ? "List (Active)" : "List (InActive)");
        loadStaffCardsAsync(cardtype);
    }

}
