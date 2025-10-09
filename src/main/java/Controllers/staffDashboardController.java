package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class staffDashboardController {

    @FXML
    private Button homebtn;

    @FXML
    private Button logoutbtn;

    @FXML
    private ImageView img;

    @FXML
    private Button toolsbtn;

    @FXML
    private AnchorPane Staff_anc;

    @FXML
    void clickHome(ActionEvent event) {

    }

    @FXML
    void clickLogout(ActionEvent event) {


    }

    @FXML
    void clickTools(ActionEvent event) {

    }

    public void initialize() throws IOException {
        AnchorPane cars = FXMLLoader.load(getClass().getResource("/View/staffCars.fxml"));
        Staff_anc.getChildren().add(cars);

        img.setImage(new Image(getClass().getResourceAsStream("/image/porsche_logo.png")));

    }

}
