package Controllers;

import Utils.AppStage;
import javafx.animation.ScaleTransition;
import javafx.animation.ParallelTransition;
import javafx.beans.binding.DoubleBinding;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.event.ActionEvent;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class staffCarsController implements Initializable {

    @FXML private ImageView nine11_select_image;
    @FXML private ImageView seven18_select_image;
    @FXML private ImageView cayenne_select_image;
    @FXML private ImageView panamera_select_image;
    @FXML private ImageView macan_select_image;
    @FXML private ImageView taycan_select_image;
    @FXML private Button homebtn;

    private List<ImageView> allImages;
    private static final Color PORSCHE_RED = Color.web("#D5001C");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        allImages = Arrays.asList(
                nine11_select_image, seven18_select_image, taycan_select_image,
                panamera_select_image, macan_select_image, cayenne_select_image
        );
        loadImages();
        setupImageInteractions();
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
                if (stream == null) return;
                Image image = new Image(stream, 0, 0, true, true);
                imageView.setImage(image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void setupImageInteractions() {
        DropShadow glowEffect = new DropShadow(20, PORSCHE_RED);
        glowEffect.setSpread(0.4);

        for (ImageView imageView : allImages) {
            imageView.setOnMouseEntered(e -> {
                applyScale(imageView, 1.08);
                imageView.setEffect(glowEffect);
                if (imageView.getScene() != null)
                    imageView.getScene().setCursor(javafx.scene.Cursor.HAND);
            });

            imageView.setOnMouseExited(e -> {
                applyScale(imageView, 1.0);
                imageView.setEffect(null);
                if (imageView.getScene() != null)
                    imageView.getScene().setCursor(javafx.scene.Cursor.DEFAULT);
            });

            imageView.setOnMouseClicked(this::redirect);
        }
    }

    private void applyScale(ImageView imageView, double scale) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(300), imageView);
        scaleTransition.setToX(scale);
        scaleTransition.setToY(scale);
        scaleTransition.play();
    }

    @FXML
    private void home(ActionEvent event) {
        createClickFeedback((Button) event.getSource());
        navigate(event, "/View/staffWelcome.fxml");
    }

    @FXML
    private void redirect(MouseEvent event) {
        ImageView clicked = (ImageView) event.getSource();
        createClickFeedback(clicked);
        navigate(event, "/View/staffModelSelect.fxml");
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
            AppStage.getStage().setScene(newScene);
            AppStage.getStage().centerOnScreen();
        } catch (IOException | NullPointerException ex) {
            ex.printStackTrace();
        }
    }
}
