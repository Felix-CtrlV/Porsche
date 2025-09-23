package Controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class managerOrderManagementController {

    @FXML
    private TextField SearchText;

    @FXML
    private Button Searchbtn;

    @FXML
    private Label confirmOrderlbl;

    @FXML
    private Label confirmOrderMessage;

    @FXML
    private Label pendingOrderlbl;

    @FXML
    private Label pendingOrderMessage;

    @FXML
    private Label cancelOrderlbl;

    @FXML
    private Label cancelOrderMessage;

    @FXML
    private TableView<?> orderTable;

    @FXML
    private TableColumn<?, ?> noCol;

    @FXML
    private TableColumn<?, ?> orderDateCol;

    @FXML
    private TableColumn<?, ?> installmentCol;

    @FXML
    private TableColumn<?, ?> orderStatusCol;

    @FXML
    private Button PreviousMonthbtn;

    @FXML
    private ChoiceBox<?> monthBox;

    @FXML
    private Button NextMonthbtn;

    @FXML
    private Button PreviousYearbth;

    @FXML
    private ChoiceBox<?> yearBox;

    @FXML
    private Button NextYearbtn;

    @FXML
    private Label paidAmountlbl;

    @FXML
    private Label remainAmountlbl;

    @FXML
    private VBox orderItemsContainer;

    @FXML
    private Label totalPricelbl;

    @FXML
    private VBox InstallmentPane;

    @FXML
    private Label totalPriceLabel12;

    @FXML
    private Label staffNamelbl;

    @FXML
    private Label customerNamelabel;

    @FXML
    private PieChart orderPieChart;

    @FXML
    private LineChart<?, ?> orderTrendChart;

    @FXML
    void nextMonthClick(MouseEvent event) {

    }

    @FXML
    void nextYearClick(MouseEvent event) {

    }

    @FXML
    void prevMonthClick(MouseEvent event) {

    }

    @FXML
    void prevYearClick(MouseEvent event) {

    }

}
