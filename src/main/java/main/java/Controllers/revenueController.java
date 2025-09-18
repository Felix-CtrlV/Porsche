package main.java.Controllers;

import main.java.Database.Porsche_DB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

import java.sql.*;

public class revenueController {

    @FXML
    private ComboBox<String> datecombo;

    @FXML
    private StackPane revenuePane;

    @FXML
    private StackedBarChart<String, Integer> barchat;

    @FXML
    private PieChart piechat;

    @FXML
    private LineChart lineChart;

    @FXML
    private ComboBox<String> categoryBox;

    ObservableList<String> title = FXCollections.observableArrayList();
    ObservableList<Integer> count = FXCollections.observableArrayList();

    public void PieLoad() throws SQLException, ClassNotFoundException {

        int cartotal = 0, parttotal = 0, customizetotal = 0;
        Porsche_DB connect = new Porsche_DB();
        Connection con = connect.connect();
        CallableStatement piestatement = con.prepareCall("call pieload(?)");
        piestatement.setString(1, datecombo.getValue().toLowerCase());
        ResultSet rs = piestatement.executeQuery();
        if (rs.next()) {
            cartotal = rs.getInt(1);
            parttotal = rs.getInt(2);
            customizetotal = rs.getInt(3);
        }
        ObservableList<PieChart.Data> piedata = FXCollections.observableArrayList(
                new PieChart.Data("Cars", cartotal),
                new PieChart.Data("Parts", parttotal),
                new PieChart.Data("Customization", customizetotal)
        );
        piechat.setData(piedata);

    }

    public void BarLoad() throws SQLException, ClassNotFoundException {
        XYChart.Series<String, Integer> series = new XYChart.Series<>();
        series.setName("");

        Porsche_DB connect = new Porsche_DB();
        Connection con = connect.connect();
        PreparedStatement chart = con.prepareStatement("SELECT c.car_id, SUM(od.price) AS total_price FROM cars c JOIN order_details od ON c.car_id = od.car_id GROUP BY c.car_id");
        ResultSet rs = chart.executeQuery();
        while (rs.next()) {
            title.add(rs.getString(1));
            count.add(rs.getInt(2));
        }

        for (int i = 0; i < title.size(); i++) {
            series.getData().add(new XYChart.Data<>(title.get(i), count.get(i)));
        }
        barchat.getData().add(series);
        connect.disconnect();
    }

    public void RevenueLoad() throws SQLException, ClassNotFoundException {
        Porsche_DB connect = new Porsche_DB();
        Connection con = connect.connect();
        PreparedStatement revenue = con.prepareStatement("SELECT SUM(price) AS total_revenue FROM order_details;");
        ResultSet rs = revenue.executeQuery();
        if (rs.next()) {
            int total = rs.getInt(1);
            Label revenue_label = new Label("$" + total);
            revenue_label.setFont(new Font("Arial", 25));
            revenuePane.getChildren().add(revenue_label);
        }
    }

    public void initialize() throws SQLException, ClassNotFoundException {

        datecombo.getSelectionModel().selectFirst();
        categoryBox.getSelectionModel().selectFirst();
        PieLoad();
        BarLoad();
        RevenueLoad();

        datecombo.setOnAction(e -> {
            try {
                PieLoad();
                BarLoad();
                RevenueLoad();
            } catch (
                    SQLException |
                    ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        });
    }
}
