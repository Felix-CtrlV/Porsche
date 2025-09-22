package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class managerStaffInstallmentController {

    @FXML
    private Label Name;

    @FXML
    private Label Price;

    @FXML
    private Label Quantity;

    public void setData(String name,String  qty ,String price){
        Name.setText(name);
        Quantity.setText(qty);
        Price.setText(price);

    }

}
