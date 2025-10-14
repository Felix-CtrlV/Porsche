package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

public class staffexternalcartController {

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

    private int quantity = 0;

    public staffexternalcartController() {
    }

    @FXML
    void clickcomfirmbtn(ActionEvent event) {
        String name = namelbl.getText();
        double price = Double.parseDouble(pricelbl.getText());
        int qty = Integer.parseInt(numberlbl.getText());

        // Handle confirmation here (e.g., add to cart)
        System.out.println("Item confirmed: " + name + " - " + price + " x " + qty);
    }

    @FXML
    void clickdeletebtn(ActionEvent event) {
        namelbl.setText("");
        pricelbl.setText("");
        numberlbl.setText("0");
        minusbtn.setVisible(true);
        plusebtn.setVisible(true);
    }

    @FXML
    void clickminusbtn(ActionEvent event) {
        int i = Integer.parseInt(numberlbl.getText());
        if (i > 0) {
            i--;
            numberlbl.setText(String.valueOf(i));
        }
        if (i == 0) {
            minusbtn.setVisible(false);
        } else {
            minusbtn.setVisible(true);
        }
    }

    @FXML
    void clickplusebtn(ActionEvent event) {
        int i = Integer.parseInt(numberlbl.getText());
        if (i < 100) {
            i++;
            numberlbl.setText(String.valueOf(i));
        }
        if (i == 100) {
            plusebtn.setVisible(false);
        } else {
            plusebtn.setVisible(true);
        }
    }

    public void setData(String name, Double price) {
        namelbl.setText(name);
        pricelbl.setText(String.format("%.2f", price));
        numberlbl.setText("0");
        minusbtn.setVisible(true);
        plusebtn.setVisible(true);
    }
}

