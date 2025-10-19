package Controllers;

import Utils.AppStage;
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
    @FXML private ImageView previewImage;
    @FXML private ImageView selected_model_image;
    @FXML private Button confirm_btn;
    @FXML private Button goback_btn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        basic_price.setText("$100,000");
        colorLabel.setText("Crayon Grey");
        color_price.setText("$4,500");
        wheelLabel.setText("21-inch RS Spyder");
        wheels_price.setText("$4,200");
        interiorLabel.setText("Exclusive Two-Tone");
        interior_price.setText("$7,500");
        handling_price.setText("$200");
        totalPriceLabel.setText("$116,400");

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
        navigate("/View/staffShopingcart.fxml");
    }

    @FXML
    private void goback(ActionEvent event) {
        navigate("/View/staffCustomize.fxml");
    }

    private void navigate(String path) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(path)));
            Scene newScene = new Scene(root, 1300, 850);
            AppStage.getStage().setScene(newScene);
            AppStage.getStage().centerOnScreen();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void setCarData(String color, String wheels, String interior, String totalPrice, String imagePath) {
        if (colorLabel != null) colorLabel.setText(color);
        if (wheelLabel != null) wheelLabel.setText(wheels);
        if (interiorLabel != null) interiorLabel.setText(interior);
        if (totalPriceLabel != null) totalPriceLabel.setText(totalPrice);
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
