package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class staffinternalcartController {

    @FXML
    private ImageView img;

    @FXML
    private Label namelbl;

    @FXML
    private Label pricelbl;

    @FXML
    private Button deletebtn;

    @FXML
    private Button minusbtn;

    @FXML
    private Label numberlbl;

    @FXML
    private Button plusebtn;

    @FXML
    private Button comfirmbtn;

    @FXML
    void clickcomfirmbtn(ActionEvent event) {

    }

    @FXML
    void clickdeletebtn(ActionEvent event) {
        namelbl.setText("");
        pricelbl.setText("");

    }

    @FXML
    void clickminusbtn(ActionEvent event) {
        int i = Integer.parseInt(numberlbl.getText());
        if(i>0 ){
            i=i-1;
            numberlbl.setText("i");

        }
        else{
            minusbtn.setVisible(false);
        }
    }

    @FXML
    void clickplusebtn(ActionEvent event) {
        int i = Integer.parseInt(numberlbl.getText());
        if(i<100){
            i=i+1;
            numberlbl.setText("i");
        }
        else{
            plusebtn.setVisible(false);
        }

    }
    public void setData(String name,Double price){
        namelbl.setText(name);
        pricelbl.setText(price.toString());
        numberlbl.setText("0");

    }

}
