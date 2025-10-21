package Utils;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class OTPService {
    private static final Logger logger = LoggerFactory.getLogger(OTPService.class);
    private static OTPService instance;
    
    private final ConcurrentHashMap<String, OTPData> otpStorage = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    
    // Email configuration - Using Gmail
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String SENDER_EMAIL = "porscheemailingsys@gmail.com";
    private static final String SENDER_PASSWORD = "kcsvkdbzgzqtplod"; // Gmail App Password
    
    // Check if email is configured
    private boolean isEmailConfigured() {
        return !SENDER_EMAIL.equals("your-email@gmail.com") && 
               !SENDER_PASSWORD.equals("your-app-password") &&
               SENDER_PASSWORD.length() >= 6; // Minimum password length
    }
    
    private OTPService() {}
    
    public static synchronized OTPService getInstance() {
        if (instance == null) {
            instance = new OTPService();
        }
        return instance;
    }
    
    /**
     * Generates a 6-digit OTP
     */
    private String generateOTP() {
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
    
    /**
     * Sends OTP to the specified email
     * @param email Recipient email address
     * @return true if OTP was sent successfully
     */
    public boolean sendOTP(String email) {
        try {
            String otp = generateOTP();
            long expiryTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5); // 5 minutes expiry
            
            // Store OTP
            otpStorage.put(email, new OTPData(otp, expiryTime));
            
            logger.info("Attempting to send OTP to: {}", email);
            
            // Try real email service first
            boolean sent = RealEmailService.getInstance().sendOTP(email, otp);
            
            if (!sent) {
                logger.warn("Real email failed, using simple email service for testing");
                sent = SimpleEmailService.getInstance().sendOTP(email, otp);
            }
            
            if (sent) {
                logger.info("OTP sent successfully to: {}", email);
                return true;
            } else {
                logger.error("Failed to send email to: {}", email);
                otpStorage.remove(email);
                return false;
            }
        } catch (Exception e) {
            logger.error("Failed to send OTP to: " + email, e);
            return false;
        }
    }
    
    /**
     * Verifies the OTP for a given email
     * @param email Email address
     * @param otp OTP to verify
     * @return true if OTP is valid and not expired
     */
    public boolean verifyOTP(String email, String otp) {
        OTPData data = otpStorage.get(email);
        
        if (data == null) {
            logger.warn("No OTP found for email: {}", email);
            return false;
        }
        
        if (System.currentTimeMillis() > data.expiryTime) {
            otpStorage.remove(email);
            logger.warn("OTP expired for email: {}", email);
            return false;
        }
        
        if (data.otp.equals(otp)) {
            otpStorage.remove(email); // Remove after successful verification
            logger.info("OTP verified successfully for: {}", email);
            return true;
        }
        
        logger.warn("Invalid OTP for email: {}", email);
        return false;
    }
    
    /**
     * Sends email using Jakarta Mail
     */
    private boolean sendEmail(String toEmail, String otp) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.trust", "*"); // Trust all certificates for testing
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.connectiontimeout", "30000");
        props.put("mail.smtp.timeout", "30000");
        props.put("mail.smtp.ssl.checkserveridentity", "false"); // Disable SSL verification for testing
        
        logger.debug("SMTP Configuration: Host={}, Port={}, From={}", SMTP_HOST, SMTP_PORT, SENDER_EMAIL);
        
        jakarta.mail.Session mailSession = jakarta.mail.Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                logger.debug("Authenticating with email: {}", SENDER_EMAIL);
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });
        
        try {
            Message message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
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
            
            logger.debug("Sending email to: {}", toEmail);
            Transport.send(message);
            logger.info("Email sent successfully to: {}", toEmail);
            return true;
            
        } catch (MessagingException e) {
            logger.error("Failed to send email to: " + toEmail, e);
            logger.error("MessagingException details: " + e.getMessage());
            if (e.getCause() != null) {
                logger.error("Root cause: " + e.getCause().getMessage());
            }
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error sending email", e);
            return false;
        }
    }
    
    /**
     * Sends a custom HTML email
     * @param toEmail Recipient email address
     * @param subject Email subject
     * @param htmlContent HTML content of the email
     * @return true if email was sent successfully
     */
    public boolean sendCustomEmail(String toEmail, String subject, String htmlContent) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.trust", "*");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.connectiontimeout", "30000");
        props.put("mail.smtp.timeout", "30000");
        props.put("mail.smtp.ssl.checkserveridentity", "false");
        
        logger.debug("Sending custom email to: {}", toEmail);
        
        jakarta.mail.Session mailSession = jakarta.mail.Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });
        
        try {
            Message message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress(SENDER_EMAIL, "Porsche Security System"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setContent(htmlContent, "text/html; charset=utf-8");
            
            Transport.send(message);
            logger.info("Custom email sent successfully to: {}", toEmail);
            return true;
            
        } catch (MessagingException e) {
            logger.error("Failed to send custom email to: " + toEmail, e);
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error sending custom email", e);
            return false;
        }
    }
    
    /**
     * Clears OTP for a specific email
     */
    public void clearOTP(String email) {
        otpStorage.remove(email);
    }
    
    /**
     * Inner class to store OTP data
     */
    private static class OTPData {
        final String otp;
        final long expiryTime;
        
        OTPData(String otp, long expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }
    }
}
