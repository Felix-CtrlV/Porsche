package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class staffModelSelectController implements Initializable {

    @FXML
    private Label speeddescription;

    @FXML
    private FlowPane flow_Pane;

    @FXML
    private Button backbtn;

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
        for (ModelData data : modelsData) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/staffCarCards.fxml"));
                Parent card = loader.load();
                staffCarcardController cardController = loader.getController();
                cardController.setCarData(data.getModelName(), data.getImagePath(), data.getPrice());
                flow_Pane.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/View/staffCars.fxml"));
            Scene scene = new Scene(root, 1300, 850);
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
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
