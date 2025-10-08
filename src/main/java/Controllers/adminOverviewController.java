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
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;

public class adminOverviewController implements Initializable {

    @FXML
    private Label averageLbl, totalSalesLbl, partsSoldLbl, servicesLbl, revenueLbl;
    @FXML
    private Label totalSalesValueLbl, partsValueLbl, servicesValueLbl, revenueGrowthLbl;
    @FXML
    private Button averagebtn, managebtn;
    @FXML
    private VBox notiPanel;
    @FXML
    private AreaChart<String, Number> areaChart;
    @FXML
    private BarChart<String, Number> barChart;
    @FXML
    private PieChart pieCar, piePart;
    @FXML
    private ChoiceBox<String> monthBox;
    @FXML
    private ChoiceBox<Integer> yearBox;
    @FXML
    private Button PreviousMonthbtn, NextMonthbtn, PreviousYearbth, NextYearbtn;

    private LocalDate today = LocalDate.now();
    private int currentMonth;
    private int currentYear;
    private boolean updatingMonthBox = false;

    private Porsche_DB connect;
    private Connection con;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            connect = new Porsche_DB();
            con = connect.connect();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        currentMonth = today.getMonthValue();
        currentYear = today.getYear();

        setupMonthYearBoxes();
        updateYearMonthLabel();

        loadCharts();
    }

    private void setupMonthYearBoxes() {
        // YearBox: last 5 years for example
        List<Integer> years = new ArrayList<>();
        for (int y = currentYear - 5; y <= currentYear; y++) {
            years.add(y);
        }
        yearBox.setItems(FXCollections.observableArrayList(years));
        yearBox.setValue(currentYear);

        // MonthBox
        updateMonthBoxForYear(currentYear);

        yearBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                currentYear = newVal;
                updateMonthBoxForYear(currentYear);
                updateYearMonthLabel();
            }
        });

        monthBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (!updatingMonthBox && newVal != null) {
                Month selectedMonth = Month.valueOf(newVal.toUpperCase());
                currentMonth = selectedMonth.getValue();
                updateYearMonthLabel();
            }
        });
    }

    private void updateMonthBoxForYear(int year) {
        int startMonth = 1;
        int endMonth = 12;
        if (year == today.getYear()) endMonth = today.getMonthValue();

        List<String> months = new ArrayList<>();
        for (int m = startMonth; m <= endMonth; m++) {
            months.add(Month.of(m).name());
        }

        updatingMonthBox = true;
        monthBox.setItems(FXCollections.observableArrayList(months));
        if (!months.contains(Month.of(currentMonth).name())) {
            currentMonth = startMonth;
        }
        monthBox.setValue(Month.of(currentMonth).name());
        updatingMonthBox = false;
    }

    private void updateYearMonthLabel() {
        // Disable future buttons
        int curMonth = today.getMonthValue();
        int curYear = today.getYear();
        NextYearbtn.setDisable(currentYear >= curYear);
        NextMonthbtn.setDisable(currentYear > curYear || (currentYear == curYear && currentMonth >= curMonth));
    }

    private void loadCharts() {
        try {
            loadPieChart(pieCar, "cars");
            loadPieChart(piePart, "parts");
            loadBarChart();
            loadAreaChart("365days", "", "");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadPieChart(PieChart chart, String type) throws SQLException {
        if (chart == null) return;
        ChartDAO dao = new ChartDAO(con);
        List<overviewPie> data = dao.getOverviewPie(type);
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        for (overviewPie row : data) {
            pieData.add(new PieChart.Data(row.getLabel(), row.getTotalSale()));
        }
        chart.setData(pieData);
    }

    private void loadBarChart() throws SQLException {
        if (barChart == null) return;
        ChartDAO dao = new ChartDAO(con);
        List<overviewBar> data = dao.getOverviewBar(currentYear, "qty");

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

    private void loadAreaChart(String choice, String start, String end) {
        if (areaChart == null) return;
        try {
            ChartDAO dao = new ChartDAO(con);
            List<overviewLine> data = dao.getOverviewLine(choice, start, end);
            areaChart.getData().clear();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Total Revenue");

            for (overviewLine row : data) {
                series.getData().add(new XYChart.Data<>(row.getDayLabel(), row.getTotalSales()));
            }

            areaChart.getData().add(series);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ---------- Navigation buttons ----------
    @FXML
    void clickNextMonth(ActionEvent event) {
        currentMonth++;
        if (currentMonth > 12) {
            currentMonth = 1;
            currentYear++;
        }
        updateYearMonthLabel();
    }

    @FXML
    void clickPreviousMonth(ActionEvent event) {
        currentMonth--;
        if (currentMonth < 1) {
            currentMonth = 12;
            currentYear--;
        }
        updateYearMonthLabel();
    }

    @FXML
    void clickNextYear(ActionEvent event) {
        currentYear++;
        updateYearMonthLabel();
    }

    @FXML
    void clickPreviousYear(ActionEvent event) {
        currentYear--;
        updateYearMonthLabel();
    }

    @FXML
    void clickCarbtn(ActionEvent event) {}
    @FXML
    void clickPartbtn(ActionEvent event) {}
}
