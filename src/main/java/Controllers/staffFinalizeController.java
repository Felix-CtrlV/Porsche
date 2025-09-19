package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

public class staffFinalizeController {

    @FXML
    private ImageView selected_model_image;

    @FXML
    private Text selected_model;

    @FXML
    private Button goback_btn;

    @FXML
    private Button confirm_btn;

    @FXML
    private Text basic_price;

    @FXML
    private Text color_price;

    @FXML
    private Text wheels_price;

    @FXML
    private Text interior_price;

    @FXML
    private Text handling_price;

    @FXML
    private Text total_price;

    @FXML
    void redirect(MouseEvent event) {
        ImageView clicked = (ImageView) event.getSource();
    }

    @FXML
    private void initialize() {
        selected_model_image.setImage(new Image(getClass().getResourceAsStream("/Image/911_select_model.png")));
    }
}
