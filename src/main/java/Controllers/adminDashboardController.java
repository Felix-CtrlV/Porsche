package Controllers;

import Database.DatabaseConnectionManager;
import MainUI.login;
import Utils.OTPService;
import Utils.Session;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
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
    private Label profileName, profileDOB;

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
    private VBox settingPane, passwordVerifyPane, profilePane, targetPane;
    
    @FXML
    private StackPane targetContentPane;
    
    @FXML
    private AnchorPane oldProfilePane;

    @FXML
    private Label settingIcon, backToSettingsBtn, backToSettingsFromTarget, editProfileBtn, passwordErrorLabel;
    
    @FXML
    private javafx.scene.control.PasswordField verifyPasswordField;
    
    @FXML
    private Button verifyPasswordBtn, cancelVerifyBtn, saveProfileBtn;
    
    @FXML
    private HBox toastNotification;
    
    @FXML
    private Label toastIcon, toastTitle, toastMessage;

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
            closeAllPanesAndResetToSettings();
        });

    }

    private void updateDatabase(){
        try {
            Connection con = DatabaseConnectionManager.getInstance().getConnection();
            PreparedStatement p = con.prepareStatement("Update user_info set user_email = ?, user_phone = ?, user_address = ? where user_id = ?");
            p.setString(1, profileEmail.getText());
            p.setString(2, profilePhone.getText());
            p.setString(3, profileAddress.getText());
            p.setInt(4, current.getUserid());
            p.execute();
        } catch (SQLException ex) {
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

        // Removed old edit button code

        // Profile view click - show password verification
        optionProfile.setOnMouseClicked(e -> {
            showPasswordVerification();
        });
        
        // Change password click in settings
        optionChange.setOnMouseClicked(e -> {
            onChangePassword();
        });
        
        // Target click in settings
        optionTarget.setOnMouseClicked(e -> {
            showTargetPane();
        });
        
        // Back to settings from profile
        if (backToSettingsBtn != null) {
            backToSettingsBtn.setOnMouseClicked(e -> {
                hideProfilePane();
            });
        }
        
        // Back to settings from target
        if (backToSettingsFromTarget != null) {
            backToSettingsFromTarget.setOnMouseClicked(e -> {
                hideTargetPane();
            });
        }
        
        // Edit profile button
        if (editProfileBtn != null) {
            editProfileBtn.setOnMouseClicked(e -> {
                toggleEditMode();
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

        // Old edit button code removed


        settingPane.setTranslateX(420);
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
                                Connection con = DatabaseConnectionManager.getInstance().getConnection();
                                CallableStatement check_out = con.prepareCall("call logout(?, ?)");
                                if (current != null) {
                                    check_out.setInt(1, current.getUserid());
                                    check_out.setString(2, String.valueOf(LocalDateTime.now()));
                                    check_out.execute();
                                }
                                con.close();
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

        try {
            Connection con = DatabaseConnectionManager.getInstance().getConnection();
            CallableStatement check_out = con.prepareCall("call logout(?, ?)");
            Session current = Session.getInstance();
            if (current != null) {
                adminid = current.getUserid();
            }
            check_out.setInt(1, adminid);
            check_out.setString(2, String.valueOf(LocalDateTime.now()));
            check_out.execute();
            con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        login log_in = new login();
        log_in.startDirectLogin(new Stage());
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

    private void openSetting() {
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

    private void closeSetting
            () {
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

    public void setProfile
            () {
        profilePane.setVisible(true);
    }


    /**
     * Sends OTP and opens change password dialog
     */
    public void proceedWithOTP() {
        try {
            // Send OTP to user's email
            OTPService otpService = OTPService.getInstance();
            String userEmail = current.getEmail();
            
            
            // Send OTP in background
            javafx.concurrent.Task<Boolean> sendOtpTask = new javafx.concurrent.Task<>() {
                @Override
                protected Boolean call() {
                    return otpService.sendOTP(userEmail);
                }
            };
            
            sendOtpTask.setOnSucceeded(e -> {
                if (sendOtpTask.getValue()) {
                    // OTP sent successfully, open change password dialog
                    openChangePasswordDialog();
                } else {
                    showErrorMessage("Failed to send OTP. Please check email configuration in OTPService.java\n\n" +
                                   "1. Update SENDER_EMAIL with your Gmail address\n" +
                                   "2. Update SENDER_PASSWORD with Gmail App Password\n" +
                                   "3. Enable 2-Factor Authentication in Google Account\n" +
                                   "4. Generate App Password in Google Account settings");
                }
            });
            
            sendOtpTask.setOnFailed(e -> {
                showErrorMessage("Error sending OTP: " + sendOtpTask.getException().getMessage());
            });
            
            new Thread(sendOtpTask).start();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            showErrorMessage("Error initializing OTP system");
        }
    }

    /**
     * Opens the change password dialog
     */
    private void openChangePasswordDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/ChangePassword.fxml"));
            Parent root = loader.load();
            
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initStyle(StageStyle.UNDECORATED);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
            showErrorMessage("Failed to open change password dialog");
        }
    }


    /**
     * Shows error message
     */
    private void showErrorMessage(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Masks email for privacy
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];

        if (username.length() <= 2) {
            return username.charAt(0) + "***@" + domain;
        }

        return username.charAt(0) + "***" + username.charAt(username.length() - 1) + "@" + domain;
    }
    
    // ========== NEW PROFILE VIEW METHODS ==========
    
    private void closeAllPanesAndResetToSettings() {
        // Determine which pane is currently visible and close it
        if (profilePane.isVisible()) {
            // Close profile pane directly
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), profilePane);
            slideOut.setFromX(0);
            slideOut.setToX(420);
            
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), overlayPane);
            fadeOut.setFromValue(0.5);
            fadeOut.setToValue(0);
            
            ParallelTransition hide = new ParallelTransition(slideOut, fadeOut);
            hide.setOnFinished(e -> {
                profilePane.setVisible(false);
                settingPane.setVisible(false); // Also hide settings pane
                overlayPane.setVisible(false);
                root.setEffect(null);
                root.setDisable(false);
            });
            hide.play();
        } else if (targetPane.isVisible()) {
            // Close target pane directly
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), targetPane);
            slideOut.setFromX(0);
            slideOut.setToX(420);
            
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), overlayPane);
            fadeOut.setFromValue(0.5);
            fadeOut.setToValue(0);
            
            ParallelTransition hide = new ParallelTransition(slideOut, fadeOut);
            hide.setOnFinished(e -> {
                targetPane.setVisible(false);
                settingPane.setVisible(false); // Also hide settings pane
                overlayPane.setVisible(false);
                root.setEffect(null);
                root.setDisable(false);
            });
            hide.play();
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
                verifyPasswordField.clear();
                passwordErrorLabel.setVisible(false);
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
        // Hide settings pane but keep overlay and blur
        TranslateTransition slideOutSettings = new TranslateTransition(Duration.millis(300), settingPane);
        slideOutSettings.setFromX(0);
        slideOutSettings.setToX(420);
        slideOutSettings.setOnFinished(e -> {
            settingPane.setVisible(false);
            
            // Show password verification pane
            passwordVerifyPane.setVisible(true);
            passwordVerifyPane.setTranslateX(420);
            verifyPasswordField.clear();
            passwordErrorLabel.setVisible(false);
            
            TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), passwordVerifyPane);
            slideIn.setFromX(420);
            slideIn.setToX(0);
            slideIn.play();
        });
        slideOutSettings.play();
    }
    
    @FXML
    private void onVerifyPassword() {
        String enteredPassword = verifyPasswordField.getText();
        
        if (enteredPassword.isEmpty()) {
            passwordErrorLabel.setText("Please enter your password");
            passwordErrorLabel.setVisible(true);
            return;
        }
        
        // Verify password against session
        String sessionPassword = current.getPassword();
        
        if (sessionPassword != null && sessionPassword.equals(enteredPassword)) {
            // Password correct - show profile
            hidePasswordVerification();
            showProfilePane();
        } else {
            passwordErrorLabel.setText("Incorrect password");
            passwordErrorLabel.setVisible(true);
        }
    }
    
    @FXML
    private void onCancelVerify() {
        hidePasswordVerification();
    }
    
    private void hidePasswordVerification() {
        // Slide out verification pane and slide in settings pane simultaneously
        TranslateTransition slideOutVerify = new TranslateTransition(Duration.millis(300), passwordVerifyPane);
        slideOutVerify.setFromX(0);
        slideOutVerify.setToX(420);
        
        // Prepare settings pane
        settingPane.setVisible(true);
        settingPane.setTranslateX(420);
        
        TranslateTransition slideInSettings = new TranslateTransition(Duration.millis(300), settingPane);
        slideInSettings.setFromX(420);
        slideInSettings.setToX(0);
        
        // Run both transitions together
        ParallelTransition transition = new ParallelTransition(slideOutVerify, slideInSettings);
        transition.setOnFinished(e -> {
            passwordVerifyPane.setVisible(false);
            verifyPasswordField.clear();
            passwordErrorLabel.setVisible(false);
        });
        transition.play();
    }
    
    private void showProfilePane() {
        // Hide settings pane to prevent stacking
        settingPane.setVisible(false);
        
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
        
        // Slide out verification pane and slide in profile pane simultaneously
        TranslateTransition slideOutVerify = new TranslateTransition(Duration.millis(300), passwordVerifyPane);
        slideOutVerify.setFromX(0);
        slideOutVerify.setToX(420);
        
        // Prepare profile pane
        profilePane.setVisible(true);
        profilePane.setTranslateX(420);
        
        TranslateTransition slideInProfile = new TranslateTransition(Duration.millis(300), profilePane);
        slideInProfile.setFromX(420);
        slideInProfile.setToX(0);
        
        // Run both transitions together
        ParallelTransition transition = new ParallelTransition(slideOutVerify, slideInProfile);
        transition.setOnFinished(e -> {
            passwordVerifyPane.setVisible(false);
            // Clear any selection after animation
            profileEmail.deselect();
            profilePhone.deselect();
            profileAddress.deselect();
        });
        transition.play();
    }
    
    private void hideProfilePane() {
        // Slide out profile pane and slide in settings pane simultaneously
        TranslateTransition slideOutProfile = new TranslateTransition(Duration.millis(300), profilePane);
        slideOutProfile.setFromX(0);
        slideOutProfile.setToX(420);
        
        // Prepare settings pane
        settingPane.setVisible(true);
        settingPane.setTranslateX(420);
        
        TranslateTransition slideInSettings = new TranslateTransition(Duration.millis(300), settingPane);
        slideInSettings.setFromX(420);
        slideInSettings.setToX(0);
        
        // Run both transitions together
        ParallelTransition transition = new ParallelTransition(slideOutProfile, slideInSettings);
        transition.setOnFinished(e -> {
            profilePane.setVisible(false);
        });
        transition.play();
    }
    
    private void toggleEditMode() {
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
            profileEmail.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 8; -fx-padding: 12; -fx-font-size: 14; -fx-text-fill: #1f2937; -fx-border-color: transparent; -fx-prompt-text-fill: #9ca3af;");
            profilePhone.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 8; -fx-padding: 12; -fx-font-size: 14; -fx-text-fill: #1f2937; -fx-border-color: transparent; -fx-prompt-text-fill: #9ca3af;");
            profileAddress.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 8; -fx-padding: 12; -fx-font-size: 14; -fx-text-fill: #1f2937; -fx-border-color: transparent; -fx-prompt-text-fill: #9ca3af;");
            saveProfileBtn.setVisible(false);
            editProfileBtn.setText("✏");
            
            // Deselect any text and revert changes
            profileEmail.deselect();
            profilePhone.deselect();
            profileAddress.deselect();
            profileAddress.setText(current.getAddress());
            profileEmail.setText(current.getEmail());
            profilePhone.setText(current.getPhone());
        }
    }
    
    @FXML
    private void onSaveProfile() {
        try {
            Connection con = DatabaseConnectionManager.getInstance().getConnection();
            PreparedStatement p = con.prepareStatement("UPDATE user_info SET user_email = ?, user_phone = ?, user_address = ? WHERE user_id = ?");
            p.setString(1, profileEmail.getText());
            p.setString(2, profilePhone.getText());
            p.setString(3, profileAddress.getText());
            p.setInt(4, current.getUserid());
            p.execute();
            
            // Update session
            current.setEmail(profileEmail.getText());
            current.setAddress(profileAddress.getText());
            current.setPhone(profilePhone.getText());
            Session.setInstance(current);
            
            // Disable editing
            toggleEditMode();
            
            // Show success toast
            showToast("Success", "Profile updated successfully!", "success");
            
        } catch (SQLException ex) {
            // Show error toast
            showToast("Error", "Failed to update profile", "error");
            ex.printStackTrace();
        }
    }
    
    private void showToast(String title, String message, String type) {
        toastTitle.setText(title);
        toastMessage.setText(message);
        
        // Set icon and color based on type
        StackPane iconContainer = (StackPane) toastIcon.getParent();
        if (type.equals("success")) {
            toastIcon.setText("✓");
            iconContainer.setStyle("-fx-background-color: #10b981; -fx-background-radius: 18;");
        } else if (type.equals("error")) {
            toastIcon.setText("✕");
            iconContainer.setStyle("-fx-background-color: #ef4444; -fx-background-radius: 18;");
        } else if (type.equals("info")) {
            toastIcon.setText("ℹ");
            iconContainer.setStyle("-fx-background-color: #3b82f6; -fx-background-radius: 18;");
        }
        
        toastNotification.setVisible(true);
        toastNotification.setTranslateY(-100);
        
        // Slide in animation from top
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), toastNotification);
        slideIn.setFromY(-100);
        slideIn.setToY(0);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toastNotification);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        ParallelTransition showToast = new ParallelTransition(slideIn, fadeIn);
        
        // Auto hide after 3 seconds
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> hideToast());
        
        SequentialTransition sequence = new SequentialTransition(showToast, pause);
        sequence.play();
    }
    
    private void hideToast() {
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), toastNotification);
        slideOut.setFromY(0);
        slideOut.setToY(-100);
        
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), toastNotification);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        
        ParallelTransition hide = new ParallelTransition(slideOut, fadeOut);
        hide.setOnFinished(e -> toastNotification.setVisible(false));
        hide.play();
    }
    
    @FXML
    private void onChangePassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/ChangePassword.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initStyle(StageStyle.UNDECORATED);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    // ========== TARGET PANE METHODS ==========
    
    private void showTargetPane() {
        // Hide settings pane
        settingPane.setVisible(false);
        
        // Load target management content if not already loaded
        if (targetContentPane.getChildren().isEmpty()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/adminTargetManagement.fxml"));
                javafx.scene.Node targetContent = loader.load();
                targetContentPane.getChildren().add(targetContent);
            } catch (IOException e) {
                e.printStackTrace();
                showToast("Error", "Failed to load target management", "error");
                return;
            }
        }
        
        // Show target pane with animation
        targetPane.setVisible(true);
        targetPane.setTranslateX(420);
        
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), targetPane);
        slideIn.setFromX(420);
        slideIn.setToX(0);
        slideIn.play();
    }
    
    private void hideTargetPane() {
        // Slide out target pane and slide in settings pane simultaneously
        TranslateTransition slideOutTarget = new TranslateTransition(Duration.millis(300), targetPane);
        slideOutTarget.setFromX(0);
        slideOutTarget.setToX(420);
        
        // Prepare settings pane
        settingPane.setVisible(true);
        settingPane.setTranslateX(420);
        
        TranslateTransition slideInSettings = new TranslateTransition(Duration.millis(300), settingPane);
        slideInSettings.setFromX(420);
        slideInSettings.setToX(0);
        
        // Run both transitions together
        ParallelTransition transition = new ParallelTransition(slideOutTarget, slideInSettings);
        transition.setOnFinished(e -> {
            targetPane.setVisible(false);
        });
        transition.play();
    }

}
