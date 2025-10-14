package Controllers;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
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
    private static final Duration FADE_DURATION = Duration.millis(400);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        allImages = Arrays.asList(
                nine11_select_image, seven18_select_image, taycan_select_image,
                panamera_select_image, macan_select_image, cayenne_select_image
        );
        loadImages();
        setupImageInteractions();
        animateImagesOnLoad();
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
            } catch (IOException e) { e.printStackTrace(); }
        });
    }

    private void setupImageInteractions() {
        DropShadow glowEffect = new DropShadow(20, PORSCHE_RED);
        glowEffect.setSpread(0.4);
        for (ImageView imageView : allImages) {
            imageView.setOnMouseEntered(e -> {
                applyScale(imageView, 1.08);
                imageView.setEffect(glowEffect);
                imageView.getScene().setCursor(Cursor.HAND);
            });
            imageView.setOnMouseExited(e -> {
                applyScale(imageView, 1.0);
                imageView.setEffect(null);
                imageView.getScene().setCursor(Cursor.DEFAULT);
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

    private void animateImagesOnLoad() {
        int delay = 0;
        for (ImageView imageView : allImages) {
            imageView.setOpacity(0);
            imageView.setTranslateY(30);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), imageView);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.setDelay(Duration.millis(delay));
            TranslateTransition slideUp = new TranslateTransition(Duration.millis(500), imageView);
            slideUp.setFromY(30);
            slideUp.setToY(0);
            slideUp.setDelay(Duration.millis(delay));
            new ParallelTransition(fadeIn, slideUp).play();
            delay += 100;
        }

    }

    @FXML
    private void redirect(MouseEvent event) {
        ImageView clicked = (ImageView) event.getSource();
        createClickFeedback(clicked);
        PauseTransition pause = new PauseTransition(Duration.millis(250));
        pause.setOnFinished(e -> navigate(event, "/View/staffModelSelect.fxml"));
        pause.play();
    }

    private void createClickFeedback(ImageView imageView) {
        ScaleTransition pulse = new ScaleTransition(Duration.millis(120), imageView);
        pulse.setToX(0.95);
        pulse.setToY(0.95);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(2);
        FadeTransition flash = new FadeTransition(Duration.millis(120), imageView);
        flash.setFromValue(1.0);
        flash.setToValue(0.8);
        flash.setAutoReverse(true);
        flash.setCycleCount(2);
        new ParallelTransition(pulse, flash).play();
    }

    private void navigate(MouseEvent event, String fxmlPath) {
        Node source = (Node) event.getSource();
        Scene currentScene = source.getScene();
        Parent root;
        try { root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath))); }
        catch (IOException | NullPointerException ex) { ex.printStackTrace(); return; }
        root.setOpacity(0);
        Stage stage = (Stage) currentScene.getWindow();
        Scene newScene = new Scene(root);
        FadeTransition fadeOut = new FadeTransition(FADE_DURATION, currentScene.getRoot());
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            stage.setScene(newScene);
            FadeTransition fadeIn = new FadeTransition(FADE_DURATION, root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        });
        fadeOut.play();
    }

    @FXML
    private void home(MouseEvent event) { navigate(event, "/View/staffWelcome.fxml"); }

    public String getSelectedModelId(ImageView imageView) { return imageView.getId(); }
    public void preloadAllImages() { allImages.stream().map(ImageView::getImage).filter(Objects::nonNull).forEach(img -> {}); }
}
