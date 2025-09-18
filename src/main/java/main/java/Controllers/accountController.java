package main.java.Controllers;

import main.java.Database.Porsche_DB;
import main.java.Model.order;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import main.java.Model.Staff_info;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class accountController {

    @FXML
    private Label StaffListTitleLabel;

    @FXML
    private Button ActiveInactiveSwitchbtn;

    @FXML
    private TableColumn<order, Double> AmountCol;

    @FXML
    private Label CancelAmountLabel;

    @FXML
    private Label CompleAmountLabel;

    @FXML
    private TableColumn<order, String > CustomerNameCol;

    @FXML
    private TableColumn<order, Date> DateCol;

    @FXML
    private Label IsInstallPaidAmountLabel;

    @FXML
    private Label IsInstallRemainAmountLabel;

    @FXML
    private BorderPane IsInstallmentBorderPane;

    @FXML
    private TableColumn<order, String> IsInstallmentCol;

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
    private TableColumn<order, Integer> NoCol;

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
    private TableView<order> ordersTable;

    @FXML
    private VBox staffListContainer;

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
                StaffListTitleLabel.setText("Staff List (InActive)");
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

    public accountController() throws SQLException, ClassNotFoundException, IOException {

    }

    Porsche_DB db = new Porsche_DB();
    Connection con = db.connect();


    @FXML
    private void  initialize() throws SQLException, IOException {

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


    private void showStaffDetails(Staff_info staff) {
        StaffNameLable.setText(staff.getStaff_name());
        StaffPhoneLabel.setText(staff.getStaff_phone());
        StaffEmailLabel.setText(staff.getStaff_email());
        StaffAddressLabel.setText(staff.getStaff_address());
        StaffDOBLabel.setText(staff.getStaff_dob().toString());

//        StaffImage.setImage(new Image(staff.getImagePath()));

        highlightSelectedCard(staff.getStaff_id());
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
            Date dob = rs.getDate("dob");
            boolean status = rs.getBoolean("user_status");


            Staff_info staff = new Staff_info(id, name, phone, email, address, dob, status);
            staffInfoList.add(staff);


            File fxmlFile = new File("src/main/resources/manager_dashboard/manager_staffcards.fxml");

            FXMLLoader loader = new FXMLLoader(fxmlFile.toURI().toURL());
            Node staffCard = loader.load();

            cardController cardController = loader.getController();

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
