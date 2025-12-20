package Controllers.java.Controllers;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class SplashController {
    private static final Logger logger = LoggerFactory.getLogger(SplashController.class);

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label loadingLabel;

    @FXML
    private ImageView porsche_logo;

    @FXML
    private AnchorPane blackOverlay;

    private Stage stage;

    @FXML
    public void initialize() {
        // Load logo
        try {
            Image logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Image/porsche_logo.png")));
            porsche_logo.setImage(logo);
        } catch (Exception e) {
            logger.error("Failed to load logo", e);
        }

        // Start loading animation
        startLoading();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void startLoading() {
        // Set initial progress
        progressBar.setProgress(0.0);
        
        Task<Void> loadingTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Loading steps with progress
                updateProgress(0.0, 100.0);
                updateMessage("Initializing system...");
                Thread.sleep(250);

                updateProgress(15.0, 100.0);
                updateMessage("Loading resources...");
                Thread.sleep(250);

                updateProgress(35.0, 100.0);
                updateMessage("Connecting to database...");
                Thread.sleep(250);

                updateProgress(55.0, 100.0);
                updateMessage("Loading UI components...");
                Thread.sleep(250);

                updateProgress(75.0, 100.0);
                updateMessage("Preparing interface...");
                Thread.sleep(250);

                updateProgress(95.0, 100.0);
                updateMessage("Finalizing...");
                Thread.sleep(200);

                updateProgress(100.0, 100.0);
                updateMessage("Ready!");
                Thread.sleep(300);

                return null;
            }
        };

        // Bind progress and message to UI
        progressBar.progressProperty().bind(loadingTask.progressProperty());
        loadingLabel.textProperty().bind(loadingTask.messageProperty());

        loadingTask.setOnSucceeded(event -> {
            // Unbind before transition
            progressBar.progressProperty().unbind();
            loadingLabel.textProperty().unbind();
            // Fade to black then show login
            fadeToBlackAndShowLogin();
        });

        loadingTask.setOnFailed(event -> {
            logger.error("Loading failed", loadingTask.getException());
            progressBar.progressProperty().unbind();
            loadingLabel.textProperty().unbind();
            showLoginScreen();
        });

        // Run in background thread
        Thread thread = new Thread(loadingTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void fadeToBlackAndShowLogin() {
        Platform.runLater(() -> {
            // Show black overlay
            blackOverlay.setVisible(true);
            blackOverlay.setOpacity(0.0);
            
            // Fade in black overlay
            FadeTransition fadeToBlack = new FadeTransition(Duration.millis(400), blackOverlay);
            fadeToBlack.setFromValue(0.0);
            fadeToBlack.setToValue(1.0);
            fadeToBlack.setOnFinished(e -> {
                // Wait a moment in black, then show login
                PauseTransition pause = new PauseTransition(Duration.millis(0));
                pause.setOnFinished(ev -> showLoginScreen());
                pause.play();
            });
            fadeToBlack.play();
        });
    }

    private void showLoginScreen() {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/View/login.fxml")));
            Scene scene = new Scene(root);
            
            Platform.runLater(() -> {
                // Start with login invisible
                root.setOpacity(0.0);
                stage.setScene(scene);
                stage.centerOnScreen();
                
                // Small pause while in black, then fade in login
                PauseTransition pause = new PauseTransition(Duration.millis(0));
                pause.setOnFinished(e -> {
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(100), root);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();
                });
                pause.play();
            });
        } catch (Exception e) {
            logger.error("Failed to load login screen", e);
        }
    }
}
