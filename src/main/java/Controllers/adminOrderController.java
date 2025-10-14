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
import javafx.scene.layout.HBox;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
    private ComboBox<String> dateBox;
    @FXML
    private Label monthlbl;
    @FXML
    private Label yearlbl;
    @FXML
    private Button monthPrev, monthNext, yearPrev, yearNext, switchbtn;
    @FXML
    private HBox dateContainer;

    private int currentMonth = LocalDate.now().getMonthValue();
    private int currentYear = LocalDate.now().getYear();
    private final String[] months = {
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };
    private boolean showingCombo = true;

    private ObservableList<orderView> list = FXCollections.observableArrayList();

    @FXML
    void clickSwitch(ActionEvent event) {
        showingCombo = !showingCombo;
        dateBox.setVisible(showingCombo);
        dateContainer.setVisible(!showingCombo);

        if (showingCombo) {
            loadOrdersByBox();
        } else {
            updateMonthYearLabels();
            loadOrdersByMonthYear(currentMonth, currentYear);
        }
    }

    @FXML
    void clickMonthNext(ActionEvent event) {
        adjustMonth(1);
    }

    @FXML
    void clickMonthPrev(ActionEvent event) {
        adjustMonth(-1);
    }

    @FXML
    void clickYearNext(ActionEvent event) {
        adjustYear(1);
    }

    @FXML
    void clickYearPrev(ActionEvent event) {
        adjustYear(-1);
    }

    private void adjustMonth(int delta) {
        currentMonth += delta;
        if (currentMonth > 12) {
            currentMonth = 1;
            currentYear++;
        }
        if (currentMonth < 1) {
            currentMonth = 12;
            currentYear--;
        }
        updateMonthYearLabels();
        loadOrdersByMonthYear(currentMonth, currentYear);
    }

    private void adjustYear(int delta) {
        currentYear += delta;
        updateMonthYearLabels();
        loadOrdersByMonthYear(currentMonth, currentYear);
    }

    private void updateMonthYearLabels() {
        LocalDate now = LocalDate.now();
        yearlbl.setText(String.valueOf(currentYear));

        if (currentYear >= now.getYear() && currentMonth > now.getMonthValue()) {
            currentMonth = now.getMonthValue();
        }

        monthlbl.setText(months[currentMonth - 1]);

        // Hide future buttons if necessary
        monthNext.setVisible(!(currentYear == now.getYear() && currentMonth == now.getMonthValue()));
        yearNext.setVisible(currentYear < now.getYear());
    }

    private void loadOrdersByBox() {
        loadOrders("call orders_general_view_date(?)", dateBox.getValue());
    }

    private void loadOrdersByMonthYear(int month, int year) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1);
        loadOrders("SELECT o.order_id, c.customer_name, o.order_date, o.order_status, SUM(d.total_price) AS total_price " +
                        "FROM orders o JOIN customer_info c ON o.customer_id = c.customer_id " +
                        "JOIN order_details d ON o.order_id = d.order_id " +
                        "WHERE o.order_date >= ? AND o.order_date < ? " +
                        "GROUP BY o.order_id, c.customer_name, o.order_date, o.order_status",
                start.toString(), end.toString());
    }

    private void loadOrders(String query, String... params) {
        table.setPlaceholder(new Label("Loading..."));

        Task<ObservableList<orderView>> task = new Task<>() {
            @Override
            protected ObservableList<orderView> call() throws Exception {
                ObservableList<orderView> tempList = FXCollections.observableArrayList();
                Porsche_DB connect = new Porsche_DB();
                Connection con = connect.connect();

                PreparedStatement ps;
                if (query.startsWith("call")) {
                    ps = con.prepareCall(query);
                    ps.setString(1, params[0]);
                } else {
                    ps = con.prepareStatement(query);
                    ps.setString(1, params[0]);
                    ps.setString(2, params[1]);
                }

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
            ObservableList<orderView> data = task.getValue();
            table.setItems(data);
            table.setPlaceholder(data.isEmpty() ? new Label("No orders found for this selection") : new Label(""));
        });

        task.setOnFailed(e -> task.getException().printStackTrace());

        new Thread(task).start();
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
        dateContainer.setVisible(false);

        if (currentYear == LocalDate.now().getYear())
            yearNext.setVisible(false);
        if (currentMonth == LocalDate.now().getMonthValue() && currentYear == LocalDate.now().getYear())
            monthNext.setVisible(false);

        orders_col.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getOrderId()));
        customer_col.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCustomername()));
        date_col.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDate().toString()));
        status_col.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        total_col.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTotal()));

        dateBox.setItems(FXCollections.observableArrayList("Today", "This Week", "This Month"));
        dateBox.getSelectionModel().selectFirst();
        loadOrdersByBox();

        dateBox.setOnAction(e -> loadOrdersByBox());
    }
}
