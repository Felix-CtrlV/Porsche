package MainUI;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.SplashScreen;

import java.io.IOException;
import java.util.Objects;

public class login extends Application {

    private double xOffset = 0;
    private double yOffset = 0;

    public void start(Stage stage) throws Exception {

        SplashScreen splash = SplashScreen.getSplashScreen();
            Parent root = null;
            try {
                root = FXMLLoader.load(Objects.requireNonNull(this.getClass().getResource("/View/login.fxml")));
            } catch (
                    IOException e) {
                throw new RuntimeException(e);
            }
            Scene scene = new Scene(root);

        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        root.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setOnShown(e -> stage.centerOnScreen());
        stage.show();
        if(splash!=null) {
            splash.close();
        }
    }
}
