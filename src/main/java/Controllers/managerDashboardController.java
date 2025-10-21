package Controllers;

import Database.DatabaseConnectionManager;
import MainUI.login;
import Utils.LogoutHelper;
import Utils.OTPService;
import Utils.Session;
import javafx.animation.*;
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
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class managerDashboardController {
    private static final Logger logger = LoggerFactory.getLogger(managerDashboardController.class);

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
    private HBox optionProfile, optionAttendance, optionChange;

    @FXML
    private Button orderbtn;

    @FXML
    private Pane overlayPane;

    @FXML
    private Button overviewbtn;

    @FXML
    private ImageView pfImage, profileImage;

    @FXML
    private Label profileName, profileDOB;

    @FXML
    private TextField profileAddress, profilePhone, profileEmail;

    @FXML
    private VBox settingPane, passwordVerifyPane, profilePane, attendancePane;
    
    @FXML
    private StackPane attendanceContentPane;

    @FXML
    private BorderPane root;

    @FXML
    private Label settingIcon;

    @FXML
    private Button staffbtn;

    // Toast Notification Components
    @FXML
    private HBox toastNotification;
    
    @FXML
    private Label toastIcon, toastTitle, toastMessage;
    
    // Confirmation Dialog Components
    @FXML
    private VBox confirmDialog, confirmContent;
    
    @FXML
    private Pane confirmOverlay;
    
    @FXML
    private Label confirmIcon, confirmTitle, confirmMessage;
    
    @FXML
    private Button confirmOkBtn, confirmCancelBtn;
    
    private Runnable confirmCallback;
    
    // Change Password Dialog Components
    @FXML
    private VBox changePasswordDialog, passwordVerificationPane, otpVerificationPane, newPasswordPane;
    
    @FXML
    private PasswordField currentPasswordField, newPasswordField, confirmPasswordField, verifyPasswordField;
    
    @FXML
    private TextField otpField;
    
    @FXML
    private Button verifyCurrentPasswordBtn, verifyOtpBtn, resendOtpBtn, changePasswordBtn;
    @FXML
    private Button verifyPasswordBtn, cancelVerifyBtn, saveProfileBtn, editProfilePhotoBtn;
    
    @FXML
    private Label passwordVerifyStatus, otpVerifyStatus, changePasswordStatus, otpEmailLabel;
    @FXML
    private Label passwordErrorLabel, backToSettingsBtn, backToSettingsFromProfile, backToSettingsFromAttendance, editProfileBtn;
    
    @FXML
    private StackPane passwordMessagePane, passwordLoadingPane;
    
    @FXML
    private Label passwordMessageLabel, closePasswordDialogBtn;
    
    private final OTPService otpService = OTPService.getInstance();
    private SequentialTransition currentPasswordAnimation;

    private Button activeButton;
    private int adminid;
    private File selectedProfilePhoto;
    private String cachedPhotoPath;
    Session current = Session.getInstance();
    private SequentialTransition animation;

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

    private void clickSetting(){
        openSetting();
        overlayPane.setOnMouseClicked(e->{
            closeSetting();
        });

    }


    public void initialize() throws IOException {
        String name = current.getUsername();
        nameLbl.setText(name);
        profileName.setText(name);
        profileAddress.setText(current.getAddress());
        profileEmail.setText(current.getEmail());
        profileDOB.setText(current.getDob().toString());
        profilePhone.setText(current.getPhone());
        
        // Add clipping to change password dialog to hide notification overflow
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        clip.widthProperty().bind(changePasswordDialog.widthProperty());
        clip.heightProperty().bind(changePasswordDialog.heightProperty());
        clip.setArcWidth(12);
        clip.setArcHeight(12);
        changePasswordDialog.setClip(clip);

        // Profile view click - show password verification
        optionProfile.setOnMouseClicked(e -> {
            showPasswordVerification();
        });
        
        // Change password click in settings
        optionChange.setOnMouseClicked(e -> {
            showChangePasswordDialog();
        });
        
        // Attendance click in settings
        optionAttendance.setOnMouseClicked(e -> {
            showAttendancePane();
        });
        
        // Back to settings from profile
        if (backToSettingsBtn != null) {
            backToSettingsBtn.setOnMouseClicked(e -> {
                hidePasswordVerification();
            });
        }
        
        // Back to settings from profile
        if (backToSettingsFromProfile != null) {
            backToSettingsFromProfile.setOnMouseClicked(e -> {
                hideProfilePane();
            });
        }
        
        // Back to settings from attendance
        if (backToSettingsFromAttendance != null) {
            backToSettingsFromAttendance.setOnMouseClicked(e -> {
                hideAttendancePane();
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

        settingPane.setTranslateX(420);
        overlayPane.setVisible(false);
        settingIcon.setOnMouseClicked(e -> {
            clickSetting();
        });

        overlayPane.setOnMouseClicked(e -> {
            if (overlayPane.isVisible()) {
                closeAllPanesAndResetToSettings();
            }
        });

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
                            } catch (Exception ex) {
                                logger.error("Error during window close logout", ex);
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

        Session current = Session.getInstance();
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

    private void openSetting() {
        // Ensure only settings pane is visible
        settingPane.setVisible(true);
        passwordVerifyPane.setVisible(false);
        profilePane.setVisible(false);
        attendancePane.setVisible(false);
        
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

    private void loadCurrentProfilePhoto() {
        // Load profile photo logic here
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

    private void showAttendancePane() {
        // Slide out settings pane first
        TranslateTransition slideOutSettings = new TranslateTransition(Duration.millis(300), settingPane);
        slideOutSettings.setFromX(0);
        slideOutSettings.setToX(420);
        slideOutSettings.setOnFinished(e -> {
            settingPane.setVisible(false);
            
            // Load attendance management content
            try {
                Parent attendanceContent = FXMLLoader.load(getClass().getResource("/View/managerAttendanceManagement.fxml"));
                attendanceContentPane.getChildren().clear();
                attendanceContentPane.getChildren().add(attendanceContent);
            } catch (IOException ex) {
                ex.printStackTrace();
                showToast("Error", "Failed to load attendance management", "error");
                return;
            }
            
            // Then slide in attendance pane
            attendancePane.setVisible(true);
            attendancePane.setTranslateX(420);
            
            TranslateTransition slideInAttendance = new TranslateTransition(Duration.millis(300), attendancePane);
            slideInAttendance.setFromX(420);
            slideInAttendance.setToX(0);
            slideInAttendance.play();
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

    private void hideProfilePane() {
        closeProfilePane(true);
    }
    
    private void closeProfilePane(boolean returnToSettings) {
        // Slide out profile pane first
        TranslateTransition slideOutProfile = new TranslateTransition(Duration.millis(300), profilePane);
        slideOutProfile.setFromX(0);
        slideOutProfile.setToX(420);
        slideOutProfile.setOnFinished(e -> {
            profilePane.setVisible(false);
            
            if (returnToSettings) {
                // Prepare settings pane off-screen before making it visible
                settingPane.setTranslateX(420);
                settingPane.setVisible(true);
                
                // Then slide in settings pane
                TranslateTransition slideInSettings = new TranslateTransition(Duration.millis(300), settingPane);
                slideInSettings.setFromX(420);
                slideInSettings.setToX(0);
                slideInSettings.play();
            } else {
                // Just close everything
                closeSetting();
            }
        });
        slideOutProfile.play();
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
        
        // Hide verification pane and show profile pane
        passwordVerifyPane.setVisible(false);
        profilePane.setVisible(true);
        profilePane.setTranslateX(420);
        
        TranslateTransition slideInProfile = new TranslateTransition(Duration.millis(300), profilePane);
        slideInProfile.setFromX(420);
        slideInProfile.setToX(0);
        slideInProfile.play();
        
        // Load current profile photo
        loadCurrentProfilePhoto();
    }

    private void hideAttendancePane() {
        // Slide out attendance pane first
        TranslateTransition slideOutAttendance = new TranslateTransition(Duration.millis(300), attendancePane);
        slideOutAttendance.setFromX(0);
        slideOutAttendance.setToX(420);
        slideOutAttendance.setOnFinished(e -> {
            attendancePane.setVisible(false);
            
            // Prepare settings pane off-screen before making it visible
            settingPane.setTranslateX(420);
            settingPane.setVisible(true);
            
            // Then slide in settings pane
            TranslateTransition slideInSettings = new TranslateTransition(Duration.millis(300), settingPane);
            slideInSettings.setFromX(420);
            slideInSettings.setToX(0);
            slideInSettings.play();
        });
        slideOutAttendance.play();
    }

    private void toggleEditMode() {
        boolean isEditMode = profileEmail.isEditable();
        
        if (!isEditMode) {
            // Enter edit mode
            profileEmail.setEditable(true);
            profilePhone.setEditable(true);
            profileAddress.setEditable(true);
            
            // Show save button and edit photo button
            saveProfileBtn.setVisible(true);
            editProfilePhotoBtn.setVisible(true);
            
            // Change edit button icon to indicate edit mode
            editProfileBtn.setText("✓");
            
            // Apply edit mode styling
            profileEmail.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 12; -fx-font-size: 14; -fx-border-color: #667eea; -fx-border-width: 2; -fx-border-radius: 8;");
            profilePhone.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 12; -fx-font-size: 14; -fx-border-color: #667eea; -fx-border-width: 2; -fx-border-radius: 8;");
            profileAddress.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 12; -fx-font-size: 14; -fx-border-color: #667eea; -fx-border-width: 2; -fx-border-radius: 8;");
        } else {
            // Exit edit mode
            profileEmail.setEditable(false);
            profilePhone.setEditable(false);
            profileAddress.setEditable(false);
            
            // Hide save button and edit photo button
            saveProfileBtn.setVisible(false);
            editProfilePhotoBtn.setVisible(false);
            
            // Change edit button icon back
            editProfileBtn.setText("✏");
            
            // Restore normal styling
            profileEmail.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 8; -fx-padding: 12; -fx-font-size: 14; -fx-text-fill: #1f2937; -fx-border-color: transparent;");
            profilePhone.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 8; -fx-padding: 12; -fx-font-size: 14; -fx-text-fill: #1f2937; -fx-border-color: transparent;");
            profileAddress.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 8; -fx-padding: 12; -fx-font-size: 14; -fx-text-fill: #1f2937; -fx-border-color: transparent;");
        }
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
            // Password correct - transition directly to profile pane
            showProfilePane();
        } else {
            passwordErrorLabel.setText("Incorrect password");
            passwordErrorLabel.setVisible(true);
        }
    }

    private void closeAllPanesAndResetToSettings() {
        // Determine which pane is currently visible and close it
        if (profilePane.isVisible()) {
            closeProfilePane(false);
        } else if (attendancePane.isVisible()) {
            // Close attendance pane directly
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), attendancePane);
            slideOut.setFromX(0);
            slideOut.setToX(420);
            
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), overlayPane);
            fadeOut.setFromValue(0.5);
            fadeOut.setToValue(0);
            
            ParallelTransition hide = new ParallelTransition(slideOut, fadeOut);
            hide.setOnFinished(e -> {
                attendancePane.setVisible(false);
                overlayPane.setVisible(false);
                root.setEffect(null);
                root.setDisable(false);
            });
            hide.play();
        } else if (passwordVerifyPane.isVisible()) {
            // Close password verification pane
            TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), passwordVerifyPane);
            slideOut.setFromX(0);
            slideOut.setToX(420);
            
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), overlayPane);
            fadeOut.setFromValue(0.5);
            fadeOut.setToValue(0);
            
            ParallelTransition hide = new ParallelTransition(slideOut, fadeOut);
            hide.setOnFinished(e -> {
                passwordVerifyPane.setVisible(false);
                overlayPane.setVisible(false);
                root.setEffect(null);
                root.setDisable(false);
                verifyPasswordField.clear();
                passwordErrorLabel.setVisible(false);
            });
            hide.play();
        } else {
            // Close settings pane
            closeSetting();
        }
    }

    // Toast notification methods
    public void showToast(String title, String message, String type) {
        toastTitle.setText(title);
        toastMessage.setText(message);

        String normalizedType = (type == null ? "info" : type.toLowerCase());

        // Set icon and color based on type
        StackPane iconContainer = (StackPane) toastIcon.getParent();
        switch (normalizedType) {
            case "success":
                toastIcon.setText("✓");
                iconContainer.setStyle("-fx-background-color: #10b981; -fx-background-radius: 18;");
                break;
            case "error":
                toastIcon.setText("✕");
                iconContainer.setStyle("-fx-background-color: #ef4444; -fx-background-radius: 18;");
                break;
            case "warning":
                toastIcon.setText("!");
                iconContainer.setStyle("-fx-background-color: #f59e0b; -fx-background-radius: 18;");
                break;
            default:
                toastIcon.setText("ℹ");
                iconContainer.setStyle("-fx-background-color: #3b82f6; -fx-background-radius: 18;");
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

    // Confirmation dialog methods
    @FXML
    private void onConfirmOk() {
        // Confirm OK
    }

    @FXML
    private void onConfirmCancel() {
        // Confirm Cancel
    }

    // Change password methods
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

        if (otpService.verifyOTP(Session.getInstance().getEmail(), otp)) {
            showPasswordMessage("OTP verified successfully!", true);
            showNewPasswordPane();
        } else {
            showPasswordMessage("Invalid OTP. Please try again.", false);
            otpField.clear();
            otpField.requestFocus();
        }
    }

    @FXML
    void onResendOTP() {
        resendOtpBtn.setDisable(true);
        otpVerifyStatus.setText("Resending OTP...");

        new Thread(() -> {
            boolean success = otpService.sendOTP(Session.getInstance().getEmail());
            javafx.application.Platform.runLater(() -> {
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
    void onSubmitPasswordChange() {
        String newPassword = newPasswordField.getText().trim();
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
            showPasswordMessage("Passwords do not match", false);
            return;
        }

        // Update password in database and session
        changePasswordBtn.setDisable(true);
        changePasswordStatus.setText("Updating password...");

        new Thread(() -> {
            try {
                // Update password in database
                Connection con = DatabaseConnectionManager.getInstance().getConnection();
                PreparedStatement stmt = con.prepareStatement("UPDATE users SET password = ? WHERE userid = ?");
                stmt.setString(1, newPassword);
                stmt.setInt(2, current.getUserid());
                
                int result = stmt.executeUpdate();
                con.close();

                javafx.application.Platform.runLater(() -> {
                    if (result > 0) {
                        // Update session password
                        current.setPassword(newPassword);
                        showPasswordMessage("Password changed successfully!", true);
                        
                        // Close dialog after 2 seconds
                        PauseTransition pause = new PauseTransition(Duration.seconds(2));
                        pause.setOnFinished(e -> closePasswordDialog());
                        pause.play();
                    } else {
                        showPasswordMessage("Failed to update password", false);
                        changePasswordBtn.setDisable(false);
                    }
                });
            } catch (Exception ex) {
                javafx.application.Platform.runLater(() -> {
                    showPasswordMessage("Error updating password", false);
                    changePasswordBtn.setDisable(false);
                });
                ex.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void onSaveProfile() {
        try {
            // Get updated values
            String email = profileEmail.getText().trim();
            String phone = profilePhone.getText().trim();
            String address = profileAddress.getText().trim();
            
            // Validate inputs
            if (email.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                showToast("Error", "Please fill in all fields", "error");
                return;
            }
            
            // Update in database
            Connection con = DatabaseConnectionManager.getInstance().getConnection();
            PreparedStatement stmt = con.prepareStatement(
                "UPDATE users SET email = ?, phone = ?, address = ? WHERE userid = ?"
            );
            stmt.setString(1, email);
            stmt.setString(2, phone);
            stmt.setString(3, address);
            stmt.setInt(4, current.getUserid());
            
            int result = stmt.executeUpdate();
            con.close();
            
            if (result > 0) {
                // Update session
                current.setEmail(email);
                current.setPhone(phone);
                current.setAddress(address);
                
                // Exit edit mode
                toggleEditMode();
                
                // Show success message
                showToast("Success", "Profile updated successfully!", "success");
            } else {
                showToast("Error", "Failed to update profile", "error");
            }
        } catch (Exception ex) {
            showToast("Error", "Failed to update profile", "error");
            ex.printStackTrace();
        }
    }

    @FXML
    private void onEditProfileImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Photo");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        Stage stage = (Stage) pfImage.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            try {
                // Load and display the new image
                Image newImage = new Image(selectedFile.toURI().toString());
                pfImage.setImage(newImage);
                profileImage.setImage(newImage);
                
                // Save the image path or copy to a specific location
                selectedProfilePhoto = selectedFile;
                
                // Optionally copy to a permanent location
                String userPhotoDir = "src/main/resources/Image/profiles/";
                File photoDir = new File(userPhotoDir);
                if (!photoDir.exists()) {
                    photoDir.mkdirs();
                }
                
                String fileName = "manager_" + current.getUserid() + getFileExtension(selectedFile.getName());
                Path targetPath = Paths.get(userPhotoDir + fileName);
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                
                // Update database with photo path
                Connection con = DatabaseConnectionManager.getInstance().getConnection();
                PreparedStatement stmt = con.prepareStatement(
                    "UPDATE users SET photo_path = ? WHERE userid = ?"
                );
                stmt.setString(1, "profiles/" + fileName);
                stmt.setInt(2, current.getUserid());
                stmt.executeUpdate();
                con.close();
                
                cachedPhotoPath = "profiles/" + fileName;
                
                showToast("Success", "Profile photo updated!", "success");
            } catch (Exception ex) {
                showToast("Error", "Failed to update profile photo", "error");
                ex.printStackTrace();
            }
        }
    }
    
    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex);
        }
        return ".png";
    }

    @FXML
    private void onCancelVerify() {
        hidePasswordVerification();
    }
    
    // Helper methods for change password dialog
    private void showPasswordMessage(String message, boolean isSuccess) {
        passwordMessageLabel.setText(message);
        passwordMessagePane.setVisible(true);
        
        StackPane iconContainer = (StackPane) passwordMessagePane.lookup(".icon-container");
        if (iconContainer == null) {
            iconContainer = new StackPane();
            iconContainer.getStyleClass().add("icon-container");
        }
        
        if (isSuccess) {
            iconContainer.setStyle("-fx-background-color: #10b981; -fx-background-radius: 12;");
        } else {
            iconContainer.setStyle("-fx-background-color: #ef4444; -fx-background-radius: 12;");
        }
        
        // Auto hide after 3 seconds
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> passwordMessagePane.setVisible(false));
        pause.play();
    }
    
    private void showPasswordLoading(boolean show) {
        passwordLoadingPane.setVisible(show);
    }
    
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
    
    private void showOtpPane() {
        passwordVerificationPane.setVisible(false);
        otpVerificationPane.setVisible(true);
        otpField.requestFocus();
    }
    
    private void showNewPasswordPane() {
        otpVerificationPane.setVisible(false);
        newPasswordPane.setVisible(true);
        newPasswordField.requestFocus();
    }

}
