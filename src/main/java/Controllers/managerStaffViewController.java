package Controllers;

import Database.Porsche_DB;
import Model.Staff_info;
import Model.manager_orders;
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
import javafx.scene.layout.VBox;


import java.io.File;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class managerStaffViewController {

    @FXML
    private Label StaffListTitleLabel;

    @FXML
    private Button ActiveInactiveSwitchbtn;

    @FXML
    private TableColumn<manager_orders, Double> AmountCol;

    @FXML
    private Label CancelAmountLabel;

    @FXML
    private Label CompleAmountLabel;

    @FXML
    private TableColumn<manager_orders, String > CustomerNameCol;

    @FXML
    private TableColumn<manager_orders, Date> DateCol;

    @FXML
    private Label IsInstallPaidAmountLabel;

    @FXML
    private Label IsInstallRemainAmountLabel;

    @FXML
    private BorderPane IsInstallmentBorderPane;

    @FXML
    private TableColumn<manager_orders, String> IsInstallmentCol;

    @FXML
    private VBox IsInstallorderItemsContainer;

    @FXML
    private Label IsInstalltotalPriceLabel;

    @FXML
    private BorderPane IsNotInstallBorderPane;

    @FXML
    private PieChart MonthlyAttendancePieChart;

    @FXML
    private Label Monthslabel;

    @FXML
    private Button NextMonthbtn;

    @FXML
    private Button NextYearbtn;

    @FXML
    private TableColumn<manager_orders, Integer> NoCol;

    @FXML
    private Label NoInstallLabel;

    @FXML
    private VBox NoInstallorderItemsContainer;

    @FXML
    private Label PendAmountLabel;

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
    private Label TotalOrdersAmountLabel;

    @FXML
    private Label Yearslabel;

    @FXML
    private TableView<manager_orders> ordersTable;

    @FXML
    private VBox staffListContainer;

    @FXML
    private Label DueDateLabel;

    private boolean cardtype = true;

    @FXML
    void SwitchMouseClick(MouseEvent event) throws SQLException, IOException {
            if (cardtype){
                cardtype = false;
                StaffListTitleLabel.setText("Staff List (InActive)");
                addStaffCard(cardtype);

                int first_id = staffInfoList.getFirst().getStaff_id();
                for (Staff_info staffInfo : staffInfoList) {
                    if (staffInfo.getStaff_id() == first_id){
                        showStaffDetails(staffInfo);
                    }
                }
                highlightSelectedCard(first_id);
            }else{
                cardtype = true;
                StaffListTitleLabel.setText("Staff List (Active)");
                addStaffCard(cardtype);

                int first_id = staffInfoList.getFirst().getStaff_id();
                for (Staff_info staffInfo : staffInfoList) {
                    if (staffInfo.getStaff_id() == first_id){
                        showStaffDetails(staffInfo);
                    }
                }
                highlightSelectedCard(first_id);
            }
    }



    private List<Staff_info> staffInfoList = new ArrayList<>();

    public managerStaffViewController() throws SQLException, ClassNotFoundException, IOException {

    }

    Porsche_DB db = new Porsche_DB();
    Connection con = db.connect();


    @FXML
    private void  initialize() throws SQLException, IOException {

        //for inserting staff cards
        StaffListTitleLabel.setText("Staff List (Active)");
        addStaffCard(cardtype);

        int first_id = staffInfoList.getFirst().getStaff_id();
       for (Staff_info staffInfo : staffInfoList) {
            if (staffInfo.getStaff_id() == first_id){
                showStaffDetails(staffInfo);
            }
       }
        highlightSelectedCard(first_id);


       // for inserting orders
        NoCol.setCellValueFactory(d ->new ReadOnlyObjectWrapper<>(d.getValue().getNo()));
        CustomerNameCol.setCellValueFactory(d->new SimpleStringProperty(d.getValue().getCus_name()));
        DateCol.setCellValueFactory(d->new ReadOnlyObjectWrapper<>(d.getValue().getOrder_date()));
        AmountCol.setCellValueFactory(d-> new ReadOnlyObjectWrapper<>(d.getValue().getTotal_amount()));
        IsInstallmentCol.setCellValueFactory(d->new SimpleStringProperty(d.getValue().getIs_installmenat()));



    }


    private void showStaffDetails(Staff_info staff) {
        StaffNameLable.setText(staff.getStaff_name());
        StaffPhoneLabel.setText(staff.getStaff_phone());
        StaffEmailLabel.setText(staff.getStaff_email());
        StaffAddressLabel.setText(staff.getStaff_address());
        StaffDOBLabel.setText(staff.getStaff_dob().toString());

//        StaffImage.setImage(new Image(staff.getImagePath()));


        highlightSelectedCard(staff.getStaff_id());

//        ordersTable.getSelectionModel().selectedItemProperty().addListener(
//                (obs, oldSelection, newSelection) -> {
//                    if (newSelection != null) {
//                        try {
//                            showOrdersDetails(newSelection);
//                        } catch (SQLException e) {
//                            throw new RuntimeException(e);
//                        }
//                    }
//                }
//        );
    }

    private void showOrdersDetails(manager_orders managerorders) throws SQLException {
        List<manager_orders> managerordersList = new ArrayList<>();
        CallableStatement cs = con.prepareCall("");
        ResultSet rs = cs.executeQuery();

        while(rs.next()){
            int no = rs.getInt(1);
            int order_id = rs.getInt(2);
            String cus_name = rs.getString(3);
            Date date = rs.getDate(4);
            Double total_amount = rs.getDouble(5);
            String is_installment = rs.getString(6);
            String carsandparts_name = rs.getString(7);
            String carsandparts_qty = rs.getString(8);
            Double payed_amount = rs.getDouble(9);
            Double remain_amount = rs.getDouble(10);
            Date due_date = rs.getDate(11);

            manager_orders od = new manager_orders(no,order_id,cus_name,date,total_amount,is_installment,carsandparts_name,carsandparts_qty,payed_amount,remain_amount,due_date);

            managerordersList.add(od);
        }

    }


    private void highlightSelectedCard(int staffId) {
        // Reset all cards to default style
        for (Node node : staffListContainer.getChildren()) {
            node.setStyle(" -fx-border-color: transparent;");
        }

        // Highlight selected card
        for (Node node : staffListContainer.getChildren()) {
            if (node.getUserData() != null && node.getUserData() instanceof Integer) {
                if ((int) node.getUserData() == staffId) {
                    node.setStyle("-fx-background-color: #3A3A3A;\n" +
                            "    -fx-background-radius: 15px;\n" +
                            "    -fx-border-width: 0;\n" +
                            "    -fx-padding: 5px;\n" +
                            "    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0.3, 0, 2);");
                    break;
                }
            }
        }
    }

    private void addStaffCard(boolean check) throws IOException, SQLException {

        staffListContainer.getChildren().clear();
        staffInfoList.clear();

        CallableStatement cs = con.prepareCall("CALL staffcard(?)");
        if(check) {
            cs.setString(1, "active");
        }else{
            cs.setString(1,"inactive");
        }
        ResultSet rs = cs.executeQuery();

        while(rs.next()){
            int id = rs.getInt("user_id");

            String name = rs.getString("user_name");
            String phone = rs.getString("user_phone");
            String email = rs.getString("user_email");
            String address = rs.getString("user_address");
            Date dob = rs.getDate("dob");
            boolean status = rs.getBoolean("user_status");


            Staff_info staff = new Staff_info(id, name, phone, email, address, dob, status);
            staffInfoList.add(staff);


            File fxmlFile = new File("src/main/resources/View/managerStaffcards.fxml");

            FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
            Node staffCard = loader.load();

            managerStaffCardController cardController = loader.getController();

            staffCard.setUserData(staff.getStaff_id());


            cardController.setData(
                    staff.getStaff_id(),
                    staff.getStaff_name(),
                    staff.isStatus()
            );

            staffCard.setOnMouseClicked(event -> {
                showStaffDetails(staff);


            });

            staffListContainer.getChildren().add(staffCard);


        }
        rs.close();
        cs.close();
    }



}
