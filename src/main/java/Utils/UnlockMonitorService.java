package Utils;

import javafx.application.Platform;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service that monitors for account unlock events and notifies the UI in real-time
 */
public class UnlockMonitorService {
    private static UnlockMonitorService instance;
    private ScheduledExecutorService scheduler;
    private boolean isMonitoring = false;
    private int userId;
    private String userType;
    private Runnable unlockCallback;
    
    private UnlockMonitorService() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "UnlockMonitor");
            t.setDaemon(true);
            return t;
        });
    }
    
    public static UnlockMonitorService getInstance() {
        if (instance == null) {
            instance = new UnlockMonitorService();
        }
        return instance;
    }
    
    /**
     * Start monitoring for unlock events for a specific user
     * @param userId The user ID to monitor
     * @param userType The user type ("admin" or "manager")
     * @param unlockCallback Callback to execute when account is unlocked
     */
    public void startMonitoring(int userId, String userType, Runnable unlockCallback) {
        if (isMonitoring) {
            stopMonitoring();
        }
        
        this.userId = userId;
        this.userType = userType;
        this.unlockCallback = unlockCallback;
        this.isMonitoring = true;
        
        System.out.println("🔍 Starting unlock monitoring for user " + userId + " (" + userType + ")");
        
        // Check every 2 seconds for unlock status
        scheduler.scheduleWithFixedDelay(this::checkUnlockStatus, 2, 2, TimeUnit.SECONDS);
    }
    
    /**
     * Stop monitoring for unlock events
     */
    public void stopMonitoring() {
        if (isMonitoring) {
            isMonitoring = false;
            System.out.println("🛑 Stopped unlock monitoring");
        }
    }
    
    /**
     * Check if the account has been unlocked
     */
    private void checkUnlockStatus() {
        if (!isMonitoring) {
            return;
        }
        
        try {
            SecurityManager securityManager = SecurityManager.getInstance();
            boolean isLocked = securityManager.isAccountLocked(userId, userType);
            
            if (!isLocked) {
                // Account has been unlocked!
                System.out.println("✅ Account unlocked detected for user " + userId + " - hiding overlay");
                
                // Stop monitoring
                stopMonitoring();
                
                // Execute unlock callback on JavaFX thread
                if (unlockCallback != null) {
                    Platform.runLater(() -> {
                        try {
                            unlockCallback.run();
                            System.out.println("🎉 Lock overlay hidden successfully");
                        } catch (Exception e) {
                            System.err.println("❌ Error hiding lock overlay: " + e.getMessage());
                            e.printStackTrace();
                        }
                    });
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error checking unlock status: " + e.getMessage());
        }
    }
    
    /**
     * Shutdown the monitoring service
     */
    public void shutdown() {
        stopMonitoring();
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
