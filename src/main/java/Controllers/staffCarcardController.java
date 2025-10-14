package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import java.io.IOException;

public class staffCarcardController {

    @FXML
    private Label carname;
    @FXML
    private ImageView carImage;
    @FXML
    private Button configbtn;

    private String modelName;
    private String imagePath;
    private String price;

    /**
     * Set data for this car card.
     * @param name Model name
     * @param imgPath Path to image resource
     * @param price Model price
     */
    public void setCarData(String name, String imgPath, String price) {
        this.modelName = name;
        this.imagePath = imgPath;
        this.price = price;

        carname.setText(name);

        try {
            Image img = new Image(getClass().getResourceAsStream(imgPath));
            carImage.setImage(img);
            carImage.setPreserveRatio(true);
            carImage.setSmooth(true);
        } catch (Exception e) {
            // fallback if image not found
            carImage.setImage(null);
        }
    }

    /**
     * Handle Configure button click: redirect to customization page.
     */
    @FXML
    private void redirectToCustomize(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/staffCustomize.fxml"));
            Parent root = loader.load();

            // Pass selected model data to customization controller
            staffCustomizeController customizeController = loader.getController();
            customizeController.setSelectedModel(modelName, imagePath, price);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
