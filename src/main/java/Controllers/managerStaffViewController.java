package Controllers;

import Database.Porsche_DB;
import Model.managerOrderView;
import Model.user;
import Utils.Session;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class managerStaffViewController {

    @FXML
    private Label StaffListTitleLabel;

    @FXML
    private Button ActiveInactiveSwitchbtn;

    @FXML
    private TableColumn<managerOrderView, Double> TotalAmountCol;

    @FXML
    private Label CompleOrderlbl;

    @FXML
    private TableColumn<managerOrderView, String> CustomerNameCol;

    @FXML
    private TableColumn<managerOrderView, Date> DateCol;


    @FXML
    private TableColumn<managerOrderView, String> IsInstallmentCol;

    @FXML
    private Button NextMonthbtn;

    @FXML
    private Button NextYearbtn;

    @FXML
    private TableColumn<managerOrderView, Integer> NoCol;

    @FXML
    private Label PendOrderlbl;

    @FXML
    private Button PreviousMonthbtn;

    @FXML
    private Button PreviousYearbth;

    @FXML
    private Button SearchNamebtn;

    @FXML
    private Label StaffAddressLabel;

    @FXML
    private Label StaffDOBLabel;

    @FXML
    private Label StaffEmailLabel;

    @FXML
    private ImageView StaffImage;

    @FXML
    private Label StaffNameLable;

    @FXML
    private Label StaffPhoneLabel;

    @FXML
    private TextField StaffSearchText;

    @FXML
    private Circle attendanceBackCircle;

    @FXML
    private Label TotalOrderlbl;

    @FXML
    private TableView<managerOrderView> ordersTable;

    @FXML
    private VBox staffListContainer;

    @FXML
    private Circle partCircle;

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
    private VBox targetlayer;

    @FXML
    private Circle attendanceCircle;

    @FXML
    private Text attendancePercent;

    @FXML
    private Circle carCircle;

    @FXML
    private ChoiceBox<String> monthBox;

    @FXML
    private ChoiceBox<Integer> yearBox;

    @FXML
    private BorderPane installmentPane ;

    @FXML
    private Label remainAmountLabel;

    @FXML
    private Label paidAmountLabel;

    @FXML
    private Label dueDateLabel ;

    @FXML
    private Label totalPriceLabel ;

    @FXML
    private TableView<String> installmentTable;

    @FXML
    private TableColumn<String,String> installmentNameCol;

    @FXML
    private TableColumn<String,String> installmentQtyCol;

    @FXML
    private TableColumn<String,String> installmentPriceCol;



    @FXML
    void SwitchMouseClick(MouseEvent event) throws SQLException, IOException {
        if (cardtype) {
            cardtype = false;
            StaffListTitleLabel.setText("Staff List (InActive)");
            addStaffCard(cardtype);

        } else {
            cardtype = true;
            StaffListTitleLabel.setText("Staff List (Active)");
            addStaffCard(cardtype);
        }
    }

    @FXML
    void nextMonthClick(MouseEvent event) {
        currentMonth++;
        if (currentMonth > 12) {
            currentMonth = 1;
            currentYear++;
        }
        ;
        updateYearMonthLabel();
    }

    @FXML
    void nextYearClick(MouseEvent event) {
        currentYear++;
        if (today.getYear() == currentYear) {
            if (currentMonth > today.getMonthValue()) {
                currentMonth = today.getMonthValue();
            }
        }
        updateYearMonthLabel();

    }

    @FXML
    void prevMonthClick(MouseEvent event) {
        currentMonth--;
        if (currentMonth < 1) {
            currentMonth = 12;
            currentYear--;
        };
        updateYearMonthLabel();

    }

    @FXML
    void prevYearClick(MouseEvent event) {
        currentYear--;
        updateYearMonthLabel();

    }

    @FXML
    private void clickStaffImage(MouseEvent event) {
        handleImageSelection(StaffImage);
    }

    @FXML
    void ordersTableClick(MouseEvent event) throws IOException {
        managerOrderView selectorder = ordersTable.getSelectionModel().getSelectedItem();


        if (selectorder == null) {
            // Show target overview when no order is selected or table is empty
            installmentPane.setVisible(false);
            targetlayer.setVisible(true);
            return;
        }
        orderDetails(selectorder);
    }


    @FXML
    private void initialize() throws SQLException, IOException {
        // Get the manager id from session
        Session current = Session.getInstance();
        if (current != null) {
            managerId = current.getUserid();
        }

        //for creating the month and year of this month
        currentDateSelect();

        //for inserting staff cards
        StaffListTitleLabel.setText("Staff List (Active)");
        addStaffCard(cardtype);


        // for inserting orders
        NoCol.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getNo()));
        CustomerNameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCus_name()));
        DateCol.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getOrder_date()));
        TotalAmountCol.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getTotal_amount()));
        IsInstallmentCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getIs_installmenat()));

        // Initialize listeners only once
        if (!listenersInitialized) {
            yearBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && selectedStaffId != 0) {
                    currentYear = newVal;
                    // Update month box for the new year
                    user selectedStaff = staffInfoList.stream()
                            .filter(s -> s.getId() == selectedStaffId)
                            .findFirst()
                            .orElse(null);
                    if (selectedStaff != null) {
                        updateMonthBoxForYear(currentYear, selectedStaff);
                    }
                    updateYearMonthLabel();
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
            
            listenersInitialized = true;
        }

        //for car and parts of the show circle
       installmentPane.setVisible(false);
        targetlayer.setVisible(true);
        setTarget();
        setupSearchBar();
        
        // Set up staff image click handler
        StaffImage.setCursor(Cursor.HAND);
        StaffImage.setPickOnBounds(true);

    }


    private boolean cardtype = true;

    private LocalDate today = LocalDate.now();

    private int currentMonth;
    private int currentYear;

    private int managerId;
    private int selectedStaffId = 0;
    private boolean listenersInitialized = false;

    private List<user> staffInfoList = new ArrayList<user>();
    private List<File> file = new ArrayList<>();

    public managerStaffViewController() throws SQLException, ClassNotFoundException, IOException {

    }

    Porsche_DB db = new Porsche_DB();
    Connection con = db.connect();


    // for staff of information like email phone address etc
    private void showStaffDetails(user staff) {
        StaffNameLable.setText(staff.getUsername());
        StaffPhoneLabel.setText(staff.getPhone());
        StaffEmailLabel.setText(staff.getEmail());
        StaffAddressLabel.setText(staff.getAddress());
        StaffDOBLabel.setText(staff.getDob().toString());

//        StaffImage.setImage(new Image(staff.getImagePath()));

        highlightSelectedCard(staff.getId());
        selectedStaffId = staff.getId();

        currentDateSelect();
        try {
            insertMonthYearChoiceBox(staff);
            updateChoiceBoxes();
            refreshOrdersTable();
            monthlyOrdersStatus(selectedStaffId, currentMonth, currentYear);
            setTarget();
            monthlyAttendance();
        } catch (
                SQLException e) {
            throw new RuntimeException(e);
        }

        targetlayer.setVisible(true);
        installmentPane.setVisible(false);
    }

    //for the staff of monthly sold out the order table
    private void refreshOrdersTable() throws SQLException {
        if (selectedStaffId != 0) {
            ordersTable.getItems().clear();
            showOrdersTable(selectedStaffId, currentMonth, currentYear);

            ordersTable.getSelectionModel().clearSelection();
            ordersTable.refresh();
        }
    }

    private void showOrdersTable(int staffId, int month, int year) throws SQLException {
        CallableStatement cs = null;
        ResultSet rs = null;
        try {
            List<managerOrderView> managerordersList = new ArrayList<>();
            cs = con.prepareCall("CALL getOrdersByUserId(?,?,?)");
            cs.setInt(1, staffId);
            cs.setInt(2, month);
            cs.setInt(3, year);
            rs = cs.executeQuery();

            while (rs.next()) {
                int no = rs.getInt(1);
                int order_id = rs.getInt(2);
                String cus_name = rs.getString(3);
                Date date = rs.getDate(4);
                Double total_amount = rs.getDouble(5);
                boolean installment = rs.getBoolean(6);
                String carsandparts_name = rs.getString(7);
                String carsandparts_qty = rs.getString(8);
                String carsandparts_price = rs.getString(9);
                Double payed_amount = rs.getDouble(10);
                Double remain_amount = rs.getDouble(11);
                Date due_date = rs.getDate(12);

                String is_installment = installment ? "Yes" : "No";

                managerOrderView od = new managerOrderView(no, order_id, cus_name, date, total_amount, 
                    is_installment, carsandparts_name, carsandparts_qty, carsandparts_price, 
                    payed_amount, remain_amount, due_date);

                managerordersList.add(od);
            }

            ordersTable.getItems().addAll(managerordersList);
        } finally {
            if (rs != null) rs.close();
            if (cs != null) cs.close();
        }
    }

    //to see like a slip of the order table
    private void orderDetails(managerOrderView orders) throws IOException {
        targetlayer.setVisible(false);
        installmentPane.setVisible(true);

        // Load order details
        String[] names = orders.getCarsandparts_name();
        String[] qty = orders.getCarsandparts_qty();
        String[] price = orders.getCarsandparts_perprice();

        totalPriceLabel.setText("$" + String.format("%.2f", orders.getTotal_amount()));
        dueDateLabel.setText(String.valueOf(orders.getDue_date()));
        remainAmountLabel.setText("$" + String.format("%.2f", orders.getRemain_amount()));
        paidAmountLabel.setText("$" + String.format("%.2f", orders.getPayed_amount()));

        // Clear and populate the installment table
        installmentTable.getItems().clear();

        for (int i = 0; i < names.length; i++) {
            // Create a formatted string for the table row (no need to trim if data is clean)
            String rowData = String.format("%s|%s|%s", names[i].trim(), qty[i].trim(), price[i].trim());
            installmentTable.getItems().add(rowData);
        }

        // Set up table columns to display the data properly
        installmentNameCol.setCellValueFactory(cellData -> {
            String[] parts = cellData.getValue().split("\\|");
            return new SimpleStringProperty(parts.length > 0 ? parts[0] : "");
        });

        installmentQtyCol.setCellValueFactory(cellData -> {
            String[] parts = cellData.getValue().split("\\|");
            return new SimpleStringProperty(parts.length > 1 ? parts[1] : "");
        });

        installmentPriceCol.setCellValueFactory(cellData -> {
            String[] parts = cellData.getValue().split("\\|");
            return new SimpleStringProperty(parts.length > 2 ? parts[2] : "");
        });
    }

    // for monthly order status box
    private void monthlyOrdersStatus(int staffid, int month, int year) throws SQLException {
        CallableStatement cs = null;
        ResultSet rs = null;
        try {
            cs = con.prepareCall("CALL getMonthlyOrderStatus(?,?,?)");
            cs.setInt(1, staffid);
            cs.setInt(2, month);
            cs.setInt(3, year);

            rs = cs.executeQuery();

            if (rs.next()) {
                TotalOrderlbl.setText(String.valueOf(rs.getInt(1)));
                CompleOrderlbl.setText(String.valueOf(rs.getInt(2)));
                PendOrderlbl.setText(String.valueOf(rs.getInt(3)));
            }
        } finally {
            if (rs != null) rs.close();
            if (cs != null) cs.close();
        }
    }

    // for staff card add and highlight the select card
    private void highlightSelectedCard(int staffId) {
        // Reset all cards to default style
        for (Node node : staffListContainer.getChildren()) {
            node.setStyle(" -fx-border-color: transparent;");
        }

        // Highlight selected card
        for (Node node : staffListContainer.getChildren()) {
            if (node.getUserData() != null && node.getUserData() instanceof Integer) {
                if ((int) node.getUserData() == staffId) {
                    node.setStyle("-fx-background-color: #e8f0fe; -fx-border-color: #1a73e8; -fx-border-radius: 8; -fx-background-radius: 8;");
                    break;
                }
            }
        }
    }

    private void addStaffCard(boolean check) throws IOException, SQLException {
        staffListContainer.getChildren().clear();
        staffInfoList.clear();

        CallableStatement cs = null;
        ResultSet rs = null;
        
        try {
            cs = con.prepareCall("CALL createCards(?,?)");
            cs.setInt(1, managerId);
            cs.setString(2, check ? "active" : "inactive");

            rs = cs.executeQuery();

            while (rs.next()) {
            int id = rs.getInt("user_id");
            String name = rs.getString("user_name");
            String phone = rs.getString("user_phone");
            String email = rs.getString("user_email");
            String address = rs.getString("user_address");
            String dob = rs.getString("dob");
            String status = rs.getInt("user_status") == 1 ? "Active" : "Inactive";
            java.sql.Date sqlStart = rs.getDate("start_date");
            java.sql.Date sqlEnd = rs.getDate("end_date");
            String reason = rs.getString("reason");

            LocalDate str = (sqlStart != null) ? sqlStart.toLocalDate() : null;
            LocalDate end = (sqlEnd != null) ? sqlEnd.toLocalDate() : null;

            user staff = new user(id, name, phone, email, address, LocalDate.parse(dob), status, str, end, reason);
            staffInfoList.add(staff);

            // Use cached loader or create new one
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/userCards.fxml"));
            Node staffCard = loader.load();

            cardController cardController = loader.getController();

            staffCard.setUserData(staff.getId());

            cardController.setData(
                    staff.getId(),
                    staff.getUsername(),
                    staff.getIs_active()
            );

            final user currentStaff = staff;
            staffCard.setOnMouseClicked(event -> {
                showStaffDetails(currentStaff);
            });
            staffListContainer.getChildren().add(staffCard);
            }

            boolean selectedExists = false;
        for (user u : staffInfoList) {
            if (u.getId() == selectedStaffId) {
                selectedExists = true;
                break;
            }
        }
        if (!selectedExists) {
            if (!staffInfoList.isEmpty()) {
                selectedStaffId = staffInfoList.get(0).getId();
            } else {
                selectedStaffId = 0; // nothing to select
            }
        }

        // If we now have a valid selectedStaffId, show its details.
        if (selectedStaffId != 0) {
            for (user staffInfo : staffInfoList) {
                if (staffInfo.getId() == selectedStaffId) {
                    showStaffDetails(staffInfo);
                    break;
                }
            }
        } else {
            // no staff: clear details and orders
            StaffNameLable.setText("");
            StaffPhoneLabel.setText("");
            StaffEmailLabel.setText("");
            StaffAddressLabel.setText("");
            StaffDOBLabel.setText("");
            ordersTable.getItems().clear();
            }
        } finally {
            if (rs != null) rs.close();
            if (cs != null) cs.close();
        }
    }

    //target side
    private void setTarget() throws SQLException {
        CallableStatement cs = null;
        ResultSet rs = null;
        try {
            cs = con.prepareCall("CALL targetviewchart(?,?,?)");
            cs.setInt(1, selectedStaffId);
            cs.setInt(2, currentMonth);
            cs.setInt(3, currentYear);

            rs = cs.executeQuery();
            if (rs.next()) {

                int target_car = rs.getInt(4);
                int target_part = rs.getInt(5);
                int achieve_car = rs.getInt(6);
                int achieve_part = rs.getInt(7);

                setCarCircle(target_car, achieve_car);
                setPartCircle(target_part, achieve_part);
            }
        } finally {
            if (rs != null) rs.close();
            if (cs != null) cs.close();
        }
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
        carCircle.setRotate(-90); // Start from top
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
        partCircle.setStrokeDashOffset(circulerPart * (1 - progressPart));
        partCircle.setRotate(-90); // Start from top
    }

    // for monthly attendance circle
    private void monthlyAttendance() throws SQLException {
        CallableStatement cs = null;
        ResultSet rs = null;
        try {
            attendanceBackCircle.setVisible(true);
            attendanceCircle.setVisible(true);
            
            cs = con.prepareCall("CALL getMonthlyAttendance(?,?,?)");
            cs.setInt(1, selectedStaffId);
            cs.setInt(2, currentMonth);
            cs.setInt(3, currentYear);

            rs = cs.executeQuery();

            if (rs.next()) {
                int present_day = rs.getInt(1);
                int absent_day = rs.getInt(2);
                double attendance_percentage = rs.getDouble(4);

                // Use attendanceCircle's radius
                double attendCircle = 2 * Math.PI * attendanceCircle.getRadius();
                double progress = attendance_percentage / 100;

                // Set up the dash array for smooth circular progress
                attendanceCircle.getStrokeDashArray().setAll(attendCircle, attendCircle);
                attendanceCircle.setStrokeDashOffset(attendCircle * (1 - progress));
                attendanceCircle.setRotate(-90); // Start from top

                // Show/hide circles based on data
                attendanceCircle.setVisible(present_day > 0);
                attendanceBackCircle.setVisible(absent_day > 0);

                attendancePercent.setText(String.format("%.1f%%", attendance_percentage));
            }
        } finally {
            if (rs != null) rs.close();
            if (cs != null) cs.close();
        }
    }
    // for search bar
    private ContextMenu searchSuggestions = new ContextMenu();

    private void setupSearchBar() {
        // Set up key listener for Enter key
        StaffSearchText.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleSearch();
            }
        });

        // Set up search button click handler
        SearchNamebtn.setOnMouseClicked(event -> {
            handleSearch();
        });

        // Set up text change listener for suggestions
        StaffSearchText.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.isEmpty()) {
                searchSuggestions.hide();
                return;
            }

            // Clear previous suggestions
            searchSuggestions.getItems().clear();

            // Find matching staff
            List<MenuItem> matches = new ArrayList<>();
            for (user staff : staffInfoList) {
                String searchText = newText.toLowerCase();
                String staffId = String.valueOf(staff.getId()).toLowerCase();
                String staffName = staff.getUsername().toLowerCase();

                if (staffId.contains(searchText) || staffName.contains(searchText)) {
                    String suggestionText = staff.getId() + " - " + staff.getUsername();
                    MenuItem item = new MenuItem(suggestionText);

                    // Set action for when suggestion is clicked
                    item.setOnAction(e -> {
                        StaffSearchText.setText(suggestionText);
                        searchSuggestions.hide();
                        showStaffDetails(staff);
                    });

                    matches.add(item);
                }
            }

            // Show suggestions if matches found
            if (!matches.isEmpty()) {
                searchSuggestions.getItems().addAll(matches);
                searchSuggestions.show(StaffSearchText, Side.BOTTOM, 0, 0);
            } else {
                searchSuggestions.hide();
            }
        });

        // Hide suggestions when text field loses focus
        StaffSearchText.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                searchSuggestions.hide();
            }
        });
    }

    private void handleSearch() {
        String searchText = StaffSearchText.getText().trim();
        if (searchText.isEmpty()) {
            return;
        }

        // Try to find a matching staff member
        for (user staff : staffInfoList) {
            String staffId = String.valueOf(staff.getId());
            String staffName = staff.getUsername();

            // Check if search text matches ID or name
            if (searchText.equalsIgnoreCase(staffId) ||
                    searchText.equalsIgnoreCase(staffName) ||
                    searchText.equalsIgnoreCase(staffId + " - " + staffName)) {

                // Show staff details
                showStaffDetails(staff);
                return;
            }
        }
    }

    private void updateYearMonthLabel() {
        Year nyear = Year.of(currentYear);
        int curyear = today.getYear();
        targetlayer.setVisible(true);
        installmentPane.setVisible(false);

        Month nmonth = Month.of(currentMonth);
        String formattedMonth = nmonth.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

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

        if (!staffInfoList.isEmpty()) {
            user selected = staffInfoList.stream()
                    .filter(s -> s.getId() == selectedStaffId)
                    .findFirst()
                    .orElse(null);

            if (selected != null && selected.getStart_date() != null) {
                LocalDate start = selected.getStart_date();



                if (currentYear <= start.getYear()) {
                    PreviousYearbth.setDisable(true);
                    PreviousYearbth.setVisible(false);


                    if (currentYear == start.getYear() && currentMonth <= start.getMonthValue()) {
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
            }
        }


        // 🔹 Sync ComboBoxes with updated currentMonth/currentYear
        String monthName = nmonth.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        if (monthBox.getItems().contains(monthName)) {
            monthBox.setValue(monthName);
        }

        if (yearBox.getItems().contains(currentYear)) {
            yearBox.setValue(currentYear);
        }

        try {
            refreshOrdersTable();
            monthlyOrdersStatus(selectedStaffId, currentMonth, currentYear);
            setTarget();
            monthlyAttendance();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private void currentDateSelect() {
        currentMonth = today.getMonthValue();
        currentYear = today.getYear();
//        Monthslabel.setText(today.format(fmonth));
        NextMonthbtn.setDisable(true);
        NextMonthbtn.setVisible(false);
//        Yearslabel.setText(today.format(fyear));
        NextYearbtn.setDisable(true);
        NextYearbtn.setVisible(false);

    }

    private void insertMonthYearChoiceBox(user staff) {
        yearBox.getItems().clear();
        LocalDate end = (staff.getEnd_date() != null) ? staff.getEnd_date() : today;

        for (int y = staff.getStart_date().getYear(); y <= end.getYear(); y++) {
            yearBox.getItems().add(y);
        }

        yearBox.setValue(currentYear);
        updateMonthBoxForYear(currentYear, staff);
    }


    private boolean updatingMonthBox = false;

    private void updateMonthBoxForYear(int year, user staff) {
        int startMonth = 1;
        int endMonth = 12;

        if (year == staff.getStart_date().getYear()) startMonth = staff.getStart_date().getMonthValue();
        if (staff.getEnd_date() != null && year == staff.getEnd_date().getYear()) {
            endMonth = staff.getEnd_date().getMonthValue();
        } else if (year == today.getYear() && staff.getEnd_date() == null) {
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

    private void updateChoiceBoxes() {
        String monthName = Month.of(currentMonth).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        if (!monthBox.getItems().contains(monthName) && !monthBox.getItems().isEmpty()) {
            monthName = monthBox.getItems().get(0);
            currentMonth = Month.valueOf(monthName.toUpperCase(Locale.ENGLISH)).getValue();
        }
        monthBox.setValue(monthName);
        yearBox.setValue(currentYear);
    }

    private void handleImageSelection(ImageView targetImageView) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(targetImageView.getScene().getWindow());
        if (selectedFile != null) {
            try {
                Image image = new Image(new FileInputStream(selectedFile));
                targetImageView.setImage(image);
                file.clear();
                file.add(selectedFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
