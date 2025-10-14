package Controllers;

import Model.car;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class staffCustomizeController {
    @FXML private Label modelLabel;
    @FXML private Label priceLabel;

    private car selectedCar;

    public void setSelectedModel(String name, String img, String price) {
        modelLabel.setText(name);
        priceLabel.setText(price);
    }

    public void setSelectedCar(car selectedCar) {
        this.selectedCar = selectedCar;
    }
}
