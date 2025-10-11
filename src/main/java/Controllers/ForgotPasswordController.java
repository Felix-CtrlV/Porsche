package Controllers;

import Database.DatabaseConnectionManager;
import Utils.OTPService;
import Utils.ThreadPoolManager;
import javafx.animation.*;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class ForgotPasswordController {
    private static final Logger logger = LoggerFactory.getLogger(ForgotPasswordController.class);

    @FXML
    private ImageView closeImg;

    @FXML
    private VBox stage1Pane, stage2Pane, stage3Pane;

    @FXML
    private TextField usernameField, otpField;

    @FXML
    private PasswordField newPasswordField, confirmPasswordField;

    @FXML
    private Button verifyUserBtn, verifyOtpBtn, resendOtpBtn, resetPasswordBtn;

    @FXML
    private Label messageLabel;

    @FXML
    private StackPane messagePane;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Circle stage1Circle, stage2Circle, stage3Circle;

    private final OTPService otpService = OTPService.getInstance();
    private String userEmail;
    private int userId;
    private int currentStage = 1;

    @FXML
    public void initialize() {
        messagePane.setTranslateY(42);

        closeImg.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Image/close.png"))));
        closeImg.setOnMouseClicked(e -> closeWindow());

        // Enter key handlers
        usernameField.setOnAction(e -> verifyUserBtn.fire());
        otpField.setOnAction(e -> verifyOtpBtn.fire());
        confirmPasswordField.setOnAction(e -> resetPasswordBtn.fire());

        // Initialize stage 1
        showStage(1);
    }

    @FXML
    void onVerifyUser(ActionEvent event) {
        String usernameOrEmail = usernameField.getText().trim();

        if (usernameOrEmail.isEmpty()) {
            showMessage("Please enter your username or email", MessageType.ERROR);
            return;
        }

        verifyUserBtn.setDisable(true);
        verifyUserBtn.setText("Checking...");

        Task<UserData> task = new Task<>() {
            @Override
            protected UserData call() throws Exception {
                return getUserByUsernameOrEmail(usernameOrEmail);
            }
        };

        task.setOnSucceeded(e -> {
            UserData userData = task.getValue();
            if (userData != null) {
                userId = userData.userId;
                userEmail = userData.email;
                showMessage("OTP sent to " + maskEmail(userEmail), MessageType.SUCCESS);
                
                // Send OTP
                sendOTP();
            } else {
                showMessage("User not found. Please check your username or email.", MessageType.ERROR);
                verifyUserBtn.setDisable(false);
                verifyUserBtn.setText("Send OTP");
            }
        });

        task.setOnFailed(e -> {
            logger.error("Failed to verify user", task.getException());
            showMessage("Error checking user. Please try again.", MessageType.ERROR);
            verifyUserBtn.setDisable(false);
            verifyUserBtn.setText("Send OTP");
        });

        ThreadPoolManager.getInstance().execute(task);
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
        verifyOtpBtn.setText("Verifying...");

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return otpService.verifyOTP(userEmail, otp);
            }
        };

        task.setOnSucceeded(e -> {
            if (task.getValue()) {
                showMessage("OTP verified successfully!", MessageType.SUCCESS);
                showStage(3);
            } else {
                showMessage("Invalid or expired OTP!", MessageType.ERROR);
                verifyOtpBtn.setDisable(false);
                verifyOtpBtn.setText("Verify");
                otpField.clear();
                otpField.requestFocus();
            }
        });

        task.setOnFailed(e -> {
            logger.error("Failed to verify OTP", task.getException());
            showMessage("Error verifying OTP", MessageType.ERROR);
            verifyOtpBtn.setDisable(false);
            verifyOtpBtn.setText("Verify");
        });

        ThreadPoolManager.getInstance().execute(task);
    }

    @FXML
    void onResendOTP(ActionEvent event) {
        resendOtpBtn.setDisable(true);
        resendOtpBtn.setText("Sending...");
        sendOTP();
    }

    @FXML
    void onResetPassword(ActionEvent event) {
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showMessage("Please fill in all password fields", MessageType.ERROR);
            return;
        }

        if (newPassword.length() < 6) {
            showMessage("Password must be at least 6 characters long", MessageType.ERROR);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showMessage("Passwords do not match", MessageType.ERROR);
            return;
        }

        resetPasswordBtn.setDisable(true);
        resetPasswordBtn.setText("Updating...");

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return updatePasswordInDatabase(userId, newPassword);
            }
        };

        task.setOnSucceeded(e -> {
            if (task.getValue()) {
                showMessage("Password updated successfully! You can now login with your new password.", MessageType.SUCCESS);
                // Close window after a delay
                PauseTransition delay = new PauseTransition(Duration.seconds(2));
                delay.setOnFinished(ev -> closeWindow());
                delay.play();
            } else {
                showMessage("Failed to update password. Please try again.", MessageType.ERROR);
                resetPasswordBtn.setDisable(false);
                resetPasswordBtn.setText("Reset Password");
            }
        });

        task.setOnFailed(e -> {
            logger.error("Failed to update password", task.getException());
            showMessage("Error updating password", MessageType.ERROR);
            resetPasswordBtn.setDisable(false);
            resetPasswordBtn.setText("Reset Password");
        });

        ThreadPoolManager.getInstance().execute(task);
    }

    private void sendOTP() {
        Task<Boolean> sendOtpTask = new Task<>() {
            @Override
            protected Boolean call() {
                return otpService.sendOTP(userEmail);
            }
        };

        sendOtpTask.setOnSucceeded(e -> {
            if (sendOtpTask.getValue()) {
                showMessage("OTP sent successfully to " + maskEmail(userEmail), MessageType.SUCCESS);
                showStage(2);
            } else {
                showMessage("Failed to send OTP. Please check email configuration.", MessageType.ERROR);
                verifyUserBtn.setDisable(false);
                verifyUserBtn.setText("Send OTP");
                resendOtpBtn.setDisable(false);
                resendOtpBtn.setText("Resend");
            }
        });

        sendOtpTask.setOnFailed(e -> {
            logger.error("Failed to send OTP", sendOtpTask.getException());
            showMessage("Error sending OTP", MessageType.ERROR);
            verifyUserBtn.setDisable(false);
            verifyUserBtn.setText("Send OTP");
            resendOtpBtn.setDisable(false);
            resendOtpBtn.setText("Resend");
        });

        ThreadPoolManager.getInstance().execute(sendOtpTask);
    }

    private UserData getUserByUsernameOrEmail(String usernameOrEmail) {
        String sql = "SELECT user_id, user_name, user_email FROM user_info WHERE user_name = ? OR user_email = ?";

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, usernameOrEmail);
            ps.setString(2, usernameOrEmail);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new UserData(rs.getInt("user_id"), rs.getString("user_name"), rs.getString("user_email"));
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to get user by username or email", e);
        }

        return null;
    }

    private boolean updatePasswordInDatabase(int userId, String newPassword) {
        String sql = "UPDATE user_info SET password = SHA2(?,256) WHERE user_id = ?";

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // MySQL will hash the password using SHA-2 (256-bit)
            ps.setString(1, newPassword);
            ps.setInt(2, userId);

            int rowsAffected = ps.executeUpdate();
            logger.info("Password updated (SHA-2 hashed) for user ID: {}", userId);
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.error("Failed to update password for user ID: " + userId, e);
            return false;
        }
    }

    private void showStage(int stage) {
        currentStage = stage;

        // Hide all stages
        stage1Pane.setVisible(false);
        stage2Pane.setVisible(false);
        stage3Pane.setVisible(false);

        // Show current stage
        switch (stage) {
            case 1:
                stage1Pane.setVisible(true);
                usernameField.requestFocus();
                break;
            case 2:
                stage2Pane.setVisible(true);
                otpField.requestFocus();
                break;
            case 3:
                stage3Pane.setVisible(true);
                newPasswordField.requestFocus();
                break;
        }

        // Update progress
        updateProgress(stage);
    }

    private void updateProgress(int stage) {
        double progress = stage / 3.0;
        progressBar.setProgress(progress);

        // Update stage circles
        stage1Circle.setFill(stage >= 1 ? Color.web("#d5001c") : Color.web("#cccccc"));
        stage2Circle.setFill(stage >= 2 ? Color.web("#d5001c") : Color.web("#cccccc"));
        stage3Circle.setFill(stage >= 3 ? Color.web("#d5001c") : Color.web("#cccccc"));
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
            messagePane.setTranslateY(42);
        }

        messageLabel.setText(message);

        // Set message pane style based on type
        if (type == MessageType.SUCCESS) {
            messagePane.setStyle("-fx-background-radius: 12px; -fx-background-color: #2e7d32; -fx-border-width: 2; -fx-border-radius: 12px; -fx-border-color: #4caf50;");
        } else {
            messagePane.setStyle("-fx-background-radius: 12px; -fx-background-color: #d5001c; -fx-border-width: 2; -fx-border-radius: 12px; -fx-border-color: #ff5722;");
        }

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), messagePane);
        slideIn.setFromY(42);
        slideIn.setToY(4);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), messagePane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ParallelTransition show = new ParallelTransition(slideIn, fadeIn);

        PauseTransition pause = new PauseTransition(Duration.seconds(3));

        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), messagePane);
        slideOut.setFromY(4);
        slideOut.setToY(42);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), messagePane);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        ParallelTransition hide = new ParallelTransition(slideOut, fadeOut);

        currentAnimation = new SequentialTransition(show, pause, hide);
        currentAnimation.play();
    }

    // Inner class to hold user data
    private static class UserData {
        final int userId;
        final String username;
        final String email;

        UserData(int userId, String username, String email) {
            this.userId = userId;
            this.username = username;
            this.email = email;
        }
    }
}
