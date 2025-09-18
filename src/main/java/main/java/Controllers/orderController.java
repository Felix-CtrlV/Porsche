package main.java.Controllers;

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

public class orderController {

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
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    private ObservableList<orderView> list = FXCollections.observableArrayList();

    boolean showingCombo = true;

    @FXML
    void clickSwitch(ActionEvent event) {
        showingCombo = !showingCombo;
        dateBox.setVisible(showingCombo);
        dateContainer.setVisible(!showingCombo);

        if (showingCombo) {
            loadOrderBox();
        } else {
            updateMonthYearLabels();
            loadOrderByCustomDate(currentMonth, currentYear);
        }
    }

    @FXML
    void clickMonthNext(ActionEvent event) {
        currentMonth++;
        if (currentMonth > 12) {
            currentMonth = 1;
            currentYear++;
            if (currentYear == LocalDate.now().getYear()) {
                yearNext.setVisible(false);
            }
        }
        updateMonthYearLabels();
        loadOrderByCustomDate(currentMonth, currentYear);
        if (currentMonth == LocalDate.now().getMonthValue() && currentYear == LocalDate.now().getYear()) {
            monthNext.setVisible(false);
        }
    }

    @FXML
    void clickMonthPrev(ActionEvent event) {
        currentMonth--;
        if (currentMonth < 1) {
            currentMonth = 12;
            currentYear--;
            yearNext.setVisible(true);
        }
        updateMonthYearLabels();
        loadOrderByCustomDate(currentMonth, currentYear);
        monthNext.setVisible(true);
    }

    @FXML
    void clickYearNext(ActionEvent event) {
        currentYear++;
        updateMonthYearLabels();
        loadOrderByCustomDate(currentMonth, currentYear);
        if (currentYear == LocalDate.now().getYear()) {
            yearNext.setVisible(false);
            if (currentMonth == LocalDate.now().getMonthValue()) {
                monthNext.setVisible(false);

            }
        }
    }

    @FXML
    void clickYearPrev(ActionEvent event) {
        currentYear--;
        updateMonthYearLabels();
        loadOrderByCustomDate(currentMonth, currentYear);
        yearNext.setVisible(true);
        monthNext.setVisible(true);
    }

    private void updateMonthYearLabels() {
        boolean flag = false;
        yearlbl.setText(String.valueOf(currentYear));
        if (currentYear == LocalDate.now().getYear()) {
            while (flag == false) {
                if (currentMonth > LocalDate.now().getMonthValue()) {
                    currentMonth--;
                }else{
                    flag = true;
                }
            }
        }
        monthlbl.setText(months[currentMonth - 1]);
    }

    private void loadOrderBox() {
        table.setPlaceholder(new Label("Loading..."));
        Task<ObservableList<orderView>> task = new Task<>() {
            @Override
            protected ObservableList<orderView> call() throws Exception {
                ObservableList<orderView> tempList = FXCollections.observableArrayList();

                Porsche_DB connect = new Porsche_DB();
                Connection con = connect.connect();

                CallableStatement cs = con.prepareCall("call orders_general_view_date(?)");
                cs.setString(1, dateBox.getValue());
                ResultSet rs = cs.executeQuery();

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d HH:mm:ss");
                while (rs.next()) {
                    int orderid = rs.getInt(1);
                    String customername = rs.getString(2);
                    String orderdate = rs.getString(3);
                    String status = rs.getString(4);
                    String total = "$" + rs.getString(5);

                    orderView orderview = new orderView(orderid, customername, LocalDateTime.parse(orderdate, formatter), status, total);
                    tempList.add(orderview);
                }
                connect.disconnect();
                return tempList;
            }
        };
        task.setOnSucceeded(e -> {
            ObservableList<orderView> data = task.getValue();
            table.setItems(data);
            if (data.isEmpty()) {
                table.setPlaceholder(new Label("No orders found for this selection"));
            } else {
                table.setPlaceholder(new Label(""));
            }
        });
        task.setOnFailed(e -> task.getException().printStackTrace());
        new Thread(task).start();
    }

    private void loadOrderByCustomDate(int month, int year) {
        table.setPlaceholder(new Label("Loading..."));
        Task<ObservableList<orderView>> task = new Task<>() {
            @Override
            protected ObservableList<orderView> call() throws Exception {
                ObservableList<orderView> tempList = FXCollections.observableArrayList();

                Porsche_DB connect = new Porsche_DB();
                Connection con = connect.connect();

                LocalDate start = LocalDate.of(year, month, 1);
                LocalDate end = start.plusMonths(1);

                PreparedStatement p = con.prepareStatement(
                        "SELECT o.order_id, c.customer_name, o.order_date, o.order_status, SUM(d.total_price) AS total_price " +
                                "FROM orders o JOIN customer_info c ON o.customer_id = c.customer_id " +
                                "JOIN order_details d ON o.order_id = d.order_id " +
                                "WHERE o.order_date >= ? AND o.order_date < ? " +
                                "GROUP BY o.order_id, c.customer_name, o.order_date, o.order_status"
                );

                p.setString(1, start.toString());
                p.setString(2, end.toString());
                ResultSet rs = p.executeQuery();

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d HH:mm:ss");
                while (rs.next()) {
                    int orderid = rs.getInt(1);
                    String customername = rs.getString(2);
                    String orderdate = rs.getString(3);
                    String status = rs.getString(4);
                    String total = "$" + rs.getString(5);

                    orderView orderview = new orderView(orderid, customername,
                            LocalDateTime.parse(orderdate, formatter),
                            status, total);
                    tempList.add(orderview);
                }

                connect.disconnect();
                return tempList;
            }
        };
        task.setOnSucceeded(e -> {
            ObservableList<orderView> data = task.getValue();
            table.setItems(data);
            if (data.isEmpty()) {
                table.setPlaceholder(new Label("No orders found for this selection"));
            } else {
                table.setPlaceholder(new Label(""));
            }
        });

        task.setOnFailed(e -> task.getException().printStackTrace());
        new Thread(task).start();
    }

    public void initialize() {
        dateContainer.setVisible(false);
        if (currentYear == LocalDate.now().getYear()) {
            yearNext.setVisible(false);
        }
        if (currentMonth == LocalDate.now().getMonthValue() && currentYear == LocalDate.now().getYear()) {
            monthNext.setVisible(false);
        }


        orders_col.setCellValueFactory(d -> new ReadOnlyObjectWrapper<>(d.getValue().getOrderId()));
        customer_col.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCustomername()));
        date_col.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDate().toString()));
        status_col.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus()));
        total_col.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTotal()));

        dateBox.setItems(FXCollections.observableArrayList("Today", "This Week", "This Month"));
        dateBox.getSelectionModel().selectFirst();

        loadOrderBox();


        dateBox.setOnAction(e -> loadOrderBox());
    }
}
