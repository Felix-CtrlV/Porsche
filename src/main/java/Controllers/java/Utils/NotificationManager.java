package Controllers.java.Utils;

import Controllers.managerDashboardController;
import javafx.application.Platform;

/**
 * Centralized notification manager for toast notifications and confirmation dialogs
 * Provides a unified interface for all controllers to show notifications
 */
public class NotificationManager {
    private static NotificationManager instance;
    private managerDashboardController dashboardController;

    private NotificationManager() {
    }

    public static NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    /**
     * Set the dashboard controller reference for showing notifications
     */
    public void setDashboardController(managerDashboardController controller) {
        this.dashboardController = controller;
    }

    /**
     * Show a success toast notification
     */
    public void showSuccess(String title, String message) {
        showToast(title, message, "success");
    }

    /**
     * Show an error toast notification
     */
    public void showError(String title, String message) {
        showToast(title, message, "error");
    }

    /**
     * Show a warning toast notification
     */
    public void showWarning(String title, String message) {
        showToast(title, message, "warning");
    }

    /**
     * Show an info toast notification
     */
    public void showInfo(String title, String message) {
        showToast(title, message, "info");
    }

    /**
     * Show a generic toast notification with custom type
     */
    public void showToast(String title, String message, String type) {
        if (dashboardController == null) {
            System.err.println("Dashboard controller not set. Cannot show toast: " + title);
            return;
        }

        Platform.runLater(() -> {
            dashboardController.showToast(title, message, type);
        });
    }

    /**
     * Show a confirmation dialog
     */
    public void showConfirmation(String title, String message, Runnable onConfirm) {
        if (dashboardController == null) {
            System.err.println("Dashboard controller not set. Cannot show confirmation: " + title);
            return;
        }

        Platform.runLater(() -> {
            dashboardController.showConfirmationDialog(title, message, onConfirm);
        });
    }

    /**
     * Show a confirmation dialog with custom buttons
     */
    public void showConfirmation(String title, String message, String confirmText, String cancelText, Runnable onConfirm) {
        if (dashboardController == null) {
            System.err.println("Dashboard controller not set. Cannot show confirmation: " + title);
            return;
        }

        Platform.runLater(() -> {
            dashboardController.showConfirmationDialog(title, message, onConfirm);
        });
    }
}
