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

import javax.xml.crypto.Data;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class managerOverviewController {

    private int managerId ;

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
        partbtn.getStyleClass().add("part_active");
        activateButton(partbtn);
    }

    @FXML
    void clickStaffbtn(ActionEvent event) throws SQLException, IOException {
        besti = "staff";
        setBesti();
        staffbtn.getStyleClass().add("staff_active");
        activateButton(staffbtn);
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
        qtyBarChart.getData().addAll(carSeries, partSeries);
        revenueAreaChart.getData().add(revenueSeries);
        styleCharts();

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

        for (int y = 2000; y <= currentYear ; y++) {
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
        CallableStatement cs = con.prepareCall("CALL targetviewchart(?,?,?)");
        cs.setInt(1,managerId);
        cs.setInt(2,currentMonth);
        cs.setInt(3,currentYear);

        ResultSet rs = cs.executeQuery();
        if(rs.next()){
            int target_car = rs.getInt(1);
            int target_part = rs.getInt(2);
            int achieve_car = rs.getInt(3);
            int achieve_part = rs.getInt(4);

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
        carCircle.setStrokeDashOffset(circulerCar * (1 - progressCar));
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
            targetOverPart.setStyle("-fx-font-weight:bold; -fx-font-size:15; -fx-text-fill:#10981;");

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
        partCircle.setStrokeDashOffset(circulerPart * (1 - progressPart));
    }

    //attendance table side
    private ObservableList<ManagerOfAttendanceView> setAttendanceTable() throws SQLException {
        ObservableList<ManagerOfAttendanceView> temporylist = FXCollections.observableArrayList();

        CallableStatement cs =con.prepareCall("CALL managerattendanceview()");
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

        CallableStatement cs = con.prepareCall("call attendancepercentage_managerview();");
        ResultSet rs = cs.executeQuery();

        while(rs.next()){
            Integer attendedUsers = rs.getInt(1);
            Integer workingUsers = rs.getInt(2);
            rating = rs.getString(3);
            piechartdata.add(new PieChart.Data("Attendance Workers",attendedUsers));
            piechartdata.add(new PieChart.Data("Total Workers",workingUsers));
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
//        slideOut.setToX(-750);

        // Move to next screen
        if(!sideTransitation) {
            currentCarouselScreenIndex++;
        }else{
            currentCarouselScreenIndex--;
        }
        VBox nextScreen = carouselScreens[currentCarouselScreenIndex];
//        nextScreen.setTranslateX(800);
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
    private ObservableList<managerOverview> chartData;
    private XYChart.Series<String, Integer> carSeries;
    private XYChart.Series<String, Integer> partSeries;
    private XYChart.Series<String, Double> revenueSeries;
    private void setCharts() throws SQLException {
        carSeries.getData().clear();
        partSeries.getData().clear();
        revenueSeries.getData().clear();

        String selectedPart = saleComboBox.getValue();
        CallableStatement cs = con.prepareCall("CALL getSalesChartData(?,?,?)");
        {
            cs.setInt(1, currentMonth);
            cs.setInt(2, currentYear);
            cs.setString(3, selectedPart);

            ResultSet rs = cs.executeQuery();
            while (rs.next()){

                String monthDate = rs.getString(1);
                int carSoldQty = rs.getInt(2);
                int partSoldQty = rs.getInt(3);
                double revenue = rs.getDouble(4);

                carSeries.getData().add(new XYChart.Data<>(monthDate, carSoldQty));
                partSeries.getData().add(new XYChart.Data<>(monthDate, partSoldQty));
                revenueSeries.getData().add(new XYChart.Data<>(monthDate, revenue));
            }
            rs.close();
            cs.close();
        }
    }
    private void styleCharts() {
        // Style bar chart
        qtyBarChart.setLegendVisible(true);
        qtyBarChart.setAnimated(false);
        qtyBarChart.setCategoryGap(20);

        // Style area chart
        revenueAreaChart.setLegendVisible(true);
        revenueAreaChart.setAnimated(false);

        // Set colors for series (optional)
        String carColor = "-fx-bar-fill:  #6D8196;";
        String partColor = "-fx-bar-fill: #ffa500;";
        String revenueColor = "-fx-background-color: #3498db, linear-gradient(to bottom, #3498db 0%, #2980b9 100%);";
    }

    //for besti of cars,parts and staff
    private ObservableList<managerOverview> bestSellerList;
    private ObservableList<managerOverview> bestCarPartList;
    private String besti ;
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
            if (besti.equals("car") || besti.equals("part")) {
                while (rs.next()) {
                    int rank = rs.getInt(1);
                    int targetQty = rs.getInt(2);
                    int soldQty = rs.getInt(3);
                    String inventoryName = rs.getString(4);
                    managerOverview item = new managerOverview(
                            rank, targetQty, soldQty, inventoryName
                    );
                    bestCarPartList.add(item);
                }
            } else {
                while (rs.next()) {
                    int rank = rs.getInt(1);
                    int staffId = rs.getInt(2);
                    int workHour = rs.getInt(3);
                    int prevWorkHour = rs.getInt(4);
                    String staffPhoto = rs.getString(5);
                    String staffName = rs.getString(6);
                    Double totalSale = rs.getDouble(7);
                    Double prevTotalSale = rs.getDouble(8);

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