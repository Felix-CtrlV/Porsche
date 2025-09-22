package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class managerOrderManagementController {

    @FXML
    private VBox InstallmentPane;

    @FXML
    private Button NextMonthbtn;

    @FXML
    private Button NextYearbtn;

    @FXML
    private Button PreviousMonthbtn;

    @FXML
    private Button PreviousYearbth;

    @FXML
    private TextField SearchText;

    @FXML
    private Button Searchbtn;

    @FXML
    private Label cancelOrderMessage;

    @FXML
    private Label cancelOrderlbl;

    @FXML
    private Label confirmOrderMessage;

    @FXML
    private Label confirmOrderlbl;

    @FXML
    private Label customerNamelabel;

    @FXML
    private TableColumn<?, ?> installmentCol;

    @FXML
    private Label monthlbl;

    @FXML
    private TableColumn<?, ?> orderDateCol;

    @FXML
    private TableColumn<?, ?> orderIdCol;

    @FXML
    private VBox orderItemsContainer;

    @FXML
    private PieChart orderPieChart;

    @FXML
    private TableColumn<?, ?> orderStatusCol;

    @FXML
    private TableView<?> orderTable;

    @FXML
    private LineChart<?, ?> orderTrendChart;

    @FXML
    private Label paidAmountlbl;

    @FXML
    private Label pendingOrderMessage;

    @FXML
    private Label pendingOrderlbl;

    @FXML
    private Label remainAmountlbl;

    @FXML
    private Label staffNamelbl;

    @FXML
    private Label totalPriceLabel12;

    @FXML
    private Label totalPricelbl;

    @FXML
    private Label yearlbl;

    @FXML
    void ClickPrevMonth(ActionEvent event) {

    }

    @FXML
    void clickNextMonth(ActionEvent event) {

    }

    @FXML
    void clickNextYear(ActionEvent event) {

    }

    @FXML
    void clickPrevYear(ActionEvent event) {

    }

}
