package Controllers;

import Database.Porsche_DB;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

public class authenticationController {

    @FXML
    private PasswordField pwtxt;

    @FXML
    private Button submit;

    @FXML
    void clickSubmit(ActionEvent event) throws SQLException, ClassNotFoundException {
        String pw = pwtxt.getText();
        Porsche_DB connect = new Porsche_DB();
        Connection con = connect.connect();
        CallableStatement call = con.prepareCall("");

    }

}
