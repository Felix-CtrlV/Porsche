package Controllers.java.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

/**
 * Simple email service that works without complex SMTP authentication
 * This is a fallback solution for testing OTP functionality
 */
public class SimpleEmailService {
    private static final Logger logger = LoggerFactory.getLogger(SimpleEmailService.class);
    private static SimpleEmailService instance;
    
    private SimpleEmailService() {}
    
    public static synchronized SimpleEmailService getInstance() {
        if (instance == null) {
            instance = new SimpleEmailService();
        }
        return instance;
    }
    
    /**
     * Sends OTP via console output and popup (for testing purposes)
     * In production, this would be replaced with actual email sending
     */
    public boolean sendOTP(String email, String otp) {
        try {
            logger.info("=== OTP EMAIL SIMULATION ===");
            logger.info("To: {}", email);
            logger.info("Subject: Porsche System - Password Change OTP");
            logger.info("Body:");
            logger.info("Hello,");
            logger.info("");
            logger.info("Your OTP for password change is: {}", otp);
            logger.info("");
            logger.info("This OTP will expire in 5 minutes.");
            logger.info("");
            logger.info("If you did not request this, please ignore this email.");
            logger.info("");
            logger.info("Best regards,");
            logger.info("Porsche System");
            logger.info("=== END EMAIL ===");
            
            // Show popup with OTP
            javafx.application.Platform.runLater(() -> {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                alert.setTitle("OTP Generated");
                alert.setHeaderText("Your OTP Code:");
                alert.setContentText("Your OTP for password change is: " + otp + "\n\nThis OTP will expire in 5 minutes.");
                alert.showAndWait();
            });
            
            // Simulate email sending delay
            Thread.sleep(1000);
            
            logger.info("OTP sent successfully to: {}", email);
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to send OTP to: " + email, e);
            return false;
        }
    }
}
