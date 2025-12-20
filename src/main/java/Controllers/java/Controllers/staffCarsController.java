    package Controllers.java.Controllers;

    import javafx.animation.ParallelTransition;
    import javafx.animation.ScaleTransition;
    import javafx.event.Event;
    import javafx.fxml.FXML;
    import javafx.fxml.FXMLLoader;
    import javafx.fxml.Initializable;
    import javafx.scene.Node;
    import javafx.scene.Parent;
    import javafx.scene.Scene;
    import javafx.scene.control.Button;
    import javafx.scene.image.Image;
    import javafx.scene.image.ImageView;
    import javafx.scene.input.MouseEvent;
    import javafx.event.ActionEvent;
    import javafx.scene.layout.*;
    import javafx.stage.Stage;
    import javafx.stage.Window;
    import javafx.util.Duration;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;

    import java.io.IOException;
    import java.io.InputStream;
    import java.net.URL;
    import java.util.*;

    public class staffCarsController implements Initializable {
        private static final Logger logger = LoggerFactory.getLogger(staffCarsController.class);

        @FXML private BorderPane rootPane;
        @FXML private ImageView nine11_select_image;
        @FXML private ImageView seven18_select_image;
        @FXML private ImageView cayenne_select_image;
        @FXML private ImageView panamera_select_image;
        @FXML private ImageView macan_select_image;
        @FXML private ImageView taycan_select_image;
        @FXML private Button homebtn;
        @FXML private Button nine11ExploreBtn;
        @FXML private Button seven18ExploreBtn;
        @FXML private Button taycanExploreBtn;
        @FXML private Button panameraExploreBtn;
        @FXML private Button macanExploreBtn;
        @FXML private Button cayenneExploreBtn;
        @FXML private GridPane modelsGrid;

        private List<ImageView> allImages;
        private List<Button> exploreButtons;
        private Map<Node, String> nodeToModelMap;

        @Override
        public void initialize(URL location, ResourceBundle resources) {
            logger.info("Initializing staffCarsController");

            allImages = Arrays.asList(
                    nine11_select_image, seven18_select_image, taycan_select_image,
                    panamera_select_image, macan_select_image, cayenne_select_image
            );

            exploreButtons = Arrays.asList(
                    nine11ExploreBtn, seven18ExploreBtn, taycanExploreBtn,
                    panameraExploreBtn, macanExploreBtn, cayenneExploreBtn
            );

            nodeToModelMap = new HashMap<>();
            nodeToModelMap.put(nine11_select_image, "911");
            nodeToModelMap.put(seven18_select_image, "718");
            nodeToModelMap.put(taycan_select_image, "Taycan");
            nodeToModelMap.put(panamera_select_image, "Panamera");
            nodeToModelMap.put(macan_select_image, "Macan");
            nodeToModelMap.put(cayenne_select_image, "Cayenne");

            nodeToModelMap.put(nine11ExploreBtn, "911");
            nodeToModelMap.put(seven18ExploreBtn, "718");
            nodeToModelMap.put(taycanExploreBtn, "Taycan");
            nodeToModelMap.put(panameraExploreBtn, "Panamera");
            nodeToModelMap.put(macanExploreBtn, "Macan");
            nodeToModelMap.put(cayenneExploreBtn, "Cayenne");

            loadImages();
            setupImageInteractions();
            setupExploreButtons();
        }

        private void loadImages() {
            Map<ImageView, String> imageMap = Map.of(
                    nine11_select_image, "/Image/911_promo.png",
                    seven18_select_image, "/Image/718_promo.png",
                    taycan_select_image, "/Image/taycan_promo.png",
                    panamera_select_image, "/Image/panamera_promo.png",
                    macan_select_image, "/Image/macan_promo.png",
                    cayenne_select_image, "/Image/cayenne_promo.png"
            );

            imageMap.forEach((imageView, path) -> {
                try (InputStream stream = getClass().getResourceAsStream(path)) {
                    if (stream == null) {
                        logger.warn("Image not found: {}", path);
                        return;
                    }
                    Image image = new Image(stream, 0, 0, true, true);
                    imageView.setImage(image);
                    logger.debug("Loaded image: {}", path);
                } catch (IOException e) {
                    logger.error("Failed to load image: " + path, e);
                }
            });
        }

        private void setupImageInteractions() {
            for (ImageView imageView : allImages) {
                imageView.setOnMouseEntered(e -> {
                    if (imageView.getScene() != null)
                        imageView.getScene().setCursor(javafx.scene.Cursor.HAND);
                });

                imageView.setOnMouseExited(e -> {
                    if (imageView.getScene() != null)
                        imageView.getScene().setCursor(javafx.scene.Cursor.DEFAULT);
                });

                imageView.setOnMouseClicked(this::redirectWithModel);
            }
        }

        private void setupExploreButtons() {
            for (Button exploreBtn : exploreButtons) {
                exploreBtn.setOnAction(this::handleExploreButton);
            }
        }

        @FXML
        private void home(ActionEvent event) {
            createClickFeedback((Node) event.getSource());
            navigate(event, "/View/staffWelcome.fxml", null);
        }

        @FXML
        private void redirectWithModel(MouseEvent event) {
            Node clicked = (Node) event.getSource();
            String modelName = nodeToModelMap.get(clicked);
            logger.info("Selected model: {}", modelName);
            createClickFeedback(clicked);
            navigate(event, "/View/staffModelSelect.fxml", modelName);
        }

        @FXML
        private void handleExploreButton(ActionEvent event) {
            Node source = (Node) event.getSource();
            String modelName = nodeToModelMap.get(source);
            if (modelName == null) {
                logger.warn("No model mapping found for explore button {}", source);
                return;
            }
            logger.info("Explore button selected model: {}", modelName);
            createClickFeedback(source);
            navigate(event, "/View/staffModelSelect.fxml", modelName);
        }

        private void createClickFeedback(Node node) {
            ScaleTransition pulse = new ScaleTransition(Duration.millis(120), node);
            pulse.setToX(0.96);
            pulse.setToY(0.96);
            pulse.setAutoReverse(true);
            pulse.setCycleCount(2);
            new ParallelTransition(pulse).play();
        }

        private void navigate(Event event, String fxmlPath, String selectedModel) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Parent root = loader.load();

                // Pass selected model only for staffModelSelect.fxml
                if (selectedModel != null && fxmlPath.contains("staffModelSelect")) {
                    staffModelSelectController controller = loader.getController();
                    if (controller != null) {
                        controller.setSelectedModel(selectedModel);
                    }
                }

                Scene newScene = new Scene(root);
                Window window = ((Node) event.getSource()).getScene().getWindow();

                if (window instanceof Stage stage) {
                    stage.setScene(newScene);

                    // Auto fullscreen
                    stage.setFullScreen(true);

                    stage.centerOnScreen();
                }

                logger.info("Navigated to: {} with model: {}", fxmlPath, selectedModel);

            } catch (IOException | NullPointerException ex) {
                logger.error("Failed to navigate: " + fxmlPath, ex);
            }
        }

    }