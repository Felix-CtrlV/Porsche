package Controllers;

import Database.DatabaseConnectionManager;
import MainUI.login;
import Utils.LogoutHelper;
import Utils.OTPService;
import Utils.Session;
import Utils.SecurityManager;
import Utils.SecurityEmailService;
import javafx.animation.*;
import javafx.application.Platform;
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
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
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

    // Profile management variables
    private File selectedProfilePhoto;
    private String cachedPhotoPath;

    @FXML
    private Label passwordVerifyStatus, otpVerifyStatus, changePasswordStatus, otpEmailLabel;
    @FXML
    private Label passwordErrorLabel, backToSettingsBtn, backToSettingsFromProfile, backToSettingsFromAttendance, editProfileBtn;

    @FXML
    private StackPane passwordMessagePane, passwordLoadingPane;

    @FXML
    private Label passwordMessageLabel, closePasswordDialogBtn;

    // Security Lock Overlay Components
    @FXML
    private StackPane securityLockOverlay;
    
    @FXML
    private Label lockIcon, lockMessage;

    private final OTPService otpService = OTPService.getInstance();
    private SequentialTransition currentPasswordAnimation;
    private final SecurityManager securityManager = SecurityManager.getInstance();
    private final SecurityEmailService emailService = SecurityEmailService.getInstance();

    private Button activeButton;
    private int adminid;
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
    }

    @FXML
    private StackPane rootPane;  // Assuming this is your root in FXML
    @FXML
    private Label sun, moon, textMode;
    @FXML
    private ToggleButton Mode;  // Assuming Mode is a Button in FXML

    public static boolean isDarkMode = false;

    public void initialize() throws IOException {
        // Load the CSS file once (do NOT clear it later)
        rootPane.getStylesheets().add(
                getClass().getResource("/CSS/manager_light_mode.css").toExternalForm()
        );

        // Initial state: Light mode (default, as per CSS :root)
        sun.setVisible(true);
        moon.setVisible(false);
        textMode.setText("Light Mode");
        isDarkMode = false;

        // Toggle button action
        Mode.setOnAction(e -> {
            rootPane.getStylesheets().clear();
            if (!isDarkMode) {
                // Switch to Dark Mode

                rootPane.getStylesheets().add(
                        getClass().getResource("/CSS/manager_dark_mode.css").toExternalForm()
                );// Add the class to rootPane
                sun.setVisible(false);
                moon.setVisible(true);
                textMode.setText("Dark Mode");
                isDarkMode = true;
            } else {
                // Switch to Light Mode

                rootPane.getStylesheets().add(
                        getClass().getResource("/CSS/manager_light_mode.css").toExternalForm()
                );// Remove the class from rootPane
                sun.setVisible(true);
                moon.setVisible(false);
                textMode.setText("Light Mode");
                isDarkMode = false;
            }
        });

        String name = current.getUsername();
        nameLbl.setText(name);
        profileName.setText(name);
        profileAddress.setText(current.getAddress());
        profileEmail.setText(current.getEmail());
        profileDOB.setText(current.getDob().toString());
        profilePhone.setText(current.getPhone());

        // Preload attendance content in background to eliminate delay
        preloadAttendanceContent();
        
        // Check if account is locked on initialization
        checkAccountLockStatus();

        // Add clipping to change password dialog to hide notification overflow
        Rectangle clip = new Rectangle();
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
            showAttendanceDialog();
        });

        // Back to settings from password verification
        if (backToSettingsBtn != null) {
            backToSettingsBtn.setOnMouseClicked(e -> {
                // Check which pane is currently visible and handle accordingly
                if (profilePane.isVisible()) {
                    hideProfilePane();
                } else if (passwordVerifyPane.isVisible()) {
                    hidePasswordVerification();
                }
            });
        }

        // Back to settings from profile (if separate button exists)
        if (backToSettingsFromProfile != null) {
            backToSettingsFromProfile.setOnMouseClicked(e -> {
                hideProfilePane();
            });
        }

        // Back to settings from attendance (now just closes the dialog)
        if (backToSettingsFromAttendance != null) {
            backToSettingsFromAttendance.setOnMouseClicked(e -> {
                closeAttendanceDialog();
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
                if (event.getCode() == KeyCode.ENTER) {
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

        // Initialize profile image with circular clipping
        applyCircularClip(pfImage, 120);
        applyCircularClip(profileImage, 48);

        if (editProfilePhotoBtn != null) {
            editProfilePhotoBtn.setVisible(false);
        }

        loadCurrentProfilePhoto();

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
        Task<FXMLLoader> task = new Task<>() {
            @Override
            protected FXMLLoader call() throws Exception {
                FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource(fxmlPath)));
                loader.load();
                return loader;
            }
        };
        task.setOnSucceeded(e -> {
            FXMLLoader loader = task.getValue();
            Parent pane = loader.getRoot();
            Object controller = loader.getController();
            
            // Set dashboard controller reference for toast notifications
            if (controller instanceof managerInventoryController) {
                ((managerInventoryController) controller).setDashboardController(this);
            } else if (controller instanceof managerAttendanceManagementController) {
                ((managerAttendanceManagementController) controller).setDashboardController(this);
            }
            
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
        hide.setOnFinished(e -> {
            // Reset all pane states
            settingPane.setVisible(false);
            passwordVerifyPane.setVisible(false);
            profilePane.setVisible(false);
            attendancePane.setVisible(false);
            overlayPane.setVisible(false);

            // Clear any form data
            if (verifyPasswordField != null) {
                verifyPasswordField.clear();
            }
            if (passwordErrorLabel != null) {
                passwordErrorLabel.setVisible(false);
            }

            root.setEffect(null);
            root.setDisable(false);
        });

        animation = new SequentialTransition(hide);
        animation.play();
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

    // Preloaded attendance content
    private Parent attendanceContent = null;
    
    // Preload attendance content in background
    private void preloadAttendanceContent() {
        new Thread(() -> {
            try {
                Parent content = FXMLLoader.load(getClass().getResource("/View/managerAttendanceManagement.fxml"));
                Platform.runLater(() -> {
                    this.attendanceContent = content;
                });
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }).start();
    }
    
    private void showAttendanceDialog() {
        // Hide settings pane directly without full close animation
        if (settingPane != null) {
            settingPane.setVisible(false);
        }

        // Show overlay and blur background immediately
        overlayPane.setVisible(true);
        overlayPane.setOpacity(0);
        root.setEffect(new GaussianBlur(10));
        root.setDisable(true);

        // Show attendance dialog
        attendancePane.setVisible(true);
        attendancePane.setTranslateY(-700);

        // Start animations immediately
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), overlayPane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(0.5);

        TranslateTransition slideDown = new TranslateTransition(Duration.millis(400), attendancePane);
        slideDown.setFromY(-700);
        slideDown.setToY(0);
        slideDown.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition showDialog = new ParallelTransition(fadeIn, slideDown);
        
        // Load attendance management content (use preloaded content if available)
        if (attendanceContent != null) {
            // Use preloaded content
            attendanceContentPane.getChildren().clear();
            attendanceContentPane.getChildren().add(attendanceContent);
            showDialog.play();
        } else {
            // Load content if not preloaded yet
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/managerAttendanceManagement.fxml"));
                Parent content = loader.load();
                
                // Set dashboard controller reference
                Object controller = loader.getController();
                if (controller instanceof managerAttendanceManagementController) {
                    ((managerAttendanceManagementController) controller).setDashboardController(this);
                }
                
                attendanceContentPane.getChildren().clear();
                attendanceContentPane.getChildren().add(content);
                this.attendanceContent = content;
                showDialog.play();
            } catch (IOException ex) {
                ex.printStackTrace();
                showToast("Error", "Failed to load attendance management", "error");
                return;
            }
        }

        // Disable overlay click for a short time to prevent immediate closing
        overlayPane.setDisable(true);
        PauseTransition enableOverlay = new PauseTransition(Duration.millis(500));
        enableOverlay.setOnFinished(e -> overlayPane.setDisable(false));
        enableOverlay.play();
    }

    @FXML
    private void closeAttendanceDialog() {
        // Animate dialog slide up
        TranslateTransition slideUp = new TranslateTransition(Duration.millis(400), attendancePane);
        slideUp.setFromY(0);
        slideUp.setToY(-700);
        slideUp.setInterpolator(Interpolator.EASE_IN);

        // Animate overlay fade out
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), overlayPane);
        fadeOut.setFromValue(0.5);
        fadeOut.setToValue(0);

        ParallelTransition hideDialog = new ParallelTransition(slideUp, fadeOut);
        hideDialog.setOnFinished(e -> {
            attendancePane.setVisible(false);
            overlayPane.setVisible(false);
            root.setEffect(null);
            root.setDisable(false);
        });
        hideDialog.play();
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
        if (!profilePane.isVisible()) {
            return;
        }

        // Slide out profile pane first
        TranslateTransition slideOutProfile = new TranslateTransition(Duration.millis(300), profilePane);
        slideOutProfile.setFromX(0);
        slideOutProfile.setToX(420);
        slideOutProfile.setOnFinished(e -> {
            profilePane.setVisible(false);
            profilePane.setTranslateX(0); // Reset position to prevent overlap

            if (returnToSettings) {
                // Reset any edit mode states
                if (profileEmail != null) {
                    profileEmail.setEditable(false);
                    profilePhone.setEditable(false);
                    profileAddress.setEditable(false);
                    saveProfileBtn.setVisible(false);
                    if (editProfilePhotoBtn != null) {
                        editProfilePhotoBtn.setVisible(false);
                    }
                    editProfileBtn.setText("✏");
                }

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
        // First slide out the password verification pane
        TranslateTransition slideOutVerify = new TranslateTransition(Duration.millis(300), passwordVerifyPane);
        slideOutVerify.setFromX(0);
        slideOutVerify.setToX(420);
        slideOutVerify.setOnFinished(e -> {
            passwordVerifyPane.setVisible(false);
            verifyPasswordField.clear();
            passwordErrorLabel.setVisible(false);

            // Ensure settings pane is completely hidden
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

            // Apply non-editable styling
            profileEmail.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 8; -fx-padding: 12; -fx-font-size: 14; -fx-text-fill: #1f2937; -fx-border-color: transparent; -fx-prompt-text-fill: #9ca3af;");
            profilePhone.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 8; -fx-padding: 12; -fx-font-size: 14; -fx-text-fill: #1f2937; -fx-border-color: transparent; -fx-prompt-text-fill: #9ca3af;");
            profileAddress.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 8; -fx-padding: 12; -fx-font-size: 14; -fx-text-fill: #1f2937; -fx-border-color: transparent; -fx-prompt-text-fill: #9ca3af;");

            // Hide buttons initially
            saveProfileBtn.setVisible(false);
            if (editProfilePhotoBtn != null) {
                editProfilePhotoBtn.setVisible(false);
            }
            editProfileBtn.setText("✏");

            // Show profile pane
            profilePane.setVisible(true);
            profilePane.setTranslateX(420);

            TranslateTransition slideInProfile = new TranslateTransition(Duration.millis(300), profilePane);
            slideInProfile.setFromX(420);
            slideInProfile.setToX(0);
            slideInProfile.play();

            // Load current profile photo
            loadCurrentProfilePhoto();
        });
        slideOutVerify.play();
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
            profileEmail.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 8; -fx-padding: 12; -fx-font-size: 14; -fx-text-fill: #1f2937; -fx-border-color: transparent; -fx-prompt-text-fill: #9ca3af;");
            profilePhone.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 8; -fx-padding: 12; -fx-font-size: 14; -fx-text-fill: #1f2937; -fx-border-color: transparent; -fx-prompt-text-fill: #9ca3af;");
            profileAddress.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 8; -fx-padding: 12; -fx-font-size: 14; -fx-text-fill: #1f2937; -fx-border-color: transparent; -fx-prompt-text-fill: #9ca3af;");
            saveProfileBtn.setVisible(false);
            editProfilePhotoBtn.setVisible(false);
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
    private void onVerifyPassword() {
        String enteredPassword = verifyPasswordField.getText();

        if (enteredPassword.isEmpty()) {
            passwordErrorLabel.setText("Please enter your password");
            passwordErrorLabel.setVisible(true);
            return;
        }

        // Check if account is already locked
        if (securityManager.isAccountLocked(current.getUserid(), "manager")) {
            showSecurityLockOverlay();
            return;
        }

        // Verify password against session
        String sessionPassword = current.getPassword();

        if (sessionPassword != null && sessionPassword.equals(enteredPassword)) {
            // Password correct - reset attempts and transition to profile pane
            securityManager.resetAttempts(current.getUserid(), "manager");
            showProfilePane();
        } else {
            // Password incorrect - clear field immediately for instant feedback
            verifyPasswordField.clear();
            
            // Record failed attempt (now optimized with caching and async operations)
            boolean shouldLock = securityManager.recordFailedAttempt(current.getUserid(), "manager");
            
            if (shouldLock) {
                // Account locked - send notifications asynchronously and show lock overlay immediately
                emailService.sendBruteForceAlert(current.getUserid(), "manager");
                showSecurityLockOverlay();
            } else {
                // Show remaining attempts immediately (from cache)
                int remaining = securityManager.getRemainingAttempts(current.getUserid(), "manager");
                passwordErrorLabel.setText("Incorrect password. " + remaining + " attempts remaining.");
                passwordErrorLabel.setVisible(true);
            }
        }
    }

    /**
     * Checks if the account is locked on initialization
     */
    private void checkAccountLockStatus() {
        if (securityManager.isAccountLocked(current.getUserid(), "manager")) {
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
            if (attendancePane != null) attendancePane.setVisible(false);
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
                "manager", 
                this::hideSecurityLockOverlay
            );
        }
    }

    /**
     * Starts the pulsing animation for the lock icon
     */
    private void startLockIconAnimation() {
        if (lockIcon != null) {
            ScaleTransition pulse = new ScaleTransition(Duration.seconds(1.5), lockIcon);
            pulse.setFromX(1.0);
            pulse.setFromY(1.0);
            pulse.setToX(1.1);
            pulse.setToY(1.1);
            pulse.setCycleCount(Timeline.INDEFINITE);
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
                "Hello %s, your account has been temporarily locked due to multiple failed login attempts. " +
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

    private void closeAllPanesAndResetToSettings() {
        // Determine which pane is currently visible and close it completely
        if (profilePane.isVisible()) {
            closeProfilePane(false);
        } else if (attendancePane.isVisible()) {
            closeAttendanceDialog();
        } else if (passwordVerifyPane.isVisible()) {
            closePasswordVerificationCompletely();
        } else if (settingPane.isVisible()) {
            // Close settings pane completely
            closeSetting();
        }
    }

    private void closePasswordVerificationCompletely() {
        // Close password verification pane completely (not back to settings)
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
    }

    private void closeAttendancePaneCompletely() {
        // Close attendance pane completely (not back to settings)
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
    /**
     * Show a confirmation dialog with custom title and message
     */
    public void showConfirmationDialog(String title, String message, Runnable onConfirm) {
        confirmTitle.setText(title);
        confirmMessage.setText(message);
        confirmCallback = onConfirm;

        // Show overlay and dialog
        confirmOverlay.setVisible(true);
        confirmOverlay.setOpacity(0);
        confirmDialog.setVisible(true);
        confirmDialog.setOpacity(0);

        FadeTransition overlayFade = new FadeTransition(Duration.millis(300), confirmOverlay);
        overlayFade.setFromValue(0);
        overlayFade.setToValue(0.5);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), confirmDialog);
        slideIn.setFromY(-100);
        slideIn.setToY(0);

        FadeTransition dialogFade = new FadeTransition(Duration.millis(300), confirmDialog);
        dialogFade.setFromValue(0);
        dialogFade.setToValue(1);

        ParallelTransition show = new ParallelTransition(overlayFade, slideIn, dialogFade);
        show.play();
    }

    @FXML
    private void onConfirmOk() {
        // Execute callback if set
        if (confirmCallback != null) {
            confirmCallback.run();
        }
        closeConfirmationDialog();
    }

    @FXML
    private void onConfirmCancel() {
        closeConfirmationDialog();
    }

    private void closeConfirmationDialog() {
        FadeTransition overlayFadeOut = new FadeTransition(Duration.millis(300), confirmOverlay);
        overlayFadeOut.setFromValue(0.5);
        overlayFadeOut.setToValue(0);
        overlayFadeOut.setOnFinished(e -> confirmOverlay.setVisible(false));

        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), confirmDialog);
        slideOut.setFromY(0);
        slideOut.setToY(-100);

        FadeTransition dialogFadeOut = new FadeTransition(Duration.millis(300), confirmDialog);
        dialogFadeOut.setFromValue(1);
        dialogFadeOut.setToValue(0);
        dialogFadeOut.setOnFinished(e -> confirmDialog.setVisible(false));

        ParallelTransition hide = new ParallelTransition(overlayFadeOut, slideOut, dialogFadeOut);
        hide.play();
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
                    showToast("Success", "OTP sent to your email", "success");
                    showOtpPane();
                } else {
                    showPasswordMessage("Failed to send OTP", false);
                    showToast("Error", "Failed to send OTP", "error");
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
            showToast("Success", "OTP verified successfully!", "success");
            showNewPasswordPane();
        } else {
            showPasswordMessage("Invalid OTP. Please try again.", false);
            showToast("Error", "Invalid OTP. Please try again.", "error");
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
                    showToast("Success", "OTP resent successfully!", "success");
                } else {
                    showPasswordMessage("Failed to resend OTP", false);
                    showToast("Error", "Failed to resend OTP", "error");
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
                PreparedStatement stmt = con.prepareStatement("UPDATE user_info SET password = SHA2(?, 256) WHERE user_id = ?");
                stmt.setString(1, newPassword);
                stmt.setInt(2, current.getUserid());

                int result = stmt.executeUpdate();
                con.close();

                javafx.application.Platform.runLater(() -> {
                    changePasswordBtn.setDisable(false);
                    changePasswordStatus.setText("");

                    if (result > 0) {
                        // Update session password
                        current.setPassword(newPassword);
                        showPasswordMessage("Password changed successfully!", true);
                        showToast("Success", "Password changed successfully!", "success");

                        // Close dialog after 2 seconds
                        PauseTransition pause = new PauseTransition(Duration.seconds(2));
                        pause.setOnFinished(e -> closePasswordDialog());
                        pause.play();
                    } else {
                        showPasswordMessage("Failed to update password", false);
                        showToast("Error", "Failed to update password", "error");
                    }
                });
            } catch (Exception ex) {
                javafx.application.Platform.runLater(() -> {
                    showPasswordMessage("Error updating password", false);
                    showToast("Error", "Error updating password", "error");
                    changePasswordBtn.setDisable(false);
                    changePasswordStatus.setText("");
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

            // Validate email format
            if (!email.contains("@") || !email.contains(".")) {
                showToast("Error", "Please enter a valid email address", "error");
                return;
            }

            // Validate phone number (basic validation)
            if (phone.length() < 10) {
                showToast("Error", "Please enter a valid phone number", "error");
                return;
            }

            // Handle profile photo if changed
            String photoPath = cachedPhotoPath;
            if (selectedProfilePhoto != null) {
                photoPath = saveProfileImage(selectedProfilePhoto, current.getUsername(), current.getUserid());
            }

            try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
                 PreparedStatement ps = con.prepareStatement("UPDATE user_info SET user_email = ?, user_phone = ?, user_address = ?, user_photo = ? WHERE user_id = ?")) {
                ps.setString(1, email);
                ps.setString(2, phone);
                ps.setString(3, address);
                ps.setString(4, photoPath);
                ps.setInt(5, current.getUserid());
                ps.executeUpdate();
            }

            cachedPhotoPath = photoPath;
            selectedProfilePhoto = null;

            // Update session copy
            current.setEmail(email);
            current.setAddress(address);
            current.setPhone(phone);
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

    // Helper methods for profile management
    private void loadCurrentProfilePhoto() {
        cachedPhotoPath = fetchPhotoFromDatabase();
        applyPhotoToImages(cachedPhotoPath);
    }

    private String fetchPhotoFromDatabase() {
        String sql = "SELECT user_photo FROM user_info WHERE user_id = ?";
        try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, current.getUserid());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("user_photo");
            }
        } catch (SQLException e) {
            logger.error("Error fetching photo from database", e);
        }
        return null;
    }

    private void applyPhotoToImages(String photoPath) {
        if (photoPath != null && !photoPath.isEmpty()) {
            try {
                File photoFile = new File(photoPath);
                if (photoFile.exists()) {
                    Image image = new Image(photoFile.toURI().toString());
                    if (pfImage != null) {
                        pfImage.setImage(image);
                        applyCircularClip(pfImage, 120);
                    }
                    if (profileImage != null) {
                        profileImage.setImage(image);
                        applyCircularClip(profileImage, 48);
                    }
                }
            } catch (Exception e) {
                logger.error("Error applying photo to images", e);
            }
        }
    }

    private String saveProfileImage(File file, String username, int userId) {
        try {
            String extension = extractExtension(file.getName());
            String sanitizedName = sanitizeForFileName(username);
            String fileName = sanitizedName + "_" + userId + extension;

            Path userPhotosDir = Paths.get("src/main/resources/Image/profiles");
            Files.createDirectories(userPhotosDir);

            Path targetPath = userPhotosDir.resolve(fileName);
            Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return targetPath.toString();
        } catch (IOException e) {
            logger.error("Error saving profile image", e);
            return null;
        }
    }

    private String extractExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex);
        }
        return ".png";
    }

    private String sanitizeForFileName(String value) {
        if (value == null) return "user";
        return value.trim().toLowerCase(Locale.ENGLISH).replaceAll("[^a-z0-9]+", "_");
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
