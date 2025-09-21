package Controllers;

import Database.Porsche_DB;
import Model.managerOrderViewStaff;
import Model.user;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import jdk.jshell.EvalException;


import javax.xml.transform.Result;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
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
import org.controlsfx.control.textfield.TextFields;

public class managerStaffViewController {

    @FXML
    private Label StaffListTitleLabel;

    @FXML
    private Button ActiveInactiveSwitchbtn;

    @FXML
    private TableColumn<managerOrderViewStaff, Double> TotalAmountCol;

    @FXML
    private Label CancelOrderlbl;

    @FXML
    private Label CompleOrderlbl;

    @FXML
    private TableColumn<managerOrderViewStaff, String > CustomerNameCol;

    @FXML
    private TableColumn<managerOrderViewStaff, Date> DateCol;

    @FXML
    private Label IsInstallPaidAmountLabel;

    @FXML
    private Label IsInstallRemainAmountLabel;

    @FXML
    private BorderPane IsInstallmentBorderPane;

    @FXML
    private TableColumn<managerOrderViewStaff, String> IsInstallmentCol;

    @FXML
    private VBox IsInstallorderItemsContainer;

    @FXML
    private Label IsInstalltotalPriceLabel;

    @FXML
    private BorderPane IsNotInstallBorderPane;


    @FXML
    private Label Monthslabel;

    @FXML
    private Button NextMonthbtn;

    @FXML
    private Button NextYearbtn;

    @FXML
    private TableColumn<managerOrderViewStaff, Integer> NoCol;

    @FXML
    private Label NoInstallTotalPriceLabel;

    @FXML
    private VBox NoInstallorderItemsContainer;

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
    private Label Yearslabel;

    @FXML
    private TableView<managerOrderViewStaff> ordersTable;

    @FXML
    private VBox staffListContainer;

    @FXML
    private Label DueDateLabel;


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
    private HBox targetlayer;

    @FXML
    private Circle attendanceCircle;

    @FXML
    private Text attendancePercent;

    @FXML
    private Circle carCircle;

    @FXML
    void SwitchMouseClick(MouseEvent event) throws SQLException, IOException {
            if (cardtype){
                cardtype = false;
                StaffListTitleLabel.setText("Staff List (InActive)");
                addStaffCard(cardtype);

            }else{
                cardtype = true;
                StaffListTitleLabel.setText("Staff List (Active)");
                addStaffCard(cardtype);

            }
    }

    @FXML
    void nextMonthClick(MouseEvent event) {
        currentMonth++;
        if(currentMonth>12){ currentMonth=1 ; currentYear++;};
        updateYearMonthLabel();

    }

    @FXML
    void nextYearClick(MouseEvent event) {
        currentYear++;
        if(today.getYear() == currentYear){
            if (currentMonth> today.getMonthValue()){
                currentMonth = today.getMonthValue();
            }
        }
        updateYearMonthLabel();

    }

    @FXML
    void prevMonthClick(MouseEvent event) {
        currentMonth--;
        if (currentMonth<1){currentMonth =12 ; currentYear--;};
        updateYearMonthLabel();

    }

    @FXML
    void prevYearClick(MouseEvent event) {
        currentYear--;
        updateYearMonthLabel();

    }

    @FXML
    void ordersTableClick(MouseEvent event) throws IOException {
            managerOrderViewStaff selectorder = ordersTable.getSelectionModel().getSelectedItem();
            orderDetails(selectorder);
    }

    @FXML
    private void  initialize() throws SQLException, IOException {



        //for creating the month  and year of this month
        currentDateSelect();

        //for inserting staff cards
        StaffListTitleLabel.setText("Staff List (Active)");
        addStaffCard(cardtype);



        // for inserting orders
        NoCol.setCellValueFactory(d ->new ReadOnlyObjectWrapper<>(d.getValue().getNo()));
        CustomerNameCol.setCellValueFactory(d->new SimpleStringProperty(d.getValue().getCus_name()));
        DateCol.setCellValueFactory(d->new ReadOnlyObjectWrapper<>(d.getValue().getOrder_date()));
        TotalAmountCol.setCellValueFactory(d-> new ReadOnlyObjectWrapper<>(d.getValue().getTotal_amount()));
        IsInstallmentCol.setCellValueFactory(d->new SimpleStringProperty(d.getValue().getIs_installmenat()));

        //for car and parts of the show circle
        IsInstallmentBorderPane.setVisible(false);
        IsNotInstallBorderPane.setVisible(false);
        targetlayer.setVisible(true);
        carsAndPartsTarget();

    }



    private boolean cardtype = true;

