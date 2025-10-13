package Controllers;

import Database.DatabaseConnectionManager;
import Model.orderView;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;

public class adminOrderController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(adminOrderController.class);

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
    private Button PreviousMonthbtn, NextMonthbtn, PreviousYearbtn, NextYearbtn;
    @FXML
    private Label totalOrdersLbl, searchedOrdersLbl, totalRevenueLbl, totalOrdersMonthLbl;

    private LocalDate today = LocalDate.now();
    private int currentMonth = today.getMonthValue();
    private int currentYear = today.getYear();
    private boolean updatingDateBox = false;
    
    private ObservableList<orderView> allOrders = FXCollections.observableArrayList();
    private ObservableList<orderView> filteredOrders = FXCollections.observableArrayList();
    private String currentSearchText = "";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
        for (int y = 2020; y <= today.getYear(); y++) {
            yearBox.getItems().add(y);
        }
        yearBox.setValue(currentYear);

        // Month box listener
        monthBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (updatingDateBox || newVal == null) return;
            // Parse short month name to month number
            int selectedMonth = -1;
            for (int i = 1; i <= 12; i++) {
                if (Month.of(i).getDisplayName(TextStyle.SHORT, Locale.ENGLISH).equals(newVal)) {
                    selectedMonth = i;
                    break;
                }
            }
            if (selectedMonth != -1 && selectedMonth != currentMonth) {
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

    private void updateMonthBox() {
        updatingDateBox = true;
        List<String> months = new ArrayList<>();
        
        // Limit months to current month if viewing current year
        int maxMonth = (currentYear == today.getYear()) ? today.getMonthValue() : 12;
        
        for (int i = 1; i <= maxMonth; i++) {
            months.add(Month.of(i).getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
        }
        monthBox.setItems(FXCollections.observableArrayList(months));
        
        // If current month exceeds max available, reset to max
        if (currentMonth > maxMonth) {
            currentMonth = maxMonth;
        }
        if (currentMonth < 1) {
            currentMonth = 1;
        }
        monthBox.setValue(Month.of(currentMonth).getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
        
        // Update month label
        if (totalOrdersMonthLbl != null) {
            totalOrdersMonthLbl.setText("in " + Month.of(currentMonth).getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + currentYear);
        }
        
        updatingDateBox = false;
    }

    private void updateDateControls() {
        boolean isCurrentYear = currentYear == today.getYear();
        boolean isCurrentMonth = isCurrentYear && currentMonth == today.getMonthValue();
        
        if (NextMonthbtn != null) {
            NextMonthbtn.setDisable(isCurrentMonth);
            NextMonthbtn.setVisible(!isCurrentMonth);
        }
        if (NextYearbtn != null) {
            NextYearbtn.setDisable(isCurrentYear);
            NextYearbtn.setVisible(!isCurrentYear);
        }
    }

    private void setupSearch() {
        if (searchField != null) {
            searchField.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ENTER) {
                    performSearch();
                }
            });
        }
    }

    private void loadOrdersByMonthYear(int month, int year) {
        Task<ObservableList<orderView>> task = new Task<>() {
            @Override
            protected ObservableList<orderView> call() throws Exception {
                ObservableList<orderView> orders = FXCollections.observableArrayList();
                
                try (Connection con = DatabaseConnectionManager.getInstance().getConnection()) {
                    LocalDate startDate = LocalDate.of(year, month, 1);
                    LocalDate endDate = startDate.plusMonths(1);

                    String query = "SELECT o.order_id, c.customer_name, o.order_date, o.order_status, SUM(d.total_price) AS total_price " +
                            "FROM orders o JOIN customer_info c ON o.customer_id = c.customer_id " +
                            "JOIN order_details d ON o.order_id = d.order_id " +
                            "WHERE o.order_date >= ? AND o.order_date < ? " +
                            "GROUP BY o.order_id, c.customer_name, o.order_date, o.order_status " +
                            "ORDER BY o.order_date DESC";

                    try (PreparedStatement ps = con.prepareStatement(query)) {
                        ps.setString(1, startDate.toString());
                        ps.setString(2, endDate.toString());

                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                // Convert Date to LocalDateTime (at start of day)
                                LocalDateTime orderDateTime = rs.getDate("order_date").toLocalDate().atStartOfDay();
                                
                                orderView order = new orderView(
                                        rs.getInt("order_id"),
                                        rs.getString("customer_name"),
                                        orderDateTime,
                                        rs.getString("order_status"),
                                        String.format("$%.2f", rs.getDouble("total_price"))
                                );
                                orders.add(order);
                            }
                        }
                    }
                }
                
                return orders;
            }
        };

        task.setOnSucceeded(e -> {
            allOrders = task.getValue();
            applySearchFilter();
            updateStats();
        });

        task.setOnFailed(e -> {
            logger.error("Failed to load orders", task.getException());
            Platform.runLater(() -> {
                allOrders.clear();
                table.setItems(allOrders);
                updateStats();
            });
        });

        new Thread(task).start();
    }

    private void applySearchFilter() {
        if (currentSearchText == null || currentSearchText.trim().isEmpty()) {
            filteredOrders = FXCollections.observableArrayList(allOrders);
        } else {
            String searchLower = currentSearchText.toLowerCase().trim();
            filteredOrders = allOrders.filtered(order ->
                    order.getCustomername().toLowerCase().contains(searchLower) ||
                    String.valueOf(order.getOrderId()).contains(searchLower) ||
                    order.getStatus().toLowerCase().contains(searchLower) ||
                    order.getTotal().toLowerCase().contains(searchLower)
            );
        }
        table.setItems(filteredOrders);
    }

    private void updateStats() {
        // Total orders count
        if (totalOrdersLbl != null) {
            totalOrdersLbl.setText(String.valueOf(allOrders.size()));
        }
        
        // Searched/filtered orders count
        if (searchedOrdersLbl != null) {
            searchedOrdersLbl.setText(String.valueOf(filteredOrders.size()));
        }
        
        // Calculate total revenue from filtered orders
        double totalRevenue = 0.0;
        for (orderView order : filteredOrders) {
            try {
                // Remove $ and parse
                String totalStr = order.getTotal().replace("$", "").replace(",", "");
                totalRevenue += Double.parseDouble(totalStr);
            } catch (NumberFormatException e) {
                logger.warn("Failed to parse total: " + order.getTotal());
            }
        }
        
        if (totalRevenueLbl != null) {
            totalRevenueLbl.setText(String.format("$%.2f", totalRevenue));
        }
    }

    private void performSearch() {
        currentSearchText = searchField != null ? searchField.getText() : "";
        applySearchFilter();
        updateStats();
    }

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

    @FXML
    void clickSearch(ActionEvent event) {
        performSearch();
    }
}
