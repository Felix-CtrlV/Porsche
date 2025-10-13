package Controllers;

import javafx.animation.FillTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

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
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/staffCustomize.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void confirmOrder(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/staffShopingcart.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void modechange(ActionEvent event) {
        Node root = darkmodebtn.getScene().getRoot();
        Line[] lines = {line1, line2, line3, line4, line5, line6, line7, line8};

        if (!darkMode) {
            root.getStyleClass().add("dark-mode");

            for (Line line : lines) {
                animateLineColor(line, (Color) line.getStroke(), DARK_LINE_COLOR);
            }
            darkMode = true;
        } else {
            root.getStyleClass().remove("dark-mode");

            for (Line line : lines) {
                animateLineColor(line, (Color) line.getStroke(), LIGHT_LINE_COLOR);
            }
            darkMode = false;
        }
    }

    private void animateLineColor(Line line, Color fromColor, Color toColor) {
        FillTransition transition = new FillTransition(Duration.millis(400), line, fromColor, toColor);
        transition.setCycleCount(1);
        transition.setAutoReverse(false);
        transition.play();
    }

    @FXML
    private void initialize() {
        try {
            selected_model_image.setImage(
                    new Image(getClass().getResourceAsStream("/Image/911_select_model.png"))
            );
        } catch (Exception e) {
            System.err.println("Error loading vehicle image: " + e.getMessage());
        }

        initializeConfigurationData();

        confirm_btn.setOnAction(this::confirmOrder);
    }

    private void initializeConfigurationData() {
        basic_price.setText("$150,000");
        color_opt.setText("Snow White");
        color_price.setText("$2,500");
        wheels_opt.setText("20-inch(fr) and 21-inch(rr) Rose Ceramic Spoke");
        wheels_price.setText("$3,800");
        interior_opt.setText("Black leather with GT-silver lining");
        interior_price.setText("$5,200");
        handling_price.setText("$1,200");
        total_price.setText("$162,700");

        car_features.setText(
                "• High-revving 4.0-litre naturally aspirated flat-six\n" +
                        "• Features front diffuser and active rear wing with Drag Reduction System\n" +
                        "• 7-speed Porsche Doppelkupplung (PDK) dual-clutch transmission\n" +
                        "• Fuel Consumption (WLTP): Combined ~13.2 l/100 km; emissions ~299 g CO₂/km"
        );
    }

    public void setVehicleImage(String imagePath) {
        try {
            selected_model_image.setImage(
                    new Image(getClass().getResourceAsStream(imagePath))
            );
        } catch (Exception e) {
            System.err.println("Error loading custom vehicle image: " + e.getMessage());
        }
    }

    public void setConfigurationData(String basePrice, String colorOption, String colorPriceValue,
                                     String wheelsOption, String wheelsPriceValue,
                                     String interiorOption, String interiorPriceValue,
                                     String handlingCost, String totalMSRP) {
        basic_price.setText(basePrice);
        color_opt.setText(colorOption);
        color_price.setText(colorPriceValue);
        wheels_opt.setText(wheelsOption);
        wheels_price.setText(wheelsPriceValue);
        interior_opt.setText(interiorOption);
        interior_price.setText(interiorPriceValue);
        handling_price.setText(handlingCost);
        total_price.setText(totalMSRP);
    }

    public void setVehicleDetails(String modelName, String features) {
        car_features.setText(features);
    }
}
