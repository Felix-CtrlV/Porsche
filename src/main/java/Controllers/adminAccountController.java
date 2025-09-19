package Controllers;

import Database.Porsche_DB;
import Model.user;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class adminAccountController {

    @FXML
    private Label StaffListTitleLabel;

    @FXML
    private Button ActiveInactiveSwitchbtn;

    @FXML
    private Label StaffAddressLabel, StaffDOBLabel, StaffEmailLabel, StaffNameLable, StaffPhoneLabel;

    @FXML
    private ImageView StaffImage;

    @FXML
    private VBox staffListContainer;

    private boolean cardtype = true;
    private List<user> staffInfoList = new ArrayList<user>();

    private Porsche_DB db = new Porsche_DB();
    private Connection con;

    public adminAccountController() throws SQLException, ClassNotFoundException {
        con = db.connect();
    }

    @FXML
    private void initialize() throws SQLException, IOException {
        StaffListTitleLabel.setText("Users (Active)");
        loadStaffCards(cardtype);
    }

    @FXML
    void SwitchMouseClick(MouseEvent event) throws SQLException, IOException {
        cardtype = !cardtype;
        StaffListTitleLabel.setText(cardtype ? "Users (Active)" : "Users (Inactive)");
        loadStaffCards(cardtype);
    }

    private void loadStaffCards(boolean active) throws SQLException, IOException {
        staffListContainer.getChildren().clear();
        staffInfoList.clear();

        String status = active ? "active" : "inactive";
        PreparedStatement generate = con.prepareStatement("select user_id, user_name, user_phone, user_email, user_address, dob, user_status from user_info order by user_status desc");
        ResultSet rs = generate.executeQuery();

        while (rs.next()) {
            int id = rs.getInt("user_id");
            String name = rs.getString("user_name");
            String phone = rs.getString("user_phone");
            String email = rs.getString("user_email");
            String address = rs.getString("user_address");
            String dob = rs.getString("dob");
            String userStatus = rs.getInt("user_status") == 1 ? "Active" : "Inactive";

            user staff = new user(id, name, phone, email, address, LocalDate.parse(dob), userStatus);
            staffInfoList.add(staff);

            Node card = createStaffCard(staff);
            staffListContainer.getChildren().add(card);
        }
        rs.close();
        generate.close();
        if (!staffInfoList.isEmpty()) {
            showStaffDetails(staffInfoList.get(0));
            highlightSelectedCard(staffInfoList.get(0).getId());
        }
    }

    private Node createStaffCard(user staff) throws IOException {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/View/userCards.fxml")));
        Node staffCard = loader.load();

        cardController cardCtrl = loader.getController();
        cardCtrl.setData(
                staff.getId(),
                staff.getUsername(),
                staff.getIs_active()
        );

        staffCard.setUserData(staff.getId());
        staffCard.setOnMouseClicked(event -> {
            showStaffDetails(staff);
        });

        return staffCard;
    }

    private void showStaffDetails(user staff) {
        StaffNameLable.setText(staff.getUsername());
        StaffPhoneLabel.setText(staff.getPhone());
        StaffEmailLabel.setText(staff.getEmail());
        StaffAddressLabel.setText(staff.getAddress());
        StaffDOBLabel.setText(staff.getDob() != null ? staff.getDob().toString() : "");

        highlightSelectedCard(staff.getId());
    }

    private void highlightSelectedCard(int staffId) {
        for (Node node : staffListContainer.getChildren()) {
            node.setStyle("-fx-border-color: transparent;");
        }

        for (Node node : staffListContainer.getChildren()) {
            if (node.getUserData() instanceof Integer && (int) node.getUserData() == staffId) {
                node.setStyle("-fx-background-color: #e8f0fe; -fx-border-color: #1a73e8; -fx-border-radius: 8; -fx-background-radius: 8;");
                break;
            }
        }
    }
}
