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
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class staffWelcomeController implements Initializable {

    @FXML
    private StackPane rootPane;

    @FXML
    private Button setbtn;

    @FXML
    private Button square_button;

    @FXML
    private Label heading;

    private VBox settingsPopup;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                DarkModeManager.getInstance().registerScene(newScene);
            }
        });

        if (setbtn != null) {
            setbtn.setOnAction(event -> showSettingsPopup());
        }

        if (square_button != null) {
            square_button.setOnAction(event -> loadCarsPage());
        }
    }

    private void showSettingsPopup() {
        if (settingsPopup == null) {
            settingsPopup = new VBox(15);
            settingsPopup.getStyleClass().add("settings-popup");
            settingsPopup.setAlignment(Pos.CENTER);
            settingsPopup.setPadding(new Insets(20));
            settingsPopup.setMaxWidth(250);
            settingsPopup.setMaxHeight(200);

            // Dark Mode Toggle Button
            Button darkModeToggle = new Button(
                    DarkModeManager.getInstance().isDarkMode() ? "☀ Light Mode" : "🌙 Dark Mode"
            );
            darkModeToggle.getStyleClass().add("dark-mode-toggle");
            darkModeToggle.setPrefWidth(200);
            darkModeToggle.setPrefHeight(45);

            darkModeToggle.setOnAction(e -> {
                DarkModeManager.getInstance().toggleDarkMode();
                darkModeToggle.setText(
                        DarkModeManager.getInstance().isDarkMode() ? "☀ Light Mode" : "🌙 Dark Mode"
                );
            });

            Button logoutButton = new Button("Logout");
            logoutButton.setPrefWidth(200);
            logoutButton.setPrefHeight(45);
            logoutButton.setOnAction(e -> {
                hideSettingsPopup();
                logout();
            });

            Button closeButton = new Button("Close");
            closeButton.setPrefWidth(200);
            closeButton.setPrefHeight(45);
            closeButton.setOnAction(e -> hideSettingsPopup());

            settingsPopup.getChildren().addAll(darkModeToggle, logoutButton, closeButton);

            StackPane.setAlignment(settingsPopup, Pos.TOP_RIGHT);
            StackPane.setMargin(settingsPopup, new Insets(80, 20, 0, 0));

            settingsPopup.setOpacity(0);
            rootPane.getChildren().add(settingsPopup);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), settingsPopup);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        } else {
            hideSettingsPopup();
        }
    }

    private void hideSettingsPopup() {
        if (settingsPopup != null) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), settingsPopup);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                rootPane.getChildren().remove(settingsPopup);
                settingsPopup = null;
            });
            fadeOut.play();
        }
    }

    private void loadCarsPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/cars.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) square_button.getScene().getWindow();

            Scene scene = new Scene(root);

            DarkModeManager.getInstance().registerScene(scene);

            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading cars page: " + e.getMessage());
        }
    }

    private void logout() {
        try {
            SessionStaff.getInstance().clearSession();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/staffLogin.fxml"));
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