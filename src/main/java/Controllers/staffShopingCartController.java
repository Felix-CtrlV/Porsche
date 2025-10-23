package Controllers;

import Model.CarConfiguration;
import Model.CustomizationOption;
import Utils.SessionStaff;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class staffShopingCartController {

    @FXML private ScrollPane scrollPane;
    @FXML private VBox accessoriesContainer;
    @FXML private Label totalPriceLabel;
    @FXML private Label basePriceLabel;
    @FXML private Label summaryBasePriceLabel;
    @FXML private Label accessoriesTotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label modelLabel, colorLabel, engineLabel;
    @FXML private Button backButton;
    @FXML private Button confirmButton;
    @FXML private Button loadAssetButton;
    @FXML private Label confirmationMessageLabel;
    @FXML private VBox configDetailsContainer;

    @FXML private ToggleGroup paymentMethodGroup;
    @FXML private RadioButton fullPaymentRadio;
    @FXML private RadioButton installmentRadio;

    @FXML private VBox installmentOptionsContainer;
    @FXML private ComboBox<String> installmentPlanCombo;
    @FXML private Label downPaymentLabel;
    @FXML private Label monthlyPaymentLabel;
    @FXML private Label monthsLabel;
    @FXML private Label aprLabel;
    @FXML private Label totalInterestLabel;
    @FXML private Label totalWithInterestLabel;

    private ObservableList<String> cartItems = FXCollections.observableArrayList();
    private double basePrice = 0;
    private double accessoriesPrice = 0;
    private double taxRate = 0.08;
    private DecimalFormat currencyFormat = new DecimalFormat("$#,##0");

    private List<AccessoryItemDisplay> displayedAccessories = new ArrayList<>();

    private static class AccessoryItemDisplay {
        String id;
        String name;
        double price;
        boolean isCarOption;

        AccessoryItemDisplay(String id, String name, double price, boolean isCarOption) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.isCarOption = isCarOption;
        }
    }

    private static class InstallmentPlan {
        int months;
        double apr;
        double downPayment;
        InstallmentPlan(int months, double apr, double downPayment) {
            this.months = months;
            this.apr = apr;
            this.downPayment = downPayment;
        }
        @Override
        public String toString() {
            return months + " Months (" + apr + "% APR)";
        }
    }

    private List<InstallmentPlan> installmentPlans = new ArrayList<>();
    private InstallmentPlan selectedPlan;

    @FXML
    public void initialize() {
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

        initializeInstallmentPlans();
        setupPaymentMethodListeners();

        backButton.setOnAction(e -> goBack());
        confirmButton.setOnAction(e -> confirmPurchase());
        loadAssetButton.setOnAction(e -> loadStaffAsset());

        loadFromSession();
    }

    public void loadFromSession() {
        SessionStaff session = SessionStaff.getInstance();
        CarConfiguration carConfig = session.getCarConfiguration();

        accessoriesContainer.getChildren().clear();
        displayedAccessories.clear();
        accessoriesPrice = 0;
        basePrice = 0;

        if (carConfig != null) {
            basePrice = carConfig.getBasePrice();

            CustomizationOption wheel = carConfig.getSelectedWheel();
            if (wheel != null && !wheel.isStandard()) {
                displayedAccessories.add(new AccessoryItemDisplay("wheel", wheel.getName(), wheel.getPrice(), true));
                accessoriesPrice += wheel.getPrice();
            }

            CustomizationOption color = carConfig.getSelectedColor();
            if (color != null && !color.isStandard()) {
                displayedAccessories.add(new AccessoryItemDisplay("color", color.getName(), color.getPrice(), true));
                accessoriesPrice += color.getPrice();
            }

            CustomizationOption interior = carConfig.getSelectedInterior();
            if (interior != null && !interior.isStandard()) {
                displayedAccessories.add(new AccessoryItemDisplay("interior", interior.getName(), interior.getPrice(), true));
                accessoriesPrice += interior.getPrice();
            }

            if (modelLabel != null) modelLabel.setText(carConfig.getModelName());
            if (colorLabel != null && color != null) colorLabel.setText(color.getName());
            if (engineLabel != null) engineLabel.setText(carConfig.getFuelType());

            addRemoveCarButton();
        } else {
            if (modelLabel != null) modelLabel.setText("Accessories Only");
            if (colorLabel != null) colorLabel.setText("N/A");
            if (engineLabel != null) engineLabel.setText("N/A");
            removeRemoveCarButton();
        }

        for (SessionStaff.AccessoryItem item : session.getAccessories().values()) {
            String accessoryId = findAccessoryIdByName(item.name);
            displayedAccessories.add(new AccessoryItemDisplay(accessoryId, item.name, item.price * item.quantity, false));
            accessoriesPrice += item.price * item.quantity;
        }

        displayAllAccessories();
        updatePriceLabels();
    }

    private void addRemoveCarButton() {
        if (configDetailsContainer == null) return;

        boolean hasRemoveButton = configDetailsContainer.getChildren().stream()
                .anyMatch(node -> node instanceof HBox &&
                        ((HBox) node).getChildren().stream()
                                .anyMatch(child -> child instanceof Button &&
                                        ((Button) child).getStyleClass().contains("remove-car-button")));

        if (!hasRemoveButton) {
            HBox removeCarRow = new HBox(15);
            removeCarRow.setAlignment(Pos.CENTER_RIGHT);
            removeCarRow.setPadding(new Insets(10, 0, 0, 0));
            removeCarRow.getStyleClass().add("remove-car-row");

            Button removeCarButton = new Button("✕ Remove Car");
            removeCarButton.getStyleClass().add("remove-car-button");
            removeCarButton.setOnAction(e -> removeCar());

            removeCarRow.getChildren().add(removeCarButton);
            configDetailsContainer.getChildren().add(removeCarRow);
        }
    }

    private void removeRemoveCarButton() {
        if (configDetailsContainer == null) return;

        configDetailsContainer.getChildren().removeIf(node ->
                node instanceof HBox && ((HBox) node).getStyleClass().contains("remove-car-row")
        );
    }

    private void removeCar() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Remove Car");
        alert.setHeaderText("Are you sure you want to remove the car?");
        alert.setContentText("This will remove the car and all its customizations from your cart.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            SessionStaff.getInstance().setCarConfiguration(null);
            loadFromSession();
        }
    }

    private String findAccessoryIdByName(String name) {
        for (SessionStaff.AccessoryItem item : SessionStaff.getInstance().getAccessories().values()) {
            if (item.name.equals(name)) return name;
        }
        return name;
    }

    private void displayAllAccessories() {
        accessoriesContainer.getChildren().clear();

        for (AccessoryItemDisplay item : displayedAccessories) {
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(8, 0, 8, 0));

            Label nameLabel = new Label(item.name);
            nameLabel.getStyleClass().add("detail-value");
            nameLabel.setMinWidth(400);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label priceLabel = new Label(currencyFormat.format(item.price));
            priceLabel.getStyleClass().add("detail-value");
            priceLabel.getStyleClass().add("price-highlight");
            priceLabel.setMinWidth(100);

            Button removeButton = new Button("✕");
            removeButton.getStyleClass().add("remove-button");
            removeButton.setOnAction(e -> removeAccessory(item));

            row.getChildren().addAll(nameLabel, spacer, priceLabel, removeButton);
            accessoriesContainer.getChildren().add(row);
        }
    }

    private void removeAccessory(AccessoryItemDisplay item) {
        if (item.isCarOption) {
            CarConfiguration carConfig = SessionStaff.getInstance().getCarConfiguration();
            if (carConfig != null) {
                switch (item.id) {
                    case "wheel" -> carConfig.setSelectedWheel(null);
                    case "color" -> carConfig.setSelectedColor(null);
                    case "interior" -> carConfig.setSelectedInterior(null);
                }
                SessionStaff.getInstance().setCarConfiguration(carConfig);
            }
        } else {
            SessionStaff.getInstance().removeAccessory(item.id);
        }

        loadFromSession();
    }

    private void initializeInstallmentPlans() {
        installmentPlans.add(new InstallmentPlan(12, 2.9, 25000));
        installmentPlans.add(new InstallmentPlan(24, 3.5, 25000));
        installmentPlans.add(new InstallmentPlan(36, 4.2, 25000));
        installmentPlans.add(new InstallmentPlan(48, 4.9, 25000));

        ObservableList<String> planNames = FXCollections.observableArrayList();
        for (InstallmentPlan plan : installmentPlans) planNames.add(plan.toString());
        installmentPlanCombo.setItems(planNames);
        installmentPlanCombo.getSelectionModel().select(1);
        selectedPlan = installmentPlans.get(1);

        installmentPlanCombo.setOnAction(e -> {
            int index = installmentPlanCombo.getSelectionModel().getSelectedIndex();
            if (index >= 0) {
                selectedPlan = installmentPlans.get(index);
                updateInstallmentDetails();
            }
        });

        updateInstallmentDetails();
    }

    private void setupPaymentMethodListeners() {
        paymentMethodGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            boolean isInstallment = newVal == installmentRadio;
            installmentOptionsContainer.setVisible(isInstallment);
            installmentOptionsContainer.setManaged(isInstallment);
        });
    }

    private void updateInstallmentDetails() {
        if (selectedPlan == null) return;
        double total = calculateGrandTotal();

        if (total < selectedPlan.downPayment) {
            downPaymentLabel.setText(currencyFormat.format(total));
            monthlyPaymentLabel.setText("$0");
            monthsLabel.setText("N/A");
            aprLabel.setText("N/A");
            totalInterestLabel.setText("$0");
            totalWithInterestLabel.setText(currencyFormat.format(total));
            return;
        }

        double amountToFinance = total - selectedPlan.downPayment;
        double monthlyRate = (selectedPlan.apr / 100) / 12;
        double monthlyPayment = amountToFinance *
                (monthlyRate * Math.pow(1 + monthlyRate, selectedPlan.months)) /
                (Math.pow(1 + monthlyRate, selectedPlan.months) - 1);
        double totalPayments = monthlyPayment * selectedPlan.months;
        double totalInterest = totalPayments - amountToFinance;
        double totalWithInterest = total + totalInterest;

        downPaymentLabel.setText(currencyFormat.format(selectedPlan.downPayment));
        monthlyPaymentLabel.setText(currencyFormat.format(monthlyPayment));
        monthsLabel.setText(selectedPlan.months + " months");
        aprLabel.setText(selectedPlan.apr + "%");
        totalInterestLabel.setText(currencyFormat.format(totalInterest));
        totalWithInterestLabel.setText(currencyFormat.format(totalWithInterest));
    }

    private void goBack() {
        CarConfiguration carConfig = SessionStaff.getInstance().getCarConfiguration();
        if (carConfig != null) navigate("/View/staffFinalize.fxml");
        else navigate("/View/staffAsset.fxml");
    }

    private void loadStaffAsset() {
        navigate("/View/staffAsset.fxml");
    }

    private void navigate(String path) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Scene scene = new Scene(loader.load(), 1300, 850);
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void confirmPurchase() {
        double total = calculateGrandTotal();

        if (total <= 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Empty Cart");
            alert.setHeaderText("Cannot confirm purchase");
            alert.setContentText("Your cart is empty. Add some items first!");
            alert.showAndWait();
            return;
        }

        String paymentMethod = installmentRadio.isSelected() ? "Installment" : "Full Payment";
        String message = "Purchase Confirmed!\n\nPayment Method: " + paymentMethod;

        CarConfiguration carConfig = SessionStaff.getInstance().getCarConfiguration();
        if (carConfig != null) message += "\nCar: " + carConfig.getModelName();
        else message += "\nAccessories Only Purchase";

        if (installmentRadio.isSelected() && selectedPlan != null && total >= selectedPlan.downPayment) {
            double amountToFinance = total - selectedPlan.downPayment;
            double monthlyRate = (selectedPlan.apr / 100) / 12;
            double monthlyPayment = amountToFinance *
                    (monthlyRate * Math.pow(1 + monthlyRate, selectedPlan.months)) /
                    (Math.pow(1 + monthlyRate, selectedPlan.months) - 1);

            message += "\nPlan: " + selectedPlan.months + " months at " + selectedPlan.apr + "% APR";
            message += "\nMonthly Payment: " + currencyFormat.format(monthlyPayment);
        }

        message += "\n\nTotal: " + currencyFormat.format(total);

        confirmationMessageLabel.setText(message);
        confirmationMessageLabel.setVisible(true);
        confirmationMessageLabel.setManaged(true);
        confirmButton.setDisable(true);
    }

    private double calculateSubtotal() {
        return basePrice + accessoriesPrice;
    }

    private double calculateTax() {
        return calculateSubtotal() * taxRate;
    }

    private double calculateGrandTotal() {
        return calculateSubtotal() + calculateTax();
    }

    private void updatePriceLabels() {
        basePriceLabel.setText(currencyFormat.format(basePrice));
        summaryBasePriceLabel.setText(currencyFormat.format(basePrice));
        accessoriesTotalLabel.setText(currencyFormat.format(accessoriesPrice));

        double tax = calculateTax();
        double total = calculateGrandTotal();

        taxLabel.setText(currencyFormat.format(tax));
        totalPriceLabel.setText(currencyFormat.format(total));

        if (installmentRadio != null && installmentRadio.isSelected()) {
            updateInstallmentDetails();
        }
    }

    public double getTotalPrice() {
        return calculateGrandTotal();
    }

    public List<String> getCartItems() {
        return cartItems;
    }

    public void clearCart() {
        cartItems.clear();
        accessoriesContainer.getChildren().clear();
        displayedAccessories.clear();
        basePrice = 0;
        accessoriesPrice = 0;
        SessionStaff.getInstance().clearAccessories();
        SessionStaff.getInstance().setCarConfiguration(null);
        updatePriceLabels();
    }
}
