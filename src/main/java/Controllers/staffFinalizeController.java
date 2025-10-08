package Controllers;

import javafx.animation.FillTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class staffFinalizeController {

    @FXML private ImageView selected_model_image;
    @FXML private Button goback_btn;
    @FXML private Button confirm_btn;
    @FXML private Button darkmodebtn;

    @FXML private Label basic_price;
    @FXML private Label color_opt;
    @FXML private Label color_price;
    @FXML private Label wheels_opt;
    @FXML private Label wheels_price;
    @FXML private Label interior_opt;
    @FXML private Label interior_price;
    @FXML private Label handling_price;
    @FXML private Label total_price;
    @FXML private Label car_features;

    @FXML private Line line1;
    @FXML private Line line2;
    @FXML private Line line3;
    @FXML private Line line4;
    @FXML private Line line5;
    @FXML private Line line6;
    @FXML private Line line7;
    @FXML private Line line8;

    private boolean darkMode = false;

    @FXML
    void goback(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/staffCustomize.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void modechange(ActionEvent event) {
        Node root = darkmodebtn.getScene().getRoot();
        Color lightLine = Color.rgb(0,0,0,0.5);
        Color darkLine = Color.rgb(255,255,255,0.3);

        Line[] lines = {line1,line2,line3,line4,line5,line6,line7,line8};

        if (!darkMode) {
            // Add dark mode class
            root.getStyleClass().add("dark-mode");

            // Animate lines to white-ish
            for (Line l : lines) {
                FillTransition ft = new FillTransition(Duration.millis(400), l, (Color) l.getStroke(), darkLine);
                ft.setCycleCount(1);
                ft.play();
            }
            darkMode = true;
        } else {
            // Remove dark mode class
            root.getStyleClass().remove("dark-mode");

            // Animate lines back to black-ish
            for (Line l : lines) {
                FillTransition ft = new FillTransition(Duration.millis(400), l, (Color) l.getStroke(), lightLine);
                ft.setCycleCount(1);
                ft.play();
            }
            darkMode = false;
        }
    }

    @FXML
    private void initialize() {
        selected_model_image.setImage(new Image(getClass().getResourceAsStream("/Image/911_select_model.png")));
    }
}
