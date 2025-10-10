package Controllers;

import Model.managerOrderView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.shape.Circle;

public class managerOrderManagementController {

    @FXML
    private Button PreviousMonthbtn;

    @FXML
    private TextField SearchText;

    @FXML
    private Button Searchbtn;

    @FXML
    private Label confirmPriceRateLabel;

    @FXML
    private Label confrimPriceLabel;

    @FXML
    private Label confrimQty;

    @FXML
    private Circle confrimQtyCircle;

    @FXML
    private TableColumn<?, ?> customerNameCol;

    @FXML
    private Label customerNamelabel;

    @FXML
    private TableColumn<managerOrderView, ?> installmentNameCol;

    @FXML
    private TableColumn<managerOrderView, ?> installmentPriceCol;

    @FXML
    private TableColumn<managerOrderView, Integer> installmentQtyCol;

    @FXML
    private TableView<managerOrderView> installmentTable;

    @FXML
    private ChoiceBox<String> monthBox;

    @FXML
    private Button monthlyRevenue;

    @FXML
    private Button nextMonthbtn;

    @FXML
    private Button nextYearbtn;

    @FXML
    private TableColumn<?, ?> orderDateCol;

    @FXML
    private TableView<managerOrderView> orderTable;

    @FXML
    private Label paidAmountlbl;

    @FXML
    private Label pendingPriceLabel;

    @FXML
    private Label pendingPriceRateLabel;

    @FXML
    private Label pendingQty;

    @FXML
    private Circle pendingQtyCircle;

    @FXML
    private Button previousYearbth;

    @FXML
    private TableColumn<?, ?> priceCol;

    @FXML
    private TableColumn<?, ?> qtyCol;

    @FXML
    private Label remainAmountlbl;

    @FXML
    private BarChart<?, ?> revenueChart;

    @FXML
    private TableColumn<?, ?> staffNameCol;

    @FXML
    private Label staffNamelabel;

    @FXML
    private TableColumn<?, ?> statusCol;

    @FXML
    private Label totalPriceLabel;

    @FXML
    private Button weeklyRevenue;

    @FXML
    private ChoiceBox<?> yearBox;

    @FXML
    void clickMonthlyRevenue(ActionEvent event) {

    }

    @FXML
    void clickNextMonthbtn(ActionEvent event) {

    }

    @FXML
    void clickNextYearbn(ActionEvent event) {

    }

    @FXML
    void clickPreviousMonthbtn(ActionEvent event) {

    }

    @FXML
    void clickPreviousYearbtn(ActionEvent event) {

    }

    @FXML
    void clickWeeklyRevenue(ActionEvent event) {

    }

    @FXML
    void searchTextAction(ActionEvent event) {

    }
    @FXML
    public void initialize(){
//        orderDateCol.setCellValueFactory(new PropertyValueFactory<>());
    }

}
