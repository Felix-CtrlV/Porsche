package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;

public class modelselectController {

    @FXML private ScrollPane scrollPane;
    @FXML private Button btnLeft;
    @FXML private Button btnRight;
    @FXML private FlowPane flow_Pane;
    @FXML private Button configure_btn;
    @FXML private Button confirm_btn;

    private final double scrollStep = 0.25;

    @FXML
    void redirect(MouseEvent event) {

    }

    @FXML
    private void initialize() {
        // Left button scroll
        btnLeft.setOnAction(e -> {
            double pos = scrollPane.getHvalue() - scrollStep;
            if (pos < 0) pos = 0;
            scrollPane.setHvalue(pos);
        });

        // Right button scroll
        btnRight.setOnAction(e -> {
            double pos = scrollPane.getHvalue() + scrollStep;
            if (pos > 1) pos = 1;
            scrollPane.setHvalue(pos);
        });
    }

    public Pane createModelPane(String modelName, String imagePath) {
        Pane pane = new Pane();
        pane.setPrefSize(216, 180);
        pane.getStyleClass().add("image_holder");

        javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(
                new javafx.scene.image.Image(getClass().getResourceAsStream(imagePath))
        );
        imageView.setFitWidth(200);
        imageView.setFitHeight(83);
        imageView.setPreserveRatio(true);
        imageView.setLayoutX(8);
        imageView.setLayoutY(14);

        javafx.scene.text.Text text = new javafx.scene.text.Text(modelName);
        text.setLayoutX(14);
        text.setLayoutY(110);
        text.getStyleClass().add("selected_model_font");

        pane.getChildren().addAll(imageView, text);
        return pane;
    }
}
