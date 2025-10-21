package Controllers;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

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
    private double basePrice = 115000;
    private double accessoriesPrice = 0;
    private double taxRate = 0.08;
    private DecimalFormat currencyFormat = new DecimalFormat("$#,##0");

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
    private boolean cameFromAsset = false;

    public void setCameFromAsset(boolean value) {
        this.cameFromAsset = value;
    }

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

        updatePriceLabels();
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
        navigate(cameFromAsset ? "/View/staffAsset.fxml" : "/View/staffFinalize.fxml");
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
        String paymentMethod = installmentRadio.isSelected() ? "Installment" : "Full Payment";
        String message = "Purchase Confirmed!\n\nPayment Method: " + paymentMethod;

        if (installmentRadio.isSelected() && selectedPlan != null) {
            double total = calculateGrandTotal();
            double amountToFinance = total - selectedPlan.downPayment;
            double monthlyRate = (selectedPlan.apr / 100) / 12;
            double monthlyPayment = amountToFinance *
                    (monthlyRate * Math.pow(1 + monthlyRate, selectedPlan.months)) /
                    (Math.pow(1 + monthlyRate, selectedPlan.months) - 1);

            message += "\nPlan: " + selectedPlan.months + " months at " + selectedPlan.apr + "% APR";
            message += "\nMonthly Payment: " + currencyFormat.format(monthlyPayment);
        }

        confirmationMessageLabel.setText(message);
        confirmationMessageLabel.setVisible(true);
        confirmationMessageLabel.setManaged(true);
        confirmButton.setDisable(true);
    }

    public void addAccessory(String name, double price) {
        accessoriesPrice += price;

        HBox accessoryRow = new HBox(15);
        accessoryRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("accessory-name");
        nameLabel.setPrefWidth(400);

        Label priceLabel = new Label(currencyFormat.format(price));
        priceLabel.getStyleClass().add("accessory-price");

        accessoryRow.getChildren().addAll(nameLabel, priceLabel);
        accessoriesContainer.getChildren().add(accessoryRow);

        updatePriceLabels();
    }

    private double calculateSubtotal() { return basePrice + accessoriesPrice; }
    private double calculateTax() { return calculateSubtotal() * taxRate; }
    private double calculateGrandTotal() { return calculateSubtotal() + calculateTax(); }

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

    public double getTotalPrice() { return calculateGrandTotal(); }
    public List<String> getCartItems() { return cartItems; }

    public void clearCart() {
        cartItems.clear();
        accessoriesContainer.getChildren().clear();
        basePrice = 115000;
        accessoriesPrice = 0;
        updatePriceLabels();
    }
}
