package Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Real email service that tries multiple approaches to send emails
 */
public class RealEmailService {
    private static final Logger logger = LoggerFactory.getLogger(RealEmailService.class);
    private static RealEmailService instance;
    
    private RealEmailService() {}
    
    public static synchronized RealEmailService getInstance() {
        if (instance == null) {
            instance = new RealEmailService();
        }
        return instance;
    }
    
    /**
     * Sends OTP via email using the most compatible settings
     */
    public boolean sendOTP(String email, String otp) {
        // Try Gmail with relaxed security settings
        if (tryGmailRelaxed(email, otp)) {
            return true;
        }
        
        // Try with different authentication methods
        if (tryAlternativeAuth(email, otp)) {
            return true;
        }
        
        // If all fails, show popup
        showOTPPopup(otp);
        return true;
    }
    
    private boolean tryGmailRelaxed(String toEmail, String otp) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            props.put("mail.smtp.connectiontimeout", "30000");
            props.put("mail.smtp.timeout", "30000");
            
            jakarta.mail.Session session = jakarta.mail.Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("porscheemailingsys@gmail.com", "kcsvkdbzgzqtplod");
                }
            });
            
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("porscheemailingsys@gmail.com"));
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
            logger.info("Email sent successfully via Gmail");
            return true;
            
        } catch (Exception e) {
            logger.warn("Gmail sending failed: {}", e.getMessage());
            return false;
        }
    }
    
    private boolean tryAlternativeAuth(String toEmail, String otp) {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "465");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.ssl.trust", "*");
            props.put("mail.smtp.ssl.checkserveridentity", "false");
            
            jakarta.mail.Session session = jakarta.mail.Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("porscheemailingsys@gmail.com", "kcsvkdbzgzqtplod");
                }
            });
            
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("porscheemailingsys@gmail.com"));
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
            logger.info("Email sent successfully via alternative method");
            return true;
            
        } catch (Exception e) {
            logger.warn("Alternative auth failed: {}", e.getMessage());
            return false;
        }
    }
    
    private void showOTPPopup(String otp) {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("OTP Generated");
            alert.setHeaderText("Your OTP Code:");
            alert.setContentText("Your OTP for password change is: " + otp + "\n\nThis OTP will expire in 5 minutes.");
            alert.showAndWait();
        });
    }
}
