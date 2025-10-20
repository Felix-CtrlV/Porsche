package Controllers;

import Database.DatabaseConnectionManager;
import MainUI.login;
import Utils.LogoutHelper;
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
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;

public class managerDashboardController {
    private static final Logger logger = LoggerFactory.getLogger(managerDashboardController.class);

    @FXML
    private ToggleButton Mode;

    @FXML
    private AnchorPane admin_anc;

    @FXML
    private Label backButton;

    @FXML
    private Label backToSettingsBtn;

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
    private HBox optionAttendance;

    @FXML
    private HBox profileChangeOption;

    @FXML
    private Button orderbtn;

    @FXML
    private Pane overlayPane;

    @FXML
    private Button overviewbtn;

    @FXML
    private ImageView pfImage;

    @FXML
    private TextField profileAddress;

    @FXML
    private Label profileDOB;

    @FXML
    private TextField profileEmail;

    @FXML
    private TextField profilePhone;

    @FXML
    private Label profileName;

    @FXML
    private VBox profilePane;

    @FXML
    private BorderPane root;

    @FXML
    private Label settingIcon;

    @FXML
    private VBox settingPane;

    @FXML
    private Button staffbtn;

    private Button activeButton;

    private int adminid;

    // New fields for profile management (matching admin)
    @FXML
    private VBox passwordVerifyPane;

    @FXML
    private PasswordField verifyPasswordField;

    @FXML
    private Label passwordErrorLabel;

    @FXML
    private Button verifyPasswordBtn, cancelVerifyBtn;

    @FXML
    private TextField profileEmailField, profilePhoneField, profileAddressField;

    @FXML
    private Button saveProfileBtn, editProfileBtn;

    Session current = Session.getInstance();

    @FXML
    void clickInventory(ActionEvent event) {
        loadView("/View/managerInventory.fxml");
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

    @FXML
    void onChangePassword(ActionEvent event) {
        openAuthenticationDialog("factor");
    }

    private void clickSetting(){
        openSetting();
        overlayPane.setOnMouseClicked(e->{
            closeAllPanesAndResetToSettings();
        });
    }

    public void initialize() throws IOException {
        // Profile change password inside profile pane
        if (profileChangeOption != null) {
            profileChangeOption.setOnMouseClicked(e -> openAuthenticationDialog("factor"));
        }

        if (optionChange != null) {
            optionChange.setOnMouseClicked(e -> openAuthenticationDialog("factor"));
        }

        backButton.setOnMouseClicked(e->{
            profilePane.setVisible(false);
        });

        // Use admin-style profile view: show password verification first
        optionProfile.setOnMouseClicked(e -> {
            showPasswordVerification();
        });

        // Change password click in settings (now opens change password dialog like admin)
        optionChange.setOnMouseClicked(e -> {
            showChangePasswordDialog();
        });

        // Back from verification pane
        if (backToSettingsBtn != null) {
            backToSettingsBtn.setOnMouseClicked(e -> {
                hidePasswordVerification();
            });
        }

        // Add Enter key listener to password verification field
        if (verifyPasswordField != null) {
            verifyPasswordField.setOnKeyPressed(event -> {
                if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                    onVerifyPassword();
                }
            });
        }

        settingPane.setTranslateX(420);
        overlayPane.setVisible(false);
        settingIcon.setOnMouseClicked(e->{clickSetting();});

        if (optionAttendance != null) {
            optionAttendance.setOnMouseClicked(e -> {
                loadView("/View/managerOverview.fxml");
                setActiveButton(overviewbtn);
                closeSetting();
            });
        }

        String name = current.getUsername();
        nameLbl.setText(name);
        profileName.setText(name);
        profileAddress.setText(current.getAddress());
        profileEmail.setText(current.getEmail());
        profilePhone.setText(current.getPhone());
        if (current.getDob() != null) {
            profileDOB.setText(current.getDob().toString());
        }

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
                                if (current != null) {
                                    LogoutHelper.performLogout(current.getUserid());
                                }
                                login log_in = new login();
                                log_in.startDirectLogin(new Stage());
                            } catch (Exception ex) {
                                logger.error("Error during window close logout", ex);
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

        if (current != null) {
            adminid = current.getUserid();
        }
        LogoutHelper.performLogout(adminid);
        login log_in = new login();
        log_in.startDirectLogin(new Stage());
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

    private void openAuthenticationDialog(String step) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/Authentication.fxml"));
            Parent root = loader.load();
            authenticationController controller = loader.getController();
            controller.setStep(step);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initStyle(StageStyle.UNDECORATED);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void openSetting(){
        // Ensure only settings pane is visible
        settingPane.setVisible(true);
        passwordVerifyPane.setVisible(false);
        profilePane.setVisible(false);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), settingPane);
        slideIn.setFromX(420);
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

