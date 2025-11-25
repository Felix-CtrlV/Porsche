package Controllers;

import DAO.InstallmentDAO;
import Model.Installment;
import Utils.SessionStaff;
import Utils.OTPService;
import Utils.SecurityManager;
import Utils.SecurityEmailService;
import Database.DatabaseConnectionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import javafx.util.Pair;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import javafx.animation.*;
import javafx.scene.effect.GaussianBlur;
import javafx.geometry.Pos;
import java.util.Objects;

public class staffWelcomeController {

    private static final Logger logger = LoggerFactory.getLogger(staffWelcomeController.class);

    @FXML private AnchorPane rootPane;
    @FXML private Button settingsButton;
    @FXML private Label topicLabel;
    @FXML private Label headingLabel;
    @FXML private Button discoverbtn;
    @FXML private Button accessorybtn;
    @FXML private ImageView backgroundImage;
    @FXML private VBox settingsPopup;
    @FXML private VBox vbox;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private ImageView profilePictureView;
    @FXML private Button paymentDetailsButton;
    @FXML private VBox paymentPopup;
    @FXML private TableView<Installment> paymentTable;
    @FXML private TableColumn<Installment, Integer> colInstallmentId;
    @FXML private TableColumn<Installment, Integer> colOrderId;
    @FXML private TableColumn<Installment, Integer> colInstallmentNumber;
    @FXML private TableColumn<Installment, String> colDueDate;
    @FXML private TableColumn<Installment, Double> colAmount;
    @FXML private TableColumn<Installment, String> colStatus;
    @FXML private TableColumn<Installment, Void> colAction;
    @FXML private TextField paymentSearchField;
    @FXML private ComboBox<String> paymentStatusFilter;
    @FXML private Label paymentPendingCount;
    @FXML private Label paymentOverdueCount;
    @FXML private Label paymentPaidCount;
    @FXML private VBox passwordVerifyPane;
    @FXML private PasswordField verifyPasswordField;
    @FXML private Label passwordErrorLabel;
    @FXML private Button verifyPasswordBtn;
    @FXML private Button cancelVerifyBtn;
    @FXML private StackPane securityLockOverlay;
    @FXML private Label lockIcon;
    @FXML private Label lockMessage;
    @FXML private VBox mainContent;
    @FXML private Button logoutButton;
    @FXML private StackPane securityAlertOverlay;
    @FXML private Label securityAlertMessage;

    private boolean isProfileMenuOpen = false;
    private boolean isPaymentMenuOpen = false;
    private ScaleTransition settingsButtonScale;
    private InstallmentDAO installmentDAO;
    private DateTimeFormatter dateFormatter;
    private final SecurityManager securityManager = SecurityManager.getInstance();
    private final SecurityEmailService emailService = SecurityEmailService.getInstance();
    private int failedAttempts = 0;
    private static final int MAX_ATTEMPTS = 5;

    @FXML
    public void initialize() {
        initializeSettingsButtonAnimation();
        loadUserInfo();
        setupAutoRefresh();
        setupClickOutsideListener();
        initializePaymentComponents();
        initializePasswordVerification();
        checkAccountLockStatus();
        setupLogoutButton();

        // Ensure security alert overlay starts hidden
        if (securityAlertOverlay != null) {
            securityAlertOverlay.setVisible(false);
            securityAlertOverlay.setManaged(false);
        }
    }

    private void loadUserInfo() {
        try {
            if (!SessionStaff.isSessionActive()) {
                showErrorAlert("Session Error", "No active session found. Please log in again.");
                redirectToLogin();
                return;
            }

            String username = SessionStaff.getLoggedInStaffName();
            String role = SessionStaff.getLoggedInStaffRole();

            if (userNameLabel != null) {
                userNameLabel.setText(username != null ? username : "User");
            }

            if (userRoleLabel != null) {
                String formattedRole = role != null ?
                        role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase() : "Staff";
                userRoleLabel.setText(formattedRole);
            }

            loadDetailedUserInfo();

        } catch (Exception e) {
            logger.error("Error loading user info", e);
            showErrorAlert("Error", "Unable to load user information.");
        }
    }

