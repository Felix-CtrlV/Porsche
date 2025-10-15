package Controllers;

import Database.DatabaseConnectionManager;
import Utils.OTPService;
import Utils.Session;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class ChangePasswordController {
    private static final Logger logger = LoggerFactory.getLogger(ChangePasswordController.class);

    // UI Components
    @FXML private Label closeBtn, otpEmailLabel;
    @FXML private VBox passwordVerificationPane, otpVerificationPane, newPasswordPane;
    @FXML private PasswordField currentPasswordField, newPasswordField, confirmPasswordField;
    @FXML private TextField otpField;
    @FXML private Button verifyPasswordBtn, verifyOtpBtn, resendOtpBtn, changePasswordBtn;
    @FXML private Label passwordVerifyStatus, otpVerifyStatus, changePasswordStatus;
    @FXML private HBox messagePane;
    @FXML private Label messageLabel;
    @FXML private StackPane loadingPane;

    // Data
    private final OTPService otpService = OTPService.getInstance();
    private String userEmail;
    private int userId;

    @FXML
    public void initialize() {
        Session session = Session.getInstance();
        userId = session.getUserid();
        userEmail = session.getEmail();

        // Set up enter key handlers
        currentPasswordField.setOnAction(e -> onVerifyPassword());
        otpField.setOnAction(e -> onVerifyOTP());
        confirmPasswordField.setOnAction(e -> onChangePassword());
    }

    @FXML
    void onVerifyPassword() {
        String password = currentPasswordField.getText().trim();

        if (password.isEmpty()) {
            showMessage("Please enter your current password", false);
            return;
        }

        Session session = Session.getInstance();
        if (!password.equals(session.getPassword())) {
            showMessage("Incorrect password!", false);
            currentPasswordField.clear();
            currentPasswordField.requestFocus();
            return;
        }

        // Password correct, send OTP
        showLoading(true);
        verifyPasswordBtn.setDisable(true);
        passwordVerifyStatus.setText("Sending OTP...");

        new Thread(() -> {
            boolean success = otpService.sendOTP(userEmail);
            javafx.application.Platform.runLater(() -> {
                showLoading(false);
                if (success) {
                    showMessage("OTP sent to " + maskEmail(userEmail), true);
                    showOtpPane();
                } else {
                    showMessage("Failed to send OTP", false);
                    verifyPasswordBtn.setDisable(false);
                }
            });
        }).start();
    }

    @FXML
    void onVerifyOTP() {
        String otp = otpField.getText().trim();

        if (otp.isEmpty()) {
            showMessage("Please enter the OTP", false);
            return;
        }

        if (otp.length() != 6 || !otp.matches("\\d+")) {
            showMessage("OTP must be 6 digits", false);
            return;
        }

        showLoading(true);
        verifyOtpBtn.setDisable(true);

        new Thread(() -> {
            boolean verified = otpService.verifyOTP(userEmail, otp);
            javafx.application.Platform.runLater(() -> {
                showLoading(false);
                if (verified) {
                    showMessage("OTP verified successfully!", true);
                    showNewPasswordPane();
                } else {
                    showMessage("Invalid or expired OTP!", false);
                    verifyOtpBtn.setDisable(false);
                    otpField.clear();
                    otpField.requestFocus();
                }
            });
        }).start();
    }

    @FXML
    void onResendOTP() {
        showLoading(true);
        resendOtpBtn.setDisable(true);

        new Thread(() -> {
            boolean success = otpService.sendOTP(userEmail);
            javafx.application.Platform.runLater(() -> {
                showLoading(false);
                if (success) {
                    showMessage("OTP resent successfully!", true);
                } else {
                    showMessage("Failed to resend OTP", false);
                }
                resendOtpBtn.setDisable(false);
            });
        }).start();
    }

    @FXML
    void onChangePassword() {
        String newPassword = newPasswordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showMessage("Please fill in all fields", false);
            return;
        }

        if (newPassword.length() < 6) {
            showMessage("Password must be at least 6 characters", false);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showMessage("Passwords do not match!", false);
            return;
        }

        showLoading(true);
        changePasswordBtn.setDisable(true);

        new Thread(() -> {
            boolean success = updatePasswordInDatabase(userId, newPassword);
            javafx.application.Platform.runLater(() -> {
                showLoading(false);
                if (success) {
                    showMessage("Password changed successfully!", true);
                    Session.getInstance().setPassword(newPassword);
                    
                    // Close window after 2 seconds
                    PauseTransition pause = new PauseTransition(Duration.seconds(2));
                    pause.setOnFinished(ev -> closeWindow());
                    pause.play();
                } else {
                    showMessage("Failed to update password", false);
                    changePasswordBtn.setDisable(false);
                }
            });
        }).start();
    }

    private boolean updatePasswordInDatabase(int userId, String newPassword) {
        String sql = "UPDATE user_info SET password = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newPassword);
            ps.setInt(2, userId);

            int rowsAffected = ps.executeUpdate();
            logger.info("Password updated for user ID: {}", userId);
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.error("Failed to update password for user ID: " + userId, e);
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

    @FXML
    public void closeWindow() {
        Stage stage = (Stage) closeBtn.getScene().getWindow();
        stage.close();
    }
    
    private void showLoading(boolean show) {
        loadingPane.setVisible(show);
    }

    private void showMessage(String message, boolean isSuccess) {
        messageLabel.setText(message);
        
        // Set color based on type
        if (isSuccess) {
            messagePane.setStyle("-fx-background-color: #10b981; -fx-background-radius: 0 0 16 16;");
        } else {
            messagePane.setStyle("-fx-background-color: #ef4444; -fx-background-radius: 0 0 16 16;");
        }
        
        messagePane.setVisible(true);
        
        // Auto-hide after 3 seconds
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> messagePane.setVisible(false));
        pause.play();
    }
}
