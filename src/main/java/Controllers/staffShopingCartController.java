package Controllers;

import Model.car;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;

public class staffShopingCartController {

    @FXML private VBox accessoriesContainer;
    @FXML private Label totalPriceLabel;
    @FXML private Label basePriceLabel;
    @FXML private Label summaryBasePriceLabel;
    @FXML private Label accessoriesTotalLabel;
    @FXML private Label modelLabel, colorLabel, engineLabel;
    @FXML private Button backButton;
    @FXML private ScrollPane scrollPane;
    @FXML private ImageView carImageView;

    private ObservableList<String> cartItems = FXCollections.observableArrayList();
    private double basePrice = 0;
    private double accessoriesPrice = 0;

    @FXML
    public void initialize() {
        // Start ScrollPane from top
        if (scrollPane != null) {
            scrollPane.setVvalue(0);

            scrollPane.setOnScroll(event -> {
                double delta = event.getDeltaY() * 0.002;
                double target = Math.min(Math.max(scrollPane.getVvalue() - delta, 0), 1);
                Timeline timeline = new Timeline(
                        new KeyFrame(Duration.millis(300),
                                new KeyValue(scrollPane.vvalueProperty(), target, Interpolator.EASE_BOTH))
                );
                timeline.play();
            });
        }

        backButton.setOnAction(e -> goBack());

        if (carImageView != null) {
            carImageView.setMouseTransparent(true);
            carImageView.setFocusTraversable(false);
        }
    }

    private void goBack() {
        try {
            Stage stage = (Stage) backButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/staffFinalize.fxml"));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void addToCart(car selectedCar, String accessories) {
        String item = selectedCar.getModelid() + " - " + selectedCar.getColor() + " (" + accessories + ")";
        cartItems.add(item);
        basePrice = selectedCar.getCurrent_price();
        updatePriceLabels();
        Label accessoryLabel = new Label(accessories + " - Price here");
        accessoriesContainer.getChildren().add(accessoryLabel);
    }

    public void addAccessory(String name, double price) {
        accessoriesPrice += price;
        Label accessoryLabel = new Label(name + " - Price here");
        accessoriesContainer.getChildren().add(accessoryLabel);
        updatePriceLabels();
    }

    public void removeAccessory(int index) {
        if (index >= 0 && index < accessoriesContainer.getChildren().size()) {
            accessoriesContainer.getChildren().remove(index);
        }
        updatePriceLabels();
    }

    private void updatePriceLabels() {
        basePriceLabel.setText("Price here");
        summaryBasePriceLabel.setText("Price here");
        accessoriesTotalLabel.setText("Price here");
        totalPriceLabel.setText("Price here");
    }

    public double getTotalPrice() {
        return basePrice + accessoriesPrice;
    }

    public List<String> getCartItems() {
        return cartItems;
    }

    public void clearCart() {
        cartItems.clear();
        accessoriesContainer.getChildren().clear();
        basePrice = 0;
        accessoriesPrice = 0;
        updatePriceLabels();
    }
}