    private void closeSetting() {
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), settingPane);
        slideOut.setFromX(0);
        slideOut.setToX(420);

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

    // ========== NEW PROFILE VIEW METHODS (Copied from admin) ==========

    private void closeAllPanesAndResetToSettings() {
        // Determine which pane is currently visible and close it
        if (profilePane.isVisible()) {
            closeProfilePane(false);
            return;
        } else if (passwordVerifyPane.isVisible()) {
            // Close password verification pane directly
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), passwordVerifyPane);
            slideOut.setFromX(0);
            slideOut.setToX(420);

            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), overlayPane);
            fadeOut.setFromValue(0.5);
            fadeOut.setToValue(0);

            ParallelTransition hide = new ParallelTransition(slideOut, fadeOut);
            hide.setOnFinished(e -> {
                passwordVerifyPane.setVisible(false);
                settingPane.setVisible(false); // Also hide settings pane
                overlayPane.setVisible(false);
                root.setEffect(null);
                root.setDisable(false);
            });
            hide.play();
        } else if (settingPane.isVisible()) {
            // Close settings pane directly
            closeSetting();
        }
    }

    private void showPasswordVerification() {
        // Slide out settings pane first
        TranslateTransition slideOutSettings = new TranslateTransition(Duration.millis(300), settingPane);
        slideOutSettings.setFromX(0);
        slideOutSettings.setToX(420);
        slideOutSettings.setOnFinished(e -> {
            settingPane.setVisible(false);

            // Then slide in verification pane
            passwordVerifyPane.setVisible(true);
            passwordVerifyPane.setTranslateX(420);
            verifyPasswordField.clear();
            passwordErrorLabel.setVisible(false);

            TranslateTransition slideInVerify = new TranslateTransition(Duration.millis(300), passwordVerifyPane);
            slideInVerify.setFromX(420);
            slideInVerify.setToX(0);
            slideInVerify.play();
        });
        slideOutSettings.play();
    }

    private void hidePasswordVerification() {
        // Slide out verification pane first
        TranslateTransition slideOutVerify = new TranslateTransition(Duration.millis(300), passwordVerifyPane);
        slideOutVerify.setFromX(0);
        slideOutVerify.setToX(420);
        slideOutVerify.setOnFinished(e -> {
            passwordVerifyPane.setVisible(false);
            verifyPasswordField.clear();
            passwordErrorLabel.setVisible(false);

            // Prepare settings pane off-screen before making it visible
            settingPane.setTranslateX(420);
            settingPane.setVisible(true);

            // Then slide in settings pane
            TranslateTransition slideInSettings = new TranslateTransition(Duration.millis(300), settingPane);
            slideInSettings.setFromX(420);
            slideInSettings.setToX(0);
            slideInSettings.play();
        });
        slideOutVerify.play();
    }

    private void showProfilePane() {
        // Settings pane is already hidden from verification step, no need to touch it

        if (settingPane != null) {
            settingPane.setVisible(false);
        }

        // Refresh profile data from session first
        if (current != null) {
            profileName.setText(current.getUsername() != null ? current.getUsername() : "");
            profileEmail.setText(current.getEmail() != null ? current.getEmail() : "");
            profilePhone.setText(current.getPhone() != null ? current.getPhone() : "");
            profileAddress.setText(current.getAddress() != null ? current.getAddress() : "");

            // Format DOB nicely
            if (current.getDob() != null) {
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy");
                profileDOB.setText(current.getDob().format(formatter));
            } else {
                profileDOB.setText("");
            }
        }

        // Ensure fields are not editable and deselect any text
        profileEmail.setEditable(false);
        profilePhone.setEditable(false);
        profileAddress.setEditable(false);
        profileEmail.setFocusTraversable(false);
        profilePhone.setFocusTraversable(false);
        profileAddress.setFocusTraversable(false);

        // Slide out verification pane first
        TranslateTransition slideOutVerify = new TranslateTransition(Duration.millis(300), passwordVerifyPane);
        slideOutVerify.setFromX(0);
        slideOutVerify.setToX(420);
        slideOutVerify.setOnFinished(e -> {
            passwordVerifyPane.setVisible(false);

            // Then slide in profile pane
            profilePane.setVisible(true);
            profilePane.setTranslateX(420);

            TranslateTransition slideInProfile = new TranslateTransition(Duration.millis(300), profilePane);
            slideInProfile.setFromX(420);
            slideInProfile.setToX(0);
            slideInProfile.setOnFinished(ev -> {
                // Clear any selection after animation
                profileEmail.deselect();
                profilePhone.deselect();
                profileAddress.deselect();
            });
            slideInProfile.play();
        });
        slideOutVerify.play();
    }

    private void hideProfilePane() {
        closeProfilePane(true);
    }

    private void closeProfilePane(boolean reopenSettings) {
        if (!profilePane.isVisible()) {
            return;
        }

        TranslateTransition slideOutProfile = new TranslateTransition(Duration.millis(300), profilePane);
        slideOutProfile.setFromX(0);
        slideOutProfile.setToX(420);

        if (reopenSettings) {
            slideOutProfile.setOnFinished(e -> {
                profilePane.setVisible(false);
                profilePane.setTranslateX(0);

                if (settingPane != null) {
                    settingPane.setTranslateX(420);
                    settingPane.setVisible(true);
                    TranslateTransition slideInSettings = new TranslateTransition(Duration.millis(300), settingPane);
                    slideInSettings.setFromX(420);
                    slideInSettings.setToX(0);
                    slideInSettings.play();
                }
            });
            slideOutProfile.play();
        } else {
            slideOutProfile.setOnFinished(e -> {
                profilePane.setVisible(false);
                profilePane.setTranslateX(0);
                if (settingPane != null) {
                    settingPane.setVisible(false);
                }

                FadeTransition fadeOverlay = new FadeTransition(Duration.millis(200), overlayPane);
                fadeOverlay.setFromValue(overlayPane.getOpacity());
                fadeOverlay.setToValue(0);
                fadeOverlay.setOnFinished(ev -> {
                    overlayPane.setVisible(false);
                    overlayPane.setOpacity(0.5);
                    root.setEffect(null);
                    root.setDisable(false);
                });
                fadeOverlay.play();
            });
            slideOutProfile.play();
        }
    }

    public void showChangePasswordDialog() {
        openAuthenticationDialog("factor");
    }

    @FXML
    public void toggleEditMode() {
        boolean isEditable = profileEmail.isEditable();

        if (!isEditable) {
            // Enable editing
            profileEmail.setEditable(true);
            profilePhone.setEditable(true);
            profileAddress.setEditable(true);
            profileEmail.setFocusTraversable(true);
            profilePhone.setFocusTraversable(true);
            profileAddress.setFocusTraversable(true);
            profileEmail.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 12; -fx-font-size: 14; -fx-text-fill: #1f2937; -fx-border-color: #3b82f6; -fx-border-width: 2; -fx-border-radius: 8;");
            profilePhone.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 12; -fx-font-size: 14; -fx-text-fill: #1f2937; -fx-border-color: #3b82f6; -fx-border-width: 2; -fx-border-radius: 8;");
            profileAddress.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 12; -fx-font-size: 14; -fx-text-fill: #1f2937; -fx-border-color: #3b82f6; -fx-border-width: 2; -fx-border-radius: 8;");
            saveProfileBtn.setVisible(true);
            editProfileBtn.setText("✖");
        } else {
            // Disable editing
            profileEmail.setEditable(false);
            profilePhone.setEditable(false);
            profileAddress.setEditable(false);
            profileEmail.setFocusTraversable(false);
            profilePhone.setFocusTraversable(false);
            profileAddress.setFocusTraversable(false);
            profileEmail.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 8; -fx-padding: 12; -fx-font-size: 14; -fx-text-fill: #1f2937; -fx-border-color: transparent;");
            profilePhone.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 8; -fx-padding: 12; -fx-font-size: 14; -fx-text-fill: #1f2937; -fx-border-color: transparent;");
            profileAddress.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 8; -fx-padding: 12; -fx-font-size: 14; -fx-text-fill: #1f2937; -fx-border-color: transparent;");
            saveProfileBtn.setVisible(false);
            editProfileBtn.setText("✏");

            // Deselect any text
            profileEmail.deselect();
            profilePhone.deselect();
            profileAddress.deselect();
        }
    }

    @FXML
    public void onVerifyPassword() {
        String enteredPassword = verifyPasswordField.getText();

        if (enteredPassword.isEmpty()) {
            passwordErrorLabel.setText("Please enter your password");
            passwordErrorLabel.setVisible(true);
            return;
        }

        // Verify password against session
        String sessionPassword = current.getPassword();

        if (sessionPassword != null && sessionPassword.equals(enteredPassword)) {
            // Password correct - transition directly to profile pane
            showProfilePane();
        } else {
            passwordErrorLabel.setText("Incorrect password");
            passwordErrorLabel.setVisible(true);
        }
    }

    @FXML
    public void onCancelVerify() {
        hidePasswordVerification();
    }

    // ========== DATABASE UPDATE FOR PROFILE (Copied from admin) ==========

    private void updateDatabase(){
        try {
            java.sql.Connection con = DatabaseConnectionManager.getInstance().getConnection();
            java.sql.PreparedStatement p = con.prepareStatement("Update user_info set user_email = ?, user_phone = ?, user_address = ? where user_id = ?");
            p.setString(1, profileEmail.getText());
            p.setString(2, profilePhone.getText());
            p.setString(3, profileAddress.getText());
            p.setInt(4, current.getUserid());
            p.execute();
            con.close();
        } catch (java.sql.SQLException ex) {
            throw new RuntimeException(ex);
        }
        current.setEmail(profileEmail.getText());
        current.setAddress(profileAddress.getText());
        current.setPhone(profilePhone.getText());
        Session.setInstance(current);
    }

    @FXML
    public void onSaveProfile() {
        // Update database and session
        updateDatabase();

        // Disable editing and hide save button
        profileEmail.setEditable(false);
        profilePhone.setEditable(false);
        profileAddress.setEditable(false);
        profileEmail.setFocusTraversable(false);
        profilePhone.setFocusTraversable(false);
        profileAddress.setFocusTraversable(false);
        profileEmail.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 8; -fx-padding: 12; -fx-font-size: 14; -fx-text-fill: #1f2937; -fx-border-color: transparent;");
        profilePhone.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 8; -fx-padding: 12; -fx-font-size: 14; -fx-text-fill: #1f2937; -fx-border-color: transparent;");
        profileAddress.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 8; -fx-padding: 12; -fx-font-size: 14; -fx-text-fill: #1f2937; -fx-border-color: transparent;");
        saveProfileBtn.setVisible(false);
        editProfileBtn.setText("✏");

        // Deselect any text and revert changes
        profileEmail.deselect();
        profilePhone.deselect();
        profileAddress.deselect();
        profileAddress.setText(current.getAddress());
        profileEmail.setText(current.getEmail());
        profilePhone.setText(current.getPhone());

        // Show success toast (if available)
    }
}
