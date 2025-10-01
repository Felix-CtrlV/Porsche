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
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class adminDashboardController {

    @FXML
    private HBox optionProfile, optionTarget, optionChange;

    @FXML
    private Label profileName, profileDOB, backButton, editButton, editEmail, editAddress, editPhone, editConfirm, editRevert, editBack;

    @FXML
    private TextField profileAddress, profilePhone, profileEmail;

    @FXML
    private ImageView pfImage;

    private int adminid;

    @FXML
    private Label nameLbl;

    @FXML
    private Button accountbtn;

    @FXML
    private Button ordersbtn;

    @FXML
    private ImageView img;

    @FXML
    private Button overviewbtn;

    @FXML
    private ImageView profileImage;

    @FXML
    private ToggleButton Mode;

    @FXML
    private AnchorPane admin_anc;

    @FXML
    private Pane overlayPane;

    @FXML
    private BorderPane root;

    @FXML
    private AnchorPane settingPane, profilePane;

    @FXML
    private Label settingIcon;

    @FXML
    private Button logoutbtn;

    private Button activeButton;

    @FXML
    void clickAccount(ActionEvent event) throws IOException {
        loadView("/View/adminAccounts.fxml");
        setActiveButton(accountbtn);
    }

    @FXML
    void clickOrders(ActionEvent event) throws IOException {
        loadView("/View/adminOrders.fxml");
        setActiveButton(ordersbtn);
    }

    @FXML
    void clickOverview(ActionEvent event) throws IOException {
        loadView("/View/adminOverview.fxml");
        setActiveButton(overviewbtn);
    }

    private void clickSetting() {
        openSetting();
        overlayPane.setOnMouseClicked(e -> {
            closeSetting();
        });

    }

    private void updateDatabase(){
        Porsche_DB connect = new Porsche_DB();
        try {
            Connection con = connect.connect();
            PreparedStatement p = con.prepareStatement("Update user_info set user_email = ?, user_phone = ?, user_address = ? where user_id = ?");
            p.setString(1, profileEmail.getText());
            p.setString(2, profilePhone.getText());
            p.setString(3, profileAddress.getText());
            p.setInt(4, current.getUserid());
            p.execute();
        } catch (
                ClassNotFoundException |
                SQLException ex) {
            throw new RuntimeException(ex);
        }
        current.setEmail(profileEmail.getText());
        current.setAddress(profileAddress.getText());
        current.setPhone(profilePhone.getText());
        Session.setInstance(current);
    }

    Session current = Session.getInstance();

    public void initialize() throws IOException {

        String name = current.getUsername();
        nameLbl.setText(name);
        profileName.setText(name);
        profileAddress.setText(current.getAddress());
        profileEmail.setText(current.getEmail());
        profileDOB.setText(current.getDob().toString());
        profilePhone.setText(current.getPhone());

        editButton.setText("\uD83D\uDEE0");

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
            } catch (
                    IOException ex) {
                ex.printStackTrace();
            }
        });

        editButton.setOnMouseClicked(e -> {
            editButton.setVisible(false);
            editBack.setVisible(true);
            editAddress.setVisible(true);
            editPhone.setVisible(true);
            editEmail.setVisible(true);
            editConfirm.setVisible(true);
            editRevert.setVisible(true);
        });

        editBack.setOnMouseClicked(e -> {
            editButton.setVisible(true);
            editBack.setVisible(false);
            editAddress.setVisible(false);
            editPhone.setVisible(false);
            editEmail.setVisible(false);
            editConfirm.setVisible(false);
            editRevert.setVisible(false);
        });

        editPhone.setOnMouseClicked(e -> {
            profilePhone.setEditable(true);
            profilePhone.requestFocus();
        });
        editEmail.setOnMouseClicked(e -> {
            profileEmail.setEditable(true);
            profileEmail.requestFocus();
        });
        editAddress.setOnMouseClicked(e -> {
            profileAddress.setEditable(true);
            profileAddress.requestFocus();
        });

        editConfirm.setOnMouseClicked(e -> {
            updateDatabase();

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Success");
            success.setContentText("Updated");
            success.show();
            profileEmail.setEditable(false);
            profileAddress.setEditable(false);
            profilePhone.setEditable(false);
            editEmail.setVisible(false);
            editPhone.setVisible(false);
            editAddress.setVisible(false);
            editBack.setVisible(false);
            editButton.setVisible(true);
            editRevert.setVisible(false);
            editConfirm.setVisible(false);
        });

        editBack.setOnMouseClicked(e -> {
            if (!profileEmail.getText().equals(current.getEmail()) || !profilePhone.getText().equals(current.getPhone()) || !profileAddress.getText().equals(current.getAddress())) {
                AnchorPane root = new AnchorPane();
                root.setPrefSize(482, 276);
                root.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/CSS/DashBoard_general.css")).toExternalForm());

                Label lbl1 = new Label("You Haven't Save Your Changes yet");
                lbl1.setLayoutX(59);
                lbl1.setLayoutY(27);
                lbl1.getStyleClass().add("topic_font");

                Label lbl2 = new Label("Do you want to Commit or Revert?");
                lbl2.setLayoutX(59);
                lbl2.setLayoutY(62);
                lbl2.getStyleClass().add("topic_font");

                Button btnCommit = new Button("Commit");
                btnCommit.setLayoutX(118);
                btnCommit.setLayoutY(181);
                btnCommit.setPrefWidth(50);
                btnCommit.setMaxWidth(50);
                btnCommit.setMnemonicParsing(false);
                btnCommit.getStyleClass().add("glassy-button");

                Button btnRevert = new Button("Revert");
                btnRevert.setLayoutX(306);
                btnRevert.setLayoutY(181);
                btnRevert.setPrefWidth(50);
                btnRevert.setMaxWidth(50);
                btnRevert.setMnemonicParsing(false);
                btnRevert.getStyleClass().add("glassy-button");

                root.getChildren().addAll(lbl1, lbl2, btnCommit, btnRevert);

                Scene scene = new Scene(root);
                Stage stage = new Stage();
                stage.setScene(scene);
                stage.initStyle(StageStyle.UNDECORATED);
                stage.initModality(Modality.WINDOW_MODAL);
                stage.setTitle("Confirmation");
                stage.show();

                btnCommit.setOnMouseClicked(event->{
                    updateDatabase();
                    stage.close();
                });
                btnRevert.setOnMouseClicked(event1->{
                    profileAddress.setText(current.getAddress());
                    profileEmail.setText(current.getEmail());
                    profilePhone.setText(current.getPhone());
                    stage.close();
                });
            }
            profileEmail.setEditable(false);
            profileAddress.setEditable(false);
            profilePhone.setEditable(false);
            editEmail.setVisible(false);
            editPhone.setVisible(false);
            editAddress.setVisible(false);
            editBack.setVisible(false);
            editButton.setVisible(true);
            editRevert.setVisible(false);
            editConfirm.setVisible(false);
        });


        editRevert.setOnMouseClicked(e -> {
            profileAddress.setText(current.getAddress());
            profileEmail.setText(current.getEmail());
            profilePhone.setText(current.getPhone());
            profileEmail.setEditable(false);
            profileAddress.setEditable(false);
            profilePhone.setEditable(false);
        });

        backButton.setOnMouseClicked(e -> {
            profilePane.setVisible(false);
        });

        optionProfile.setOnMouseClicked(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/Authentication.fxml"));
                Parent root = loader.load();
                authenticationController controller = loader.getController();
                controller.init(this);
                controller.setStep("password");
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.initStyle(StageStyle.UNDECORATED);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.show();
            } catch (
                    IOException ex) {
                ex.printStackTrace();
            }
        });


        settingPane.setTranslateX(350);
        overlayPane.setVisible(false);
        settingIcon.setOnMouseClicked(e -> {
            clickSetting();
        });

        Image porsche_logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Image/porsche_logo.png")));
        img.setImage(porsche_logo);
        loadView("/View/adminOverview.fxml");
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
    void clickLogout
            (ActionEvent
                     event) throws
            Exception {
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


    private void setActiveButton
            (Button
                     button) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("active");
        }
        if (!button.getStyleClass().contains("active")) {
            button.getStyleClass().add("active");
        }
        activeButton = button;
    }

    private SequentialTransition animation;

    private void openSetting
            () {
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

    private void closeSetting
            () {
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

    public void setProfile
            () {
        profilePane.setVisible(true);
    }

}