    private void loadDetailedUserInfo() {
        try {
            int userId = SessionStaff.getLoggedInStaffId();
            String sql = "SELECT user_name, user_role, user_photo FROM user_info WHERE user_id = ?";

            try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String userName = rs.getString("user_name");
                    String userRole = rs.getString("user_role");
                    String userPhoto = rs.getString("user_photo");

                    Platform.runLater(() -> {
                        if (userNameLabel != null && userName != null) {
                            userNameLabel.setText(userName);
                        }
                        if (userRoleLabel != null && userRole != null) {
                            String formattedRole = userRole.substring(0, 1).toUpperCase() + userRole.substring(1).toLowerCase();
                            userRoleLabel.setText(formattedRole);
                        }
                        loadProfilePicture(userPhoto);
                    });
                }
            }
        } catch (SQLException e) {
            logger.error("Error loading detailed user info", e);
        }
    }

    private void setupLogoutButton() {
        if (logoutButton != null) {
            logoutButton.setOnAction(e -> handleLogout());

            logoutButton.setStyle("-fx-background-color: transparent; " +
                    "-fx-text-fill: #d5001f; " +
                    "-fx-font-weight: bold; " +
                    "-fx-font-size: 14px; " +
                    "-fx-border-color: #d5001f; " +
                    "-fx-border-width: 2; " +
                    "-fx-border-radius: 5; " +
                    "-fx-background-radius: 5; " +
                    "-fx-cursor: hand; " +
                    "-fx-padding: 8 16 8 16;");

            logoutButton.setOnMouseEntered(e -> {
                logoutButton.setStyle("-fx-background-color: #d5001f; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 14px; " +
                        "-fx-border-color: #d5001f; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 5; " +
                        "-fx-background-radius: 5; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 8 16 8 16; " +
                        "-fx-effect: dropshadow(gaussian, rgba(213, 0, 31, 0.5), 10, 0.5, 0, 0);");
            });

            logoutButton.setOnMouseExited(e -> {
                logoutButton.setStyle("-fx-background-color: transparent; " +
                        "-fx-text-fill: #d5001f; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 14px; " +
                        "-fx-border-color: #d5001f; " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 5; " +
                        "-fx-background-radius: 5; " +
                        "-fx-cursor: hand; " +
                        "-fx-padding: 8 16 8 16; " +
                        "-fx-effect: null;");
            });
        }
    }

    @FXML
    private void handleLogout() {
        try {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Logout");
            confirmAlert.setHeaderText("CONFIRM LOGOUT");
            confirmAlert.setContentText("Are you sure you want to logout from the system?\n\nYou will need to login again to access all features.");

            DialogPane dialogPane = confirmAlert.getDialogPane();
            dialogPane.setStyle("-fx-background-color: #000000; " +
                    "-fx-border-color: #d5001f; " +
                    "-fx-border-width: 2; " +
                    "-fx-background-radius: 10; " +
                    "-fx-border-radius: 10;");

            dialogPane.lookup(".content.label").setStyle("-fx-text-fill: #ffffff; " +
                    "-fx-font-size: 14px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-alignment: center;");

            dialogPane.lookup(".header-panel").setStyle("-fx-background-color: #000000;");
            dialogPane.lookup(".header-panel .label").setStyle("-fx-text-fill: #d5001f; " +
                    "-fx-font-size: 18px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-alignment: center;");

            ButtonType yesButton = new ButtonType("CONFIRM LOGOUT", ButtonBar.ButtonData.YES);
            ButtonType noButton = new ButtonType("CANCEL", ButtonBar.ButtonData.NO);
            confirmAlert.getButtonTypes().setAll(yesButton, noButton);

            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == yesButton) {
                    performPorscheLogoutAnimation();
                }
            });

            Platform.runLater(() -> {
                Node yesButtonNode = dialogPane.lookupButton(yesButton);
                if (yesButtonNode instanceof Button) {
                    Button yesBtn = (Button) yesButtonNode;
                    yesBtn.setStyle("-fx-background-color: #d5001f; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-font-size: 14px; " +
                            "-fx-background-radius: 5; " +
                            "-fx-border-radius: 5; " +
                            "-fx-padding: 10 20 10 20; " +
                            "-fx-cursor: hand;");

                    yesBtn.setOnMouseEntered(e -> {
                        yesBtn.setStyle("-fx-background-color: #ff4444; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-weight: bold; " +
                                "-fx-font-size: 14px; " +
                                "-fx-background-radius: 5; " +
                                "-fx-border-radius: 5; " +
                                "-fx-padding: 10 20 10 20; " +
                                "-fx-cursor: hand; " +
                                "-fx-scale-x: 1.05; " +
                                "-fx-scale-y: 1.05;");
                    });

                    yesBtn.setOnMouseExited(e -> {
                        yesBtn.setStyle("-fx-background-color: #d5001f; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-weight: bold; " +
                                "-fx-font-size: 14px; " +
                                "-fx-background-radius: 5; " +
                                "-fx-border-radius: 5; " +
                                "-fx-padding: 10 20 10 20; " +
                                "-fx-cursor: hand; " +
                                "-fx-scale-x: 1.0; " +
                                "-fx-scale-y: 1.0;");
                    });
                }

                Node noButtonNode = dialogPane.lookupButton(noButton);
                if (noButtonNode instanceof Button) {
                    Button noBtn = (Button) noButtonNode;
                    noBtn.setStyle("-fx-background-color: #000000; " +
                            "-fx-text-fill: #ffffff; " +
                            "-fx-font-weight: bold; " +
                            "-fx-font-size: 14px; " +
                            "-fx-background-radius: 5; " +
                            "-fx-border-radius: 5; " +
                            "-fx-padding: 10 20 10 20; " +
                            "-fx-cursor: hand; " +
                            "-fx-border-color: #ffffff; " +
                            "-fx-border-width: 1;");

                    noBtn.setOnMouseEntered(e -> {
                        noBtn.setStyle("-fx-background-color: #333333; " +
                                "-fx-text-fill: #ffffff; " +
                                "-fx-font-weight: bold; " +
                                "-fx-font-size: 14px; " +
                                "-fx-background-radius: 5; " +
                                "-fx-border-radius: 5; " +
                                "-fx-padding: 10 20 10 20; " +
                                "-fx-cursor: hand; " +
                                "-fx-border-color: #ffffff; " +
                                "-fx-border-width: 1; " +
                                "-fx-scale-x: 1.05; " +
                                "-fx-scale-y: 1.05;");
                    });

                    noBtn.setOnMouseExited(e -> {
                        noBtn.setStyle("-fx-background-color: #000000; " +
                                "-fx-text-fill: #ffffff; " +
                                "-fx-font-weight: bold; " +
                                "-fx-font-size: 14px; " +
                                "-fx-background-radius: 5; " +
                                "-fx-border-radius: 5; " +
                                "-fx-padding: 10 20 10 20; " +
                                "-fx-cursor: hand; " +
                                "-fx-border-color: #ffffff; " +
                                "-fx-border-width: 1; " +
                                "-fx-scale-x: 1.0; " +
                                "-fx-scale-y: 1.0;");
                    });
                }
            });

        } catch (Exception e) {
            logger.error("Error during logout confirmation", e);
            performLogout();
        }
    }

    private void performPorscheLogoutAnimation() {
        try {
            StackPane animationOverlay = new StackPane();
            animationOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.9);");
            animationOverlay.setPrefSize(rootPane.getWidth(), rootPane.getHeight());

            ImageView porscheLogo = new ImageView();
            try {
                Image logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Image/porsche_logo.png")));
                porscheLogo.setImage(logo);
                porscheLogo.setFitWidth(50);
                porscheLogo.setFitHeight(60);
                porscheLogo.setOpacity(0);
            } catch (Exception e) {
                porscheLogo.setVisible(false);
            }

            Label logoutLabel = new Label("Logging out | Thank you for your work");
            logoutLabel.setStyle("-fx-text-fill: white; " +
                    "-fx-font-size: 24px; " +
                    "-fx-font-weight: bold; ");
            logoutLabel.setOpacity(0);

            VBox animationContent = new VBox(20, porscheLogo, logoutLabel);
            animationContent.setAlignment(Pos.CENTER);
            animationContent.setStyle("-fx-background-color: transparent;");

            animationOverlay.getChildren().add(animationContent);
            rootPane.getChildren().add(animationOverlay);

            SequentialTransition logoutAnimation = new SequentialTransition();

            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), animationOverlay);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            FadeTransition logoFadeIn = new FadeTransition(Duration.millis(800), porscheLogo);
            logoFadeIn.setFromValue(0);
            logoFadeIn.setToValue(1);

            ScaleTransition logoScale = new ScaleTransition(Duration.millis(1000), porscheLogo);
            logoScale.setFromX(0.5);
            logoScale.setFromY(0.5);
            logoScale.setToX(1.0);
            logoScale.setToY(1.0);

            ParallelTransition logoAnimation = new ParallelTransition(logoFadeIn, logoScale);

            FadeTransition textFadeIn = new FadeTransition(Duration.millis(600), logoutLabel);
            textFadeIn.setFromValue(0);
            textFadeIn.setToValue(1);

            PauseTransition pause = new PauseTransition(Duration.millis(1000));

            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), animationOverlay);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);

            logoutAnimation.getChildren().addAll(
                    fadeIn,
                    logoAnimation,
                    textFadeIn,
                    pause,
                    fadeOut
            );

            logoutAnimation.setOnFinished(e -> {
                rootPane.getChildren().remove(animationOverlay);
                performLogout();
            });

            logoutAnimation.play();

        } catch (Exception e) {
            logger.error("Error during logout animation", e);
            performLogout();
        }
    }

    private void performLogout() {
        try {
            closeProfileMenu();
            closePaymentMenu();
            hidePasswordVerification();

            logLogoutActivity();

            SessionStaff.handleLogout();

            navigateToLoginPage();

        } catch (Exception e) {
            logger.error("Error during logout process", e);
            forceNavigateToLogin();
        }
    }

    private void logLogoutActivity() {
        try {
            String checkTableSql = "SELECT COUNT(*) as count FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'activity_log'";
            String insertSql = "INSERT INTO activity_log (user_email, activity_type, activity_description, activity_timestamp) VALUES (?, ?, ?, NOW())";

            try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
                 PreparedStatement checkPs = conn.prepareStatement(checkTableSql);
                 ResultSet rs = checkPs.executeQuery()) {

                if (rs.next() && rs.getInt("count") > 0) {
                    try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                        String staffEmail = SessionStaff.getLoggedInStaffName() != null ?
                                SessionStaff.getLoggedInStaffName().replace(" ", ".").toLowerCase() + "@showroom.com" : "staff@showroom.com";

                        insertPs.setString(1, staffEmail);
                        insertPs.setString(2, "LOGOUT");
                        insertPs.setString(3, "User logged out from the system");
                        insertPs.executeUpdate();
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Activity logging failed during logout", e);
        }
    }

    private void navigateToLoginPage() throws IOException {
        String[] possiblePaths = {
                "/View/login.fxml",
                "/login.fxml"
        };

        for (String path : possiblePaths) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
                Parent root = loader.load();
                Stage stage = (Stage) rootPane.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Login - Car Showroom Management System");
                stage.centerOnScreen();
                stage.show();
                return;
            } catch (Exception e) {
                logger.debug("Failed to load login page from: {}", path);
            }
        }
        throw new IOException("Could not find login.fxml in any location");
    }

    private void forceNavigateToLogin() {
        try {
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.close();

            Stage loginStage = new Stage();
            Parent root = FXMLLoader.load(getClass().getResource("/View/login.fxml"));
            Scene scene = new Scene(root);
            loginStage.setScene(scene);
            loginStage.setTitle("Login - Car Showroom Management System");
            loginStage.show();

        } catch (Exception e) {
            logger.error("Critical error during forced logout", e);
            Platform.exit();
        }
    }

    private void redirectToLogin() {
        Platform.runLater(() -> {
            try {
                performLogout();
            } catch (Exception e) {
                logger.error("Error redirecting to login", e);
                forceNavigateToLogin();
            }
        });
    }

    private void initializePasswordVerification() {
        if (passwordVerifyPane != null) {
            passwordVerifyPane.setVisible(false);
            passwordVerifyPane.setManaged(false);
        }

        if (verifyPasswordField != null) {
            verifyPasswordField.setOnKeyPressed(event -> {
                if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                    onVerifyPassword();
                }
            });
        }

        if (verifyPasswordBtn != null) {
            verifyPasswordBtn.setOnAction(e -> onVerifyPassword());
        }

        if (cancelVerifyBtn != null) {
            cancelVerifyBtn.setOnAction(e -> hidePasswordVerification());
        }
    }

    private void checkAccountLockStatus() {
        if (failedAttempts >= MAX_ATTEMPTS) {
            showTooManyAttemptsPopup();
        }
    }

    private void showTooManyAttemptsPopup() {
        Platform.runLater(() -> {
            // Show the security alert overlay
            securityAlertOverlay.setVisible(true);
            securityAlertOverlay.setManaged(true);

            // Add fade-in animation
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), securityAlertOverlay);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

            // Disable interaction with other elements
            if (mainContent != null) {
                mainContent.setDisable(true);
            }
        });
    }

    @FXML
    private void handleSecurityAlertOk() {
        // Hide the security alert
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), securityAlertOverlay);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            securityAlertOverlay.setVisible(false);
            securityAlertOverlay.setManaged(false);

            // Re-enable interaction with other elements
            if (mainContent != null) {
                mainContent.setDisable(false);
            }

            // Proceed with logout
            forceLogoutDueToSecurity();
        });
        fadeOut.play();
    }

    private void forceLogoutDueToSecurity() {
        try {
            // Ensure popup is hidden
            if (securityAlertOverlay != null) {
                securityAlertOverlay.setVisible(false);
                securityAlertOverlay.setManaged(false);
            }

            closeProfileMenu();
            closePaymentMenu();
            hidePasswordVerification();

            logger.warn("User forcefully logged out due to too many failed password attempts");

            SessionStaff.handleLogout();

            navigateToLoginPage();

        } catch (Exception e) {
            logger.error("Error during security logout", e);
            forceNavigateToLogin();
        }
    }

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

    private void updateLockMessage() {
        if (lockMessage != null) {
            String username = SessionStaff.getLoggedInStaffName();
            String message = String.format(
                    "Hello %s, your account has been temporarily locked due to multiple failed password attempts. " +
                            "Please wait 30 minutes or contact administrator to unlock your account.",
                    username != null ? username : "Staff"
            );
            lockMessage.setText(message);
        }
    }

    @FXML
    private void toggleProfileMenu() {
        if (isProfileMenuOpen) {
            closeProfileMenu();
        } else {
            showPasswordVerification();
        }
    }

    private void showPasswordVerification() {
        if (!SessionStaff.isSessionActive()) {
            showErrorAlert("Session Expired", "Your session has expired. Please log in again.");
            redirectToLogin();
            return;
        }

        passwordVerifyPane.setVisible(true);
        passwordVerifyPane.setManaged(true);

        verifyPasswordField.clear();
        passwordErrorLabel.setVisible(false);

        GaussianBlur blur = new GaussianBlur(10);
        if (mainContent != null) {
            mainContent.setEffect(blur);
        }

        passwordVerifyPane.setScaleX(0.8);
        passwordVerifyPane.setScaleY(0.8);
        passwordVerifyPane.setOpacity(0);

        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), passwordVerifyPane);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);

        FadeTransition fadeTransition = new FadeTransition(Duration.millis(200), passwordVerifyPane);
        fadeTransition.setToValue(1.0);

        ParallelTransition parallelTransition = new ParallelTransition(scaleTransition, fadeTransition);
        parallelTransition.play();

        Platform.runLater(() -> verifyPasswordField.requestFocus());
    }

    private void hidePasswordVerification() {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(150), passwordVerifyPane);
        fadeTransition.setToValue(0);
        fadeTransition.setOnFinished(e -> {
            passwordVerifyPane.setVisible(false);
            passwordVerifyPane.setManaged(false);
            if (mainContent != null) {
                mainContent.setEffect(null);
            }
            verifyPasswordField.clear();
            passwordErrorLabel.setVisible(false);
        });
        fadeTransition.play();
    }

    @FXML
    private void onVerifyPassword() {
        String enteredPassword = verifyPasswordField.getText();

        if (enteredPassword.isEmpty()) {
            passwordErrorLabel.setText("Please enter your password");
            passwordErrorLabel.setVisible(true);
            return;
        }

        if (!SessionStaff.isSessionActive()) {
            passwordErrorLabel.setText("User session error. Please logout and login again.");
            passwordErrorLabel.setVisible(true);
            return;
        }

        if (failedAttempts >= MAX_ATTEMPTS) {
            showTooManyAttemptsPopup();
            return;
        }

        boolean isPasswordCorrect = verifyPasswordWithStoredProcedure(enteredPassword);

        if (isPasswordCorrect) {
            failedAttempts = 0;
            hidePasswordVerification();
            openProfileMenu();
        } else {
            verifyPasswordField.clear();
            failedAttempts++;

            if (failedAttempts >= MAX_ATTEMPTS) {
                showTooManyAttemptsPopup();
            } else {
                int remainingAttempts = MAX_ATTEMPTS - failedAttempts;
                passwordErrorLabel.setText("Incorrect password. " + remainingAttempts + " attempts remaining.");
                passwordErrorLabel.setVisible(true);
            }
        }
    }

    private boolean verifyPasswordWithStoredProcedure(String enteredPassword) {
        try {
            if (!SessionStaff.isSessionActive()) {
                return false;
            }

            String username = SessionStaff.getLoggedInStaffName();
            if (username == null) {
                return false;
            }

            try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
                 CallableStatement p1 = con.prepareCall("call login(?,?,?)")) {

                p1.setString(1, username);
                p1.setString(2, enteredPassword);
                p1.setString(3, String.valueOf(java.time.LocalDateTime.now()));

                try (ResultSet rs = p1.executeQuery()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        return id != 0;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error verifying password with stored procedure", e);
        }
        return false;
    }

    private void openProfileMenu() {
        settingsPopup.setVisible(true);
        settingsPopup.setManaged(true);
        isProfileMenuOpen = true;

        settingsPopup.setScaleX(0.8);
        settingsPopup.setScaleY(0.8);
        settingsPopup.setOpacity(0);

        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), settingsPopup);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);

        FadeTransition fadeTransition = new FadeTransition(Duration.millis(200), settingsPopup);
        fadeTransition.setToValue(1.0);

        ParallelTransition parallelTransition = new ParallelTransition(scaleTransition, fadeTransition);
        parallelTransition.play();
    }

    @FXML
    private void closeProfileMenu() {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(150), settingsPopup);
        fadeTransition.setToValue(0);
        fadeTransition.setOnFinished(e -> {
            settingsPopup.setVisible(false);
            settingsPopup.setManaged(false);
            isProfileMenuOpen = false;
        });
        fadeTransition.play();
    }

    @FXML
    private void handleChangePassword() {
        closeProfileMenu();
        openPasswordChangeDialog();
    }

    @FXML
    private void handleChangeProfilePicture() {
        closeProfileMenu();
        openProfilePictureDialog();
    }

    @FXML
    private void handlePaymentDetails() {
        closeProfileMenu();
        togglePaymentMenu();
    }

    private void initializePaymentComponents() {
        installmentDAO = new InstallmentDAO();
        dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        initializePaymentTable();
        loadPaymentData();
    }

    private void initializePaymentTable() {
        colInstallmentId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("installmentId"));
        colOrderId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("orderId"));
        colInstallmentNumber.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("installmentNumber"));

        colDueDate.setCellValueFactory(cellData -> {
            LocalDate dueDate = cellData.getValue().getDueDate();
            return new javafx.beans.property.SimpleStringProperty(dueDate != null ? dueDate.format(dateFormatter) : "N/A");
        });

        colAmount.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("installmentAmount"));
        colAmount.setCellFactory(column -> new TableCell<Installment, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", amount));
                }
            }
        });

        colStatus.setCellValueFactory(cellData -> {
            Installment installment = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(installment.getStatus());
        });

        colStatus.setCellFactory(column -> new TableCell<Installment, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status) {
                        case "PAID":
                            setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                            break;
                        case "OVERDUE":
                            setStyle("-fx-text-fill: #ff4444; -fx-font-weight: bold;");
                            break;
                        case "PENDING":
                            setStyle("-fx-text-fill: #ffa500; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        colAction.setCellFactory(param -> new TableCell<Installment, Void>() {
            private final Button payButton = new Button("Pay");
            private final Label paidLabel = new Label("Paid ✓");

            {
                payButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10 5 10; -fx-cursor: hand;");
                payButton.setOnAction(event -> {
                    Installment installment = getTableView().getItems().get(getIndex());
                    handleInstallmentPayment(installment);
                });

                paidLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    Installment installment = getTableView().getItems().get(getIndex());
                    if (installment.isFinished()) {
                        setGraphic(paidLabel);
                    } else {
                        setGraphic(payButton);
                    }
                }
            }
        });

        paymentStatusFilter.getItems().addAll("ALL", "PENDING", "PAID", "OVERDUE");
        paymentStatusFilter.setValue("ALL");

        paymentSearchField.textProperty().addListener((observable, oldValue, newValue) -> applyPaymentFilters());
        paymentStatusFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyPaymentFilters());
    }

    private void loadPaymentData() {
        try {
            List<Installment> installments = installmentDAO.getAllInstallments();
            paymentTable.getItems().setAll(installments);
            updatePaymentStatistics();
        } catch (Exception e) {
            logger.error("Error loading payment data", e);
            showErrorAlert("Payment Data Error", "Failed to load installment data.");
        }
    }

    private void updatePaymentStatistics() {
        int pending = 0;
        int overdue = 0;
        int paid = 0;

        for (Installment installment : paymentTable.getItems()) {
            if (installment.isFinished()) {
                paid++;
            } else if (installment.getDueDate().isBefore(LocalDate.now())) {
                overdue++;
            } else {
                pending++;
            }
        }

        paymentPendingCount.setText(String.valueOf(pending));
        paymentOverdueCount.setText(String.valueOf(overdue));
        paymentPaidCount.setText(String.valueOf(paid));
    }

    private void applyPaymentFilters() {
        String searchText = paymentSearchField.getText().trim().toLowerCase();
        String statusFilter = paymentStatusFilter.getValue();

        paymentTable.getItems().clear();
        List<Installment> allInstallments = installmentDAO.getAllInstallments();

        for (Installment installment : allInstallments) {
            boolean matchesSearch = searchText.isEmpty() ||
                    String.valueOf(installment.getOrderId()).contains(searchText) ||
                    String.valueOf(installment.getInstallmentNumber()).contains(searchText);

            boolean matchesStatus = statusFilter.equals("ALL") ||
                    (statusFilter.equals("PENDING") && installment.getStatus().equals("PENDING")) ||
                    (statusFilter.equals("PAID") && installment.getStatus().equals("PAID")) ||
                    (statusFilter.equals("OVERDUE") && installment.getStatus().equals("OVERDUE"));

            if (matchesSearch && matchesStatus) {
                paymentTable.getItems().add(installment);
            }
        }

        updatePaymentStatistics();
    }

    private void handleInstallmentPayment(Installment installment) {
        if (installment.isFinished()) {
            showInfoAlert("Already Paid", "This installment has already been paid.");
            return;
        }

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirm Payment");
        confirmationAlert.setHeaderText("Process Installment Payment");
        confirmationAlert.setContentText(String.format(
                "Process payment for:\nOrder #%d, Installment #%d\nAmount: $%.2f\nDue Date: %s",
                installment.getOrderId(),
                installment.getInstallmentNumber(),
                installment.getInstallmentAmount(),
                installment.getDueDate().format(dateFormatter)
        ));

        ButtonType yesButton = new ButtonType("Confirm Payment", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("Cancel", ButtonBar.ButtonData.NO);
        confirmationAlert.getButtonTypes().setAll(yesButton, noButton);

        confirmationAlert.showAndWait().ifPresent(response -> {
            if (response == yesButton) {
                processInstallmentPayment(installment);
            }
        });
    }

    private void processInstallmentPayment(Installment installment) {
        try {
            boolean success = installmentDAO.markInstallmentAsPaid(installment.getInstallmentId());

            if (success) {
                installment.setFinished(true);
                paymentTable.refresh();
                updatePaymentStatistics();
                logPaymentActivity(installment);

                showSuccessAlert("Payment Processed",
                        String.format("Payment for Installment #%d of Order #%d has been successfully processed.",
                                installment.getInstallmentNumber(), installment.getOrderId()));
            } else {
                showErrorAlert("Payment Failed", "Failed to process the payment. Please try again.");
            }

        } catch (Exception e) {
            logger.error("Error processing payment for installment ID: {}", installment.getInstallmentId(), e);
            showErrorAlert("Payment Error", "An error occurred while processing the payment.");
        }
    }

    private void logPaymentActivity(Installment installment) {
        try {
            String activityDescription = String.format(
                    "Staff %s processed payment for Order #%d, Installment #%d - Amount: $%.2f",
                    SessionStaff.getLoggedInStaffName() != null ? SessionStaff.getLoggedInStaffName() : "Unknown",
                    installment.getOrderId(),
                    installment.getInstallmentNumber(),
                    installment.getInstallmentAmount()
            );

            String checkTableSql = "SELECT COUNT(*) as count FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'activity_log'";
            String insertSql = "INSERT INTO activity_log (user_email, activity_type, activity_description, activity_timestamp) VALUES (?, ?, ?, NOW())";

            try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
                 PreparedStatement checkPs = conn.prepareStatement(checkTableSql);
                 ResultSet rs = checkPs.executeQuery()) {

                if (rs.next() && rs.getInt("count") > 0) {
                    try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                        String staffEmail = SessionStaff.getLoggedInStaffName() != null ?
                                SessionStaff.getLoggedInStaffName().replace(" ", ".").toLowerCase() + "@showroom.com" : "staff@showroom.com";

                        insertPs.setString(1, staffEmail);
                        insertPs.setString(2, "INSTALLMENT_PAYMENT");
                        insertPs.setString(3, activityDescription);
                        insertPs.executeUpdate();
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Activity logging failed", e);
        }
    }

    private void togglePaymentMenu() {
        if (isPaymentMenuOpen) {
            closePaymentMenu();
        } else {
            openPaymentMenu();
        }
    }

    private void openPaymentMenu() {
        loadPaymentData();

        paymentPopup.setVisible(true);
        paymentPopup.setManaged(true);
        isPaymentMenuOpen = true;

        paymentPopup.setScaleX(0.8);
        paymentPopup.setScaleY(0.8);
        paymentPopup.setOpacity(0);

        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), paymentPopup);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);

        FadeTransition fadeTransition = new FadeTransition(Duration.millis(200), paymentPopup);
        fadeTransition.setToValue(1.0);

        ParallelTransition parallelTransition = new ParallelTransition(scaleTransition, fadeTransition);
        parallelTransition.play();
    }

    @FXML
    private void closePaymentMenu() {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(150), paymentPopup);
        fadeTransition.setToValue(0);
        fadeTransition.setOnFinished(e -> {
            paymentPopup.setVisible(false);
            paymentPopup.setManaged(false);
            isPaymentMenuOpen = false;
        });
        fadeTransition.play();
    }

    @FXML
    private void handleRefreshPayments() {
        loadPaymentData();
        showInfoAlert("Refreshed", "Payment data has been refreshed.");
    }

    @FXML
    private void handleClosePayments() {
        closePaymentMenu();
    }

    private void initializeSettingsButtonAnimation() {
        settingsButtonScale = new ScaleTransition(Duration.millis(200), settingsButton);
        settingsButtonScale.setFromX(1.0);
        settingsButtonScale.setFromY(1.0);
        settingsButtonScale.setToX(1.1);
        settingsButtonScale.setToY(1.1);
    }

    private void setupClickOutsideListener() {
        rootPane.setOnMouseClicked(event -> {
            if (isProfileMenuOpen && !isClickInsideSettingsMenu(event)) {
                closeProfileMenu();
            }
            if (isPaymentMenuOpen && !isClickInsidePaymentMenu(event)) {
                closePaymentMenu();
            }
            if (passwordVerifyPane.isVisible() && !isClickInsidePasswordVerification(event)) {
                hidePasswordVerification();
            }
        });
    }

    private boolean isClickInsideSettingsMenu(MouseEvent event) {
        return settingsPopup.getBoundsInParent().contains(event.getX(), event.getY()) ||
                settingsButton.getBoundsInParent().contains(event.getX(), event.getY());
    }

    private boolean isClickInsidePaymentMenu(MouseEvent event) {
        return paymentPopup.getBoundsInParent().contains(event.getX(), event.getY());
    }

    private boolean isClickInsidePasswordVerification(MouseEvent event) {
        return passwordVerifyPane.getBoundsInParent().contains(event.getX(), event.getY());
    }

    private void loadProfilePicture(String userPhotoPath) {
        try {
            if (userPhotoPath != null && !userPhotoPath.isEmpty()) {
                File imageFile = new File(userPhotoPath);
                if (imageFile.exists()) {
                    Image profileImage = new Image(imageFile.toURI().toString());
                    profilePictureView.setImage(profileImage);
                    return;
                }
            }
            loadDefaultProfilePicture();
        } catch (Exception e) {
            logger.error("Error loading profile picture", e);
            loadDefaultProfilePicture();
        }
    }

    private void loadDefaultProfilePicture() {
        try {
            String[] possiblePaths = {
                    "/Image/default_profile.png",
                    "/View/default_profile.png",
                    "/Fxml/default_profile.png",
                    "/Resources/default_profile.png"
            };

            for (String path : possiblePaths) {
                try {
                    InputStream stream = getClass().getResourceAsStream(path);
                    if (stream != null) {
                        Image defaultImage = new Image(stream);
                        profilePictureView.setImage(defaultImage);
                        return;
                    }
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            logger.error("Error loading default profile image", e);
        }
    }

    @FXML
    private void onSettingsButtonEntered(MouseEvent event) {
        settingsButton.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-cursor: hand; " +
                "-fx-text-fill: white; -fx-font-size: 20; -fx-padding: 8 8 8 8;");
        settingsButtonScale.play();
    }

    @FXML
    private void onSettingsButtonExited(MouseEvent event) {
        settingsButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand; " +
                "-fx-text-fill: white; -fx-font-size: 20; -fx-padding: 8 8 8 8;");

        ScaleTransition reverseScale = new ScaleTransition(Duration.millis(200), settingsButton);
        reverseScale.setToX(1.0);
        reverseScale.setToY(1.0);
        reverseScale.play();
    }

    private void openProfilePictureDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        File selectedFile = fileChooser.showOpenDialog(rootPane.getScene().getWindow());
        if (selectedFile != null) {
            try {
                String savedImagePath = saveProfilePicture(selectedFile);
                boolean updated = updateProfilePictureInDatabase(savedImagePath);

                if (updated) {
                    Image newProfileImage = new Image(selectedFile.toURI().toString());
                    profilePictureView.setImage(newProfileImage);
                    showInfoAlert("Success", "Profile picture updated successfully!");
                } else {
                    showErrorAlert("Error", "Failed to update profile picture in database.");
                }
            } catch (Exception e) {
                logger.error("Error updating profile picture", e);
                showErrorAlert("Error", "Failed to update profile picture: " + e.getMessage());
            }
        }
    }

    private String saveProfilePicture(File sourceFile) throws IOException {
        File profileDir = new File("profile_pictures");
        if (!profileDir.exists()) {
            profileDir.mkdirs();
        }

        String fileExtension = getFileExtension(sourceFile.getName());
        String fileName = "profile_" + SessionStaff.getLoggedInStaffId() + "_" + System.currentTimeMillis() + fileExtension;
        File destinationFile = new File(profileDir, fileName);

        Files.copy(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return destinationFile.getAbsolutePath();
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(lastDotIndex) : ".jpg";
    }

    private boolean updateProfilePictureInDatabase(String imagePath) {
        String sql = "UPDATE user_info SET user_photo = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, imagePath);
            ps.setInt(2, SessionStaff.getLoggedInStaffId());
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            logger.error("Error updating profile picture in database", e);
            return false;
        }
    }

    private void openPasswordChangeDialog() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Change Password");
        dialog.setHeaderText("Security Verification Required");

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #000000; -fx-border-color: #d5001f; -fx-border-width: 2;");

        ButtonType changePasswordButtonType = new ButtonType("Change Password", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(changePasswordButtonType, ButtonType.CANCEL);

        Node changePasswordButton = dialog.getDialogPane().lookupButton(changePasswordButtonType);
        changePasswordButton.setStyle("-fx-background-color: #d5001f; -fx-text-fill: white; -fx-font-weight: bold;");

        Node cancelButton = dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color: #000000; -fx-text-fill: white; -fx-border-color: white; -fx-border-width: 1;");

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(25, 35, 25, 35));
        grid.setStyle("-fx-background-color: #000000;");

        Label emailLabel = new Label("EMAIL:");
        emailLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 12;");

        Label passwordLabel = new Label("NEW PASSWORD:");
        passwordLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 12;");

        Label confirmLabel = new Label("CONFIRM PASSWORD:");
        confirmLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 12;");

        Label otpLabel = new Label("VERIFICATION CODE:");
        otpLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 12;");

        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email");
        emailField.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: white; -fx-border-color: #555; -fx-pref-width: 250;");

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Enter new password");
        newPasswordField.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: white; -fx-border-color: #555; -fx-pref-width: 250;");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm new password");
        confirmPasswordField.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: white; -fx-border-color: #555; -fx-pref-width: 250;");

        TextField otpField = new TextField();
        otpField.setPromptText("Enter OTP code");
        otpField.setStyle("-fx-background-color: #1a1a1a; -fx-text-fill: white; -fx-border-color: #555; -fx-pref-width: 150;");

        Button sendOtpButton = new Button("SEND CODE");
        sendOtpButton.setStyle("-fx-background-color: #d5001f; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-width: 100;");

        Label otpStatusLabel = new Label();
        otpStatusLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 11;");

        grid.add(emailLabel, 0, 0);
        grid.add(emailField, 1, 0, 2, 1);

        grid.add(passwordLabel, 0, 1);
        grid.add(newPasswordField, 1, 1, 2, 1);

        grid.add(confirmLabel, 0, 2);
        grid.add(confirmPasswordField, 1, 2, 2, 1);

        grid.add(otpLabel, 0, 3);
        grid.add(otpField, 1, 3);
        grid.add(sendOtpButton, 2, 3);

        grid.add(otpStatusLabel, 1, 4, 2, 1);

        changePasswordButton.setDisable(true);

        sendOtpButton.setOnAction(e -> {
            String email = emailField.getText().trim();
            if (email.isEmpty()) {
                showErrorAlert("Error", "Please enter your email address");
                return;
            }

            if (!isValidEmail(email)) {
                showErrorAlert("Error", "Please enter a valid email address");
                return;
            }

            if (!verifyEmailExists(email)) {
                showErrorAlert("Error", "Email not found in system");
                return;
            }

            boolean otpSent = OTPService.getInstance().sendOTP(email);
            if (otpSent) {
                otpStatusLabel.setText("✓ Verification code sent to your email");
                otpStatusLabel.setStyle("-fx-text-fill: #90EE90;");
            } else {
                otpStatusLabel.setText("✗ Failed to send code. Please try again.");
                otpStatusLabel.setStyle("-fx-text-fill: #ff6b6b;");
            }
        });

        Runnable validateFields = () -> {
            String email = emailField.getText();
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();
            String otp = otpField.getText();

            boolean isValid = !email.isEmpty() &&
                    !newPassword.isEmpty() &&
                    !confirmPassword.isEmpty() &&
                    !otp.isEmpty() &&
                    newPassword.equals(confirmPassword) &&
                    newPassword.length() >= 6;

            changePasswordButton.setDisable(!isValid);

            if (isValid) {
                changePasswordButton.setStyle("-fx-background-color: #d5001f; -fx-text-fill: white; -fx-font-weight: bold;");
            } else {
                changePasswordButton.setStyle("-fx-background-color: #666; -fx-text-fill: #999;");
            }
        };

        emailField.textProperty().addListener((observable, oldValue, newValue) -> validateFields.run());
        newPasswordField.textProperty().addListener((observable, oldValue, newValue) -> validateFields.run());
        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> validateFields.run());
        otpField.textProperty().addListener((observable, oldValue, newValue) -> validateFields.run());

        dialog.getDialogPane().setContent(grid);
        Platform.runLater(emailField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == changePasswordButtonType) {
                return new Pair<>(emailField.getText(), newPasswordField.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();
        result.ifPresent(emailPassword -> {
            String email = emailPassword.getKey();
            String newPassword = emailPassword.getValue();
            String otp = otpField.getText();

            boolean isOtpValid = OTPService.getInstance().verifyOTP(email, otp);
            if (isOtpValid) {
                boolean passwordChanged = changePasswordInDatabase(email, newPassword);
                if (passwordChanged) {
                    showPorscheStyledAlert("Success", "Password updated successfully!");
                } else {
                    showErrorAlert("Error", "Failed to change password. Please try again.");
                }
            } else {
                showErrorAlert("Error", "Invalid or expired verification code.");
            }
        });
    }

    private void showPorscheStyledAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #000000; -fx-border-color: #d5001f;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white; -fx-font-size: 14;");

        alert.showAndWait();
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email != null && email.matches(emailRegex);
    }

    private boolean verifyEmailExists(String email) {
        String sql = "SELECT COUNT(*) as count FROM user_info WHERE user_email = ?";

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt("count") > 0;

        } catch (SQLException e) {
            logger.error("Error verifying email existence: {}", email, e);
        }
        return false;
    }

    private boolean changePasswordInDatabase(String email, String newPassword) {
        String sql = "UPDATE user_info SET password = SHA2(?, 256) WHERE user_email = ?";

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newPassword);
            ps.setString(2, email);
            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                logPasswordChangeActivity(email);
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            logger.error("Error changing password for user: {}", email, e);
            return false;
        }
    }

    private void logPasswordChangeActivity(String email) {
        String checkTableSql = "SELECT COUNT(*) as count FROM information_schema.tables WHERE table_schema = 'car_show_room' AND table_name = 'activity_log'";
        String insertSql = "INSERT INTO activity_log (user_email, activity_type, activity_description, activity_timestamp) VALUES (?, ?, ?, NOW())";

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkTableSql);
             ResultSet rs = checkPs.executeQuery()) {

            if (rs.next() && rs.getInt("count") > 0) {
                try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                    insertPs.setString(1, email);
                    insertPs.setString(2, "PASSWORD_CHANGE");
                    insertPs.setString(3, "User changed their password via OTP verification");
                    insertPs.executeUpdate();
                }
            }
        } catch (SQLException e) {
            logger.error("Error logging password change activity", e);
        }
    }

    @FXML
    private void discover() {
        try {
            navigateToCarManagement();
        } catch (IOException e) {
            logger.error("Failed to navigate to car management", e);
            showErrorAlert("Navigation Error", "Unable to open car models page.");
        }
    }

    @FXML
    private void clickaccessorybtn() {
        try {
            navigateToAssetManagement();
        } catch (IOException e) {
            logger.error("Failed to navigate to asset management", e);
            showErrorAlert("Navigation Error", "Unable to open accessories page.");
        }
    }

    private void navigateToCarManagement() throws IOException {
        String[] possiblePaths = {
                "/Fxml/staffCars.fxml",
                "/View/staffCars.fxml",
                "/staffCars.fxml"
        };

        for (String path : possiblePaths) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
                Parent root = loader.load();
                Stage stage = (Stage) rootPane.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Car Management");
                stage.centerOnScreen();
                return;
            } catch (Exception e) {
            }
        }
        throw new IOException("Could not find staffCars.fxml in any location");
    }

    private void navigateToAssetManagement() throws IOException {
        String[] possiblePaths = {
                "/Fxml/staffAsset.fxml",
                "/View/staffAsset.fxml",
                "/staffAsset.fxml"
        };

        for (String path : possiblePaths) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
                Parent root = loader.load();
                Stage stage = (Stage) rootPane.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Accessory Management");
                stage.centerOnScreen();
                return;
            } catch (Exception e) {
            }
        }
        throw new IOException("Could not find staffAsset.fxml in any location");
    }

    @FXML
    private void logout() {
        handleLogout();
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #000000; -fx-border-color: #d5001f;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white; -fx-font-size: 14;");

        alert.showAndWait();
    }

    private void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #000000; -fx-border-color: #d5001f;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white; -fx-font-size: 14;");

        alert.showAndWait();
    }

    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #000000; -fx-border-color: #4CAF50;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white; -fx-font-size: 14;");

        alert.showAndWait();
    }

    public void handleCloseRequest() {
        DatabaseConnectionManager.getInstance().shutdown();
    }

    private void setupAutoRefresh() {
        Timeline timeline = new Timeline(
                new KeyFrame(
                        Duration.minutes(5),
                        e -> {}
                )
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }
}