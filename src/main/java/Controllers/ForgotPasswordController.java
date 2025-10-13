package Controllers;

import Database.DatabaseConnectionManager;
import Utils.OTPService;
import Utils.ThreadPoolManager;
import javafx.animation.*;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Objects;

public class ForgotPasswordController {
    private static final Logger logger = LoggerFactory.getLogger(ForgotPasswordController.class);

    @FXML
    private TextField emailTxt;

    @FXML
    private TextField otpTxt;

    @FXML
    private PasswordField newPasswordTxt;

    @FXML
    private PasswordField confirmPasswordTxt;

    @FXML
    private Button sendOtpBtn;

    @FXML
    private Button verifyOtpBtn;

    @FXML
    private Button resetPasswordBtn;

    @FXML
    private Button backToLoginBtn;

    @FXML
    private Label statusLabel;

    @FXML
    private Label statusMessage1;

    @FXML
    private Label statusMessage2;

    @FXML
    private Label statusMessage3;

    @FXML
    private AnchorPane emailPane;

    @FXML
    private AnchorPane otpPane;

    @FXML
    private AnchorPane passwordPane;

    @FXML
    private ImageView closeimg;

    @FXML
    private ImageView porsche_logo_image;

    @FXML
    private Label emailDisplayLabel;

    @FXML
    private Button resendOtpBtn;

    private String verifiedEmail = null;
    private String verifiedUsername = null;
    private String userInputEmail = null;

    @FXML
    public void initialize() {
        // Show only email pane initially
        showEmailPane();

        // Load images
        try {
            Image porsche_logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Image/porsche_logo.png")));
            porsche_logo_image.setImage(porsche_logo);
            Image close = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Image/close.png")));
            closeimg.setImage(close);
        } catch (Exception e) {
            logger.error("Failed to load images", e);
        }

        closeimg.setOnMouseClicked(e -> {
            System.exit(0);
        });
        
        // Add Enter key listeners
        emailTxt.setOnKeyPressed(e -> {
            if (e.getCode().toString().equals("ENTER")) {
                clickSendOtp(new ActionEvent());
            }
        });
        
        otpTxt.setOnKeyPressed(e -> {
            if (e.getCode().toString().equals("ENTER")) {
                clickVerifyOtp(new ActionEvent());
            }
        });
        
        confirmPasswordTxt.setOnKeyPressed(e -> {
            if (e.getCode().toString().equals("ENTER")) {
                clickResetPassword(new ActionEvent());
            }
        });
    }

    private void showEmailPane() {
        emailPane.setVisible(true);
        otpPane.setVisible(false);
        passwordPane.setVisible(false);
    }

    private void showOtpPane() {
        emailPane.setVisible(false);
        otpPane.setVisible(true);
        passwordPane.setVisible(false);
    }

    private void showPasswordPane() {
        emailPane.setVisible(false);
        otpPane.setVisible(false);
        passwordPane.setVisible(true);
    }

    private void showStatus(Label statusLabel, String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setVisible(true);
        
        if (isError) {
            statusLabel.setStyle("-fx-text-fill: #d5001c; -fx-font-size: 12px; -fx-background-color: rgba(213, 0, 28, 0.1); -fx-padding: 8px; -fx-background-radius: 4px;");
        } else {
            statusLabel.setStyle("-fx-text-fill: #00ff00; -fx-font-size: 12px; -fx-background-color: rgba(0, 255, 0, 0.1); -fx-padding: 8px; -fx-background-radius: 4px;");
        }
        
        // Auto-hide after 5 seconds
        PauseTransition pause = new PauseTransition(Duration.seconds(5));
        pause.setOnFinished(e -> statusLabel.setVisible(false));
        pause.play();
    }

    private void hideAllStatus() {
        statusMessage1.setVisible(false);
        statusMessage2.setVisible(false);
        statusMessage3.setVisible(false);
    }

