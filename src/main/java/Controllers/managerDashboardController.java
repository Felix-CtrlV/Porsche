package Controllers;

import Database.DatabaseConnectionManager;
import MainUI.login;
import Utils.LogoutHelper;
import Utils.OTPService;
import Utils.Session;
import java.sql.CallableStatement;
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
    private ImageView img;

    @FXML
    private Button inventorybtn;

    @FXML
    private Button logoutbtn;

    @FXML
    private Label nameLbl;

    @FXML
    private HBox optionChange, optionProfile, optionAttendance;

    @FXML
    private Button orderbtn;

    @FXML
    private Pane overlayPane;

    @FXML
    private Button overviewbtn;

    @FXML
    private ImageView pfImage;

    @FXML
    private TextField profileAddress, profilePhone, profileEmail;

    @FXML
    private Label profileDOB, profileName;

    @FXML
    private VBox profilePane, settingPane, passwordVerifyPane;

    @FXML
    private BorderPane root;

    @FXML
    private Label settingIcon, backToSettingsBtn, editProfileBtn, passwordErrorLabel;

    @FXML
    private Button staffbtn;

    @FXML
    private PasswordField verifyPasswordField;

    @FXML
    private Button verifyPasswordBtn, cancelVerifyBtn, saveProfileBtn, editProfilePhotoBtn;

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
    private StackPane passwordMessagePane, passwordLoadingPane;

    @FXML
    private Label passwordMessageLabel, closePasswordDialogBtn;
    
    @FXML
    private StackPane lockoutOverlay;
    
    @FXML
    private Label lockoutStatusLabel;

    private final OTPService otpService = OTPService.getInstance();

    private SequentialTransition currentPasswordAnimation;

    private Button activeButton;

    private int adminid;

    private File selectedProfilePhoto;
    private String cachedPhotoPath;
    
    // Security lockout fields
    private int failedVerificationAttempts = 0;
    private static final int MAX_VERIFICATION_ATTEMPTS = 5;
    private boolean isLocked = false;
    private String lockToken = null;

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

    private void clickSetting() {
        openSetting();
        overlayPane.setOnMouseClicked(e -> {
            closeAllPanesAndResetToSettings();
        });

    }


    Session current = Session.getInstance();

    public void initialize() throws IOException {

        String name = current.getUsername();
        nameLbl.setText(name);
        profileName.setText(name);
        profileAddress.setText(current.getAddress());
        profileEmail.setText(current.getEmail());
        profilePhone.setText(current.getPhone());
        profileDOB.setText(current.getDob().toString());

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

        // Attendance click in settings (placeholder for now)
        if (optionAttendance != null) {
            optionAttendance.setOnMouseClicked(e -> {
                // TODO: Implement attendance view later
                showToast("Info", "Attendance feature coming soon!", "info");
            });
        }

        // Back to settings from profile
        if (backToSettingsBtn != null) {
            backToSettingsBtn.setOnMouseClicked(e -> {
                hideProfilePane();
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
                            } catch (
                                    Exception ex) {
                                logger.error("Error during window close logout", ex);
                            }
                        });
                    }
                });
            }
        });

        applyCircularClip(pfImage, 120);

        if (editProfilePhotoBtn != null) {
            editProfilePhotoBtn.setVisible(false);
        }

        loadCurrentProfilePhoto();
        
        // Start unlock server for email unlock functionality
        try {
            Utils.UnlockServer.getInstance().start();
        } catch (Exception e) {
            logger.warn("Failed to start unlock server: " + e.getMessage());
        }
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

    // ========== HELPER METHODS ==========

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

    // ========== PROFILE VIEW METHODS ==========

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
                settingPane.setVisible(false);
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

        // Verify password against session
        String sessionPassword = current.getPassword();

        if (sessionPassword != null && sessionPassword.equals(enteredPassword)) {
            // Password correct - reset attempts and show profile pane
            failedVerificationAttempts = 0;
            showProfilePane();
        } else {
            // Incorrect password - increment attempts and clear field
            failedVerificationAttempts++;
            verifyPasswordField.clear();
            
            int remainingAttempts = MAX_VERIFICATION_ATTEMPTS - failedVerificationAttempts;
            
            if (failedVerificationAttempts >= MAX_VERIFICATION_ATTEMPTS) {
                // Lock the dashboard
                lockDashboard();
            } else {
                passwordErrorLabel.setText("Incorrect password. " + remainingAttempts + " attempts remaining.");
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
        if (settingPane != null) {
            settingPane.setVisible(false);
        }

        if (current != null) {
            profileName.setText(current.getUsername() != null ? current.getUsername() : "");
            profileEmail.setText(current.getEmail() != null ? current.getEmail() : "");
            profilePhone.setText(current.getPhone() != null ? current.getPhone() : "");
            profileAddress.setText(current.getAddress() != null ? current.getAddress() : "");

            if (current.getDob() != null) {
                java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy");
                profileDOB.setText(current.getDob().format(formatter));
            } else {
                profileDOB.setText("");
            }
        }

        profileEmail.setEditable(false);
        profilePhone.setEditable(false);
        profileAddress.setEditable(false);
        profileEmail.setFocusTraversable(false);
        profilePhone.setFocusTraversable(false);
        profileAddress.setFocusTraversable(false);

        TranslateTransition slideOutVerify = new TranslateTransition(Duration.millis(300), passwordVerifyPane);
        slideOutVerify.setFromX(0);
        slideOutVerify.setToX(420);
        slideOutVerify.setOnFinished(e -> {
            passwordVerifyPane.setVisible(false);
            profilePane.setVisible(true);
            profilePane.setTranslateX(420);

            TranslateTransition slideInProfile = new TranslateTransition(Duration.millis(300), profilePane);
            slideInProfile.setFromX(420);
            slideInProfile.setToX(0);
            slideInProfile.setOnFinished(ev -> {
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

        saveProfileBtn.setVisible(true);
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

            current.setEmail(profileEmail.getText());
            current.setAddress(profileAddress.getText());
            current.setPhone(profilePhone.getText());
            Session.setInstance(current);

            applyPhotoToImages(cachedPhotoPath);
            toggleEditMode();

            if (editProfilePhotoBtn != null) {
                editProfilePhotoBtn.setVisible(false);
            }
            saveProfileBtn.setVisible(false);

            showToast("Success", "Profile updated successfully!", "success");

        } catch (
                SQLException ex) {
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
        } catch (
                SQLException ignored) {
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
                } catch (
                        Exception ignored) {
                    image = null;
                }
            }
        }

        if (image == null) {
            try {
                image = new Image(Objects.requireNonNull(getClass().getResource("/Image/defaultUserProfile.jpg")).toExternalForm(), true);
            } catch (
                    Exception ignored) {
                image = null;
            }
        }

        if (image != null) {
            if (pfImage != null) {
                pfImage.setImage(image);
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
        } catch (
                Exception e) {
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

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), toastNotification);
        slideIn.setFromY(-100);
        slideIn.setToY(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toastNotification);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ParallelTransition showToast = new ParallelTransition(slideIn, fadeIn);

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

    public void showConfirmDialog(String title, String message, String icon, Runnable onConfirm) {
        confirmTitle.setText(title);
        confirmMessage.setText(message);
        confirmIcon.setText(icon);
        confirmCallback = onConfirm;

        confirmContent.getChildren().clear();
        confirmContent.getChildren().add(confirmMessage);

        confirmOverlay.setVisible(true);
        confirmOverlay.setOpacity(0);

        FadeTransition overlayFade = new FadeTransition(Duration.millis(300), confirmOverlay);
        overlayFade.setFromValue(0);
        overlayFade.setToValue(1);
        overlayFade.play();

        confirmDialog.setVisible(true);
        confirmDialog.setTranslateY(-600);

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
        FadeTransition overlayFadeOut = new FadeTransition(Duration.millis(300), confirmOverlay);
        overlayFadeOut.setFromValue(1);
        overlayFadeOut.setToValue(0);
        overlayFadeOut.setOnFinished(e -> confirmOverlay.setVisible(false));
        overlayFadeOut.play();

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

    @FXML
    public void showChangePasswordDialog() {
        passwordVerificationPane.setVisible(true);
        otpVerificationPane.setVisible(false);
        newPasswordPane.setVisible(false);

        currentPasswordField.clear();
        otpField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();

        passwordVerifyStatus.setText("");
        otpVerifyStatus.setText("");
        changePasswordStatus.setText("");

        confirmOverlay.setVisible(true);
        confirmOverlay.setOpacity(0);

        FadeTransition overlayFade = new FadeTransition(Duration.millis(300), confirmOverlay);
        overlayFade.setFromValue(0);
        overlayFade.setToValue(1);
        overlayFade.play();

        changePasswordDialog.setVisible(true);
        changePasswordDialog.setTranslateY(-700);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(400), changePasswordDialog);
        slideIn.setFromY(-700);
        slideIn.setToY(0);
        slideIn.setInterpolator(javafx.animation.Interpolator.EASE_OUT);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(400), changePasswordDialog);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ParallelTransition showDialog = new ParallelTransition(slideIn, fadeIn);
        showDialog.play();

        currentPasswordField.setOnAction(e -> onVerifyCurrentPassword());
        otpField.setOnAction(e -> onVerifyOTP());
        confirmPasswordField.setOnAction(e -> onSubmitPasswordChange());
    }

    @FXML
    public void closePasswordDialog() {
        FadeTransition overlayFadeOut = new FadeTransition(Duration.millis(300), confirmOverlay);
        overlayFadeOut.setFromValue(1);
        overlayFadeOut.setToValue(0);
        overlayFadeOut.setOnFinished(e -> confirmOverlay.setVisible(false));
        overlayFadeOut.play();

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

        } catch (
                SQLException e) {
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
    
    // ========== SECURITY LOCKOUT METHODS ==========
    
    private void lockDashboard() {
        isLocked = true;
        lockToken = java.util.UUID.randomUUID().toString();
        
        // Insert lock record into database
        try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement("INSERT INTO security_locks (user_id, lock_token) VALUES (?, ?)")) {
            ps.setInt(1, current.getUserid());
            ps.setString(2, lockToken);
            ps.executeUpdate();
        } catch (SQLException e) {
            // Table might not exist, create it
            createSecurityLocksTable();
        }
        
        // Close all open panes
        passwordVerifyPane.setVisible(false);
        profilePane.setVisible(false);
        settingPane.setVisible(false);
        overlayPane.setVisible(false);
        
        // Apply blur effect to root
        javafx.scene.effect.GaussianBlur blur = new javafx.scene.effect.GaussianBlur(15);
        root.setEffect(blur);
        root.setDisable(true);
        
        // Show lockout overlay
        lockoutOverlay.setVisible(true);
        lockoutOverlay.setOpacity(0);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), lockoutOverlay);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
        
        // Send security alert email
        sendSecurityAlertEmail();
    }
    
    private void createSecurityLocksTable() {
        String createTableSql = "CREATE TABLE IF NOT EXISTS security_locks (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id INT NOT NULL, " +
                "lock_token VARCHAR(255) NOT NULL UNIQUE, " +
                "unlock_status BOOLEAN DEFAULT FALSE, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "unlocked_at TIMESTAMP NULL, " +
                "ip_address VARCHAR(45) NULL, " +
                "user_agent TEXT NULL, " +
                "FOREIGN KEY (user_id) REFERENCES user_info(user_id) ON DELETE CASCADE, " +
                "INDEX idx_user_token (user_id, lock_token), " +
                "INDEX idx_created_at (created_at)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        
        try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(createTableSql)) {
            ps.executeUpdate();
            
            // Try inserting again
            try (PreparedStatement insertPs = con.prepareStatement("INSERT INTO security_locks (user_id, lock_token) VALUES (?, ?)")) {
                insertPs.setInt(1, current.getUserid());
                insertPs.setString(2, lockToken);
                insertPs.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void sendSecurityAlertEmail() {
        new Thread(() -> {
            try {
                String userEmail = current.getEmail();
                String userName = current.getUsername();
                String unlockUrl = "http://localhost:8080/unlock?token=" + lockToken + "&userId=" + current.getUserid();
                
                String subject = "🔒 Security Alert: Dashboard Locked";
                String htmlContent = String.format(
                    "<!DOCTYPE html>" +
                    "<html>" +
                    "<head>" +
                    "    <style>" +
                    "        body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #f3f4f6; margin: 0; padding: 20px; }" +
                    "        .container { max-width: 600px; margin: 0 auto; background-color: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }" +
                    "        .header { background: linear-gradient(135deg, #ef4444 0%%, #dc2626 100%%); padding: 40px 30px; text-align: center; }" +
                    "        .header h1 { color: white; margin: 0; font-size: 28px; }" +
                    "        .lock-icon { font-size: 60px; margin-bottom: 10px; }" +
                    "        .content { padding: 40px 30px; }" +
                    "        .alert-box { background-color: #fef2f2; border-left: 4px solid #ef4444; padding: 15px; margin: 20px 0; border-radius: 4px; }" +
                    "        .alert-box p { margin: 5px 0; color: #991b1b; }" +
                    "        .info { color: #6b7280; line-height: 1.6; margin: 15px 0; }" +
                    "        .unlock-button { display: inline-block; background: linear-gradient(135deg, #10b981 0%%, #059669 100%%); color: white; padding: 15px 40px; text-decoration: none; border-radius: 8px; font-weight: 600; margin: 20px 0; box-shadow: 0 4px 6px rgba(16, 185, 129, 0.3); }" +
                    "        .unlock-button:hover { background: linear-gradient(135deg, #059669 0%%, #047857 100%%); }" +
                    "        .footer { background-color: #f9fafb; padding: 20px 30px; text-align: center; color: #6b7280; font-size: 12px; }" +
                    "        .warning { color: #dc2626; font-weight: 600; }" +
                    "    </style>" +
                    "</head>" +
                    "<body>" +
                    "    <div class='container'>" +
                    "        <div class='header'>" +
                    "            <div class='lock-icon'>🔒</div>" +
                    "            <h1>Security Alert</h1>" +
                    "        </div>" +
                    "        <div class='content'>" +
                    "            <h2 style='color: #1f2937; margin-top: 0;'>Hello %s,</h2>" +
                    "            <div class='alert-box'>" +
                    "                <p><strong>⚠ SECURITY WARNING</strong></p>" +
                    "                <p>Multiple failed verification attempts detected on your dashboard!</p>" +
                    "            </div>" +
                    "            <p class='info'>" +
                    "                Someone attempted to access your profile information <strong>5 times</strong> with incorrect passwords. " +
                    "                As a security measure, your dashboard has been automatically locked." +
                    "            </p>" +
                    "            <p class='info'>" +
                    "                <strong>Time:</strong> %s<br>" +
                    "                <strong>Action:</strong> Profile verification failed (5 attempts)<br>" +
                    "                <strong>Status:</strong> <span class='warning'>Dashboard Locked</span>" +
                    "            </p>" +
                    "            <p class='info'>" +
                    "                If this was you, click the button below to unlock your dashboard. " +
                    "                If you did not attempt to access your profile, please change your password immediately." +
                    "            </p>" +
                    "            <center>" +
                    "                <a href='%s' class='unlock-button'>🔓 Unlock Dashboard</a>" +
                    "            </center>" +
                    "            <p class='info' style='font-size: 12px; color: #9ca3af; margin-top: 30px;'>" +
                    "                <strong>Note:</strong> This link will expire in 1 hour for security reasons." +
                    "            </p>" +
                    "        </div>" +
                    "        <div class='footer'>" +
                    "            <p>This is an automated security notification from Porsche Management System.</p>" +
                    "            <p>If you did not request this, please contact your system administrator immediately.</p>" +
                    "        </div>" +
                    "    </div>" +
                    "</body>" +
                    "</html>",
                    userName,
                    java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a")),
                    unlockUrl
                );
                
                boolean sent = otpService.sendCustomEmail(userEmail, subject, htmlContent);
                
                javafx.application.Platform.runLater(() -> {
                    if (sent) {
                        lockoutStatusLabel.setText("Security alert sent to " + maskEmail(userEmail));
                    } else {
                        lockoutStatusLabel.setText("Failed to send email. Please contact administrator.");
                    }
                });
                
                // Start polling for unlock status
                startUnlockPolling();
                
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    lockoutStatusLabel.setText("Error sending email. Please contact administrator.");
                });
            }
        }).start();
    }
    
    private void startUnlockPolling() {
        // Poll database every 3 seconds to check if unlock button was clicked
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(Duration.seconds(3), e -> checkUnlockStatus())
        );
        timeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        timeline.play();
    }
    
    private void checkUnlockStatus() {
        if (!isLocked) return;
        
        new Thread(() -> {
            try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
                 PreparedStatement ps = con.prepareStatement(
                     "SELECT unlock_status FROM security_locks WHERE user_id = ? AND lock_token = ? AND created_at > DATE_SUB(NOW(), INTERVAL 1 HOUR)")) {
                
                ps.setInt(1, current.getUserid());
                ps.setString(2, lockToken);
                
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getBoolean("unlock_status")) {
                        javafx.application.Platform.runLater(this::unlockDashboard);
                    }
                }
            } catch (SQLException ex) {
                // Table might not exist yet, ignore
            }
        }).start();
    }
    
    private void unlockDashboard() {
        isLocked = false;
        failedVerificationAttempts = 0;
        
        // Fade out lockout overlay
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), lockoutOverlay);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            lockoutOverlay.setVisible(false);
            root.setEffect(null);
            root.setDisable(false);
        });
        fadeOut.play();
        
        // Show success toast
        showToast("Success", "Dashboard unlocked successfully!", "success");
    }
    
    @FXML
    private void onLockoutLogout() {
        try {
            Stage stage = (Stage) lockoutOverlay.getScene().getWindow();
            stage.close();
            
            // Perform logout
            try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
                 CallableStatement check_out = con.prepareCall("call logout(?, ?)")) {
                check_out.setInt(1, current.getUserid());
                check_out.setString(2, String.valueOf(java.time.LocalDateTime.now()));
                check_out.execute();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            
            login log_in = new login();
            log_in.startDirectLogin(new Stage());
            Session.clearSession();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
