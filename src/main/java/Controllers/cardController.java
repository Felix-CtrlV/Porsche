package main.java.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;

public class cardController {

    @FXML
    private Circle ActiveColor;

    @FXML
    private Label StaffId;

    @FXML
    private ImageView StaffImage;

    @FXML
    private Label StaffName;


    @FXML
    private Polygon cardShapePolygon;


    public void setData(int id,String name,boolean status){

        StaffId.setText(String.valueOf(id));
        StaffName.setText(name);
//        if (imgPath != null && !imgPath.isEmpty()) {
//            StaffImage.setImage(new Image(imgPath));
//        }
        if (status) {
            ActiveColor.setStyle("-fx-fill: green;");
        } else {
            ActiveColor.setStyle("-fx-fill: red;");
        }

    }


}
