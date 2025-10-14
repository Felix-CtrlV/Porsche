package Controllers;

import Database.Porsche_DB;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.File;
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
    private ImageView photoPreview;
    
    @FXML
    private Button uploadPhotoBtn;
    
    @FXML
    private Label photoPathLabel;
    
    private String selectedPhotoPath = null;

    @FXML
    private DatePicker dob;

    @FXML
    private ComboBox<String> rolecombo;
    
    @FXML
    private TextField salarytx;
    
    @FXML
    private ComboBox<String> managerCombo;
    
    @FXML
    private VBox managerSection;
    
    @FXML
    private HBox toastNotification;
    
    @FXML
    private Label toastIcon, toastTitle, toastMessage;
    
    @FXML
    private Label ageLbl;

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
        dob.setValue(null);
        nrc_first.getSelectionModel().clearSelection();
        nrc_second.getSelectionModel().clearSelection();
        nrc_third.getSelectionModel().clearSelection();
        nrc_number.clear();
        rolecombo.getSelectionModel().clearSelection();
        salarytx.clear();
        managerCombo.getSelectionModel().clearSelection();
        selectedPhotoPath = null;
        photoPreview.setImage(null);
        photoPathLabel.setText("No file selected");
    }
    
    @FXML
    void clickUploadPhoto(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Photo");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );
        
        Stage stage = (Stage) uploadPhotoBtn.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            selectedPhotoPath = selectedFile.toURI().toString();
            photoPathLabel.setText(selectedFile.getName());
            
            try {
                Image image = new Image(selectedPhotoPath);
                photoPreview.setImage(image);
            } catch (Exception e) {
                showToast("Error", "Failed to load image. Please select a valid image file.", "error");
            }
        }
    }
    
    private void loadManagers() {
        try {
            Porsche_DB connect = new Porsche_DB();
            Connection con = connect.connect();
            
            // Only load users with Manager role
            String query = "SELECT user_name FROM user_info WHERE user_role = 'Manager'";
            PreparedStatement ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            
            managerCombo.getItems().clear();
            while (rs.next()) {
                managerCombo.getItems().add(rs.getString("user_name"));
            }
            
            connect.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Old showNotification method removed - now using showToast
    
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
    
    private boolean isValidPhone(String phone) {
        // Myanmar phone format: 09XXXXXXXXX (11 digits starting with 09)
        String phoneRegex = "^09\\d{7,9}$";
        return phone.matches(phoneRegex);
    }
    
    private void updateAge() {
        if (dob.getValue() != null) {
            LocalDate birthDate = dob.getValue();
            LocalDate now = LocalDate.now();
            int age = now.getYear() - birthDate.getYear();
            
            // Adjust if birthday hasn't occurred yet this year
            if (now.getMonthValue() < birthDate.getMonthValue() || 
                (now.getMonthValue() == birthDate.getMonthValue() && now.getDayOfMonth() < birthDate.getDayOfMonth())) {
                age--;
            }
            
            ageLbl.setText(age + " Y/O");
            ageLbl.setStyle("-fx-text-fill: #3b82f6; -fx-font-size: 13; -fx-font-weight: bold;");
        } else {
            ageLbl.setText("");
        }
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


    @FXML
    void clickregister(ActionEvent event) throws SQLException, ClassNotFoundException, IOException {

        // Validation
        boolean isStaff = "Staff".equals(rolecombo.getValue());
        
        if (usernametx.getText().isBlank() || emailtx.getText().isBlank() || nrc_first.getValue() == null || nrc_second.getValue() == null || nrc_third.getValue() == null || nrc_number.getText().isBlank() || addresstx.getText().isBlank() || phonetx.getText().isBlank() || rolecombo.getValue() == null || dob.getValue() == null || salarytx.getText().isBlank() || (isStaff && managerCombo.getValue() == null)) {

            String errorMsg = "";
            
            if (usernametx.getText().isBlank()) {
                errorMsg = "Please fill in the username";
            } else if (emailtx.getText().isBlank()) {
                errorMsg = "Please fill in the email";
            } else if (!isValidEmail(emailtx.getText())) {
                errorMsg = "Please enter a valid email address (e.g., user@example.com)";
            } else if (nrc_first.getValue() == null || nrc_second.getValue() == null || nrc_third.getValue() == null || nrc_number.getText().isBlank()) {
                errorMsg = "Please fill in the complete NRC";
            } else if (addresstx.getText().isBlank()) {
                errorMsg = "Please fill in the address";
            } else if (phonetx.getText().isBlank()) {
                errorMsg = "Please fill in the phone number";
            } else if (!isValidPhone(phonetx.getText())) {
                errorMsg = "Please enter a valid Myanmar phone number (e.g., 09XXXXXXXXX)";
            } else if (rolecombo.getValue() == null) {
                errorMsg = "Please select a role";
            } else if (dob.getValue() == null) {
                errorMsg = "Please select date of birth";
            } else if (salarytx.getText().isBlank()) {
                errorMsg = "Please enter salary amount";
            } else if (isStaff && managerCombo.getValue() == null) {
                errorMsg = "Please select a manager for this staff member";
            }
            
            showToast("Validation Error", errorMsg, "error");
        } else {
            String name = usernametx.getText();
            String email = emailtx.getText();
            String Nrc = nrc_first.getValue() + "/" + nrc_second.getValue() + "(" + nrc_third.getValue() + ")" + nrc_number.getText();
            String address = addresstx.getText();
            String phone = phonetx.getText();
            String role = rolecombo.getValue();
            String date_of_birth = String.valueOf(dob.getValue());
            String defaultPassword = "123456";
            double salary = Double.parseDouble(salarytx.getText().replace("$", "").replace(",", ""));
            String managerName = isStaff ? managerCombo.getValue() : null;
            
            Porsche_DB connect = new Porsche_DB();
            Connection con = connect.connect();
            
            // Call the stored procedure
            CallableStatement cs = con.prepareCall("{call createUser(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}");
            cs.setString(1, selectedPhotoPath != null ? selectedPhotoPath : "");
            cs.setString(2, name);
            cs.setString(3, email);
            cs.setString(4, Nrc);
            cs.setString(5, address);
            cs.setString(6, phone);
            cs.setString(7, role);
            cs.setString(8, date_of_birth);
            cs.setString(9, defaultPassword);
            cs.setDouble(10, salary);
            cs.setString(11, managerName);

            cs.execute();
            
            showToast("Success", "User registered successfully! Default password: 123456", "success");
            
            // Clear form after successful registration
            clickClear(null);
            
            connect.disconnect();
        }
    }

    public void initialize() {
        // Load managers for the dropdown
        loadManagers();
        
        // Setup role change listener to update salary and show/hide manager field
        rolecombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                switch (newVal) {
                    case "Staff":
                        salarytx.setText("1000");
                        managerSection.setVisible(true);
                        managerSection.setManaged(true);
                        break;
                    case "Manager":
                        salarytx.setText("2500");
                        managerSection.setVisible(false);
                        managerSection.setManaged(false);
                        break;
                    case "Admin":
                        salarytx.setText("5000");
                        managerSection.setVisible(false);
                        managerSection.setManaged(false);
                        break;
                }
            }
        });
        
        // Format salary input
        salarytx.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                salarytx.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        
        // Update age when date of birth changes
        dob.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateAge();
        });

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
    
    private void showToast(String title, String message, String type) {
        toastTitle.setText(title);
        toastMessage.setText(message);
        
        // Set icon and color based on type
        StackPane iconContainer = (StackPane) toastIcon.getParent();
        if (type.equals("success")) {
            toastIcon.setText("✓");
            iconContainer.setStyle("-fx-background-color: #10b981; -fx-background-radius: 18;");
        } else if (type.equals("error")) {
            toastIcon.setText("✕");
            iconContainer.setStyle("-fx-background-color: #ef4444; -fx-background-radius: 18;");
        } else if (type.equals("info")) {
            toastIcon.setText("ℹ");
            iconContainer.setStyle("-fx-background-color: #3b82f6; -fx-background-radius: 18;");
        }
        
        toastNotification.setVisible(true);
        toastNotification.setTranslateY(-100);
        
        // Slide in animation from top
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), toastNotification);
        slideIn.setFromY(-100);
        slideIn.setToY(0);
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), toastNotification);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        ParallelTransition showToast = new ParallelTransition(slideIn, fadeIn);
        
        // Auto hide after 3 seconds
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> hideToast());
        
        SequentialTransition sequence = new SequentialTransition(showToast, pause);
        sequence.play();
    }
    
    private void hideToast() {
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), toastNotification);
        slideOut.setFromY(0);
        slideOut.setToY(-100);
        
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), toastNotification);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        
        ParallelTransition hide = new ParallelTransition(slideOut, fadeOut);
        hide.setOnFinished(e -> toastNotification.setVisible(false));
        hide.play();
    }
}

