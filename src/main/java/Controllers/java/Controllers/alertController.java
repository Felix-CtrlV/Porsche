package Controllers.java.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class alertController {

    @FXML
    public Button cancelBtn;

    @FXML
    public Button confirmBtn;

    @FXML
    private Label warningText;

    private Runnable onConfirm;
    private Runnable onCancel;

    public void setAlert(String text) {
        warningText.setText(text);
    }

    public void setOnConfirm(Runnable action) {
        this.onConfirm = action;
    }

    public void setOnCancel(Runnable action) {
        this.onCancel = action;
    }

    @FXML
    private void initialize() {
        cancelBtn.setOnAction(e -> {
            if (onCancel != null) {
                onCancel.run();
            }
            closeWindow();
        });

        confirmBtn.setOnAction(e -> {
            if (onConfirm != null) {
                onConfirm.run();
            }
            closeWindow();
        });
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }
}