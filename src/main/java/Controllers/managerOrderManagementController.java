package Controllers;

import java.sql.Date;

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
    private TableColumn<managerOrderView, String> customerNameCol;

    @FXML
    private Label customerNamelabel;

    @FXML
    private TableColumn<managerOrderView, String> installmentNameCol;

    @FXML
    private TableColumn<managerOrderView, Double> installmentPriceCol;

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
    private TableColumn<managerOrderView, Date> orderDateCol;

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
    private TableColumn<managerOrderView, Double> priceCol;

    @FXML
    private TableColumn<managerOrderView, String> qtyCol;

    @FXML
    private Label remainAmountlbl;

    @FXML
    private BarChart<?, ?> revenueChart;

    @FXML
    private TableColumn<managerOrderView, String> staffNameCol;

    @FXML
    private Label staffNamelabel;

    @FXML
    private TableColumn<managerOrderView, String> statusCol;

    @FXML
    private Label totalPriceLabel;

    @FXML
    private Button weeklyRevenue;

    @FXML
    private ChoiceBox<Integer> yearBox;

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

    void searchTextAction(ActionEvent event) {

    }
    @FXML
    public void initialize(){
        orderDateCol.setCellValueFactory(new PropertyValueFactory<>("order_date"));
        customerNameCol.setCellValueFactory(new PropertyValueFactory<>("cus_name"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("total_amount"));
        staffNameCol.setCellValueFactory(new PropertyValueFactory<>("staff_name"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("is_installmenat"));
        
        // Installment table columns
        installmentNameCol.setCellValueFactory(new PropertyValueFactory<>("carsandparts_name"));
        installmentPriceCol.setCellValueFactory(new PropertyValueFactory<>("carsandparts_perprice"));
        installmentQtyCol.setCellValueFactory(new PropertyValueFactory<>("carsandparts_qty"));
    }

}
