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
    private Button confirmbtn;

    private int quantity = 0;

    public void setData(String name, double price) {
        namelbl.setText(name);
        pricelbl.setText(String.format("%.2f", price));
        numberlbl.setText("0");
    }

    @FXML
    void clickConfirmbtn(ActionEvent event) {
        // You can handle cart confirmation here if needed
        String name = namelbl.getText();
        double price = Double.parseDouble(pricelbl.getText());
        int qty = Integer.parseInt(numberlbl.getText());
        System.out.println("Item confirmed: " + name + " - " + price + " x " + qty);
    }

    @FXML
    void clickDeletebtn(ActionEvent event) {
        namelbl.setText("");
        pricelbl.setText("");
        numberlbl.setText("0");
    }

    @FXML
    void clickMinusbtn(ActionEvent event) {
        int i = Integer.parseInt(numberlbl.getText());
        if (i > 0) {
            i--;
            numberlbl.setText(String.valueOf(i));
        }
    }

    @FXML
    void clickPlusebtn(ActionEvent event) {
        int i = Integer.parseInt(numberlbl.getText());
        if (i < 100) {
            i++;
            numberlbl.setText(String.valueOf(i));
        }
    }
}
