package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import java.io.IOException;

public class staffCarcardController {

    @FXML
    private ImageView carImage;

    @FXML
    private Label carname;

    @FXML
    private Button configbtn;

    public void setCard(String label, Image image) {
        carImage.setImage(image);
        carname.setText(label);
    }

    @FXML
    private void redirectToCustomize() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/staffCustomize.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) configbtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
