package Controllers;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import Database.Porsche_DB;
import Model.managerOrderView;
import Utils.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.chart.BarChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.beans.property.SimpleStringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class managerOrderManagementController {
    
    private static final Logger logger = LoggerFactory.getLogger(managerOrderManagementController.class);
    private Connection con;
    private ObservableList<managerOrderView> allOrdersData = FXCollections.observableArrayList();
    private ObservableList<managerOrderView> searchResultData = FXCollections.observableArrayList();
    private int managerId;
    private ContextMenu searchSuggestions = new ContextMenu();
    
    private LocalDate today = LocalDate.now();
    private int currentMonth;
    private int currentYear;
    private boolean listenersInitialized = false;
    private boolean updatingMonthBox = false;

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
    private TableColumn<String, String> installmentNameCol;

    @FXML
    private TableColumn<String, String> installmentPriceCol;

    @FXML
    private TableColumn<String, String> installmentQtyCol;

    @FXML
    private TableView<String> installmentTable;

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
    private Label dueDateLabel;

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
        currentMonth++;
        if (currentMonth > 12) {
            currentMonth = 1;
            currentYear++;
        }
        updateYearMonthLabel();
        loadOrder(); // Reload with new month/year
    }

    @FXML
    void clickNextYearbtn(ActionEvent event) {
        currentYear++;
        if (today.getYear() == currentYear) {
            if (currentMonth > today.getMonthValue()) {
                currentMonth = today.getMonthValue();
            }
        }
        updateYearMonthLabel();
        loadOrder(); // Reload with new year
    }

    @FXML
    void clickPreviousMonthbtn(ActionEvent event) {
        currentMonth--;
        if (currentMonth < 1) {
            currentMonth = 12;
            currentYear--;
        }
        updateYearMonthLabel();
        loadOrder(); // Reload with new month/year
    }

    @FXML
    void clickPreviousYearbtn(ActionEvent event) {
        currentYear--;
        updateYearMonthLabel();
        loadOrder(); // Reload with new year
    }

    @FXML
    void clickWeeklyRevenue(ActionEvent event) {

    }
    
    @FXML
    void orderTableClick(MouseEvent event) throws IOException {
        managerOrderView selectedOrder = orderTable.getSelectionModel().getSelectedItem();
        
        if (selectedOrder == null) {
            clearOrderDetails();
            return;
        }
        orderDetails(selectedOrder);
    }

    @FXML
    void searchTextAction(ActionEvent event) {
        handleSearch();
    }

    @FXML
    public void initialize(){
        orderDateCol.setCellValueFactory(new PropertyValueFactory<>("order_date"));
        customerNameCol.setCellValueFactory(new PropertyValueFactory<>("cus_name"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("total_amount"));
        staffNameCol.setCellValueFactory(new PropertyValueFactory<>("staff_name"));
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("totalQty"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("is_installmenat"));
        
        // Set up installment table columns
        installmentNameCol.setCellValueFactory(cellData -> {
            String[] parts = cellData.getValue().split("\\|");
            return new SimpleStringProperty(parts.length > 0 ? parts[0] : "");
        });
        
        // Add text wrapping to name column
        installmentNameCol.setCellFactory(column -> {
            TableCell<String, String> cell = new TableCell<String, String>() {
                private final Text text = new Text();
                
                {
                    text.wrappingWidthProperty().bind(column.widthProperty().subtract(10));
                    setGraphic(text);
                }
                
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        text.setText(null);
                    } else {
                        text.setText(item);
                    }
                }
            };
            return cell;
        });
        
        installmentQtyCol.setCellValueFactory(cellData -> {
            String[] parts = cellData.getValue().split("\\|");
            return new SimpleStringProperty(parts.length > 1 ? parts[1] : "");
        });
        
        installmentPriceCol.setCellValueFactory(cellData -> {
            String[] parts = cellData.getValue().split("\\|");
            return new SimpleStringProperty(parts.length > 2 ? parts[2] : "");
        });
        
        // Connect search button to searchOrder method
        if (Searchbtn != null) {
            Searchbtn.setOnAction(event -> handleSearch());
        }
        
        // Setup search bar with auto-complete
        setupSearchBar();
        
        // Initialize current date first (sets month/year to today)
        currentDateSelect();
        
        // Initialize month and year choice boxes with today's date
        initializeMonthYearBoxes();
        
        // Load all orders initially (will be filtered by current month/year)
        loadOrder();
        
        // Set up listeners for month/year boxes (only once)
        if (!listenersInitialized) {
            yearBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    currentYear = newVal;
                    updateMonthBoxForYear(currentYear); // Update available months based on selected year
                    updateYearMonthLabel();
                    loadOrder(); // Reload with filter (also recalculates quantities)
                }
            });
            
            monthBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (!updatingMonthBox && newVal != null) {
                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);
                    Month parsedMonth = Month.from(fmt.parse(newVal));
                    currentMonth = parsedMonth.getValue();
                    updateYearMonthLabel();
                    loadOrder(); // Reload with filter (also recalculates quantities)
                }
            });
            
            listenersInitialized = true;
        }
    }

    public void loadOrder(){
        CallableStatement cs = null;
        ResultSet rs = null;
        
        try {
            // Establish database connection
            Porsche_DB db = new Porsche_DB();
            con = db.connect();
            
            List<managerOrderView> ordersList = new ArrayList<>();
            
            // Always call stored procedure to get ALL orders
            cs = con.prepareCall("CALL getAllOrders()");
            rs = cs.executeQuery();
            
            while (rs.next()) {
                Integer order_id = rs.getInt(1);
                Date order_date = rs.getDate(2);
                String cus_name = rs.getString(3);
                String staff_name = rs.getString(4);
                Integer totalQty = rs.getInt(5);
                double total_amount = rs.getDouble(6);
                boolean installment = rs.getBoolean(7);
                double payed_amount = rs.getDouble(8);
                double remain_amount = rs.getDouble(9);
                Date due_date = rs.getDate(10);
                String carsandparts_name = rs.getString(11);
                String carsandparts_qty = rs.getString(12);
                String carsandparts_price = rs.getString(13);
                
                String is_installment = installment ? "Yes" : "No";
                managerOrderView order = new managerOrderView(
                    order_id, order_date, cus_name, staff_name, 
                    totalQty, total_amount, is_installment, payed_amount, 
                    remain_amount, due_date, carsandparts_name, 
                    carsandparts_qty, carsandparts_price
                );
                
                ordersList.add(order);
            }
            
            // Store all orders in allOrdersData
            allOrdersData.clear();
            allOrdersData.addAll(ordersList);
            
            // Filter orders based on selected month/year
            List<managerOrderView> filteredOrders = new ArrayList<>();
            if (currentMonth > 0 && currentYear > 0) {
                // Filter by month and year
                for (managerOrderView order : allOrdersData) {
                    if (order.getOrder_date() != null) {
                        // Convert java.sql.Date to LocalDate
                        LocalDate orderDate = new java.sql.Date(order.getOrder_date().getTime()).toLocalDate();
                        if (orderDate.getMonthValue() == currentMonth && orderDate.getYear() == currentYear) {
                            filteredOrders.add(order);
                        }
                    }
                }
                orderTable.setItems(FXCollections.observableArrayList(filteredOrders));
                
                // Auto-select first row if data exists and display details
                if (!filteredOrders.isEmpty()) {
                    orderTable.getSelectionModel().selectFirst();
                    displaySelectedOrderDetails();
                } else {
                    orderTable.getSelectionModel().clearSelection();
                    clearOrderDetails();
                }
            } else {
                // Show all orders
                orderTable.setItems(allOrdersData);
                
                // Auto-select first row if data exists and display details
                if (!allOrdersData.isEmpty()) {
                    orderTable.getSelectionModel().selectFirst();
                    displaySelectedOrderDetails();
                } else {
                    orderTable.getSelectionModel().clearSelection();
                    clearOrderDetails();
                }
            }
            
            logger.info("Loaded {} orders successfully", ordersList.size());
            
        } catch (SQLException e) {
            logger.error("Error loading orders from database", e);
            // You might want to show an alert to the user here
        } finally {
            // Clean up resources
            try {
                if (rs != null) rs.close();
                if (cs != null) cs.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                logger.error("Error closing database resources", e);
            }
        }
        
        // Calculate quantities and prices after loading orders
        calculateSoldQuantities();
    }

    private void setupSearchBar() {
        // Set up key listener for Enter key
        SearchText.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleSearch();
            }
        });
        
        // Set up text change listener for suggestions
        SearchText.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.isEmpty()) {
                searchSuggestions.hide();
                return;
            }
            
            // Clear previous suggestions
            searchSuggestions.getItems().clear();
            
            // Find matching orders
            List<MenuItem> matches = new ArrayList<>();
            String searchText = newText.toLowerCase();
            
            for (managerOrderView order : allOrdersData) {
                String orderId = String.valueOf(order.getOrder_id());
                String cusName = order.getCus_name().toLowerCase();
                String staffName = order.getStaff_name().toLowerCase();
                String orderDate = order.getOrder_date() != null ? order.getOrder_date().toString() : "";
                
                // Get car/part names from the order
                String[] itemNames = order.getCarsandparts_name();
                StringBuilder allItemNames = new StringBuilder();
                if (itemNames != null) {
                    for (String name : itemNames) {
                        allItemNames.append(name.toLowerCase()).append(" ");
                    }
                }
                String itemNamesStr = allItemNames.toString();
                
                // Check if search text matches any field
                if (orderId.contains(searchText) 
                    || cusName.contains(searchText) 
                    || staffName.contains(searchText)
                    || orderDate.contains(searchText)
                    || itemNamesStr.contains(searchText)) {
                    
                    // Create suggestion text with matched field
                    String matchType = "";
                    if (cusName.contains(searchText)) {
                        matchType = "Customer: " + order.getCus_name();
                    } else if (staffName.contains(searchText)) {
                        matchType = "Staff: " + order.getStaff_name();
                    } else if (orderDate.contains(searchText)) {
                        matchType = "Date: " + orderDate;
                    } else if (itemNamesStr.contains(searchText)) {
                        matchType = "Items: " + (itemNames.length > 0 ? itemNames[0] : "");
                    } else {
                        matchType = "Order #" + orderId;
                    }
                    
                    String suggestionText = matchType + " - $" + String.format("%.2f", order.getTotal_amount());
                    MenuItem item = new MenuItem(suggestionText);
                    
                    // Set action for when suggestion is clicked
                    item.setOnAction(e -> {
                        SearchText.setText(newText);
                        searchSuggestions.hide();
                        handleSearch();
                    });
                    
                    matches.add(item);
                    
                    // Limit suggestions to 10
                    if (matches.size() >= 10) {
                        break;
                    }
                }
            }
            
            // Show suggestions if matches found
            if (!matches.isEmpty()) {
                searchSuggestions.getItems().addAll(matches);
                searchSuggestions.show(SearchText, Side.BOTTOM, 0, 0);
            } else {
                searchSuggestions.hide();
            }
        });
        
        // Hide suggestions when text field loses focus
        SearchText.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                searchSuggestions.hide();
            }
        });
    }
    
    private void handleSearch() {
        String searchText = SearchText.getText().trim();
        
        // If search text is empty, show all orders
        if (searchText.isEmpty()) {
            orderTable.setItems(allOrdersData);
            return;
        }
        
        // Search through all orders
        searchResultData.clear();
        String searchLower = searchText.toLowerCase();
        
        for (managerOrderView order : allOrdersData) {
            String orderId = String.valueOf(order.getOrder_id());
            String cusName = order.getCus_name();
            String staffName = order.getStaff_name();
            String installment = order.getIs_installmenat();
            String orderDate = order.getOrder_date() != null ? order.getOrder_date().toString() : "";
            
            // Get car/part names from the order
            String[] itemNames = order.getCarsandparts_name();
            StringBuilder allItemNames = new StringBuilder();
            if (itemNames != null) {
                for (String name : itemNames) {
                    allItemNames.append(name.toLowerCase()).append(" ");
                }
            }
            String itemNamesStr = allItemNames.toString();
            
            // Check if search text matches any field
            if (orderId.contains(searchLower) ||
                cusName.toLowerCase().contains(searchLower) ||
                staffName.toLowerCase().contains(searchLower) ||
                orderDate.contains(searchLower) ||
                itemNamesStr.contains(searchLower) ||
                (installment != null && installment.toLowerCase().contains(searchLower))) {
                searchResultData.add(order);
            }
        }
        
        // Update table with search results
        orderTable.setItems(searchResultData);
        
        // Auto-select first result if exists
        if (!searchResultData.isEmpty()) {
            orderTable.getSelectionModel().selectFirst();
            displaySelectedOrderDetails();
        } else {
            orderTable.getSelectionModel().clearSelection();
            clearOrderDetails();
        }
        
        // Recalculate sold quantities based on search results
        calculateSoldQuantities();
        
        logger.info("Search completed: found {} orders matching '{}'", searchResultData.size(), searchText);
    }
    
    private void currentDateSelect() {
        currentMonth = today.getMonthValue();
        currentYear = today.getYear();
        nextMonthbtn.setDisable(true);
        nextMonthbtn.setVisible(false);
        nextYearbtn.setDisable(true);
        nextYearbtn.setVisible(false);
    }
    
    private void initializeMonthYearBoxes() {
        // Populate year box (e.g., from 2020 to current year)
        yearBox.getItems().clear();
        int startYear = 2020; // Adjust based on your business needs
        for (int y = startYear; y <= today.getYear(); y++) {
            yearBox.getItems().add(y);
        }
        yearBox.setValue(currentYear);
        
        // Populate month box
        updateMonthBoxForYear(currentYear);
    }
    
    private void updateMonthBoxForYear(int year) {
        int startMonth = 1;
        int endMonth = 12;
        int currentYearNow = LocalDate.now().getYear();
        int currentMonthNow = LocalDate.now().getMonthValue();
        
        // If selected year is THIS YEAR, show months from January to current month only
        if (year == currentYearNow) {
            endMonth = currentMonthNow;
        }
        // If selected year is PREVIOUS YEAR (or older), show all 12 months
        else if (year < currentYearNow) {
            endMonth = 12;
        }
        // If selected year is FUTURE YEAR (shouldn't happen, but handle it)
        else {
            endMonth = 12;
        }
        
        List<String> months = new ArrayList<>();
        for (int m = startMonth; m <= endMonth; m++) {
            months.add(Month.of(m).getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
        }
        
        updatingMonthBox = true;
        monthBox.setItems(FXCollections.observableArrayList(months));
        
        // Make sure currentMonth is valid for the selected year
        String currentMonthName = Month.of(currentMonth).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        if (!months.contains(currentMonthName)) {
            // If current month is not available, default to last available month
            currentMonth = endMonth;
            currentMonthName = Month.of(currentMonth).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        }
        
        monthBox.setValue(currentMonthName);
        updatingMonthBox = false;
    }
    
    private void updateYearMonthLabel() {
        Month nmonth = Month.of(currentMonth);
        int curyear = today.getYear();
        int curmonth = today.getMonthValue();
        
        // Control next year/month button visibility
        if (currentYear >= curyear) {
            nextYearbtn.setDisable(true);
            nextYearbtn.setVisible(false);
            if (currentMonth >= curmonth) {
                nextMonthbtn.setDisable(true);
                nextMonthbtn.setVisible(false);
            } else {
                nextMonthbtn.setDisable(false);
                nextMonthbtn.setVisible(true);
            }
        } else {
            nextYearbtn.setDisable(false);
            nextYearbtn.setVisible(true);
            nextMonthbtn.setDisable(false);
            nextMonthbtn.setVisible(true);
        }
        
        // Control previous year/month button visibility
        int startYear = 2020; // Should match initializeMonthYearBoxes
        if (currentYear <= startYear) {
            previousYearbth.setDisable(true);
            previousYearbth.setVisible(false);
            if (currentMonth <= 1) {
                PreviousMonthbtn.setDisable(true);
                PreviousMonthbtn.setVisible(false);
            } else {
                PreviousMonthbtn.setDisable(false);
                PreviousMonthbtn.setVisible(true);
            }
        } else {
            previousYearbth.setDisable(false);
            previousYearbth.setVisible(true);
            PreviousMonthbtn.setDisable(false);
            PreviousMonthbtn.setVisible(true);
        }
        
        // Sync ComboBoxes with updated currentMonth/currentYear
        String monthName = nmonth.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        if (monthBox.getItems().contains(monthName)) {
            updatingMonthBox = true;
            monthBox.setValue(monthName);
            updatingMonthBox = false;
        }
        
        if (yearBox.getItems().contains(currentYear)) {
            yearBox.setValue(currentYear);
        }
        
        // Reload orders for the selected month/year
        loadOrder();
    }

    private void orderDetails(managerOrderView orders) throws IOException {

        // Load order details
        String[] names = orders.getCarsandparts_name();
        String[] qty = orders.getCarsandparts_qty();
        String[] price = orders.getCarsandparts_perprice();

        totalPriceLabel.setText(String.valueOf(orders.getTotal_amount()));
        remainAmountlbl.setText(String.valueOf(orders.getRemain_amount()));
        paidAmountlbl.setText(String.valueOf(orders.getPayed_amount()));
        customerNamelabel.setText(orders.getCus_name());
        staffNamelabel.setText(orders.getStaff_name());
        
        // Set due date
        if (orders.getDue_date() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            dueDateLabel.setText(new java.sql.Date(orders.getDue_date().getTime()).toLocalDate().format(formatter));
        } else {
            dueDateLabel.setText("N/A");
        }

        // Clear and populate the installment table
        installmentTable.getItems().clear();

        for (int i = 0; i < names.length; i++) {
            // Create a formatted string for the table row (no need to trim if data is clean)
            String rowData = String.format("%s|%s|%s", names[i].trim(), qty[i].trim(), price[i].trim());
            installmentTable.getItems().add(rowData);
        }
    }
    
    private void clearOrderDetails() {
        customerNamelabel.setText("");
        staffNamelabel.setText("");
        totalPriceLabel.setText("0.00");
        paidAmountlbl.setText("0.00");
        remainAmountlbl.setText("0.00");
        dueDateLabel.setText("N/A");
        installmentTable.getItems().clear();
    }
    
    private void displaySelectedOrderDetails() {
        managerOrderView selectedOrder = orderTable.getSelectionModel().getSelectedItem();
        if (selectedOrder != null) {
            try {
                orderDetails(selectedOrder);
            } catch (IOException e) {
                logger.error("Error displaying order details", e);
            }
        }
    }
    
    private void calculateSoldQuantities() {
        // Current month values
        int confirmQty = 0;
        double confirmPrice = 0.0;
        int pendingQty = 0;
        double pendingPrice = 0.0;
        
        // Previous month values for rate calculation
        double prevConfirmPrice = 0.0;
        double prevPendingPrice = 0.0;
        
        // Get the currently displayed orders (filtered by month/year or search)
        ObservableList<managerOrderView> currentOrders = orderTable.getItems();
        
        // Calculate current month totals
        if (currentOrders != null && !currentOrders.isEmpty()) {
            for (managerOrderView order : currentOrders) {
                boolean isInstallment = order.getIs_installmenat().equalsIgnoreCase("Yes");
                int orderQty = order.getTotalQty();
                double orderAmount = order.getTotal_amount();
                
                if (isInstallment) {
                    pendingQty += orderQty;
                    pendingPrice += orderAmount;
                } else {
                    confirmQty += orderQty;
                    confirmPrice += orderAmount;
                }
            }
        }
        
        // Calculate previous month totals
        int prevMonth = currentMonth - 1;
        int prevYear = currentYear;
        if (prevMonth < 1) {
            prevMonth = 12;
            prevYear--;
        }
        
        // Filter allOrdersData for previous month
        for (managerOrderView order : allOrdersData) {
            if (order.getOrder_date() != null) {
                // Convert java.sql.Date to LocalDate
                LocalDate orderDate = new java.sql.Date(order.getOrder_date().getTime()).toLocalDate();
                
                // Check if order is from previous month/year
                if (orderDate.getMonthValue() == prevMonth && orderDate.getYear() == prevYear) {
                    boolean isInstallment = order.getIs_installmenat().equalsIgnoreCase("Yes");
                    double orderAmount = order.getTotal_amount();
                    
                    if (isInstallment) {
                        prevPendingPrice += orderAmount;
                    } else {
                        prevConfirmPrice += orderAmount;
                    }
                }
            }
        }
        
        // Set current values
        setCarCircle(confirmQty);
        setCarPrice(confirmPrice);
        setPartCircle(pendingQty);
        setPartPrice(pendingPrice);
        
        // Calculate and set rates
        setConfirmPriceRate(confirmPrice, prevConfirmPrice);
        setPendingPriceRate(pendingPrice, prevPendingPrice);
    }
    
    private void setCarCircle(int soldQty) {
        // Show only sold quantity (sold cars)
        confrimQty.setText(String.valueOf(soldQty));
        confrimQtyCircle.setVisible(true);
        
        // Set text style
        if (soldQty == 0) {
            confrimQty.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: normal;");
        } else {
            confrimQty.setStyle("-fx-text-fill: #6d8196; -fx-font-weight: bold;");
        }
        
        // No stroke animation - clear dash array
        confrimQtyCircle.getStrokeDashArray().clear();
    }
    
    private void setPartCircle(int soldQty) {
        // Show only sold quantity (sold parts)
        pendingQty.setText(String.valueOf(soldQty));
        pendingQtyCircle.setVisible(true);
        
        // Set text style
        if (soldQty == 0) {
            pendingQty.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: normal;");
        } else {
            pendingQty.setStyle("-fx-text-fill: #6d8196; -fx-font-weight: bold;");
        }
        
        // No stroke animation - clear dash array
        pendingQtyCircle.getStrokeDashArray().clear();
    }
    
    private void setCarPrice(double totalPrice) {
        // Format and display total car sales price
        confrimPriceLabel.setText(String.format("$%.2f", totalPrice));
        
        // Set text style
        if (totalPrice == 0) {
            confrimPriceLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: normal;");
        } else {
            confrimPriceLabel.setStyle("-fx-text-fill: #6d8196; -fx-font-weight: bold;");
        }
    }
    
    private void setPartPrice(double totalPrice) {
        // Format and display total pending (installment) price
        pendingPriceLabel.setText(String.format("$%.2f", totalPrice));
        
        // Set text style
        if (totalPrice == 0) {
            pendingPriceLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: normal;");
        } else {
            pendingPriceLabel.setStyle("-fx-text-fill: #6d8196; -fx-font-weight: bold;");
        }
    }
    
    private void setConfirmPriceRate(double currentPrice, double previousPrice) {
        // Calculate percentage change from previous month to current month
        double rate = 0.0;
        String rateText = "0.0%";
        String style = "-fx-text-fill: #94a3b8; -fx-font-weight: normal;";
        
        if (previousPrice > 0) {
            rate = ((currentPrice - previousPrice) / previousPrice) * 100;
            
            if (rate > 0) {
                // Positive growth - green
                rateText = String.format("+%.1f%%", rate);
                style = "-fx-text-fill: #10b981; -fx-font-weight: bold;";
            } else if (rate < 0) {
                // Negative growth - red
                rateText = String.format("%.1f%%", rate);
                style = "-fx-text-fill: #ef4444; -fx-font-weight: bold;";
            } else {
                // No change - gray
                rateText = "0.0%";
                style = "-fx-text-fill: #94a3b8; -fx-font-weight: normal;";
            }
        } else if (currentPrice > 0) {
            // No previous data but have current data - show as new
            rateText = "New";
            style = "-fx-text-fill: #3b82f6; -fx-font-weight: bold;";
        }
        
        confirmPriceRateLabel.setText(rateText);
        confirmPriceRateLabel.setStyle(style);
    }
    
    private void setPendingPriceRate(double currentPrice, double previousPrice) {
        // Calculate percentage change from previous month to current month
        double rate = 0.0;
        String rateText = "0.0%";
        String style = "-fx-text-fill: #94a3b8; -fx-font-weight: normal;";
        
        if (previousPrice > 0) {
            rate = ((currentPrice - previousPrice) / previousPrice) * 100;
            
            if (rate > 0) {
                // Positive growth - green
                rateText = String.format("+%.1f%%", rate);
                style = "-fx-text-fill: #10b981; -fx-font-weight: bold;";
            } else if (rate < 0) {
                // Negative growth - red
                rateText = String.format("%.1f%%", rate);
                style = "-fx-text-fill: #ef4444; -fx-font-weight: bold;";
            } else {
                // No change - gray
                rateText = "0.0%";
                style = "-fx-text-fill: #94a3b8; -fx-font-weight: normal;";
            }
        } else if (currentPrice > 0) {
            // No previous data but have current data - show as new
            rateText = "New";
            style = "-fx-text-fill: #3b82f6; -fx-font-weight: bold;";
        }
        
        pendingPriceRateLabel.setText(rateText);
        pendingPriceRateLabel.setStyle(style);
    }

}
