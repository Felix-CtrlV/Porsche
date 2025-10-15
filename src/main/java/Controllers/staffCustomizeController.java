package Controllers;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

public class staffCustomizeController implements Initializable {

    @FXML private ImageView model_image;
    @FXML private Label model_name;
    @FXML private Label model_price;
    @FXML private Label car_description;
    @FXML private Label car_frame_num;
    @FXML private Label fuel_type;

    @FXML private Button confirm_btn;
    @FXML private Button back_btn;

    @FXML private Button leftbtn;
    @FXML private Button rightbtn;
    @FXML private Button leftbtn1;
    @FXML private Button rightbtn1;
    @FXML private Button leftbtn2;
    @FXML private Button rightbtn2;

    @FXML private ScrollPane wheels_scroll;
    @FXML private ScrollPane color_scroll;
    @FXML private ScrollPane interior_scroll;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        model_name.setText("Carrera GTS");
        model_price.setText("$140000");
        car_description.setText("The 911 Carrera GTS epitomizes precision and driving passion. Its naturally aspirated flat-six, razor-sharp handling, and track-focused aerodynamics deliver uncompromising performance—every roar and shift a statement of speed, control, and automotive excellence.");
        car_frame_num.setText("911");
        fuel_type.setText("GASOLINE");
    }

    @FXML
    private void clickConfirm(ActionEvent event) {
        navigate(event, "/View/staffFinalize.fxml");
    }

    @FXML
    private void back(ActionEvent event) {
        navigate(event, "/View/staffModelSelect.fxml");
    }

    @FXML
    private void left(ActionEvent event) {
        ScrollPane scroll = getScrollFromEvent(event);
        if (scroll != null) scroll.setHvalue(scroll.getHvalue() - 0.2);
    }

    @FXML
    private void right(ActionEvent event) {
        ScrollPane scroll = getScrollFromEvent(event);
        if (scroll != null) scroll.setHvalue(scroll.getHvalue() + 0.2);
    }

    private ScrollPane getScrollFromEvent(ActionEvent event) {
        Object source = event.getSource();
        if (source == leftbtn || source == rightbtn) return wheels_scroll;
        if (source == leftbtn1 || source == rightbtn1) return color_scroll;
        if (source == leftbtn2 || source == rightbtn2) return interior_scroll;
        return null;
    }

    private void navigate(ActionEvent event, String path) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(path)));
            Stage stage = (Stage)((Button)event.getSource()).getScene().getWindow();
            root.setOpacity(0);
            Scene newScene = new Scene(root, 1376, 768);
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), stage.getScene().getRoot());
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                stage.setScene(newScene);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            });
            fadeOut.play();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void setCarData(String name, String price, String imagePath) {
        if (model_name != null) model_name.setText(name);
        if (model_price != null) model_price.setText(price);
        if (imagePath != null && model_image != null) {
            try {
                Image img = new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath)));
                model_image.setImage(img);
            } catch (Exception e) {
                System.err.println("Image not found: " + imagePath);
            }
        }
    }
}
