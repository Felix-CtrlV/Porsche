package Controllers;

import DAO.CarDAO;
import Model.car;
import Model.CarConfiguration;
import Utils.SessionStaff;
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
import javafx.scene.layout.HBox;
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
import java.util.*;

public class staffModelSelectController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(staffModelSelectController.class);

    @FXML private VBox rootPane;
    @FXML private FlowPane flow_Pane;
    @FXML private Button backbtn;
    @FXML private Label modelTitle;
    @FXML private HBox headerBox;
    @FXML private Button refreshButton;

    private CarDAO carDAO;
    private String selectedModel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logger.info("Initializing staffModelSelectController");
        carDAO = new CarDAO();

        rootPane.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((obs, oldWindow, newWindow) -> {
                    if (newWindow != null) {
                        newWindow.showingProperty().addListener((showObs, oldShowing, newShowing) -> {
                            if (newShowing) {
                                Platform.runLater(() -> {
                                    restoreSelectedModel();
                                    refreshCarData();
                                });
                            }
                        });
                        if (newWindow.isShowing()) {
                            Platform.runLater(() -> {
                                restoreSelectedModel();
                                refreshCarData();
                            });
                        }
                    }
                });
                if (newScene.getWindow() != null && newScene.getWindow().isShowing()) {
                    Platform.runLater(() -> {
                        restoreSelectedModel();
                        refreshCarData();
                    });
                }
            }
        });
    }

    private void restoreSelectedModel() {
        if (selectedModel == null) {
            String lastModel = SessionStaff.getInstance().getLastSelectedModel();
            if (lastModel != null && !lastModel.isEmpty()) {
                setSelectedModel(lastModel);
                logger.info("Restored selected model from session: {}", lastModel);
            }
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        logger.info("Manual refresh triggered");
        refreshCarData();
    }

    public void setSelectedModel(String modelName) {
        this.selectedModel = modelName;
        SessionStaff.getInstance().setLastSelectedModel(modelName);
        logger.info("Selected model set to: {}", modelName);
        if (modelTitle != null) {
            modelTitle.setText(modelName + " Models");
        }
        Platform.runLater(() -> {
            refreshCarData();
        });
    }

    public void refreshCarData() {
        if (flow_Pane != null && selectedModel != null) {
            logger.info("Refreshing car cards for model: {}", selectedModel);
            loadCarCards();
        } else {
            logger.warn("Cannot refresh - flow_Pane: {}, selectedModel: {}", flow_Pane != null, selectedModel);
        }
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
                logger.error("CarDAO returned null!");
                return;
            }

            List<car> uniqueCars = getUniqueCarsByModelAndTrim(cars);

            Platform.runLater(() -> {
                if (uniqueCars.isEmpty()) {
                    logger.warn("No cars found in database for model: {}", selectedModel);
                    showNoCarsMessage();
                    return;
                }

                logger.info("Found {} unique cars for model: {}", uniqueCars.size(), selectedModel);
                flow_Pane.getChildren().clear();

                for (car carData : uniqueCars) {
                    try {
                        VBox card = createCarCard(carData);
                        flow_Pane.getChildren().add(card);
                    } catch (Exception e) {
                        logger.error("Unexpected error creating card for: {} {}", carData.getModelName(), carData.getTrimName(), e);
                    }
                }
            });
        } catch (Exception e) {
            logger.error("Failed to load cars from database", e);
            showErrorAlert("Database Error", "Failed to load cars: " + e.getMessage());
        }
    }

    private List<car> getUniqueCarsByModelAndTrim(List<car> cars) {
        Map<String, car> uniqueCarsMap = new LinkedHashMap<>();
        for (car carData : cars) {
            String key = carData.getModelName() + "_" + carData.getTrimName();
            if (!uniqueCarsMap.containsKey(key)) {
                uniqueCarsMap.put(key, carData);
            }
        }
        return new ArrayList<>(uniqueCarsMap.values());
    }

    private void showNoCarsMessage() {
        Platform.runLater(() -> {
            Label noCarsLabel = new Label("No cars found for " + selectedModel);
            noCarsLabel.getStyleClass().add("no-cars-message");
            flow_Pane.getChildren().clear();
            flow_Pane.getChildren().add(noCarsLabel);
        });
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

        loadCarImage(carData, carImage);
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

    private void loadCarImage(car carData, ImageView imageView) {
        String carPhoto = carData.getCarPhoto();
        if (carPhoto == null || carPhoto.trim().isEmpty()) {
            loadPlaceholderImage(imageView);
            return;
        }
        String imagePath = carPhoto.trim();
        Image image = loadImageWithFallbacks(imagePath);
        if (image != null && !image.isError()) {
            imageView.setImage(image);
            return;
        }
        loadPlaceholderImage(imageView);
    }

    private Image loadImageWithFallbacks(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }
        try {
            if (imagePath.startsWith("/")) {
                InputStream stream = getClass().getResourceAsStream(imagePath);
                if (stream != null) {
                    return new Image(stream);
                }
            }
            InputStream stream = getClass().getResourceAsStream("/" + imagePath);
            if (stream != null) {
                return new Image(stream);
            }
            stream = getClass().getResourceAsStream("/Images/" + imagePath);
            if (stream != null) {
                return new Image(stream);
            }
            if (imagePath.startsWith("file:")) {
                return new Image(imagePath);
            } else {
                File file = new File(imagePath);
                if (file.exists()) {
                    try (FileInputStream fileStream = new FileInputStream(file)) {
                        return new Image(fileStream);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Error loading image: {}", imagePath, e);
        }
        return null;
    }

    private void loadPlaceholderImage(ImageView imageView) {
        String[] placeholderPaths = {
                "/Images/placeholder_car.png",
                "/Images/911_select_model.png",
                "/Images/placeholder.png",
                "/Images/placeholder_car.jpg",
                "/Images/placeholder_car.jpeg"
        };
        for (String path : placeholderPaths) {
            try {
                InputStream stream = getClass().getResourceAsStream(path);
                if (stream != null) {
                    Image image = new Image(stream);
                    imageView.setImage(image);
                    return;
                }
            } catch (Exception e) {
            }
        }
        imageView.setStyle("-fx-background-color: #cccccc; -fx-min-width: 300; -fx-min-height: 200;");
    }

    private void navigateToCustomize(car selectedCar) {
        try {
            CarConfiguration carConfig = new CarConfiguration(selectedCar);
            SessionStaff.getInstance().setCarConfiguration(carConfig);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/staffCustomize.fxml"));
            Parent root = loader.load();

            staffCustomizeController controller = loader.getController();
            if (controller != null) {
                controller.setSelectedCar(selectedCar);
            }

            Window window = flow_Pane.getScene().getWindow();
            Scene scene = new Scene(root);

            if (window instanceof Stage stage) {
                stage.setScene(scene);

                // Make fullscreen
                stage.setFullScreen(true);

                stage.centerOnScreen();
            }

        } catch (IOException e) {
            logger.error("Unable to load staffCustomize.fxml", e);
            showErrorAlert("Navigation Error", "Failed to load customization page: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error navigating to customize page", e);
            showErrorAlert("Error", "An unexpected error occurred: " + e.getMessage());
        }
    }


    @FXML
    private void goBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/View/staffCars.fxml"));
            Scene scene = new Scene(root);

            Window window = ((Node) event.getSource()).getScene().getWindow();
            if (window instanceof Stage stage) {
                stage.setScene(scene);
                stage.setFullScreen(true);
                stage.centerOnScreen();
            }
        } catch (IOException e) {
            logger.error("Failed to navigate to staffCars", e);
            showErrorAlert("Navigation Error", "Failed to navigate back: " + e.getMessage());
        }
    }


    private void showErrorAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}