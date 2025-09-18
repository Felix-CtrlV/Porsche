package Controllers;

import DAO.ChartDAO;
import Database.Porsche_DB;
import Model.overviewBar;
import Model.overviewLine;
import Model.overviewPie;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class overviewController {

    @FXML
    private Label averageLbl;

    @FXML
    private Button averagebtn;

    @FXML
    private VBox overview_vbox;

    @FXML
    private StackPane carPane;

    @FXML
    private StackPane partPane;

    @FXML
    private StackPane totalPane;

    @FXML
    private StackPane customizePane;

    @FXML
    private ComboBox lineCombo;

    @FXML
    private Button managebtn;

    @FXML
    private LineChart<String, Number> lineChart;

    @FXML
    private Button previous;

    @FXML
    private Button next;

    @FXML
    private BarChart<String, Number> barChart;

    @FXML
    private PieChart pieChart;

    @FXML
    private Label yearlbl;

    @FXML
    private StackPane pieCars, pieAccessories, pieCustomize;

    @FXML
    private Circle circleCars, circleAccessories, circleCustomize;

    private int currentYear = LocalDate.now().getYear();

    public overviewController() throws SQLException, ClassNotFoundException {
    }

    private void updateYearLabels() {
        yearlbl.setText(String.valueOf(currentYear));
    }


    private void applyFilter(int range) {
        int days = range;
//        lineData(days);
    }

    @FXML
    void clickManage(ActionEvent event) {

    }

    @FXML
    void clickNext(ActionEvent event) throws SQLException {
        currentYear++;
        updateYearLabels();
        if (currentYear == LocalDate.now().getYear()) {
            next.setText("");
            next.setVisible(false);
        }
        BarData();
    }

    @FXML
    void clickPrev(ActionEvent event) throws SQLException {
        currentYear--;
        updateYearLabels();
        next.setText(">");
        next.setVisible(true);
        BarData();
    }

    String choice = "qty";
    String selected, startDate, endDate;

    private void getSelected() {
        Object value = lineCombo.getValue();
        if (value == null) {
            selected = "7days";
            startDate = "";
            endDate = "";
            return;
        }
        String val = value.toString();
        switch (val) {
            case "Last 7 Days" -> { selected = "7days"; startDate = ""; endDate = ""; }
            case "Last 30 Days" -> { selected = "30days"; startDate = ""; endDate = ""; }
            case "Last 365 Days" -> { selected = "365days"; startDate = ""; endDate = ""; }
            default -> selected = "custom";
        }
    }

    boolean current = false;

    @FXML
    void clickAverage(ActionEvent event) throws SQLException {
        if (current) {
            choice = "qty";
            current = false;
            averageLbl.setText("Average Sale (Quantity)");
            BarData();
        } else {
            choice = "price";
            current = true;
            averageLbl.setText("Average Sale (Price)");
            BarData();
        }
    }

    Porsche_DB connect = new Porsche_DB();
    Connection con = connect.connect();

    public void initialize() throws SQLException, ClassNotFoundException {
        lineCombo.getItems().addAll("Last 7 Days", "Last 30 Days", "Last 365 Days", "Custom");
        lineCombo.getSelectionModel().selectFirst();
        getSelected();
        BarData();
        LineData(selected, startDate, endDate);

        pieCars.setOnMouseClicked(e->{
            category = "cars";
            try {
                pieData();
            } catch (
                    SQLException ex) {
                throw new RuntimeException(ex);
            }
            circleCars.setStyle("-fx-fill: #C21E2B;");
            circleAccessories.setStyle("-fx-fill: black");
            circleCustomize.setStyle("-fx-fill: black");
        });
        pieAccessories.setOnMouseClicked(e->{
            category = "parts";
            try {
                pieData();
            } catch (
                    SQLException ex) {
                throw new RuntimeException(ex);
            }
            circleCars.setStyle("-fx-fill: black;");
            circleAccessories.setStyle("-fx-fill: #C21E2B;");
            circleCustomize.setStyle("-fx-fill: black;");
        });
        pieCustomize.setOnMouseClicked(e->{
            category = "customize";
            try {
                pieData();
            } catch (
                    SQLException ex) {
                throw new RuntimeException(ex);
            }
            circleCars.setStyle("-fx-fill: black");
            circleAccessories.setStyle("-fx-fill: black");
            circleCustomize.setStyle("-fx-fill: #C21E2B;");
        });

        lineCombo.setOnAction(e->{
            getSelected();
            LineData(selected, startDate, endDate);
        });
        pieData();
        updateYearLabels();
        if (currentYear == LocalDate.now().getYear()) {
            next.setText("");
            next.setVisible(false);
        }
    }

    String category = "cars";

    private void pieData() throws SQLException {

        ChartDAO chartDAO = new ChartDAO(con);
        List<overviewPie> data = chartDAO.getOverviewPie(category);
        pieChart.getData().clear();
        ObservableList<PieChart.Data> pie = FXCollections.observableArrayList();
        for (overviewPie row : data) {
            pie.add(new PieChart.Data(row.getLabel(), row.getTotalSale()));
        }
        pieChart.setData(pie);
    }

    private void BarData() throws SQLException {
        ChartDAO chartDAO = new ChartDAO(con);
        List<overviewBar> data = chartDAO.getOverviewBar(Integer.parseInt(yearlbl.getText()), choice);

        barChart.getData().clear();

        XYChart.Series<String, Number> seriesCars = new XYChart.Series<>();
        seriesCars.setName("Cars");

        XYChart.Series<String, Number> seriesParts = new XYChart.Series<>();
        seriesParts.setName("Parts");

        for (overviewBar row : data) {
            seriesCars.getData().add(new XYChart.Data<>(row.getMonth(), row.getCarValue()));
            seriesParts.getData().add(new XYChart.Data<>(row.getMonth(), row.getPartValue()));
        }
        barChart.getData().addAll(seriesCars, seriesParts);
    }

    public void LineData(String choice, String start, String end) {
        try {
            ChartDAO chartDAO = new ChartDAO(con);
            List<overviewLine> data = chartDAO.getOverviewLine(choice, start, end);

            lineChart.getData().clear();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Total Revenue");

            for (overviewLine row : data) {
                series.getData().add(new XYChart.Data<>(row.getDayLabel(), row.getTotalSales()));
            }

            lineChart.getData().add(series);

        } catch (
                SQLException e) {
            e.printStackTrace();
        }
    }

}
