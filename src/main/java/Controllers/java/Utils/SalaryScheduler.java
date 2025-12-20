package Controllers.java.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Scheduler to automatically process month-end salary payments
 * Runs daily at midnight and checks if it's the last day of the month
 */
public class SalaryScheduler {
    private static final Logger logger = LoggerFactory.getLogger(SalaryScheduler.class);
    private static ScheduledExecutorService scheduler;
    private static boolean isRunning = false;
    
    /**
     * Start the salary scheduler
     * Checks daily at midnight if it's the last day of the month
     */
    public static void start() {
        if (isRunning) {
            logger.warn("Salary scheduler is already running");
            return;
        }
        
        scheduler = Executors.newScheduledThreadPool(1);
        
        // Calculate delay until next midnight
        long initialDelay = calculateDelayUntilMidnight();
        
        // Schedule daily check at midnight
        scheduler.scheduleAtFixedRate(() -> {
            try {
                logger.info("Running daily salary check...");
                
                if (MonthEndSalaryService.isLastDayOfMonth()) {
                    logger.info("Today is the last day of the month. Processing salaries...");
                    MonthEndSalaryService.processMonthEndSalary();
                } else {
                    logger.info("Not the last day of the month. Next check tomorrow.");
                }
                
            } catch (Exception e) {
                logger.error("Error in salary scheduler", e);
            }
        }, initialDelay, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
        
        isRunning = true;
        logger.info("Salary scheduler started. Next check in {} seconds", initialDelay);
    }
    
    /**
     * Stop the salary scheduler
     */
    public static void stop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            isRunning = false;
            logger.info("Salary scheduler stopped");
        }
    }
    
    /**
     * Calculate seconds until next midnight
     */
    private static long calculateDelayUntilMidnight() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay();
        return ChronoUnit.SECONDS.between(now, nextMidnight);
    }
    
    /**
     * Manually trigger salary processing (for testing or admin action)
     */
    public static void triggerManualProcessing() {
        logger.info("Manual salary processing triggered");
        new Thread(() -> {
            try {
                MonthEndSalaryService.processMonthEndSalary();
            } catch (Exception e) {
                logger.error("Error in manual salary processing", e);
            }
        }).start();
    }
    
    /**
     * Check if scheduler is running
     */
    public static boolean isRunning() {
        return isRunning;
    }
}
