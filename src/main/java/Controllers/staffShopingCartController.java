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
import javafx.scene.Node;
import javafx.scene.Parent;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

        if (backButton != null) backButton.setOnAction(e -> goBack());
        if (confirmButton != null) confirmButton.setOnAction(e -> confirmPurchase());
        if (loadAssetButton != null) loadAssetButton.setOnAction(e -> loadStaffAsset());

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
            // CHANGED: Show "No car model has been selected" instead of "Accessories Only"
            if (modelLabel != null) modelLabel.setText("No car model has been selected");
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
        double grandTotal = selectedPlan.downPayment + totalPayments;

        downPaymentLabel.setText(currencyFormat.format(selectedPlan.downPayment));
        monthlyPaymentLabel.setText(currencyFormat.format(monthlyPayment));
        monthsLabel.setText(String.valueOf(selectedPlan.months));
        aprLabel.setText(selectedPlan.apr + "%");
        totalInterestLabel.setText(currencyFormat.format(totalInterest));
        totalWithInterestLabel.setText(currencyFormat.format(grandTotal));
    }

    private void updatePriceLabels() {
        double subtotal = basePrice + accessoriesPrice;
        double tax = subtotal * taxRate;
        double total = subtotal + tax;

        if (basePriceLabel != null) basePriceLabel.setText(currencyFormat.format(basePrice));
        if (summaryBasePriceLabel != null) summaryBasePriceLabel.setText(currencyFormat.format(basePrice));
        if (accessoriesTotalLabel != null) accessoriesTotalLabel.setText(currencyFormat.format(accessoriesPrice));
        if (taxLabel != null) taxLabel.setText(currencyFormat.format(tax));
        if (totalPriceLabel != null) totalPriceLabel.setText(currencyFormat.format(total));

        updateInstallmentDetails();
    }

    private double calculateGrandTotal() {
        double subtotal = basePrice + accessoriesPrice;
        return subtotal + (subtotal * taxRate);
    }

    private void goBack() {
        navigateTo("/View/staffFinalize.fxml");
    }

    private void loadStaffAsset() {
        navigateTo("/View/staffAsset.fxml");
    }

    private void confirmPurchase() {
        // Create custom dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Order Confirmed");
        dialog.setHeaderText("Thank you for your purchase!");

        // Create content
        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));

        Label successMessage = new Label("✓ Your order has been successfully placed!");
        successMessage.setStyle("-fx-font-size: 16px; -fx-text-fill: #4CAF50; -fx-font-weight: bold;");

        Label orderDetails = new Label("Order Total: " + totalPriceLabel.getText());
        orderDetails.setStyle("-fx-font-size: 14px;");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Label orderDate = new Label("Order Date: " + dateFormat.format(new Date()));
        orderDate.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        content.getChildren().addAll(successMessage, orderDetails, orderDate);
        dialog.getDialogPane().setContent(content);

        // Create custom buttons
        ButtonType returnHomeButton = new ButtonType("Return to Home", ButtonBar.ButtonData.OK_DONE);
        ButtonType printReceiptButton = new ButtonType("Print Receipt", ButtonBar.ButtonData.OTHER);

        dialog.getDialogPane().getButtonTypes().addAll(returnHomeButton, printReceiptButton);

        // Style the dialog
        dialog.getDialogPane().setStyle("-fx-background-color: white; -fx-border-color: #D5001C; -fx-border-width: 2px;");

        // Show dialog and handle button clicks
        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent()) {
            if (result.get() == returnHomeButton) {
                // Clear cart and go to home
                SessionStaff.getInstance().clearCart();
                navigateTo("/View/staffWelcome.fxml");
            } else if (result.get() == printReceiptButton) {
                // Print receipt then clear cart and go home
                printReceipt();
                SessionStaff.getInstance().clearCart();
                navigateTo("/View/staffWelcome.fxml");
            }
        }
    }

    private void printReceipt() {
        StringBuilder receipt = new StringBuilder();
        receipt.append("═══════════════════════════════════════════════════\n");
        receipt.append("                 PORSCHE RECEIPT\n");
        receipt.append("═══════════════════════════════════════════════════\n\n");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        receipt.append("Date: ").append(dateFormat.format(new Date())).append("\n");
        receipt.append("Staff: ").append(SessionStaff.getInstance().getUsername()).append("\n\n");

        receipt.append("───────────────────────────────────────────────────\n");
        receipt.append("ORDER DETAILS\n");
        receipt.append("───────────────────────────────────────────────────\n\n");

        CarConfiguration carConfig = SessionStaff.getInstance().getCarConfiguration();
        if (carConfig != null) {
            receipt.append("Vehicle:\n");
            receipt.append("  Model: ").append(carConfig.getModelName()).append("\n");
            receipt.append("  Base Price: ").append(currencyFormat.format(carConfig.getBasePrice())).append("\n\n");

            if (!displayedAccessories.isEmpty()) {
                receipt.append("Customizations:\n");
                for (AccessoryItemDisplay item : displayedAccessories) {
                    if (item.isCarOption) {
                        receipt.append("  - ").append(item.name)
                                .append(": ").append(currencyFormat.format(item.price)).append("\n");
                    }
                }
                receipt.append("\n");
            }
        } else {
            receipt.append("Vehicle: No car model selected\n\n");
        }

        if (SessionStaff.getInstance().getAccessories().size() > 0) {
            receipt.append("Accessories:\n");
            for (SessionStaff.AccessoryItem item : SessionStaff.getInstance().getAccessories().values()) {
                receipt.append("  - ").append(item.name).append(" (x").append(item.quantity).append(")")
                        .append(": ").append(currencyFormat.format(item.price * item.quantity)).append("\n");
            }
            receipt.append("\n");
        }

        receipt.append("───────────────────────────────────────────────────\n");
        receipt.append("PAYMENT SUMMARY\n");
        receipt.append("───────────────────────────────────────────────────\n");
        receipt.append(String.format("%-30s %19s\n", "Base Price:", basePriceLabel.getText()));
        receipt.append(String.format("%-30s %19s\n", "Accessories:", accessoriesTotalLabel.getText()));
        receipt.append(String.format("%-30s %19s\n", "Tax (8%):", taxLabel.getText()));
        receipt.append("───────────────────────────────────────────────────\n");
        receipt.append(String.format("%-30s %19s\n", "GRAND TOTAL:", totalPriceLabel.getText()));
        receipt.append("═══════════════════════════════════════════════════\n\n");

        if (installmentRadio.isSelected() && selectedPlan != null) {
            receipt.append("Payment Method: Installment Plan\n");
            receipt.append("  Plan: ").append(selectedPlan.months).append(" months @ ").append(selectedPlan.apr).append("% APR\n");
            receipt.append("  Down Payment: ").append(downPaymentLabel.getText()).append("\n");
            receipt.append("  Monthly Payment: ").append(monthlyPaymentLabel.getText()).append("\n");
            receipt.append("  Total with Interest: ").append(totalWithInterestLabel.getText()).append("\n\n");
        } else {
            receipt.append("Payment Method: Full Payment\n\n");
        }

        receipt.append("        Thank you for choosing Porsche!\n");
        receipt.append("═══════════════════════════════════════════════════\n");

        // Print to console (in a real app, this would go to a printer)
        System.out.println(receipt.toString());

        // Show confirmation
        Alert printConfirmation = new Alert(Alert.AlertType.INFORMATION);
        printConfirmation.setTitle("Receipt Printed");
        printConfirmation.setHeaderText("Receipt sent to printer");
        printConfirmation.setContentText("Please check your printer for the receipt.");
        printConfirmation.showAndWait();
    }

    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1300, 850);
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Failed to navigate");
            alert.setContentText("Could not load: " + fxmlPath);
            alert.showAndWait();
        }
    }
}