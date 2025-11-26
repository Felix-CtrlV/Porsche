package Controllers;

import DAO.CarDAO;
import DAO.CarPartsDAO;
import DAO.OrderDAO;
import DAO.PaymentDAO;
import Model.CarConfiguration;
import Model.CarPart;
import Utils.SessionStaff;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import Database.DatabaseConnectionManager;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class staffShopingCartController {

    private static final Logger logger = LoggerFactory.getLogger(staffShopingCartController.class);

    @FXML private Label modelLabel;
    @FXML private Label totalPriceLabel;
    @FXML private Label basePriceLabel;
    @FXML private RadioButton fullPaymentRadio;
    @FXML private RadioButton installmentRadio;
    @FXML private ToggleGroup paymentMethodGroup;
    @FXML private VBox installmentOptionsContainer;
    @FXML private ComboBox<String> installmentPlanCombo;
    @FXML private Label downPaymentLabel;
    @FXML private Label monthlyPaymentLabel;
    @FXML private Label monthsLabel;
    @FXML private Label aprLabel;
    @FXML private Label totalInterestLabel;
    @FXML private Label totalWithInterestLabel;
    @FXML private Label summaryBasePriceLabel;
    @FXML private Label accessoriesTotalLabel;
    @FXML private Button loadAssetButton;
    @FXML private Button backButton;
    @FXML private Button homeButton;
    @FXML private Button confirmButton;
    @FXML private VBox accessoriesContainer;
    @FXML private VBox accessoriesListContainer;
    @FXML private VBox configDetailsContainer;
    @FXML private StackPane passwordVerifyOverlay;
    @FXML private VBox passwordVerifyPane;
    @FXML private PasswordField verifyPasswordField;
    @FXML private Label passwordErrorLabel;
    @FXML private Button verifyPasswordBtn;
    @FXML private Button cancelVerifyBtn;
    @FXML private StackPane customerInfoOverlay;
    @FXML private VBox customerInfoPane;
    @FXML private TextField customerNameField;
    @FXML private ComboBox<String> nrc_first;
    @FXML private ComboBox<String> nrc_second;
    @FXML private ComboBox<String> nrc_third;
    @FXML private TextField nrc_number;
    @FXML private TextField customerPhoneField;
    @FXML private TextField customerAddressField;
    @FXML private TextField customerEmailField;
    @FXML private Label customerErrorLabel;
    @FXML private Button confirmDetailsBtn;
    @FXML private StackPane successOverlay;
    @FXML private VBox successPane;
    @FXML private Label successOrderIdLabel;
    @FXML private Button successHomeButton;
    @FXML private Label phoneErrorLabel;

    private CarConfiguration carConfig;
    private List<CarPart> allParts;
    private int currentOrderId;
    private int currentCustomerId;

    @FXML
    public void initialize() {
        initializeOverlays();
        loadCarConfiguration();
        loadAccessoriesFromSession();
        setupEventHandlers();
        setupPaymentOptions();
        setupInstallmentPlans();
        setupButtonHoverEffects();
        updateConfirmButtonState();
        setupNRCComponents();
        setupPhoneValidation();
    }

    private void initializeOverlays() {
        if (passwordVerifyOverlay != null) {
            passwordVerifyOverlay.setVisible(false);
            passwordVerifyOverlay.setManaged(false);
        }
        if (passwordVerifyPane != null) {
            passwordVerifyPane.setVisible(false);
            passwordVerifyPane.setManaged(false);
        }
        if (customerInfoOverlay != null) {
            customerInfoOverlay.setVisible(false);
            customerInfoOverlay.setManaged(false);
        }
        if (customerInfoPane != null) {
            customerInfoPane.setVisible(false);
            customerInfoPane.setManaged(false);
        }
        if (successOverlay != null) {
            successOverlay.setVisible(false);
            successOverlay.setManaged(false);
        }
        if (successPane != null) {
            successPane.setVisible(false);
            successPane.setManaged(false);
        }

        if (verifyPasswordField != null) {
            verifyPasswordField.setOnKeyPressed(event -> {
                if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                    onVerifyPassword();
                }
            });
        }
        if (verifyPasswordBtn != null) {
            verifyPasswordBtn.setOnAction(e -> onVerifyPassword());
        }
        if (cancelVerifyBtn != null) {
            cancelVerifyBtn.setOnAction(e -> hidePasswordVerification());
        }
        if (confirmDetailsBtn != null) {
            confirmDetailsBtn.setOnAction(e -> onConfirmDetails());
        }
        if (successHomeButton != null) {
            successHomeButton.setOnAction(e -> onSuccessHome());
        }
        if (homeButton != null) {
            homeButton.setOnAction(e -> goHome());
        }
        if (phoneErrorLabel != null) {
            phoneErrorLabel.setVisible(false);
            phoneErrorLabel.setManaged(false);
        }
    }

    private void setupButtonHoverEffects() {
        if (verifyPasswordBtn != null) {
            verifyPasswordBtn.setOnMouseEntered(e -> {
                verifyPasswordBtn.setStyle("-fx-background-color: #b00016; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-width: 140; -fx-pref-height: 32; -fx-background-radius: 8;");
            });
            verifyPasswordBtn.setOnMouseExited(e -> {
                verifyPasswordBtn.setStyle("-fx-background-color: #d5001f; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-width: 140; -fx-pref-height: 32; -fx-background-radius: 8;");
            });
        }
        if (successHomeButton != null) {
            successHomeButton.setOnMouseEntered(e -> {
                successHomeButton.setStyle("-fx-background-color: #45a049; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-width: 160; -fx-pref-height: 40; -fx-background-radius: 10; -fx-font-size: 14;");
            });
            successHomeButton.setOnMouseExited(e -> {
                successHomeButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-pref-width: 160; -fx-pref-height: 40; -fx-background-radius: 10; -fx-font-size: 14;");
            });
        }
        if (homeButton != null) {
            homeButton.setOnMouseEntered(e -> {
                homeButton.setStyle("-fx-background-color: #D5001C; -fx-text-fill: #FFFFFF; -fx-border-color: #D5001C; -fx-scale-x: 1.05; -fx-scale-y: 1.05;");
            });
            homeButton.setOnMouseExited(e -> {
                homeButton.setStyle("-fx-background-color: rgba(255, 255, 255, 0.8); -fx-text-fill: #D5001C; -fx-border-color: rgba(213, 0, 28, 0.3); -fx-scale-x: 1.0; -fx-scale-y: 1.0;");
            });
        }
    }

    private void setupNRCComponents() {
        nrc_first.getItems().addAll("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14");
        nrc_third.getItems().addAll("N", "E", "P", "A", "F", "G", "D", "L", "M", "Y", "K", "S", "H", "T", "W", "Z", "B", "C");
        nrc_first.setOnAction(e -> {
            String selected = nrc_first.getValue();
            nrc_second.getItems().clear();
            switch (selected) {
                case "1" -> nrc_second.getItems().addAll("BaMaNa", "KhaHpaNa", "DaHpaNa", "HaPaNa", "HpaKaNa", "AhGaYa", "KaMaTa", "KaPaTa", "KhaLaHpa", "LaGaNa", "MaKhaBa", "MaSaNa", "MaKaTa", "MaNyaNa", "MaMaNa", "MaLaNa", "NaMaNa", "PaWaNa", "PaNaDa", "PaTaAh", "SaDaNa", "YaBaYa", "YaKaNa", "SaBaNa", "SaPaYa", "TaNaNa", "TaSaLa", "WaMaNa");
                case "2" -> nrc_second.getItems().addAll("BaLaKha", "DaMaSa", "HpaSaNa", "HpaYaSa", "LaKaNa", "MaSaNa", "YaTaNa", "YaThaNa");
                case "3" -> nrc_second.getItems().addAll("BaGaLa", "LaBaNa", "BaAhNa", "HpaPaNa", "BaThaSa", "KaMaMa", "KaKaYa", "KaDaNa", "KaSaKa", "KaDaTa", "LaThaNa", "MaWaTa", "PaKaNa", "YaYaTha", "SaKaLa", "ThaTaNa", "ThaTaKa", "WaLaMa");
                case "4" -> nrc_second.getItems().addAll("KaKhaNa", "HpaLaNa", "HaKhaNa", "KaPaLa", "MaTaPa", "MaTaNa", "PaLaWa", "YaZaNa", "YaKhaDa", "SaMaNa", "TaTaNa", "HtaTaLa", "TaZaNa");
                case "5" -> nrc_second.getItems().addAll("AhYaTa", "BaMaNa", "BaTaLa", "KhaOuTa", "KhaTaNa", "HaMaLa", "AhTaNa", "KaLaHta", "KaLaWa", "KaBaLa", "KaNaNa", "KaThaNa", "KaLaTa", "KhaOuNa", "KaLaNa", "LaHaNa", "LaYaNa", "MaLaNa", "MaKaNa", "MaYaNa", "MaMaNa", "MaMaTa", "NaYaNa", "NgaZaNa", "PaLaNa", "HpaPaNa", "PaLaBa", "SaKaNa", "SaLaKa", "YaBaNa", "DaPaYa", "TaMaNa", "TaSaNa", "HtaKhaNa", "WaLaNa", "WaThaNa", "YaOuNa", "'YaMaPa", "KaMaNa", "KhaPaNa");
                case "6" -> nrc_second.getItems().addAll("BaPaNa", "HtaWaNa", "KaLaAh", "KaThaNa", "KaSaNa", "LaLaNa", "MaMaNa", "PaLaNa", "TaThaYa", "ThaYaKha", "YaHpaNa", "KhaMaNa", "MaTaNa", "PaLaTa", "KaYaYa");
                case "7" -> nrc_second.getItems().addAll("DaOuNa", "KaPaKa", "KaWaNa", "KaKaNa", "KaTaKha", "LaPaTa", "MaLaNa", "MaNyaNa", "NaTaLa", "NyaLaPa", "AhHpaNa", "AhTaNa", "PaTaNa", "PaKhaTa", "PaKhaNa", "PaTaTa", "PaNaKa", "HpaMaNa", "PaMaNa", "YaTaNa", "YaKaNa", "HtaTaPa", "TaNgaNa", "ThaNaPa", "ThaWaTa", "ThaKaNa", "ThaSaNa", "WaMaNa", "YaTaYa", "ZaKaNa", "PaTaSa");
                case "8" -> nrc_second.getItems().addAll("AhLaNa", "KhaMaNa", "GaGaNa", "KaMaNa", "MaKaNa", "MaBaNa", "MaTaNa", "MaLaNa", "MaMaNa", "MaHtaNa", "MaThaNa", "NaMaNa", "NgaHpaNa", "PaKhaKa", "PaMaNa", "PaHpaNa", "SaLaNa", "SaMaNa", "SaHpaNa", "SaTaYa", "SaPaWa", "TaTaKa", "ThaYaNa", "HtaLaNa", "YaNaKha", "YaSaKa", "KaHtaNa");
                case "9" -> nrc_second.getItems().addAll("AhMaya", "AhMaZa", "KhaAhZa", "KhaMaSa", "KaPaTa", "KaSaNa", "MaTaYa", "MaHaMa", "MaLaNa", "MaHtaLa", "MaKaNa", "MaKhaNa", "MaThaNa", "NaHtaKa", "NgaThaYa", "NgaZaNa", "NyaOuNa", "PaThaKa", "PaBaNa", "PaKaKha", "PaOuLa", "SaKaNa", "SaKaTa", "ThaPaKa", "TaTaOu", "TaThaNa", "ThaSaNa", "WaTaNa", "YaMaTha", "TaKaTa", "MaMaNa", "DaKhaTha", "LaWaNa", "0uTaTha", "PaBaTha", "PaMaNa", "TaKaNa", "ZaBaTha", "ZaYaTha");
                case "10" -> nrc_second.getItems().addAll("BaLaNa", "KhaSaNa", "KhaZaNa", "KaMaYa", "KaHtaNa", "LaMaNa", "MaLaMa", "MaDaNa", "PaMaNa", "ThaHpaYa", "ThaHtaNa", "NaMaNa");
                case "11" -> nrc_second.getItems().addAll("AhMaNa", "BaThaTa", "GaMaNa", "KaHpaNa", "KaTaNa", "MaAhTa", "MaTaNa", "MaPaNa", "MaAhNa", "MaOuNa", "MaPaTa", "PaTaNa", "PaNaTa", "YaBaNa", "YaThaTa", "SaTaNa", "ThaTaNa", "TaKaNa", "KaTaLa", "TaPaWa");
                case "12" -> nrc_second.getItems().addAll("AhLaNa", "BaHaNa", "BaTaHta", "KaKaKa", "DaGaYa", "DaGaMa", "DaGaSa", "DaGaTa", "DaGaNa", "DaLaNa", "DaPaNa", "LaThaYa", "LaMaNa", "LaKaNa", "MaBaNa", "HtaTaPa", "AhSaNa", "KaMaYa", "KaMaNa", "KhaYaNa", "KaKhaKa", "KaTaTa", "KaTaNa", "KaMaTa", "LaMaTa", "LaThaNa", "MaYaKa", "MaGaDa", "MaGaTa", "0uKaMa", "PaBaTa", "PaZaTa", "SaKhaNa", "SaKaKha", "SaKaNa", "YaPaTha", "0uKaTa", "TaTaHta", "TaKaNa", "TaMaNa", "ThaKaTa", "ThaLaNa", "ThaGaKa", "ThaKhaNa", "TaTaNa", "YaKaNa", "0uKaNa");
                case "13" -> nrc_second.getItems().addAll("AhKhaNa", "KhaYaHa", "KhaMaNa", "HaTaNa", "HaPaNa", "HaPaTa", "SaHpaNa", "ThaNaNa", "SaSaNa", "ThaPaNa", "KaLaHpa", "KaLaNa", "KaLaDa", "KaMaSa", "KaTaNa", "KaYaNa", "KaTaTa", "KaHaNa", "KaLaTa", "KaKhaNa", "KaMaNa", "KaTaLa", "KaThaNa", "LaKhaNa", "LaKhaTa", "LaYaNa", "LaKaNa", "LaHaNa", "LaLaNa", "MaBaNa", "MaMaSa", "MaTaNa", "MaTaTa", "MaMaNa", "MaMaNa", "MaHpaNa", "MaKaNa", "MaPaNa", "MaHpaNa", "MaSaNa", "MaYaNa", "MaKaNa", "MaKhaNa", "MaLaNa", "MaMaTa", "MaMaTa", "MaNaNa", "MaPaNa", "MaTaNa", "MaYaTa", "MaYaNa", "MaYaNa", "MaSaTa", "NaKhaWa", "NaTaNa", "NaKhaNa", "NaMaTa", "NaHpaNa", "NaSaNa", "NaKaNa", "NaWaNa", "NaPhaNa", "NaKhaNa", "NaKhaTa", "NyaYaNa", "PaKhaNa", "PaYaNa", "PaSaNa", "PaWaNa", "HpaKhaNa", "PaTaYa", "PaLaNa", "TaKhaLa", "TaYaNa", "TaKaNa", "YaLaNa", "YaSaNa", "YaHpaNa", "YaNgaNa", "NaTaYa", "PaLaTa", "KhaLaNa", "MaHaYa", "PaPaKa", "TaMaNya", "MaBaTa", "MaNgaNa", "AhTaNa", "TaLaNa");
                case "14" -> nrc_second.getItems().addAll("AhMaTa", "BaKaLa", "DaNaHpa", "DaDaYa", "HaKaKa", "HaThaTa", "AhGaPa", "KaKaHta", "KaLaNa", "KaKhaNa", "KaKaNa", "KaPaNa", "LaPaTa", "LaMaNa", "MaAhPa", "MaMaKa", "MaMaNa", "NgaPaTa", "NgaThaKha", "NgaYaKa", "NgaSaNa", "NgaThaYa", "NyaTaNa", "NyaTaNa", "PaTaNa", "PaThaNa", "HpaPaNa", "PaSaLa", "YaThaYa", "ThaPaNa", "WaKhaMa", "YaKaNa", "ZaLaNa");
            }
        });
        nrc_number.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                nrc_number.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    private void setupPhoneValidation() {
        if (customerPhoneField != null && phoneErrorLabel != null) {
            customerPhoneField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.length() > 20) {
                    customerPhoneField.setText(oldValue);
                    showPhoneError("Phone number too long. Maximum 20 characters allowed.");
                } else if (!newValue.matches("^[0-9+\\-\\s()]*$")) {
                    showPhoneError("Invalid characters. Only numbers, +, -, (, ) and spaces allowed.");
                } else if (newValue.length() > 15) {
                    showPhoneError("Phone number getting long (" + newValue.length() + "/20)");
                } else if (newValue.length() > 10) {
                    hidePhoneError();
                } else if (newValue.isEmpty()) {
                    hidePhoneError();
                } else {
                    hidePhoneError();
                }
            });

            customerPhoneField.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) {
                    validatePhoneFormat();
                }
            });
        }
    }

    private void showPhoneError(String message) {
        if (phoneErrorLabel != null) {
            phoneErrorLabel.setText(message);
            phoneErrorLabel.setStyle("-fx-text-fill: #d5001c; -fx-font-size: 11px; -fx-font-weight: bold;");
            phoneErrorLabel.setVisible(true);
            phoneErrorLabel.setManaged(true);
        }
    }

    private void hidePhoneError() {
        if (phoneErrorLabel != null) {
            phoneErrorLabel.setVisible(false);
            phoneErrorLabel.setManaged(false);
        }
    }

    private void validatePhoneFormat() {
        String phone = customerPhoneField.getText().trim();
        if (!phone.isEmpty()) {
            if (phone.length() < 8) {
                showPhoneError("Phone number too short. Minimum 8 digits required.");
            } else if (!phone.matches("^[+]?[0-9\\s\\-\\(\\)]{8,20}$")) {
                showPhoneError("Invalid phone number format");
            } else {
                hidePhoneError();
            }
        }
    }

    private void loadCarConfiguration() {
        try {
            carConfig = SessionStaff.getInstance().getCarConfiguration();
            if (carConfig != null) {
                updatePriceDisplay();
            } else {
                updatePriceDisplayForAccessoriesOnly();
            }
        } catch (Exception e) {
            logger.error("Error loading car configuration", e);
        }
    }

    private void loadAccessoriesFromSession() {
        SessionStaff session = SessionStaff.getInstance();
        List<Integer> accessoryIds = session.getSelectedAccessories();
        if (accessoryIds.isEmpty()) {
            showEmptyAccessoriesState();
            return;
        }
        loadAllParts();
        displayAccessories(accessoryIds);
    }

    private void loadAllParts() {
        try {
            CarPartsDAO dao = new CarPartsDAO();
            allParts = dao.getAllParts();
        } catch (Exception e) {
            logger.error("Error loading parts", e);
        }
    }

    private void displayAccessories(List<Integer> accessoryIds) {
        if (accessoriesListContainer == null) return;
        accessoriesListContainer.getChildren().clear();
        Map<Integer, Integer> itemQuantities = new HashMap<>();
        for (int accessoryId : accessoryIds) {
            itemQuantities.put(accessoryId, itemQuantities.getOrDefault(accessoryId, 0) + 1);
        }
        double accessoriesTotal = 0.0;
        for (Map.Entry<Integer, Integer> entry : itemQuantities.entrySet()) {
            int accessoryId = entry.getKey();
            int quantity = entry.getValue();
            CarPart part = findPartById(accessoryId);
            if (part != null) {
                HBox accessoryItem = createAccessoryItem(part, quantity);
                accessoriesListContainer.getChildren().add(accessoryItem);
                accessoriesTotal += part.getPrice() * quantity;
            }
        }
        updateAccessoriesPriceSummary(accessoriesTotal);
    }

    private CarPart findPartById(int partId) {
        if (allParts == null) return null;
        for (CarPart part : allParts) {
            if (part.getPartId() == partId) {
                return part;
            }
        }
        return null;
    }

    private HBox createAccessoryItem(CarPart part, int quantity) {
        HBox itemBox = new HBox(15);
        itemBox.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 8; -fx-padding: 15; -fx-border-color: #eeeeee; -fx-border-width: 1;");
        VBox infoBox = new VBox(5);
        Label nameLabel = new Label(part.getPartName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333333;");
        Label priceLabel = new Label(String.format("$%.2f", part.getPrice()));
        priceLabel.setStyle("-fx-text-fill: #E60023; -fx-font-size: 14px; -fx-font-weight: bold;");
        Label quantityLabel = new Label("Qty: " + quantity);
        quantityLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px; -fx-font-weight: bold;");
        infoBox.getChildren().addAll(nameLabel, priceLabel, quantityLabel);
        Button removeButton = new Button("Remove");
        removeButton.setStyle("-fx-background-color: #FF4444; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10;");
        removeButton.setOnAction(event -> removeAccessoryFromCart(part.getPartId()));
        HBox.setHgrow(infoBox, javafx.scene.layout.Priority.ALWAYS);
        itemBox.getChildren().addAll(infoBox, removeButton);
        return itemBox;
    }

    private void removeAccessoryFromCart(int partId) {
        SessionStaff session = SessionStaff.getInstance();
        session.removeSelectedAccessory(partId);
        loadAccessoriesFromSession();
        updateConfirmButtonState();
        if (session.getSelectedAccessories().isEmpty() && carConfig == null) {
            resetAllPricesToZero();
        }
    }

    private void resetAllPricesToZero() {
        if (accessoriesTotalLabel != null) accessoriesTotalLabel.setText("$0.00");
        if (totalPriceLabel != null) totalPriceLabel.setText("$0.00");
        if (summaryBasePriceLabel != null) summaryBasePriceLabel.setText("$0.00");
        if (basePriceLabel != null) basePriceLabel.setText("$0.00");
        updateInstallmentCalculation();
    }

    private void updatePriceDisplay() {
        if (carConfig != null) {
            if (modelLabel != null) modelLabel.setText(carConfig.getModelName());
            if (totalPriceLabel != null) totalPriceLabel.setText(String.format("$%,.2f", carConfig.getTotalPrice()));
            if (basePriceLabel != null) basePriceLabel.setText(String.format("$%,.2f", carConfig.getBasePrice()));
            if (summaryBasePriceLabel != null) summaryBasePriceLabel.setText(String.format("$%,.2f", carConfig.getBasePrice()));
            double accessoriesTotal = calculateAccessoriesTotal();
            if (accessoriesTotalLabel != null) accessoriesTotalLabel.setText(String.format("$%,.2f", accessoriesTotal));
            double grandTotal = carConfig.getTotalPrice() + accessoriesTotal;
            if (totalPriceLabel != null) totalPriceLabel.setText(String.format("$%,.2f", grandTotal));
        }
    }

    private void updatePriceDisplayForAccessoriesOnly() {
        if (configDetailsContainer != null) {
            configDetailsContainer.setVisible(false);
            configDetailsContainer.setManaged(false);
        }
        if (modelLabel != null) modelLabel.setText("Accessories Only");
        if (basePriceLabel != null) basePriceLabel.setText("$0.00");
        if (summaryBasePriceLabel != null) summaryBasePriceLabel.setText("$0.00");
        double accessoriesTotal = calculateAccessoriesTotal();
        if (accessoriesTotalLabel != null) accessoriesTotalLabel.setText(String.format("$%,.2f", accessoriesTotal));
        if (accessoriesTotal > 0) {
            if (totalPriceLabel != null) totalPriceLabel.setText(String.format("$%,.2f", accessoriesTotal));
        } else {
            resetAllPricesToZero();
        }
    }

    private void updateAccessoriesPriceSummary(double accessoriesTotal) {
        if (accessoriesTotalLabel != null) accessoriesTotalLabel.setText(String.format("$%,.2f", accessoriesTotal));
        double basePrice = carConfig != null ? carConfig.getTotalPrice() : 0.0;
        double grandTotal = basePrice + accessoriesTotal;
        if (totalPriceLabel != null) totalPriceLabel.setText(String.format("$%,.2f", grandTotal));
    }

    private double calculateAccessoriesTotal() {
        SessionStaff session = SessionStaff.getInstance();
        List<Integer> accessoryIds = session.getSelectedAccessories();
        double total = 0.0;
        Map<Integer, Integer> itemQuantities = new HashMap<>();
        for (int accessoryId : accessoryIds) {
            itemQuantities.put(accessoryId, itemQuantities.getOrDefault(accessoryId, 0) + 1);
        }
        for (Map.Entry<Integer, Integer> entry : itemQuantities.entrySet()) {
            CarPart part = findPartById(entry.getKey());
            if (part != null) {
                total += part.getPrice() * entry.getValue();
            }
        }
        return total;
    }

    private void showEmptyAccessoriesState() {
        if (accessoriesListContainer != null) {
            accessoriesListContainer.getChildren().clear();
            Label emptyLabel = new Label("No accessories selected");
            emptyLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 16px; -fx-padding: 20;");
            accessoriesListContainer.getChildren().add(emptyLabel);
        }
        if (accessoriesTotalLabel != null) accessoriesTotalLabel.setText("$0.00");
    }

    private void setupEventHandlers() {
        if (confirmButton != null) confirmButton.setOnAction(event -> showPasswordVerification());
        if (backButton != null) backButton.setOnAction(event -> goBack());
        if (homeButton != null) homeButton.setOnAction(event -> goHome());
        if (loadAssetButton != null) loadAssetButton.setOnAction(event -> loadAccessories());
        if (paymentMethodGroup != null) {
            paymentMethodGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == installmentRadio) {
                    showInstallmentOptions();
                    hideGrandTotalDisplay();
                } else {
                    hideInstallmentOptions();
                    showGrandTotalDisplay();
                }
            });
        }
    }

    private void hideGrandTotalDisplay() {
        if (totalPriceLabel != null) {
            totalPriceLabel.setVisible(false);
            totalPriceLabel.setManaged(false);
        }
    }

    private void showGrandTotalDisplay() {
        if (totalPriceLabel != null) {
            totalPriceLabel.setVisible(true);
            totalPriceLabel.setManaged(true);
        }
    }

    private void updateConfirmButtonState() {
        SessionStaff session = SessionStaff.getInstance();
        boolean hasItems = !session.getSelectedAccessories().isEmpty() || carConfig != null;
        if (confirmButton != null) {
            if (hasItems) {
                confirmButton.setDisable(false);
                confirmButton.setStyle("-fx-background-color: #D5001C; -fx-text-fill: #FFFFFF; -fx-font-weight: 700; -fx-background-radius: 10px; -fx-cursor: hand;");
            } else {
                confirmButton.setDisable(true);
                confirmButton.setStyle("-fx-background-color: #CCCCCC; -fx-text-fill: #666666; -fx-font-weight: 700; -fx-background-radius: 10px; -fx-cursor: default;");
            }
        }
    }

    private void showPasswordVerification() {
        if (!SessionStaff.isSessionActive()) {
            showAlert("Session Expired", "Your session has expired. Please log in again.");
            return;
        }
        SessionStaff session = SessionStaff.getInstance();
        if (session.getSelectedAccessories().isEmpty() && carConfig == null) {
            showAlert("Empty Cart", "Your cart is empty. Please add accessories or select a car.");
            return;
        }
        passwordVerifyOverlay.setVisible(true);
        passwordVerifyOverlay.setManaged(true);
        passwordVerifyPane.setVisible(true);
        passwordVerifyPane.setManaged(true);
        verifyPasswordField.clear();
        passwordErrorLabel.setVisible(false);
        GaussianBlur blur = new GaussianBlur(10);
        if (accessoriesContainer != null) accessoriesContainer.setEffect(blur);
        passwordVerifyPane.setScaleX(0.8);
        passwordVerifyPane.setScaleY(0.8);
        passwordVerifyPane.setOpacity(0);
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), passwordVerifyPane);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(200), passwordVerifyPane);
        fadeTransition.setToValue(1.0);
        ParallelTransition parallelTransition = new ParallelTransition(scaleTransition, fadeTransition);
        parallelTransition.play();
        javafx.application.Platform.runLater(() -> verifyPasswordField.requestFocus());
    }

    private void hidePasswordVerification() {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(150), passwordVerifyPane);
        fadeTransition.setToValue(0);
        fadeTransition.setOnFinished(e -> {
            passwordVerifyPane.setVisible(false);
            passwordVerifyPane.setManaged(false);
            passwordVerifyOverlay.setVisible(false);
            passwordVerifyOverlay.setManaged(false);
            if (accessoriesContainer != null) accessoriesContainer.setEffect(null);
            verifyPasswordField.clear();
            passwordErrorLabel.setVisible(false);
        });
        fadeTransition.play();
    }

    private void showCustomerInfoOverlay() {
        customerInfoOverlay.setVisible(true);
        customerInfoOverlay.setManaged(true);
        customerInfoPane.setVisible(true);
        customerInfoPane.setManaged(true);
        customerNameField.clear();
        nrc_first.getSelectionModel().clearSelection();
        nrc_second.getSelectionModel().clearSelection();
        nrc_third.getSelectionModel().clearSelection();
        nrc_number.clear();
        customerPhoneField.clear();
        customerAddressField.clear();
        customerEmailField.clear();
        customerErrorLabel.setVisible(false);
        hidePhoneError();
        confirmDetailsBtn.setDisable(false);
        GaussianBlur blur = new GaussianBlur(10);
        if (accessoriesContainer != null) accessoriesContainer.setEffect(blur);
        customerInfoPane.setScaleX(0.8);
        customerInfoPane.setScaleY(0.8);
        customerInfoPane.setOpacity(0);
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), customerInfoPane);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(200), customerInfoPane);
        fadeTransition.setToValue(1.0);
        ParallelTransition parallelTransition = new ParallelTransition(scaleTransition, fadeTransition);
        parallelTransition.play();
        javafx.application.Platform.runLater(() -> customerNameField.requestFocus());
    }

    private void hideCustomerInfoOverlay() {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(150), customerInfoPane);
        fadeTransition.setToValue(0);
        fadeTransition.setOnFinished(e -> {
            customerInfoPane.setVisible(false);
            customerInfoPane.setManaged(false);
            customerInfoOverlay.setVisible(false);
            customerInfoOverlay.setManaged(false);
            if (accessoriesContainer != null) accessoriesContainer.setEffect(null);
        });
        fadeTransition.play();
    }

    private void showSuccessOverlay() {
        successOverlay.setVisible(true);
        successOverlay.setManaged(true);
        successPane.setVisible(true);
        successPane.setManaged(true);
        successOrderIdLabel.setText("Order #" + currentOrderId);
        GaussianBlur blur = new GaussianBlur(10);
        if (accessoriesContainer != null) accessoriesContainer.setEffect(blur);
        successPane.setScaleX(0.8);
        successPane.setScaleY(0.8);
        successPane.setOpacity(0);
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(300), successPane);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(300), successPane);
        fadeTransition.setToValue(1.0);
        ParallelTransition parallelTransition = new ParallelTransition(scaleTransition, fadeTransition);
        parallelTransition.play();
    }

    private void hideSuccessOverlay() {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(150), successPane);
        fadeTransition.setToValue(0);
        fadeTransition.setOnFinished(e -> {
            successPane.setVisible(false);
            successPane.setManaged(false);
            successOverlay.setVisible(false);
            successOverlay.setManaged(false);
            if (accessoriesContainer != null) accessoriesContainer.setEffect(null);
        });
        fadeTransition.play();
    }

    @FXML
    private void onVerifyPassword() {
        String enteredPassword = verifyPasswordField.getText();
        if (enteredPassword.isEmpty()) {
            passwordErrorLabel.setText("Please enter your password");
            passwordErrorLabel.setVisible(true);
            return;
        }
        if (!SessionStaff.isSessionActive()) {
            passwordErrorLabel.setText("User session error. Please logout and login again.");
            passwordErrorLabel.setVisible(true);
            return;
        }
        boolean isPasswordCorrect = verifyPasswordWithStoredProcedure(enteredPassword);
        if (isPasswordCorrect) {
            hidePasswordVerification();
            processOrderConfirmation();
        } else {
            verifyPasswordField.clear();
            passwordErrorLabel.setText("Incorrect password. Please try again.");
            passwordErrorLabel.setVisible(true);
        }
    }

    @FXML
    private void onConfirmDetails() {
        if (validateCustomerInputs()) {
            int customerId = findOrCreateCustomer();
            if (customerId > 0) {
                currentCustomerId = customerId;
                customerErrorLabel.setVisible(false);
                confirmDetailsBtn.setDisable(true);
                createOrderWithCustomer(customerId);
            } else if (customerId == -1) {
                // NRC conflict error - already shown to user
                // Don't proceed with order creation
                confirmDetailsBtn.setDisable(false);
            } else {
                customerErrorLabel.setText("Failed to save customer information. Please try again.");
                customerErrorLabel.setVisible(true);
                confirmDetailsBtn.setDisable(false);
            }
        } else {
            confirmDetailsBtn.setDisable(false);
        }
    }

    private void onSuccessHome() {
        hideSuccessOverlay();
        navigateToStaffWelcome();
    }

    private void goHome() {
        clearCart();
        navigateToStaffWelcome();
    }

    private int findOrCreateCustomer() {
        String nrc = nrc_first.getValue() + "/" + nrc_second.getValue() + "(" + nrc_third.getValue() + ")" + nrc_number.getText().trim();
        String name = customerNameField.getText().trim();
        String phone = customerPhoneField.getText().trim();

        // First check if exact match exists
        Integer existingCustomerId = findExistingCustomer(name, nrc, phone);
        if (existingCustomerId != null) {
            logger.info("Found exact matching customer with ID: " + existingCustomerId);
            return existingCustomerId;
        }

        // Check if NRC exists but details are different
        Integer customerIdByNRC = findExistingCustomerByNRC(nrc);
        if (customerIdByNRC != null) {
            logger.warn("NRC " + nrc + " exists but with different details. Showing error to user.");
            customerErrorLabel.setText("This NRC number already exists with different customer information. Please verify the details.");
            customerErrorLabel.setVisible(true);
            return -1; // Indicate error
        }

        // Create new customer
        return createNewCustomer(name, nrc, phone);
    }

    private Integer findExistingCustomer(String name, String nrc, String phone) {
        String sql = "SELECT customer_id FROM customer_info WHERE customer_name = ? AND customer_nrc = ? AND customer_phone = ?";
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, nrc);
            ps.setString(3, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("customer_id");
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding existing customer", e);
        }
        return null;
    }

    private Integer findExistingCustomerByNRC(String nrc) {
        String sql = "SELECT customer_id FROM customer_info WHERE customer_nrc = ?";
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nrc);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("customer_id");
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding existing customer by NRC", e);
        }
        return null;
    }

    private int createNewCustomer(String name, String nrc, String phone) {
        String sql = "INSERT INTO customer_info (customer_name, customer_nrc, customer_phone, customer_address, customer_email, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, nrc);
            ps.setString(3, phone);
            ps.setString(4, customerAddressField.getText().trim());
            ps.setString(5, customerEmailField.getText().trim());
            ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            int affectedRows = ps.executeUpdate();
            logger.info("Customer insert affected rows: " + affectedRows);
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int customerId = generatedKeys.getInt(1);
                        logger.info("Customer created with ID: " + customerId);
                        return customerId;
                    }
                }
            }
            logger.error("No customer ID generated");
            return -1;
        } catch (SQLException e) {
            logger.error("Error saving customer to database", e);
            logger.error("SQL State: " + e.getSQLState() + ", Error Code: " + e.getErrorCode());
            return -1;
        }
    }

    private void createOrderWithCustomer(int customerId) {
        try {
            logger.info("Creating order for customer ID: " + customerId);
            List<OrderDAO.OrderLine> orderLines = getOrderLinesFromCart();
            logger.info("Order lines count: " + orderLines.size());
            if (orderLines.isEmpty()) {
                showAlert("Error", "No items in cart to purchase.");
                return;
            }
            double totalAmount = calculateTotalAmount();
            boolean isInstallment = installmentRadio != null && installmentRadio.isSelected();
            double paidAmount = isInstallment ? (totalAmount * 0.2) : totalAmount;
            logger.info("Total amount: " + totalAmount + ", Installment: " + isInstallment + ", Paid: " + paidAmount);
            int userId = SessionStaff.getLoggedInStaffId();
            logger.info("User ID: " + userId);
            OrderDAO orderDAO = new OrderDAO();
            int orderId = orderDAO.createOrder(customerId, isInstallment, paidAmount, orderLines, userId);
            logger.info("Order created with ID: " + orderId);
            if (orderId != -1) {
                if (carConfig != null) updateCarQuantity();
                updateAccessoriesQuantity();
                if (isInstallment) {
                    PaymentDAO paymentDAO = new PaymentDAO();
                    int months = getSelectedInstallmentMonths();
                    boolean installmentCreated = paymentDAO.createInstallmentPlan(orderId, totalAmount, months);
                    logger.info("Installment plan created: " + installmentCreated + " for " + months + " months");
                }
                int carCount = carConfig != null ? 1 : 0;
                int accessoryCount = getAccessoryIdsFromSession().size();
                updateUserTargets(carCount, accessoryCount);
                clearCart();
                currentOrderId = orderId;
                hideCustomerInfoOverlay();
                showSuccessOverlay();
                SessionStaff.getInstance().clearPendingCustomizations();
            } else {
                showAlert("Error", "Failed to create order. Please try again.");
            }
        } catch (Exception e) {
            logger.error("Error in createOrderWithCustomer", e);
            showAlert("Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    private void updateUserTargets(int carCount, int accessoryCount) {
        try {
            int staffId = SessionStaff.getLoggedInStaffId();
            Integer managerId = getManagerId(staffId);
            if (managerId != null) {
                updateSingleUserTarget(staffId, carCount, accessoryCount, false);
                updateManagerTarget(managerId);
            } else {
                updateSingleUserTarget(staffId, carCount, accessoryCount, false);
            }
        } catch (Exception e) {
            logger.error("Error updating user targets", e);
        }
    }

    private Integer getManagerId(int staffId) {
        String sql = "SELECT manager FROM user_workinfo WHERE user_id = ?";
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("manager");
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting manager ID for staff: " + staffId, e);
        }
        return null;
    }

    private void updateSingleUserTarget(int userId, int carCount, int accessoryCount, boolean isManagerUpdate) {
        String selectSql = "SELECT achieve FROM user_target WHERE user_id = ?";
        String updateSql = "UPDATE user_target SET achieve = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement selectPs = conn.prepareStatement(selectSql);
             PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
            selectPs.setInt(1, userId);
            String currentAchieve = "car-0,parts-0";
            try (ResultSet rs = selectPs.executeQuery()) {
                if (rs.next()) {
                    currentAchieve = rs.getString("achieve");
                    if (currentAchieve == null) {
                        currentAchieve = "car-0,parts-0";
                    }
                }
            }
            int currentCars = 0;
            int currentParts = 0;
            try {
                String[] parts = currentAchieve.split(",");
                if (parts.length == 2) {
                    currentCars = Integer.parseInt(parts[0].replace("car-", "").trim());
                    currentParts = Integer.parseInt(parts[1].replace("parts-", "").trim());
                }
            } catch (NumberFormatException e) {
                logger.warn("Error parsing achievement string: " + currentAchieve + ", resetting to 0");
                currentCars = 0;
                currentParts = 0;
            }
            int newCars, newParts;
            if (isManagerUpdate) {
                newCars = carCount;
                newParts = accessoryCount;
            } else {
                newCars = currentCars + carCount;
                newParts = currentParts + accessoryCount;
            }
            String newAchieve = "car-" + newCars + ",parts-" + newParts;
            updatePs.setString(1, newAchieve);
            updatePs.setInt(2, userId);
            int rowsAffected = updatePs.executeUpdate();
            logger.info("Updated user_target for user " + userId + " from '" + currentAchieve + "' to '" + newAchieve + "', rows affected: " + rowsAffected);
        } catch (SQLException e) {
            logger.error("Error updating user_target for user: " + userId, e);
        }
    }

    private void updateManagerTarget(int managerId) {
        String sql = "SELECT achieve FROM user_target WHERE user_id IN (SELECT user_id FROM user_workinfo WHERE manager = ?)";
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, managerId);
            int totalCars = 0;
            int totalParts = 0;
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String staffAchieve = rs.getString("achieve");
                    if (staffAchieve != null) {
                        try {
                            String[] parts = staffAchieve.split(",");
                            if (parts.length == 2) {
                                int staffCars = Integer.parseInt(parts[0].replace("car-", "").trim());
                                int staffParts = Integer.parseInt(parts[1].replace("parts-", "").trim());
                                totalCars += staffCars;
                                totalParts += staffParts;
                            }
                        } catch (NumberFormatException e) {
                            logger.warn("Error parsing staff achievement: " + staffAchieve);
                        }
                    }
                }
            }
            updateSingleUserTarget(managerId, totalCars, totalParts, true);
            logger.info("Updated manager " + managerId + " target with total: car-" + totalCars + ",parts-" + totalParts);
        } catch (SQLException e) {
            logger.error("Error updating manager target for manager: " + managerId, e);
        }
    }

    private boolean verifyPasswordWithStoredProcedure(String enteredPassword) {
        try {
            if (!SessionStaff.isSessionActive()) return false;
            String username = SessionStaff.getLoggedInStaffName();
            if (username == null) return false;
            try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
                 CallableStatement p1 = con.prepareCall("call login(?,?,?)")) {
                p1.setString(1, username);
                p1.setString(2, enteredPassword);
                p1.setString(3, String.valueOf(java.time.LocalDateTime.now()));
                try (ResultSet rs = p1.executeQuery()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        return id != 0;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error verifying password", e);
            return false;
        }
        return false;
    }

    private void setupPaymentOptions() {
        if (fullPaymentRadio != null) fullPaymentRadio.setSelected(true);
        hideInstallmentOptions();
        showGrandTotalDisplay();
    }

    private void setupInstallmentPlans() {
        if (installmentPlanCombo != null) {
            installmentPlanCombo.getItems().addAll(
                    "12 Months - 0% Interest",
                    "24 Months - 3.5% Interest",
                    "36 Months - 5.0% Interest",
                    "48 Months - 6.5% Interest"
            );
            installmentPlanCombo.getSelectionModel().selectFirst();
            installmentPlanCombo.setOnAction(event -> updateInstallmentCalculation());
        }
    }

    private void showInstallmentOptions() {
        if (installmentOptionsContainer != null) {
            installmentOptionsContainer.setVisible(true);
            installmentOptionsContainer.setManaged(true);
            updateInstallmentCalculation();
        }
    }

    private void hideInstallmentOptions() {
        if (installmentOptionsContainer != null) {
            installmentOptionsContainer.setVisible(false);
            installmentOptionsContainer.setManaged(false);
        }
    }

    private void updateInstallmentCalculation() {
        double totalAmount = calculateTotalAmount();
        if (totalAmount == 0) {
            if (downPaymentLabel != null) downPaymentLabel.setText("$0.00");
            if (monthlyPaymentLabel != null) monthlyPaymentLabel.setText("$0.00");
            if (monthsLabel != null) monthsLabel.setText("0 months");
            if (aprLabel != null) aprLabel.setText("0%");
            if (totalInterestLabel != null) totalInterestLabel.setText("$0.00");
            if (totalWithInterestLabel != null) totalWithInterestLabel.setText("$0.00");
            return;
        }
        String selectedPlan = installmentPlanCombo != null ? installmentPlanCombo.getSelectionModel().getSelectedItem() : "12 Months - 0% Interest";
        int months = 12;
        double interestRate = 0.0;
        if (selectedPlan.contains("24")) {
            months = 24;
            interestRate = 0.035;
        } else if (selectedPlan.contains("36")) {
            months = 36;
            interestRate = 0.05;
        } else if (selectedPlan.contains("48")) {
            months = 48;
            interestRate = 0.065;
        }
        double downPayment = totalAmount * 0.2;
        double totalWithInterest = totalAmount * (1 + interestRate);
        double totalInterest = totalWithInterest - totalAmount;
        double monthlyPayment = (totalWithInterest - downPayment) / months;
        if (downPaymentLabel != null) downPaymentLabel.setText(String.format("$%,.2f", downPayment));
        if (monthlyPaymentLabel != null) monthlyPaymentLabel.setText(String.format("$%,.2f", monthlyPayment));
        if (monthsLabel != null) monthsLabel.setText(months + " months");
        if (aprLabel != null) aprLabel.setText(String.format("%.1f%%", interestRate * 100));
        if (totalInterestLabel != null) totalInterestLabel.setText(String.format("$%,.2f", totalInterest));
        if (totalWithInterestLabel != null) totalWithInterestLabel.setText(String.format("$%,.2f", totalWithInterest));
    }

    @FXML
    private void confirmPurchase() {
        showPasswordVerification();
    }

    @FXML
    private void loadAccessories() {
        try {
            String[] possiblePaths = {"/Fxml/staffAsset.fxml", "/View/staffAsset.fxml", "/staffAsset.fxml"};
            Parent root = null;
            for (String path : possiblePaths) {
                try {
                    root = FXMLLoader.load(getClass().getResource(path));
                    break;
                } catch (Exception e) {}
            }
            if (root == null) {
                showAlert("Navigation Error", "Could not find accessories page.");
                return;
            }
            Stage currentStage = (Stage) loadAssetButton.getScene().getWindow();
            currentStage.setScene(new Scene(root));
            currentStage.setTitle("Accessories");
            currentStage.show();
        } catch (Exception e) {
            showAlert("Navigation Error", "Failed to load accessories: " + e.getMessage());
        }
    }

    private void processOrderConfirmation() {
        SessionStaff session = SessionStaff.getInstance();
        if (session.getSelectedAccessories().isEmpty() && carConfig == null) {
            showAlert("Error", "No items in cart to purchase.");
            return;
        }
        showCustomerInfoOverlay();
    }

    private boolean updateCarQuantity() {
        if (carConfig == null) return true;
        try {
            int carId = getCarIdFromConfig();
            CarDAO carDAO = new CarDAO();
            Model.car car = carDAO.getCarById(carId);
            if (car != null) {
                String currentQtyStr = car.getCarQty();
                int currentQty = Integer.parseInt(currentQtyStr);
                int newQty = currentQty - 1;
                if (newQty >= 0) {
                    boolean updated = carDAO.updateCarQuantity(carId, newQty);
                    return updated;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error("Error updating car quantity", e);
            return false;
        }
    }

    private boolean updateAccessoriesQuantity() {
        try {
            List<Integer> accessoryIds = getAccessoryIdsFromSession();
            if (accessoryIds.isEmpty()) return true;
            boolean allUpdated = true;
            for (int accessoryId : accessoryIds) {
                boolean updated = updateSingleAccessoryQuantity(accessoryId);
                if (!updated) allUpdated = false;
            }
            return allUpdated;
        } catch (Exception e) {
            logger.error("Error updating accessories quantity", e);
            return false;
        }
    }

    private boolean updateSingleAccessoryQuantity(int accessoryId) {
        String sql = "UPDATE car_parts SET part_qty = part_qty - 1 WHERE part_id = ? AND part_qty > 0";
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accessoryId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.error("Error updating accessory quantity", e);
            return false;
        }
    }

    private List<Integer> getAccessoryIdsFromSession() {
        List<Integer> accessoryIds = new ArrayList<>();
        try {
            accessoryIds = SessionStaff.getInstance().getSelectedAccessories();
        } catch (Exception e) {
            logger.error("Error getting accessory IDs from session", e);
        }
        return accessoryIds;
    }

    private int getSelectedInstallmentMonths() {
        if (installmentPlanCombo == null) return 12;
        String selectedPlan = installmentPlanCombo.getSelectionModel().getSelectedItem();
        if (selectedPlan.contains("24")) return 24;
        if (selectedPlan.contains("36")) return 36;
        if (selectedPlan.contains("48")) return 48;
        return 12;
    }

    private List<OrderDAO.OrderLine> getOrderLinesFromCart() {
        List<OrderDAO.OrderLine> lines = new ArrayList<>();
        if (carConfig != null) {
            int carId = getCarIdFromConfig();
            lines.add(new OrderDAO.OrderLine(carId, null, 1, carConfig.getTotalPrice()));
        }
        Map<Integer, Integer> itemQuantities = new HashMap<>();
        List<Integer> accessoryIds = getAccessoryIdsFromSession();
        for (int accessoryId : accessoryIds) {
            itemQuantities.put(accessoryId, itemQuantities.getOrDefault(accessoryId, 0) + 1);
        }
        for (Map.Entry<Integer, Integer> entry : itemQuantities.entrySet()) {
            CarPart part = findPartById(entry.getKey());
            if (part != null) {
                lines.add(new OrderDAO.OrderLine(null, entry.getKey(), entry.getValue(), part.getPrice()));
            }
        }
        return lines;
    }

    private double calculateTotalAmount() {
        double total = 0.0;
        if (carConfig != null) total += carConfig.getTotalPrice();
        total += calculateAccessoriesTotal();
        return total;
    }

    private void navigateToStaffWelcome() {
        try {
            String[] possiblePaths = {"/Fxml/staffWelcome.fxml", "/View/staffWelcome.fxml", "/staffWelcome.fxml"};
            Parent root = null;
            for (String path : possiblePaths) {
                try {
                    root = FXMLLoader.load(getClass().getResource(path));
                    break;
                } catch (Exception e) {}
            }
            if (root == null) throw new IOException("Could not find staffWelcome.fxml in any location");
            Stage currentStage = (Stage) homeButton.getScene().getWindow();
            currentStage.setScene(new Scene(root));
            currentStage.setTitle("Welcome");
            currentStage.setFullScreen(true);
            currentStage.show();
        } catch (IOException e) {
            showAlert("Navigation Error", "Failed to return to dashboard: " + e.getMessage());
        }
    }

    private int getCarIdFromConfig() {
        if (carConfig != null) return carConfig.getCarId();
        return -1;
    }

    private void clearCart() {
        SessionStaff.getInstance().clearCart();
    }

    private boolean validateCustomerInputs() {
        if (customerNameField.getText().trim().isEmpty() ||
                nrc_first.getValue() == null ||
                nrc_second.getValue() == null ||
                nrc_third.getValue() == null ||
                nrc_number.getText().trim().isEmpty() ||
                customerPhoneField.getText().trim().isEmpty() ||
                customerAddressField.getText().trim().isEmpty()) {
            customerErrorLabel.setText("Please fill in all required fields");
            customerErrorLabel.setVisible(true);
            return false;
        }

        String phone = customerPhoneField.getText().trim();
        if (phone.length() > 20) {
            showPhoneError("Phone number too long. Maximum 20 characters allowed.");
            return false;
        }

        if (phone.length() < 8) {
            showPhoneError("Phone number too short. Minimum 8 digits required.");
            return false;
        }

        if (!phone.matches("^[+]?[0-9\\s\\-\\(\\)]{8,20}$")) {
            showPhoneError("Invalid phone number format");
            return false;
        }

        return true;
    }

    @FXML
    private void removeCarFromCart() {
        if (carConfig != null) {
            SessionStaff.getInstance().setCarConfiguration(null);
            carConfig = null;
            updatePriceDisplayForAccessoriesOnly();
            updateConfirmButtonState();
            SessionStaff session = SessionStaff.getInstance();
            if (session.getSelectedAccessories().isEmpty()) resetAllPricesToZero();
        }
    }

    @FXML
    private void goBack() {
        String[] possiblePaths;
        SessionStaff session = SessionStaff.getInstance();

        boolean hasAccessories = !session.getSelectedAccessories().isEmpty();

        if (carConfig == null && hasAccessories) {
            possiblePaths = new String[]{"/Fxml/staffAsset.fxml", "/View/staffAsset.fxml", "/staffAsset.fxml"};
        } else if (carConfig != null) {
            possiblePaths = new String[]{"/Fxml/staffCustomize.fxml", "/View/staffCustomize.fxml", "/staffCustomize.fxml"};
        } else {
            possiblePaths = new String[]{"/Fxml/staffWelcome.fxml", "/View/staffWelcome.fxml", "/staffWelcome.fxml"};
        }

        Parent root = null;

        for (String path : possiblePaths) {
            try {
                root = FXMLLoader.load(getClass().getResource(path));
                break;
            } catch (Exception ignored) {}
        }

        // Fallback navigation
        if (root == null) {
            navigateToStaffWelcome();
            return;
        }

        Stage currentStage = (Stage) backButton.getScene().getWindow();
        Scene scene = new Scene(root);

        currentStage.setScene(scene);
        currentStage.setTitle("Navigation");

        // Enable fullscreen
        currentStage.setFullScreen(true);

        currentStage.centerOnScreen();
        currentStage.show();
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}