package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class managerOrderManagementController {

    @FXML
    private TextField lblsearch;

    @FXML
    private Button btnclick;

    @FXML
    private Button PreviousMonthbtn;

    @FXML
    private Label Monthslabel;

    @FXML
    private Button NextMonthbtn;

    @FXML
    private Button PreviousYearbth;

    @FXML
    private Label Yearslabel;

    @FXML
    private Button NextYearbtn;

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
    private LineChart<?, ?> orderTrendChart;

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
    private Label totalPriceLabel12;

    @FXML
    private Label totalPriceLabel1;

    @FXML
    private Label totalPriceLabel11;

    @FXML
    void clickClick(ActionEvent event) {

    }

}
