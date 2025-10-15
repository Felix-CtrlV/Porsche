package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;

public class staffCarcardController {

    @FXML
    private ImageView model_image;

    @FXML
    private Label model_name;

    @FXML
    private Label model_price;

    private String imagePath;
    private String carName;
    private String carPrice;

    public void setCarData(String name, String imagePath, String price) {
        this.carName = name;
        this.imagePath = imagePath;
        this.carPrice = price;

        if (model_name != null) model_name.setText(name);
        if (model_price != null) model_price.setText(price);

        if (imagePath != null && model_image != null) {
            try (InputStream stream = getClass().getResourceAsStream(imagePath)) {
                if (stream != null) {
                    Image image = new Image(stream);
                    model_image.setImage(image);
                } else {
                    System.err.println("⚠ Image not found: " + imagePath);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleCardClick(MouseEvent event) {
        System.out.println("Selected Car: " + carName + " | Price: " + carPrice);
    }

    @FXML
    public void redirectToCustomize(ActionEvent actionEvent) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/View/staffCustomize.fxml"));
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 1376, 768);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("⚠ Unable to load staffCustomize.fxml");
        }
    }
}
