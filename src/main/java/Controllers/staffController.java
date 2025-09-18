package Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

public class staffController {

    @FXML
    private ComboBox<String> combostaff;

    @FXML
    private TableView<User> tbluserinfo;

    @FXML
    private TableColumn<User, Integer> colid;

    @FXML
    private TableColumn<User, String> colname;

    @FXML
    private TableColumn<User, String> colemail;

    @FXML
    private TableColumn<User, String> colnrc;

    @FXML
    private TableColumn<User, String> colpassword;

    @FXML
    private TableColumn<User, String> coladdress;

    @FXML
    private TableColumn<User, String> colphone;

    @FXML
    private TableColumn<User, String> colrole;

    @FXML
    private TableColumn<User, String> colactive;

    @FXML
    private AnchorPane mainContentPane;

    private ObservableList<User> userData = FXCollections.observableArrayList();
    private ObservableList<String> staffNames = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Set up the table columns
        setupTableColumns();

        // Load manual data
        loadManualData();

        // Set up the combo box
        combostaff.setItems(staffNames);

        // Set data to table
        tbluserinfo.setItems(userData);

        // Set up responsive layout after the scene is available
        setupResponsiveLayoutAfterSceneIsSet();
    }

    // This method will be called when the scene is set
    private void setupResponsiveLayoutAfterSceneIsSet() {
        // Use a listener to wait for the scene to be set
        mainContentPane.sceneProperty().addListener((observable, oldValue, newScene) -> {
            if (newScene != null) {
                // Bind table width to scene width minus padding
                tbluserinfo.prefWidthProperty().bind(newScene.widthProperty().subtract(28));

                // Make columns resizable with percentage-based widths
                colid.prefWidthProperty().bind(tbluserinfo.widthProperty().multiply(0.06));
                colname.prefWidthProperty().bind(tbluserinfo.widthProperty().multiply(0.10));
                colemail.prefWidthProperty().bind(tbluserinfo.widthProperty().multiply(0.15));
                colnrc.prefWidthProperty().bind(tbluserinfo.widthProperty().multiply(0.08));
                colpassword.prefWidthProperty().bind(tbluserinfo.widthProperty().multiply(0.08));
                coladdress.prefWidthProperty().bind(tbluserinfo.widthProperty().multiply(0.12));
                colphone.prefWidthProperty().bind(tbluserinfo.widthProperty().multiply(0.08));
                colrole.prefWidthProperty().bind(tbluserinfo.widthProperty().multiply(0.07));
                colactive.prefWidthProperty().bind(tbluserinfo.widthProperty().multiply(0.07));
            }
        });
    }

    private void setupTableColumns() {
        colid.setCellValueFactory(new PropertyValueFactory<>("id"));
        colname.setCellValueFactory(new PropertyValueFactory<>("name"));
        colemail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colnrc.setCellValueFactory(new PropertyValueFactory<>("nrc"));
        colpassword.setCellValueFactory(new PropertyValueFactory<>("password"));
        coladdress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colphone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colrole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colactive.setCellValueFactory(new PropertyValueFactory<>("activeStatus"));
    }

    private void loadManualData() {
        // Manual data
        userData.addAll(
                new User(1, "Seller One", "seller1@gmail.com", "12/ABC123456", "pass123",
                        "Yangon", "091234567", "Seller", true),
                new User(2, "Seller Two", "seller2@gmail.com", "13/DEF654321", "pass456",
                        "Mandalay", "092345678", "Seller", true),
                new User(3, "Seller Three", "seller3@gmail.com", "14/GHI789012", "pass789",
                        "Bago", "093456789", "Seller", true)
        );

        for (User user : userData) {
            staffNames.add(user.getName());
        }
    }

    @FXML
    void selectstaff() {
        String selectedStaffName = combostaff.getSelectionModel().getSelectedItem();

        if (selectedStaffName != null) {
            // Find the selected user
            User selectedUser = null;
            for (User user : userData) {
                if (user.getName().equals(selectedStaffName)) {
                    selectedUser = user;
                    break;
                }
            }

            if (selectedUser != null) {
                // Hide user table and show order management
                tbluserinfo.setVisible(false);
                showOrderManagementUI(selectedUser);
            }
        }
    }

    private void showOrderManagementUI(User selectedUser) {
        mainContentPane.getChildren().clear();
        mainContentPane.setVisible(true);

        VBox mainContainer = new VBox(15);
        mainContainer.setLayoutX(20);
        mainContainer.setLayoutY(20);

        // Bind to mainContentPane width with padding
        mainContainer.prefWidthProperty().bind(mainContentPane.widthProperty().subtract(40));
        mainContainer.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 10;");

        Label titleLabel = new Label("Order Management for: " + selectedUser.getName() + " (ID: " + selectedUser.getId() + ")");
        titleLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 0 0 10 0;");

        // Order ComboBox with order IDs
        HBox comboBoxContainer = new HBox(10);
        comboBoxContainer.setStyle("-fx-background-color: #e8f4f8; -fx-padding: 10; -fx-background-radius: 8;");

        Label selectOrderLabel = new Label("Select Order:");
        selectOrderLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        ComboBox<String> orderComboBox = new ComboBox<>();
        orderComboBox.setPromptText("Choose an order");
        orderComboBox.setPrefWidth(250);
        orderComboBox.setStyle("-fx-background-color: white; -fx-border-color: #3498db; -fx-border-radius: 5; -fx-font-size: 14;");

        comboBoxContainer.getChildren().addAll(selectOrderLabel, orderComboBox);

        // Container for order table and customer info
        HBox contentContainer = new HBox(20);
        contentContainer.prefWidthProperty().bind(mainContainer.widthProperty());
        contentContainer.setPrefHeight(350);

        // Order Table
        TableView<Order> orderTable = new TableView<>();
        orderTable.prefWidthProperty().bind(contentContainer.widthProperty().multiply(0.65));
        orderTable.setPrefHeight(350);
        orderTable.setStyle("-fx-background-radius: 8; -fx-border-radius: 8;");

        // Order Table Columns with adjusted widths
        TableColumn<Order, Integer> orderIdCol = new TableColumn<>("Order ID");
        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        orderIdCol.prefWidthProperty().bind(orderTable.widthProperty().multiply(0.15));
        orderIdCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Order, Integer> userIdCol = new TableColumn<>("User ID");
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
        userIdCol.prefWidthProperty().bind(orderTable.widthProperty().multiply(0.12));
        userIdCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Order, Integer> customerIdCol = new TableColumn<>("Customer ID");
        customerIdCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        customerIdCol.prefWidthProperty().bind(orderTable.widthProperty().multiply(0.15));
        customerIdCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Order, String> orderDateCol = new TableColumn<>("Order Date");
        orderDateCol.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        orderDateCol.prefWidthProperty().bind(orderTable.widthProperty().multiply(0.20));
        orderDateCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Order, String> orderStatusCol = new TableColumn<>("Status");
        orderStatusCol.setCellValueFactory(new PropertyValueFactory<>("orderStatus"));
        orderStatusCol.prefWidthProperty().bind(orderTable.widthProperty().multiply(0.15));
        orderStatusCol.setStyle("-fx-alignment: CENTER;");

        orderTable.getColumns().addAll(orderIdCol, userIdCol, customerIdCol, orderDateCol, orderStatusCol);

        // Sample order data
        ObservableList<Order> orderData = FXCollections.observableArrayList();

        // Add orders based on selected user
        if (selectedUser.getId() == 1) {
            orderData.addAll(
                    new Order(1001, 1, 501, "2024-01-15", "Completed"),
                    new Order(1002, 1, 502, "2024-01-16", "Pending"),
                    new Order(1003, 1, 503, "2024-01-17", "Completed")
            );
        } else if (selectedUser.getId() == 2) {
            orderData.addAll(
                    new Order(2001, 2, 601, "2024-01-18", "Pending"),
                    new Order(2002, 2, 602, "2024-01-19", "Completed")
            );
        } else if (selectedUser.getId() == 3) {
            orderData.addAll(
                    new Order(3001, 3, 701, "2024-01-20", "Completed"),
                    new Order(3002, 3, 702, "2024-01-21", "Pending"),
                    new Order(3003, 3, 703, "2024-01-22", "Cancelled")
            );
        }

        orderTable.setItems(orderData);

        // Populate order ComboBox
        ObservableList<String> orderIds = FXCollections.observableArrayList();
        for (Order order : orderData) {
            orderIds.add("Order #" + order.getOrderId());
        }
        orderComboBox.setItems(orderIds);

        // Customer Info Panel (initially empty)
        VBox customerInfoPanel = new VBox(15);
        customerInfoPanel.prefWidthProperty().bind(contentContainer.widthProperty().multiply(0.33));
        customerInfoPanel.setPrefHeight(350);
        customerInfoPanel.setStyle("-fx-background-color: linear-gradient(to bottom, #e8f4f8, #d4e6f1); -fx-padding: 20; -fx-background-radius: 10; -fx-border-color: #aed6f1; -fx-border-width: 2; -fx-border-radius: 10;");

        Label customerTitle = new Label("Customer Information");
        customerTitle.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 0 0 15 0;");

        // Customer details labels
        VBox customerDetails = new VBox(12);
        customerDetails.setStyle("-fx-background-color: rgba(255, 255, 255, 0.7); -fx-padding: 15; -fx-background-radius: 8;");

        Label customerIdLabel = createDetailLabel("Customer ID: ");
        Label customerNameLabel = createDetailLabel("Name: ");
        Label customerNrcLabel = createDetailLabel("NRC: ");
        Label customerPhoneLabel = createDetailLabel("Phone: ");
        Label customerAddressLabel = createDetailLabel("Address: ");
        Label customerCreatedLabel = createDetailLabel("Created At: ");

        // Add event handler for the orderComboBox
        orderComboBox.setOnAction(e -> {
            String selectedOrder = orderComboBox.getSelectionModel().getSelectedItem();
            if (selectedOrder != null) {
                // Extract order ID from the selected string (e.g., "Order #1001" -> 1001)
                int orderId = Integer.parseInt(selectedOrder.replace("Order #", ""));

                // Find the customer for this order
                Customer customer = findCustomerByOrderId(orderId);

                if (customer != null) {
                    // Update customer info panel
                    customerIdLabel.setText("Customer ID: " + customer.getCustomerId());
                    customerNameLabel.setText("Name: " + customer.getName());
                    customerNrcLabel.setText("NRC: " + customer.getNrc());
                    customerPhoneLabel.setText("Phone: " + customer.getPhone());
                    customerAddressLabel.setText("Address: " + customer.getAddress());
                    customerCreatedLabel.setText("Created At: " + customer.getCreatedAt());

                    // Highlight the selected order in the table
                    orderTable.getSelectionModel().clearSelection();
                    for (Order order : orderData) {
                        if (order.getOrderId() == orderId) {
                            orderTable.getSelectionModel().select(order);
                            orderTable.scrollTo(order);
                            break;
                        }
                    }
                }
            }
        });

        // Add components to customer details container
        customerDetails.getChildren().addAll(
                customerIdLabel,
                customerNameLabel,
                customerNrcLabel,
                customerPhoneLabel,
                customerAddressLabel,
                customerCreatedLabel
        );

        // Add components to customer info panel
        customerInfoPanel.getChildren().addAll(customerTitle, customerDetails);

        // Add order table and customer info to content container
        contentContainer.getChildren().addAll(orderTable, customerInfoPanel);

        // Make the content container responsive
        HBox.setHgrow(orderTable, Priority.ALWAYS);
        HBox.setHgrow(customerInfoPanel, Priority.ALWAYS);

        // Add components to main container
        mainContainer.getChildren().addAll(titleLabel, comboBoxContainer, contentContainer);

        // Make the main container responsive
        VBox.setVgrow(contentContainer, Priority.ALWAYS);
        mainContentPane.getChildren().add(mainContainer);
    }

    private Label createDetailLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 14; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");
        return label;
    }

    private Customer findCustomerByOrderId(int orderId) {
        // Sample customer data based on order ID
        if (orderId == 1001) {
            return new Customer(501, "Customer One", "12/ABC123456", "091111111",
                    "Yangon", "2024-01-10 10:30:00");
        } else if (orderId == 1002) {
            return new Customer(502, "Customer Two", "13/DEF654321", "092222222",
                    "Mandalay", "2024-01-11 14:45:00");
        } else if (orderId == 1003) {
            return new Customer(503, "Customer Three", "14/GHI789012", "093333333",
                    "Bago", "2024-01-12 09:15:00");
        } else if (orderId == 2001) {
            return new Customer(601, "Customer Four", "15/JKL345678", "094444444",
                    "Naypyitaw", "2024-01-13 16:20:00");
        } else if (orderId == 2002) {
            return new Customer(602, "Customer Five", "16/MNO901234", "095555555",
                    "Taunggyi", "2024-01-14 11:10:00");
        } else if (orderId == 3001) {
            return new Customer(701, "Customer Six", "17/PQR567890", "096666666",
                    "Mawlamyine", "2024-01-15 13:25:00");
        } else if (orderId == 3002) {
            return new Customer(702, "Customer Seven", "18/STU123456", "097777777",
                    "Pathein", "2024-01-16 08:40:00");
        } else if (orderId == 3003) {
            return new Customer(703, "Customer Eight", "19/VWX789012", "098888888",
                    "Sittwe", "2024-01-17 15:55:00");
        }
        return null;
    }

    // User model class
    public static class User {
        private int id;
        private String name;
        private String email;
        private String nrc;
        private String password;
        private String address;
        private String phone;
        private String role;
        private boolean active;

        public User(int id, String name, String email, String nrc, String password,
                    String address, String phone, String role, boolean active) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.nrc = nrc;
            this.password = password;
            this.address = address;
            this.phone = phone;
            this.role = role;
            this.active = active;
        }

        // Getters
        public int getId() { return id; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getNrc() { return nrc; }
        public String getPassword() { return password; }
        public String getAddress() { return address; }
        public String getPhone() { return phone; }
        public String getRole() { return role; }
        public boolean isActive() { return active; }

        public String getActiveStatus() {
            return active ? "Active" : "Inactive";
        }
    }

    // Order model class
    public static class Order {
        private int orderId;
        private int userId;
        private int customerId;
        private String orderDate;
        private String orderStatus;

        public Order(int orderId, int userId, int customerId, String orderDate, String orderStatus) {
            this.orderId = orderId;
            this.userId = userId;
            this.customerId = customerId;
            this.orderDate = orderDate;
            this.orderStatus = orderStatus;
        }

        // Getters
        public int getOrderId() { return orderId; }
        public int getUserId() { return userId; }
        public int getCustomerId() { return customerId; }
        public String getOrderDate() { return orderDate; }
        public String getOrderStatus() { return orderStatus; }
    }

    // Customer model class
    public static class Customer {
        private int customerId;
        private String name;
        private String nrc;
        private String phone;
        private String address;
        private String createdAt;

        public Customer(int customerId, String name, String nrc, String phone,
                        String address, String createdAt) {
            this.customerId = customerId;
            this.name = name;
            this.nrc = nrc;
            this.phone = phone;
            this.address = address;
            this.createdAt = createdAt;
        }

        // Getters
        public int getCustomerId() { return customerId; }
        public String getName() { return name; }
        public String getNrc() { return nrc; }
        public String getPhone() { return phone; }
        public String getAddress() { return address; }
        public String getCreatedAt() { return createdAt; }
    }
}