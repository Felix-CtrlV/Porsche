package Controllers;

import Database.Porsche_DB;
import Model.ManagerOfAttendanceView;

import Model.managerOverview;
import Utils.Session;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class managerOverviewController {

    private int managerId;
    private boolean listenersInitialized = false;

    @FXML
    private Button NextMonthbtn;

    @FXML
    private Button NextYearbtn;

    @FXML
    private Button PreviousMonthbtn;

    @FXML
    private Button PreviousYearbth;

    @FXML
    private Button arrowLeftbtn;

    @FXML
    private Button arrowRightbtn;

    @FXML
    private VBox attendanceBox;

    @FXML
    private PieChart attendancePieChart;

    @FXML
    private TableView<ManagerOfAttendanceView> attendanceTable;

    @FXML
    private Circle carCircle;

    @FXML
    private Button carbtn;

    @FXML
    private StackPane carouselStackPane;

    @FXML
    private ChoiceBox<String> monthBox;

    @FXML
    private Circle partCircle;

    @FXML
    private Button partbtn;

    @FXML
    private ComboBox<String> saleComboBox;

    @FXML
    private VBox scrollPane;
    @FXML
    private TableColumn<ManagerOfAttendanceView, Time> signInTimeCol;

    @FXML
    private TableColumn<ManagerOfAttendanceView, String> statusCol;

    @FXML
    private VBox targetBox;

    @FXML
    private Text targetCar;

    @FXML
    private Label targetCarMessagelbl;

    @FXML
    private Label targetOverCar;

    @FXML
    private Label targetOverPart;

    @FXML
    private Text targetPart;

    @FXML
    private Label targetPartMessagelbl;

    @FXML
    private TableColumn<ManagerOfAttendanceView, String> workerCol;

    @FXML
    private ChoiceBox<Integer> yearBox;

    @FXML
    private Button staffbtn;

    @FXML
    private Button carChart;

    @FXML
    private Button partChart;

    @FXML
    private BarChart<String, Integer> qtyBarChart;

    @FXML
    private AreaChart<String, Double> revenueAreaChart;

    public managerOverviewController() throws SQLException, ClassNotFoundException {
    }

    @FXML
    void ClickSaleComboBox(ActionEvent event) {
        try {
            setCharts();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void clickArrowLeftbtn(ActionEvent event) {
        if (currentCarouselScreenIndex == 0) return;
        sideTransitation = true;
        arrowLeftbtn.setDisable(true);
        arrowRightbtn.setDisable(true);
        setFadePane();

    }

    @FXML
    void clickArrowRightbtn(ActionEvent event) {
        if (currentCarouselScreenIndex == carouselScreens.length - 1) return;
        sideTransitation = false;
        arrowLeftbtn.setDisable(true);
        arrowRightbtn.setDisable(true);
        setFadePane();
    }

    @FXML
    void clickCarbtn(ActionEvent event) throws SQLException, IOException {
        besti = "car";
        setBesti();
        activateButton(carbtn);
    }

    @FXML
    void clickNextMonth(ActionEvent event) {
        currentMonth++;
        if (currentMonth > 12) {
            currentMonth = 1;
            currentYear++;
        }
        updateYearMonthLabel();
    }

    @FXML
    void clickNextYear(ActionEvent event) {
        currentYear++;
        if (today.getYear() == currentYear) {
            if (currentMonth > today.getMonthValue()) {
                currentMonth = today.getMonthValue();
            }
        }
        updateYearMonthLabel();

    }

    @FXML
    void clickPartbtn(ActionEvent event) throws SQLException, IOException {
        besti = "part";
        setBesti();
        activateButton(partbtn);
    }

    @FXML
    void clickStaffbtn(ActionEvent event) throws SQLException, IOException {
        besti = "staff";
        setBesti();
        staffbtn.getStyleClass().add("staff_active");
        activateButton(staffbtn);
    }

    private void activateChartButton(Button activeBtn) {
        // Remove active class from both buttons
        carChart.getStyleClass().remove("active");
        partChart.getStyleClass().remove("active");
        
        // Add active class to clicked button
        activeBtn.getStyleClass().add("active");
        
        // Apply color-coded text styling
        if (carChart == activeBtn) {
            // Car is active - use car color (#6D8196)
            carChart.setStyle("-fx-text-fill: #6D8196; -fx-font-weight: bold; -fx-background-color: transparent;");
            partChart.setStyle("-fx-text-fill: rgba(255,255,255,0.6); -fx-font-weight: normal; -fx-background-color: transparent;");
        } else {
            // Part is active - use part color (#ffa500)
            partChart.setStyle("-fx-text-fill: #ffa500; -fx-font-weight: bold; -fx-background-color: transparent;");
            carChart.setStyle("-fx-text-fill: rgba(255,255,255,0.6); -fx-font-weight: normal; -fx-background-color: transparent;");
        }
    }

    @FXML
    void clickCarChart(ActionEvent event) throws SQLException {
        System.out.println("Car chart button clicked - switching to car mode");
        chartMode = "car";
        setBarChartOnly(); // Refresh ONLY bar chart to show car data
        activateChartButton(carChart);
        System.out.println("Car chart data loaded. Car series size: " + carSeries.getData().size());
    }

    @FXML
    void clickPartChart(ActionEvent event) throws SQLException {
        System.out.println("Part chart button clicked - switching to part mode");
        chartMode = "part";
        setBarChartOnly(); // Refresh ONLY bar chart to show part data
        activateChartButton(partChart);
        System.out.println("Part chart data loaded. Part series size: " + partSeries.getData().size());
    }

    @FXML
    void clickPreviousMonth(ActionEvent event) {
        currentMonth--;
        if (currentMonth < 1) {
            currentMonth = 12;
            currentYear--;
        };
        updateYearMonthLabel();
    }

    @FXML
    void clickPreviousYear(ActionEvent event) {
        currentYear--;
        updateYearMonthLabel();
    }

    @FXML
    private void initialize() throws SQLException {
        //to get the manager id
        Session current = Session.getInstance();
        if (current != null) {
            managerId = current.getUserid();
        }

        //for bar chart and area chart of the sale performance
        saleComboBox.getItems().addAll("Daily", "Weekly", "Monthly");
        saleComboBox.setValue("Monthly");
        carSeries = new XYChart.Series<>();
        carSeries.setName("Cars Sold");
        partSeries = new XYChart.Series<>();
        partSeries.setName("Parts Sold");
        revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("Revenue");
        
        // Initially show car data (default mode)
        updateChartSeries();
        revenueAreaChart.getData().add(revenueSeries);
        styleCharts();
        
        // Initialize chart buttons - set car as default active
        activateChartButton(carChart);

        //for besti of cars and parts and staff
        besti = "car";
        bestSellerList = FXCollections.observableArrayList();
        bestCarPartList = FXCollections.observableArrayList();
        activateButton(carbtn);

         //for attendance table
        workerCol.setCellValueFactory(e->
                new SimpleStringProperty(e.getValue().getWorkers()));
        signInTimeCol.setCellValueFactory(e->
                new ReadOnlyObjectWrapper<Time>(e.getValue().getSign_in_time()));
        statusCol.setCellValueFactory(e->
                new SimpleStringProperty(e.getValue().getStatus()));
        attendanceTable.setItems(setAttendanceTable());

        //for first show the attendance table path
        attendanceBox.setVisible(true);
        targetBox.setVisible(false);
        arrowLeftbtn.setDisable(true);
        arrowRightbtn.setDisable(false);

        //for slide pane of attendance and target
        carouselScreens = new VBox[]{attendanceBox, targetBox};
        for (int i = 0; i < carouselScreens.length; i++) {
            if (i == 0) {
                carouselScreens[i].setVisible(true);
                carouselScreens[i].setTranslateX(0);
            } else {
                carouselScreens[i].setVisible(false);
                carouselScreens[i].setTranslateX(500);
            }
        }
        arrowLeftbtn.setDisable(true);
        arrowRightbtn.setDisable(carouselScreens.length <= 1);
        arrowLeftbtn.setVisible(false);
        arrowRightbtn.setVisible(true);
        //part of not to throughout the stack pane
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(carouselStackPane.widthProperty());
        clip.heightProperty().bind(carouselStackPane.heightProperty());
        clip.setArcWidth(16); // Match your border radius
        clip.setArcHeight(16); // Match your border radius
        carouselStackPane.setClip(clip);

        //for creating month and year firstly
        currentDateSelect();
    }

    private void activateButton(Button activeBtn) {
        carbtn.getStyleClass().remove("active");
        partbtn.getStyleClass().remove("active");
        staffbtn.getStyleClass().remove("active");

        activeBtn.getStyleClass().add("active");
    }


    //to connect with the database
    Porsche_DB db = new Porsche_DB();
    Connection con = db.connect();

    //for date box or date side
    private LocalDate today = LocalDate.now();
    private boolean updatingMonthBox = false;
    private int currentMonth;
    private int currentYear;
    private void currentDateSelect() {
        currentMonth = today.getMonthValue();
        currentYear = today.getYear();
        NextMonthbtn.setDisable(true);
        NextMonthbtn.setVisible(false);
        NextYearbtn.setDisable(true);
        NextYearbtn.setVisible(false);

        for (int y = 2020; y <= currentYear ; y++) {
            yearBox.getItems().add(y);
        }
        yearBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                currentYear = newVal;
                updateYearMonthLabel();
                // Update chart when year changes
            }
        });
        monthBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (!updatingMonthBox && newVal != null) {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);
                Month parsedMonth = Month.from(fmt.parse(newVal));
                currentMonth = parsedMonth.getValue();
                updateYearMonthLabel();

            }
        });

        updateYearMonthLabel();
        try {
            setAttendancePieChart();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
    private void updateYearMonthLabel() {
        int curyear = today.getYear();
        Month nmonth = Month.of(currentMonth);
        int curmonth = today.getMonthValue();
        
        // Define earliest date (company start date: October 2020)
        final int EARLIEST_YEAR = 2020;
        final int EARLIEST_MONTH = 10;

        // Handle Next buttons (future dates)
        if (currentYear >= curyear) {
            NextYearbtn.setDisable(true);
            NextYearbtn.setVisible(false);
            if (currentMonth >= curmonth) {
                NextMonthbtn.setDisable(true);
                NextMonthbtn.setVisible(false);
            } else {
                NextMonthbtn.setDisable(false);
                NextMonthbtn.setVisible(true);
            }
        } else {
            NextYearbtn.setDisable(false);
            NextYearbtn.setVisible(true);
            NextMonthbtn.setDisable(false);
            NextMonthbtn.setVisible(true);
        }
        
        // Handle Previous buttons (past dates)
        if (currentYear <= EARLIEST_YEAR) {
            PreviousYearbth.setDisable(true);
            PreviousYearbth.setVisible(false);
            if (currentMonth <= EARLIEST_MONTH) {
                PreviousMonthbtn.setDisable(true);
                PreviousMonthbtn.setVisible(false);
            } else {
                PreviousMonthbtn.setDisable(false);
                PreviousMonthbtn.setVisible(true);
            }
        } else {
            PreviousYearbth.setDisable(false);
            PreviousYearbth.setVisible(true);
            PreviousMonthbtn.setDisable(false);
            PreviousMonthbtn.setVisible(true);
        }

        // 🔹 Sync ComboBoxes with updated currentMonth/currentYear
        String monthName = nmonth.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        if (monthBox.getItems().contains(monthName)) {
            monthBox.setValue(monthName);
        }

        if (yearBox.getItems().contains(currentYear)) {
            yearBox.setValue(currentYear);
        }
        updateMonthBoxForYear(currentYear);

        //to set target
        try {
            setTarget();
            setCharts();
            setBesti();
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void updateMonthBoxForYear(int year) {
        int startMonth = 1;
        int endMonth = 12;
        
        // Company started in October 2020
        if (year == 2020) {
            startMonth = 10; // October
        }

        if (year == today.getYear()) {
            endMonth = today.getMonthValue();
        }

        List<String> months = new ArrayList<>();
        for (int m = startMonth; m <= endMonth; m++) {
            months.add(Month.of(m).getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
        }

        updatingMonthBox = true;
        monthBox.setItems(FXCollections.observableArrayList(months));

        // Make sure currentMonth is valid
        if (!months.contains(Month.of(currentMonth).getDisplayName(TextStyle.SHORT, Locale.ENGLISH))) {
            currentMonth = startMonth; // default to first available month
        }

        monthBox.setValue(Month.of(currentMonth).getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
        updatingMonthBox = false;
    }

    //target side
    private void setTarget() throws SQLException {
        CallableStatement cs = con.prepareCall("CALL targetViewChart(?,?,?)");
        cs.setInt(1,managerId);
        cs.setInt(2,currentMonth);
        cs.setInt(3,currentYear);

        ResultSet rs = cs.executeQuery();
        if(rs.next()){
            int target_car = rs.getInt(4);
            int target_part = rs.getInt(5);
            int achieve_car = rs.getInt(6);
            int achieve_part = rs.getInt(7);

            setCarCircle(target_car,achieve_car);
            setPartCircle(target_part,achieve_part);
        }
        cs.close();
        rs.close();
    }
    private void setCarCircle(int target,int achieve){
        carCircle.setVisible(true);
        targetCar.setText(String.valueOf(achieve) + "/" + String.valueOf(target));
        int targetOverC = 0;
        double progressCar = 0;
        if (achieve == 0 && target == 0) {
            targetOverCar.setText("0");
            carCircle.setVisible(false);
            targetCarMessagelbl.setText("No Target");

        } else if (achieve >= target) {
            targetOverC = achieve - target;
            progressCar = (target > 0) ? (double) achieve / achieve : 0;

            targetOverCar.setText("+" + String.valueOf(targetOverC));
            targetOverCar.setStyle("-fx-font-weight:bold; -fx-font-size:15; -fx-text-fill:#10b981;");

            targetCar.setText(String.valueOf(achieve) + "/" + String.valueOf(achieve));

            targetCarMessagelbl.setText("Target Achieved! \uD83C\uDF89");
            targetCarMessagelbl.setStyle("-fx-text-fill: #10b981; -fx-font-weight:bold; -fx-font-size:18;");

        } else {
            targetOverC = target - achieve;
            progressCar = (target > 0) ? (double) achieve / target : 0;
            targetOverCar.setText("-" + String.valueOf(targetOverC));

            targetOverCar.setStyle("-fx-font-weight:bold; -fx-font-size:15; -fx-text-fill: #ef4444;");

            if(yearBox.getSelectionModel().getSelectedItem().equals(today.getYear())){
                targetCarMessagelbl.setText("Need to hit target");
            }else{
                targetCarMessagelbl.setText("Missed the target");
            }
            targetCarMessagelbl.setStyle("-fx-text-fill: #ef4444; -fx-font-weight:bold; -fx-font-size:18;");
        }
        targetCar.setStyle("-fx-font-size:18;-fx-font-weight:bold; -fx-text-fill:  #6d8196;");

        double circulerCar = 2 * Math.PI * carCircle.getRadius();
        carCircle.getStrokeDashArray().setAll(circulerCar, circulerCar);
        
        // Calculate final offset
        double finalOffset = circulerCar * (1 - progressCar);
        
        // Start with full offset (hidden) and animate to final offset
        carCircle.setStrokeDashOffset(circulerCar);
        
        // Create wave animation
        javafx.animation.Timeline timeline = new javafx.animation.Timeline();
        javafx.animation.KeyFrame keyFrame = new javafx.animation.KeyFrame(
            javafx.util.Duration.millis(1500), // 1.5 seconds
            new javafx.animation.KeyValue(
                carCircle.strokeDashOffsetProperty(),
                finalOffset,
                javafx.animation.Interpolator.EASE_OUT
            )
        );
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }
    private void setPartCircle(int target, int achieve) {
        targetPart.setText(String.valueOf(achieve) + "/" + String.valueOf(target));
        partCircle.setVisible(true);
        int targetOverP = 0;
        double progressPart = 0;
        if (target == 0 && achieve == 0) {
            targetOverPart.setText("0");
            partCircle.setVisible(false);
            targetPartMessagelbl.setText("No target");

        } else if (achieve >= target) {
            targetOverP = achieve - target;
            progressPart = (target > 0) ? (double) achieve / achieve : 0;
            targetOverPart.setText("+" + String.valueOf(targetOverP));
            targetOverPart.setStyle("-fx-font-weight:bold; -fx-font-size:15; -fx-text-fill:#10b981;");

            targetPart.setText(String.valueOf(achieve) + "/" + String.valueOf(achieve));

            targetPartMessagelbl.setText("Target Achieved! \uD83C\uDF89");
            targetPartMessagelbl.setStyle("-fx-text-fill: #ffa500; -fx-font-weight:bold; -fx-font-size:18;");

        } else {
            targetOverP = target - achieve;
            progressPart = (target > 0) ? (double) achieve / target : 0;

            targetOverPart.setText("-" + String.valueOf(targetOverP));
            targetOverPart.setStyle("-fx-font-weight:bold; -fx-font-size:15; -fx-text-fill: #ef4444;");

            if(yearBox.getSelectionModel().getSelectedItem().equals(today.getYear())){
                targetPartMessagelbl.setText("Need to hit target");
            }else{
                targetPartMessagelbl.setText("Missed the target");
            }
            targetPartMessagelbl.setStyle("-fx-text-fill: #ef4444; -fx-font-weight:bold; -fx-font-size:18;");
        }
        targetCar.setStyle("-fx-font-size:18;-fx-font-weight:bold; -fx-text-fill:  #ffa500;");

        double circulerPart = 2 * Math.PI * partCircle.getRadius();
        partCircle.getStrokeDashArray().setAll(circulerPart, circulerPart);
        
        // Calculate final offset
        double finalOffset = circulerPart * (1 - progressPart);
        
        // Start with full offset (hidden) and animate to final offset
        partCircle.setStrokeDashOffset(circulerPart);
        
        // Create wave animation
        javafx.animation.Timeline timeline = new javafx.animation.Timeline();
        javafx.animation.KeyFrame keyFrame = new javafx.animation.KeyFrame(
            javafx.util.Duration.millis(1500), // 1.5 seconds
            new javafx.animation.KeyValue(
                partCircle.strokeDashOffsetProperty(),
                finalOffset,
                javafx.animation.Interpolator.EASE_OUT
            )
        );
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

    //attendance table side
    private ObservableList<ManagerOfAttendanceView> setAttendanceTable() throws SQLException {
        ObservableList<ManagerOfAttendanceView> temporylist = FXCollections.observableArrayList();

        CallableStatement cs =con.prepareCall("CALL getAttendanceView()");
        ResultSet rs = cs.executeQuery();

        while(rs.next()) {
            String name = rs.getString(1);
            Time in_time = rs.getTime(2);
            String status = rs.getString(3);
            ManagerOfAttendanceView managerOfAttendanceView = new ManagerOfAttendanceView(name, in_time,status);

            temporylist.add(managerOfAttendanceView);
        }

        rs.close();
        cs.close();
        return temporylist;
    }
    private  void setAttendancePieChart() throws SQLException {
        ObservableList<PieChart.Data> piechartdata = FXCollections.observableArrayList();
        String rating ="0 %";

        CallableStatement cs = con.prepareCall("call getAttendancePercentage();");
        ResultSet rs = cs.executeQuery();

        while(rs.next()){
            Integer attendedUsers = rs.getInt(1);    // Workers who attended
            Integer totalWorkers = rs.getInt(2);     // Total workers
            rating = rs.getString(3);
            
            // Calculate absent workers
            Integer absentUsers = totalWorkers - attendedUsers;
            
            // Only add slices if there are workers
            if (totalWorkers > 0) {
                if (attendedUsers > 0) {
                    piechartdata.add(new PieChart.Data("Present (" + attendedUsers + ")", attendedUsers));
                }
                if (absentUsers > 0) {
                    piechartdata.add(new PieChart.Data("Absent (" + absentUsers + ")", absentUsers));
                }
                
                // If all workers attended, show only the "Present" slice (100%)
                if (absentUsers == 0 && attendedUsers > 0) {
                    // Chart will show 100% attendance
                }
            }
        }
        rs.close();
        cs.close();

        attendancePieChart.setData(piechartdata);
    }

    //for slide pane of target and attendance
    private VBox[] carouselScreens;
    private int currentCarouselScreenIndex = 0;
    private boolean sideTransitation = true;
    private void setFadePane(){
        VBox currentScreen = carouselScreens[currentCarouselScreenIndex];

        // Fade out current screen while sliding
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), currentScreen);
        fadeOut.setToValue(0.3);

        // Slide current screen
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(400), currentScreen);

        // Move to next screen
        if(!sideTransitation) {
            currentCarouselScreenIndex++;
        }else{
            currentCarouselScreenIndex--;
        }
        VBox nextScreen = carouselScreens[currentCarouselScreenIndex];
        nextScreen.setVisible(true);



        // Fade in next screen
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), nextScreen);
        fadeIn.setFromValue(0.3);
        fadeIn.setToValue(1.0);

        // Slide new screen in from the right
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), nextScreen);
        slideIn.setToX(0);

        if(!sideTransitation){
            slideOut.setToX(-750);
            nextScreen.setTranslateX(800);
        }else{
            slideOut.setToX(750);
            nextScreen.setTranslateX(-800);
        }

        // Play animations in parallel
        ParallelTransition outTransition = new ParallelTransition(fadeOut, slideOut);
        ParallelTransition inTransition = new ParallelTransition(fadeIn, slideIn);

        outTransition.play();
        inTransition.play();
        outTransition.setOnFinished(e -> {
            currentScreen.setVisible(false);
            currentScreen.setOpacity(1.0); // Reset opacity for next time

            inTransition.setOnFinished(e2 -> {
                arrowLeftbtn.setDisable(currentCarouselScreenIndex == 0);
                arrowRightbtn.setDisable(currentCarouselScreenIndex == carouselScreens.length - 1);
                if(!sideTransitation) {
                    arrowRightbtn.setVisible(false);
                    arrowLeftbtn.setVisible(true);
                }else{
                    arrowRightbtn.setVisible(true);
                    arrowLeftbtn.setVisible(false);
                }
            });
        });
    }

    //for bar chart and area chart of the sale performance
    private XYChart.Series<String, Integer> carSeries;
    private XYChart.Series<String, Integer> partSeries;
    private XYChart.Series<String, Double> revenueSeries;
    private String chartMode = "car"; // Track current chart mode: "car" or "part"
    
    //for besti of cars, parts and staff
    private String besti;
    private ObservableList<managerOverview> bestSellerList;
    private ObservableList<managerOverview> bestCarPartList;
    
    /**
     * Updates ONLY the bar chart series without affecting revenue chart
     */
    private void updateBarChartSeries() {
        // Clear existing series from bar chart
        qtyBarChart.getData().clear();
        
        // Add appropriate series based on chart mode
        if (chartMode.equals("car")) {
            qtyBarChart.getData().add(carSeries);
            System.out.println("Added car series to bar chart. Car series data points: " + carSeries.getData().size());
        } else if (chartMode.equals("part")) {
            qtyBarChart.getData().add(partSeries);
            System.out.println("Added part series to bar chart. Part series data points: " + partSeries.getData().size());
        }
        
        System.out.println("Chart mode: " + chartMode + ", Bar chart series count: " + qtyBarChart.getData().size());
        
        // Apply styling to bar chart only
        styleBarChartOnly();
    }
    
    /**
     * Updates BOTH bar chart and revenue chart series
     */
    private void updateChartSeries() {
        // Clear existing series from bar chart
        qtyBarChart.getData().clear();
        
        // Add appropriate series based on chart mode
        if (chartMode.equals("car")) {
            qtyBarChart.getData().add(carSeries);
            revenueSeries.setName("Car Revenue");
            System.out.println("Added car series to bar chart. Car series data points: " + carSeries.getData().size());
        } else if (chartMode.equals("part")) {
            qtyBarChart.getData().add(partSeries);
            revenueSeries.setName("Part Revenue");
            System.out.println("Added part series to bar chart. Part series data points: " + partSeries.getData().size());
        }
        
        System.out.println("Chart mode: " + chartMode + ", Bar chart series count: " + qtyBarChart.getData().size());
        
        // Apply styling after updating series
        styleCharts();
    }
    
    /**
     * Updates ONLY the bar chart (car/part quantities) without affecting the revenue chart
     */
    private void setBarChartOnly() throws SQLException {
        carSeries.getData().clear();
        partSeries.getData().clear();

        String selectedPeriod = saleComboBox.getValue();
        
        try {
            // Call getSalesChartData procedure directly
            CallableStatement cs = con.prepareCall("CALL getSalesChartData(?,?,?,?)");
            cs.setInt(1, managerId);
            cs.setInt(2, currentMonth);
            cs.setInt(3, currentYear);
            cs.setString(4, selectedPeriod);

            ResultSet rs = cs.executeQuery();
            while (rs.next()){
                String periodLabel = rs.getString("period_label");
                int carQty = rs.getInt("car_qty");
                int partQty = rs.getInt("part_qty");
                // Don't read revenue data - we're not updating revenue chart

                carSeries.getData().add(new XYChart.Data<>(periodLabel, carQty));
                partSeries.getData().add(new XYChart.Data<>(periodLabel, partQty));
                // Don't add to revenueSeries - keep revenue chart unchanged
            }
            rs.close();
            cs.close();
            
            // Update only bar chart series
            updateBarChartSeries();
            
        } catch (Exception e) {
            System.err.println("Error loading bar chart data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Updates BOTH bar chart and revenue chart (used when month/year changes)
     */
    private void setCharts() throws SQLException {
        carSeries.getData().clear();
        partSeries.getData().clear();
        revenueSeries.getData().clear();

        String selectedPeriod = saleComboBox.getValue();
        
        try {
            // Call getSalesChartData procedure directly
            CallableStatement cs = con.prepareCall("CALL getSalesChartData(?,?,?,?)");
            cs.setInt(1, managerId);
            cs.setInt(2, currentMonth);
            cs.setInt(3, currentYear);
            cs.setString(4, selectedPeriod);

            ResultSet rs = cs.executeQuery();
            while (rs.next()){
                String periodLabel = rs.getString("period_label");
                int carQty = rs.getInt("car_qty");
                int partQty = rs.getInt("part_qty");
                double revenue = rs.getDouble("revenue");

                carSeries.getData().add(new XYChart.Data<>(periodLabel, carQty));
                partSeries.getData().add(new XYChart.Data<>(periodLabel, partQty));
                revenueSeries.getData().add(new XYChart.Data<>(periodLabel, revenue));
            }
            rs.close();
            cs.close();
            
            // Update chart series after loading data
            updateChartSeries();
            
        } catch (Exception e) {
            System.err.println("Error loading chart data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    /**
     * Styles ONLY the bar chart without affecting revenue chart
     */
    private void styleBarChartOnly() {
        // Style bar chart - hide default legend, we have custom legend in FXML
        qtyBarChart.setLegendVisible(false);
        qtyBarChart.setAnimated(false);
        qtyBarChart.setCategoryGap(20);

        // Apply custom colors to match the selected mode (car or part)
        Platform.runLater(() -> {
            String color = chartMode.equals("car") ? "#6D8196" : "#ffa500";
            
            // Apply styling to all bars using lookupAll
            qtyBarChart.lookupAll(".chart-bar").forEach(node -> {
                node.setStyle("-fx-bar-fill: " + color + ";");
            });
        });
    }
    
    /**
     * Styles BOTH bar chart and revenue chart
     */
    private void styleCharts() {
        // Style bar chart - hide default legend, we have custom legend in FXML
        qtyBarChart.setLegendVisible(false);
        qtyBarChart.setAnimated(false);
        qtyBarChart.setCategoryGap(20);

        // Style area chart
        revenueAreaChart.setLegendVisible(true);
        revenueAreaChart.setAnimated(false);
        revenueAreaChart.setCreateSymbols(true); // Enable symbols for smooth curve styling

        // Apply custom colors to match the selected mode (car or part)
        Platform.runLater(() -> {
            String color = chartMode.equals("car") ? "#6D8196" : "#ffa500";
            
            // Apply styling to all bars using lookupAll (like adminOverviewController)
            qtyBarChart.lookupAll(".chart-bar").forEach(node -> {
                node.setStyle("-fx-bar-fill: " + color + ";");
            });
            
            // Apply colors to area chart based on active mode
            if (revenueSeries.getNode() != null) {
                if (chartMode.equals("car")) {
                    // Car mode - use car color
                    revenueSeries.getNode().setStyle("-fx-fill: rgba(109,129,150,0.3); -fx-stroke: #6D8196; -fx-stroke-width: 2px;");
                } else if (chartMode.equals("part")) {
                    // Part mode - use part color
                    revenueSeries.getNode().setStyle("-fx-fill: rgba(255,165,0,0.3); -fx-stroke: #ffa500; -fx-stroke-width: 2px;");
                }
            }
            
            // Apply smooth curves after a short delay to ensure paths are created
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(100));
            pause.setOnFinished(e -> applySmoothCurves());
            pause.play();
        });
    }
    
    // ---------- Smooth Curve Methods ----------
    private void applySmoothCurves() {
        if (revenueAreaChart == null)
            return;

        // Check if paths are ready, if not, retry after a short delay
        var linePaths = revenueAreaChart.lookupAll(".chart-series-area-line");
        var fillPaths = revenueAreaChart.lookupAll(".chart-series-area-fill");

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

    //for besti of cars,parts and staff
    private void setBesti() throws SQLException, IOException {
        // Initialize the lists if they are null
        if (bestSellerList == null) {
            bestSellerList = FXCollections.observableArrayList();
        }
        if (bestCarPartList == null) {
            bestCarPartList = FXCollections.observableArrayList();
        }

        // Clear previous data
        bestSellerList.clear();
        bestCarPartList.clear();

        CallableStatement cs = null;
        boolean check = true;

        switch (besti){
            case "car":
                cs = con.prepareCall("CALL getBestSellingCars(?,?,?)");
                break;
            case "part":
                cs = con.prepareCall("CALL getBestSellingParts(?,?,?)");
                break;
            case "staff":
                cs = con.prepareCall("CALL getBestStaff(?,?,?)");
                break;
            default:
                check = false;
                break;
        }

        if(check) {
            cs.setInt(1, managerId);
            cs.setInt(2, currentMonth);
            cs.setInt(3, currentYear);
            ResultSet rs = cs.executeQuery();

            // FIXED: Use OR condition instead of AND   
            if (besti.equals("car")) {
                // getBestSellingCars returns: car_rank, target_qty, sold_qty, inventory_name, car_id, car_color, fuel_type, price, achievement_percentage
                while (rs.next()) {
                    int rank = rs.getInt(1);           // car_rank
                    int targetQty = rs.getInt(2);      // target_qty
                    int soldQty = rs.getInt(3);        // sold_qty  
                    String inventoryName = rs.getString(4); // inventory_name (model + trim)
                    managerOverview item = new managerOverview(
                            rank, targetQty, soldQty, inventoryName
                    );
                    bestCarPartList.add(item);
                }
            } else if (besti.equals("part")) {
                // getBestSellingParts returns: part_rank, target_qty, sold_qty, inventory_name, part_id, achievement_percentage
                while (rs.next()) {
                    int rank = rs.getInt(1);           // part_rank
                    int targetQty = rs.getInt(2);      // target_qty
                    int soldQty = rs.getInt(3);        // sold_qty
                    String inventoryName = rs.getString(4); // inventory_name (part_name)
                    managerOverview item = new managerOverview(
                            rank, targetQty, soldQty, inventoryName
                    );
                    bestCarPartList.add(item);
                }
            } else {
                // getBestStaff returns: rank, staffId, staffName, staffPhoto, workHours, prevWorkHours, totalSale, prevTotalSale
                while (rs.next()) {
                    int rank = rs.getInt(1);
                    int staffId = rs.getInt(2);
                    String staffName = rs.getString(3);
                    String staffPhoto = rs.getString(4);
                    int workHour = rs.getInt(5);        // work_hours (column 5)
                    int prevWorkHour = rs.getInt(6);    // prev_work_hours (column 6)
                    Double totalSale = rs.getDouble(7); // total_sale (column 7)
                    Double prevTotalSale = rs.getDouble(8); // prev_total_sale (column 8)

                    managerOverview seller = new managerOverview(
                            rank, staffId, workHour, prevWorkHour,
                            staffPhoto, staffName, totalSale, prevTotalSale
                    );
                    bestSellerList.add(seller);
                }
            }

            rs.close();
            cs.close();


            scrollPane.getChildren().clear();
            // FIXED: Use OR condition instead of AND
            if (besti.equals("car") || besti.equals("part")) {

                for (managerOverview item : bestCarPartList) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/managerCarPartProgressBar.fxml"));
                    HBox progressCard = loader.load();

                    managerCarPartProgressBarController controller = loader.getController();
                    controller.setDate(item);

                    scrollPane.getChildren().add(progressCard);
                }
            } else {
                // Handle staff data
                for (managerOverview seller : bestSellerList) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/bestSeller.fxml"));
                    HBox sellerCard = loader.load();

                    bestSellerController controller = loader.getController();
                    controller.setData(seller, currentMonth, currentYear);

                    scrollPane.getChildren().add(sellerCard);
                }
            }
        }
    }
}