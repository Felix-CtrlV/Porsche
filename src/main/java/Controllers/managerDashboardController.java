package Controllers;

import Database.Porsche_DB;
import MainUI.login;
import Utils.Session;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.Objects;

public class managerDashboardController {

    @FXML
    private ToggleButton Mode;

    @FXML
    private AnchorPane admin_anc;

    @FXML
    private Label backButton;

    @FXML
    private ImageView img;

    @FXML
    private Button inventorybtn;

    @FXML
    private Button logoutbtn;

    @FXML
    private Label nameLbl;

    @FXML
    private HBox optionChange;

    @FXML
    private HBox optionProfile;

    @FXML
    private HBox optionTarget;

    @FXML
    private Button orderbtn;

    @FXML
    private Pane overlayPane;

    @FXML
    private Button overviewbtn;

    @FXML
    private ImageView pfImage;

    @FXML
    private Label profileAddress;

    @FXML
    private Label profileDOB;

    @FXML
    private Label profileEmail;

    @FXML
    private Label profileName;

    @FXML
    private AnchorPane profilePane;

    @FXML
    private BorderPane root;

    @FXML
    private Label settingIcon;

    @FXML
    private AnchorPane settingPane;

    @FXML
    private Button staffbtn;

    private Button activeButton;

    private int adminid;

    @FXML
    void clickInventory(ActionEvent event) {
        loadView("");
        setActiveButton(inventorybtn);
    }

    @FXML
    void clickOrders(ActionEvent event) {
        loadView("/View/managerOrderManagement.fxml");
        setActiveButton(orderbtn);
    }

    @FXML
    void clickOverview(ActionEvent event) {
        loadView("/View/managerOverview.fxml");
        setActiveButton(overviewbtn);
    }

    @FXML
    void clickStaffs(ActionEvent event) {
        loadView("/View/managerStaffview.fxml");
        setActiveButton(staffbtn);
    }

    private void clickSetting(){
        openSetting();
        overlayPane.setOnMouseClicked(e->{
            closeSetting();
        });

    }


    public void initialize() throws IOException {

        optionChange.setOnMouseClicked(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/Authentication.fxml"));
                Parent root = loader.load();
                authenticationController controller = loader.getController();
                controller.setStep("factor");
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.initStyle(StageStyle.UNDECORATED);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });


        backButton.setOnMouseClicked(e->{
            profilePane.setVisible(false);
        });

        optionProfile.setOnMouseClicked(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/Authentication.fxml"));
                Parent root = loader.load();
                authenticationController controller = loader.getController();
                controller.setStep("password");
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.initStyle(StageStyle.UNDECORATED);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });


        settingPane.setTranslateX(350);
        overlayPane.setVisible(false);
        settingIcon.setOnMouseClicked(e->{clickSetting();});

        Session current = Session.getInstance();
        String name = current.getUsername();
        nameLbl.setText(name);
        profileName.setText(name);
        profileAddress.setText(current.getAddress());
        profileEmail.setText(current.getEmail());
        profileDOB.setText(current.getDob().toString());

        Image porsche_logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Image/porsche_logo.png")));
        img.setImage(porsche_logo);
        loadView("/View/managerOverview.fxml");

        setActiveButton(overviewbtn);
        admin_anc.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((obsWin, oldWin, newWin) -> {
                    if (newWin != null) {
                        Stage stage = (Stage) newWin;
                        stage.setOnCloseRequest(event -> {
                            try {
                                Porsche_DB connect = new Porsche_DB();
                                Connection con = connect.connect();
                                CallableStatement check_out = con.prepareCall("call logout(?, ?)");
                                if (current != null) {
                                    check_out.setInt(1, current.getUserid());
                                    check_out.setString(2, String.valueOf(LocalDateTime.now()));
                                    check_out.execute();
                                }
                                connect.disconnect();
                                Session.clearSession();
                            } catch (
                                    Exception ex) {
                                ex.printStackTrace();
                            }
                        });
                    }
                });
            }
        });
    }

    private void loadView(String fxmlPath) {
        Task<Parent> task = new Task<>() {
            @Override
            protected Parent call() throws Exception {
                return FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
            }
        };
        task.setOnSucceeded(e -> {
            Parent pane = task.getValue();
            admin_anc.getChildren().setAll(pane);
            AnchorPane.setTopAnchor(pane, 0.0);
            AnchorPane.setBottomAnchor(pane, 0.0);
            AnchorPane.setLeftAnchor(pane, 0.0);
            AnchorPane.setRightAnchor(pane, 0.0);
        });
        task.setOnFailed(e -> task.getException().printStackTrace());
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    @FXML
    void clickLogout(ActionEvent event) throws Exception {
        Stage home = (Stage) ((Node) event.getSource()).getScene().getWindow();
        home.close();

        Porsche_DB connect = new Porsche_DB();
        Connection con = connect.connect();
        CallableStatement check_out = con.prepareCall("call logout(?, ?)");
        Session current = Session.getInstance();
        if (current != null) {
            adminid = current.getUserid();
        }
        check_out.setInt(1, adminid);
        check_out.setString(2, String.valueOf(LocalDateTime.now()));
        check_out.execute();
        login log_in = new login();
        log_in.start(new Stage());
        Session.clearSession();
    }


    private void setActiveButton(Button button) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("active");
        }
        if (!button.getStyleClass().contains("active")) {
            button.getStyleClass().add("active");
        }
        activeButton = button;
    }

    private SequentialTransition animation;

    private void openSetting(){
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), settingPane);
        slideIn.setFromX(350);
        slideIn.setToX(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), settingPane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        FadeTransition dimIn = new FadeTransition(Duration.millis(300), overlayPane);
        dimIn.setFromValue(0);
        dimIn.setToValue(0.5);

        ParallelTransition show = new ParallelTransition(slideIn, fadeIn, dimIn);
        animation = new SequentialTransition(show);
        animation.play();
        GaussianBlur blur = new GaussianBlur(10);
        root.setEffect(blur);
        root.setDisable(true);
        overlayPane.setVisible(true);
    }
    private void closeSetting(){
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), settingPane);
        slideOut.setFromX(0);
        slideOut.setToX(350);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(600), settingPane);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        FadeTransition dimOut = new FadeTransition(Duration.millis(300), overlayPane);
        dimOut.setFromValue(0.5);
        dimOut.setToValue(0);

        ParallelTransition hide = new ParallelTransition(slideOut, fadeOut, dimOut);

        animation = new SequentialTransition(hide);
        animation.play();
        root.setEffect(null);
        root.setDisable(false);
        overlayPane.setVisible(false);
    }

}
