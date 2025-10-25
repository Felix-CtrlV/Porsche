package Controllers;

import Utils.DarkModeManager;
import Utils.SessionStaff;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class staffWelcomeController implements Initializable {

    @FXML
    private AnchorPane rootPane;

    @FXML
    private Button logoutbtn;

    @FXML
    private Button discoverbtn;

    @FXML
    private Button accessorybtn;

    @FXML
    private Label topicLabel;

    @FXML
    private Label headingLabel;

    @FXML
    private VBox settingsPopup;

    @FXML
    private Button darkModeBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                DarkModeManager.getInstance().registerScene(newScene);
            }
        });

        if (settingsPopup != null) {
            settingsPopup.setVisible(false);
            settingsPopup.setManaged(false);
        }

        if (darkModeBtn != null) {
            updateDarkModeButtonText();
        }
    }

    @FXML
    private void togglePopup() {
        if (settingsPopup != null) {
            boolean isVisible = settingsPopup.isVisible();
            settingsPopup.setVisible(!isVisible);
            settingsPopup.setManaged(!isVisible);

            if (!isVisible) {
                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), settingsPopup);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            }
        }
    }

    @FXML
    private void toggleDarkMode() {
        DarkModeManager.getInstance().toggleDarkMode();
        updateDarkModeButtonText();
    }

    private void updateDarkModeButtonText() {
        if (darkModeBtn != null) {
            if (DarkModeManager.getInstance().isDarkMode()) {
                darkModeBtn.setText("Light");
            } else {
                darkModeBtn.setText("Dark");
            }
        }
    }

    @FXML
    private void discover() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/staffCars.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) discoverbtn.getScene().getWindow();
            Scene scene = new Scene(root);
            DarkModeManager.getInstance().registerScene(scene);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading cars page: " + e.getMessage());
        }
    }

    @FXML
    private void clickaccessorybtn() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/staffAsset.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) accessorybtn.getScene().getWindow();
            Scene scene = new Scene(root);
            DarkModeManager.getInstance().registerScene(scene);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading accessories page: " + e.getMessage());
        }
    }

    @FXML
    private void logout() {
        try {
            if (settingsPopup != null) {
                settingsPopup.setVisible(false);
                settingsPopup.setManaged(false);
            }

            SessionStaff.getInstance().clearSession();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) rootPane.getScene().getWindow();
            Scene scene = new Scene(root);

            DarkModeManager.getInstance().unregisterScene(rootPane.getScene());
            DarkModeManager.getInstance().setDarkMode(false);

            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error during logout: " + e.getMessage());
        }
    }
}