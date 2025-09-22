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
    private Label Monthslabel;

    @FXML
    private Button NextMonthbtn;

    @FXML
    private Button NextYearbtn;

    @FXML
    private Button PrevMonthbtn;

    @FXML
    private Button PrevYearbtn;

    @FXML
    private Label Yearslabel;

    @FXML
    private Button btnclick;

    @FXML
    private Label cancelOrderMessage;

    @FXML
    private Label cancelOrderlbl;

    @FXML
    private Label confirmOrderMessage;

    @FXML
    private Label confirmOrderlbl;

    @FXML
    private TextField lblsearch;

    @FXML
    private TableColumn<?, ?> orderCustomerCol;

    @FXML
    private TableColumn<?, ?> orderDateCol;

    @FXML
    private TableColumn<?, ?> orderIdCol;

    @FXML
    private TableColumn<?, ?> orderInstallmentCol;

    @FXML
    private VBox orderItemsContainer;

    @FXML
    private VBox orderItemsContainer1;

    @FXML
    private PieChart orderPieChart;

    @FXML
    private TableColumn<?, ?> orderStaffNameCol;

    @FXML
    private TableColumn<?, ?> orderStatusCol;

    @FXML
    private LineChart<?, ?> orderTrendChart;

    @FXML
    private Label orderTrendYear;

    @FXML
    private TableView<?> orderViewTabel;

    @FXML
    private Label paidAmountLabel;

    @FXML
    private Label pendingOrderMessage;

    @FXML
    private Label pendingOrderlbl;

    @FXML
    private Label remainAmountLabel;

    @FXML
    private Label totalPriceLabel;

    @FXML
    private Label totalPriceLabel1;

    @FXML
    private Label totalPriceLabel11;

    @FXML
    private Label totalPriceLabel12;

    @FXML
    void clickClick(ActionEvent event) {

    }

    @FXML
    void clickNextMonth(ActionEvent event) {

    }

    @FXML
    void clickNextYear(ActionEvent event) {

    }

    @FXML
    void clickPrevMonth(ActionEvent event) {

    }

    @FXML
    void clickPrevYear(ActionEvent event) {

    }

}
