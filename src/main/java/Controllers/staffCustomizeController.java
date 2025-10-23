package Controllers;

import Model.car;
import Model.CarConfiguration;
import Model.CustomizationOption;
import Utils.SessionStaff;
import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class staffCustomizeController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(staffCustomizeController.class.getName());
    private static final Duration ANIMATION_DURATION = Duration.millis(350);
    private static final int SLIDE_DISTANCE = 150;
    private static final double IMAGE_FIT_HEIGHT = 100.0;

    @FXML private ImageView modelImage;
    @FXML private Label carDescription;
    @FXML private Button confirmBtn;
    @FXML private Label modelName;
    @FXML private Label modelPrice;
    @FXML private Label carFrameNum;
    @FXML private Label fuelType;
    @FXML private Button backBtn;
    @FXML private Button wheelsLeftBtn;
    @FXML private Button wheelsRightBtn;
    @FXML private ScrollPane wheelsScroll;
    @FXML private Label wheelPriceLabel;
    @FXML private Button colorLeftBtn;
    @FXML private Button colorRightBtn;
    @FXML private ScrollPane colorScroll;
    @FXML private Label colorPriceLabel;
    @FXML private Button interiorLeftBtn;
    @FXML private Button interiorRightBtn;
    @FXML private ScrollPane interiorScroll;
    @FXML private Label interiorPriceLabel;

    private final List<CustomizationOption> wheelOptions = new ArrayList<>();
    private final List<CustomizationOption> colorOptions = new ArrayList<>();
    private final List<CustomizationOption> interiorOptions = new ArrayList<>();

    private int currentWheelIndex = 0;
    private int currentColorIndex = 0;
    private int currentInteriorIndex = 0;

    private CarConfiguration carConfig;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        CarConfiguration savedConfig = SessionStaff.getInstance().getCarConfiguration();

        if (savedConfig != null) {
            carConfig = savedConfig;
        } else {
            initializeCarConfiguration();
        }

        loadCustomizationOptions();
        displayInitialSelections();
    }

    private void initializeCarConfiguration() {
        car newCar = new car();
        newCar.setCarid(1);
        newCar.setModelid(911);
        newCar.setQuantity(1);
        newCar.setColor("Carrara White");
        newCar.setProduction_year(2024);
        newCar.setCurrent_price(140000.0);
        newCar.setStatus("available");

        carConfig = new CarConfiguration(newCar);
        carConfig.setModelName("Carrera GTS");
        carConfig.setModelImagePath("/Image/911_select_model.png");
        carConfig.setFrameNumber("911");
        carConfig.setFuelType("GASOLINE");
        carConfig.setDescription(
                "The 911 Carrera GTS epitomizes precision and driving passion. " +
                        "Its naturally aspirated flat-six, razor-sharp handling, and track-focused " +
                        "aerodynamics deliver uncompromising performance—every roar and shift a " +
                        "statement of speed, control, and automotive excellence."
        );

        updateCarDisplay();
    }

    private void updateCarDisplay() {
        modelName.setText(carConfig.getModelName());
        modelPrice.setText(String.format("$%,d", (int) carConfig.getBasePrice()));
        carDescription.setText(carConfig.getDescription());
        carFrameNum.setText(carConfig.getFrameNumber());
        fuelType.setText(carConfig.getFuelType());
    }

    private void loadCustomizationOptions() {
        wheelOptions.add(new CustomizationOption("18-inch Standard Wheels", 0, loadImage("/Image/rim 1.png"), true));
        wheelOptions.add(new CustomizationOption("20-inch Carrera S Wheels", 2500, loadImage("/Image/rim 2.png"), false));
        wheelOptions.add(new CustomizationOption("21-inch RS Spyder Design", 4200, loadImage("/Image/rim 3.png"), false));

        colorOptions.add(new CustomizationOption("Carrara White", 0, loadImage("/Image/white.jpg"), true));
        colorOptions.add(new CustomizationOption("Jet Black Metallic", 3200, loadImage("/Image/black.jpg"), false));
        colorOptions.add(new CustomizationOption("Guards Red", 4500, loadImage("/Image/red.jpg"), false));

        interiorOptions.add(new CustomizationOption("Standard Black Leather", 0, loadImage("/Image/interior 1.jpg"), true));
        interiorOptions.add(new CustomizationOption("Exclusive Two-Tone Leather", 7500, loadImage("/Image/interior 2.jpg"), false));

        carConfig.setSelectedWheel(wheelOptions.get(0));
        carConfig.setSelectedColor(colorOptions.get(0));
        carConfig.setSelectedInterior(interiorOptions.get(0));
    }

    private Image loadImage(String path) {
        try {
            return new Image(Objects.requireNonNull(getClass().getResourceAsStream(path)));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load image: " + path, e);
            return null;
        }
    }

    private void displayInitialSelections() {
        if (!wheelOptions.isEmpty()) {
            displayOptionWithPrice(wheelsScroll, wheelOptions.get(currentWheelIndex), wheelPriceLabel);
        }
        if (!colorOptions.isEmpty()) {
            displayOptionWithPrice(colorScroll, colorOptions.get(currentColorIndex), colorPriceLabel);
        }
        if (!interiorOptions.isEmpty()) {
            displayOptionWithPrice(interiorScroll, interiorOptions.get(currentInteriorIndex), interiorPriceLabel);
        }
    }

    private void displayOptionWithPrice(ScrollPane scrollPane, CustomizationOption option, Label priceLabel) {
        if (option == null || option.getImage() == null || scrollPane == null) return;

        FlowPane container = (FlowPane) scrollPane.getContent();
        container.getChildren().clear();

        ImageView imageView = new ImageView(option.getImage());
        imageView.setFitHeight(IMAGE_FIT_HEIGHT);
        imageView.setPreserveRatio(true);

        container.setAlignment(Pos.CENTER);
        container.getChildren().add(imageView);

        if (priceLabel != null) {
            priceLabel.setText(option.getFormattedPrice());
            if (option.isStandard()) {
                priceLabel.setStyle("-fx-text-fill: #757575;");
            } else {
                priceLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: 600;");
            }
        }
    }

    @FXML
    private void handleWheelsLeft(ActionEvent event) {
        navigateSelection(wheelsScroll, wheelOptions, currentWheelIndex, -1, wheelPriceLabel,
                index -> {
                    currentWheelIndex = index;
                    carConfig.setSelectedWheel(wheelOptions.get(index));
                });
    }

    @FXML
    private void handleWheelsRight(ActionEvent event) {
        navigateSelection(wheelsScroll, wheelOptions, currentWheelIndex, 1, wheelPriceLabel,
                index -> {
                    currentWheelIndex = index;
                    carConfig.setSelectedWheel(wheelOptions.get(index));
                });
    }

    @FXML
    private void handleColorLeft(ActionEvent event) {
        navigateSelection(colorScroll, colorOptions, currentColorIndex, -1, colorPriceLabel,
                index -> {
                    currentColorIndex = index;
                    carConfig.setSelectedColor(colorOptions.get(index));
                });
    }

    @FXML
    private void handleColorRight(ActionEvent event) {
        navigateSelection(colorScroll, colorOptions, currentColorIndex, 1, colorPriceLabel,
                index -> {
                    currentColorIndex = index;
                    carConfig.setSelectedColor(colorOptions.get(index));
                });
    }

    @FXML
    private void handleInteriorLeft(ActionEvent event) {
        navigateSelection(interiorScroll, interiorOptions, currentInteriorIndex, -1, interiorPriceLabel,
                index -> {
                    currentInteriorIndex = index;
                    carConfig.setSelectedInterior(interiorOptions.get(index));
                });
    }

    @FXML
    private void handleInteriorRight(ActionEvent event) {
        navigateSelection(interiorScroll, interiorOptions, currentInteriorIndex, 1, interiorPriceLabel,
                index -> {
                    currentInteriorIndex = index;
                    carConfig.setSelectedInterior(interiorOptions.get(index));
                });
    }

    private void navigateSelection(ScrollPane scrollPane, List<CustomizationOption> optionList,
                                   int currentIndex, int direction, Label priceLabel, IndexUpdater updater) {
        if (optionList.isEmpty() || scrollPane == null) return;

        int newIndex = (currentIndex + direction + optionList.size()) % optionList.size();
        CustomizationOption newOption = optionList.get(newIndex);

        animateImageTransition(scrollPane, newOption.getImage(), direction,
                () -> {
                    updater.update(newIndex);
                    if (priceLabel != null) {
                        priceLabel.setText(newOption.getFormattedPrice());
                        if (newOption.isStandard()) {
                            priceLabel.setStyle("-fx-text-fill: #757575;");
                        } else {
                            priceLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: 600;");
                        }
                    }
                });
    }

    private void animateImageTransition(ScrollPane scrollPane, Image newImage, int direction, Runnable onComplete) {
        FlowPane container = (FlowPane) scrollPane.getContent();
        if (container.getChildren().isEmpty()) return;

        ImageView currentImageView = (ImageView) container.getChildren().get(0);

        ImageView nextImageView = new ImageView(newImage);
        nextImageView.setFitHeight(IMAGE_FIT_HEIGHT);
        nextImageView.setPreserveRatio(true);
        nextImageView.setOpacity(0);
        nextImageView.setTranslateX(direction * SLIDE_DISTANCE);

        container.getChildren().add(nextImageView);

        TranslateTransition slideOutCurrent = new TranslateTransition(ANIMATION_DURATION, currentImageView);
        slideOutCurrent.setToX(-direction * SLIDE_DISTANCE);

        FadeTransition fadeOutCurrent = new FadeTransition(ANIMATION_DURATION, currentImageView);
        fadeOutCurrent.setToValue(0);

        TranslateTransition slideInNext = new TranslateTransition(ANIMATION_DURATION, nextImageView);
        slideInNext.setToX(0);

        FadeTransition fadeInNext = new FadeTransition(ANIMATION_DURATION, nextImageView);
        fadeInNext.setToValue(1);

        ParallelTransition transition = new ParallelTransition(slideOutCurrent, fadeOutCurrent, slideInNext, fadeInNext);

        transition.setOnFinished(e -> {
            container.getChildren().clear();
            container.getChildren().add(nextImageView);
            if (onComplete != null) {
                onComplete.run();
            }
        });

        transition.play();
    }

    @FXML
    private void handleConfirmOrder(ActionEvent event) {
        carConfig.updateCarPrice();
        SessionStaff.getInstance().setCarConfiguration(carConfig);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/staffFinalize.fxml"));
            Parent root = loader.load();

            staffFinalizeController finalizeController = loader.getController();
            finalizeController.setCarConfiguration(carConfig);

            Scene newScene = new Scene(root, 1300, 850);
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(newScene);
            stage.centerOnScreen();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to navigate to finalize page", ex);
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        navigateToScene(event, "/View/staffModelSelect.fxml");
    }

    private void navigateToScene(ActionEvent event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
            Scene newScene = new Scene(root, 1300, 850);
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            stage.setScene(newScene);
            stage.centerOnScreen();
        } catch (IOException | NullPointerException ex) {
            LOGGER.log(Level.SEVERE, "Failed to navigate to: " + fxmlPath, ex);
        }
    }

    public void setCarData(car existingCar, String name, String imagePath) {
        if (existingCar != null) {
            carConfig.setCarData(existingCar);
            carConfig.setModelName(name);
            carConfig.setModelImagePath(imagePath);
            updateCarDisplay();

            if (modelImage != null && imagePath != null) {
                try {
                    Image img = new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath)));
                    modelImage.setImage(img);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to set car image: " + imagePath, e);
                }
            }
        }
    }

    public car getConfiguredCar() {
        carConfig.updateCarPrice();
        return carConfig.getCarData();
    }

    @FunctionalInterface
    private interface IndexUpdater {
        void update(int newIndex);
    }
}
