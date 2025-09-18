package main.java.Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
public class customizeController {

    @FXML
    private ImageView Car_model_display_image;

    @FXML
    private ImageView car_model_view_image;

    @FXML
    private Button carPrevBtn;

    @FXML
    private Button carNextBtn;

    @FXML
    private Label car_frame_num;

    @FXML
    private Label fuel_type;

    @FXML
    private Label car_model_name;

//    @FXML
//    private Line car_description;

    @FXML
    private VBox wheel_selector;

    @FXML
    private Label rim_price;

    @FXML
    private Button wheelPrevBtn;

    @FXML
    private ImageView wheels_image;

    @FXML
    private Button wheelNextBtn;

    @FXML
    private VBox Tyre_selector;

    @FXML
    private Label tyre_price;

    @FXML
    private Button tyrePrevBtn;

    @FXML
    private ImageView tyres_image;

    @FXML
    private Button tyreNextBtn;

    @FXML
    private VBox color_selector;

    @FXML
    private Label color_price;

    @FXML
    private Button colorPrevBtn;

    @FXML
    private ImageView color_image;

    @FXML
    private Button colorNextBtn;

    @FXML
    private ImageView porsche_logo_image;

    @FXML
    private Button modelbtn;

    @FXML
    private Button customizebtn;

    @FXML
    private Button cartbtn;

    @FXML
    private Button confirmorderbtn;

    public void initialize() {
        Image porsche_logo = new Image(getClass().getResourceAsStream("/Image/porsche_logo.png"));
        porsche_logo_image.setImage(porsche_logo);
        Image car_model_view = new Image(getClass().getResourceAsStream("/Image/911_gt3_rs_car_model.png"));
        car_model_view_image.setImage(car_model_view);
        Image car_modell_text = new Image(getClass().getResourceAsStream("/Image/911_gt3rs_displaytext.png"));
        Car_model_display_image.setImage(car_modell_text);
    }
    @FXML
    void clickCarNext(ActionEvent event) {

    }

    @FXML
    void clickCarPrev(ActionEvent event) {

    }

    @FXML
    void clickCart(ActionEvent event) {

    }

    @FXML
    void clickColorNext(ActionEvent event) {

    }

    @FXML
    void clickColorPerv(ActionEvent event) {

    }

    @FXML
    void clickConfirm(ActionEvent event) {

    }

    @FXML
    void clickCustomize(ActionEvent event) {

    }

    @FXML
    void clickModels(ActionEvent event) {

    }

    @FXML
    void clickTyreNext(ActionEvent event) {

    }

    @FXML
    void clickTyrePrev(ActionEvent event) {

    }

    @FXML
    void clickWheelNext(ActionEvent event) {

    }

    @FXML
    void clickWheelPrev(ActionEvent event) {

    }

}


