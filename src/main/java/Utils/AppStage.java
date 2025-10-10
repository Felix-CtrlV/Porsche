package Utils;

import javafx.stage.Stage;

public class AppStage {

    private static Stage stage;

    public static void setStage(Stage primaryStage) {
        stage = primaryStage;
        stage.setWidth(1300);
        stage.setHeight(850);
    }

    public static Stage getStage() {
        return stage;
    }
}
