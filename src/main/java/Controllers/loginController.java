package Controllers;

import Database.Porsche_DB;
import Model.user;
import Utils.Session;
import Utils.defaultStage;
import javafx.animation.*;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public class loginController {

    @FXML
    private MediaView videoview;

    private Media media;
    private MediaPlayer mediaPlayer;

    @FXML
    private PasswordField pwtxt;

    @FXML
    private TextField nametxt;

    @FXML
    private Button login;

    @FXML
    private Button clear;

    @FXML
    private ImageView usericon_image;

    @FXML
    private ImageView porsche_image;

    @FXML
    private ImageView porsche_logo_image;

    @FXML
    private ImageView closeimg;

    @FXML
    private StackPane errorPane;

    @FXML
    private Label errorlbl;

    @FXML
    void clickClear(ActionEvent event) {
        nametxt.clear();
        pwtxt.clear();
        nametxt.requestFocus();
    }

    private SequentialTransition currentAnimation;

    private void slideError(String errormsg){
        if(currentAnimation!=null){
            currentAnimation.stop();
            errorPane.setTranslateY(42);
            errorPane.setTranslateY(4);
        }
        errorlbl.setText(errormsg);
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), errorPane);
        slideIn.setFromY(42);
        slideIn.setToY(4);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), errorPane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ParallelTransition show = new ParallelTransition(slideIn, fadeIn);

        PauseTransition pause = new PauseTransition(Duration.seconds(2));

        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), errorPane);
        slideOut.setFromY(4);
        slideOut.setToY(42);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), errorPane);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        ParallelTransition hide = new ParallelTransition(slideOut, fadeOut);

        currentAnimation = new SequentialTransition(show, pause, hide);
        currentAnimation.play();
    }

    @FXML
    void clickLogin(ActionEvent event) throws IOException, SQLException, ClassNotFoundException {
        login.setDisable(true);
        login.setText("Loading...");
        clear.setDisable(true);
        if (nametxt.getText().isBlank()) {
            slideError("ERROR : Please Fill the Username");
            login.setDisable(false);
            login.setText("Login");
            clear.setDisable(false);
            nametxt.requestFocus();
        }else if(pwtxt.getText().isBlank()){
            slideError("ERROR : Please Fill the Password");
            login.setDisable(false);
            login.setText("Login");
            clear.setDisable(false);
            pwtxt.requestFocus();
        }else{
            Task<user> task = new Task<user>() {
                @Override
                protected user call() throws Exception {
                    String name = nametxt.getText();
                    String password = pwtxt.getText();
                    Porsche_DB connect = new Porsche_DB();
                    Connection con = connect.connect();

                    CallableStatement p1 = con.prepareCall("call login(?,?,?)");
                    p1.setString(1, name);
                    p1.setString(2, password);
                    p1.setString(3, String.valueOf(LocalDateTime.now()));
                    ResultSet rs = p1.executeQuery();
                    user u = null;
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        String role = rs.getString(7);
                        String pw = rs.getString(3);
                        String email = rs.getString(4);
                        String address = rs.getString(5);
                        String dob = rs.getString(6);

                        u = new user(id, name, role, pw, email, address, LocalDate.parse(dob));
                    }
                    connect.disconnect();
                    return u;
                }
            };
            task.setOnSucceeded(e1 -> {
                user u = task.getValue();
                String role = u.getRole();
                int id = u.getId();
                String username = u.getUsername();
                String password = u.getPassword();
                String email = u.getEmail();
                String address = u.getAddress();
                LocalDate dob = u.getDob();

                if (role == null) {
                    login.setDisable(false);
                    login.setText("Login");
                    clear.setDisable(false);

                    slideError("ERROR : Username or Password is INCORRECT!");
                    nametxt.clear();
                    pwtxt.clear();
                    nametxt.requestFocus();
                } else {
                    Session.startSession(id, username, role, password, email, address, dob);
                    try {
                        Parent root = switch (u.getRole().toLowerCase()) {
                            case "admin" ->
                                    FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/View/adminDashboard.fxml")));
                            case "manager" ->
                                    FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/View/managerDashboard.fxml")));
                            case "staff" ->
                                    FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/View/StaffDashboard.fxml")));
                            default ->
                                    throw new IllegalStateException("Unknown role: " + u.getRole());
                        };
                        Stage stage = new Stage();
                        stage.setScene(new Scene(root));
                        defaultStage DStage = new defaultStage();
                        DStage.setStage(stage);
                        stage.show();
                        Stage home = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        home.close();
                    } catch (
                            IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            task.setOnFailed(e -> {
                slideError("No Connection. Please TRY AGAIN...");
                login.setDisable(false);
                login.setText("Login");
                clear.setDisable(false);
                task.getException().printStackTrace();
            });
            new Thread(task).start();
        }
    }

    public void initialize() throws URISyntaxException {
        Media media = new Media(Objects.requireNonNull(getClass().getResource("/Image/porscheFinal.mp4")).toExternalForm());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        mediaPlayer.setAutoPlay(true);

        mediaPlayer.setOnReady(() -> {
            videoview.setMediaPlayer(mediaPlayer);
            mediaPlayer.play();
        });

        nametxt.setPromptText("Fill your Username");
        pwtxt.setPromptText("Fill your Password");

        Image porsche_text = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Image/porsche_text.png")));
        porsche_image.setImage(porsche_text);
        Image usericon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Image/usericon.png")));
        usericon_image.setImage(usericon);
        Image porsche_logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Image/porsche_logo.png")));
        porsche_logo_image.setImage(porsche_logo);
        Image close = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/Image/close.png")));
        closeimg.setImage(close);

        nametxt.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                pwtxt.requestFocus();
            }
        });
        pwtxt.setOnKeyPressed(pwevent -> {
            if (pwevent.getCode() == KeyCode.ENTER) {
                login.fire();
            }
        });

        closeimg.setOnMouseClicked(e->{
            System.exit(0);
        });
    }
}
