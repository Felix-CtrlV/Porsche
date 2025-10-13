package Controllers;

import Database.DatabaseConnectionManager;
import Model.orderView;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.util.Duration;
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
    private TableColumn<orderView, String> orders_col;
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
    @FXML
    private VBox detailPane;
    @FXML
    private VBox detailContent;

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
        orders_col.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCarName()));
        customer_col.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCustomername()));
        date_col.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDate().toString()));
        status_col.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        total_col.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTotal()));

        // Setup table placeholder
        setupTablePlaceholder();

        // Setup month/year controls
        setupMonthYear();
        updateMonthBox();
        updateDateControls();

        // Setup search
        setupSearch();

        // Setup table row click listener
        setupTableRowClickListener();

        // Load initial data
        loadOrdersByMonthYear(currentMonth, currentYear);
    }

    private void setupTableRowClickListener() {
        table.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                orderView selectedOrder = table.getSelectionModel().getSelectedItem();
                if (selectedOrder != null) {
                    showOrderDetails(selectedOrder);
                }
            }
        });
    }

    private void setupTablePlaceholder() {
        Label placeholder = new Label("Loading orders...");
        placeholder.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
        table.setPlaceholder(placeholder);
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
        // Show loading state
        Platform.runLater(() -> {
            Label loadingLabel = new Label("Loading orders...");
            loadingLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
            table.setPlaceholder(loadingLabel);
        });

        Task<ObservableList<orderView>> task = new Task<>() {
            @Override
            protected ObservableList<orderView> call() throws Exception {
                ObservableList<orderView> orders = FXCollections.observableArrayList();
                
                try (Connection con = DatabaseConnectionManager.getInstance().getConnection()) {
                    LocalDate startDate = LocalDate.of(year, month, 1);
                    LocalDate endDate = startDate.plusMonths(1);

                    String query = "SELECT o.order_id, c.customer_name, o.order_date, o.order_status, " +
                            "SUM(d.total_price) AS total_price, " +
                            "GROUP_CONCAT(DISTINCT cm.model_name SEPARATOR ', ') AS car_names, " +
                            "GROUP_CONCAT(DISTINCT p.part_name SEPARATOR ', ') AS part_names " +
                            "FROM orders o " +
                            "JOIN customer_info c ON o.customer_id = c.customer_id " +
                            "JOIN order_details d ON o.order_id = d.order_id " +
                            "LEFT JOIN cars ON d.car_id = cars.car_id " +
                            "LEFT JOIN car_models cm ON cars.model_id = cm.model_id " +
                            "LEFT JOIN car_parts p ON d.part_id = p.part_id " +
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
                                String carNames = rs.getString("car_names");
                                String partNames = rs.getString("part_names");
                                
                                // Build display string based on what's in the order
                                StringBuilder orderItems = new StringBuilder();
                                if (carNames != null && !carNames.isEmpty()) {
                                    orderItems.append(carNames);
                                }
                                if (partNames != null && !partNames.isEmpty()) {
                                    if (orderItems.length() > 0) {
                                        orderItems.append(", ");
                                    }
                                    orderItems.append("Parts: ").append(partNames);
                                }
                                if (orderItems.length() == 0) {
                                    orderItems.append("N/A");
                                }
                                
                                orderView order = new orderView(
                                        rs.getInt("order_id"),
                                        rs.getString("customer_name"),
                                        orderItems.toString(),
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
            
            // Update placeholder based on data
            if (allOrders.isEmpty()) {
                Label noDataLabel = new Label("No orders found for " + 
                    Month.of(currentMonth).getDisplayName(TextStyle.FULL, Locale.ENGLISH) + 
                    " " + currentYear);
                noDataLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
                table.setPlaceholder(noDataLabel);
            }
        });

        task.setOnFailed(e -> {
            logger.error("Failed to load orders", task.getException());
            Platform.runLater(() -> {
                allOrders.clear();
                table.setItems(allOrders);
                updateStats();
                
                // Show error message
                Label errorLabel = new Label("Failed to load orders. Please try again.");
                errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ef4444;");
                table.setPlaceholder(errorLabel);
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

    @FXML
    void closeDetailPane(ActionEvent event) {
        hideDetailPane();
    }

    private void showOrderDetails(orderView order) {
        // Fetch detailed order information
        Task<Map<String, Object>> task = new Task<>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                Map<String, Object> details = new HashMap<>();
                List<Map<String, String>> cars = new ArrayList<>();
                List<Map<String, String>> parts = new ArrayList<>();
                
                try (Connection con = DatabaseConnectionManager.getInstance().getConnection()) {
                    // Get car details
                    String carQuery = "SELECT cm.model_name, d.qty, d.total_price " +
                            "FROM order_details d " +
                            "JOIN cars c ON d.car_id = c.car_id " +
                            "JOIN car_models cm ON c.model_id = cm.model_id " +
                            "WHERE d.order_id = ? AND d.car_id IS NOT NULL";
                    
                    try (PreparedStatement ps = con.prepareStatement(carQuery)) {
                        ps.setInt(1, order.getOrderId());
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                Map<String, String> car = new HashMap<>();
                                car.put("name", rs.getString("model_name"));
                                car.put("quantity", String.valueOf(rs.getInt("qty")));
                                car.put("price", String.format("$%.2f", rs.getDouble("total_price")));
                                cars.add(car);
                            }
                        }
                    }
                    
                    // Get part details
                    String partQuery = "SELECT p.part_name, d.qty, d.total_price " +
                            "FROM order_details d " +
                            "JOIN car_parts p ON d.part_id = p.part_id " +
                            "WHERE d.order_id = ? AND d.part_id IS NOT NULL";
                    
                    try (PreparedStatement ps = con.prepareStatement(partQuery)) {
                        ps.setInt(1, order.getOrderId());
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                Map<String, String> part = new HashMap<>();
                                part.put("name", rs.getString("part_name"));
                                part.put("quantity", String.valueOf(rs.getInt("qty")));
                                part.put("price", String.format("$%.2f", rs.getDouble("total_price")));
                                parts.add(part);
                            }
                        }
                    }
                }
                
                details.put("cars", cars);
                details.put("parts", parts);
                return details;
            }
        };
        
        task.setOnSucceeded(e -> {
            Map<String, Object> details = task.getValue();
            List<Map<String, String>> cars = (List<Map<String, String>>) details.get("cars");
            List<Map<String, String>> parts = (List<Map<String, String>>) details.get("parts");
            
            displayOrderDetails(cars, parts);
        });
        
        task.setOnFailed(e -> {
            logger.error("Failed to load order details", task.getException());
        });
        
        new Thread(task).start();
    }

    private void displayOrderDetails(List<Map<String, String>> cars, List<Map<String, String>> parts) {
        detailContent.getChildren().clear();
        
        // Add cars section if there are cars
        if (!cars.isEmpty()) {
            Label carsHeader = new Label("Cars");
            carsHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #1e293b;");
            detailContent.getChildren().add(carsHeader);
            
            for (Map<String, String> car : cars) {
                HBox itemRow = createItemRow(car.get("name"), car.get("quantity"), car.get("price"));
                detailContent.getChildren().add(itemRow);
            }
        }
        
        // Add separator if both cars and parts exist
        if (!cars.isEmpty() && !parts.isEmpty()) {
            Line separator = new Line();
            separator.setStartX(0);
            separator.setEndX(400);
            separator.setStyle("-fx-stroke: #e2e8f0; -fx-stroke-width: 1;");
            VBox.setMargin(separator, new Insets(10, 0, 10, 0));
            detailContent.getChildren().add(separator);
        }
        
        // Add parts section if there are parts
        if (!parts.isEmpty()) {
            Label partsHeader = new Label("Parts");
            partsHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #1e293b;");
            detailContent.getChildren().add(partsHeader);
            
            for (Map<String, String> part : parts) {
                HBox itemRow = createItemRow(part.get("name"), part.get("quantity"), part.get("price"));
                detailContent.getChildren().add(itemRow);
            }
        }
        
        // Show empty message if no items
        if (cars.isEmpty() && parts.isEmpty()) {
            Label emptyLabel = new Label("No items found in this order");
            emptyLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13;");
            detailContent.getChildren().add(emptyLabel);
        }
        
        // Animate the detail pane
        showDetailPane();
    }

    private HBox createItemRow(String name, String quantity, String price) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 6; -fx-padding: 10;");
        
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #334155;");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        
        Label qtyLabel = new Label("Qty: " + quantity);
        qtyLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #64748b;");
        qtyLabel.setMinWidth(60);
        
        Label priceLabel = new Label(price);
        priceLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #059669;");
        priceLabel.setMinWidth(80);
        priceLabel.setAlignment(Pos.CENTER_RIGHT);
        
        row.getChildren().addAll(nameLabel, qtyLabel, priceLabel);
        return row;
    }

    private void showDetailPane() {
        detailPane.setVisible(true);
        detailPane.setManaged(true);
        
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(detailPane.prefHeightProperty(), 0),
                new KeyValue(detailPane.opacityProperty(), 0)
            ),
            new KeyFrame(Duration.millis(300),
                new KeyValue(detailPane.prefHeightProperty(), 300),
                new KeyValue(detailPane.opacityProperty(), 1)
            )
        );
        timeline.play();
    }

    private void hideDetailPane() {
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(detailPane.prefHeightProperty(), detailPane.getHeight()),
                new KeyValue(detailPane.opacityProperty(), 1)
            ),
            new KeyFrame(Duration.millis(300),
                new KeyValue(detailPane.prefHeightProperty(), 0),
                new KeyValue(detailPane.opacityProperty(), 0)
            )
        );
        timeline.setOnFinished(e -> {
            detailPane.setVisible(false);
            detailPane.setManaged(false);
        });
        timeline.play();
    }
}
