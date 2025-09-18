package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class adminSettingController {

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField textField;

    @FXML
    private Button toggleEyeButton;

    @FXML
    private PasswordField newpasswordField;

    @FXML
    private TextField newtextField;

    @FXML
    private Button newtoggleEyeButton;

    @FXML
    private PasswordField com_passwordField;

    @FXML
    private TextField com_textField;

    @FXML
    private Button com_toggleEyeButton;

    @FXML
    private Button comfirmbtn;

    @FXML
    private Button deketebtn;

    @FXML
    void clickComfirm(ActionEvent event) {

    }

    @FXML
    void clickDelete(ActionEvent event) {
        textField.clear();
        passwordField.clear();
        newtextField.clear();
        newpasswordField.clear();
        com_passwordField.clear();
        com_textField.clear();
    }
    private boolean isPasswordVisible=false;
    private boolean isNewPasswordVisible=false;
    private boolean isComPasswordVisible=false;


@FXML
    private void initialize() {
        // Keep both fields synced
        textField.textProperty().bindBidirectional(passwordField.textProperty());
        newtextField.textProperty().bindBidirectional(newpasswordField.textProperty());
        com_passwordField.textProperty().bindBidirectional(com_textField.textProperty());
    }

    @FXML
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Hide password (show dots)
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            textField.setVisible(false);
            textField.setManaged(false);
            toggleEyeButton.setText("👁"); // closed eye icon
            isPasswordVisible = false;
        } else {
            // Show password (plain text)
            textField.setVisible(true);
            textField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            toggleEyeButton.setText("🙈"); // open eye icon
            isPasswordVisible = true;
        }
    }
    @FXML
    public void toggleNewPasswordVisibility() {
    if (isNewPasswordVisible) {
        newpasswordField.setVisible(true);
        newpasswordField.setManaged(true);
        newtextField.setVisible(false);
        newtextField.setManaged(false);
        newtoggleEyeButton.setText("👁");
        isNewPasswordVisible=false;
    }
    else {
        newpasswordField.setVisible(false);
        newpasswordField.setManaged(false);
        newtextField.setVisible(true);
        newtextField.setManaged(true);
        newtoggleEyeButton.setText("🙈");
        isNewPasswordVisible=true;
    }
    }
    @FXML
    private void toggleComPasswordVisibility() {
        if (isComPasswordVisible) {
            com_passwordField.setVisible(true);
            com_passwordField.setManaged(true);
            com_textField.setVisible(false);
            com_textField.setManaged(false);
            com_toggleEyeButton.setText("👁");
            isComPasswordVisible=false;
        }
        else {
            com_passwordField.setVisible(false);
            com_passwordField.setManaged(false);
            com_textField.setVisible(true);
            com_textField.setManaged(true);
            com_toggleEyeButton.setText("🙈");
            isComPasswordVisible=true;

        }
    }
}
