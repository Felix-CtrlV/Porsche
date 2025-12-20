package Controllers.java.Controllers;

import Database.DatabaseConnectionManager;
import Utils.OTPService;
import Utils.ThreadPoolManager;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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
    private StackPane loadingPane;

    @FXML
    private StackPane messagePane;

    @FXML
    private Label messageLabel;

    @FXML
    private VBox emailPane;

    @FXML
    private VBox otpPane;

    @FXML
    private VBox passwordPane;

    @FXML
    private Label closeBtn;

    @FXML
    private Label emailDisplayLabel;

    @FXML
    private Button resendOtpBtn;

    private String verifiedEmail = null;
    private String verifiedUsername = null;
    private String userInputEmail = null;

    private SequentialTransition currentAnimation;

    @FXML
    public void initialize() {
        // Show only email pane initially
        showEmailPane();

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

        hideMessage();
        showLoading(false);
    }

    private void showEmailPane() {
        emailPane.setVisible(true);
        otpPane.setVisible(false);
        passwordPane.setVisible(false);
        emailTxt.requestFocus();
    }

    private void showOtpPane() {
        emailPane.setVisible(false);
        otpPane.setVisible(true);
        passwordPane.setVisible(false);
        otpTxt.requestFocus();
    }

    private void showPasswordPane() {
        emailPane.setVisible(false);
        otpPane.setVisible(false);
        passwordPane.setVisible(true);
        newPasswordTxt.requestFocus();
    }

    @FXML
    void clickSendOtp(ActionEvent event) {
        String input = emailTxt.getText().trim();

        if (input.isEmpty()) {
            showMessage("Please enter your email or username", false);
            return;
        }
        
        hideMessage();

        showLoading(true);
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
            showLoading(false);
            String result = task.getValue();
            sendOtpBtn.setDisable(false);
            sendOtpBtn.setText("Send Verification Code");

            if ("NOT_FOUND".equals(result)) {
                showMessage("No account found with this email or username", false);
            } else if (result == null) {
                showMessage("Failed to send verification code. Please try again", false);
            } else {
                String[] parts = result.split("\\|");
                verifiedUsername = parts[0];
                String email = parts[1];
                emailDisplayLabel.setText(email);
                showOtpPane();
                showMessage("Verification code sent successfully!", true);
            }
        });

        task.setOnFailed(e -> {
            logger.error("Failed to send OTP", task.getException());
            showLoading(false);
            showMessage("Failed to send verification code. Please try again", false);
            sendOtpBtn.setDisable(false);
            sendOtpBtn.setText("Send Verification Code");
        });

        ThreadPoolManager.getInstance().execute(task);
    }

    @FXML
    void clickResendOtp(ActionEvent event) {
        if (userInputEmail == null) {
            showMessage("Please start from the beginning", false);
            return;
        }
        
        hideMessage();
        showLoading(true);
        resendOtpBtn.setDisable(true);
        resendOtpBtn.setText("Sending...");

        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return OTPService.getInstance().sendOTP(userInputEmail);
            }
        };

        task.setOnSucceeded(e -> {
            showLoading(false);
            boolean sent = task.getValue();
            resendOtpBtn.setDisable(false);
            resendOtpBtn.setText("Resend OTP");

            if (sent) {
                showMessage("Verification code resent successfully!", true);
            } else {
                showMessage("Failed to resend code. Please try again", false);
            }
        });

        task.setOnFailed(e -> {
            logger.error("Failed to resend OTP", task.getException());
            showLoading(false);
            showMessage("Failed to resend code. Please try again", false);
            resendOtpBtn.setDisable(false);
            resendOtpBtn.setText("Resend OTP");
        });

        ThreadPoolManager.getInstance().execute(task);
    }

    @FXML
    void clickVerifyOtp(ActionEvent event) {
        String otp = otpTxt.getText().trim();

        if (otp.isEmpty()) {
            showMessage("Please enter the verification code", false);
            return;
        }

        if (otp.length() != 6) {
            showMessage("Verification code must be 6 digits", false);
            return;
        }
        
        hideMessage();

        showLoading(true);
        verifyOtpBtn.setDisable(true);
        verifyOtpBtn.setText("Verifying...");

        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return OTPService.getInstance().verifyOTP(userInputEmail, otp);
            }
        };

        task.setOnSucceeded(e -> {
            showLoading(false);
            boolean verified = task.getValue();
            verifyOtpBtn.setDisable(false);
            verifyOtpBtn.setText("Verify OTP");

            if (verified) {
                verifiedEmail = userInputEmail;
                showPasswordPane();
                showMessage("Code verified! Please set your new password", true);
            } else {
                showMessage("Invalid or expired verification code", false);
            }
        });

        task.setOnFailed(e -> {
            logger.error("Failed to verify OTP", task.getException());
            showLoading(false);
            showMessage("Failed to verify code. Please try again", false);
            verifyOtpBtn.setDisable(false);
            verifyOtpBtn.setText("Verify OTP");
        });

        ThreadPoolManager.getInstance().execute(task);
    }

    @FXML
    void clickResetPassword(ActionEvent event) {
        String newPassword = newPasswordTxt.getText();
        String confirmPassword = confirmPasswordTxt.getText();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showMessage("Please fill in all password fields", false);
            return;
        }

        if (newPassword.length() < 6) {
            showMessage("Password must be at least 6 characters", false);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showMessage("Passwords do not match", false);
            return;
        }

        if (verifiedEmail == null || verifiedUsername == null) {
            showMessage("Please verify your email first", false);
            return;
        }
        
        hideMessage();

        showLoading(true);
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
            showLoading(false);
            boolean success = task.getValue();
            resetPasswordBtn.setDisable(false);
            resetPasswordBtn.setText("Reset Password");

            if (success) {
                showMessage("Password reset successfully! Redirecting to login...", true);
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
                showMessage("Failed to reset password", false);
            }
        });

        task.setOnFailed(e -> {
            logger.error("Failed to reset password", task.getException());
            showLoading(false);
            showMessage("Failed to reset password. Please try again", false);
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

    private void showMessage(String message, boolean isSuccess) {
        if (currentAnimation != null) {
            currentAnimation.stop();
            currentAnimation = null;
        }

        messageLabel.setText(message);

        if (isSuccess) {
            messagePane.setStyle("-fx-background-radius: 12 12 0 0; -fx-background-color: rgb(34, 197, 94); -fx-border-width: 2; -fx-border-radius: 12 12 0 0; -fx-border-color: rgb(22, 163, 74);");
        } else {
            messagePane.setStyle("-fx-background-radius: 12 12 0 0; -fx-background-color: rgb(255, 60, 41); -fx-border-width: 2; -fx-border-radius: 12 12 0 0; -fx-border-color: rgb(255, 102, 40);");
        }

        messagePane.setVisible(true);
        messagePane.setOpacity(0);
        messagePane.setTranslateY(42);

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
        hide.setOnFinished(e -> messagePane.setVisible(false));

        currentAnimation = new SequentialTransition(show, pause, hide);
        currentAnimation.setOnFinished(e -> {
            messagePane.setVisible(false);
            messagePane.setOpacity(0);
            messagePane.setTranslateY(42);
            currentAnimation = null;
        });
        currentAnimation.play();
    }

    private void hideMessage() {
        if (currentAnimation != null) {
            currentAnimation.stop();
            currentAnimation = null;
        }
        if (messagePane != null) {
            messagePane.setVisible(false);
            messagePane.setOpacity(0);
            messagePane.setTranslateY(42);
        }
    }

    private void showLoading(boolean show) {
        if (loadingPane != null) {
            loadingPane.setVisible(show);
        }
    }

    @FXML
    private void onCloseClicked() {
        try {
            returnToLogin();
        } catch (IOException e) {
            logger.error("Failed to return to login", e);
        }
    }
}
