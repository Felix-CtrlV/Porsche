package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;

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

    // Keep references to loaded views
    private Parent externalView;
    private Parent internalView;

    @FXML
    void clickExternalbtn(ActionEvent event) {
        scrollpane.setContent(externalView); // just swap, no reload
    }

    @FXML
    void clickInternalbtn(ActionEvent event) {
        scrollpane.setContent(internalView); // just swap, no reload
    }

    @FXML
    void initialize() {
        try {
            // Load both FXML files only once
            externalView = FXMLLoader.load(getClass().getResource("/View/staffexternalcart.fxml"));
            internalView = FXMLLoader.load(getClass().getResource("/View/staffinternalcart.fxml"));

            // Set default view (e.g., internal)
            scrollpane.setContent(internalView);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
