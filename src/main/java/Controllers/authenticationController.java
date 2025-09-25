package Controllers;

import Utils.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.Objects;

public class authenticationController {

    @FXML
    private PasswordField pwtxt;

    @FXML
    private ImageView closeImg;

    @FXML
    private Button submit, factorPw;

    @FXML
    void clickSubmit(ActionEvent event) {
        String pw = pwtxt.getText();
        Session current = Session.getInstance();

        if (pw.equals(current.getPassword())) {
            if (dashboardController != null) {
                dashboardController.setProfile();
            }

            Stage stage = (Stage) submit.getScene().getWindow();
            stage.close();
        }
    }


    private adminDashboardController dashboardController;

    public void init(adminDashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }


    @FXML
    void clickFactor(ActionEvent event) {
        System.out.println("clicked");
    }

    public void setStep(String step) {
        if (step == null)
            step = "password";

        if (step.equals("password")) {
            factorPw.setVisible(false);
            submit.setVisible(true);
        } else if (step.equals("factor")) {
            submit.setVisible(false);
            factorPw.setVisible(true);
        }
    }


    public void initialize() {
        String step = null;
        setStep(step);

        closeImg.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Image/closeRemoved.png"))));

        closeImg.setOnMouseClicked(e -> {
            Stage stage = (Stage) closeImg.getScene().getWindow();
            stage.close();
        });
    }

}
