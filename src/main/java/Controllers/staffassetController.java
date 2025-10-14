package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class staffassetController {

    @FXML
    private ImageView img;

    @FXML
    private Button internalbtn;

    @FXML
    private Button externalbtn;

    @FXML
    private ScrollPane scrollpane;

    @FXML
    private VBox cardContainer;

    // Keep references to loaded views
    private Parent externalView;
    private Parent internalView;


    @FXML
    void clickInternalbtn(ActionEvent event) {
        cardContainer.getChildren().clear();
        loadInternalCards();
    }

    @FXML
    void clickExternalbtn(ActionEvent event) {
        cardContainer.getChildren().clear();
        loadExternalCards();
    }

    private void loadInternalCards() {
        try {
            for (int i = 1; i <= 5; i++) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/staffinternalcart.fxml"));
                Parent card = loader.load();
                staffinternalcartController controller = loader.getController();
                controller.setData("Internal Item " + i, 100.0 + i);
                cardContainer.getChildren().add(card);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadExternalCards() {
        try {
            for (int i = 1; i <= 5; i++) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/staffexternalcart.fxml"));
                Parent card = loader.load();
                staffexternalcartController controller = loader.getController();
                controller.setData("External Item " + i, 200.0 + i);
                cardContainer.getChildren().add(card);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initialize() {
        try {
            // Example: add several internal cart cards
            for (int i = 1; i <= 5; i++) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/staffinternalcart.fxml"));
                Parent card = loader.load();

                staffinternalcartController controller = loader.getController();
                controller.setData("Internal Item " + i, 100.0 + i);

                // Add to VBox

                cardContainer.getChildren().add(card);
            }

            // You can also load external cart cards
            for (int i = 1; i <= 3; i++) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/staffexternalcart.fxml"));
                Parent card = loader.load();

                staffexternalcartController controller = loader.getController();
                controller.setData("External Item " + i, 200.0 + i);

                cardContainer.getChildren().add(card);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


