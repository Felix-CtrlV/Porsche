package Controllers;

import Database.Porsche_DB;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.sql.CallableStatement;
import java.sql.Connection;
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
    void clickSubmit(ActionEvent event) throws SQLException, ClassNotFoundException {
        String pw = pwtxt.getText();
        Porsche_DB connect = new Porsche_DB();
        Connection con = connect.connect();
        CallableStatement call = con.prepareCall("");
    }

    @FXML
    void clickFactor(ActionEvent event){
        System.out.println("clicked");
    }

    public void setStep(String step){
        if(step == null) step = "password";

        if(step.equals("password")){
            factorPw.setVisible(false);
            submit.setVisible(true);
        } else if(step.equals("factor")){
            submit.setVisible(false);
            factorPw.setVisible(true);
        }
    }


    public void initialize(){
        String step = null;
        setStep(step);

        closeImg.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Image/closeRemoved.png"))));

        closeImg.setOnMouseClicked(e->{
            Stage stage = (Stage) closeImg.getScene().getWindow();
            stage.close();
        });
    }

}
