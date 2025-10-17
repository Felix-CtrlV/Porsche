package Controllers;

import javafx.animation.*;
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

import java.io.IOException;
import java.util.Objects;

public class staffWelcomeController {

    @FXML private VBox vbox;
    @FXML private Label topicLabel;
    @FXML private Label headingLabel;
    @FXML private Button discoverbtn;
    @FXML private Button logoutbtn;
    @FXML private Button accessorybtn;
    @FXML private ImageView backgroundImage;
    @FXML private StackPane imageContainer;
    @FXML private VBox settingsPopup;

    private static final Duration FADE_DURATION = Duration.millis(400);

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

        // Remove entrance animation
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

        FadeTransition flash = new FadeTransition(Duration.millis(120), node);
        flash.setFromValue(1.0);
        flash.setToValue(0.8);
        flash.setAutoReverse(true);
        flash.setCycleCount(2);

        new ParallelTransition(pulse, flash).play();
    }

    private void navigate(Event event, String fxmlPath) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        Parent root;

        try {
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
        } catch (IOException | NullPointerException ex) {
            System.err.println("Failed to navigate: " + fxmlPath);
            ex.printStackTrace();
            return;
        }

        root.setOpacity(0);
        Scene newScene = new Scene(root, 1133, 580);

        FadeTransition fadeOut = new FadeTransition(FADE_DURATION, stage.getScene().getRoot());
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            stage.setScene(newScene);
            stage.centerOnScreen();
            FadeTransition fadeIn = new FadeTransition(FADE_DURATION, root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        });
        fadeOut.play();
    }

    @FXML
    private void discover(ActionEvent event) {
        createClickFeedback(discoverbtn);
        PauseTransition pause = new PauseTransition(Duration.millis(250));
        pause.setOnFinished(e -> navigate(event, "/View/staffCars.fxml"));
        pause.play();
    }

    @FXML
    private void logout(ActionEvent event) {
        createClickFeedback(logoutbtn);
        PauseTransition pause = new PauseTransition(Duration.millis(250));
        pause.setOnFinished(e -> navigate(event, "/View/login.fxml"));
        pause.play();
    }

    @FXML
    private void clickaccessorybtn(ActionEvent event) {
        createClickFeedback(accessorybtn);
        PauseTransition pause = new PauseTransition(Duration.millis(250));
        pause.setOnFinished(e -> navigate(event, "/View/staffasset.fxml"));
        pause.play();
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

        if (!isVisible) {
            settingsPopup.setOpacity(0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), settingsPopup);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        } else {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), settingsPopup);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.play();
        }
    }
}
