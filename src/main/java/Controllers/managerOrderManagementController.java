package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

public class managerOrderManagementController {

    @FXML
    private TableView<?> tblordermanagement;

    @FXML
    private TableColumn<?, ?> rollorderid;

    @FXML
    private TableColumn<?, ?> rollorderdate;

    @FXML
    private TableColumn<?, ?> rollstatus;

    @FXML
    private TableColumn<?, ?> rollisinstallmant;

    @FXML
    private TableColumn<?, ?> rollsellername;

    @FXML
    private TableColumn<?, ?> rollcustomername;

    @FXML
    private Label paidAmountLabel;

    @FXML
    private Label remainAmountLabel;

    @FXML
    private VBox orderItemsContainer;

    @FXML
    private Label totalPriceLabel;

    @FXML
    private VBox orderItemsContainer1;

    @FXML
    private Label totalPriceLabel1;

}
