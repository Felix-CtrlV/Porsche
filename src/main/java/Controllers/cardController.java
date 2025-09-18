package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class cardController {

    @FXML
    private Label StaffId;

    @FXML
    private Label StaffName;

    @FXML
    private Circle ActiveColor;

    private int staffId;

    public void setData(int id, String name, boolean isActive) {
        this.staffId = id;
        StaffId.setText(String.valueOf(id));
        StaffName.setText(name);
        ActiveColor.setFill(isActive ? Color.LIME : Color.RED);
    }

    public int getStaffId() {
        return staffId;
    }
}
