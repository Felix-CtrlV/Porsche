package Controllers;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class staffModelSelectController implements Initializable {

    @FXML
    private FlowPane flow_Pane;

    @FXML
    private javafx.scene.control.Button backbtn;

    private final Duration fadeDuration = Duration.millis(300);

    private final List<ModelData> modelsData = Arrays.asList(
            new ModelData("911 Carrera", "/Image/911_model.png", "$110,000"),
            new ModelData("Taycan", "/Image/taycan_model.png", "$120,000"),
            new ModelData("Cayenne", "/Image/cayenne_model.png", "$105,000")
    );

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadCarCards();
    }

    private void loadCarCards() {
        int delay = 0;
        for (ModelData data : modelsData) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/staffCarCards.fxml"));
                Parent card = loader.load();
                staffCarcardController cardController = loader.getController();
                cardController.setCarData(data.getModelName(), data.getImagePath(), data.getPrice());

                flow_Pane.getChildren().add(card);

                card.setOpacity(0);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(400), card);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.setDelay(Duration.millis(delay));
                fadeIn.play();
                delay += 150;

                card.setOnMouseEntered(e -> scale(card, 1.05));
                card.setOnMouseExited(e -> scale(card, 1.0));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void scale(Node node, double scale) {
        ScaleTransition st = new ScaleTransition(Duration.millis(200), node);
        st.setToX(scale);
        st.setToY(scale);
        st.play();
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/View/staffCars.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 1376, 768);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class ModelData {
        private final String modelName;
        private final String imagePath;
        private final String price;

        public ModelData(String modelName, String imagePath, String price) {
            this.modelName = modelName;
            this.imagePath = imagePath;
            this.price = price;
        }

        public String getModelName() { return modelName; }
        public String getImagePath() { return imagePath; }
        public String getPrice() { return price; }
    }
}
