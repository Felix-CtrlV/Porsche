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
import javafx.scene.layout.VBox;


import java.io.File;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class managerStaffViewController {

    @FXML
    private Label StaffListTitleLabel;

    @FXML
    private Button ActiveInactiveSwitchbtn;

    @FXML
    private TableColumn<managerOrderViewStaff, Double> TotalAmountCol;

    @FXML
    private Label CancelAmountLabel;

    @FXML
    private Label CompleAmountLabel;

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
    private PieChart MonthlyAttendancePieChart;

    @FXML
    private Label Monthslabel;

    @FXML
    private Button NextMonthbtn;

    @FXML
    private Button NextYearbtn;

    @FXML
    private TableColumn<managerOrderViewStaff, Integer> NoCol;

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
    private TableView<managerOrderViewStaff> ordersTable;

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

                int first_id = staffInfoList.getFirst().getId();
                for (user staffInfo : staffInfoList) {
                    if (staffInfo.getId() == first_id){
                        showStaffDetails(staffInfo);
                    }
                }
                highlightSelectedCard(first_id);
            }else{
                cardtype = true;
                StaffListTitleLabel.setText("Staff List (Active)");
                addStaffCard(cardtype);

                int first_id = staffInfoList.getFirst().getId();
                for (user staffInfo : staffInfoList) {
                    if (staffInfo.getId() == first_id){
                        showStaffDetails(staffInfo);
                    }
                }
                highlightSelectedCard(first_id);
            }
    }



    private List<user> staffInfoList = new ArrayList<user>();

    public managerStaffViewController() throws SQLException, ClassNotFoundException, IOException {

    }

    Porsche_DB db = new Porsche_DB();
    Connection con = db.connect();


    @FXML
    private void  initialize() throws SQLException, IOException {

        //for inserting staff cards
        StaffListTitleLabel.setText("Staff List (Active)");
        addStaffCard(cardtype);

        int first_id = staffInfoList.getFirst().getId();
       for (user staffInfo : staffInfoList) {
            if (staffInfo.getId() == first_id){
                showStaffDetails(staffInfo);
            }
       }
        highlightSelectedCard(first_id);


       // for inserting orders
        NoCol.setCellValueFactory(d ->new ReadOnlyObjectWrapper<>(d.getValue().getNo()));
        CustomerNameCol.setCellValueFactory(d->new SimpleStringProperty(d.getValue().getCus_name()));
        DateCol.setCellValueFactory(d->new ReadOnlyObjectWrapper<>(d.getValue().getOrder_date()));
        TotalAmountCol.setCellValueFactory(d-> new ReadOnlyObjectWrapper<>(d.getValue().getTotal_amount()));
        IsInstallmentCol.setCellValueFactory(d->new SimpleStringProperty(d.getValue().getIs_installmenat()));



    }


    private void showStaffDetails(user staff) {
        StaffNameLable.setText(staff.getUsername());
        StaffPhoneLabel.setText(staff.getPhone());
        StaffEmailLabel.setText(staff.getEmail());
        StaffAddressLabel.setText(staff.getAddress());
        StaffDOBLabel.setText(staff.getDob().toString());

//        StaffImage.setImage(new Image(staff.getImagePath()));


        highlightSelectedCard(staff.getId());

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

    private void showOrdersDetails(managerOrderViewStaff managerorders) throws SQLException {
        List<managerOrderViewStaff> managerordersList = new ArrayList<>();
        CallableStatement cs = con.prepareCall("");
        ResultSet rs = cs.executeQuery();

        while(rs.next()){
            int no = rs.getInt(1);
            int order_id = rs.getInt(2);
            String cus_name = rs.getString(3);
            Date date = rs.getDate(4);
            Double total_amount = rs.getDouble(5);
            boolean installment = rs.getBoolean(6);
            String carsandparts_name = rs.getString(7);
            String carsandparts_qty = rs.getString(8);
            Double payed_amount = rs.getDouble(9);
            Double remain_amount = rs.getDouble(10);
            Date due_date = rs.getDate(11);

            String is_installment = "No" ;
            if (installment == true){
                is_installment = "Yes";
            }else{
                is_installment = "No";
            }

            managerOrderViewStaff od = new managerOrderViewStaff(no,order_id,cus_name,date,total_amount,is_installment,carsandparts_name,carsandparts_qty,payed_amount,remain_amount,due_date);

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
                    node.setStyle("-fx-background-color: #e8f0fe; -fx-border-color: #1a73e8; -fx-border-radius: 8; -fx-background-radius: 8;");
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

            staffCard.setOnMouseClicked(event -> {
                showStaffDetails(staff);
            });
            staffListContainer.getChildren().add(staffCard);
        }
        rs.close();
        cs.close();
    }
}