    @FXML
    void clickSendOtp(ActionEvent event) {
        String input = emailTxt.getText().trim();

        if (input.isEmpty()) {
            showStatus(statusMessage1, "Please enter your email or username", true);
            return;
        }
        
        hideAllStatus();

        sendOtpBtn.setDisable(true);
        sendOtpBtn.setText("Sending...");

        Task<String> task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                // Check if input is email or username
                String sql = "SELECT user_id, user_name, user_email, user_phone, user_address, dob, user_role " +
                           "FROM user_info WHERE (user_email = ? OR user_name = ?) AND user_status = 1 LIMIT 1";
                
                try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
                     PreparedStatement stmt = con.prepareStatement(sql)) {

                    stmt.setString(1, input);
                    stmt.setString(2, input);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            String username = rs.getString("user_name");
                            String email = rs.getString("user_email");
                            userInputEmail = email;
                            // Send OTP
                            boolean sent = OTPService.getInstance().sendOTP(email);
                            if (sent) {
                                return username + "|" + email;
                            } else {
                                return null;
                            }
                        } else {
                            return "NOT_FOUND";
                        }
                    }
                }
            }
        };

        task.setOnSucceeded(e -> {
            String result = task.getValue();
            sendOtpBtn.setDisable(false);
            sendOtpBtn.setText("Send OTP");

            if ("NOT_FOUND".equals(result)) {
                showStatus(statusMessage1, "No account found with this email or username", true);
            } else if (result == null) {
                showStatus(statusMessage1, "Failed to send verification code. Please try again", true);
            } else {
                String[] parts = result.split("\\|");
                verifiedUsername = parts[0];
                String email = parts[1];
                emailDisplayLabel.setText(email);
                showOtpPane();
                showStatus(statusMessage2, "Verification code sent successfully!", false);
            }
        });

        task.setOnFailed(e -> {
            logger.error("Failed to send OTP", task.getException());
            showStatus(statusMessage1, "Failed to send verification code. Please try again", true);
            sendOtpBtn.setDisable(false);
            sendOtpBtn.setText("SEND VERIFICATION CODE");
        });

        ThreadPoolManager.getInstance().execute(task);
    }

    @FXML
    void clickResendOtp(ActionEvent event) {
        if (userInputEmail == null) {
            showStatus(statusMessage2, "Please start from the beginning", true);
            return;
        }
        
        hideAllStatus();
        resendOtpBtn.setDisable(true);
        resendOtpBtn.setText("Sending...");

        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return OTPService.getInstance().sendOTP(userInputEmail);
            }
        };

        task.setOnSucceeded(e -> {
            boolean sent = task.getValue();
            resendOtpBtn.setDisable(false);
            resendOtpBtn.setText("Resend OTP");

            if (sent) {
                showStatus(statusMessage2, "Verification code resent successfully!", false);
            } else {
                showStatus(statusMessage2, "Failed to resend code. Please try again", true);
            }
        });

        task.setOnFailed(e -> {
            logger.error("Failed to resend OTP", task.getException());
            showStatus(statusMessage2, "Failed to resend code. Please try again", true);
            resendOtpBtn.setDisable(false);
            resendOtpBtn.setText("Resend Code");
        });

        ThreadPoolManager.getInstance().execute(task);
    }

    @FXML
    void clickVerifyOtp(ActionEvent event) {
        String otp = otpTxt.getText().trim();

        if (otp.isEmpty()) {
            showStatus(statusMessage2, "Please enter the verification code", true);
            return;
        }

        if (otp.length() != 6) {
            showStatus(statusMessage2, "Verification code must be 6 digits", true);
            return;
        }
        
        hideAllStatus();

        verifyOtpBtn.setDisable(true);
        verifyOtpBtn.setText("Verifying...");

        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return OTPService.getInstance().verifyOTP(userInputEmail, otp);
            }
        };

        task.setOnSucceeded(e -> {
            boolean verified = task.getValue();
            verifyOtpBtn.setDisable(false);
            verifyOtpBtn.setText("Verify OTP");

            if (verified) {
                verifiedEmail = userInputEmail;
                showPasswordPane();
                showStatus(statusMessage3, "Code verified! Please set your new password", false);
            } else {
                showStatus(statusMessage2, "Invalid or expired verification code", true);
            }
        });

        task.setOnFailed(e -> {
            logger.error("Failed to verify OTP", task.getException());
            showStatus(statusMessage2, "Failed to verify code. Please try again", true);
            verifyOtpBtn.setDisable(false);
            verifyOtpBtn.setText("VERIFY CODE");
        });

        ThreadPoolManager.getInstance().execute(task);
    }

    @FXML
    void clickResetPassword(ActionEvent event) {
        String newPassword = newPasswordTxt.getText();
        String confirmPassword = confirmPasswordTxt.getText();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showStatus(statusMessage3, "Please fill in all password fields", true);
            return;
        }

        if (newPassword.length() < 6) {
            showStatus(statusMessage3, "Password must be at least 6 characters", true);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showStatus(statusMessage3, "Passwords do not match", true);
            return;
        }

        if (verifiedEmail == null || verifiedUsername == null) {
            showStatus(statusMessage3, "Please verify your email first", true);
            return;
        }
        
        hideAllStatus();

        resetPasswordBtn.setDisable(true);
        resetPasswordBtn.setText("Resetting...");

        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                // Update password using SHA2 hash function
                String sql = "UPDATE user_info SET password = SHA2(?, 256) WHERE user_name = ?";
                
                try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
                     PreparedStatement stmt = con.prepareStatement(sql)) {

                    stmt.setString(1, newPassword);
                    stmt.setString(2, verifiedUsername);
                    int rowsAffected = stmt.executeUpdate();
                    return rowsAffected > 0;
                }
            }
        };

        task.setOnSucceeded(e -> {
            boolean success = task.getValue();
            resetPasswordBtn.setDisable(false);
            resetPasswordBtn.setText("Reset Password");

            if (success) {
                showStatus(statusMessage3, "Password reset successfully! Redirecting to login...", false);
                // Wait 2 seconds then go back to login
                PauseTransition pause = new PauseTransition(Duration.seconds(2));
                pause.setOnFinished(ev -> {
                    try {
                        returnToLogin();
                    } catch (IOException ex) {
                        logger.error("Failed to return to login", ex);
                    }
                });
                pause.play();
            } else {
                showStatus(statusMessage3, "Failed to reset password", true);
            }
        });

        task.setOnFailed(e -> {
            logger.error("Failed to reset password", task.getException());
            showStatus(statusMessage3, "Failed to reset password. Please try again", true);
            resetPasswordBtn.setDisable(false);
            resetPasswordBtn.setText("RESET PASSWORD");
        });

        ThreadPoolManager.getInstance().execute(task);
    }

    @FXML
    void clickBackToLogin(ActionEvent event) throws IOException {
        returnToLogin();
    }

    private void returnToLogin() throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/View/login.fxml")));
        Stage stage = (Stage) emailPane.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }
}
