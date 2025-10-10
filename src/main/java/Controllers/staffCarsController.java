package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class staffCarsController {

    @FXML private ImageView nine11_select_image;
    @FXML private ImageView seven18_select_image;
    @FXML private ImageView cayenne_select_image;
    @FXML private ImageView panamera_select_image;
    @FXML private ImageView macan_select_image;
    @FXML private ImageView taycan_select_image;
    @FXML private Button homebtn;

    @FXML
    void redirect(MouseEvent event) {
        System.out.println("Redirect called!");
        ImageView clicked = (ImageView) event.getSource();
        System.out.println("Image clicked: " + clicked.getId());

        try {
            Parent root = FXMLLoader.load(
                    Objects.requireNonNull(getClass().getResource("/View/staffModelSelect.fxml"))
            );
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void home() {
        System.out.println("Home button clicked!");
        try {
            Parent root = FXMLLoader.load(
                    Objects.requireNonNull(getClass().getResource("/View/staffWelcome.fxml"))
            );
            Stage stage = (Stage) homebtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void initialize() {
        nine11_select_image.setImage(new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/Image/911_promo.png"))));
        seven18_select_image.setImage(new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/Image/718_promo.png"))));
        taycan_select_image.setImage(new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/Image/taycan_promo.png"))));
        panamera_select_image.setImage(new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/Image/panamera_promo.png"))));
        macan_select_image.setImage(new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/Image/macan_promo.png"))));
        cayenne_select_image.setImage(new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/Image/cayenne_promo.png"))));
    }
}
