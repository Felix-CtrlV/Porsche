package Controllers;

import Database.Porsche_DB;
import Model.orderView;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class adminOrderController {

    @FXML
    private TableView<orderView> table;
    @FXML
    private TableColumn<orderView, Integer> orders_col;
    @FXML
    private TableColumn<orderView, String> customer_col;
    @FXML
    private TableColumn<orderView, String> date_col;
    @FXML
    private TableColumn<orderView, String> status_col;
    @FXML
    private TableColumn<orderView, String> total_col;
    @FXML
    private TextField searchField;
    @FXML
    private Button searchBtn;
    @FXML
    private ChoiceBox<String> monthBox;
    @FXML
    private ChoiceBox<Integer> yearBox;
    @FXML
    private Button monthPrev, monthNext, yearPrev, yearNext;
    @FXML
    private Text totalOrdersText, searchedOrdersText;

    private int currentMonth = LocalDate.now().getMonthValue();
    private int currentYear = LocalDate.now().getYear();
    private ObservableList<orderView> allOrders = FXCollections.observableArrayList();
    private ObservableList<orderView> filteredOrders = FXCollections.observableArrayList();
    private String currentSearchText = "";
    private boolean updatingDateBox = false;


    @FXML
    void clickMonthNext(ActionEvent event) {
        if (currentMonth == 12) {
            currentMonth = 1;
            currentYear++;
            yearBox.setValue(currentYear);
        } else {
            currentMonth++;
        }
        updateMonthBox();
        updateDateControls();
        loadOrdersByMonthYear(currentMonth, currentYear);
    }

    @FXML
    void clickMonthPrev(ActionEvent event) {
        if (currentMonth == 1) {
            currentMonth = 12;
            currentYear--;
            yearBox.setValue(currentYear);
        } else {
            currentMonth--;
        }
        updateMonthBox();
        updateDateControls();
        loadOrdersByMonthYear(currentMonth, currentYear);
    }

    @FXML
    void clickYearNext(ActionEvent event) {
        currentYear++;
        yearBox.setValue(currentYear);
        updateMonthBox();
        updateDateControls();
        loadOrdersByMonthYear(currentMonth, currentYear);
    }

    @FXML
    void clickYearPrev(ActionEvent event) {
        currentYear--;
        yearBox.setValue(currentYear);
        updateMonthBox();
        updateDateControls();
        loadOrdersByMonthYear(currentMonth, currentYear);
    }

    private void updateMonthBox() {
        updatingDateBox = true;
        List<String> months = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            months.add(java.time.Month.of(i).getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
        }
        monthBox.setItems(FXCollections.observableArrayList(months));
        if (currentMonth < 1 || currentMonth > 12) {
            currentMonth = 1;
        }
        monthBox.setValue(java.time.Month.of(currentMonth).getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
        updatingDateBox = false;
    }

    private void updateDateControls() {
        LocalDate now = LocalDate.now();
        boolean isCurrentYear = currentYear == now.getYear();
        boolean isCurrentMonth = isCurrentYear && currentMonth == now.getMonthValue();
        
        monthNext.setDisable(isCurrentMonth);
        yearNext.setDisable(isCurrentYear);
        monthNext.setVisible(!isCurrentMonth);
        yearNext.setVisible(!isCurrentYear);
    }

    private void loadOrdersByMonthYear(int month, int year) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1);
        
        table.setPlaceholder(new Label("Loading..."));

        Task<ObservableList<orderView>> task = new Task<>() {
            @Override
            protected ObservableList<orderView> call() throws Exception {
                ObservableList<orderView> tempList = FXCollections.observableArrayList();
                Porsche_DB connect = new Porsche_DB();
                Connection con = connect.connect();

                String query = "SELECT o.order_id, c.customer_name, o.order_date, o.order_status, SUM(d.total_price) AS total_price " +
                        "FROM orders o JOIN customer_info c ON o.customer_id = c.customer_id " +
                        "JOIN order_details d ON o.order_id = d.order_id " +
                        "WHERE o.order_date >= ? AND o.order_date < ? " +
                        "GROUP BY o.order_id, c.customer_name, o.order_date, o.order_status";
                
                PreparedStatement ps = con.prepareStatement(query);
                ps.setString(1, start.toString());
                ps.setString(2, end.toString());

                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    int orderid = rs.getInt(1);
                    String customername = rs.getString(2);
                    String orderdate = rs.getString(3);
                    String status = rs.getString(4);
                    String total = "$" + rs.getString(5);

                    tempList.add(new orderView(orderid, customername, parseOrderDate(orderdate), status, total));
                }

                connect.disconnect();
                return tempList;
            }
        };

        task.setOnSucceeded(e -> {
            allOrders = task.getValue();
            applySearchFilter();
            updateOrderCounts();
            table.setPlaceholder(allOrders.isEmpty() ? new Label("No orders found for this selection") : new Label(""));
        });

        task.setOnFailed(e -> task.getException().printStackTrace());

        new Thread(task).start();
    }

    private void applySearchFilter() {
        if (currentSearchText == null || currentSearchText.trim().isEmpty()) {
            filteredOrders = FXCollections.observableArrayList(allOrders);
            table.setItems(filteredOrders);
            updateOrderCounts();
        } else {
            // Run search in background to avoid UI freezing
            Task<ObservableList<orderView>> searchTask = new Task<>() {
                @Override
                protected ObservableList<orderView> call() throws Exception {
                    ObservableList<orderView> results = FXCollections.observableArrayList();
                    String searchLower = currentSearchText.toLowerCase().trim();
                    
                    for (orderView order : allOrders) {
                        if (containsCarModel(order.getOrderId(), searchLower)) {
                            results.add(order);
                        }
                    }
                    return results;
                }
            };
            
            searchTask.setOnSucceeded(e -> {
                filteredOrders = searchTask.getValue();
                table.setItems(filteredOrders);
                updateOrderCounts();
            });
            
            new Thread(searchTask).start();
        }
    }

    private boolean containsCarModel(int orderId, String searchText) {
        try {
            Porsche_DB connect = new Porsche_DB();
            Connection con = connect.connect();
            
            String query = "SELECT p.product_name FROM order_details od " +
                    "JOIN products p ON od.product_id = p.product_id " +
                    "WHERE od.order_id = ?";
            
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                String productName = rs.getString(1);
                if (productName != null && productName.toLowerCase().contains(searchText)) {
                    connect.disconnect();
                    return true;
                }
            }
            
            connect.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void updateOrderCounts() {
        totalOrdersText.setText(String.valueOf(allOrders.size()));
        searchedOrdersText.setText(String.valueOf(filteredOrders.size()));
    }

    private LocalDateTime parseOrderDate(String orderDateStr) {
        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-M-d HH:mm:ss");
            return LocalDateTime.parse(orderDateStr, dtf);
        } catch (
                Exception e) {
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-M-d");
            return LocalDate.parse(orderDateStr, df).atStartOfDay();
        }
    }

    public void initialize() {
        // Setup table columns
        orders_col.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getOrderId()));
        customer_col.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCustomername()));
        date_col.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDate().toString()));
        status_col.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        total_col.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTotal()));

        // Setup month/year controls
        setupMonthYear();
        updateMonthBox();
        updateDateControls();

        // Setup search
        setupSearch();

        // Load initial data
        loadOrdersByMonthYear(currentMonth, currentYear);
    }

    private void setupMonthYear() {
        // Populate year box
        yearBox.getItems().clear();
        int currentYearValue = LocalDate.now().getYear();
        for (int y = 2020; y <= currentYearValue; y++) {
            yearBox.getItems().add(y);
        }
        yearBox.setValue(currentYear);

        // Month box listener
        monthBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (updatingDateBox || newVal == null) return;
            int selectedMonth = java.time.Month.valueOf(newVal.toUpperCase(Locale.ENGLISH)).getValue();
            if (selectedMonth != currentMonth) {
                currentMonth = selectedMonth;
                updateDateControls();
                loadOrdersByMonthYear(currentMonth, currentYear);
            }
        });

        // Year box listener
        yearBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (updatingDateBox || newVal == null) return;
            if (newVal != currentYear) {
                currentYear = newVal;
                updateMonthBox();
                updateDateControls();
                loadOrdersByMonthYear(currentMonth, currentYear);
            }
        });
    }

    private void setupSearch() {
        // Search on Enter key
        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                performSearch();
            }
        });

        // Search on button click
        searchBtn.setOnMouseClicked(e -> performSearch());

        // Real-time search as user types
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            currentSearchText = newVal;
            applySearchFilter();
        });
    }

    private void performSearch() {
        currentSearchText = searchField.getText();
        applySearchFilter();
    }
}
