package Controllers;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;

public class carsController {

    @FXML private ImageView nine11_select_image;
    @FXML private ImageView seven18_select_image;
    @FXML private ImageView cayenne_select_image;
    @FXML private ImageView panamera_select_image;
    @FXML private ImageView macan_select_image;
    @FXML private ImageView taycan_select_image;

    @FXML
    void redirect(MouseEvent event) {
        ImageView clicked = (ImageView) event.getSource();
        System.out.println("Image clicked: " + clicked.getId());
        // add navigation here
    }

    @FXML
    private void initialize() {
        nine11_select_image.setImage(new Image(getClass().getResourceAsStream("/Image/911_promo.png")));
        seven18_select_image.setImage(new Image(getClass().getResourceAsStream("/Image/718_promo.png")));
        taycan_select_image.setImage(new Image(getClass().getResourceAsStream("/Image/taycan_promo.png")));
        panamera_select_image.setImage(new Image(getClass().getResourceAsStream("/Image/panamera_promo.png")));
        macan_select_image.setImage(new Image(getClass().getResourceAsStream("/Image/macan_promo.png")));
        cayenne_select_image.setImage(new Image(getClass().getResourceAsStream("/Image/cayenne_promo.png")));
    }
}
