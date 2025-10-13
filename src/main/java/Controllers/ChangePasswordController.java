package Controllers;

import Database.DatabaseConnectionManager;
import Utils.OTPService;
import Utils.Session;
import javafx.animation.*;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

public class ChangePasswordController {
    private static final Logger logger = LoggerFactory.getLogger(ChangePasswordController.class);

    @FXML
    private ImageView closeImg;

    @FXML
    private VBox passwordVerificationPane, otpVerificationPane, newPasswordPane;

    @FXML
    private PasswordField currentPasswordField, newPasswordField, confirmPasswordField;

    @FXML
    private TextField otpField;

    @FXML
    private Button verifyPasswordBtn, verifyOtpBtn, resendOtpBtn, changePasswordBtn;

    @FXML
    private Label passwordVerifyStatus, otpVerifyStatus, changePasswordStatus;

    @FXML
    private StackPane messagePane;

    @FXML
    private Label messageLabel;

    private final OTPService otpService = OTPService.getInstance();
    private String userEmail;
    private int userId;

    @FXML
    public void initialize() {
        messagePane.setTranslateY(34);

        Session session = Session.getInstance();
        userId = session.getUserid();
        userEmail = session.getEmail();

        closeImg.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Image/closeRemoved.png"))));
        closeImg.setOnMouseClicked(e -> closeWindow());

        // Enter key handlers
        currentPasswordField.setOnAction(e -> verifyPasswordBtn.fire());
        otpField.setOnAction(e -> verifyOtpBtn.fire());
        confirmPasswordField.setOnAction(e -> changePasswordBtn.fire());
    }

    @FXML
    void onVerifyPassword(ActionEvent event) {
        String password = currentPasswordField.getText().trim();

        if (password.isEmpty()) {
            showMessage("Please enter your current password", MessageType.ERROR);
            return;
        }

        Session session = Session.getInstance();
        if (!password.equals(session.getPassword())) {
            showMessage("Incorrect password!", MessageType.ERROR);
            currentPasswordField.clear();
            currentPasswordField.requestFocus();
            return;
        }

        // Password correct, send OTP
        verifyPasswordBtn.setDisable(true);
        passwordVerifyStatus.setText("Sending OTP to " + maskEmail(userEmail) + "...");

        Task<Boolean> sendOtpTask = new Task<>() {
            @Override
            protected Boolean call() {
                return otpService.sendOTP(userEmail);
            }
        };

        sendOtpTask.setOnSucceeded(e -> {
            if (sendOtpTask.getValue()) {
                showMessage("OTP sent successfully to your email!", MessageType.SUCCESS);
                showOtpPane();
            } else {
                showMessage("Failed to send OTP. Please check email configuration.", MessageType.ERROR);
                verifyPasswordBtn.setDisable(false);
            }
        });

        sendOtpTask.setOnFailed(e -> {
            showMessage("Error sending OTP: " + sendOtpTask.getException().getMessage(), MessageType.ERROR);
            verifyPasswordBtn.setDisable(false);
        });

        new Thread(sendOtpTask).start();
    }

    @FXML
    void onVerifyOTP(ActionEvent event) {
        String otp = otpField.getText().trim();

        if (otp.isEmpty()) {
            showMessage("Please enter the OTP", MessageType.ERROR);
            return;
        }

        if (otp.length() != 6 || !otp.matches("\\d+")) {
            showMessage("OTP must be 6 digits", MessageType.ERROR);
            return;
        }

        verifyOtpBtn.setDisable(true);

        Task<Boolean> verifyTask = new Task<>() {
            @Override
            protected Boolean call() {
                return otpService.verifyOTP(userEmail, otp);
            }
        };

        verifyTask.setOnSucceeded(e -> {
            if (verifyTask.getValue()) {
                showMessage("OTP verified successfully!", MessageType.SUCCESS);
                showNewPasswordPane();
            } else {
                showMessage("Invalid or expired OTP!", MessageType.ERROR);
                verifyOtpBtn.setDisable(false);
                otpField.clear();
                otpField.requestFocus();
            }
        });

        verifyTask.setOnFailed(e -> {
            showMessage("Error verifying OTP", MessageType.ERROR);
            verifyOtpBtn.setDisable(false);
        });

        new Thread(verifyTask).start();
    }

