package Controllers.java.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Manages thread pools for asynchronous operations.
 * Provides efficient thread management instead of creating new threads manually.
 */
public class ThreadPoolManager {
    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolManager.class);
    private static ThreadPoolManager instance;
    private final ExecutorService executorService;

    private ThreadPoolManager() {
        // Create a cached thread pool that reuses threads
        this.executorService = Executors.newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true); // Daemon threads won't prevent JVM shutdown
            return thread;
        });
        logger.info("Thread pool manager initialized");
    }

    public static synchronized ThreadPoolManager getInstance() {
        if (instance == null) {
            instance = new ThreadPoolManager();
        }
        return instance;
    }

    /**
     * Submits a task for execution.
     * @param task The task to execute
     */
    public void execute(Runnable task) {
        executorService.execute(task);
    }

    /**
     * Shuts down the thread pool gracefully.
     */
    public void shutdown() {
        logger.info("Shutting down thread pool...");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    logger.error("Thread pool did not terminate");
                }
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("Thread pool shut down complete");
    }
}
