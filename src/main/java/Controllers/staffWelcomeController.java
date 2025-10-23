package Controllers;

import Utils.SessionStaff;
import javafx.animation.ScaleTransition;
import javafx.animation.ParallelTransition;
import javafx.beans.binding.DoubleBinding;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

public class staffWelcomeController {
    private static final Logger logger = LoggerFactory.getLogger(staffWelcomeController.class);

    @FXML private VBox vbox;
    @FXML private Label topicLabel;
    @FXML private Label headingLabel;
    @FXML private Button discoverbtn;
    @FXML private Button logoutbtn;
    @FXML private Button accessorybtn;
    @FXML private ImageView backgroundImage;
    @FXML private StackPane imageContainer;
    @FXML private VBox settingsPopup;

    public void initialize() {
        Image image = new Image(Objects.requireNonNull(
                getClass().getResource("/Image/startUpImage.png")
        ).toExternalForm());
        backgroundImage.setImage(image);

        backgroundImage.fitWidthProperty().bind(imageContainer.widthProperty());
        backgroundImage.fitHeightProperty().bind(imageContainer.heightProperty());

        vbox.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                DoubleBinding widthScale = newScene.widthProperty().divide(1133.0);
                DoubleBinding heightScale = newScene.heightProperty().divide(580.0);
                widthScale.addListener((o, oldVal, newVal) -> scaleFonts(newVal.doubleValue(), heightScale.get()));
                heightScale.addListener((o, oldVal, newVal) -> scaleFonts(widthScale.get(), newVal.doubleValue()));
                scaleFonts(widthScale.get(), heightScale.get());

                settingsPopup.layoutXProperty().bind(newScene.widthProperty().subtract(settingsPopup.prefWidthProperty()).subtract(20));
                settingsPopup.layoutYProperty().bind(logoutbtn.heightProperty().add(20));
            }
        });

        vbox.setOpacity(1);
        vbox.setTranslateY(0);
    }

    private void scaleFonts(double widthScale, double heightScale) {
        double scale = Math.min(widthScale, heightScale);
        topicLabel.setStyle("-fx-font-size: " + (24 * scale) + "px;");
        headingLabel.setStyle("-fx-font-size: " + (36 * scale) + "px;");
    }

    private void createClickFeedback(Node node) {
        ScaleTransition pulse = new ScaleTransition(Duration.millis(120), node);
        pulse.setToX(0.95);
        pulse.setToY(0.95);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(2);
        new ParallelTransition(pulse).play();
    }

    private void navigate(Event event, String fxmlPath) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
            Scene newScene = new Scene(root, 1300, 850);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(newScene);
            stage.centerOnScreen();
            logger.info("Navigated to: {}", fxmlPath);
        } catch (IOException | NullPointerException ex) {
            logger.error("Failed to navigate: " + fxmlPath, ex);
            System.err.println("Failed to navigate: " + fxmlPath);
            ex.printStackTrace();
        }
    }

    @FXML
    private void discover(ActionEvent event) {
        createClickFeedback(discoverbtn);
        navigate(event, "/View/staffCars.fxml");
    }

    @FXML
    private void clickaccessorybtn(ActionEvent event) {
        createClickFeedback(accessorybtn);
        navigate(event, "/View/staffAsset.fxml");
    }

    @FXML
    private void logout(ActionEvent event) {
        createClickFeedback(logoutbtn);
        SessionStaff.handleLogout();
        navigateToLogin(event);
    }

    @FXML
    private void settingsLogout(ActionEvent event) {
        createClickFeedback((Node) event.getSource());
        SessionStaff.handleLogout();
        navigateToLogin(event);
    }

    private void navigateToLogin(Event event) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/View/login.fxml")));

            // Let the login.fxml use its own preferred size
            Scene loginScene = new Scene(root);

            // Close current stage
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

            // Create new stage for login with its natural size
            Stage loginStage = new Stage();
            loginStage.setScene(loginScene);
            loginStage.centerOnScreen();
            loginStage.show();

            logger.info("Navigated to login screen");
        } catch (IOException ex) {
            logger.error("Failed to navigate to login.fxml", ex);
            System.err.println("Failed to navigate to login.fxml");
            ex.printStackTrace();
        }
    }

    @FXML
    private void viewProfile(ActionEvent event) {}

    @FXML
    private void changePassword(ActionEvent event) {}

    @FXML
    private void togglePopup(ActionEvent event) {
        boolean isVisible = settingsPopup.isVisible();
        settingsPopup.setVisible(!isVisible);
        settingsPopup.setManaged(!isVisible);
        settingsPopup.setOpacity(1);
    }
}