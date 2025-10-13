package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import porsche.model.Accessory;
import porsche.model.CarConfiguration;

import java.text.NumberFormat;
import java.util.Locale;

public class staffShopingCartController {

    @FXML
    private ImageView carImageView;

    @FXML
    private Label modelLabel;

    @FXML
    private Label colorLabel;

    @FXML
    private Label engineLabel;

    @FXML
    private Label basePriceLabel;

    @FXML
    private VBox accessoriesContainer;

    @FXML
    private Label summaryBasePriceLabel;

    @FXML
    private Label accessoriesTotalLabel;

    @FXML
    private Label totalPriceLabel;

    @FXML
    private Button confirmButton;

    @FXML
    private Label confirmationMessageLabel;

    private CarConfiguration carConfiguration;
    private NumberFormat currencyFormatter;

    public CheckoutController() {
        currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
    }

    @FXML
    public void initialize() {
        loadSampleData();
    }

    private void loadSampleData() {
        carConfiguration = new CarConfiguration(
                "911 Carrera S",
                "Guards Red",
                "3.0L Twin-Turbo Flat-6",
                115000.00,
                "https://images.pexels.com/photos/544542/pexels-photo-544542.jpeg"
        );

        carConfiguration.addAccessory(new Accessory("Sport Chrono Package", 3590.00));
        carConfiguration.addAccessory(new Accessory("Premium Sound System", 4970.00));
        carConfiguration.addAccessory(new Accessory("Adaptive Sport Seats Plus", 2990.00));
        carConfiguration.addAccessory(new Accessory("Carbon Fiber Interior Package", 5850.00));
        carConfiguration.addAccessory(new Accessory("LED Matrix Headlights", 2100.00));

        updateUI();
    }

    public void setCarConfiguration(CarConfiguration config) {
        this.carConfiguration = config;
        updateUI();
    }

    private void updateUI() {
        if (carConfiguration == null) {
            return;
        }

        modelLabel.setText(carConfiguration.getModel());
        colorLabel.setText(carConfiguration.getColor());
        engineLabel.setText(carConfiguration.getEngineType());
        basePriceLabel.setText(formatCurrency(carConfiguration.getBasePrice()));
        summaryBasePriceLabel.setText(formatCurrency(carConfiguration.getBasePrice()));
        accessoriesTotalLabel.setText(formatCurrency(carConfiguration.getAccessoriesTotalPrice()));
        totalPriceLabel.setText(formatCurrency(carConfiguration.getTotalPrice()));

        if (carConfiguration.getImageUrl() != null && !carConfiguration.getImageUrl().isEmpty()) {
            try {
                Image image = new Image(carConfiguration.getImageUrl(), true);
                carImageView.setImage(image);
            } catch (Exception e) {
                System.err.println("Error loading image: " + e.getMessage());
            }
        }

        accessoriesContainer.getChildren().clear();
        for (Accessory accessory : carConfiguration.getAccessories()) {
            HBox accessoryRow = createAccessoryRow(accessory);
            accessoriesContainer.getChildren().add(accessoryRow);
        }
    }

    private HBox createAccessoryRow(Accessory accessory) {
        HBox row = new HBox(15);
        row.getStyleClass().add("accessory-row");

        Label nameLabel = new Label(accessory.getName());
        nameLabel.getStyleClass().add("accessory-name");
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(nameLabel, javafx.scene.layout.Priority.ALWAYS);

        Label priceLabel = new Label(formatCurrency(accessory.getPrice()));
        priceLabel.getStyleClass().add("accessory-price");

        row.getChildren().addAll(nameLabel, priceLabel);
        return row;
    }

    @FXML
    private void handleConfirmPurchase() {
        confirmButton.setDisable(true);
        confirmationMessageLabel.setText("Thank you for your purchase! Your Porsche will be delivered soon.");
        confirmationMessageLabel.setVisible(true);

        System.out.println("Purchase Confirmed!");
        System.out.println("Model: " + carConfiguration.getModel());
        System.out.println("Total: " + formatCurrency(carConfiguration.getTotalPrice()));
    }

    private String formatCurrency(double amount) {
        return currencyFormatter.format(amount);
    }
}