    private LocalDate today = LocalDate.now();
    private DateTimeFormatter fmonth = DateTimeFormatter.ofPattern("MMM");

    private DateTimeFormatter fyear = DateTimeFormatter.ofPattern("yyyy");

    private int currentMonth ;
    private int currentYear ;

    private int selectedStaffId = 0;


    private List<user> staffInfoList = new ArrayList<user>();

    public managerStaffViewController() throws SQLException, ClassNotFoundException, IOException {

    }

    Porsche_DB db = new Porsche_DB();
    Connection con = db.connect();


    // for staff of information like email phone address etc
    private void showStaffDetails(user staff)  {
        StaffNameLable.setText(staff.getUsername());
        StaffPhoneLabel.setText(staff.getPhone());
        StaffEmailLabel.setText(staff.getEmail());
        StaffAddressLabel.setText(staff.getAddress());
        StaffDOBLabel.setText(staff.getDob().toString());

//        StaffImage.setImage(new Image(staff.getImagePath()));




        highlightSelectedCard(staff.getId());
        selectedStaffId = staff.getId();

        System.out.println(selectedStaffId);
        currentDateSelect();
        try {
            refreshOrdersTable();
            monthlyOrdersStatus(selectedStaffId,currentMonth,currentYear);
            carsAndPartsTarget();
            monthlyAttendance();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        };
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

    private void showOrdersTable( int staffId, int month ,int year) throws SQLException {
        List<managerOrderViewStaff> managerordersList = new ArrayList<>();
        CallableStatement cs = con.prepareCall("CALL getordersbyuserid(?,?,?)");
        cs.setInt(1,staffId);
        cs.setInt(2,month);
        cs.setInt(3,year);
        ResultSet rs = cs.executeQuery();

        ordersTable.getItems().clear();
        managerordersList.clear();

        while(rs.next()){
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

            String is_installment = "No" ;
            if (installment == true){
                is_installment = "Yes";
            }else{
                is_installment = "No";
            }

            managerOrderViewStaff od = new managerOrderViewStaff(no,order_id,cus_name,date,total_amount,is_installment,carsandparts_name,carsandparts_qty,carsandparts_price,payed_amount,remain_amount,due_date);

            managerordersList.add(od);

        }


        ordersTable.getItems().addAll(managerordersList);

//        if (!managerordersList.isEmpty()) {
//            ordersTable.getSelectionModel().select(0);
//        }
    }

    //to see like a slip of the order table
    private void orderDetails(managerOrderViewStaff orders) throws IOException {

        NoInstallorderItemsContainer.getChildren().clear();
        IsInstallorderItemsContainer.getChildren().clear();
        targetlayer.setVisible(false);
        //to load the orders detail in the page like slip
        String [] names = orders.getCarsandparts_name();
        String  [] qty = orders.getCarsandparts_qty();
        String [] price = orders.getCarsandparts_perprice();

        if (orders.getIs_installmenat().equalsIgnoreCase("yes")){


            IsNotInstallBorderPane.setVisible(false);
            IsInstallmentBorderPane.setVisible(true);
            IsInstalltotalPriceLabel.setText(String.valueOf(orders.getTotal_amount()));
            DueDateLabel.setText(String.valueOf(orders.getDue_date()));
            IsInstallRemainAmountLabel.setText(String.valueOf(orders.getRemain_amount()));
            IsInstallPaidAmountLabel.setText(String.valueOf(orders.getPayed_amount()));

            for (int i =0; i<names.length;i++){
                File fxmlFile = new File("src/main/resources/View/managerStaffInstallment.fxml");
                FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
                Node orderItem = loader.load();

                managerStaffInstallmentController controller = loader.getController();
                controller.setData(names[i], qty[i], price[i]);

                System.out.println(names[i] + " "+qty[i] +" "+ price[i]);
                IsInstallorderItemsContainer.getChildren().add(orderItem);

            }

        }else{


            IsNotInstallBorderPane.setVisible(true);
            IsInstallmentBorderPane.setVisible(false);
            NoInstallTotalPriceLabel.setText(String.valueOf(orders.getTotal_amount()));

            for (int i =0; i<names.length;i++){
                File fxmlFile = new File("src/main/resources/View/managerStaffInstallment.fxml");
                FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
                Node orderItem = loader.load();

                managerStaffInstallmentController controller = loader.getController();
                controller.setData(names[i], qty[i], price[i]);

                System.out.println(names[i] + " "+qty[i] +" "+ price[i]);
                NoInstallorderItemsContainer.getChildren().add(orderItem);

            }
        }


    }

    // for monthly order status box
    private void monthlyOrdersStatus(int staffid ,int month,int year) throws SQLException {

        CallableStatement cs = con.prepareCall("CALL monthlyorderstatus(?,?,?)");
        cs.setInt(1,staffid);
        cs.setInt(2,month);
        cs.setInt(3,year);

        ResultSet rs = cs.executeQuery();


        if (rs.next()){
            TotalOrderlbl.setText(String.valueOf(rs.getInt(1)));
            CompleOrderlbl.setText(String.valueOf(rs.getInt(2)));
            PendOrderlbl.setText(String.valueOf(rs.getInt(3)));
            CancelOrderlbl.setText(String.valueOf(rs.getInt(4)));
        }
        rs.close();
        cs.close();
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

        CallableStatement cs = con.prepareCall("CALL createcards(?,?,?,?)");
        if(check) {
            cs.setString(1, "active");
        }else{
            cs.setString(1,"inactive");
        }

        cs.setString(2,"manager");
        cs.setInt(3,currentMonth);
        cs.setInt(4,currentYear);

        ResultSet rs = cs.executeQuery();

        while(rs.next()){
            int id = rs.getInt("user_id");

            String name = rs.getString("user_name");
            String phone = rs.getString("user_phone");
            String email = rs.getString("user_email");
            String address = rs.getString("user_address");
            String dob = rs.getString("dob");
            String status = rs.getInt("user_status") == 1 ? "Active" : "Inactive";

            user staff = new user(id, name, phone, email, address, LocalDate.parse(dob), status);
            staffInfoList.add(staff);


            File fxmlFile = new File("src/main/resources/View/userCards.fxml");

            FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
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


        rs.close();
        cs.close();

    }


    // for month and year box
    private void updateYearMonthLabel(){
        Year nyear = Year.of(currentYear);
        int curyear = Integer.parseInt(today.format(fyear));

        Yearslabel.setText(nyear.format(fyear));

        Month nmonth = Month.of(currentMonth);
        String formattedMonth = nmonth.getDisplayName(TextStyle.SHORT, Locale.ENGLISH); // "Sep"

        int curmonth = today.getMonthValue();
        Monthslabel.setText(formattedMonth);

        if(currentYear>= curyear){

            NextYearbtn.setDisable(true);
            NextYearbtn.setVisible(false);
            if(currentMonth >= curmonth){
                NextMonthbtn.setDisable(true);
                NextMonthbtn.setVisible(false);

            }else{
                NextMonthbtn.setDisable(false);
                NextMonthbtn.setVisible(true);
            }
        }else{
            NextYearbtn.setDisable(false);
            NextYearbtn.setVisible(true);
            NextMonthbtn.setDisable(false);
            NextMonthbtn.setVisible(true);
        }


        try {
            refreshOrdersTable();
            monthlyOrdersStatus(selectedStaffId,currentMonth,currentYear);
            carsAndPartsTarget();
            monthlyAttendance();

        } catch ( SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void currentDateSelect(){
        currentMonth = today.getMonthValue();
        currentYear = today.getYear();
        Monthslabel.setText(today.format(fmonth));
        NextMonthbtn.setDisable(true);
        NextMonthbtn.setVisible(false);
        Yearslabel.setText(today.format(fyear));
        NextYearbtn.setDisable(true);
        NextYearbtn.setVisible(false);

    }


    // for car and part of target  circle
    private void carsAndPartsTarget() throws SQLException {
        IsNotInstallBorderPane.setVisible(false);
        IsInstallmentBorderPane.setVisible(false);
        targetlayer.setVisible(true);
        CallableStatement cs = con.prepareCall("CALL targetviewchart(?,?,?)");
        cs.setInt(1, selectedStaffId);
        cs.setInt(2, currentMonth);
        cs.setInt(3, currentYear);
        ResultSet rs = cs.executeQuery();
        if (rs.next()) {
            int target_car = rs.getInt(1);
            int target_part = rs.getInt(2);
            int achieve_car = rs.getInt(3);
            int achieve_part = rs.getInt(4);

            generate_carCircle(target_car,achieve_car);
            generate_partCircle(target_part,achieve_part);

        }
    }

    private void generate_carCircle(int target , int achieve){
        targetCar.setText(String.valueOf(achieve)+"/"+String.valueOf(target));


        int targetOverC = 0;
        if(achieve == 0 && target ==0){
            targetOverCar.setText("0");
            double progressCar = (target > 0) ? (double) achieve / target : 0;
            double circulerCar = 2 * Math.PI * carCircle.getRadius();
            carCircle.getStrokeDashArray().setAll(circulerCar, circulerCar);
            carCircle.setStrokeDashOffset(circulerCar * (1 - progressCar));
            targetCarMessagelbl.setText("No Target");

        }else if(achieve>=target){
            targetOverC = achieve - target;
            targetOverCar.setText("+"+String.valueOf(targetOverC));

            if(achieve >target){
                double progressCar = (target > 0) ? (double) achieve / achieve : 0;
                double circulerCar = 2 * Math.PI * carCircle.getRadius();
                carCircle.getStrokeDashArray().setAll(circulerCar, circulerCar);
                carCircle.setStrokeDashOffset(circulerCar * (1 - progressCar));
                targetCarMessagelbl.setText("Extra Bonous ");

            }else{
                double progressCar = (target > 0) ? (double) achieve / target : 0;
                double circulerCar = 2 * Math.PI * carCircle.getRadius();
                carCircle.getStrokeDashArray().setAll(circulerCar, circulerCar);
                carCircle.setStrokeDashOffset(circulerCar * (1 - progressCar));
                targetCarMessagelbl.setText("Hit the target");
            }


        }else{
            targetOverC = target - achieve;
            targetOverCar.setText("-"+String.valueOf(targetOverC));

            double progressCar = (target > 0) ? (double) achieve / target : 0;
            double circulerCar = 2 * Math.PI * carCircle.getRadius();
            carCircle.getStrokeDashArray().setAll(circulerCar, circulerCar);
            carCircle.setStrokeDashOffset(circulerCar * (1 - progressCar));
            targetCarMessagelbl.setText("Need to hit target");
        }




    }

    private void generate_partCircle(int target , int achieve){
        targetPart.setText(String.valueOf(achieve)+"/"+String.valueOf(target));


        int targetOverP = 0;
        if(target ==0 && achieve ==0){
            targetOverPart.setText("0");
            double progressPart = (target > 0) ? (double) achieve / target : 0;
            double circulerPart = 2 * Math.PI * partCircle.getRadius();
            partCircle.getStrokeDashArray().setAll(circulerPart, circulerPart);
            partCircle.setStrokeDashOffset(circulerPart * (1 - progressPart));
            targetPartMessagelbl.setText("No target");
        }else if(achieve>=target){
            targetOverP = achieve - target;
            targetOverPart.setText("+"+String.valueOf(targetOverP));

            if(achieve >target){
                double progressPart = (target > 0) ? (double) achieve / achieve : 0;
                double circulerPart = 2 * Math.PI * partCircle.getRadius();
                partCircle.getStrokeDashArray().setAll(circulerPart, circulerPart);
                partCircle.setStrokeDashOffset(circulerPart * (1 - progressPart));
                targetPartMessagelbl.setText("Extra Bonous ");

            }else{
                double progressPart = (target > 0) ? (double) achieve / target : 0;
                double circulerPart = 2 * Math.PI * partCircle.getRadius();
                partCircle.getStrokeDashArray().setAll(circulerPart, circulerPart);
                partCircle.setStrokeDashOffset(circulerPart * (1 - progressPart));
                targetPartMessagelbl.setText("Hit the target");
            }


        }else{
            targetOverP = target - achieve;
            targetOverPart.setText("-"+String.valueOf(targetOverP));

            double progressPart = (target > 0) ? (double) achieve / target : 0;
            double circulerPart = 2 * Math.PI * partCircle.getRadius();
            partCircle.getStrokeDashArray().setAll(circulerPart, circulerPart);
            partCircle.setStrokeDashOffset(circulerPart * (1 - progressPart));
            targetPartMessagelbl.setText("Need to hit target");
        }




    }

    // for monthly attendance circle
    private void monthlyAttendance() throws SQLException {
            CallableStatement cs = con.prepareCall("CALL getMonthlyAttendance(?,?,?)");
            cs.setInt(1,selectedStaffId);
            cs.setInt(2,currentMonth);
            cs.setInt(3,currentYear);

            ResultSet rs = cs.executeQuery();

            if(rs.next()){
                int present_day = rs.getInt(1);
                int absent_day = rs.getInt(2);
                double attendance_perentage = rs.getDouble(4);

                double attendCircle = 2 * Math.PI * partCircle.getRadius();
                double progress = attendance_perentage/100;
                attendanceCircle.getStrokeDashArray().setAll(attendCircle, attendCircle);
                attendanceCircle.setStrokeDashOffset(attendCircle * (1-progress));
                attendancePercent.setText(String.valueOf(attendance_perentage)+"%");

                if(absent_day ==0){
                    attendanceBackCircle.setStrokeDashOffset(0);
                }
            }

    }

    // for search bar
    private void searchBar(){
        TextFields.bindAutoCompletion(StaffSearchText, staffInfoList);
    }
}
