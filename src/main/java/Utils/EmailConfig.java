package Utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Email configuration helper
 */
public class EmailConfig {
    private static final String CONFIG_FILE = "email.properties";
    
    public static Properties loadEmailConfig() {
        Properties props = new Properties();
        
        // Try to load from file first
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            props.load(fis);
            return props;
        } catch (IOException e) {
            // If file doesn't exist, create default config
            return createDefaultConfig();
        }
    }
    
    private static Properties createDefaultConfig() {
        Properties props = new Properties();
        
        // Default configuration - you can modify these
        props.setProperty("smtp.host", "smtp.gmail.com");
        props.setProperty("smtp.port", "587");
        props.setProperty("smtp.username", "porscheemailingsys@gmail.com");
        props.setProperty("smtp.password", "kcsvkdbzgzqtplod");
        props.setProperty("smtp.auth", "true");
        props.setProperty("smtp.starttls", "true");
        
        return props;
    }
}
