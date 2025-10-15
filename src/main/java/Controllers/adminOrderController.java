package Controllers;

import Database.Porsche_DB;
import Model.orderView;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class adminOrderController {

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
    private ChoiceBox<String> monthBox;
    @FXML
    private ChoiceBox<Integer> yearBox;
    @FXML
    private Button PreviousMonthbtn, NextMonthbtn, PreviousYearbtn, NextYearbtn;
    @FXML
    private TextField searchField;
    @FXML
    private Label resultCountLbl;
    @FXML
    private Label totalAmountLbl;
    @FXML
    private Label totalOrdersLbl;
    @FXML
    private VBox detailPane;
    @FXML
    private VBox detailContent;

    private int currentMonth = LocalDate.now().getMonthValue();
    private int currentYear = LocalDate.now().getYear();
    private final String[] months = {
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    private ObservableList<orderView> allOrders = FXCollections.observableArrayList();
    private ObservableList<orderView> filteredOrders = FXCollections.observableArrayList();
    private int totalOrdersInMonth = 0;

    @FXML
    void clickNextMonth(ActionEvent event) {
        if (currentMonth == 12) {
            currentMonth = 1;
            currentYear++;
            yearBox.setValue(currentYear);
        } else {
            currentMonth++;
        }
        updateMonthBox();
        updateDateControls();
        loadOrders();
    }

    @FXML
    void clickPreviousMonth(ActionEvent event) {
        if (currentMonth == 1) {
            currentMonth = 12;
            currentYear--;
            yearBox.setValue(currentYear);
        } else {
            currentMonth--;
        }
        updateMonthBox();
        updateDateControls();
        loadOrders();
    }

    @FXML
    void clickNextYear(ActionEvent event) {
        currentYear++;
        yearBox.setValue(currentYear);
        updateMonthBox();
        updateDateControls();
        loadOrders();
    }

    @FXML
    void clickPreviousYear(ActionEvent event) {
        currentYear--;
        yearBox.setValue(currentYear);
        updateMonthBox();
        updateDateControls();
        loadOrders();
    }

    private void updateMonthBox() {
        monthBox.setValue(months[currentMonth - 1]);
    }

    private void updateDateControls() {
        LocalDate now = LocalDate.now();
        boolean isFuture = currentYear > now.getYear() || 
                          (currentYear == now.getYear() && currentMonth > now.getMonthValue());
        
        if (isFuture) {
            currentYear = now.getYear();
            currentMonth = now.getMonthValue();
            yearBox.setValue(currentYear);
            updateMonthBox();
        }
        
        // Disable next buttons if at current month/year
        boolean isCurrentYear = currentYear == now.getYear();
        boolean isCurrentMonth = isCurrentYear && currentMonth == now.getMonthValue();
        NextMonthbtn.setDisable(isCurrentMonth);
        NextYearbtn.setDisable(isCurrentYear);
        
        // Hide next buttons if at current month/year
        NextMonthbtn.setVisible(!isCurrentMonth);
        NextYearbtn.setVisible(!isCurrentYear);
        
        // Previous buttons are always enabled/visible (no start date constraint for orders)
        PreviousMonthbtn.setDisable(false);
        PreviousYearbtn.setDisable(false);
        PreviousMonthbtn.setVisible(true);
        PreviousYearbtn.setVisible(true);
    }

    private void loadOrders() {
        LocalDate start = LocalDate.of(currentYear, currentMonth, 1);
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
                              "GROUP BY o.order_id, c.customer_name, o.order_date, o.order_status " +
                              "ORDER BY o.order_date DESC";
                
                PreparedStatement ps = con.prepareStatement(query);
                ps.setString(1, start.toString());
                ps.setString(2, end.toString());

                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    int orderid = rs.getInt(1);
                    String customername = rs.getString(2);
                    String orderdate = rs.getString(3);
                    String status = rs.getString(4);
                    double totalPrice = rs.getDouble(5);
                    String total = String.format("$%,.2f", totalPrice);
                    
                    // Get order items (cars and parts)
                    String orderItems = getOrderItems(con, orderid);

                    orderView order = new orderView(orderid, customername, parseOrderDate(orderdate), status, total);
                    order.setOrderItems(orderItems);
                    tempList.add(order);
                }

                connect.disconnect();
                return tempList;
            }
        };

        task.setOnSucceeded(e -> {
            allOrders = task.getValue();
            totalOrdersInMonth = allOrders.size();
            totalOrdersLbl.setText(String.valueOf(totalOrdersInMonth));
            applySearchFilter();
        });

        task.setOnFailed(e -> {
            task.getException().printStackTrace();
            table.setPlaceholder(new Label("Error loading orders"));
        });

        new Thread(task).start();
    }
    
    private String getOrderItems(Connection con, int orderId) throws SQLException {
        StringBuilder items = new StringBuilder();
        
        // Get cars with model names
        String carQuery = "SELECT cm.model_name, cm.trim_name FROM order_details od " +
                         "JOIN cars c ON od.car_id = c.car_id " +
                         "JOIN car_models cm ON c.model_id = cm.model_id " +
                         "WHERE od.order_id = ? AND od.car_id IS NOT NULL";
        PreparedStatement carPs = con.prepareStatement(carQuery);
        carPs.setInt(1, orderId);
        ResultSet carRs = carPs.executeQuery();
        
        while (carRs.next()) {
            if (items.length() > 0) items.append(", ");
            String modelName = carRs.getString("model_name");
            String trimName = carRs.getString("trim_name");
            items.append(modelName);
            if (trimName != null && !trimName.isEmpty()) {
                items.append(" ").append(trimName);
            }
        }
        carRs.close();
        carPs.close();
        
        // Get parts
        String partQuery = "SELECT p.part_name FROM order_details od " +
                          "JOIN car_parts p ON od.part_id = p.part_id " +
                          "WHERE od.order_id = ? AND od.part_id IS NOT NULL";
        PreparedStatement partPs = con.prepareStatement(partQuery);
        partPs.setInt(1, orderId);
        ResultSet partRs = partPs.executeQuery();
        
        while (partRs.next()) {
            if (items.length() > 0) items.append(", ");
            items.append(partRs.getString("part_name"));
        }
        partRs.close();
        partPs.close();
        
        return items.length() > 0 ? items.toString() : "N/A";
    }

    private void applySearchFilter() {
        String searchText = searchField.getText().toLowerCase().trim();
        
        if (searchText.isEmpty()) {
            filteredOrders = FXCollections.observableArrayList(allOrders);
        } else {
            filteredOrders = allOrders.filtered(order -> 
                order.getCustomername().toLowerCase().contains(searchText) ||
                order.getStatus().toLowerCase().contains(searchText) ||
                (order.getOrderItems() != null && order.getOrderItems().toLowerCase().contains(searchText))
            );
        }
        
        table.setItems(filteredOrders);
        updateSummaryCards();
        
        if (filteredOrders.isEmpty()) {
            table.setPlaceholder(new Label(searchText.isEmpty() ? 
                "No orders found for this month" : 
                "No results found for '" + searchField.getText() + "'"));
        }
    }
    
    private void updateSummaryCards() {
        int count = filteredOrders.size();
        double totalAmount = 0.0;
        
        for (orderView order : filteredOrders) {
            String total = order.getTotal().replace("$", "").replace(",", "");
            try {
                totalAmount += Double.parseDouble(total);
            } catch (NumberFormatException e) {
                // Skip invalid amounts
            }
        }
        
        resultCountLbl.setText(String.valueOf(count));
        totalAmountLbl.setText(String.format("$%,.2f", totalAmount));
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
        orders_col.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getOrderItems() != null ? d.getValue().getOrderItems() : "N/A"));
        customer_col.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCustomername()));
        date_col.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))));
        status_col.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        total_col.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTotal()));

        // Setup month choice box
        monthBox.setItems(FXCollections.observableArrayList(months));
        monthBox.setValue(months[currentMonth - 1]);
        monthBox.setOnAction(e -> {
            String selected = monthBox.getValue();
            for (int i = 0; i < months.length; i++) {
                if (months[i].equals(selected)) {
                    currentMonth = i + 1;
                    updateDateControls();
                    loadOrders();
                    break;
                }
            }
        });

        // Setup year choice box
        ObservableList<Integer> years = FXCollections.observableArrayList();
        int currentYearNow = LocalDate.now().getYear();
        for (int i = currentYearNow; i >= currentYearNow - 10; i--) {
            years.add(i);
        }
        yearBox.setItems(years);
        yearBox.setValue(currentYear);
        yearBox.setOnAction(e -> {
            currentYear = yearBox.getValue();
            updateDateControls();
            loadOrders();
        });

        // Setup search field
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applySearchFilter();
        });
        
        // Setup table row click listener
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showOrderDetails(newSelection);
            }
        });

        // Initial load
        updateDateControls();
        loadOrders();
    }
    
    @FXML
    void closeDetailPane(ActionEvent event) {
        hideDetailPane();
    }
    
    private void hideDetailPane() {
        // Slide down and fade out
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(detailPane.translateYProperty(), 0),
                new KeyValue(detailPane.opacityProperty(), 1.0)
            ),
            new KeyFrame(Duration.millis(300),
                new KeyValue(detailPane.translateYProperty(), detailPane.getHeight()),
                new KeyValue(detailPane.opacityProperty(), 0.0)
            )
        );
        timeline.setOnFinished(e -> {
            detailPane.setVisible(false);
            detailPane.setManaged(false);
            detailPane.setTranslateY(0);
            // Clear table selection so the same row can be clicked again
            table.getSelectionModel().clearSelection();
        });
        timeline.play();
    }
    
    private void showOrderDetails(orderView order) {
        detailContent.getChildren().clear();
        
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Porsche_DB connect = new Porsche_DB();
                Connection con = connect.connect();
                
                // Get car details
                String carQuery = "SELECT cm.model_name, cm.trim_name, c.car_color, c.production_year, " +
                                 "od.qty, od.total_price " +
                                 "FROM order_details od " +
                                 "JOIN cars c ON od.car_id = c.car_id " +
                                 "JOIN car_models cm ON c.model_id = cm.model_id " +
                                 "WHERE od.order_id = ? AND od.car_id IS NOT NULL";
                PreparedStatement carPs = con.prepareStatement(carQuery);
                carPs.setInt(1, order.getOrderId());
                ResultSet carRs = carPs.executeQuery();
                
                boolean hasCars = false;
                VBox carSection = new VBox(8);
                carSection.setStyle("-fx-background-color: #f0f9ff; -fx-background-radius: 8; -fx-padding: 15; -fx-border-color: #bae6fd; -fx-border-radius: 8; -fx-border-width: 1;");
                
                Label carHeader = new Label("🚗 Cars");
                carHeader.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #0369a1;");
                carSection.getChildren().add(carHeader);
                
                while (carRs.next()) {
                    hasCars = true;
                    String modelName = carRs.getString("model_name");
                    String trimName = carRs.getString("trim_name");
                    String color = carRs.getString("car_color");
                    int year = carRs.getInt("production_year");
                    int qty = carRs.getInt("qty");
                    double price = carRs.getDouble("total_price");
                    
                    VBox itemBox = new VBox(5);
                    itemBox.setStyle("-fx-background-color: white; -fx-background-radius: 6; -fx-padding: 10;");
                    
                    Label nameLabel = new Label(modelName + (trimName != null && !trimName.isEmpty() ? " " + trimName : ""));
                    nameLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
                    
                    Label detailsLabel = new Label(String.format("%d %s • Qty: %d", year, color, qty));
                    detailsLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #64748b;");
                    
                    Label priceLabel = new Label(String.format("$%,.2f", price));
                    priceLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #0369a1;");
                    
                    itemBox.getChildren().addAll(nameLabel, detailsLabel, priceLabel);
                    carSection.getChildren().add(itemBox);
                }
                carRs.close();
                carPs.close();
                
                // Get part details
                String partQuery = "SELECT p.part_name, p.for_car, od.qty, od.total_price " +
                                  "FROM order_details od " +
                                  "JOIN car_parts p ON od.part_id = p.part_id " +
                                  "WHERE od.order_id = ? AND od.part_id IS NOT NULL";
                PreparedStatement partPs = con.prepareStatement(partQuery);
                partPs.setInt(1, order.getOrderId());
                ResultSet partRs = partPs.executeQuery();
                
                boolean hasParts = false;
                VBox partSection = new VBox(8);
                partSection.setStyle("-fx-background-color: #fef3c7; -fx-background-radius: 8; -fx-padding: 15; -fx-border-color: #fde68a; -fx-border-radius: 8; -fx-border-width: 1;");
                
                Label partHeader = new Label("🔧 Parts");
                partHeader.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #92400e;");
                partSection.getChildren().add(partHeader);
                
                while (partRs.next()) {
                    hasParts = true;
                    String partName = partRs.getString("part_name");
                    String forCar = partRs.getString("for_car");
                    int qty = partRs.getInt("qty");
                    double price = partRs.getDouble("total_price");
                    
                    VBox itemBox = new VBox(5);
                    itemBox.setStyle("-fx-background-color: white; -fx-background-radius: 6; -fx-padding: 10;");
                    
                    Label nameLabel = new Label(partName);
                    nameLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
                    
                    Label detailsLabel = new Label(String.format("%s • Qty: %d", forCar != null ? "For " + forCar : "Universal", qty));
                    detailsLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #64748b;");
                    
                    Label priceLabel = new Label(String.format("$%,.2f", price));
                    priceLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #92400e;");
                    
                    itemBox.getChildren().addAll(nameLabel, detailsLabel, priceLabel);
                    partSection.getChildren().add(itemBox);
                }
                partRs.close();
                partPs.close();
                
                connect.disconnect();
                
                // Update UI on JavaFX thread
                boolean finalHasCars = hasCars;
                boolean finalHasParts = hasParts;
                javafx.application.Platform.runLater(() -> {
                    if (finalHasCars) {
                        detailContent.getChildren().add(carSection);
                    }
                    if (finalHasParts) {
                        detailContent.getChildren().add(partSection);
                    }
                    
                    // Show detail pane with smooth slide-up animation from bottom
                    detailPane.setVisible(true);
                    detailPane.setManaged(true);
                    detailPane.setOpacity(0);
                    
                    // Use fixed height for animation (280px as set in FXML)
                    double paneHeight = 280;
                    
                    // Start from below (translated down)
                    detailPane.setTranslateY(paneHeight);
                    
                    Timeline timeline = new Timeline(
                        new KeyFrame(Duration.ZERO,
                            new KeyValue(detailPane.translateYProperty(), paneHeight),
                            new KeyValue(detailPane.opacityProperty(), 0.0)
                        ),
                        new KeyFrame(Duration.millis(400),
                            new KeyValue(detailPane.translateYProperty(), 0),
                            new KeyValue(detailPane.opacityProperty(), 1.0)
                        )
                    );
                    timeline.play();
                });
                
                return null;
            }
        };
        
        task.setOnFailed(e -> {
            task.getException().printStackTrace();
        });
        
        new Thread(task).start();
    }
}
