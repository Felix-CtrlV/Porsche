package Controllers;

import Database.DatabaseConnectionManager;
import MainUI.login;
import Utils.OTPService;
import Utils.Session;
import Utils.CSSManager;
import Utils.SecurityManager;
import Utils.SecurityEmailService;
import javafx.stage.FileChooser;
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
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class adminDashboardController {

    @FXML
    private HBox optionProfile, optionTarget, optionSalary, optionChange;

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
    private VBox settingPane, passwordVerifyPane, profilePane, targetPane, salaryPane;
    
    @FXML
    private StackPane targetContentPane, salaryContentPane,rootPane;
    
    @FXML
    private AnchorPane oldProfilePane;

    @FXML
    private Label settingIcon, backToSettingsBtn, backToSettingsFromTarget, backToSettingsFromSalary, editProfileBtn, passwordErrorLabel;
    
    @FXML
    private javafx.scene.control.PasswordField verifyPasswordField;
    
    @FXML
    private Button verifyPasswordBtn, cancelVerifyBtn, saveProfileBtn;

    @FXML
    private Button editProfilePhotoBtn;
    
    @FXML
    private HBox toastNotification;
    
    @FXML
    private Label toastIcon, toastTitle, toastMessage;
    
    @FXML
    private VBox confirmDialog, confirmContent;
    
    @FXML
    private Pane confirmOverlay;
    
    @FXML
    private Label confirmIcon, confirmTitle, confirmMessage;
    
    @FXML
    private Button confirmOkBtn, confirmCancelBtn;
    
    private Runnable confirmCallback;
    
    // Change Password Dialog
    @FXML
    private VBox changePasswordDialog, passwordVerificationPane, otpVerificationPane, newPasswordPane;
    
    @FXML
    private PasswordField currentPasswordField, newPasswordField, confirmPasswordField;
    
    @FXML
    private TextField otpField;
    
    @FXML
    private Button verifyCurrentPasswordBtn, verifyOtpBtn, resendOtpBtn, changePasswordBtn;
    
    @FXML
    private Label passwordVerifyStatus, otpVerifyStatus, changePasswordStatus, otpEmailLabel;
    
    @FXML
    private StackPane passwordMessagePane;
    
    @FXML
    private Label passwordMessageLabel, closePasswordDialogBtn;
    
    @FXML
    private StackPane passwordLoadingPane;
    
    // Security Lock Overlay Components
    @FXML
    private StackPane securityLockOverlay;
    
    @FXML
    private Label lockIcon, lockMessage;
    
    private final OTPService otpService = OTPService.getInstance();
    private final SecurityManager securityManager = SecurityManager.getInstance();
    private final SecurityEmailService emailService = SecurityEmailService.getInstance();
    
    private SequentialTransition currentPasswordAnimation;

    @FXML
    private Button logoutbtn;

    private Button activeButton;

    private File selectedProfilePhoto;
    private String cachedPhotoPath;

    @FXML
    void clickAccount(ActionEvent event) throws IOException {
        loadView("/View/adminAccounts.fxml");
        setActiveButton(accountbtn);
    }

    private void applyCircularClip(ImageView imageView, double size) {
        if (imageView == null) {
            return;
        }

        imageView.setFitWidth(size);
        imageView.setFitHeight(size);
        double radius = size / 2.0;
        Circle clip = new Circle(radius, radius, radius);
        imageView.setClip(clip);
    }

    @FXML
    private void onEditProfileImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Photo");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        Stage stage = (Stage) admin_anc.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile == null) {
            return;
        }

        selectedProfilePhoto = selectedFile;
        if (pfImage != null) {
            pfImage.setImage(new Image(selectedFile.toURI().toString()));
        }
        if (profileImage != null) {
            profileImage.setImage(new Image(selectedFile.toURI().toString()));
        }

        saveProfileBtn.setVisible(true);
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
    boolean isDarkMode = false;
    public void initialize() throws IOException {

            rootPane.getStylesheets().add(
                    getClass().getResource("/CSS/admin_dark_mode.css").toExternalForm()
            );


        String name = current.getUsername();
        nameLbl.setText(name);
        profileName.setText(name);
        profileAddress.setText(current.getAddress());
        profileEmail.setText(current.getEmail());
        profileDOB.setText(current.getDob().toString());
        profilePhone.setText(current.getPhone());

        // Check if account is locked on initialization
        checkAccountLockStatus();
        
        // Add clipping to change password dialog to hide notification overflow
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        clip.widthProperty().bind(changePasswordDialog.widthProperty());
        clip.heightProperty().bind(changePasswordDialog.heightProperty());
        clip.setArcWidth(12);
        clip.setArcHeight(12);
        changePasswordDialog.setClip(clip);

        // Removed old edit button code

        // Profile view click - show password verification
        optionProfile.setOnMouseClicked(e -> {
            showPasswordVerification();
        });
        
        // Change password click in settings
        optionChange.setOnMouseClicked(e -> {
            showChangePasswordDialog();
        });
        
        // Target click in settings
        optionTarget.setOnMouseClicked(e -> {
            showTargetPane();
        });
        
        // Salary click in settings
        optionSalary.setOnMouseClicked(e -> {
            showSalaryPane();
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
        
        // Back to settings from salary
        if (backToSettingsFromSalary != null) {
            backToSettingsFromSalary.setOnMouseClicked(e -> {
                hideSalaryPane();
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

        applyCircularClip(pfImage, 120);
        applyCircularClip(profileImage, 48);

        if (editProfilePhotoBtn != null) {
            editProfilePhotoBtn.setVisible(false);
        }

        loadCurrentProfilePhoto();
    }

    private void loadView(String fxmlPath) {
        Task<Parent> task = new Task<>() {
            @Override
            protected Parent call() throws Exception {
                FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource(fxmlPath)));
                Parent view = loader.load();

                Object controller = loader.getController();
                if (controller instanceof adminAccountController accountController) {
                    accountController.setDashboardController(adminDashboardController.this);
                }

                return view;
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

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            if (ex != null) {
                ex.printStackTrace();
            }
        });

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
                    showChangePasswordDialog();
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
            closeProfilePane(false);
            return;
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
        } else if (salaryPane.isVisible()) {
            // Close salary pane directly
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), salaryPane);
            slideOut.setFromX(0);
            slideOut.setToX(420);
            
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), overlayPane);
            fadeOut.setFromValue(0.5);
            fadeOut.setToValue(0);
            
            ParallelTransition hide = new ParallelTransition(slideOut, fadeOut);
            hide.setOnFinished(e -> {
                salaryPane.setVisible(false);
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
    
    @FXML
    private void onVerifyPassword() {
        String enteredPassword = verifyPasswordField.getText();
        
        if (enteredPassword.isEmpty()) {
            passwordErrorLabel.setText("Please enter your password");
            passwordErrorLabel.setVisible(true);
            return;
        }
        
        // Check if account is already locked
        if (securityManager.isAccountLocked(current.getUserid(), "admin")) {
            showSecurityLockOverlay();
            return;
        }
        
        // Verify password against session
        String sessionPassword = current.getPassword();
        
        if (sessionPassword != null && sessionPassword.equals(enteredPassword)) {
            // Password correct - reset attempts and transition to profile pane
            securityManager.resetAttempts(current.getUserid(), "admin");
            showProfilePane();
        } else {
            // Password incorrect - record failed attempt
            boolean shouldLock = securityManager.recordFailedAttempt(current.getUserid(), "admin");
            
            if (shouldLock) {
                // Account locked - send notifications and show lock overlay
                emailService.sendBruteForceAlert(current.getUserid(), "admin");
                showSecurityLockOverlay();
            } else {
                // Show remaining attempts
                int remaining = securityManager.getRemainingAttempts(current.getUserid(), "admin");
                passwordErrorLabel.setText("Incorrect password. " + remaining + " attempts remaining.");
                passwordErrorLabel.setVisible(true);
            }
        }
    }
    
    @FXML
    private void onCancelVerify() {
        hidePasswordVerification();
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
            CSSManager.applyAdminTextField(profileEmail);
            CSSManager.applyAdminTextField(profilePhone);
            CSSManager.applyAdminTextField(profileAddress);
            saveProfileBtn.setVisible(true);
            if (editProfilePhotoBtn != null) {
                editProfilePhotoBtn.setVisible(true);
            }
            editProfileBtn.setText("✖");
        } else {
            // Disable editing
            profileEmail.setEditable(false);
            profilePhone.setEditable(false);
            profileAddress.setEditable(false);
            profileEmail.setFocusTraversable(false);
            profilePhone.setFocusTraversable(false);
            profileAddress.setFocusTraversable(false);
            CSSManager.removeAdminTextField(profileEmail);
            CSSManager.removeAdminTextField(profilePhone);
            CSSManager.removeAdminTextField(profileAddress);
            saveProfileBtn.setVisible(false);
            editProfileBtn.setText("✏");
            
            // Deselect any text and revert changes
            profileEmail.deselect();
            profilePhone.deselect();
            profileAddress.deselect();
            profileAddress.setText(current.getAddress());
            profileEmail.setText(current.getEmail());
            profilePhone.setText(current.getPhone());

            selectedProfilePhoto = null;
            applyPhotoToImages(cachedPhotoPath);
        }
    }
    
    @FXML
    private void onSaveProfile() {
        try {
            String photoPath = cachedPhotoPath;
            if (selectedProfilePhoto != null) {
                photoPath = saveProfileImage(selectedProfilePhoto, current.getUsername(), current.getUserid());
            }

            try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
                 PreparedStatement ps = con.prepareStatement("UPDATE user_info SET user_email = ?, user_phone = ?, user_address = ?, user_photo = ? WHERE user_id = ?")) {
                ps.setString(1, profileEmail.getText());
                ps.setString(2, profilePhone.getText());
                ps.setString(3, profileAddress.getText());
                ps.setString(4, photoPath);
                ps.setInt(5, current.getUserid());
                ps.executeUpdate();
            }

            cachedPhotoPath = photoPath;
            selectedProfilePhoto = null;

            // Update session copy
            current.setEmail(profileEmail.getText());
            current.setAddress(profileAddress.getText());
            current.setPhone(profilePhone.getText());
            Session.setInstance(current);

            applyPhotoToImages(cachedPhotoPath);

            // Disable editing
            toggleEditMode();

            if (editProfilePhotoBtn != null) {
                editProfilePhotoBtn.setVisible(false);
            }
            saveProfileBtn.setVisible(false);

            // Show success toast
            showToast("Success", "Profile updated successfully!", "success");

        } catch (SQLException ex) {
            // Show error toast
            showToast("Error", "Failed to update profile", "error");
            ex.printStackTrace();
        }
    }

    private void loadCurrentProfilePhoto() {
        cachedPhotoPath = fetchPhotoFromDatabase();
        applyPhotoToImages(cachedPhotoPath);
    }

    private String fetchPhotoFromDatabase() {
        String sql = "SELECT user_photo FROM user_info WHERE user_id = ?";
        try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, current.getUserid());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("user_photo");
                }
            }
        } catch (SQLException ignored) {
        }
        return null;
    }

    private void applyPhotoToImages(String storedPath) {
        Image image = null;

        if (storedPath != null && !storedPath.isBlank()) {
            Optional<String> resolved = UserPhotoResolver.resolve(storedPath);
            if (resolved.isPresent()) {
                try {
                    image = new Image(resolved.get(), true);
                } catch (Exception ignored) {
                    image = null;
                }
            }
        }

        if (image == null) {
            try {
                image = new Image(Objects.requireNonNull(getClass().getResource("/Image/defaultUserProfile.jpg")).toExternalForm(), true);
            } catch (Exception ignored) {
                image = null;
            }
        }

        if (image != null) {
            if (pfImage != null) {
                pfImage.setImage(image);
            }
            if (profileImage != null) {
                profileImage.setImage(image);
            }
        }
    }

    private String saveProfileImage(File file, String username, int userId) {
        try {
            String extension = extractExtension(file.getName());
            String sanitizedName = sanitizeForFileName(username);
            String fileName = String.format("user_%d_%s%s", userId, sanitizedName, extension);

            Path imagesDir = Paths.get(System.getProperty("user.dir"), "Images");
            Files.createDirectories(imagesDir);
            Path target = imagesDir.resolve(fileName);

            Files.copy(file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);

            return "Images/" + fileName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cachedPhotoPath;
    }

    private String extractExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex);
        }
        return ".png";
    }

    private String sanitizeForFileName(String value) {
        if (value == null || value.isBlank()) {
            return "user";
        }
        return value.trim().toLowerCase(Locale.ENGLISH).replaceAll("[^a-z0-9]+", "_");
    }
    
    public void showToast(String title, String message, String type) {
        toastTitle.setText(title);
        toastMessage.setText(message);

        String normalizedType = (type == null ? "info" : type.toLowerCase());

        // Set icon and color based on type
        StackPane iconContainer = (StackPane) toastIcon.getParent();
        
        // Clear existing toast styles
        iconContainer.getStyleClass().removeAll("toast-icon-success", "toast-icon-error", "toast-icon-warning", "toast-icon-info");
        
        switch (normalizedType) {
            case "success":
                toastIcon.setText("✓");
                iconContainer.getStyleClass().add("toast-icon-success");
                break;
            case "error":
                toastIcon.setText("✕");
                iconContainer.getStyleClass().add("toast-icon-error");
                break;
            case "warning":
                toastIcon.setText("!");
                iconContainer.getStyleClass().add("toast-icon-warning");
                break;
            default:
                toastIcon.setText("ℹ");
                iconContainer.getStyleClass().add("toast-icon-info");
                break;
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
    public void showChangePasswordDialog() {
        // Reset to step 1
        passwordVerificationPane.setVisible(true);
        otpVerificationPane.setVisible(false);
        newPasswordPane.setVisible(false);
        
        // Clear fields
        currentPasswordField.clear();
        otpField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
        
        // Clear status labels
        passwordVerifyStatus.setText("");
        otpVerifyStatus.setText("");
        changePasswordStatus.setText("");
        
        // Show overlay
        confirmOverlay.setVisible(true);
        confirmOverlay.setOpacity(0);
        
        FadeTransition overlayFade = new FadeTransition(Duration.millis(300), confirmOverlay);
        overlayFade.setFromValue(0);
        overlayFade.setToValue(1);
        overlayFade.play();
        
        // Show dialog
        changePasswordDialog.setVisible(true);
        changePasswordDialog.setTranslateY(-700);
        
        // Slide in animation from top
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), changePasswordDialog);
        slideIn.setFromY(-700);
        slideIn.setToY(0);
        slideIn.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), changePasswordDialog);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        ParallelTransition showDialog = new ParallelTransition(slideIn, fadeIn);
        showDialog.play();
        
        // Set up enter key handlers
        currentPasswordField.setOnAction(e -> onVerifyCurrentPassword());
        otpField.setOnAction(e -> onVerifyOTP());
        confirmPasswordField.setOnAction(e -> onSubmitPasswordChange());
    }
    
    @FXML
    public void closePasswordDialog() {
        // Fade out overlay
        FadeTransition overlayFadeOut = new FadeTransition(Duration.millis(300), confirmOverlay);
        overlayFadeOut.setFromValue(1);
        overlayFadeOut.setToValue(0);
        overlayFadeOut.setOnFinished(e -> confirmOverlay.setVisible(false));
        overlayFadeOut.play();
        
        // Slide out dialog
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), changePasswordDialog);
        slideOut.setFromY(0);
        slideOut.setToY(-700);
        slideOut.setInterpolator(javafx.animation.Interpolator.EASE_IN);
        
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), changePasswordDialog);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        
        ParallelTransition hide = new ParallelTransition(slideOut, fadeOut);
        hide.setOnFinished(e -> changePasswordDialog.setVisible(false));
        hide.play();
    }
    
    @FXML
    void onVerifyCurrentPassword() {
        String password = currentPasswordField.getText().trim();

        if (password.isEmpty()) {
            showPasswordMessage("Please enter your current password", false);
            return;
        }

        Session session = Session.getInstance();
        if (!password.equals(session.getPassword())) {
            showPasswordMessage("Incorrect password!", false);
            currentPasswordField.clear();
            currentPasswordField.requestFocus();
            return;
        }

        // Password correct, send OTP
        showPasswordLoading(true);
        verifyCurrentPasswordBtn.setDisable(true);
        passwordVerifyStatus.setText("Sending OTP...");

        new Thread(() -> {
            boolean success = otpService.sendOTP(session.getEmail());
            javafx.application.Platform.runLater(() -> {
                showPasswordLoading(false);
                if (success) {
                    showPasswordMessage("OTP sent to " + maskEmail(session.getEmail()), true);
                    showOtpPane();
                } else {
                    showPasswordMessage("Failed to send OTP", false);
                    verifyCurrentPasswordBtn.setDisable(false);
                }
            });
        }).start();
    }

    @FXML
    void onVerifyOTP() {
        String otp = otpField.getText().trim();

        if (otp.isEmpty()) {
            showPasswordMessage("Please enter the OTP", false);
            return;
        }

        if (otp.length() != 6 || !otp.matches("\\d+")) {
            showPasswordMessage("OTP must be 6 digits", false);
            return;
        }

        showPasswordLoading(true);
        verifyOtpBtn.setDisable(true);

        new Thread(() -> {
            boolean verified = otpService.verifyOTP(Session.getInstance().getEmail(), otp);
            javafx.application.Platform.runLater(() -> {
                showPasswordLoading(false);
                if (verified) {
                    showPasswordMessage("OTP verified successfully!", true);
                    showNewPasswordPane();
                } else {
                    showPasswordMessage("Invalid or expired OTP!", false);
                    verifyOtpBtn.setDisable(false);
                    otpField.clear();
                    otpField.requestFocus();
                }
            });
        }).start();
    }

    @FXML
    void onResendOTP() {
        showPasswordLoading(true);
        resendOtpBtn.setDisable(true);

        new Thread(() -> {
            boolean success = otpService.sendOTP(Session.getInstance().getEmail());
            javafx.application.Platform.runLater(() -> {
                showPasswordLoading(false);
                if (success) {
                    showPasswordMessage("OTP resent successfully!", true);
                } else {
                    showPasswordMessage("Failed to resend OTP", false);
                }
                resendOtpBtn.setDisable(false);
            });
        }).start();
    }

    @FXML
    void onSubmitPasswordChange() {        String newPassword = newPasswordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showPasswordMessage("Please fill in all fields", false);
            return;
        }

        if (newPassword.length() < 6) {
            showPasswordMessage("Password must be at least 6 characters", false);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showPasswordMessage("Passwords do not match!", false);
            return;
        }

        showPasswordLoading(true);
        changePasswordBtn.setDisable(true);

        new Thread(() -> {
            boolean success = updatePasswordInDatabase(Session.getInstance().getUserid(), newPassword);
            javafx.application.Platform.runLater(() -> {
                showPasswordLoading(false);
                if (success) {
                    showPasswordMessage("Password changed successfully!", true);
                    Session.getInstance().setPassword(newPassword);
                    
                    // Close dialog after 2 seconds
                    PauseTransition pause = new PauseTransition(Duration.seconds(2));
                    pause.setOnFinished(ev -> closePasswordDialog());
                    pause.play();
                } else {
                    showPasswordMessage("Failed to update password", false);
                    changePasswordBtn.setDisable(false);
                }
            });
        }).start();
    }
    
    private boolean updatePasswordInDatabase(int userId, String newPassword) {
        String sql = "UPDATE user_info SET password = SHA2(?, 256) WHERE user_id = ?";

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newPassword);
            ps.setInt(2, userId);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            return false;
        }
    }

    private void showOtpPane() {
        passwordVerificationPane.setVisible(false);
        otpVerificationPane.setVisible(true);
        newPasswordPane.setVisible(false);
        otpField.requestFocus();
    }

    private void showNewPasswordPane() {
        passwordVerificationPane.setVisible(false);
        otpVerificationPane.setVisible(false);
        newPasswordPane.setVisible(true);
        newPasswordField.requestFocus();
    }
    
    private void showPasswordLoading(boolean show) {
        passwordLoadingPane.setVisible(show);
    }

    private void showPasswordMessage(String message, boolean isSuccess) {
        if (currentPasswordAnimation != null) {
            currentPasswordAnimation.stop();
        }

        passwordMessagePane.setVisible(false);

        String type = isSuccess ? "success" : "error";
        String title = isSuccess ? "Success" : "Error";

        showToast(title, message, type);
    }
    
    // ========== TARGET PANE METHODS ==========
    
    private void showTargetPane() {
        // Load target management content if not already loaded
        if (targetContentPane.getChildren().isEmpty()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/adminTargetManagement.fxml"));
                javafx.scene.Node targetContent = loader.load();
                
                // Get the controller and pass reference to this dashboard controller
                adminTargetManagementController targetController = loader.getController();
                targetController.setDashboardController(this);
                
                targetContentPane.getChildren().add(targetContent);
            } catch (IOException e) {
                e.printStackTrace();
                showToast("Error", "Failed to load target management", "error");
                return;
            }
        }
        
        // Slide out settings pane to the right first
        TranslateTransition slideOutSettings = new TranslateTransition(Duration.millis(300), settingPane);
        slideOutSettings.setFromX(0);
        slideOutSettings.setToX(420);
        
        slideOutSettings.setOnFinished(e -> {
            settingPane.setVisible(false);
            settingPane.setTranslateX(0); // Reset position
            
            // Then slide in target pane from the right
            targetPane.setVisible(true);
            targetPane.setTranslateX(420);
            
            TranslateTransition slideInTarget = new TranslateTransition(Duration.millis(300), targetPane);
            slideInTarget.setFromX(420);
            slideInTarget.setToX(0);
            slideInTarget.play();
        });
        
        slideOutSettings.play();
    }
    
    private void hideTargetPane() {
        // Slide out target pane to the right first
        TranslateTransition slideOutTarget = new TranslateTransition(Duration.millis(300), targetPane);
        slideOutTarget.setFromX(0);
        slideOutTarget.setToX(420);
        
        slideOutTarget.setOnFinished(e -> {
            targetPane.setVisible(false);
            
            // Then slide in settings pane from the right
            settingPane.setVisible(true);
            settingPane.setTranslateX(420);
            
            TranslateTransition slideInSettings = new TranslateTransition(Duration.millis(300), settingPane);
            slideInSettings.setFromX(420);
            slideInSettings.setToX(0);
            slideInSettings.play();
        });
        
        slideOutTarget.play();
    }
    
    // ========== SALARY PANE METHODS ==========
    
    private void showSalaryPane() {
        // Load salary management content if not already loaded
        if (salaryContentPane.getChildren().isEmpty()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/adminSalaryManagement.fxml"));
                javafx.scene.Node salaryContent = loader.load();
                
                // Get the controller and pass reference to this dashboard controller
                adminSalaryManagementController salaryController = loader.getController();
                salaryController.setDashboardController(this);
                
                salaryContentPane.getChildren().add(salaryContent);
            } catch (IOException e) {
                e.printStackTrace();
                showToast("Error", "Failed to load salary management", "error");
                return;
            }
        }
        
        // Slide out settings pane to the right first
        TranslateTransition slideOutSettings = new TranslateTransition(Duration.millis(300), settingPane);
        slideOutSettings.setFromX(0);
        slideOutSettings.setToX(420);
        
        slideOutSettings.setOnFinished(e -> {
            settingPane.setVisible(false);
            settingPane.setTranslateX(0); // Reset position
            
            // Then slide in salary pane from the right
            salaryPane.setVisible(true);
            salaryPane.setTranslateX(420);
            
            TranslateTransition slideInSalary = new TranslateTransition(Duration.millis(300), salaryPane);
            slideInSalary.setFromX(420);
            slideInSalary.setToX(0);
            slideInSalary.play();
        });
        
        slideOutSettings.play();
    }
    
    private void hideSalaryPane() {
        // Slide out salary pane to the right first
        TranslateTransition slideOutSalary = new TranslateTransition(Duration.millis(300), salaryPane);
        slideOutSalary.setFromX(0);
        slideOutSalary.setToX(420);
        
        slideOutSalary.setOnFinished(e -> {
            salaryPane.setVisible(false);
            
            // Then slide in settings pane from the right
            settingPane.setVisible(true);
            settingPane.setTranslateX(420);
            
            TranslateTransition slideInSettings = new TranslateTransition(Duration.millis(300), settingPane);
            slideInSettings.setFromX(420);
            slideInSettings.setToX(0);
            slideInSettings.play();
        });
        
        slideOutSalary.play();
    }
    
    // ========== CONFIRMATION DIALOG METHODS ==========
    
    public void showConfirmDialog(String title, String message, String icon, Runnable onConfirm) {
        confirmTitle.setText(title);
        confirmMessage.setText(message);
        confirmIcon.setText(icon);
        confirmCallback = onConfirm;
        
        // Clear any custom content
        confirmContent.getChildren().clear();
        confirmContent.getChildren().add(confirmMessage);
        
        // Show overlay
        confirmOverlay.setVisible(true);
        confirmOverlay.setOpacity(0);
        
        FadeTransition overlayFade = new FadeTransition(Duration.millis(300), confirmOverlay);
        overlayFade.setFromValue(0);
        overlayFade.setToValue(1);
        overlayFade.play();
        
        // Show dialog
        confirmDialog.setVisible(true);
        confirmDialog.setTranslateY(-600);
        
        // Slide in animation from top
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), confirmDialog);
        slideIn.setFromY(-600);
        slideIn.setToY(0);
        slideIn.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), confirmDialog);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        ParallelTransition showDialog = new ParallelTransition(slideIn, fadeIn);
        showDialog.play();
    }
    
    public void showConfirmDialogWithDetails(String title, String icon, javafx.scene.Node detailsNode, Runnable onConfirm) {
        confirmTitle.setText(title);
        confirmIcon.setText(icon);
        confirmCallback = onConfirm;
        
        // Set custom content
        confirmContent.getChildren().clear();
        confirmContent.getChildren().add(detailsNode);
        
        // Show overlay
        confirmOverlay.setVisible(true);
        confirmOverlay.setOpacity(0);
        
        FadeTransition overlayFade = new FadeTransition(Duration.millis(300), confirmOverlay);
        overlayFade.setFromValue(0);
        overlayFade.setToValue(1);
        overlayFade.play();
        
        // Show dialog
        confirmDialog.setVisible(true);
        confirmDialog.setTranslateY(-600);
        
        // Slide in animation from top
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), confirmDialog);
        slideIn.setFromY(-600);
        slideIn.setToY(0);
        slideIn.setInterpolator(javafx.animation.Interpolator.EASE_OUT);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), confirmDialog);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        ParallelTransition showDialog = new ParallelTransition(slideIn, fadeIn);
        showDialog.play();
    }
    
    @FXML
    private void onConfirmOk() {
        hideConfirmDialog();
        if (confirmCallback != null) {
            confirmCallback.run();
            confirmCallback = null;
        }
    }
    
    @FXML
    private void onConfirmCancel() {
        hideConfirmDialog();
        confirmCallback = null;
    }
    
    private void hideConfirmDialog() {
        // Fade out overlay
        FadeTransition overlayFadeOut = new FadeTransition(Duration.millis(300), confirmOverlay);
        overlayFadeOut.setFromValue(1);
        overlayFadeOut.setToValue(0);
        overlayFadeOut.setOnFinished(e -> confirmOverlay.setVisible(false));
        overlayFadeOut.play();
        
        // Slide out dialog
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), confirmDialog);
        slideOut.setFromY(0);
        slideOut.setToY(-600);
        slideOut.setInterpolator(javafx.animation.Interpolator.EASE_IN);
        
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), confirmDialog);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        
        ParallelTransition hide = new ParallelTransition(slideOut, fadeOut);
        hide.setOnFinished(e -> confirmDialog.setVisible(false));
        hide.play();
    }

    /**
     * Checks if the account is locked on initialization
     */
    private void checkAccountLockStatus() {
        if (securityManager.isAccountLocked(current.getUserid(), "admin")) {
            showSecurityLockOverlay();
        }
    }

    /**
     * Shows the security lock overlay when account is locked
     */
    private void showSecurityLockOverlay() {
        if (securityLockOverlay != null) {
            // Hide all other panes first
            if (settingPane != null) settingPane.setVisible(false);
            if (passwordVerifyPane != null) passwordVerifyPane.setVisible(false);
            if (profilePane != null) profilePane.setVisible(false);
            if (targetPane != null) targetPane.setVisible(false);
            if (salaryPane != null) salaryPane.setVisible(false);
            if (overlayPane != null) overlayPane.setVisible(false);
            
            // Clear any form data
            if (verifyPasswordField != null) verifyPasswordField.clear();
            if (passwordErrorLabel != null) passwordErrorLabel.setVisible(false);
            
            // Remove any existing effects
            if (root != null) {
                root.setEffect(null);
                root.setDisable(true); // Disable the entire dashboard
            }
            
            // Show the security lock overlay
            securityLockOverlay.setVisible(true);
            securityLockOverlay.setOpacity(0);
            
            // Animate the overlay appearance
            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), securityLockOverlay);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
            
            // Start lock icon pulse animation
            startLockIconAnimation();
            
            // Update lock message with user-specific information
            updateLockMessage();
            
            // Start monitoring for unlock events
            Utils.UnlockMonitorService.getInstance().startMonitoring(
                current.getUserid(), 
                "admin", 
                this::hideSecurityLockOverlay
            );
        }
    }

    /**
     * Starts the pulsing animation for the lock icon
     */
    private void startLockIconAnimation() {
        if (lockIcon != null) {
            javafx.animation.ScaleTransition pulse = new javafx.animation.ScaleTransition(Duration.seconds(1.5), lockIcon);
            pulse.setFromX(1.0);
            pulse.setFromY(1.0);
            pulse.setToX(1.1);
            pulse.setToY(1.1);
            pulse.setCycleCount(javafx.animation.Timeline.INDEFINITE);
            pulse.setAutoReverse(true);
            pulse.play();
        }
    }

    /**
     * Updates the lock message with user-specific information
     */
    private void updateLockMessage() {
        if (lockMessage != null && current != null) {
            String message = String.format(
                "Hello %s, your administrator account has been temporarily locked due to multiple failed login attempts. " +
                "Please check your email (%s) for an unlock link to regain access immediately.",
                current.getUsername(),
                current.getEmail()
            );
            lockMessage.setText(message);
        }
    }

    /**
     * Hides the security lock overlay (called when account is unlocked)
     */
    public void hideSecurityLockOverlay() {
        if (securityLockOverlay != null && securityLockOverlay.isVisible()) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), securityLockOverlay);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                securityLockOverlay.setVisible(false);
                if (root != null) {
                    root.setDisable(false);
                }
            });
            fadeOut.play();
        }
    }

}
