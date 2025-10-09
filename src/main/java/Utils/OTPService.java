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
    
    // Email configuration - UPDATE THESE WITH YOUR SMTP DETAILS
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String SENDER_EMAIL = "your-email@gmail.com"; // UPDATE THIS
    private static final String SENDER_PASSWORD = "your-app-password"; // UPDATE THIS (use App Password for Gmail)
    
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
            
            // Send email
            boolean sent = sendEmail(email, otp);
            
            if (sent) {
                logger.info("OTP sent successfully to: {}", email);
                return true;
            } else {
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
        props.put("mail.smtp.ssl.trust", SMTP_HOST);
        
        jakarta.mail.Session mailSession = jakarta.mail.Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
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
            
            Transport.send(message);
            return true;
            
        } catch (MessagingException e) {
            logger.error("Failed to send email", e);
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
