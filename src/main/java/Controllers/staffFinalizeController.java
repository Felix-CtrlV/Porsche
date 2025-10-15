package Controllers;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class staffFinalizeController implements Initializable {

    @FXML private Label basic_price;
    @FXML private Label colorLabel;
    @FXML private Label color_price;
    @FXML private Label wheelLabel;
    @FXML private Label wheels_price;
    @FXML private Label interiorLabel;
    @FXML private Label interior_price;
    @FXML private Label handling_price;
    @FXML private Label totalPriceLabel;

    @FXML private ImageView previewImage; // make sure this exists in FXML
    @FXML private ImageView selected_model_image;

    @FXML private Button confirm_btn;
    @FXML private Button goback_btn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Default example values
        basic_price.setText("$100,000");
        colorLabel.setText("Crayon Grey");
        color_price.setText("$4,500");
        wheelLabel.setText("21-inch RS Spyder");
        wheels_price.setText("$4,200");
        interiorLabel.setText("Exclusive Two-Tone");
        interior_price.setText("$7,500");
        handling_price.setText("$200");
        totalPriceLabel.setText("$116,400");

        // Default image example
        try {
            Image img = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Image/911_select_model.png")));
            selected_model_image.setImage(img);
            if (previewImage != null) previewImage.setImage(img);
        } catch (Exception e) {
            System.err.println("Image not found: /Image/911_select_model.png");
        }
    }

    @FXML
    private void confirmOrder(ActionEvent event) {
        System.out.println("✅ Finalized configuration saved!");
        navigate(event, "/View/staffShopingcart.fxml");
    }

    @FXML
    private void goback(ActionEvent event) {
        navigate(event, "/View/staffCustomize.fxml");
    }

    private void navigate(ActionEvent event, String path) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(path)));
            Stage stage = (Stage)((Button)event.getSource()).getScene().getWindow();

            root.setOpacity(0);
            Scene newScene = new Scene(root, 1376, 768);
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), stage.getScene().getRoot());
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                stage.setScene(newScene);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            });
            fadeOut.play();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Dynamically set the car configuration info.
     */
    public void setCarData(String color, String wheels, String interior, String totalPrice, String imagePath) {
        if (colorLabel != null) colorLabel.setText(color);
        if (wheelLabel != null) wheelLabel.setText(wheels);
        if (interiorLabel != null) interiorLabel.setText(interior);
        if (totalPriceLabel != null) totalPriceLabel.setText(totalPrice);

        // Optional: update image
        if (imagePath != null && selected_model_image != null) {
            try {
                Image img = new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath)));
                selected_model_image.setImage(img);
                if (previewImage != null) previewImage.setImage(img);
            } catch (Exception e) {
                System.err.println("Image not found: " + imagePath);
            }
        }
    }
}