    @FXML
    void onResendOTP(ActionEvent event) {
        resendOtpBtn.setDisable(true);
        otpVerifyStatus.setText("Resending OTP...");

        Task<Boolean> resendTask = new Task<>() {
            @Override
            protected Boolean call() {
                return otpService.sendOTP(userEmail);
            }
        };

        resendTask.setOnSucceeded(e -> {
            if (resendTask.getValue()) {
                showMessage("OTP resent successfully!", MessageType.SUCCESS);
            } else {
                showMessage("Failed to resend OTP", MessageType.ERROR);
            }
            resendOtpBtn.setDisable(false);
        });

        resendTask.setOnFailed(e -> {
            showMessage("Error resending OTP", MessageType.ERROR);
            resendOtpBtn.setDisable(false);
        });

        new Thread(resendTask).start();
    }

    @FXML
    void onChangePassword(ActionEvent event) {
        String newPassword = newPasswordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showMessage("Please fill in all fields", MessageType.ERROR);
            return;
        }

        if (newPassword.length() < 6) {
            showMessage("Password must be at least 6 characters", MessageType.ERROR);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showMessage("Passwords do not match!", MessageType.ERROR);
            return;
        }

        changePasswordBtn.setDisable(true);
        changePasswordStatus.setText("Updating password...");

        Task<Boolean> changeTask = new Task<>() {
            @Override
            protected Boolean call() {
                return updatePasswordInDatabase(userId, newPassword);
            }
        };

        changeTask.setOnSucceeded(e -> {
            if (changeTask.getValue()) {
                showMessage("Password changed successfully!", MessageType.SUCCESS);
                
                // Update session
                Session.getInstance().setPassword(newPassword);
                
                // Close window after 2 seconds
                PauseTransition pause = new PauseTransition(Duration.seconds(2));
                pause.setOnFinished(ev -> closeWindow());
                pause.play();
            } else {
                showMessage("Failed to update password in database", MessageType.ERROR);
                changePasswordBtn.setDisable(false);
            }
        });

        changeTask.setOnFailed(e -> {
            showMessage("Error updating password: " + changeTask.getException().getMessage(), MessageType.ERROR);
            changePasswordBtn.setDisable(false);
        });

        new Thread(changeTask).start();
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

    private void closeWindow() {
        Stage stage = (Stage) closeImg.getScene().getWindow();
        stage.close();
    }

    private enum MessageType {
        SUCCESS, ERROR
    }

    private SequentialTransition currentAnimation;

    private void showMessage(String message, MessageType type) {
        if (currentAnimation != null) {
            currentAnimation.stop();
            messagePane.setTranslateY(34);
        }

        messageLabel.setText(message);

        // Set color based on type
        if (type == MessageType.SUCCESS) {
            messagePane.setStyle("-fx-background-radius: 0 0 12 12; -fx-background-color: rgb(76, 175, 80); -fx-border-width: 2; -fx-border-radius: 0 0 12 12; -fx-border-color: rgb(102, 187, 106);");
        } else {
            messagePane.setStyle("-fx-background-radius: 0 0 12 12; -fx-background-color: rgb(255, 60, 41); -fx-border-width: 2; -fx-border-radius: 0 0 12 12; -fx-border-color: rgb(255, 102, 40);");
        }

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), messagePane);
        slideIn.setFromY(34);
        slideIn.setToY(2);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), messagePane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ParallelTransition show = new ParallelTransition(slideIn, fadeIn);

        PauseTransition pause = new PauseTransition(Duration.seconds(3));

        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), messagePane);
        slideOut.setFromY(2);
        slideOut.setToY(34);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), messagePane);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        ParallelTransition hide = new ParallelTransition(slideOut, fadeOut);

        currentAnimation = new SequentialTransition(show, pause, hide);
        currentAnimation.play();
    }
}
