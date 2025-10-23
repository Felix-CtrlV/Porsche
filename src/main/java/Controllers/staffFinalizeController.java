package Controllers;

import Model.CarConfiguration;
import Model.CustomizationOption;
import Utils.SessionStaff;
import Utils.SessionStaff;
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

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class staffFinalizeController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(staffFinalizeController.class.getName());

    @FXML private Label modelNameLabel;
    @FXML private Label basic_price;
    @FXML private Label colorLabel;
    @FXML private Label color_price;
    @FXML private Label wheelLabel;
    @FXML private Label wheels_price;
    @FXML private Label interiorLabel;
    @FXML private Label interior_price;
    @FXML private Label totalPriceLabel;
    @FXML private ImageView previewImage;
    @FXML private ImageView selected_model_image;
    @FXML private Button confirm_btn;
    @FXML private Button goback_btn;

    private CarConfiguration carConfig;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        carConfig = SessionStaff.getInstance().getCarConfiguration();

        if (carConfig != null) {
            updateDisplay();
        } else {
            loadDefaultValues();
        }

        loadDefaultImage();
    }

    private void loadDefaultValues() {
        if (basic_price != null) basic_price.setText("$140,000");
        if (colorLabel != null) colorLabel.setText("Carrara White");
        if (color_price != null) color_price.setText("STANDARD");
        if (wheelLabel != null) wheelLabel.setText("18-inch Standard Wheels");
        if (wheels_price != null) wheels_price.setText("STANDARD");
        if (interiorLabel != null) interiorLabel.setText("Standard Black Leather");
        if (interior_price != null) interior_price.setText("STANDARD");
        if (totalPriceLabel != null) totalPriceLabel.setText("$140,000");
    }

    private void loadDefaultImage() {
        try {
            Image img = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Image/911_select_model.png")));
            if (selected_model_image != null) selected_model_image.setImage(img);
            if (previewImage != null) previewImage.setImage(img);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load default image", e);
        }
    }

    public void setCarConfiguration(CarConfiguration config) {
        this.carConfig = config;
        SessionStaff.getInstance().setCarConfiguration(config);
        updateDisplay();
    }

    private void updateDisplay() {
        if (carConfig == null) return;

        if (modelNameLabel != null) {
            modelNameLabel.setText(carConfig.getModelName());
        }

        if (basic_price != null) {
            basic_price.setText(String.format("$%,d", (int) carConfig.getBasePrice()));
        }

        CustomizationOption wheel = carConfig.getSelectedWheel();
        if (wheel != null && wheelLabel != null && wheels_price != null) {
            wheelLabel.setText(wheel.getName());
            if (wheel.isStandard()) {
                wheels_price.setText("STANDARD");
                wheels_price.setStyle("-fx-text-fill: #757575;");
            } else {
                wheels_price.setText(String.format("+ $%,d", (int) wheel.getPrice()));
                wheels_price.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: 600;");
            }
        }

        CustomizationOption color = carConfig.getSelectedColor();
        if (color != null && colorLabel != null && color_price != null) {
            colorLabel.setText(color.getName());
            if (color.isStandard()) {
                color_price.setText("STANDARD");
                color_price.setStyle("-fx-text-fill: #757575;");
            } else {
                color_price.setText(String.format("+ $%,d", (int) color.getPrice()));
                color_price.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: 600;");
            }
        }

        CustomizationOption interior = carConfig.getSelectedInterior();
        if (interior != null && interiorLabel != null && interior_price != null) {
            interiorLabel.setText(interior.getName());
            if (interior.isStandard()) {
                interior_price.setText("STANDARD");
                interior_price.setStyle("-fx-text-fill: #757575;");
            } else {
                interior_price.setText(String.format("+ $%,d", (int) interior.getPrice()));
                interior_price.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: 600;");
            }
        }

        if (totalPriceLabel != null) {
            totalPriceLabel.setText(carConfig.getFormattedTotalPrice());
            totalPriceLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: 700; -fx-text-fill: #212121;");
        }

        String imagePath = carConfig.getModelImagePath();
        if (imagePath != null) {
            try {
                Image img = new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath)));
                if (selected_model_image != null) selected_model_image.setImage(img);
                if (previewImage != null) previewImage.setImage(img);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to load car image: " + imagePath, e);
            }
        }
    }

    @FXML
    private void confirmOrder(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/staffShopingcart.fxml"));
            Parent root = loader.load();

            staffShopingCartController cartController = loader.getController();
            cartController.loadFromSession();

            Scene newScene = new Scene(root, 1300, 850);
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(newScene);
            stage.centerOnScreen();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to navigate to shopping cart", ex);
        }
    }

    @FXML
    private void goback(ActionEvent event) {
        navigate(event, "/View/staffCustomize.fxml");
    }

    private void navigate(ActionEvent event, String path) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(path)));
            Scene newScene = new Scene(root, 1300, 850);
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(newScene);
            stage.centerOnScreen();
        } catch (IOException | NullPointerException ex) {
            LOGGER.log(Level.SEVERE, "Failed to navigate to: " + path, ex);
        }
    }
}