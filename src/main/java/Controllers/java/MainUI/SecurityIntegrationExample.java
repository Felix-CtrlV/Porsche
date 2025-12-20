package Controllers.java.MainUI;

import Controllers.java.Utils.SecuritySystemInitializer;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Example of how to integrate the security system with your main application
 * 
 * Add this code to your existing main application class (e.g., login.java or Main.java)
 */
public class SecurityIntegrationExample extends Application {
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize the security system BEFORE starting your application
        initializeSecurity();
        
        // Your existing application startup code goes here
        // For example:
        // login loginApp = new login();
        // loginApp.start(primaryStage);
        
        System.out.println("Application started with security system enabled!");
    }
    
    /**
     * Initialize the security system
     */
    private void initializeSecurity() {
        try {
            SecuritySystemInitializer securityInit = SecuritySystemInitializer.getInstance();
            securityInit.initialize();
            
            // Add shutdown hook to properly close security system
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down security system...");
                securityInit.shutdown();
            }));
            
        } catch (Exception e) {
            System.err.println("Failed to initialize security system: " + e.getMessage());
            e.printStackTrace();
            // You can choose to continue without security or exit the application
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}

/*
 * INTEGRATION INSTRUCTIONS:
 * 
 * 1. Add this code to your existing main application class:
 * 
 *    @Override
 *    public void start(Stage primaryStage) throws Exception {
 *        // Initialize security system
 *        SecuritySystemInitializer.getInstance().initialize();
 *        
 *        // Add shutdown hook
 *        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
 *            SecuritySystemInitializer.getInstance().shutdown();
 *        }));
 *        
 *        // Your existing code...
 *    }
 * 
 * 2. The security system will automatically:
 *    - Create database tables if they don't exist
 *    - Start the unlock server on port 8080
 *    - Enable brute force protection on both dashboards
 * 
 * 3. Test the system:
 *    - Try entering wrong passwords 5 times in profile verification
 *    - Check console for email notifications (simulated)
 *    - Visit http://localhost:8080/unlock?token=test to see unlock page
 * 
 * 4. For production:
 *    - Configure email service in SecurityEmailService.java
 *    - Update server host for cross-device compatibility
 *    - Set up proper SSL/HTTPS
 */
