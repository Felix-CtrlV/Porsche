package MainUI;

import Controllers.SplashController;
import Utils.SecuritySystemInitializer;
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
        // Initialize security system and unlock server
        initializeSecurity();
        
        try {
            FXMLLoader splashLoader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/View/splash.fxml")));
            Parent splashRoot = splashLoader.load();

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
            loadLoginScreen(primaryStage);
        }
    }
    
    /**
     * Initialize the security system including the unlock server
     */
    private void initializeSecurity() {
        try {
            SecuritySystemInitializer securityInit = SecuritySystemInitializer.getInstance();
            securityInit.initialize();
            logger.info("✅ Security system initialized successfully");
            
            // Add shutdown hook to properly close security system
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    securityInit.shutdown();
                    logger.info("Security system shutdown completed");
                } catch (Exception e) {
                    logger.error("Error during security system shutdown", e);
                }
            }));
            
        } catch (Exception e) {
            logger.error("❌ Failed to initialize security system", e);
        }
    }

    private void loadLoginScreen(Stage stage) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/View/login.fxml")));
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

    public void startDirectLogin(Stage primaryStage) throws Exception {
        loadLoginScreen(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
