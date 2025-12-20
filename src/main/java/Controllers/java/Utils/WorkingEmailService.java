package Controllers.java.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Working email service that uses different SMTP settings
 */
public class WorkingEmailService {
    private static final Logger logger = LoggerFactory.getLogger(WorkingEmailService.class);
    private static WorkingEmailService instance;
    
    private WorkingEmailService() {}
    
    public static synchronized WorkingEmailService getInstance() {
        if (instance == null) {
            instance = new WorkingEmailService();
        }
        return instance;
    }
    
    /**
     * Sends OTP via email using multiple SMTP providers
     */
    public boolean sendOTP(String email, String otp) {
        // Try different SMTP providers
        String[] smtpConfigs = {
            // Gmail with app password
            "smtp.gmail.com:587:porscheemailingsys@gmail.com:kcsvkdbzgzqtplod",
            // Gmail SSL
            "smtp.gmail.com:465:porscheemailingsys@gmail.com:kcsvkdbzgzqtplod"
        };
        
        for (String config : smtpConfigs) {
            String[] parts = config.split(":");
            if (trySendEmail(email, otp, parts[0], parts[1], parts[2], parts[3])) {
                return true;
            }
        }
        
        // If all SMTP providers fail, show popup
        showOTPPopup(otp);
        return true;
    }
    
    private boolean trySendEmail(String toEmail, String otp, String host, String port, String username, String password) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);
            props.put("mail.smtp.ssl.trust", "*");
            props.put("mail.smtp.ssl.checkserveridentity", "false");
            props.put("mail.smtp.connectiontimeout", "10000");
            props.put("mail.smtp.timeout", "10000");
            
            jakarta.mail.Session session = jakarta.mail.Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
            
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Porsche System - Password Change OTP");
            
            String emailBody = String.format(
                "Hello,\n\n" +
                "Your OTP for password change is: %s\n\n" +
                "This OTP will expire in 5 minutes.\n\n" +
                "If you did not request this, please ignore this email.\n\n" +
                "Best regards,\n" +
                "Porsche System",
                otp
            );
            
            message.setText(emailBody);
            
            Transport.send(message);
            logger.info("Email sent successfully via {}:{}", host, port);
            return true;
            
        } catch (Exception e) {
            logger.warn("Failed to send via {}:{} - {}", host, port, e.getMessage());
            return false;
        }
    }
    
    private void showOTPPopup(String otp) {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("OTP Generated");
            alert.setHeaderText("Email sending failed, but here's your OTP:");
            alert.setContentText("Your OTP for password change is: " + otp + "\n\nThis OTP will expire in 5 minutes.");
            alert.showAndWait();
        });
    }
}
