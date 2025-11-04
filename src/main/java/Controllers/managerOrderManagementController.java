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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import Database.Porsche_DB;
import Model.managerOrderView;
import Utils.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
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
    private ObservableList<managerOrderView> allOrdersData = FXCollections.observableArrayList();
    private ObservableList<managerOrderView> searchResultData = FXCollections.observableArrayList();
    private int managerId;
    private ContextMenu searchSuggestions = new ContextMenu();
    private boolean dataLoaded = false; // Track if data has been loaded from database
    
    private LocalDate today = LocalDate.now();
    private int currentMonth;
    private int currentYear;
    private boolean listenersInitialized = false;
    private boolean updatingMonthBox = false;
    private boolean isWeeklyView = true; // Track which revenue view is active

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
    private TableColumn<managerOrderView, Integer> qtyCol;

    @FXML
    private Label remainAmountlbl;

    @FXML
    private Label dueDateLabel;

    @FXML
    private AreaChart<String, Number> revenueChart;

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
        
        isWeeklyView = false;
        // Update button styles
        activateRevenueButton(monthlyRevenue);
        
        try {
            showMonthlyRevenueChart();
        } catch (Exception e) {
            System.err.println("Error showing monthly revenue chart: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void clickNextMonthbtn(ActionEvent event) {
        currentMonth++;
        if (currentMonth > 12) {
            currentMonth = 1;
            currentYear++;
        }
        updateYearMonthLabel();
        filterOrders(); // Filter with new month/year
        refreshRevenueChart(); // Refresh the active chart view
    }

    @FXML
    void clickNextYearbtn(ActionEvent event) {
        if (currentYear < today.getYear()) {
            currentYear++;
            if (currentYear == today.getYear()) {
                // Reset to current month when reaching current year
                currentMonth = today.getMonthValue();
            }
        }
        updateYearMonthLabel();
        filterOrders(); // Filter with new year
        refreshRevenueChart(); // Refresh the active chart view
    }

    @FXML
    void clickPreviousMonthbtn(ActionEvent event) {
        currentMonth--;
        if (currentMonth < 1) {
            currentMonth = 12;
            currentYear--;
        }
        updateYearMonthLabel();
        filterOrders(); // Filter with new month/year
        refreshRevenueChart(); // Refresh the active chart view
    }

    @FXML
    void clickPreviousYearbtn(ActionEvent event) {
        currentYear--;
        updateYearMonthLabel();
        filterOrders(); // Filter with new year
        refreshRevenueChart(); // Refresh the active chart view
    }

    @FXML
    void clickWeeklyRevenue(ActionEvent event) {
        
        isWeeklyView = true;
        // Update button styles
        activateRevenueButton(weeklyRevenue);
        
        try {
            showWeeklyRevenueChart();
        } catch (Exception e) {
            System.err.println("Error showing weekly revenue chart: " + e.getMessage());
            e.printStackTrace();
        }
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
        // Get the manager id from session
        Session current = Session.getInstance();
        if (current != null) {
            managerId = current.getUserid();
        }
        
        orderDateCol.setCellValueFactory(new PropertyValueFactory<>("order_date"));
        customerNameCol.setCellValueFactory(new PropertyValueFactory<>("cus_name"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("total_amount"));
        staffNameCol.setCellValueFactory(new PropertyValueFactory<>("staff_name"));
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("totalQty"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("is_installmenat"));
        
        // Format date column properly with visible text
        orderDateCol.setCellFactory(column -> new TableCell<managerOrderView, Date>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(new java.sql.Date(item.getTime()).toLocalDate().format(formatter));
                }
            }
        });

        // Format customer name column with visible text
        customerNameCol.setCellFactory(column -> new TableCell<managerOrderView, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                }
            }
        });

        // Format staff name column with visible text
        staffNameCol.setCellFactory(column -> new TableCell<managerOrderView, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                }
            }
        });

        // Format quantity column with visible text
        qtyCol.setCellFactory(column -> new TableCell<managerOrderView, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });

        // Format price column properly with visible text
        priceCol.setCellFactory(column -> new TableCell<managerOrderView, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", item));
                }
            }
        });

        // Format status column with visible text
        statusCol.setCellFactory(column -> new TableCell<managerOrderView, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                }
            }
        });

        // Force table and text visibility with normal font weight
        orderTable.setFixedCellSize(-1);
        
        orderTable.setRowFactory(tv -> {
            TableRow<managerOrderView> row = new TableRow<>();
            
            // Update row style based on selection
//            row.itemProperty().addListener((obs, oldItem, newItem) -> updateRowStyle(row));
//            row.selectedProperty().addListener((obs, oldSelected, newSelected) -> updateRowStyle(row));
            
            row.setPrefHeight(javafx.scene.layout.Region.USE_COMPUTED_SIZE);

//            row.setOnMouseExited(e -> {
//                updateRowStyle(row);
//            });

            return row;
        });
        
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
                        text.getStyleClass().add("textHeader");
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
        
        // Set up installment table with fixed row size
        installmentTable.setFixedCellSize(60);

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
        
        // Load all orders initially from database (only once)
        loadAllOrdersFromDatabase();
        
        // Filter and display orders for current month/year
        filterOrders();
        
        // Initialize revenue chart buttons and show weekly chart by default
        activateRevenueButton(weeklyRevenue);
        try {
            showWeeklyRevenueChart();
        } catch (Exception e) {
            System.err.println("Error loading initial revenue chart: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Set up listeners for month/year boxes (only once)
        if (!listenersInitialized) {
            yearBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    currentYear = newVal;
                    updateMonthBoxForYear(currentYear); // Update available months based on selected year
                    updateYearMonthLabel();
                    filterOrders(); // Filter with new year (also recalculates quantities)
                    refreshRevenueChart(); // Refresh the active chart view
                }
            });
            
            monthBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                if (!updatingMonthBox && newVal != null) {
                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);
                    Month parsedMonth = Month.from(fmt.parse(newVal));
                    currentMonth = parsedMonth.getValue();
                    updateYearMonthLabel();
                    filterOrders(); // Filter with new month (also recalculates quantities)
                    refreshRevenueChart(); // Refresh the active chart view
                }
            });
            
            listenersInitialized = true;
        }
    }

    /**
     * Load all orders from database ONCE (called only during initialization)
     * This method queries the database and stores all orders in memory
     */
    private void loadAllOrdersFromDatabase(){
        // Skip if data already loaded
        if (dataLoaded) {
            return;
        }
        
        CallableStatement cs = null;
        ResultSet rs = null;
        Connection tempCon = null;
        
        try {
            // Establish database connection
            Porsche_DB db = new Porsche_DB();
            tempCon = db.connect();
            
            List<managerOrderView> ordersList = new ArrayList<>();
            
            // Call stored procedure to get ALL orders (no month/year filter)
            cs = tempCon.prepareCall("CALL getAllOrders(?, ?, ?)");
            cs.setInt(1, managerId);
            cs.setNull(2, java.sql.Types.INTEGER);  // month = NULL (get all months)
            cs.setNull(3, java.sql.Types.INTEGER);  // year = NULL (get all years)
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
            dataLoaded = true; // Mark data as loaded
            
            logger.info("Loaded {} orders from database successfully", ordersList.size());
            
        } catch (SQLException e) {
            logger.error("Error loading orders from database", e);
            dataLoaded = false;
        } finally {
            // Clean up resources
            try {
                if (rs != null) rs.close();
                if (cs != null) cs.close();
                if (tempCon != null) tempCon.close();
            } catch (SQLException e) {
                logger.error("Error closing database resources", e);
            }
        }
    }
    
    /**
     * Filter already-loaded orders based on current month/year
     * This method works with in-memory data (no database query)
     */
    private void filterOrders() {
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
            ObservableList<managerOrderView> observableList = FXCollections.observableArrayList(filteredOrders);
            orderTable.setItems(observableList);
            orderTable.refresh(); // Force table refresh
            
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
            orderTable.refresh(); // Force table refresh
            
            // Auto-select first row if data exists and display details
            if (!allOrdersData.isEmpty()) {
                orderTable.getSelectionModel().selectFirst();
                displaySelectedOrderDetails();
            } else {
                orderTable.getSelectionModel().clearSelection();
                clearOrderDetails();
            }
        }
        
        // Calculate quantities and prices after filtering orders
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
            // Clear stored search value when user manually types
            SearchText.setUserData(null);
            
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
                    
                    // Create suggestion text with matched field and determine search value
                    String matchType = "";
                    String searchValue = ""; // The actual value to search for
                    if (cusName.contains(searchText)) {
                        matchType = "Customer: " + order.getCus_name();
                        searchValue = order.getCus_name();
                    } else if (staffName.contains(searchText)) {
                        matchType = "Staff: " + order.getStaff_name();
                        searchValue = order.getStaff_name();
                    } else if (orderDate.contains(searchText)) {
                        matchType = "Date: " + orderDate;
                        searchValue = orderDate;
                    } else if (itemNamesStr.contains(searchText)) {
                        matchType = "Items: " + (itemNames.length > 0 ? itemNames[0] : "");
                        searchValue = itemNames.length > 0 ? itemNames[0] : "";
                    } else {
                        matchType = "Order #" + orderId;
                        searchValue = orderId;
                    }
                    
                    String suggestionText = matchType + " - $" + String.format("%.2f", order.getTotal_amount());
                    MenuItem item = new MenuItem(suggestionText);
                    
                    // Set action for when suggestion is clicked
                    final String finalSearchValue = searchValue; // Make effectively final for lambda
                    final String finalSuggestionText = suggestionText;
                    item.setOnAction(e -> {
                        SearchText.setText(finalSuggestionText); // Display full text in search bar
                        SearchText.setUserData(finalSearchValue); // Store actual search value
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
        
        // Use stored search value if available (from suggestion click), otherwise use the text field value
        String actualSearchValue = SearchText.getUserData() != null ? 
                                   SearchText.getUserData().toString() : searchText;
        
        // Search through all orders
        searchResultData.clear();
        String searchLower = actualSearchValue.toLowerCase();
        
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
        
        // Filter orders for the selected month/year (no database query)
        filterOrders();
    }

    private void orderDetails(managerOrderView orders) throws IOException {

        // Load order details
        String[] names = orders.getCarsandparts_name();
        String[] qty = orders.getCarsandparts_qty();
        String[] price = orders.getCarsandparts_perprice();

        totalPriceLabel.setText("$" + String.format("%.2f", orders.getTotal_amount()));
        remainAmountlbl.setText("$" + String.format("%.2f", orders.getRemain_amount()));
        paidAmountlbl.setText("$" + String.format("%.2f", orders.getPayed_amount()));
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

        // Find the minimum length to avoid ArrayIndexOutOfBoundsException
        int minLength = Math.min(Math.min(names.length, qty.length), price.length);
        
        for (int i = 0; i < minLength; i++) {
            // Create a formatted string for the table row (no need to trim if data is clean)
            String rowData = String.format("%s|%s|%s", names[i].trim(), qty[i].trim(), price[i].trim());
            installmentTable.getItems().add(rowData);
        }
    }
    
    private void clearOrderDetails() {
        customerNamelabel.setText("");
        staffNamelabel.setText("");
        totalPriceLabel.setText("$0.00");
        paidAmountlbl.setText("$0.00");
        remainAmountlbl.setText("$0.00");
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
    
    private void activateRevenueButton(Button activeBtn) {
        // Reset both buttons to inactive style
        String inactiveStyle = "-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 8 16 8 16; -fx-background-radius: 8;";
        String activeStyle = "-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 8 16 8 16; -fx-background-radius: 8;";
        
        weeklyRevenue.setStyle(inactiveStyle);
        monthlyRevenue.setStyle(inactiveStyle);
        
        // Set active button style
        activeBtn.setStyle(activeStyle);
    }
    
    private void refreshRevenueChart() {
        try {
            if (isWeeklyView) {
                showWeeklyRevenueChart();
            } else {
                showMonthlyRevenueChart();
            }
        } catch (Exception e) {
            System.err.println("Error refreshing revenue chart: " + e.getMessage());
            e.printStackTrace();
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
        
        // Calculate current month totals for quantities and prices
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
        
        // Calculate total orders for percentage calculation
        int totalOrders = confirmQty + pendingQty;
        
        // Set current values with targets and actual sold quantities from orders
        // Calculate pending progress first to pass to confirm circle
        double pendingProgress = (totalOrders > 0) ? (double) pendingQty / totalOrders : 0;
        setPendingQtyCircle(pendingQty, totalOrders);
        setPartPrice(pendingPrice);
        setConfirmQtyCircle(confirmQty, totalOrders, pendingProgress);
        setCarPrice(confirmPrice);
        
        // Calculate and set rates
        setConfirmPriceRate(confirmPrice, prevConfirmPrice);
        setPendingPriceRate(pendingPrice, prevPendingPrice);
    }
    
    /**
     * Sets the confirm quantity circle based on non-installment orders
     * @param confirmQty Number of confirmed (non-installment) orders
     * @param totalOrders Total number of all orders (confirm + pending)
     * @param pendingProgress Progress of the pending circle (0 to 1) to determine start position
     */
    private void setConfirmQtyCircle(int confirmQty, int totalOrders, double pendingProgress) {
        confrimQtyCircle.setVisible(true);
        confrimQty.setText(String.valueOf(confirmQty));
        
        double progressConfirm = 0;
        if (confirmQty == 0 && totalOrders == 0) {
            confrimQtyCircle.setVisible(false);
            confrimQty.setText("0");
            confrimQty.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: normal;");
        } else if (totalOrders > 0) {
            // Calculate progress as percentage of total orders
            progressConfirm = (double) confirmQty / totalOrders;
            // Progress is already between 0 and 1 since confirmQty <= totalOrders
            confrimQty.setStyle("-fx-text-fill: #6d8196; -fx-font-weight: bold; -fx-font-size: 16;");
        } else {
            // No orders, just show confirm count
            confrimQty.setText(String.valueOf(confirmQty));
            confrimQty.setStyle("-fx-text-fill: #6d8196; -fx-font-weight: bold; -fx-font-size: 16;");
            progressConfirm = 0;
        }
        
        // Set stroke color and width for confirm circle
        confrimQtyCircle.setStroke(javafx.scene.paint.Color.web("#6d8196"));
        confrimQtyCircle.setStrokeWidth(6);
        confrimQtyCircle.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        confrimQtyCircle.setFill(javafx.scene.paint.Color.TRANSPARENT);
        
        // Set circular progress animation with confirm color #6d8196
        double circumference = 2 * Math.PI * confrimQtyCircle.getRadius();
        confrimQtyCircle.getStrokeDashArray().setAll(circumference, circumference);
        
        // Rotate the inner circle to start where outer circle ends
        // Pending circle ends at: -90 + (pendingProgress * 360) degrees
        double startAngle = -90 + (pendingProgress * 360);
        confrimQtyCircle.setRotate(startAngle);
        
        // Calculate final offset
        double finalOffset = circumference - (circumference * progressConfirm);
        
        // Start with full offset (hidden) and animate to final offset
        confrimQtyCircle.setStrokeDashOffset(circumference);
        
        // Create wave animation
        javafx.animation.Timeline timeline = new javafx.animation.Timeline();
        javafx.animation.KeyFrame keyFrame = new javafx.animation.KeyFrame(
            javafx.util.Duration.millis(1500), // 1.5 seconds
            new javafx.animation.KeyValue(
                confrimQtyCircle.strokeDashOffsetProperty(),
                finalOffset,
                javafx.animation.Interpolator.EASE_OUT
            )
        );
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }
    
    /**
     * Sets the pending quantity circle based on installment orders
     * @param pendingQty Number of pending (installment) orders
     * @param totalOrders Total number of all orders (confirm + pending)
     */
    private void setPendingQtyCircle(int pendingQty, int totalOrders) {
        pendingQtyCircle.setVisible(true);
        this.pendingQty.setText(String.valueOf(pendingQty));
        
        double progressPending = 0;
        if (pendingQty == 0 && totalOrders == 0) {
            pendingQtyCircle.setVisible(false);
            this.pendingQty.setText("0");
            this.pendingQty.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: normal;");
        } else if (totalOrders > 0) {
            // Calculate progress as percentage of total orders
            progressPending = (double) pendingQty / totalOrders;
            // Progress is already between 0 and 1 since pendingQty <= totalOrders
            this.pendingQty.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold; -fx-font-size: 16;");
        } else {
            // No orders, just show pending count
            this.pendingQty.setText(String.valueOf(pendingQty));
            this.pendingQty.setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold; -fx-font-size: 16;");
            progressPending = 0;
        }
        
        // Set stroke color and width for pending circle
        pendingQtyCircle.setStroke(javafx.scene.paint.Color.web("#e67e22"));
        pendingQtyCircle.setStrokeWidth(6);
        pendingQtyCircle.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        pendingQtyCircle.setFill(javafx.scene.paint.Color.TRANSPARENT);
        
        // Set circular progress animation with pending color #e67e22
        double circumference = 2 * Math.PI * pendingQtyCircle.getRadius();
        pendingQtyCircle.getStrokeDashArray().setAll(circumference, circumference);
        
        // Start from top (rotate -90 degrees so stroke starts at 12 o'clock position)
        pendingQtyCircle.setRotate(-90);
        
        // Calculate final offset
        double finalOffset = circumference - (circumference * progressPending);
        
        // Start with full offset (hidden) and animate to final offset
        pendingQtyCircle.setStrokeDashOffset(circumference);
        
        // Create wave animation
        javafx.animation.Timeline timeline = new javafx.animation.Timeline();
        javafx.animation.KeyFrame keyFrame = new javafx.animation.KeyFrame(
            javafx.util.Duration.millis(1500), // 1.5 seconds
            new javafx.animation.KeyValue(
                pendingQtyCircle.strokeDashOffsetProperty(),
                finalOffset,
                javafx.animation.Interpolator.EASE_OUT
            )
        );
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
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
    
    @SuppressWarnings("unchecked")
    private void showWeeklyRevenueChart() {
        
        // Check if chart is null
        if (revenueChart == null) {
            System.err.println("ERROR: revenueChart is null! Check FXML fx:id='revenueChart'");
            return;
        }
        
        try {
            // Clear existing chart data and force layout refresh
            revenueChart.getData().clear();
            revenueChart.layout();
            revenueChart.setTitle(null); // Remove title since info is shown in top boxes
            
            // Create series for the chart (using raw type to match FXML)
            XYChart.Series series = new XYChart.Series();
            series.setName("Revenue");
        
        // Get the number of days in the selected month
        LocalDate firstDayOfMonth = LocalDate.of(currentYear, currentMonth, 1);
        int daysInMonth = firstDayOfMonth.lengthOfMonth();
        int numberOfWeeks = (int) Math.ceil(daysInMonth / 7.0);
        
        // Pre-calculate revenue by week using HashMap for O(1) lookup
        Map<Integer, Double> weekRevenueMap = new HashMap<>();
        for (int i = 1; i <= numberOfWeeks; i++) {
            weekRevenueMap.put(i, 0.0);
        }
        
        // Single pass through orders - O(n) instead of O(n*weeks)
        for (managerOrderView order : allOrdersData) {
            if (order.getOrder_date() != null) {
                LocalDate orderDate = new java.sql.Date(order.getOrder_date().getTime()).toLocalDate();
                
                // Check if order is in current month/year
                if (orderDate.getYear() == currentYear && orderDate.getMonthValue() == currentMonth) {
                    int dayOfMonth = orderDate.getDayOfMonth();
                    int week = ((dayOfMonth - 1) / 7) + 1; // Calculate week number
                    weekRevenueMap.put(week, weekRevenueMap.get(week) + order.getTotal_amount());
                }
            }
        }
        
        // Add data to chart
        for (int week = 1; week <= numberOfWeeks; week++) {
            double weekRevenue = weekRevenueMap.get(week);
            
            String weekLabel = "Week " + week;
            series.getData().add(new XYChart.Data(weekLabel, weekRevenue));
        }
        
        if (series.getData().isEmpty()) {
            System.err.println("WARNING: No data to display in chart!");
        }
        
        revenueChart.getData().add(series);
        revenueChart.setLegendVisible(false);
        revenueChart.setAnimated(false); // Disable animation to prevent label overlap issues
        revenueChart.setCreateSymbols(true); // Enable symbols for smooth curve styling
        
        // Force layout update after adding data
        revenueChart.layout();
        
        // Hide chart temporarily to prevent showing sharp lines
        revenueChart.setOpacity(0);
        
        // Apply smooth curves after a short delay to ensure paths are created
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(100));
        pause.setOnFinished(e -> applySmoothCurves());
        pause.play();
        
        } catch (Exception e) {
            System.err.println("ERROR in showWeeklyRevenueChart: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings("unchecked")
    private void showMonthlyRevenueChart() {
        
        // Check if chart is null
        if (revenueChart == null) {
            System.err.println("ERROR: revenueChart is null! Check FXML fx:id='revenueChart'");
            return;
        }
        
        try {
            // Clear existing chart data and force layout refresh
            revenueChart.getData().clear();
            revenueChart.layout();
            revenueChart.setTitle(null); // Remove title since info is shown in top boxes
            
            // Create series for the chart (using raw type to match FXML)
            XYChart.Series series = new XYChart.Series();
            series.setName("Revenue");
        
        // Pre-calculate revenue by month using HashMap for O(1) lookup
        Map<Integer, Double> monthRevenueMap = new HashMap<>();
        for (int i = 1; i <= 12; i++) {
            monthRevenueMap.put(i, 0.0);
        }
        
        // Single pass through orders - O(n) instead of O(n*12)
        for (managerOrderView order : allOrdersData) {
            if (order.getOrder_date() != null) {
                LocalDate orderDate = new java.sql.Date(order.getOrder_date().getTime()).toLocalDate();
                
                // Check if order is in current year
                if (orderDate.getYear() == currentYear) {
                    int month = orderDate.getMonthValue();
                    monthRevenueMap.put(month, monthRevenueMap.get(month) + order.getTotal_amount());
                }
            }
        }
        
        // Add data to chart
        for (int month = 1; month <= 12; month++) {
            double monthRevenue = monthRevenueMap.get(month);
            String monthLabel = Month.of(month).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            series.getData().add(new XYChart.Data(monthLabel, monthRevenue));
        }
        
        if (series.getData().isEmpty()) {
            System.err.println("WARNING: No data to display in chart!");
        }
        
        revenueChart.getData().add(series);
        revenueChart.setLegendVisible(false);
        revenueChart.setAnimated(false); // Disable animation to prevent label overlap issues
        revenueChart.setCreateSymbols(true); // Enable symbols for smooth curve styling
        
        // Force layout update after adding data
        revenueChart.layout();
        
        // Hide chart temporarily to prevent showing sharp lines
        revenueChart.setOpacity(0);
        
        // Apply smooth curves after a short delay to ensure paths are created
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(100));
        pause.setOnFinished(e -> applySmoothCurves());
        pause.play();
        
        } catch (Exception e) {
            System.err.println("ERROR in showMonthlyRevenueChart: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Updates the style of a table row based on its selection state
     */
//    private void updateRowStyle(TableRow<managerOrderView> row) {
//
//        if (row.isEmpty()) {
//            row.setStyle("");
//        } else if (row.isSelected()) {
//            // Selected row: bold text with highlight background and spacing
//            row.setStyle("-fx-background-color: #e3f2fd; " +
//                        "-fx-text-fill: black !important; " +
//                        "-fx-font-size: 14px !important; " +
//                        "-fx-font-weight: bold; " +
//                        "-fx-border-color: #2196f3; " +
//                        "-fx-border-width: 0 0 2 0; " +
//                        "-fx-padding: 8 0 8 0;");
//        } else {
//            // Normal row: regular text with spacing
//            row.setStyle("-fx-background-color: white; " +
//                        "-fx-text-fill: black !important; " +
//                        "-fx-font-size: 14px !important; " +
//                        "-fx-font-weight: normal; " +
//                        "-fx-border-color: #e9ecef; " +
//                        "-fx-border-width: 0 0 1 0; " +
//                        "-fx-padding: 8 0 8 0;");
//        }
//    }
    
    // ---------- Smooth Curve Methods ----------
    private void applySmoothCurves() {
        if (revenueChart == null)
            return;

        // Check if paths are ready, if not, retry after a short delay
        var linePaths = revenueChart.lookupAll(".chart-series-area-line");
        var fillPaths = revenueChart.lookupAll(".chart-series-area-fill");

        if (linePaths.isEmpty() || fillPaths.isEmpty()) {
            // Paths not ready yet, try again after 50ms
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(50));
            pause.setOnFinished(e -> applySmoothCurves());
            pause.play();
            return;
        }

        // Store the smoothed line elements to copy to fill area
        java.util.List<javafx.scene.shape.PathElement> smoothedLineElements = new java.util.ArrayList<>();

        // Smooth the line first and store the elements
        linePaths.forEach(node -> {
            if (node instanceof javafx.scene.shape.Path) {
                javafx.scene.shape.Path path = (javafx.scene.shape.Path) node;
                smoothPath(path, false);
                // Copy the smoothed elements
                smoothedLineElements.addAll(new java.util.ArrayList<>(path.getElements()));
            }
        });

        // Apply the same smooth curve to the fill area
        fillPaths.forEach(node -> {
            if (node instanceof javafx.scene.shape.Path && !smoothedLineElements.isEmpty()) {
                javafx.scene.shape.Path fillPath = (javafx.scene.shape.Path) node;
                var originalFillElements = new java.util.ArrayList<>(fillPath.getElements());

                // Find the baseline (bottom of chart) from original fill path
                double baselineY = 0;
                double startX = 0;
                double endX = 0;

                for (var element : originalFillElements) {
                    if (element instanceof javafx.scene.shape.LineTo) {
                        javafx.scene.shape.LineTo lt = (javafx.scene.shape.LineTo) element;
                        baselineY = Math.max(baselineY, lt.getY());
                    }
                }

                // Get start and end X coordinates from smoothed line
                if (smoothedLineElements.get(0) instanceof javafx.scene.shape.MoveTo) {
                    javafx.scene.shape.MoveTo firstMove = (javafx.scene.shape.MoveTo) smoothedLineElements.get(0);
                    startX = firstMove.getX();
                }

                var lastElement = smoothedLineElements.get(smoothedLineElements.size() - 1);
                if (lastElement instanceof javafx.scene.shape.CubicCurveTo) {
                    javafx.scene.shape.CubicCurveTo lastCurve = (javafx.scene.shape.CubicCurveTo) lastElement;
                    endX = lastCurve.getX();
                }

                // Clear and rebuild: smooth line + close to baseline
                fillPath.getElements().clear();

                // Add the smoothed line elements
                for (var element : smoothedLineElements) {
                    if (element instanceof javafx.scene.shape.MoveTo) {
                        javafx.scene.shape.MoveTo mt = (javafx.scene.shape.MoveTo) element;
                        fillPath.getElements().add(new javafx.scene.shape.MoveTo(mt.getX(), mt.getY()));
                    } else if (element instanceof javafx.scene.shape.CubicCurveTo) {
                        javafx.scene.shape.CubicCurveTo cc = (javafx.scene.shape.CubicCurveTo) element;
                        fillPath.getElements().add(new javafx.scene.shape.CubicCurveTo(
                                cc.getControlX1(), cc.getControlY1(),
                                cc.getControlX2(), cc.getControlY2(),
                                cc.getX(), cc.getY()
                        ));
                    }
                }

                // Close the path to baseline
                fillPath.getElements().add(new javafx.scene.shape.LineTo(endX, baselineY));
                fillPath.getElements().add(new javafx.scene.shape.LineTo(startX, baselineY));
                fillPath.getElements().add(new javafx.scene.shape.ClosePath());
            }
        });

        // Show chart after smoothing is complete
        revenueChart.setOpacity(1);
        
        // Apply wave animation (drawing effect from left to right) to both line and fill
        applyWaveAnimation(linePaths, fillPaths);
    }

    private void smoothPath(javafx.scene.shape.Path path, boolean isFillArea) {
        var elements = path.getElements();
        if (elements.size() < 3)
            return;

        // Extract points from path
        java.util.List<Double> xPoints = new java.util.ArrayList<>();
        java.util.List<Double> yPoints = new java.util.ArrayList<>();

        for (var element : elements) {
            if (element instanceof javafx.scene.shape.MoveTo) {
                javafx.scene.shape.MoveTo moveTo = (javafx.scene.shape.MoveTo) element;
                xPoints.add(moveTo.getX());
                yPoints.add(moveTo.getY());
            } else if (element instanceof javafx.scene.shape.LineTo) {
                javafx.scene.shape.LineTo lineTo = (javafx.scene.shape.LineTo) element;
                xPoints.add(lineTo.getX());
                yPoints.add(lineTo.getY());
            }
        }

        if (xPoints.size() < 3)
            return;

        // Find the baseline (bottom of chart) for clamping
        double baseline = yPoints.stream().mapToDouble(Double::doubleValue).max().orElse(0);

        // Clear existing elements and rebuild with smooth curves
        elements.clear();

        // Start at first point
        elements.add(new javafx.scene.shape.MoveTo(xPoints.get(0), yPoints.get(0)));

        // Create smooth Catmull-Rom spline curves between points
        for (int i = 0; i < xPoints.size() - 1; i++) {
            double x0 = i > 0 ? xPoints.get(i - 1) : xPoints.get(i);
            double y0 = i > 0 ? yPoints.get(i - 1) : yPoints.get(i);
            double x1 = xPoints.get(i);
            double y1 = yPoints.get(i);
            double x2 = xPoints.get(i + 1);
            double y2 = yPoints.get(i + 1);
            double x3 = i < xPoints.size() - 2 ? xPoints.get(i + 2) : x2;
            double y3 = i < xPoints.size() - 2 ? yPoints.get(i + 2) : y2;

            // Calculate control points for cubic Bezier curve
            double cp1x = x1 + (x2 - x0) / 6.0;
            double cp1y = y1 + (y2 - y0) / 6.0;
            double cp2x = x2 - (x3 - x1) / 6.0;
            double cp2y = y2 - (y3 - y1) / 6.0;

            // Strict clamping to prevent overshooting
            // Control points must stay between the two data points
            double minY = Math.min(y1, y2);
            double maxY = Math.max(y1, y2);

            // Clamp control points strictly within the range of the two endpoints
            cp1y = Math.max(minY, Math.min(cp1y, maxY));
            cp2y = Math.max(minY, Math.min(cp2y, maxY));

            // Also ensure they don't go below baseline
            cp1y = Math.min(cp1y, baseline);
            cp2y = Math.min(cp2y, baseline);

            elements.add(new javafx.scene.shape.CubicCurveTo(cp1x, cp1y, cp2x, cp2y, x2, y2));
        }
    }
    
    // Apply wave animation (drawing effect from left to right)
    private void applyWaveAnimation(java.util.Set<javafx.scene.Node> linePaths, java.util.Set<javafx.scene.Node> fillPaths) {
        // Get the chart bounds for clipping
        double chartWidth = revenueChart.getWidth();
        double chartHeight = revenueChart.getHeight();
        
        // Create a rectangle for clipping animation
        javafx.scene.shape.Rectangle clipRect = new javafx.scene.shape.Rectangle(0, 0, 0, chartHeight);
        
        // Apply clip to both line and fill paths
        linePaths.forEach(node -> {
            if (node instanceof javafx.scene.shape.Path) {
                javafx.scene.shape.Path path = (javafx.scene.shape.Path) node;
                path.setClip(clipRect);
            }
        });
        
        fillPaths.forEach(node -> {
            if (node instanceof javafx.scene.shape.Path) {
                javafx.scene.shape.Path path = (javafx.scene.shape.Path) node;
                // Create a separate clip rectangle for fill (they need independent clips)
                javafx.scene.shape.Rectangle fillClip = new javafx.scene.shape.Rectangle(0, 0, 0, chartHeight);
                path.setClip(fillClip);
                
                // Bind fill clip width to line clip width for synchronized animation
                fillClip.widthProperty().bind(clipRect.widthProperty());
            }
        });
        
        // Create timeline animation for the clip rectangle
        javafx.animation.Timeline timeline = new javafx.animation.Timeline();
        javafx.animation.KeyFrame keyFrame = new javafx.animation.KeyFrame(
            javafx.util.Duration.millis(2000), // Animation duration: 2 seconds
            new javafx.animation.KeyValue(
                clipRect.widthProperty(), 
                chartWidth, 
                javafx.animation.Interpolator.EASE_OUT
            )
        );
        timeline.getKeyFrames().add(keyFrame);
        
        // Remove clips after animation completes
        timeline.setOnFinished(e -> {
            linePaths.forEach(node -> {
                if (node instanceof javafx.scene.shape.Path) {
                    ((javafx.scene.shape.Path) node).setClip(null);
                }
            });
            fillPaths.forEach(node -> {
                if (node instanceof javafx.scene.shape.Path) {
                    ((javafx.scene.shape.Path) node).setClip(null);
                }
            });
        });
        
        timeline.play();
    }

}
