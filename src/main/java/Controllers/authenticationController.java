package Controllers;

import Utils.Session;
import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Objects;

public class authenticationController {

    @FXML
    private StackPane errorPane;

    @FXML
    private Label errorMsg;

    @FXML
    private PasswordField pwtxt;

    @FXML
    private ImageView closeImg;

    @FXML
    private Button submit, factorPw;

    @FXML
    void clickSubmit(ActionEvent event) {
        if (pwtxt.getText().isEmpty()) {
            slideError("Please Enter the Password");
        } else {
            String pw = pwtxt.getText();
            Session current = Session.getInstance();

            if (pw.equals(current.getPassword())) {
                if (dashboardController != null) {
                    // Check if this is for password change (OTP flow)
                    if (isPasswordChangeFlow) {
                        dashboardController.proceedWithOTP();
                    } else {
                        dashboardController.setProfile();
                    }
                }
                Stage stage = (Stage) submit.getScene().getWindow();
                stage.close();
            } else {
                slideError("Wrong Password");
                pwtxt.clear();
                pwtxt.requestFocus();
            }
        }
    }


    private adminDashboardController dashboardController;
    private boolean isPasswordChangeFlow = false;

    public void init(adminDashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    public void init(adminDashboardController dashboardController, boolean isPasswordChangeFlow) {
        this.dashboardController = dashboardController;
        this.isPasswordChangeFlow = isPasswordChangeFlow;
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
        errorPane.setTranslateY(34);
        String step = null;
        setStep(step);

        // Image is already set in FXML, no need to set it again
        // closeImg.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Image/closeRemoved.png"))));

        closeImg.setOnMouseClicked(e -> {
            Stage stage = (Stage) closeImg.getScene().getWindow();
            stage.close();
        });

        pwtxt.setOnKeyPressed(pwevent -> {
            if (pwevent.getCode() == KeyCode.ENTER) {
                submit.fire();
            }
        });
    }

    private SequentialTransition currentAnimation;

    private void slideError(String error) {
        if (currentAnimation != null) {
            currentAnimation.stop();
            errorPane.setTranslateY(42);
            errorPane.setTranslateY(4);
        }
        errorMsg.setText(error);
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), errorPane);
        slideIn.setFromY(34);
        slideIn.setToY(2);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), errorPane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ParallelTransition show = new ParallelTransition(slideIn, fadeIn);

        PauseTransition pause = new PauseTransition(Duration.seconds(2));

        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), errorPane);
        slideOut.setFromY(2);
        slideOut.setToY(34);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), errorPane);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        ParallelTransition hide = new ParallelTransition(slideOut, fadeOut);

        currentAnimation = new SequentialTransition(show, pause, hide);
        currentAnimation.play();
    }

}
