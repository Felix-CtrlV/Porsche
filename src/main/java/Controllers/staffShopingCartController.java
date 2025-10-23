package Controllers;

import Model.CarConfiguration;
import Model.CustomizationOption;
import Utils.SessionStaff;
import Utils.DarkModeManager;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
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
            scrollPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    DarkModeManager.getInstance().registerScene(newScene);
                }
            });

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

        downPaymentLabel.setText(currencyFormat.format(selectedPlan.downPayment));
        monthsLabel.setText(selectedPlan.months + " months");
        aprLabel.setText(selectedPlan.apr + "%");

        double loanAmount = total - selectedPlan.downPayment;
        double monthlyRate = (selectedPlan.apr / 100) / 12;
        double monthlyPayment = (loanAmount * monthlyRate * Math.pow(1 + monthlyRate, selectedPlan.months)) /
                (Math.pow(1 + monthlyRate, selectedPlan.months) - 1);

        monthlyPaymentLabel.setText(currencyFormat.format(monthlyPayment));

        double totalPayment = (monthlyPayment * selectedPlan.months) + selectedPlan.downPayment;
        double totalInterest = totalPayment - total;

        totalInterestLabel.setText(currencyFormat.format(totalInterest));
        totalWithInterestLabel.setText(currencyFormat.format(totalPayment));
    }

    private void updatePriceLabels() {
        if (basePriceLabel != null) basePriceLabel.setText(currencyFormat.format(basePrice));
        if (summaryBasePriceLabel != null) summaryBasePriceLabel.setText(currencyFormat.format(basePrice));
        if (accessoriesTotalLabel != null) accessoriesTotalLabel.setText(currencyFormat.format(accessoriesPrice));

        double subtotal = basePrice + accessoriesPrice;
        double tax = subtotal * taxRate;
        double total = subtotal + tax;

        if (taxLabel != null) taxLabel.setText(currencyFormat.format(tax));
        if (totalPriceLabel != null) totalPriceLabel.setText(currencyFormat.format(total));

        updateInstallmentDetails();
    }

    private double calculateGrandTotal() {
        return (basePrice + accessoriesPrice) * (1 + taxRate);
    }

    private void goBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/View/staffWelcome.fxml"));
            Scene scene = new Scene(root);
            DarkModeManager.getInstance().registerScene(scene);
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void confirmPurchase() {
        if (basePrice == 0 && accessoriesPrice == 0) {
            showAlert("Empty Cart", "Your cart is empty. Please add items before confirming.");
            return;
        }

        String paymentMethod = fullPaymentRadio.isSelected() ? "Full Payment" : "Installment Plan";
        showSuccessDialog(paymentMethod);
    }

    private void showSuccessDialog(String paymentMethod) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initStyle(StageStyle.UNDECORATED);
        dialogStage.setTitle("Purchase Successful");

        VBox dialogVBox = new VBox(20);
        dialogVBox.setAlignment(Pos.CENTER);
        dialogVBox.setPadding(new Insets(30));
        dialogVBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.98); " +
                "-fx-background-radius: 20; " +
                "-fx-border-color: rgba(213, 0, 28, 0.3); " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 20; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 30, 0, 0, 10);");

        Label successIcon = new Label("✓");
        successIcon.setStyle("-fx-font-size: 60px; -fx-text-fill: #4CAF50; -fx-font-weight: bold;");

        Label messageLabel = new Label("Purchase Successful!");
        messageLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1A1A1A;");

        Label detailsLabel = new Label("Payment Method: " + paymentMethod);
        detailsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button homeButton = new Button("Return to Home");
        homeButton.setStyle("-fx-background-color: #D5001C; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-padding: 12px 24px; -fx-background-radius: 8px; -fx-cursor: hand;");
        homeButton.setOnAction(e -> {
            SessionStaff.getInstance().clearSession();
            dialogStage.close();
            returnToHome();
        });

        Button printButton = new Button("Print Receipt");
        printButton.setStyle("-fx-background-color: white; -fx-text-fill: #D5001C; " +
                "-fx-border-color: #D5001C; -fx-border-width: 2px; " +
                "-fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-padding: 12px 24px; -fx-background-radius: 8px; " +
                "-fx-border-radius: 8px; -fx-cursor: hand;");
        printButton.setOnAction(e -> {
            printReceipt(paymentMethod);
            dialogStage.close();
            SessionStaff.getInstance().clearSession();
            returnToHome();
        });

        buttonBox.getChildren().addAll(homeButton, printButton);
        dialogVBox.getChildren().addAll(successIcon, messageLabel, detailsLabel, buttonBox);

        Scene dialogScene = new Scene(dialogVBox, 450, 300);
        dialogScene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        dialogStage.setScene(dialogScene);
        dialogStage.showAndWait();
    }

    private void printReceipt(String paymentMethod) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("=".repeat(50)).append("\n");
        receipt.append("           PORSCHE PURCHASE RECEIPT\n");
        receipt.append("=".repeat(50)).append("\n\n");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        receipt.append("Date: ").append(sdf.format(new Date())).append("\n");
        receipt.append("Payment Method: ").append(paymentMethod).append("\n\n");

        receipt.append("-".repeat(50)).append("\n");
        receipt.append("ITEMS:\n");
        receipt.append("-".repeat(50)).append("\n");

        CarConfiguration carConfig = SessionStaff.getInstance().getCarConfiguration();
        if (carConfig != null) {
            receipt.append(String.format("%-30s %15s\n", carConfig.getModelName(), currencyFormat.format(basePrice)));

            for (AccessoryItemDisplay item : displayedAccessories) {
                if (item.isCarOption) {
                    receipt.append(String.format("  %-28s %15s\n", item.name, currencyFormat.format(item.price)));
                }
            }
        }

        for (AccessoryItemDisplay item : displayedAccessories) {
            if (!item.isCarOption) {
                receipt.append(String.format("%-30s %15s\n", item.name, currencyFormat.format(item.price)));
            }
        }

        receipt.append("-".repeat(50)).append("\n");
        receipt.append(String.format("%-30s %15s\n", "Subtotal:", currencyFormat.format(basePrice + accessoriesPrice)));
        receipt.append(String.format("%-30s %15s\n", "Tax (8%):", currencyFormat.format((basePrice + accessoriesPrice) * taxRate)));
        receipt.append("=".repeat(50)).append("\n");
        receipt.append(String.format("%-30s %15s\n", "TOTAL:", currencyFormat.format(calculateGrandTotal())));
        receipt.append("=".repeat(50)).append("\n\n");

        if (paymentMethod.equals("Installment Plan") && selectedPlan != null) {
            receipt.append("INSTALLMENT DETAILS:\n");
            receipt.append("-".repeat(50)).append("\n");
            receipt.append(String.format("Down Payment: %s\n", currencyFormat.format(selectedPlan.downPayment)));
            receipt.append(String.format("Monthly Payment: %s\n", monthlyPaymentLabel.getText()));
            receipt.append(String.format("Number of Months: %d\n", selectedPlan.months));
            receipt.append(String.format("APR: %.1f%%\n", selectedPlan.apr));
            receipt.append(String.format("Total with Interest: %s\n", totalWithInterestLabel.getText()));
            receipt.append("-".repeat(50)).append("\n\n");
        }

        receipt.append("     Thank you for your purchase!\n");
        receipt.append("           Drive with passion.\n");
        receipt.append("=".repeat(50)).append("\n");

        System.out.println(receipt);
    }

    private void loadStaffAsset() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/View/staffAsset.fxml"));
            Scene scene = new Scene(root);
            DarkModeManager.getInstance().registerScene(scene);
            Stage stage = (Stage) loadAssetButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void returnToHome() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/View/staffWelcome.fxml"));
            Scene scene = new Scene(root);
            DarkModeManager.getInstance().registerScene(scene);
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}