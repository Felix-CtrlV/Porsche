package Controllers;

import Utils.defaultStage;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.util.Objects;

public class staffWelcomeController {

    @FXML private VBox vbox;
    @FXML private Label topicLabel;
    @FXML private Label headingLabel;
    @FXML private Button discoverbtn;
    @FXML private Button logoutbtn;
    @FXML private Button accessorybtn;
    @FXML private ImageView backgroundImage;
    @FXML private StackPane imageContainer;

    public void initialize() {
        Image image = new Image(Objects.requireNonNull(
                getClass().getResource("/Image/startUpImage.png")
        ).toExternalForm());
        backgroundImage.setImage(image);

        backgroundImage.fitWidthProperty().bind(imageContainer.widthProperty());
        backgroundImage.fitHeightProperty().bind(imageContainer.heightProperty());

        vbox.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                DoubleBinding widthScale = newScene.widthProperty().divide(1188.0);
                DoubleBinding heightScale = newScene.heightProperty().divide(580.0);
                widthScale.addListener((o, oldVal, newVal) -> scaleFonts(newVal.doubleValue(), heightScale.get()));
                heightScale.addListener((o, oldVal, newVal) -> scaleFonts(widthScale.get(), newVal.doubleValue()));
                scaleFonts(widthScale.get(), heightScale.get());
            }
        });
    }

    private void scaleFonts(double widthScale, double heightScale) {
        double scale = Math.min(widthScale, heightScale);
        topicLabel.setStyle("-fx-font-size: " + (24 * scale) + "px;");
        headingLabel.setStyle("-fx-font-size: " + (36 * scale) + "px;");
    }

    @FXML
    private void discover(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/View/staffCars.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    private void logout(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/View/login.fxml")));
        Scene scene = new Scene(root);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        defaultStage ds = new defaultStage();
        ds.setStage(stage);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    @FXML
    private void clickaccessorybtn(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/View/staffasset.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}
