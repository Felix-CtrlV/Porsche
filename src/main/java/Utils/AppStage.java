package Utils;

import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;

public class AppStage {

    private static Stage stage;

    public static void setStage(Stage primaryStage) {
        stage = primaryStage;
        stage.setWidth(1188);
        stage.setHeight(580);
        stage.setResizable(true);
        stage.setFullScreenExitHint("");
        centerStage();
    }

    public static Stage getStage() {
        return stage;
    }

    private static void centerStage() {
        if (stage != null) {
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
            stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
        }
    }

    public static void resize(double width, double height) {
        if (stage != null) {
            stage.setWidth(width);
            stage.setHeight(height);
            centerStage();
        }
    }
}
