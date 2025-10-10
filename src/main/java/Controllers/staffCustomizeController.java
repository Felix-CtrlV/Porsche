package Controllers;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;

public class staffCustomizeController {

    @FXML private ImageView model_image;
    @FXML private Label car_frame_num;
    @FXML private Label fuel_type;
    @FXML private Label model_name;
    @FXML private Label car_description;
    @FXML private Button confirm_btn;
    @FXML private Button back_btn;

    @FXML private ScrollPane wheels_scroll;
    @FXML private ScrollPane color_scroll;
    @FXML private ScrollPane interior_scroll;

    @FXML private Button leftbtn;
    @FXML private Button rightbtn;
    @FXML private Button leftbtn1;
    @FXML private Button rightbtn1;
    @FXML private Button leftbtn2;
    @FXML private Button rightbtn2;

    public void initialize() {
        loadCarImage();
        initializeScrollPanes();
        animateCarImageEntry();
    }

    private void loadCarImage() {
        try (InputStream imgStream = getClass().getResourceAsStream("/Image/911_carrera_gts.png")) {
            if (imgStream == null) {
                System.err.println("Image not found at /Image/911_carrera_gts.png");
                try (InputStream fallbackStream = getClass().getResourceAsStream("/Image/911_model.png")) {
                    if (fallbackStream != null) {
                        Image car_model_view = new Image(fallbackStream);
                        model_image.setImage(car_model_view);
                    }
                }
                return;
            }
            Image car_model_view = new Image(imgStream);
            model_image.setImage(car_model_view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeScrollPanes() {
        if (wheels_scroll != null) wheels_scroll.setHvalue(0);
        if (color_scroll != null) color_scroll.setHvalue(0);
        if (interior_scroll != null) interior_scroll.setHvalue(0);
    }

    private void animateCarImageEntry() {
        if (model_image == null) return;

        model_image.setOpacity(0);
        model_image.setScaleX(0.9);
        model_image.setScaleY(0.9);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(1200), model_image);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(1200), model_image);
        scaleUp.setFromX(0.9);
        scaleUp.setFromY(0.9);
        scaleUp.setToX(1.0);
        scaleUp.setToY(1.0);

        fadeIn.play();
        scaleUp.play();
    }

    @FXML
    void clickConfirm(ActionEvent event) {
        animateButtonPress(confirm_btn, () -> {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/View/staffFinalize.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    void back(ActionEvent event) {
        animateButtonPress(back_btn, () -> {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/View/staffModelSelect.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    void left(ActionEvent event) {
        Object source = event.getSource();

        if (source == null) return;

        if (source == leftbtn && wheels_scroll != null) {
            animateScroll(wheels_scroll, -0.2);
        } else if (source == leftbtn1 && color_scroll != null) {
            animateScroll(color_scroll, -0.2);
        } else if (source == leftbtn2 && interior_scroll != null) {
            animateScroll(interior_scroll, -0.2);
        }
    }

    @FXML
    void right(ActionEvent event) {
        Object source = event.getSource();

        if (source == null) return;

        if (source == rightbtn && wheels_scroll != null) {
            animateScroll(wheels_scroll, 0.2);
        } else if (source == rightbtn1 && color_scroll != null) {
            animateScroll(color_scroll, 0.2);
        } else if (source == rightbtn2 && interior_scroll != null) {
            animateScroll(interior_scroll, 0.2);
        }
    }

    private void animateScroll(ScrollPane pane, double delta) {
        if (pane == null) return;

        double current = pane.getHvalue();
        double target = Math.max(0, Math.min(1, current + delta));

        TranslateTransition transition = new TranslateTransition(Duration.millis(300), pane.getContent());
        transition.setOnFinished(e -> pane.setHvalue(target));

        pane.setHvalue(target);
    }

    private void animateButtonPress(Button button, Runnable action) {
        if (button == null) return;

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), button);
        scaleDown.setToX(0.95);
        scaleDown.setToY(0.95);

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), button);
        scaleUp.setToX(1.0);
        scaleUp.setToY(1.0);

        scaleDown.setOnFinished(e -> {
            scaleUp.play();
            if (action != null) {
                action.run();
            }
        });
        scaleDown.play();
    }
}
