package Controllers;

import DAO.CarDAO;
import Model.car;
import javafx.animation.*;
import javafx.fxml.*;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class staffModelSelectController implements Initializable {
    @FXML private ScrollPane scrollPane;
    @FXML private FlowPane flow_Pane;

    private final CarDAO carDAO = new CarDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        scrollPane.setFitToWidth(true);
        loadCarsFromDB();
    }

    private void loadCarsFromDB() {
        try {
            List<car> cars = carDAO.getAllCars();
            flow_Pane.getChildren().clear();

            for (car c : cars) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/staffCarcards.fxml"));
                VBox card = loader.load();

                staffCarcardController cardController = loader.getController();
                String price = "$" + String.format("%.2f", c.getCurrent_price());
                cardController.setCarData("Model " + c.getModelid(), "/images/default.png", price);

                card.setOnMouseClicked(e -> openCustomizeScreen(e, c));
                flow_Pane.getChildren().add(card);
            }

            animateCardsOnLoad();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openCustomizeScreen(javafx.scene.input.MouseEvent event, car selectedCar) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/staffCustomize.fxml"));
            Parent root = loader.load();

            staffCustomizeController controller = loader.getController();
            controller.setSelectedCar(selectedCar);
            controller.setSelectedModel("Model " + selectedCar.getModelid(), "/images/default.png", "$" + selectedCar.getCurrent_price());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void animateCardsOnLoad() {
        int delay = 0;
        for (Node node : flow_Pane.getChildren()) {
            FadeTransition fade = new FadeTransition(Duration.millis(400), node);
            fade.setFromValue(0); fade.setToValue(1);
            fade.setDelay(Duration.millis(delay));
            TranslateTransition slide = new TranslateTransition(Duration.millis(400), node);
            slide.setFromY(20); slide.setToY(0);
            slide.setDelay(Duration.millis(delay));
            new ParallelTransition(fade, slide).play();
            delay += 70;
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
