package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.event.ActionEvent;
import java.io.IOException;

public class staffModelSelectController {

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private FlowPane flow_Pane;

    @FXML
    private Button back_btn;

    @FXML
    private void initialize() {
        scrollPane.setPannable(true);
        loadCarCard("911 Carrera", "/Image/911_model.png");
        loadCarCard("Cayenne Turbo", "/Image/911_select_model.png");
        loadCarCard("Panamera", "/Image/911_select_model.png");
    }

    private void loadCarCard(String modelName, String imagePath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/staffCarcards.fxml"));
            Pane card = loader.load();
            staffCarcardController controller = loader.getController();
            controller.setCard(modelName, new javafx.scene.image.Image(getClass().getResourceAsStream(imagePath)));
            flow_Pane.getChildren().add(card);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/View/staffCars.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
