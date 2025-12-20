package Controllers.java.Utils;

import javafx.stage.Screen;
import javafx.stage.Stage;

public class defaultStage {

    private Stage stage;

    public void setStage(Stage stage){
        this.stage = stage;
        stage.setMaximized(true);

        // Fallback in case maximizing is disallowed by the OS or stage style
        if (!stage.isMaximized()) {
            Screen.getPrimary().getVisualBounds();
            stage.setWidth(Screen.getPrimary().getVisualBounds().getWidth());
            stage.setHeight(Screen.getPrimary().getVisualBounds().getHeight());
        }
    }

}
