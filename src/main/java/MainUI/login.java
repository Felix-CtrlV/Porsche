package MainUI;

import Controllers.SplashController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class login extends Application {
    private static final Logger logger = LoggerFactory.getLogger(login.class);

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Load splash screen immediately
            FXMLLoader splashLoader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/View/splash.fxml")));
            Parent splashRoot = splashLoader.load();
            
            // Get controller and set stage
            SplashController splashController = splashLoader.getController();
            splashController.setStage(primaryStage);
            
            Scene splashScene = new Scene(splashRoot);
            
            primaryStage.initStyle(StageStyle.UNDECORATED);
            primaryStage.setScene(splashScene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
            primaryStage.show();
            
        } catch (Exception e) {
            logger.error("Failed to load splash screen", e);
            // Fallback to login screen directly
            loadLoginScreen(primaryStage);
        }
    }

    private void loadLoginScreen(Stage stage) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/View/staffWelcome.fxml")));
            Scene scene = new Scene(root);
            
            Platform.runLater(() -> {
                stage.initStyle(StageStyle.UNDECORATED);
                stage.setScene(scene);
                stage.setResizable(true);
                stage.centerOnScreen();
                stage.show();
            });
        } catch (Exception e) {
            logger.error("Failed to load login screen", e);
        }
    }

    /**
     * Starts the application directly at the login screen, skipping the splash screen.
     * Used for logout functionality.
     */
    public void startDirectLogin(Stage primaryStage) throws Exception {
        loadLoginScreen(primaryStage);
    }
}
