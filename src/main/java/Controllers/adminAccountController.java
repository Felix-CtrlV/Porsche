package Controllers;

import DAO.ChartDAO;
import DAO.userDAO;
import Database.Porsche_DB;
import Model.user;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class adminAccountController {

    @FXML
    private Label StaffListTitleLabel;

    @FXML
    private Button ActiveInactiveSwitchbtn;

    @FXML
    private LineChart<?,?> linePerformance;

    @FXML
    private Label StaffAddressLabel, StaffDOBLabel, StaffEmailLabel, StaffNameLable, StaffPhoneLabel;

    @FXML
    private PieChart carTargetPie, partTargetPie;

    @FXML
    private Label carTarget, partTarget;

    @FXML
    private ImageView userImage, plusImage;

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

        plusImage.setOnMouseClicked(e-> {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/adminUserRegister.fxml"));
            Stage stage = new Stage();
            Scene scene;
            try {
                scene = new Scene(loader.load());
            } catch (
                    IOException ex) {
                throw new RuntimeException(ex);
            }
            stage.setScene(scene);
            stage.initStyle(StageStyle.UNDECORATED);
        });
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

        userDAO dao = new userDAO(con);

        staffInfoList = dao.accountData();

        for (user u : staffInfoList) {
            Node card = createStaffCard(u);
            staffListContainer.getChildren().add(card);
        }

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
