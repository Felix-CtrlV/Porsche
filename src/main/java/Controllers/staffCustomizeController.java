package Controllers;

import DAO.CarDAO;
import Model.car;
import Model.CarConfiguration;
import Model.CustomizationOption;
import Utils.SessionStaff;
import Utils.DarkModeManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class staffCustomizeController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(staffCustomizeController.class);
    private static final double IMAGE_FIT_HEIGHT = 100.0;

    @FXML private StackPane rootPane;
    @FXML private ImageView modelImage;
    @FXML private Label carDescription;
    @FXML private Button confirmBtn;
    @FXML private Label modelName;
    @FXML private Label modelPrice;
    @FXML private Label fuelType;
    @FXML private Button backBtn;
    @FXML private FlowPane colorContainer;
    @FXML private FlowPane interiorContainer;
    @FXML private Label carSpeedLabel;
    @FXML private Label productionYearLabel;

    private final List<CustomizationOption> colorOptions = new ArrayList<>();
    private final List<CustomizationOption> interiorOptions = new ArrayList<>();
    private CarConfiguration carConfig;
    private CarDAO carDAO;

    private VBox selectedColorOption;
    private VBox selectedInteriorOption;
    private List<car> allCarsForModel;
    private car selectedCarForOrder;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        LOGGER.info("Initializing staffCustomizeController");
        carDAO = new CarDAO();

        if (rootPane != null) {
            rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    DarkModeManager.getInstance().registerScene(newScene);
                }
            });
        }

        confirmBtn.setDisable(true);

        CarConfiguration savedConfig = SessionStaff.getInstance().getCarConfiguration();

        if (savedConfig != null && savedConfig.getBaseCar() != null) {
            carConfig = savedConfig;
            loadAllCarsForModel();
            updateCarDisplay();
            loadCustomizationOptions();
            displayAllOptions();

            // FIX: Restore selections after UI is built
            restoreSelectedOptions();
        }
    }

    public void setSelectedCar(car selectedCar) {
        if (selectedCar != null) {
            carConfig = new CarConfiguration(selectedCar);
            SessionStaff.getInstance().setCarConfiguration(carConfig);
            selectedCarForOrder = selectedCar;
            loadAllCarsForModel();
            updateCarDisplay();
            loadCustomizationOptions();
            displayAllOptions();

            // FIX: Restore selections after UI is built
            restoreSelectedOptions();
        }
    }

    // ADD THIS METHOD TO RESTORE SELECTIONS
    private void restoreSelectedOptions() {
        if (carConfig == null) return;

        LOGGER.info("Restoring selected options - Color: {}, Interior: {}",
                carConfig.getSelectedColor() != null ? carConfig.getSelectedColor().getName() : "null",
                carConfig.getSelectedInterior() != null ? carConfig.getSelectedInterior().getName() : "null");

        // Restore color selection
        if (carConfig.getSelectedColor() != null) {
            for (int i = 0; i < colorContainer.getChildren().size(); i++) {
                Node node = colorContainer.getChildren().get(i);
                if (node instanceof VBox) {
                    VBox optionBox = (VBox) node;
                    CustomizationOption option = getOptionFromVBox(optionBox);
                    if (option != null && isSameOption(option, carConfig.getSelectedColor())) {
                        selectOption(optionBox, option, "color");
                        break;
                    }
                }
            }
        }

        // Restore interior selection
        if (carConfig.getSelectedInterior() != null) {
            for (int i = 0; i < interiorContainer.getChildren().size(); i++) {
                Node node = interiorContainer.getChildren().get(i);
                if (node instanceof VBox) {
                    VBox optionBox = (VBox) node;
                    CustomizationOption option = getOptionFromVBox(optionBox);
                    if (option != null && isSameOption(option, carConfig.getSelectedInterior())) {
                        selectOption(optionBox, option, "interior");
                        break;
                    }
                }
            }
        }

        // Enable confirm button if both selections are valid
        if (carConfig.getSelectedColor() != null && carConfig.getSelectedInterior() != null) {
            confirmBtn.setDisable(false);
        }
    }

    // ADD THIS HELPER METHOD TO GET OPTION FROM VBOX
    private CustomizationOption getOptionFromVBox(VBox optionBox) {
        String optionName = null;

        // Find the name label in the VBox
        for (Node node : optionBox.getChildren()) {
            if (node instanceof Label) {
                Label label = (Label) node;
                if (label.getStyleClass().contains("option-name")) {
                    optionName = label.getText();
                    break;
                }
            }
        }

        if (optionName != null) {
            // Search in color options
            for (CustomizationOption option : colorOptions) {
                if (option.getName().equals(optionName)) {
                    return option;
                }
            }

            // Search in interior options
            for (CustomizationOption option : interiorOptions) {
                if (option.getName().equals(optionName)) {
                    return option;
                }
            }
        }

        return null;
    }

    // ADD THIS HELPER METHOD TO COMPARE OPTIONS
    private boolean isSameOption(CustomizationOption option1, CustomizationOption option2) {
        if (option1 == null || option2 == null) return false;
        return option1.getName().equals(option2.getName()) &&
                option1.getPrice() == option2.getPrice();
    }

    private void loadAllCarsForModel() {
        if (carConfig == null || carConfig.getBaseCar() == null) return;

        car baseCar = carConfig.getBaseCar();
        String modelName = baseCar.getModelName();
        String trimName = baseCar.getTrimName();

        LOGGER.info("Loading all cars for model: {}, trim: {}", modelName, trimName);

        List<car> allCars = carDAO.getAllCars();
        allCarsForModel = new ArrayList<>();

        for (car car : allCars) {
            if (car.getModelName().equals(modelName) && car.getTrimName().equals(trimName)) {
                allCarsForModel.add(car);
            }
        }

        LOGGER.info("Found {} cars for model {} and trim {}", allCarsForModel.size(), modelName, trimName);
    }

    private void updateCarDisplay() {
        if (carConfig == null || carConfig.getBaseCar() == null) return;

        car baseCar = carConfig.getBaseCar();

        String fullModelName = baseCar.getModelName();
        if (baseCar.getTrimName() != null && !baseCar.getTrimName().isEmpty()) {
            fullModelName += " " + baseCar.getTrimName();
        }
        modelName.setText(fullModelName);

        modelPrice.setText(String.format("$%,.0f", carConfig.getTotalPrice()));

        if (baseCar.getCarDescription() != null && !baseCar.getCarDescription().isEmpty()) {
            carDescription.setText(baseCar.getCarDescription());
        } else {
            carDescription.setText("");
        }

        if (baseCar.getFuelType() != null) {
            fuelType.setText(baseCar.getFuelType());
        }

        if (carSpeedLabel != null) {
            carSpeedLabel.setText(baseCar.getCarSpeed() + " km/h");
        }

        if (productionYearLabel != null) {
            productionYearLabel.setText(String.valueOf(baseCar.getProductionYear()));
        }

        if (modelImage != null && baseCar.getCarPhoto() != null) {
            Image img = loadCarImage(baseCar.getCarPhoto());
            if (img != null) modelImage.setImage(img);
            else loadDefaultCarImage();
        } else {
            loadDefaultCarImage();
        }
    }

    private Image loadCarImage(String path) {
        try {
            File f = new File(path);
            if (f.exists()) return new Image(new FileInputStream(f));
        } catch (Exception ignored) {}
        return null;
    }

    private void loadDefaultCarImage() {
        try {
            InputStream stream = getClass().getResourceAsStream("/Image/911_select_model.png");
            if (stream != null) modelImage.setImage(new Image(stream));
        } catch (Exception ignored) {}
    }

    private void loadCustomizationOptions() {
        colorOptions.clear();
        interiorOptions.clear();

        if (allCarsForModel == null || allCarsForModel.isEmpty()) return;

        String baseCarColor = carConfig.getBaseCar().getCarColor();

        boolean hasAvailableBlackColor = false;
        boolean hasAvailableRedColor = false;

        for (car car : allCarsForModel) {
            if (isCarAvailable(car)) {
                String carColor = car.getCarColor();
                if (carColor != null) {
                    if (carColor.equalsIgnoreCase("black")) {
                        hasAvailableBlackColor = true;
                    } else if (carColor.equalsIgnoreCase("red")) {
                        hasAvailableRedColor = true;
                    }
                }
            }
        }

        LOGGER.info("Color availability - Black: {}, Red: {}", hasAvailableBlackColor, hasAvailableRedColor);
        LOGGER.info("Base car color: {}", baseCarColor);

        // FIX: Always show default option
        colorOptions.add(new CustomizationOption("Default", 0, loadColorOption("⊘"), true));

        // FIX: Only show black color option if base car is NOT black
        if (baseCarColor == null || !baseCarColor.equalsIgnoreCase("black")) {
            CustomizationOption blackOption = new CustomizationOption("Jet Black Metallic", 3200, loadColorOption("/Image/black.jpg"), hasAvailableBlackColor);
            colorOptions.add(blackOption);
            LOGGER.info("Added black color option (base car is not black)");
        } else {
            LOGGER.info("Skipping black color option (base car is already black)");
        }

        // FIX: Only show red color option if base car is NOT red
        if (baseCarColor == null || !baseCarColor.equalsIgnoreCase("red")) {
            CustomizationOption redOption = new CustomizationOption("Guards Red", 4500, loadColorOption("/Image/red.jpg"), hasAvailableRedColor);
            colorOptions.add(redOption);
            LOGGER.info("Added red color option (base car is not red)");
        } else {
            LOGGER.info("Skipping red color option (base car is already red)");
        }

        // FIX: If we have a selected color that might not be in the standard options, ensure it's included
        if (carConfig.getSelectedColor() != null) {
            boolean foundSelectedColor = false;
            for (CustomizationOption option : colorOptions) {
                if (isSameOption(option, carConfig.getSelectedColor())) {
                    foundSelectedColor = true;
                    break;
                }
            }

            // If selected color is not in the options, add it back (this handles edge cases)
            if (!foundSelectedColor) {
                LOGGER.info("Selected color {} not found in options, adding it back", carConfig.getSelectedColor().getName());
                CustomizationOption selectedColor = carConfig.getSelectedColor();
                if (selectedColor.getName().equals("Jet Black Metallic")) {
                    // Add black option back but mark as unavailable
                    CustomizationOption unavailableBlack = new CustomizationOption("Jet Black Metallic", 3200, loadColorOption("/Image/black.jpg"), false);
                    colorOptions.add(unavailableBlack);
                } else if (selectedColor.getName().equals("Guards Red")) {
                    // Add red option back but mark as unavailable
                    CustomizationOption unavailableRed = new CustomizationOption("Guards Red", 4500, loadColorOption("/Image/red.jpg"), false);
                    colorOptions.add(unavailableRed);
                }
            }
        }

        updateInteriorOptions();

        // Only auto-select if no selection exists
        if (carConfig.getSelectedColor() == null) {
            carConfig.setSelectedColor(colorOptions.get(0));
        }
        if (carConfig.getSelectedInterior() == null) {
            autoSelectInteriorOption();
        }
    }

    private void updateInteriorOptions() {
        interiorOptions.clear();

        if (allCarsForModel == null || allCarsForModel.isEmpty()) return;

        CustomizationOption selectedColor = carConfig.getSelectedColor();
        String targetColor = getTargetColorFromOption(selectedColor);

        boolean hasAvailableBlackInterior = false;
        boolean hasAvailableRedInterior = false;

        LOGGER.info("Checking interior availability for color: '{}'", targetColor);

        for (car car : allCarsForModel) {
            if (isCarAvailable(car)) {
                String carColor = car.getCarColor();
                String interiorColor = car.getInteriorColor();

                boolean colorMatches;
                if (targetColor.equals("base")) {
                    colorMatches = carColor != null && carColor.equalsIgnoreCase(carConfig.getBaseCar().getCarColor());
                } else {
                    colorMatches = targetColor.isEmpty() ||
                            (carColor != null && carColor.equalsIgnoreCase(targetColor));
                }

                if (colorMatches && interiorColor != null) {
                    if (interiorColor.equalsIgnoreCase("black")) {
                        hasAvailableBlackInterior = true;
                    } else if (interiorColor.equalsIgnoreCase("two_tone")) {
                        hasAvailableRedInterior = true;
                    }
                }
            }
        }

        // FIX: Always show both interior options, just mark availability
        interiorOptions.add(new CustomizationOption("Standard Black Leather", 0, loadPlaceholderImageForOption(), hasAvailableBlackInterior));
        interiorOptions.add(new CustomizationOption("Exclusive Two-Tone Leather", 7500, loadPlaceholderImageForOption(), hasAvailableRedInterior));

        // FIX: If we have a selected interior that might not match current availability, ensure it stays selected
        if (carConfig.getSelectedInterior() != null) {
            boolean foundSelectedInterior = false;
            for (CustomizationOption option : interiorOptions) {
                if (isSameOption(option, carConfig.getSelectedInterior())) {
                    foundSelectedInterior = true;
                    break;
                }
            }

            // If selected interior is not in options but we want to keep it, update availability
            if (!foundSelectedInterior) {
                LOGGER.info("Selected interior {} not perfectly matching, ensuring availability reflects selection", carConfig.getSelectedInterior().getName());
                // The interior options are always the same names, so this should rarely happen
            }
        }
    }

    private void autoSelectInteriorOption() {
        String baseCarInterior = carConfig.getBaseCar().getInteriorColor();

        if (baseCarInterior != null) {
            if (baseCarInterior.equalsIgnoreCase("black")) {
                for (CustomizationOption option : interiorOptions) {
                    if (option.getName().equals("Standard Black Leather") && option.isAvailable()) {
                        carConfig.setSelectedInterior(option);
                        return;
                    }
                }
            } else if (baseCarInterior.equalsIgnoreCase("two_tone")) {
                for (CustomizationOption option : interiorOptions) {
                    if (option.getName().equals("Exclusive Two-Tone Leather") && option.isAvailable()) {
                        carConfig.setSelectedInterior(option);
                        return;
                    }
                }
            }
        }

        if (!interiorOptions.isEmpty()) {
            for (CustomizationOption option : interiorOptions) {
                if (option.isAvailable()) {
                    carConfig.setSelectedInterior(option);
                    return;
                }
            }
            // If none available, still select the first one but it will show as unavailable
            carConfig.setSelectedInterior(interiorOptions.get(0));
        }
    }

    private boolean isCarAvailable(car car) {
        try {
            int qty = Integer.parseInt(car.getCarQty());
            return qty > 0;
        } catch (NumberFormatException e) {
            LOGGER.warn("Invalid quantity format for car ID: {}", car.getCarId());
            return false;
        }
    }

    private String getTargetColorFromOption(CustomizationOption colorOption) {
        if (colorOption == null || colorOption.getName().equals("Default")) {
            return "base";
        } else if (colorOption.getName().contains("Black")) {
            return "black";
        } else if (colorOption.getName().contains("Red")) {
            return "red";
        }
        return "";
    }

    private Image loadColorOption(String path) {
        if (path.equals("⊘")) {
            return null;
        }
        try {
            InputStream s = getClass().getResourceAsStream(path);
            if (s != null) return new Image(s);
        } catch (Exception ignored) {}
        return loadPlaceholderImageForOption();
    }

    private Image loadPlaceholderImageForOption() {
        try {
            InputStream stream = getClass().getResourceAsStream("/Images/placeholder.png");
            if (stream != null) return new Image(stream);
        } catch (Exception ignored) {}
        return null;
    }

    private void displayAllOptions() {
        displayOptions(colorContainer, colorOptions, "color");
        displayOptions(interiorContainer, interiorOptions, "interior");
    }

    private void displayOptions(FlowPane container, List<CustomizationOption> options, String type) {
        container.getChildren().clear();

        for (CustomizationOption option : options) {
            VBox optionBox = createOptionBox(option, type);
            container.getChildren().add(optionBox);
        }
    }

    private VBox createOptionBox(CustomizationOption option, String type) {
        VBox optionBox = new VBox();
        optionBox.setAlignment(Pos.CENTER);
        optionBox.setSpacing(5);
        optionBox.getStyleClass().add("option-container");
        optionBox.getStyleClass().add(type + "-option");

        // FIX: Add faint grey border to all option boxes for better visual appeal
        optionBox.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 1px; -fx-border-radius: 8px;");

        // FIX: Apply unavailable styling and make unclickable
        if (!option.isAvailable()) {
            optionBox.getStyleClass().add("unavailable-option");
            optionBox.setDisable(true); // Make unavailable options unclickable
        } else {
            optionBox.getStyleClass().add("available-option");
            optionBox.setDisable(false);
        }

        if (option.getImage() != null) {
            ImageView img = new ImageView(option.getImage());
            img.setFitHeight(IMAGE_FIT_HEIGHT);
            img.setPreserveRatio(true);
            img.getStyleClass().add("option-image-container");
            // FIX: Add border to image container as well
            img.setStyle("-fx-border-color: #f0f0f0; -fx-border-width: 1px; -fx-border-radius: 6px;");
            optionBox.getChildren().add(img);
        }

        Label nameLabel = new Label(option.getName());
        nameLabel.getStyleClass().add("option-name");
        optionBox.getChildren().add(nameLabel);

        Label priceLabel = new Label(option.getFormattedPrice());
        if (option.getPrice() > 0) {
            priceLabel.getStyleClass().add("premium-price");
        } else {
            priceLabel.getStyleClass().add("standard-price");
        }
        optionBox.getChildren().add(priceLabel);

        // FIX: Only add click handler for available options
        if (option.isAvailable()) {
            optionBox.setOnMouseClicked(event -> {
                selectOption(optionBox, option, type);
            });
        }

        return optionBox;
    }

    private void selectOption(VBox optionBox, CustomizationOption option, String type) {
        // FIX: Don't allow selecting unavailable options
        if (!option.isAvailable()) {
            LOGGER.warn("Attempted to select unavailable option: {}", option.getName());
            return;
        }

        if ("color".equals(type)) {
            if (selectedColorOption != null) {
                selectedColorOption.getStyleClass().remove("selected");
                updateLabelsStyle(selectedColorOption, false);
            }
            selectedColorOption = optionBox;
            carConfig.setSelectedColor(option);

            // FIX: Update session immediately
            SessionStaff.getInstance().setCarConfiguration(carConfig);

            updateInteriorOptions();

            trackCustomization(option, "c_color");

            if (option.getName().equals("Default")) {
                validateCurrentInteriorSelection();
            } else {
                autoSelectInteriorOption();
            }

            displayOptions(interiorContainer, interiorOptions, "interior");

            // FIX: Restore interior selection after rebuilding interior options
            if (carConfig.getSelectedInterior() != null) {
                restoreInteriorSelection();
            }
        } else if ("interior".equals(type)) {
            if (selectedInteriorOption != null) {
                selectedInteriorOption.getStyleClass().remove("selected");
                updateLabelsStyle(selectedInteriorOption, false);
            }
            selectedInteriorOption = optionBox;
            carConfig.setSelectedInterior(option);

            // FIX: Update session immediately
            SessionStaff.getInstance().setCarConfiguration(carConfig);

            trackCustomization(option, "i_color");

            confirmBtn.setDisable(false);
        }

        optionBox.getStyleClass().add("selected");
        updateLabelsStyle(optionBox, true);

        updateCarImageBasedOnSelection();
        updateTotalPrice();
    }

    // ADD THIS METHOD TO RESTORE INTERIOR SELECTION
    private void restoreInteriorSelection() {
        if (carConfig.getSelectedInterior() != null) {
            for (int i = 0; i < interiorContainer.getChildren().size(); i++) {
                Node node = interiorContainer.getChildren().get(i);
                if (node instanceof VBox) {
                    VBox optionBox = (VBox) node;
                    CustomizationOption option = getOptionFromVBox(optionBox);
                    if (option != null && isSameOption(option, carConfig.getSelectedInterior())) {
                        selectOption(optionBox, option, "interior");
                        break;
                    }
                }
            }
        }
    }

    private void trackCustomization(CustomizationOption option, String customizationType) {
        if (option.getPrice() > 0) {
            List<SessionStaff.CustomizationRecord> currentCustomizations = SessionStaff.getInstance().getPendingCustomizations();
            List<SessionStaff.CustomizationRecord> updatedCustomizations = new ArrayList<>();

            boolean found = false;
            for (SessionStaff.CustomizationRecord cust : currentCustomizations) {
                if (!cust.getType().equals(customizationType)) {
                    updatedCustomizations.add(cust);
                } else {
                    found = true;
                }
            }

            if (!found) {
                updatedCustomizations.add(new SessionStaff.CustomizationRecord(customizationType, "1", option.getPrice()));
            }

            SessionStaff.getInstance().setPendingCustomizations(updatedCustomizations);
        } else {
            List<SessionStaff.CustomizationRecord> currentCustomizations = SessionStaff.getInstance().getPendingCustomizations();
            List<SessionStaff.CustomizationRecord> updatedCustomizations = new ArrayList<>();

            for (SessionStaff.CustomizationRecord cust : currentCustomizations) {
                if (!cust.getType().equals(customizationType)) {
                    updatedCustomizations.add(cust);
                }
            }

            SessionStaff.getInstance().setPendingCustomizations(updatedCustomizations);
        }
    }

    private void validateCurrentInteriorSelection() {
        CustomizationOption currentInterior = carConfig.getSelectedInterior();
        if (currentInterior != null) {
            String baseCarColor = carConfig.getBaseCar().getCarColor();
            boolean isInteriorAvailableForBaseCar = false;

            for (car car : allCarsForModel) {
                if (isCarAvailable(car)) {
                    String carColor = car.getCarColor();
                    String interiorColor = car.getInteriorColor();

                    boolean colorMatches = carColor != null && carColor.equalsIgnoreCase(baseCarColor);
                    boolean interiorMatches = false;

                    if (currentInterior.getName().equals("Standard Black Leather")) {
                        interiorMatches = interiorColor != null && interiorColor.equalsIgnoreCase("black");
                    } else if (currentInterior.getName().equals("Exclusive Two-Tone Leather")) {
                        interiorMatches = interiorColor != null && interiorColor.equalsIgnoreCase("two_tone");
                    }

                    if (colorMatches && interiorMatches) {
                        isInteriorAvailableForBaseCar = true;
                        break;
                    }
                }
            }

            if (!isInteriorAvailableForBaseCar) {
                autoSelectInteriorOption();
            }
        }
    }

    private void updateCarImageBasedOnSelection() {
        if (carConfig == null || carConfig.getBaseCar() == null || allCarsForModel == null) return;

        CustomizationOption selectedColor = carConfig.getSelectedColor();
        CustomizationOption selectedInterior = carConfig.getSelectedInterior();

        String targetColor = getTargetColorFromOption(selectedColor);
        String targetInterior = getTargetInteriorFromOption(selectedInterior);

        for (car car : allCarsForModel) {
            if (isCarAvailable(car)) {
                String carColor = car.getCarColor();

                boolean colorMatch;
                if (targetColor.equals("base")) {
                    colorMatch = carColor != null && carColor.equalsIgnoreCase(carConfig.getBaseCar().getCarColor());
                } else {
                    colorMatch = targetColor.isEmpty() ||
                            (carColor != null && carColor.equalsIgnoreCase(targetColor));
                }

                boolean interiorMatch = targetInterior.isEmpty() ||
                        (car.getInteriorColor() != null && car.getInteriorColor().equalsIgnoreCase(targetInterior));

                if (colorMatch && interiorMatch) {
                    Image img = loadCarImage(car.getCarPhoto());
                    if (img != null) {
                        modelImage.setImage(img);
                        selectedCarForOrder = car;
                        return;
                    }
                }
            }
        }

        car baseCar = carConfig.getBaseCar();
        if (baseCar.getCarPhoto() != null) {
            Image img = loadCarImage(baseCar.getCarPhoto());
            if (img != null) {
                modelImage.setImage(img);
                selectedCarForOrder = baseCar;
                return;
            }
        }

        loadDefaultCarImage();
        selectedCarForOrder = carConfig.getBaseCar();
    }

    private String getTargetInteriorFromOption(CustomizationOption interiorOption) {
        if (interiorOption == null || interiorOption.getName().equals("Standard Black Leather")) {
            return "";
        } else if (interiorOption.getName().contains("Two-Tone")) {
            return "two_tone";
        }
        return "";
    }

    private void updateLabelsStyle(VBox optionBox, boolean selected) {
        for (Node node : optionBox.getChildren()) {
            if (node instanceof Label) {
                Label label = (Label) node;
                if (selected) {
                    label.getStyleClass().add("selected");
                } else {
                    label.getStyleClass().remove("selected");
                }
            }
        }
    }

    private void updateTotalPrice() {
        if (carConfig != null) {
            carConfig.updateCarPrice();
            double totalPrice = carConfig.getTotalPrice();
            modelPrice.setText(String.format("$%,.0f", totalPrice));
        }
    }

    @FXML
    private void handleConfirmOrder() {
        try {
            carConfig.updateCarPrice();

            if (selectedCarForOrder != null) {
                carConfig.setBaseCar(selectedCarForOrder);
                SessionStaff.getInstance().setCarConfiguration(carConfig);
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/staffShopingcart.fxml"));
            Parent root = loader.load();

            Window w = confirmBtn.getScene().getWindow();
            Scene scene = new Scene(root);

            // Dark mode support
            DarkModeManager.getInstance().registerScene(scene);

            if (w instanceof Stage stage) {
                stage.setScene(scene);

                // Make fullscreen
                stage.setFullScreen(true);

                stage.centerOnScreen();
            }

        } catch (Exception e) {
            LOGGER.error("Failed to proceed to shopping cart page", e);
        }
    }


    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/staffModelSelect.fxml"));
            Parent root = loader.load();

            Window w = ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);

            // Dark mode support
            DarkModeManager.getInstance().registerScene(scene);

            if (w instanceof Stage stage) {
                stage.setScene(scene);

                // Auto fullscreen
                stage.setFullScreen(true);

                stage.centerOnScreen();
            }

        } catch (IOException e) {
            LOGGER.error("Back navigation failed", e);
        }
    }

}