package Controllers;

import Model.managerOverview;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

public class managerCarPartProgressBarController {

    @FXML
    private Label Name;

    @FXML
    private Label message;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label progressPercentage;

    @FXML
    private Label rank;

    @FXML
    private Label soldQty;

    public void setDate(managerOverview overview) {
        // Handle long car/part names with smart truncation
        String itemName = overview.getInventoryName();
        if (itemName != null) {
            Name.setText(itemName);
            // Always add tooltip to show full name on hover
            Name.setTooltip(new javafx.scene.control.Tooltip(itemName));
        } else {
            Name.setText("Unknown");
        }
        
        rank.setText(overview.getRank());
        soldQty.setText(String.valueOf(overview.getSoldQty()) + " Units");

        double progress = calculateProgress(overview.getSoldQty(), overview.getTargetQty());

        progressBar.setProgress(progress);

        progressPercentage.setText(String.format("%.1f%%", progress * 100));

        setProgressMessage(progress, overview.getSoldQty(), overview.getTargetQty());
    }

    private double calculateProgress(int soldQty, int targetQty) {
        if (targetQty <= 0) {
            return 0.0;
        }

        double progress = (double) soldQty / targetQty;

        // Cap at 100% if exceeded target
        return Math.min(progress, 1.0);
    }

    private void setProgressMessage(double progress, int soldQty, int targetQty) {
        int remaining = targetQty - soldQty;

        if (progress >= 1.0) {
            int exceeded = soldQty - targetQty;
            if (exceeded > 0) {
                message.setText("Exceeded target by " + exceeded + "! 🏆");
            } else {
                message.setText("Target Achieved! 🎉");
            }
            message.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
        } else if (progress >= 0.9) {
            message.setText("Almost there! " + remaining + " more to go!");
            message.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
        } else if (progress >= 0.75) {
            message.setText("Good pace! " + remaining + " remaining");
            message.setStyle("-fx-text-fill: #3b82f6; -fx-font-weight: bold;");
        } else if (progress >= 0.5) {
            message.setText("Halfway there! " + remaining + " to target");
            message.setStyle("-fx-text-fill: #6366f1; -fx-font-weight: bold;");
        } else if (progress >= 0.25) {
            message.setText("Making progress - " + remaining + " needed");
            message.setStyle("-fx-text-fill: #8b5cf6; -fx-font-weight: bold;");
        } else {
            message.setText("Getting started - " + remaining + " to target");
            message.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
        }
    }
}