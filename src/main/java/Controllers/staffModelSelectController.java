package Controllers;

import DAO.CarDAO;
import Model.car;
import Utils.DarkModeManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class staffModelSelectController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(staffModelSelectController.class);

    @FXML private VBox rootPane;
    @FXML private FlowPane flow_Pane;
    @FXML private Button backbtn;
    @FXML private Label modelTitle;

    private CarDAO carDAO;
    private String selectedModel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logger.info("Initializing staffModelSelectController");
        if (rootPane != null) {
            rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    DarkModeManager.getInstance().registerScene(newScene);
                }
            });
        }
        carDAO = new CarDAO();
    }

    public void setSelectedModel(String modelName) {
        this.selectedModel = modelName;
        logger.info("Selected model set to: {}", modelName);
        if (modelTitle != null) {
            modelTitle.setText(modelName + " Models");
        }
        Platform.runLater(() -> {
            if (flow_Pane != null) {
                logger.info("FlowPane is available, loading car cards for model: {}", modelName);
                loadCarCards();
            } else {
                logger.error("FlowPane is null!");
            }
        });
    }

    private void loadCarCards() {
        try {
            logger.info("Fetching cars from database for model: {}", selectedModel);
            List<car> cars;
            if (selectedModel != null && !selectedModel.isEmpty()) {
                cars = carDAO.getCarsByModelName(selectedModel);
            } else {
                cars = carDAO.getAllCars();
            }
            if (cars == null) {
                logger.error("getCarsByModelName() returned null!");
                return;
            }
            if (cars.isEmpty()) {
                logger.warn("No cars found in database for model: {}", selectedModel);
                return;
            }
            logger.info("Found {} cars for model: {}", cars.size(), selectedModel);
            flow_Pane.getChildren().clear();
            for (car carData : cars) {
                try {
                    logger.info("Creating card for car: {} (ID: {})", carData.getModelName(), carData.getCarId());
                    VBox card = createCarCard(carData);
                    flow_Pane.getChildren().add(card);
                    logger.info("Successfully added car card: {}", carData.getModelName());
                } catch (Exception e) {
                    logger.error("Unexpected error creating card for: " + carData.getModelName(), e);
                }
            }
            logger.info("Finished loading {} car cards", flow_Pane.getChildren().size());
        } catch (Exception e) {
            logger.error("Failed to load cars from database", e);
            e.printStackTrace();
        }
    }

    private VBox createCarCard(car carData) {
        VBox card = new VBox();
        card.getStyleClass().add("car-card");
        card.setPrefSize(320, 380);
        card.setAlignment(Pos.TOP_CENTER);
        card.setSpacing(0);

        StackPane imageContainer = new StackPane();
        imageContainer.getStyleClass().add("image-container");
        imageContainer.setPrefSize(320, 220);
        imageContainer.setAlignment(Pos.CENTER);

        ImageView carImage = new ImageView();
        carImage.setFitWidth(300);
        carImage.setFitHeight(200);
        carImage.setPreserveRatio(true);
        carImage.setSmooth(true);
        carImage.getStyleClass().add("car-image");

        String imagePath = carData.getCarPhoto();
        if (imagePath != null && !imagePath.isEmpty()) {
            try (InputStream stream = getClass().getResourceAsStream(imagePath)) {
                if (stream != null) {
                    carImage.setImage(new Image(stream));
                    logger.debug("Loaded car image: {}", imagePath);
                } else {
                    logger.warn("Image not found: {}", imagePath);
                    loadPlaceholderImage(carImage);
                }
            } catch (Exception e) {
                logger.error("Failed to load car image: " + imagePath, e);
                loadPlaceholderImage(carImage);
            }
        } else {
            loadPlaceholderImage(carImage);
        }

        imageContainer.getChildren().add(carImage);

        VBox detailsContainer = new VBox(12);
        detailsContainer.getStyleClass().add("details-container");
        detailsContainer.setAlignment(Pos.CENTER);
        detailsContainer.setPadding(new Insets(20, 20, 20, 20));

        Label modelNameLabel = new Label(carData.getFullName());
        modelNameLabel.getStyleClass().add("model-name");
        modelNameLabel.setWrapText(true);
        modelNameLabel.setMaxWidth(280);
        modelNameLabel.setAlignment(Pos.CENTER);

        Label priceLabel = new Label("$" + String.format("%,.0f", carData.getPrice()));
        priceLabel.getStyleClass().add("model-price");

        Label infoLabel = new Label(carData.getProductionYear() + " • " + carData.getCarStatus().toUpperCase());
        infoLabel.getStyleClass().add("model-info");

        Button customizeButton = new Button("CUSTOMIZE");
        customizeButton.getStyleClass().add("customize-button");
        customizeButton.setPrefWidth(200);
        customizeButton.setOnAction(e -> navigateToCustomize(carData));

        card.setOnMouseEntered(e -> card.setStyle("-fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-cursor: default;"));

        detailsContainer.getChildren().addAll(modelNameLabel, priceLabel, infoLabel, customizeButton);
        card.getChildren().addAll(imageContainer, detailsContainer);

        return card;
    }

    private void loadPlaceholderImage(ImageView imageView) {
        String placeholderPath = "/Image/placeholder_car.png";
        try (InputStream stream = getClass().getResourceAsStream(placeholderPath)) {
            if (stream != null) {
                imageView.setImage(new Image(stream));
                logger.debug("Loaded placeholder image");
            } else {
                logger.error("Placeholder image not found at: {}", placeholderPath);
            }
        } catch (Exception e) {
            logger.error("Failed to load placeholder image", e);
        }
    }

    private void navigateToCustomize(car selectedCar) {
        try {
            logger.info("Navigating to customize page with car: {}", selectedCar.getModelName());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/staffCustomize.fxml"));
            Parent root = loader.load();
            staffCustomizeController controller = loader.getController();
            if (controller != null) {
                controller.setSelectedCar(selectedCar);
            }
            Stage stage = (Stage) flow_Pane.getScene().getWindow();
            Scene scene = new Scene(root, 1300, 850);
            DarkModeManager.getInstance().registerScene(scene);
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            logger.error("Unable to load staffCustomize.fxml", e);
            e.printStackTrace();
        }
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/View/staffCars.fxml"));
            Scene scene = new Scene(root, 1300, 850);
            DarkModeManager.getInstance().registerScene(scene);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
            logger.info("Navigated back to staffCars");
        } catch (IOException e) {
            logger.error("Failed to navigate to staffCars", e);
        }
    }
}
