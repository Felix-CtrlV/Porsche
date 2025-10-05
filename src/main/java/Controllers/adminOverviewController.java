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
import javafx.scene.layout.VBox;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;

public class adminOverviewController {
    @FXML
    private Label averageLbl, totalSalesLbl, partsSoldLbl, servicesLbl, revenueLbl;
    @FXML
    private Label totalSalesValueLbl, partsValueLbl, servicesValueLbl, revenueGrowthLbl;
    @FXML
    private Button averagebtn;
    @FXML
    private VBox notiPanel;
    @FXML
    private Button managebtn;
    @FXML
    private AreaChart<String, Number> areaChart;
    @FXML
    private Button PreviousMonthbtn, NextMonthbtn, PreviousYearbth, NextYearbtn;
    @FXML
    private BarChart<String, Number> barChart;
    @FXML
    private PieChart pieCar, piePart;
    @FXML
    private ChoiceBox monthBox, yearBox;

    @FXML
    void clickCarbtn(ActionEvent event) {
    }

    private LocalDate today = LocalDate.now();
    private boolean updatingMonthBox = false;
    private int currentMonth;
    private int currentYear;

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
    void clickNextYear(ActionEvent event) {
        currentYear++;
        if (today.getYear() == currentYear) {
            if (currentMonth > today.getMonthValue()) {
                currentMonth = today.getMonthValue();
            }
        }
        updateYearMonthLabel();
    }

    @FXML
    void clickPartbtn(ActionEvent event) {
    }

    @FXML
    void clickPreviousMonth(ActionEvent event) {
        currentMonth--;
        if (currentMonth < 1) {
            currentMonth = 12;
            currentYear--;
        }
        ;
        updateYearMonthLabel();
    }

    @FXML
    void clickPreviousYear(ActionEvent event) {
        currentYear--;
        updateYearMonthLabel();
    }

    private void updateYearMonthLabel() {
        int curyear = today.getYear();
        Month nmonth = Month.of(currentMonth);
        int curmonth = today.getMonthValue();
        if (currentYear >= curyear) {
            NextYearbtn.setDisable(true);
            NextYearbtn.setVisible(false);
            if (currentMonth >= curmonth) {
                NextMonthbtn.setDisable(true);
                NextMonthbtn.setVisible(false);
            } else {
                NextMonthbtn.setDisable(false);
                NextMonthbtn.setVisible(true);
            }
        } else {
            NextYearbtn.setDisable(false);
            NextYearbtn.setVisible(true);
            NextMonthbtn.setDisable(false);
            NextMonthbtn.setVisible(true);
        } // 🔹 Sync ComboBoxes with updated currentMonth/currentYear
        String monthName = nmonth.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        if (monthBox.getItems().contains(monthName)) {
            monthBox.setValue(monthName);
        }
        if (yearBox.getItems().contains(currentYear)) {
            yearBox.setValue(currentYear);
        }
        updateMonthBoxForYear(currentYear);
    }

    private void updateMonthBoxForYear(int year) {
        int startMonth = 1;
        int endMonth = 12;
        if (year == today.getYear()) {
            endMonth = today.getMonthValue();
        }
        List<String> months = new ArrayList<>();
        for (int m = startMonth; m <= endMonth; m++) {
            months.add(Month.of(m).getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
        }
        updatingMonthBox = true;
        monthBox.setItems(FXCollections.observableArrayList(months));
        if (!months.contains(Month.of(currentMonth).getDisplayName(TextStyle.SHORT, Locale.ENGLISH))) {
            currentMonth = startMonth; // default to first available month
        }
        monthBox.setValue(Month.of(currentMonth).getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
        updatingMonthBox = false;
    }

    Porsche_DB connect = new Porsche_DB();
    Connection con = connect.connect();

    public adminOverviewController() throws SQLException, ClassNotFoundException {
    }

    public void initialize() throws SQLException, ClassNotFoundException {
        pieCarData();
        piePartData();
        BarData();
        LineData("365days", "", "");
    }

    private void pieCarData() throws SQLException {
        ChartDAO chartDAO = new ChartDAO(con);
        List<overviewPie> data = chartDAO.getOverviewPie("cars");
        pieCar.getData().clear();
        ObservableList<PieChart.Data> pie = FXCollections.observableArrayList();
        for (overviewPie row : data) {
            pie.add(new PieChart.Data(row.getLabel(), row.getTotalSale()));
        }
        pieCar.setData(pie);
    }

    private void piePartData() throws SQLException {
        ChartDAO chartDAO = new ChartDAO(con);
        List<overviewPie> data = chartDAO.getOverviewPie("parts");
        piePart.getData().clear();
        ObservableList<PieChart.Data> pie = FXCollections.observableArrayList();
        for (overviewPie row : data) {
            pie.add(new PieChart.Data(row.getLabel(), row.getTotalSale()));
        }
        piePart.setData(pie);
    }


    private void BarData() throws SQLException {
        ChartDAO chartDAO = new ChartDAO(con);
        List<overviewBar> data = chartDAO.getOverviewBar(2025, "qty");
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
            areaChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Total Revenue");
            for (overviewLine row : data) {
                series.getData().add(new XYChart.Data<>(row.getDayLabel(), row.getTotalSales()));
            }
            areaChart.getData().add(series);
        } catch (
                SQLException e) {
            e.printStackTrace();
        }
    }
}