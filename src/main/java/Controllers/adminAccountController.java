package Controllers;

import DAO.AdminAccountDAO;
import Model.user;
import Utils.Session;
import Utils.ThreadPoolManager;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
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
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class adminAccountController {
    private static final Logger logger = LoggerFactory.getLogger(adminAccountController.class);
    private static final DateTimeFormatter SHORT_MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);

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
    @FXML
    private Label performanceLabel;

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
    private final FilteredList<user> filteredStaff = new FilteredList<>(staffList, s -> true);
    private final PauseTransition searchDebounce = new PauseTransition(Duration.millis(500));
    private String pendingSearchText = "";
    private int selectedStaffId = 0;
    private final ContextMenu searchMenu = new ContextMenu();
    private final LocalDate today = LocalDate.now();
    private int currentMonth, currentYear;
    private LocalDate currentStaffStartDate;
    private LocalDate currentStaffEndDate;
    private boolean isManagementPaneOpen = false;
    private adminDashboardController dashboardController;

    public void setDashboardController(adminDashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    public adminAccountController() {
        try {
            dao = new AdminAccountDAO();
        } catch (
                Exception e) {
            logger.error("Failed to initialize AdminAccountDAO", e);
            throw new RuntimeException("Failed to initialize controller", e);
        }
    }

    @FXML
    public void initialize() {
        currentMonth = today.getMonthValue();
        currentYear = today.getYear();

        if (StaffImage != null) {
            applyCircularClip(StaffImage);
        }

        // Initialize role combo
        roleCombo.setItems(FXCollections.observableArrayList("Manager", "Staff"));
        roleCombo.setValue("Manager");
        updatePerformanceLabel("Manager");
        roleCombo.valueProperty().addListener((obs, o, n) -> {
            updatePerformanceLabel(n);
            loadStaffCardsAsync(showActive);
        });

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

    private void updatePerformanceLabel(String role) {
        if (performanceLabel == null)
            return;

        if ("Manager".equals(role)) {
            performanceLabel.setText("👥 Members");
        } else {
            performanceLabel.setText("📈 Sale Performance");
        }
    }

    private void applyCircularClip(ImageView imageView) {
        if (imageView == null) {
            return;
        }

        Circle clip;
        if (imageView.getClip() instanceof Circle existing) {
            clip = existing;
        } else {
            clip = new Circle();
            imageView.setClip(clip);
        }

        clip.radiusProperty().bind(Bindings.min(imageView.fitWidthProperty(), imageView.fitHeightProperty()).divide(2));
        clip.centerXProperty().bind(imageView.fitWidthProperty().divide(2));
        clip.centerYProperty().bind(imageView.fitHeightProperty().divide(2));
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
        } catch (
                IOException e) {
            logger.error("Failed to open user registration window", e);
            showToast("error", "Error", "Failed to open registration window");
        }
    }

    // ---------- Chart ----------
    private void setupEmptyChart() {
        if (lineChart == null)
            return;

        // Enable smooth curves for the chart
        lineChart.setCreateSymbols(true);
        lineChart.setLegendVisible(false);
        lineChart.setAnimated(false); // Disable animation for immediate rendering

        lineChart.getData().clear();
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        s.setName("Weekly Sales");
        for (int i = 1; i <= 4; i++)
            s.getData().add(new XYChart.Data<>("Week " + i, 0));
        lineChart.getData().add(s);

        // Hide chart temporarily to prevent showing sharp lines
        lineChart.setOpacity(0);

        // Apply smooth curves after a short delay to ensure paths are created
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(100));
        pause.setOnFinished(e -> applySmoothCurves());
        pause.play();
    }

    private void applySmoothCurves() {
        if (lineChart == null)
            return;

        // Check if paths are ready, if not, retry after a short delay
        var linePaths = lineChart.lookupAll(".chart-series-area-line");
        var fillPaths = lineChart.lookupAll(".chart-series-area-fill");

        if (linePaths.isEmpty() || fillPaths.isEmpty()) {
            // Paths not ready yet, try again after 50ms
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(50));
            pause.setOnFinished(e -> applySmoothCurves());
            pause.play();
            return;
        }

        // Store the smoothed line elements to copy to fill area
        java.util.List<javafx.scene.shape.PathElement> smoothedLineElements = new java.util.ArrayList<>();

        // Smooth the line first and store the elements
        linePaths.forEach(node -> {
            if (node instanceof javafx.scene.shape.Path) {
                javafx.scene.shape.Path path = (javafx.scene.shape.Path) node;
                smoothPath(path, false);
                // Copy the smoothed elements
                smoothedLineElements.addAll(new java.util.ArrayList<>(path.getElements()));
            }
        });

        // Apply the same smooth curve to the fill area
        fillPaths.forEach(node -> {
            if (node instanceof javafx.scene.shape.Path && !smoothedLineElements.isEmpty()) {
                javafx.scene.shape.Path fillPath = (javafx.scene.shape.Path) node;
                var originalFillElements = new java.util.ArrayList<>(fillPath.getElements());

                // Find the baseline (bottom of chart) from original fill path
                double baselineY = 0;
                double startX = 0;
                double endX = 0;

                for (var element : originalFillElements) {
                    if (element instanceof javafx.scene.shape.LineTo) {
                        javafx.scene.shape.LineTo lt = (javafx.scene.shape.LineTo) element;
                        baselineY = Math.max(baselineY, lt.getY());
                    }
                }

                // Get start and end X coordinates from smoothed line
                if (smoothedLineElements.get(0) instanceof javafx.scene.shape.MoveTo) {
                    javafx.scene.shape.MoveTo firstMove = (javafx.scene.shape.MoveTo) smoothedLineElements.get(0);
                    startX = firstMove.getX();
                }

                var lastElement = smoothedLineElements.get(smoothedLineElements.size() - 1);
                if (lastElement instanceof javafx.scene.shape.CubicCurveTo) {
                    javafx.scene.shape.CubicCurveTo lastCurve = (javafx.scene.shape.CubicCurveTo) lastElement;
                    endX = lastCurve.getX();
                }

                // Clear and rebuild: smooth line + close to baseline
                fillPath.getElements().clear();

                // Add the smoothed line elements
                for (var element : smoothedLineElements) {
                    if (element instanceof javafx.scene.shape.MoveTo) {
                        javafx.scene.shape.MoveTo mt = (javafx.scene.shape.MoveTo) element;
                        fillPath.getElements().add(new javafx.scene.shape.MoveTo(mt.getX(), mt.getY()));
                    } else if (element instanceof javafx.scene.shape.CubicCurveTo) {
                        javafx.scene.shape.CubicCurveTo cc = (javafx.scene.shape.CubicCurveTo) element;
                        fillPath.getElements().add(new javafx.scene.shape.CubicCurveTo(
                                cc.getControlX1(), cc.getControlY1(),
                                cc.getControlX2(), cc.getControlY2(),
                                cc.getX(), cc.getY()
                        ));
                    }
                }

                // Close the path to baseline
                fillPath.getElements().add(new javafx.scene.shape.LineTo(endX, baselineY));
                fillPath.getElements().add(new javafx.scene.shape.LineTo(startX, baselineY));
                fillPath.getElements().add(new javafx.scene.shape.ClosePath());
            }
        });

        // Show chart after smoothing is complete
        lineChart.setOpacity(1);
    }

    private void smoothPath(javafx.scene.shape.Path path, boolean isFillArea) {
        var elements = path.getElements();
        if (elements.size() < 3)
            return;

        // Extract points from path
        java.util.List<Double> xPoints = new java.util.ArrayList<>();
        java.util.List<Double> yPoints = new java.util.ArrayList<>();

        for (var element : elements) {
            if (element instanceof javafx.scene.shape.MoveTo) {
                javafx.scene.shape.MoveTo moveTo = (javafx.scene.shape.MoveTo) element;
                xPoints.add(moveTo.getX());
                yPoints.add(moveTo.getY());
            } else if (element instanceof javafx.scene.shape.LineTo) {
                javafx.scene.shape.LineTo lineTo = (javafx.scene.shape.LineTo) element;
                xPoints.add(lineTo.getX());
                yPoints.add(lineTo.getY());
            }
        }

        if (xPoints.size() < 3)
            return;

        // Find the baseline (bottom of chart) for clamping
        double baseline = yPoints.stream().mapToDouble(Double::doubleValue).max().orElse(0);

        // Clear existing elements and rebuild with smooth curves
        elements.clear();

        // Start at first point
        elements.add(new javafx.scene.shape.MoveTo(xPoints.get(0), yPoints.get(0)));

        // Create smooth Catmull-Rom spline curves between points
        for (int i = 0; i < xPoints.size() - 1; i++) {
            double x0 = i > 0 ? xPoints.get(i - 1) : xPoints.get(i);
            double y0 = i > 0 ? yPoints.get(i - 1) : yPoints.get(i);
            double x1 = xPoints.get(i);
            double y1 = yPoints.get(i);
            double x2 = xPoints.get(i + 1);
            double y2 = yPoints.get(i + 1);
            double x3 = i < xPoints.size() - 2 ? xPoints.get(i + 2) : x2;
            double y3 = i < xPoints.size() - 2 ? yPoints.get(i + 2) : y2;

            // Calculate control points for cubic Bezier curve
            double cp1x = x1 + (x2 - x0) / 6.0;
            double cp1y = y1 + (y2 - y0) / 6.0;
            double cp2x = x2 - (x3 - x1) / 6.0;
            double cp2y = y2 - (y3 - y1) / 6.0;

            // Strict clamping to prevent overshooting
            // Control points must stay between the two data points
            double minY = Math.min(y1, y2);
            double maxY = Math.max(y1, y2);

            // Clamp control points strictly within the range of the two endpoints
            cp1y = Math.max(minY, Math.min(cp1y, maxY));
            cp2y = Math.max(minY, Math.min(cp2y, maxY));

            // Also ensure they don't go below baseline
            cp1y = Math.min(cp1y, baseline);
            cp2y = Math.min(cp2y, baseline);

            elements.add(new javafx.scene.shape.CubicCurveTo(cp1x, cp1y, cp2x, cp2y, x2, y2));
        }
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

        // Hide chart temporarily to prevent showing sharp lines
        lineChart.setOpacity(0);

        // Apply smooth curves after a short delay to ensure paths are created
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(100));
        pause.setOnFinished(e -> applySmoothCurves());
        pause.play();
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

        Task<AdminAccountDAO.AttendanceSummary> task = new Task<>() {
            @Override
            protected AdminAccountDAO.AttendanceSummary call() throws Exception {
                return dao.getMonthlyAttendance(getCurrentUserId(), month, year);
            }
        };

        task.setOnSucceeded(e -> {
            AdminAccountDAO.AttendanceSummary summary = task.getValue();
            AdminAccountDAO.AttendanceRecord record = summary != null ? summary.findByUserId(staffId) : null;
            updateAttendance(record);
        });
        task.setOnFailed(e -> {
            logger.error("Failed to load attendance", task.getException());
            Platform.runLater(() -> updateAttendance(null));
        });
        ThreadPoolManager.getInstance().execute(task);
    }

    private void updateAttendance(AdminAccountDAO.AttendanceRecord record) {
        if (attendancePercent == null || attendanceCircle == null)
            return;

        double percent = record != null ? record.getAttendancePercentage() : 0.0;
        attendancePercent.setText(String.format("%.0f%%", percent));
        updateCircleProgress(attendanceCircle, percent);
    }

    private int getCurrentUserId() {
        Session current = Session.getInstance();
        if (current != null)
            return current.getUserid();
        return 0;
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
        targetCar.setText(Math.min(carAchieved, carTarget) + "/" + carTarget);

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
        targetPart.setText(Math.min(partAchieved, partTarget) + "/" + partTarget);

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
        double clampedPercent = Math.max(0, Math.min(percent, 100));

        if (!circle.getStyle().contains("-fx-stroke-line-cap")) {
            circle.setStyle(circle.getStyle() + "; -fx-stroke-line-cap: round;");
        }

        circle.getStrokeDashArray().clear();
        circle.getStrokeDashArray().addAll(circumference, circumference);

        Double previousOffset = (Double) circle.getProperties().get("progressPrevOffset");
        if (previousOffset == null) {
            previousOffset = circumference;
        }

        double targetOffset = circumference - (clampedPercent / 100.0) * circumference;

        Timeline existing = (Timeline) circle.getProperties().get("progressTimeline");
        if (existing != null) {
            existing.stop();
        }

        circle.setStrokeDashOffset(previousOffset);
        circle.setRotate(-90); // Start from top
        circle.setVisible(true);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(circle.strokeDashOffsetProperty(), previousOffset)),
                new KeyFrame(Duration.seconds(1.2), new KeyValue(circle.strokeDashOffsetProperty(), targetOffset, Interpolator.EASE_OUT))
        );

        timeline.setOnFinished(e -> circle.getProperties().put("progressPrevOffset", targetOffset));
        circle.getProperties().put("progressTimeline", timeline);
        timeline.play();
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
            applyStaffFilter(StaffSearchText != null ? StaffSearchText.getText() : "");
        });

        task.setOnFailed(e -> {
            logger.error("Failed to load staff cards", task.getException());
            Platform.runLater(() -> staffListContainer.getChildren().clear());
        });
        ThreadPoolManager.getInstance().execute(task);
    }

    private void populateStaffCards() {
        staffListContainer.getChildren().clear();

        for (user staff : filteredStaff) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/userCards.fxml"));
                Node card = loader.load();
                if (loader.getController() instanceof cardController cc)
                    cc.setData(staff.getId(), staff.getUsername(), staff.getIs_active(), staff.getImagePath());

                card.setUserData(staff.getId());
                card.setOnMouseClicked(e -> {
                    showStaffDetails(staff);
                    loadDataForSelectedUser(staff.getId());
                });

                staffListContainer.getChildren().add(card);
            } catch (
                    IOException ex) {
                logger.error("Failed to load staff card for user ID: " + staff.getId(), ex);
            }
        }

        if (!filteredStaff.isEmpty()) {
            user currentSelection = filteredStaff.stream()
                    .filter(s -> s.getId() == selectedStaffId)
                    .findFirst()
                    .orElse(filteredStaff.get(0));
            showStaffDetails(currentSelection);
            loadDataForSelectedUser(currentSelection.getId());
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
            updatePerformanceLabel("Manager");
            loadStaffUnderManagerAsync(userId);
        } else {
            // For staff, show weekly sales chart
            lineChart.setVisible(true);
            staffTableView.setVisible(false);
            updatePerformanceLabel("Staff");
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
                applyCircularClip(StaffImage);
            } else {
                StaffImage.setImage(new Image(getClass().getResource("/Image/defaultUserProfile.jpg").toExternalForm(), true));
                applyCircularClip(StaffImage);
            }
        } catch (
                Exception e) {
            logger.warn("Failed to load image for staff ID: " + staff.getId(), e);
            try {
                StaffImage.setImage(new Image(getClass().getResource("/Image/defaultUserProfile.jpg").toExternalForm(), true));
                applyCircularClip(StaffImage);
            } catch (Exception ignored) {
            }
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
            if (e.getCode() == KeyCode.ENTER) {
                searchDebounce.stop();
                applyStaffFilter(StaffSearchText.getText());
                searchStaff();
            }
        });
        SearchNamebtn.setOnMouseClicked(e -> searchStaff());

        StaffSearchText.textProperty().addListener((obs, o, text) -> {
            pendingSearchText = text;
            searchDebounce.playFromStart();
            if (searchMenu.isShowing())
                searchMenu.hide();
        });

        searchDebounce.setOnFinished(ev -> applyStaffFilter(pendingSearchText));

        StaffSearchText.focusedProperty().addListener((obs, o, focus) -> {
            if (!focus)
                searchMenu.hide();
        });
    }

    private void applyStaffFilter(String text) {
        String filter = text == null ? "" : text.trim().toLowerCase();

        if (filter.isEmpty()) {
            filteredStaff.setPredicate(s -> true);
        } else {
            filteredStaff.setPredicate(s -> {
                if (s == null)
                    return false;
                String name = Optional.ofNullable(s.getUsername()).orElse("").toLowerCase();
                String idString = String.valueOf(s.getId());
                return name.contains(filter) || idString.contains(filter);
            });
        }

        populateStaffCards();
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
            int selectedMonth;
            try {
                selectedMonth = Month.from(SHORT_MONTH_FORMATTER.parse(newVal)).getValue();
            } catch (DateTimeParseException ex) {
                logger.warn("Failed to parse month value: {}", newVal, ex);
                return;
            }
            if (selectedMonth != currentMonth) {
                currentMonth = selectedMonth;
                updateDateControls();
                if (selectedStaffId != 0)
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
            showToast("Error", "No Employee Selected", "Please select an employee to manage.");
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
        if (!isManagementPaneOpen)
            return;

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
            showToast("error", "Error", "Failed to load employee data.");
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
                if (breakdown.length() > 0)
                    breakdown.append("\n");
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
            showToast("warning", "No Employee Selected", "Please select an employee first.");
            return;
        }

        try {
            double newSalary = Double.parseDouble(salaryField.getText().trim());

            if (newSalary <= 0) {
                showToast("error", "Invalid Salary", "Salary must be greater than 0.");
                return;
            }

            if (dashboardController != null) {
                dashboardController.showConfirmDialog("Confirm Salary Update", String.format("Are you sure you want to update the salary to $ %.2f?", newSalary), "⚠", () -> updateSalaryAsync(newSalary)
                );
            } else {
                showToast("info", "Confirmation Needed", "Unable to show confirmation dialog. Please retry from dashboard view.");
            }

        } catch (
                NumberFormatException e) {
            showToast("error", "Invalid Input", "Please enter a valid salary amount.");
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
                showToast("success", "Success", "Salary updated successfully!");
                loadManagementData(); // Refresh bonus calculation
            } else {
                showToast("error", "Error", "Failed to update salary.");
            }
        });

        task.setOnFailed(e -> {
            logger.error("Failed to update salary", task.getException());
            showToast("error", "Error", "An error occurred while updating salary.");
        });

        ThreadPoolManager.getInstance().execute(task);
    }

    @FXML
    private void onApplyBonus() {
        if (selectedStaffId == 0) {
            showToast("warning", "No Employee Selected", "Please select an employee first.");
            return;
        }

        try {
            String bonusText = totalBonusLabel.getText().replace("$", "").trim();
            double bonusAmount = Double.parseDouble(bonusText);

            if (bonusAmount <= 0) {
                showToast("warning", "No Bonus", "There is no bonus to apply for this employee.");
                return;
            }else {
                dashboardController.showConfirmDialog("Confirm Apply Bonus", String.format("Are you sure you want to apply bonus of $ %.2f?", bonusAmount), "⚠", () -> applyBonusAsync(bonusAmount));
            }
        } catch (
                NumberFormatException e) {
            showToast("error", "Invalid Bonus", "Unable to parse bonus amount.");
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
                showToast("success", "Success", "Bonus applied successfully!");
            } else {
                showToast("error", "Error", "Failed to apply bonus.");
            }
        });

        task.setOnFailed(e -> {
            logger.error("Failed to apply bonus", task.getException());
            showToast("error", "Error", "An error occurred while applying bonus.");
        });

        ThreadPoolManager.getInstance().execute(task);
    }

    @FXML
    private void onTerminateEmployee() {
        if (selectedStaffId == 0) {
            showToast("warning", "No Employee Selected", "Please select an employee first.");
            return;
        }

        String reason = terminationReasonField.getText().trim();

        if (reason.isEmpty()) {
            showToast("warning", "Reason Required", "Please provide a reason for termination.");
            return;
        }

        // Confirm termination

        dashboardController.showConfirmDialog("Confirm Employee Termination", ("⚠ WARNING: This action cannot be undone!\nAre you sure you want to terminate this employee?\n\nReason: " + reason), "⚠", () -> terminateEmployeeAsync(reason));
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
                showToast("success", "Success", "Employee has been terminated successfully.");
                closeManagementPaneWithAnimation();
                // Reload staff list
                loadStaffCardsAsync(showActive);
            } else {
                showToast("error", "Error", "Failed to terminate employee.");
            }
        });

        task.setOnFailed(e -> {
            logger.error("Failed to terminate employee", task.getException());
            showToast("error", "Error", "An error occurred while terminating employee.");
        });

        ThreadPoolManager.getInstance().execute(task);
    }

    // ---------- Toast Notification Methods ----------
    private void showToast(String type, String title, String message) {
        if (dashboardController != null) {
            dashboardController.showToast(title, message, type);
        } else {
            logger.warn("Dashboard controller not set; unable to show toast: {} - {}", title, message);
        }
    }

}
