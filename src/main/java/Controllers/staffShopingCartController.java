package Controllers;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.text.NumberFormat;
import java.util.Locale;

public class staffShopingCartController {

    @FXML
    private Label carNameLabel;

    @FXML
    private Label carModelLabel;

    @FXML
    private Label basePriceLabel;

    @FXML
    private TableView<Accessory> accessoriesTable;

    @FXML
    private TableColumn<Accessory, String> accessoryNameColumn;

    @FXML
    private TableColumn<Accessory, String> accessoryPriceColumn;

    @FXML
    private Label subtotalLabel;

    @FXML
    private Label taxLabel;

    @FXML
    private Label grandTotalLabel;

    @FXML
    private Button confirmButton;

    @FXML
    private Button backButton;

    private String carName;
    private String carModel;
    private double basePrice;
    private ObservableList<Accessory> accessories;
    private double subtotal;
    private double tax;
    private double grandTotal;

    private static final double TAX_RATE = 0.10;
    private final NumberFormat currencyFormat;

    public staffShopingCartController() {
        this.accessories = FXCollections.observableArrayList();
        this.currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        loadDemoData();
        calculateTotals();
        displayData();
    }

    private void setupTableColumns() {
        accessoryNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName()));

        accessoryPriceColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(currencyFormat.format(cellData.getValue().getPrice())));

        // right-align the price column
        accessoryPriceColumn.setStyle("-fx-alignment: CENTER-RIGHT;");
    }

    private void loadDemoData() {
        // Demo default values — you can replace by calling setCarDetails / setAccessories from outside
        carName = "Mercedes-Benz S-Class";
        carModel = "S 500 4MATIC";
        basePrice = 115000.00;

        accessories.add(new Accessory("Premium Sound System", 2500.00));
        accessories.add(new Accessory("Leather Interior Upgrade", 1800.00));
        accessories.add(new Accessory("Advanced Driver Assistance", 1200.00));
        accessories.add(new Accessory("Panoramic Sunroof", 1500.00));
        accessories.add(new Accessory("Custom Alloy Wheels", 800.00));

        accessoriesTable.setItems(accessories);
    }

    /**
     * Use these setters to inject real data from the caller before showing the scene.
     */
    public void setCarDetails(String name, String model, double price) {
        this.carName = name;
        this.carModel = model;
        this.basePrice = price;
    }

    public void setAccessories(ObservableList<Accessory> accessories) {
        this.accessories = accessories;
        if (accessoriesTable != null) {
            this.accessoriesTable.setItems(accessories);
        }
    }

    private void calculateTotals() {
        double accessoriesTotal = accessories.stream()
                .mapToDouble(Accessory::getPrice)
                .sum();

        subtotal = basePrice + accessoriesTotal;
        tax = subtotal * TAX_RATE;
        grandTotal = subtotal + tax;
    }

    private void displayData() {
        if (carNameLabel != null) carNameLabel.setText(carName);
        if (carModelLabel != null) carModelLabel.setText(carModel);
        if (basePriceLabel != null) basePriceLabel.setText(currencyFormat.format(basePrice));

        if (subtotalLabel != null) subtotalLabel.setText(currencyFormat.format(subtotal));
        if (taxLabel != null) taxLabel.setText(currencyFormat.format(tax));
        if (grandTotalLabel != null) grandTotalLabel.setText(currencyFormat.format(grandTotal));
    }

    @FXML
    private void handleConfirmPurchase() {
        System.out.println("=".repeat(60));
        System.out.println("PURCHASE CONFIRMATION");
        System.out.println("=".repeat(60));
        System.out.println("\nCar Details:");
        System.out.println("  Name: " + carName);
        System.out.println("  Model: " + carModel);
        System.out.println("  Base Price: " + currencyFormat.format(basePrice));

        System.out.println("\nSelected Accessories:");
        for (Accessory accessory : accessories) {
            System.out.println("  - " + accessory.getName() + ": " +
                    currencyFormat.format(accessory.getPrice()));
        }

        System.out.println("\nPrice Summary:");
        System.out.println("  Subtotal: " + currencyFormat.format(subtotal));
        System.out.println("  Tax (10%): " + currencyFormat.format(tax));
        System.out.println("  Grand Total: " + currencyFormat.format(grandTotal));
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Purchase confirmed successfully!");
        System.out.println("=".repeat(60) + "\n");

        showConfirmationDialog();
    }

    @FXML
    private void handleBackToSelection() {
        // Simple behavior: close current window. Replace with navigation logic if you have scenes to switch.
        Stage stage = (Stage) backButton.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }

    private void showConfirmationDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Purchase Confirmed");
        alert.setHeaderText("Thank You for Your Purchase!");
        alert.setContentText("Your " + carName + " has been confirmed.\n" +
                "Total Amount: " + currencyFormat.format(grandTotal) + "\n\n" +
                "A confirmation email will be sent shortly.");

        alert.showAndWait();
    }

    public static class Accessory {
        private final SimpleStringProperty name;
        private final SimpleDoubleProperty price;

        public Accessory(String name, double price) {
            this.name = new SimpleStringProperty(name);
            this.price = new SimpleDoubleProperty(price);
        }

        public String getName() {
            return name.get();
        }

        public void setName(String value) {
            name.set(value);
        }

        public SimpleStringProperty nameProperty() {
            return name;
        }

        public double getPrice() {
            return price.get();
        }

        public void setPrice(double value) {
            price.set(value);
        }

        public SimpleDoubleProperty priceProperty() {
            return price;
        }
    }
}
