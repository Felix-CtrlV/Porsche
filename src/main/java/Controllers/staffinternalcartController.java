package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import java.io.IOException;

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

    public staffinternalcartController() throws IOException {
    }


    @FXML
    void clickcomfirmbtn(ActionEvent event) {
        String name = namelbl.getText();
        double price = Double.parseDouble(pricelbl.getText());

        // Get instance of shopping cart controller and add accessory
        if (staffShopingCartControllerInstance != null) {
            staffShopingCartControllerInstance.addAccessory(name, price);
        }

        System.out.println("Accessory confirmed: " + name + " - " + price);
    }


    @FXML
    void clickdeletebtn(ActionEvent event) {
        namelbl.setText("");
        pricelbl.setText("");

    }
    private staffShopingCartController staffShopingCartControllerInstance;

    public void setCartController(staffShopingCartController controller) {
        this.staffShopingCartControllerInstance = controller;}

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
    FXMLLoader loader = new FXMLLoader(getClass().getResource("staffShopingCart.fxml"));
    Parent root = loader.load();
    staffShopingCartController cartController = loader.getController();

}
