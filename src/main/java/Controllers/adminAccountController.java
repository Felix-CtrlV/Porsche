package Controllers;

import DAO.AdminAccountDAO;
import Model.user;
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

import java.io.IOException;
import java.io.File;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

public class adminAccountController {

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

    public adminAccountController() throws Exception {
        dao = new AdminAccountDAO();
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
        task.setOnFailed(e -> task.getException().printStackTrace());
        new Thread(task, "WeeklySales").start();
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

    // ---------- Load Staff ----------
    private void loadStaffCardsAsync(boolean active) {
        Task<List<user>> task = new Task<>() {
            @Override
            protected List<user> call() throws Exception {
                return dao.getStaffCards(active ? "Active" : "Inactive", roleCombo.getValue());
            }
        };

        task.setOnSucceeded(e -> {
            staffList.setAll(task.getValue()); // Works now because staffList is ObservableList
            populateStaffCards();
        });

        task.setOnFailed(e -> task.getException().printStackTrace());
        new Thread(task, "LoadStaffCards").start();
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
                });

                staffListContainer.getChildren().add(card);
            } catch (
                    IOException ex) {
                ex.printStackTrace();
            }
        }

        if (!staffList.isEmpty()) {
            showStaffDetails(staffList.get(0));
            loadWeeklySalesAsync(staffList.get(0).getId(), currentMonth, currentYear);
        } else {
            selectedStaffId = 0;
            setupEmptyChart();
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
        } catch (
                Exception ignored) {
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
                loadWeeklySalesAsync(selectedStaffId, currentMonth, currentYear);
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
            }
        });
    }

    private void updateMonthYearOptions(user staff) {
        updatingDateBox = true;

        yearBox.getItems().clear();
        LocalDate start = Optional.ofNullable(staff.getStart_date()).orElse(today);
        LocalDate end = Optional.ofNullable(staff.getEnd_date()).orElse(today);

        for (int y = start.getYear(); y <= end.getYear(); y++)
            yearBox.getItems().add(y);
        if (!yearBox.getItems().contains(currentYear))
            currentYear = start.getYear();
        yearBox.setValue(currentYear);

        updateMonthBox();
        updatingDateBox = false;
    }

    private void updateMonthBox() {
        updatingDateBox = true;

        List<String> months = new ArrayList<>();

        // Optional: Limit months by staff start/end if needed
        for (int i = 1; i <= 12; i++)
            months.add(Month.of(i).getDisplayName(TextStyle.SHORT, Locale.ENGLISH));

        monthBox.setItems(FXCollections.observableArrayList(months));
        if (currentMonth < 1 || currentMonth > 12)
            currentMonth = 1;
        monthBox.setValue(Month.of(currentMonth).getDisplayName(TextStyle.SHORT, Locale.ENGLISH));

        updatingDateBox = false;
    }

    private void updateDateControls() {
        boolean isCurrentYear = currentYear == today.getYear();
        NextMonthbtn.setDisable(isCurrentYear && currentMonth >= today.getMonthValue());
        NextYearbtn.setDisable(currentYear >= today.getYear());
        PreviousMonthbtn.setDisable(currentYear <= today.getYear() && currentMonth <= 1);
        PreviousYearbtn.setDisable(currentYear <= 1);
    }

    @FXML
    private void nextMonthClick(MouseEvent e) {
        if (currentMonth == 12) {
            currentMonth = 1;
            currentYear++;
        } else
            currentMonth++;
        updateMonthBox();
        updateDateControls();
        loadWeeklySalesAsync(selectedStaffId, currentMonth, currentYear);
    }

    @FXML
    private void prevMonthClick(MouseEvent e) {
        if (currentMonth == 1) {
            currentMonth = 12;
            currentYear--;
        } else
            currentMonth--;
        updateMonthBox();
        updateDateControls();
        loadWeeklySalesAsync(selectedStaffId, currentMonth, currentYear);
    }

    @FXML
    private void nextYearClick(MouseEvent e) {
        currentYear++;
        updateMonthBox();
        updateDateControls();
        loadWeeklySalesAsync(selectedStaffId, currentMonth, currentYear);
    }

    @FXML
    private void prevYearClick(MouseEvent e) {
        currentYear--;
        updateMonthBox();
        updateDateControls();
        loadWeeklySalesAsync(selectedStaffId, currentMonth, currentYear);
    }

    boolean cardtype = true;

    @FXML
    void SwitchMouseClick(MouseEvent event) {
        cardtype = !cardtype;
        StaffListTitleLabel.setText(cardtype ? "List (Active)" : "List (InActive)");
        loadStaffCardsAsync(cardtype);
    }

}
