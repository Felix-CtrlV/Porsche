package Controllers;

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

import java.io.IOException;
import java.io.InputStream;

public class staffCustomizeController {

    @FXML private ImageView model_image;
    @FXML private Label car_frame_num;
    @FXML private Label fuel_type;
    @FXML private Label model_name;
    @FXML private Button confirm_btn;
    @FXML private Button back_btn;

    // ScrollPanes for each customization section
    @FXML private ScrollPane wheels_scroll;
    @FXML private ScrollPane color_scroll;
    @FXML private ScrollPane interior_scroll;

    public void initialize() {
        try (InputStream imgStream = getClass().getResourceAsStream("/Image/911_carrera_gts.png")) {
            if (imgStream == null) {
                System.err.println("Image not found at /Image/911_carrera_gts.png");
                return;
            }
            Image car_model_view = new Image(imgStream);
            model_image.setImage(car_model_view);
        } catch (Exception e) {
            e.printStackTrace();
        }

        wheels_scroll.setHvalue(0);
        color_scroll.setHvalue(0);
        interior_scroll.setHvalue(0);
    }

    @FXML
    void clickConfirm(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/View/staffFinalize.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void back(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/View/staffModelSelect.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void left(ActionEvent event) {
        Object source = event.getSource();

        if (source == null) return;

        if (source == leftbtn) scrollLeft(wheels_scroll);
        else if (source == leftbtn1) scrollLeft(color_scroll);
        else if (source == leftbtn2) scrollLeft(interior_scroll);
    }

    @FXML
    void right(ActionEvent event) {
        Object source = event.getSource();

        if (source == null) return;

        if (source == rightbtn) scrollRight(wheels_scroll);
        else if (source == rightbtn1) scrollRight(color_scroll);
        else if (source == rightbtn2) scrollRight(interior_scroll);
    }

    private void scrollLeft(ScrollPane pane) {
        double current = pane.getHvalue();
        pane.setHvalue(Math.max(current - 0.2, 0));
    }

    private void scrollRight(ScrollPane pane) {
        double current = pane.getHvalue();
        pane.setHvalue(Math.min(current + 0.2, 1));
    }

    @FXML private Button leftbtn;
    @FXML private Button rightbtn;
    @FXML private Button leftbtn1;
    @FXML private Button rightbtn1;
    @FXML private Button leftbtn2;
    @FXML private Button rightbtn2;
}
