package Controllers;

import Database.Porsche_DB;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.*;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;

public class adminUserRegisterController {

    @FXML
    private Button registerbtn;

    @FXML
    private Button clearbtn;

    @FXML
    private TextField usernametx;

    @FXML
    private TextField emailtx;

    @FXML
    private TextField addresstx;

    @FXML
    private TextField phonetx;

    @FXML
    private PasswordField password;

    @FXML
    private DatePicker dob;

    @FXML
    private ComboBox<?> rolecombo;

    @FXML
    private ComboBox<String> nrc_first;

    @FXML
    private ComboBox<String> nrc_second;

    @FXML
    private ComboBox<String> nrc_third;

    @FXML
    private TextField nrc_number;

    @FXML
    void clickClear(ActionEvent event) {
        usernametx.clear();
        emailtx.clear();
        addresstx.clear();
        phonetx.clear();
        password.clear();
        dob.setValue(null);
        nrc_first.getSelectionModel().clearSelection();
        nrc_second.getSelectionModel().clearSelection();
        nrc_third.getSelectionModel().clearSelection();
        nrc_number.clear();
    }
    @FXML
    public void errorshow() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/View/error.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(root);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setAlwaysOnTop(true);
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setScene(scene);
        stage.show();
    }

//    private errorController error;
//
//    public void setError(errorController error){
//        this.error = error;
//    }

    @FXML
    void clickregister(ActionEvent event) throws SQLException, ClassNotFoundException, IOException {

        if (usernametx.getText().isBlank() || emailtx.getText().isBlank() || nrc_first.getValue() == null || nrc_second.getValue() == null || nrc_third.getValue() == null || nrc_number.getText().isBlank() || addresstx.getText().isBlank() || phonetx.getText().isBlank() || rolecombo.getValue() == null || dob.getValue() == null || password.getText().isBlank()) {

            if (usernametx.getText().isBlank()) {
//                error.setErrortxt(new Label("Please Fill your Name"));
                errorshow();
            } else if (emailtx.getText().isBlank()) {
//                error.setErrortxt(new Label("Please Fill your Email"));
                errorshow();
            } else if (nrc_first.getValue() == null || nrc_second.getValue() == null || nrc_third.getValue() == null || nrc_number.getText().isBlank()) {
//                error.setErrortxt(new Label("Please Fill your Full NRC"));
                errorshow();
            } else if (addresstx.getText().isBlank()) {
//                error.setErrortxt(new Label("Please Fill your Address"));
                errorshow();
            } else if (phonetx.getText().isBlank()) {
//                error.setErrortxt(new Label("Please Fill your Phone Number"));
                errorshow();
            } else if (rolecombo.getValue() == null) {
//                error.setErrortxt(new Label("Please Fill your Role"));
                errorshow();
            } else if (dob.getValue() == null) {
//                error.setErrortxt(new Label("Please Fill your BirthDay"));
                errorshow();
            } else if (password.getText().isBlank()) {
//                error.setErrortxt(new Label("Please Fill your Password"));
                errorshow();
            }
        } else {
            String name = usernametx.getText();
            String email = emailtx.getText();
            String Nrc = nrc_first.getValue() + "/" + nrc_second.getValue() + "(" + nrc_third.getValue() + ")" + nrc_number.getText();
            String address = addresstx.getText();
            String phone = phonetx.getText();
            String role = (String) rolecombo.getSelectionModel().getSelectedItem();
            String date_of_birth = String.valueOf(dob.getValue());
            String pw = password.getText();
            Porsche_DB connect = new Porsche_DB();
            Connection con = connect.connect();
            CallableStatement c = con.prepareCall("call create_user(?,?,?,?,?,?,?,?)");
            c.setString(1, name);
            c.setString(2, email);
            c.setString(3, Nrc);
            c.setString(4, address);
            c.setString(5, phone);
            c.setString(6, role);
            c.setString(7, date_of_birth);
            c.setString(8, pw);

            int r = c.executeUpdate();
            if (r > 0) {
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setContentText("Register Successfully");
                success.setTitle("Success");
                success.show();
            }
            connect.disconnect();
        }
    }

    public void initialize() {

        dob.setEditable(false);
        dob.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                if (date.isAfter(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #eeeeee;");
                }
            }
        });

        nrc_first.setOnAction(e -> {
            String selected = nrc_first.getValue();
            nrc_second.getItems().clear();
            switch (selected) {
                case "1" ->
                        nrc_second.getItems().addAll("BaMaNa", "KhaHpaNa", "DaHpaNa", "HaPaNa"
                                , "HpaKaNa", "AhGaYa", "KaMaTa", "KaPaTa", "KhaLaHpa", "LaGaNa"
                                , "MaKhaBa", "MaSaNa", "MaKaTa", "MaNyaNa", "MaMaNa", "MaLaNa"
                                , "NaMaNa", "PaWaNa", "PaNaDa", "PaTaAh", "SaDaNa", "YaBaYa"
                                , "YaKaNa", "SaBaNa", "SaPaYa", "TaNaNa", "TaSaLa", "WaMaNa");
                case "2" ->
                        nrc_second.getItems().addAll("BaLaKha", "DaMaSa", "HpaSaNa", "HpaYaSa"
                                , "LaKaNa", "MaSaNa", "YaTaNa", "YaThaNa");
                case "3" ->
                        nrc_second.getItems().addAll("BaGaLa", "LaBaNa", "BaAhNa", "HpaPaNa", "BaThaSa"
                                , "KaMaMa", "KaKaYa", "KaDaNa", "KaSaKa", "KaDaTa", "LaThaNa"
                                , "MaWaTa", "PaKaNa", "YaYaTha", "SaKaLa", "ThaTaNa", "ThaTaKa", "WaLaMa");
                case "4" ->
                        nrc_second.getItems().addAll("KaKhaNa", "HpaLaNa", "HaKhaNa", "KaPaLa", "MaTaPa"
                                , "MaTaNa", "PaLaWa", "YaZaNa", "YaKhaDa", "SaMaNa", "TaTaNa", "HtaTaLa", "TaZaNa");
                case "5" ->
                        nrc_second.getItems().addAll("AhYaTa", "BaMaNa", "BaTaLa", "KhaOuTa", "KhaTaNa"
                                , "HaMaLa", "AhTaNa", "KaLaHta", "KaLaWa", "KaBaLa", "KaNaNa", "KaThaNa", "KaLaTa"
                                , "KhaOuNa", "KaLaNa", "LaHaNa", "LaYaNa", "MaLaNa", "MaKaNa", "MaYaNa", "MaMaNa"
                                , "MaMaTa", "NaYaNa", "NgaZaNa", "PaLaNa", "HpaPaNa", "PaLaBa", "SaKaNa", "SaLaKa", "YaBaNa"
                                , "DaPaYa", "TaMaNa", "TaSaNa", "HtaKhaNa", "WaLaNa", "WaThaNa", "YaOuNa", "'YaMaPa", "KaMaNa", "KhaPaNa");
                case "6" ->
                        nrc_second.getItems().addAll("BaPaNa", "HtaWaNa", "KaLaAh", "KaThaNa", "KaSaNa", "LaLaNa"
                                , "MaMaNa", "PaLaNa", "TaThaYa", "ThaYaKha", "YaHpaNa", "KhaMaNa", "MaTaNa", "PaLaTa", "KaYaYa");
                case "7" ->
                        nrc_second.getItems().addAll("DaOuNa", "KaPaKa", "KaWaNa", "KaKaNa", "KaTaKha", "LaPaTa"
                                , "MaLaNa", "MaNyaNa", "NaTaLa", "NyaLaPa", "AhHpaNa", "AhTaNa", "PaTaNa", "PaKhaTa", "PaKhaNa"
                                , "PaTaTa", "PaNaKa", "HpaMaNa", "PaMaNa", "YaTaNa", "YaKaNa", "HtaTaPa", "TaNgaNa"
                                , "ThaNaPa", "ThaWaTa", "ThaKaNa", "ThaSaNa", "WaMaNa", "YaTaYa", "ZaKaNa", "PaTaSa");
                case "8" ->
                        nrc_second.getItems().addAll("AhLaNa", "KhaMaNa", "GaGaNa", "KaMaNa", "MaKaNa", "MaBaNa", "MaTaNa", "MaLaNa"
                                , "MaMaNa", "MaHtaNa", "MaThaNa", "NaMaNa", "NgaHpaNa", "PaKhaKa", "PaMaNa", "PaHpaNa", "SaLaNa", "SaMaNa"
                                , "SaHpaNa", "SaTaYa", "SaPaWa", "TaTaKa", "ThaYaNa", "HtaLaNa", "YaNaKha", "YaSaKa", "KaHtaNa");
                case "9" ->
                        nrc_second.getItems().addAll("AhMaya", "AhMaZa", "KhaAhZa", "KhaMaSa", "KaPaTa", "KaSaNa"
                                , "MaTaYa", "MaHaMa", "MaLaNa", "MaHtaLa", "MaKaNa", "MaKhaNa", "MaThaNa", "NaHtaKa"
                                , "NgaThaYa", "NgaZaNa", "NyaOuNa", "PaThaKa", "PaBaNa", "PaKaKha", "PaOuLa", "SaKaNa"
                                , "SaKaTa", "ThaPaKa", "TaTaOu", "TaThaNa", "ThaSaNa", "WaTaNa", "YaMaTha", "TaKaTa"
                                , "MaMaNa", "DaKhaTha", "LaWaNa", "0uTaTha", "PaBaTha", "PaMaNa", "TaKaNa", "ZaBaTha", "ZaYaTha");
                case "10" ->
                        nrc_second.getItems().addAll("BaLaNa", "KhaSaNa", "KhaZaNa", "KaMaYa", "KaHtaNa", "LaMaNa"
                                , "MaLaMa", "MaDaNa", "PaMaNa", "ThaHpaYa", "ThaHtaNa", "NaMaNa");
                case "11" ->
                        nrc_second.getItems().addAll("AhMaNa", "BaThaTa", "GaMaNa", "KaHpaNa", "KaTaNa", "MaAhTa"
                                , "MaTaNa", "MaPaNa", "MaAhNa", "MaOuNa", "MaPaTa", "PaTaNa", "PaNaTa", "YaBaNa"
                                , "YaThaTa", "SaTaNa", "ThaTaNa", "TaKaNa", "KaTaLa", "TaPaWa");
                case "12" ->
                        nrc_second.getItems().addAll("AhLaNa", "BaHaNa", "BaTaHta", "KaKaKa", "DaGaYa", "DaGaMa", "DaGaSa"
                                , "DaGaTa", "DaGaNa", "DaLaNa", "DaPaNa", "LaThaYa", "LaMaNa", "LaKaNa", "MaBaNa", "HtaTaPa"
                                , "AhSaNa", "KaMaYa", "KaMaNa", "KhaYaNa", "KaKhaKa", "KaTaTa", "KaTaNa", "KaMaTa", "LaMaTa"
                                , "LaThaNa", "MaYaKa", "MaGaDa", "MaGaTa", "0uKaMa", "PaBaTa", "PaZaTa", "SaKhaNa", "SaKaKha"
                                , "SaKaNa", "YaPaTha", "0uKaTa", "TaTaHta", "TaKaNa", "TaMaNa", "ThaKaTa", "ThaLaNa"
                                , "ThaGaKa", "ThaKhaNa", "TaTaNa", "YaKaNa", "0uKaNa");
                case "13" ->
                        nrc_second.getItems().addAll("AhKhaNa", "KhaYaHa", "KhaMaNa", "HaTaNa", "HaPaNa", "HaPaTa"
                                , "SaHpaNa", "ThaNaNa", "SaSaNa", "ThaPaNa", "KaLaHpa", "KaLaNa", "KaLaDa", "KaMaSa", "KaTaNa"
                                , "KaYaNa", "KaTaTa", "KaHaNa", "KaLaTa", "KaKhaNa", "KaMaNa", "KaTaLa", "KaThaNa", "LaKhaNa"
                                , "LaKhaTa", "LaYaNa", "LaKaNa", "LaHaNa", "LaLaNa", "MaBaNa", "MaMaSa", "MaTaNa", "MaTaTa"
                                , "MaMaNa", "MaMaNa", "MaHpaNa", "MaKaNa", "MaPaNa", "MaHpaNa", "MaSaNa", "MaYaNa", "MaKaNa"
                                , "MaKhaNa", "MaLaNa", "MaMaTa", "MaMaTa", "MaNaNa", "MaPaNa", "MaTaNa", "MaYaTa", "MaYaNa"
                                , "MaYaNa", "MaSaTa", "NaKhaWa", "NaTaNa", "NaKhaNa", "NaMaTa", "NaHpaNa", "NaSaNa", "NaKaNa"
                                , "NaWaNa", "NaPhaNa", "NaKhaNa", "NaKhaTa", "NyaYaNa", "PaKhaNa", "PaYaNa", "PaSaNa", "PaWaNa"
                                , "HpaKhaNa", "PaTaYa", "PaLaNa", "TaKhaLa", "TaYaNa", "TaKaNa", "YaLaNa", "YaSaNa", "YaHpaNa"
                                , "YaNgaNa", "NaTaYa", "PaLaTa", "KhaLaNa", "MaHaYa", "PaPaKa", "TaMaNya", "MaBaTa", "MaNgaNa"
                                , "AhTaNa", "TaLaNa");
                case "14" ->
                        nrc_second.getItems().addAll("AhMaTa", "BaKaLa", "DaNaHpa", "DaDaYa", "HaKaKa", "HaThaTa"
                                , "AhGaPa", "KaKaHta", "KaLaNa", "KaKhaNa", "KaKaNa", "KaPaNa", "LaPaTa", "LaMaNa"
                                , "MaAhPa", "MaMaKa", "MaMaNa", "NgaPaTa", "NgaThaKha", "NgaYaKa", "NgaSaNa"
                                , "NgaThaYa", "NyaTaNa", "NyaTaNa", "PaTaNa", "PaThaNa", "HpaPaNa", "PaSaLa"
                                , "YaThaYa", "ThaPaNa", "WaKhaMa", "YaKaNa", "ZaLaNa");
            }
        });
        nrc_number.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                nrc_number.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }
}

