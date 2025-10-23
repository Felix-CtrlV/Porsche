package Controllers;

import Model.car;
import Model.CarConfiguration;
import Model.CustomizationOption;
import Utils.SessionStaff;
import Utils.DarkModeManager;
import javafx.animation.*;
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
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
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

    @FXML private StackPane rootPane;
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
        if (rootPane != null) {
            rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    DarkModeManager.getInstance().registerScene(newScene);
                }
            });
        }

        CarConfiguration savedConfig = SessionStaff.getInstance().getCarConfiguration();

        if (savedConfig != null) {
            carConfig = savedConfig;
        } else {
            initializeCarConfiguration();
        }

        loadCustomizationOptions();
        displayInitialSelections();
    }

    public void setSelectedCar(car selectedCar) {
        if (selectedCar != null) {
            carConfig = new CarConfiguration(selectedCar);
            carConfig.setModelName(selectedCar.getModelName());
            carConfig.setModelImagePath(selectedCar.getCarPhoto());
            carConfig.setFrameNumber(String.valueOf(selectedCar.getCarId()));
            carConfig.setFuelType(selectedCar.getFuelType() != null ? selectedCar.getFuelType() : "GASOLINE");
            carConfig.setDescription(selectedCar.getCarDescription() != null ? selectedCar.getCarDescription() : "");

            updateCarDisplay();
            loadCustomizationOptions();
            displayInitialSelections();
        }
    }

    private void initializeCarConfiguration() {
        car newCar = new car();
        newCar.setCarId(1);
        newCar.setModelName("911 Carrera GTS");
        newCar.setCarColor("Carrara White");
        newCar.setProductionYear(2024);
        newCar.setPrice(140000.0);
        newCar.setCarStatus("available");
        newCar.setCarPhoto("/Image/911_select_model.png");
        newCar.setFuelType("GASOLINE");
        newCar.setCarDescription(
                "The 911 Carrera GTS epitomizes precision and driving passion. " +
                        "Its naturally aspirated flat-six, razor-sharp handling, and track-focused " +
                        "aerodynamics deliver uncompromising performance—every roar and shift a " +
                        "statement of speed, control, and automotive excellence."
        );

        carConfig = new CarConfiguration(newCar);
        carConfig.setModelName(newCar.getModelName());
        carConfig.setModelImagePath(newCar.getCarPhoto());
        carConfig.setFrameNumber(String.valueOf(newCar.getCarId()));
        carConfig.setFuelType(newCar.getFuelType());
        carConfig.setDescription(newCar.getCarDescription());

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
        wheelOptions.clear();
        colorOptions.clear();
        interiorOptions.clear();

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

        ParallelTransition outTransition = new ParallelTransition(slideOutCurrent, fadeOutCurrent);
        ParallelTransition inTransition = new ParallelTransition(slideInNext, fadeInNext);

        SequentialTransition sequence = new SequentialTransition(outTransition, inTransition);
        sequence.setOnFinished(e -> {
            container.getChildren().remove(currentImageView);
            if (onComplete != null) onComplete.run();
        });

        sequence.play();
    }

    @FunctionalInterface
    private interface IndexUpdater {
        void update(int index);
    }

    @FXML
    private void confirmConfiguration(ActionEvent event) {
        SessionStaff.getInstance().setCarConfiguration(carConfig);
        navigate(event, "/View/staffFinalize.fxml");
    }

    @FXML
    private void goback(ActionEvent event) {
        navigate(event, "/View/staffModelSelect.fxml");
    }

    private void navigate(ActionEvent event, String path) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(path)));
            Scene newScene = new Scene(root, 1300, 850);
            DarkModeManager.getInstance().registerScene(newScene);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(newScene);
            stage.centerOnScreen();
        } catch (IOException | NullPointerException ex) {
            LOGGER.log(Level.SEVERE, "Failed to navigate to: " + path, ex);
        }
    }
}