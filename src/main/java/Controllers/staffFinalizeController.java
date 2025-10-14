package Controllers;

import Model.car;
import javafx.animation.FillTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Objects;

public class staffFinalizeController {

    @FXML private ImageView selected_model_image;
    @FXML private Button goback_btn;
    @FXML private Button confirm_btn;
    @FXML private Button darkmodebtn;

    @FXML private Label basic_price;
    @FXML private Label color_opt;
    @FXML private Label color_price;
    @FXML private Label wheels_opt;
    @FXML private Label wheels_price;
    @FXML private Label interior_opt;
    @FXML private Label interior_price;
    @FXML private Label handling_price;
    @FXML private Label total_price;
    @FXML private Label car_features;

    @FXML private Line line1;
    @FXML private Line line2;
    @FXML private Line line3;
    @FXML private Line line4;
    @FXML private Line line5;
    @FXML private Line line6;
    @FXML private Line line7;
    @FXML private Line line8;

    private boolean darkMode = false;

    private final Color LIGHT_LINE_COLOR = Color.rgb(0, 0, 0, 0.5);
    private final Color DARK_LINE_COLOR = Color.rgb(255, 255, 255, 0.3);

    @FXML
    void goback(ActionEvent event) {
        switchScene(event, "/view/staffCustomize.fxml");
    }

    @FXML
    void confirmOrder(ActionEvent event) {
        switchScene(event, "/view/staffShopingcart.fxml");
    }

    private void switchScene(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void modechange(ActionEvent event) {
        if (darkmodebtn == null || darkmodebtn.getScene() == null) return;
        Node root = darkmodebtn.getScene().getRoot();
        Line[] lines = { line1, line2, line3, line4, line5, line6, line7, line8 };

        if (!darkMode) {
            if (!root.getStyleClass().contains("dark-mode")) root.getStyleClass().add("dark-mode");
            for (Line line : lines) {
                if (line != null) animateLineColor(line, (Color) line.getStroke(), DARK_LINE_COLOR);
            }
        } else {
            root.getStyleClass().remove("dark-mode");
            for (Line line : lines) {
                if (line != null) animateLineColor(line, (Color) line.getStroke(), LIGHT_LINE_COLOR);
            }
        }
        darkMode = !darkMode;
    }

    private void animateLineColor(Line line, Color fromColor, Color toColor) {
        if (line == null) return;
        FillTransition transition = new FillTransition(Duration.millis(400), line, fromColor, toColor);
        transition.setCycleCount(1);
        transition.setAutoReverse(false);
        transition.play();
    }

    @FXML
    private void initialize() {
        try {
            Image placeholderImage = new Image(
                    Objects.requireNonNull(getClass().getResourceAsStream("/Image/911_select_model.png"))
            );
            if (selected_model_image != null) selected_model_image.setImage(placeholderImage);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        initializeConfigurationData();
        if (confirm_btn != null) confirm_btn.setOnAction(this::confirmOrder);
    }

    private void initializeConfigurationData() {
        if (basic_price != null) basic_price.setText("$150,000");
        if (color_opt != null) color_opt.setText("Snow White");
        if (color_price != null) color_price.setText("$2,500");
        if (wheels_opt != null) wheels_opt.setText("20-inch (fr) and 21-inch (rr) Rose Ceramic Spoke");
        if (wheels_price != null) wheels_price.setText("$3,800");
        if (interior_opt != null) interior_opt.setText("Black leather with GT-silver lining");
        if (interior_price != null) interior_price.setText("$5,200");
        if (handling_price != null) handling_price.setText("$1,200");
        if (total_price != null) total_price.setText("$162,700");
        if (car_features != null) car_features.setText(
                "• High-revving 4.0-litre naturally aspirated flat-six\n" +
                        "• Active rear wing with Drag Reduction System\n" +
                        "• 7-speed Porsche Doppelkupplung (PDK) dual-clutch transmission\n" +
                        "• Fuel Consumption (WLTP): Combined ~13.2 L/100 km; CO₂ ~299 g/km"
        );
    }

    public void setVehicleImage(String imagePath) {
        if (imagePath == null || selected_model_image == null) return;
        try {
            Image newImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath)));
            selected_model_image.setImage(newImage);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void setConfigurationData(
            String basePrice, String colorOption, String colorPriceValue,
            String wheelsOption, String wheelsPriceValue,
            String interiorOption, String interiorPriceValue,
            String handlingCost, String totalMSRP
    ) {
        if (basic_price != null) basic_price.setText(basePrice);
        if (color_opt != null) color_opt.setText(colorOption);
        if (color_price != null) color_price.setText(colorPriceValue);
        if (wheels_opt != null) wheels_opt.setText(wheelsOption);
        if (wheels_price != null) wheels_price.setText(wheelsPriceValue);
        if (interior_opt != null) interior_opt.setText(interiorOption);
        if (interior_price != null) interior_price.setText(interiorPriceValue);
        if (handling_price != null) handling_price.setText(handlingCost);
        if (total_price != null) total_price.setText(totalMSRP);
    }

    public void setVehicleDetails(String modelName, String features) {
        if (car_features != null) car_features.setText(features);
    }

    public void setFinalCarDetails(String modelName, String price, String imagePath) {
        if (imagePath != null) setVehicleImage(imagePath);
        if (price != null) {
            if (basic_price != null) basic_price.setText(price);
            if (total_price != null) total_price.setText(price);
        }
        if (modelName != null && car_features != null) {
            car_features.setText("Selected Model: " + modelName + "\n\nCustomize options shown below reflect your choices.");
        }
    }
}
