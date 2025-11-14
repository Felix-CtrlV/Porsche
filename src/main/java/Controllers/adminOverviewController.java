package Controllers;

import Database.DatabaseConnectionManager;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import Utils.CSSManager;

import java.net.URL;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.*;

public class adminOverviewController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(adminOverviewController.class);

    @FXML
    private Label totalSalesLbl, partsSoldLbl, servicesLbl, revenueLbl;
    @FXML
    private Label totalSalesValueLbl, partsValueLbl, servicesValueLbl, revenueGrowthLbl;
    @FXML
    private Label totalTargetLbl, carTargetProgressLbl, partTargetProgressLbl;
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
    private Button PreviousMonthbtn, NextMonthbtn, PreviousYearbtn, NextYearbtn, carbtn, partbtn;
    @FXML
    private Circle carRevenueCircle, partRevenueCircle;

    private final LocalDate today = LocalDate.now();
    private int currentMonth;
    private int currentYear;
    private boolean updatingDateBox = false;
    private LocalDate overviewStartDate;
    private LocalDate overviewEndDate;
    private static final DateTimeFormatter SHORT_MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);
    private String currentBarChartType = "car";

    int year;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentMonth = today.getMonthValue();
        currentYear = today.getYear();

        // Setup area chart for smooth curves
        if (areaChart != null) {
            areaChart.setCreateSymbols(true);
            areaChart.setLegendVisible(false);
            areaChart.setAnimated(false);
        }

        overviewStartDate = getOverviewEarliestDate();
        overviewEndDate = today;

        setupMonthYear();
        updateYearBox();
        updateMonthBox();
        updateDateControls();

        // Initialize button states - ensure CAR is selected by default
        if (carbtn != null && partbtn != null) {
            // Clear any existing classes first
            carbtn.getStyleClass().removeAll("active");
            partbtn.getStyleClass().removeAll("active");

            // Set CAR as active
            carbtn.getStyleClass().add("active");
            currentBarChartType = "car";

            System.out.println("Initialized with CAR selected, currentBarChartType: " + currentBarChartType);
        }

        loadAllData(true);
        year = yearBox.getValue();
    }

    private void setupMonthYear() {
        monthBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (updatingDateBox || newVal == null)
                return;
            int selectedMonth;
            try {
                selectedMonth = Month.from(SHORT_MONTH_FORMATTER.parse(newVal)).getValue();
            } catch (DateTimeParseException ex) {
                logger.warn("Failed to parse month value: {}", newVal, ex);
                return;
            }
            if (selectedMonth != currentMonth) {
                currentMonth = selectedMonth;
                updateDateControls();
                loadAllData(false);
            }
        });

        yearBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (updatingDateBox || newVal == null)
                return;
            if (newVal != currentYear) {
                currentYear = newVal;
                updateMonthBox();
                updateDateControls();
                loadAllData(true);
            }
        });
    }

    private void updateMonthBox() {
        updatingDateBox = true;

        List<String> months = new ArrayList<>();

        int startMonth = (currentYear == overviewStartDate.getYear()) ? overviewStartDate.getMonthValue() : 1;
        int endMonth = (currentYear == overviewEndDate.getYear()) ? overviewEndDate.getMonthValue() : 12;

        for (int i = startMonth; i <= endMonth; i++)
            months.add(Month.of(i).getDisplayName(TextStyle.SHORT, Locale.ENGLISH));

        monthBox.setItems(FXCollections.observableArrayList(months));

        if (currentMonth < startMonth || currentMonth > endMonth)
            currentMonth = startMonth;

        monthBox.setValue(Month.of(currentMonth).getDisplayName(TextStyle.SHORT, Locale.ENGLISH));

        updatingDateBox = false;
    }

    private void updateYearBox() {
        updatingDateBox = true;

        yearBox.getItems().clear();
        int startYear = overviewStartDate.getYear();
        int endYear = overviewEndDate.getYear();

        for (int y = startYear; y <= endYear; y++)
            yearBox.getItems().add(y);

        if (currentYear < startYear)
            currentYear = startYear;
        if (currentYear > endYear)
            currentYear = endYear;

        yearBox.setValue(currentYear);

        updatingDateBox = false;
    }

    private void updateDateControls() {
        boolean isEndYear = currentYear == overviewEndDate.getYear();
        boolean isEndMonth = isEndYear && currentMonth == overviewEndDate.getMonthValue();
        NextMonthbtn.setDisable(isEndMonth);
        NextYearbtn.setDisable(isEndYear);
        NextMonthbtn.setVisible(!isEndMonth);
        NextYearbtn.setVisible(!isEndYear);

        boolean isStartYear = currentYear == overviewStartDate.getYear();
        boolean isStartMonth = isStartYear && currentMonth == overviewStartDate.getMonthValue();
        PreviousMonthbtn.setDisable(isStartMonth);
        PreviousYearbtn.setDisable(isStartYear);
        PreviousMonthbtn.setVisible(!isStartMonth);
        PreviousYearbtn.setVisible(!isStartYear);
    }

    private LocalDate getOverviewEarliestDate() {
        LocalDate earliest = today.withDayOfMonth(1);
        try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT MIN(order_date) FROM orders")) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Date date = rs.getDate(1);
                    if (date != null)
                        earliest = date.toLocalDate().withDayOfMonth(1);
                }
            }
        } catch (SQLException ex) {
            logger.warn("Failed to determine earliest order date", ex);
        }
        return earliest;
    }

    @FXML
    private void clickNextMonth(ActionEvent e) {
        if (currentMonth == 12) {
            currentMonth = 1;
            currentYear++;
            yearBox.setValue(currentYear);
        } else {
            currentMonth++;
        }
        updateMonthBox();
        updateDateControls();
        loadAllData(true);
    }

    @FXML
    private void clickPreviousMonth(ActionEvent e) {
        if (currentMonth == 1) {
            currentMonth = 12;
            currentYear--;
            yearBox.setValue(currentYear);
        } else {
            currentMonth--;
        }
        updateMonthBox();
        updateDateControls();
        loadAllData(true);
    }

    @FXML
    private void clickNextYear(ActionEvent e) {
        currentYear++;
        yearBox.setValue(currentYear);
        updateMonthBox();
        updateDateControls();
        loadAllData(true);
    }

    @FXML
    private void clickPreviousYear(ActionEvent e) {
        currentYear--;
        yearBox.setValue(currentYear);
        updateMonthBox();
        updateDateControls();
        loadAllData(true);
    }

    private void loadAllData(boolean reloadYearData) {
        loadTopPaneMetrics();
        if (reloadYearData) {
            loadAreaChart();
            loadBarChart();
        }
        loadCarPieChart();
        loadPartPieChart();
        loadTargetProgress();

    }

    private void loadTopPaneMetrics() {
        Task<Map<String, Object>> task = new Task<>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                Map<String, Object> metrics = new HashMap<>();

                try (Connection con = DatabaseConnectionManager.getInstance().getConnection()) {
                    LocalDate startDate = LocalDate.of(currentYear, currentMonth, 1);
                    LocalDate endDate = startDate.plusMonths(1);

                    // Get car sales (quantity and paid value)
                    String carQuery = """
                            SELECT COALESCE(SUM(d.qty), 0) as qty,
                                   COALESCE(SUM(d.total_price), 0) as amount
                            FROM orders o 
                            JOIN order_details d ON o.order_id = d.order_id 
                            WHERE o.order_date >= ? AND o.order_date < ? AND d.car_id IS NOT NULL
                            """;
                    try (PreparedStatement ps = con.prepareStatement(carQuery)) {
                        ps.setString(1, startDate.toString());
                        ps.setString(2, endDate.toString());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                metrics.put("carQty", rs.getInt("qty"));
                                metrics.put("carAmount", rs.getDouble("amount"));
                            }
                        }
                    }
                    // Get part sales (quantity and paid value)
                    String partQuery = """
                            SELECT COALESCE(SUM(d.qty), 0) as qty,
                                   COALESCE(SUM(d.total_price), 0) as amount
                            FROM orders o 
                            JOIN order_details d ON o.order_id = d.order_id 
                            WHERE o.order_date >= ? AND o.order_date < ? AND d.part_id IS NOT NULL
                            """;
                    try (PreparedStatement ps = con.prepareStatement(partQuery)) {
                        ps.setString(1, startDate.toString());
                        ps.setString(2, endDate.toString());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                metrics.put("partQty", rs.getInt("qty"));
                                metrics.put("partAmount", rs.getDouble("amount"));
                            }
                        }
                    }

                    // Get customize sales (quantity and paid value)
                    String customQuery = """
                            SELECT COUNT(d.detail_id) as qty,
                                   COALESCE(SUM(d.total_price), 0) as amount
                            FROM orders o 
                            JOIN order_details d ON o.order_id = d.order_id 
                            WHERE o.order_date >= ? AND o.order_date < ? AND d.is_customize = 1
                            """;
                    try (PreparedStatement ps = con.prepareStatement(customQuery)) {
                        ps.setString(1, startDate.toString());
                        ps.setString(2, endDate.toString());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                metrics.put("customQty", rs.getInt("qty"));
                                metrics.put("customAmount", rs.getDouble("amount"));
                            }
                        }
                    }

                    String nonInstallmentRevenueQuery = """
                            SELECT COALESCE(SUM(d.total_price), 0) as amount
                            FROM orders o
                            JOIN order_details d ON o.order_id = d.order_id
                            WHERE o.order_date >= ? AND o.order_date < ? AND o.is_installment = 0
                            """;
                    try (PreparedStatement ps = con.prepareStatement(nonInstallmentRevenueQuery)) {
                        ps.setString(1, startDate.toString());
                        ps.setString(2, endDate.toString());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                metrics.put("nonInstallmentRevenue", rs.getDouble("amount"));
                            }
                        }
                    }

                    String installmentPaidQuery = """
                            SELECT COALESCE(SUM(COALESCE(o.paid_amount, 0)), 0) as amount
                            FROM orders o
                            WHERE o.order_date >= ? AND o.order_date < ? AND o.is_installment = 1
                            """;
                    try (PreparedStatement ps = con.prepareStatement(installmentPaidQuery)) {
                        ps.setString(1, startDate.toString());
                        ps.setString(2, endDate.toString());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                metrics.put("installmentPaid", rs.getDouble("amount"));
                            }
                        }
                    }

                    // Get previous month paid revenue for growth calculation
                    LocalDate prevStartDate = startDate.minusMonths(1);
                    LocalDate prevEndDate = startDate;
                    String prevRevenueQuery = """
                            SELECT 
                                COALESCE(SUM(CASE WHEN is_installment = 0 THEN order_total ELSE COALESCE(paid_amount, 0) END), 0) as amount
                            FROM (
                                SELECT o.order_id,
                                       o.is_installment,
                                       o.paid_amount,
                                       SUM(d.total_price) as order_total
                                FROM orders o
                                JOIN order_details d ON o.order_id = d.order_id
                                WHERE o.order_date >= ? AND o.order_date < ?
                                GROUP BY o.order_id, o.is_installment, o.paid_amount
                            ) sub
                            """;
                    try (PreparedStatement ps = con.prepareStatement(prevRevenueQuery)) {
                        ps.setString(1, prevStartDate.toString());
                        ps.setString(2, prevEndDate.toString());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                metrics.put("prevRevenue", rs.getDouble("amount"));
                            }
                        }
                    }
                }

                return metrics;
            }
        };

        task.setOnSucceeded(e -> {
            Map<String, Object> metrics = task.getValue();
            int carQty = (int) metrics.getOrDefault("carQty", 0);
            double carAmount = (double) metrics.getOrDefault("carAmount", 0.0);
            int partQty = (int) metrics.getOrDefault("partQty", 0);
            double partAmount = (double) metrics.getOrDefault("partAmount", 0.0);
            int customQty = (int) metrics.getOrDefault("customQty", 0);
            double customAmount = (double) metrics.getOrDefault("customAmount", 0.0);
            double nonInstallmentRevenue = (double) metrics.getOrDefault("nonInstallmentRevenue", 0.0);
            double installmentPaid = (double) metrics.getOrDefault("installmentPaid", 0.0);
            double prevRevenue = (double) metrics.getOrDefault("prevRevenue", 0.0);

            double totalRevenue = nonInstallmentRevenue + installmentPaid;

            totalSalesLbl.setText(carQty + " SOLD");
            totalSalesValueLbl.setText(String.format("$%,.2f", carAmount));
            partsSoldLbl.setText(partQty + " SOLD");
            partsValueLbl.setText(String.format("$%,.2f", partAmount));
            servicesLbl.setText(customQty + " DONE");
            servicesValueLbl.setText(String.format("$%,.2f", customAmount));
            revenueLbl.setText(String.format("$%,.2f", totalRevenue));

            if (prevRevenue > 0) {
                double growth = ((totalRevenue - prevRevenue) / prevRevenue) * 100;
                String sign = growth >= 0 ? "+" : "";
                revenueGrowthLbl.setText(String.format("%s%.1f%%", sign, growth));
            } else if (totalRevenue > 0) {
                revenueGrowthLbl.setText("+100%");
            } else {
                revenueGrowthLbl.setText("0%");
            }
        });

        task.setOnFailed(e -> logger.error("Failed to load top pane metrics", task.getException()));
        new Thread(task).start();
    }

    private void loadBarChart() {
        if (barChart == null)
            return;

        Task<List<XYChart.Data<String, Number>>> task = new Task<>() {
            @Override
            protected List<XYChart.Data<String, Number>> call() throws Exception {
                List<XYChart.Data<String, Number>> chartData = new ArrayList<>();
                String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

                try (Connection con = DatabaseConnectionManager.getInstance().getConnection()) {
                    String query;
                    if ("car".equals(currentBarChartType)) {
                        query = """
                                SELECT 
                                    MONTH(o.order_date) as month_num,
                                    COUNT(od.detail_id) as quantity
                                FROM orders o 
                                JOIN order_details od ON o.order_id = od.order_id 
                                WHERE YEAR(o.order_date) = ? AND od.car_id IS NOT NULL
                                GROUP BY MONTH(o.order_date)
                                ORDER BY MONTH(o.order_date)
                                """;
                    } else {
                        query = """
                                SELECT 
                                    MONTH(o.order_date) as month_num,
                                    COALESCE(SUM(od.qty), 0) as quantity
                                FROM orders o 
                                JOIN order_details od ON o.order_id = od.order_id 
                                WHERE YEAR(o.order_date) = ? AND od.part_id IS NOT NULL
                                GROUP BY MONTH(o.order_date)
                                ORDER BY MONTH(o.order_date)
                                """;
                    }

                    try (PreparedStatement ps = con.prepareStatement(query)) {
                        ps.setInt(1, currentYear);

                        // Initialize all months with 0 quantity
                        Map<Integer, Integer> monthQuantity = new HashMap<>();
                        for (int i = 1; i <= 12; i++) {
                            monthQuantity.put(i, 0);
                        }

                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                int month = rs.getInt("month_num");
                                int quantity = rs.getInt("quantity");
                                monthQuantity.put(month, quantity);
                            }
                        }

                        // Convert to chart data
                        for (int i = 1; i <= 12; i++) {
                            String monthName = monthNames[i - 1];
                            int quantity = monthQuantity.get(i);
                            chartData.add(new XYChart.Data<>(monthName, quantity));
                        }
                    }
                }

                return chartData;
            }
        };

        task.setOnSucceeded(e -> {
            List<XYChart.Data<String, Number>> chartData = task.getValue();

            Platform.runLater(() -> {
                barChart.getData().clear();
                barChart.setAnimated(false);
                barChart.setLegendVisible(false);

                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName("car".equals(currentBarChartType) ? "Car Sales" : "Part Sales");
                series.getData().addAll(chartData);

                barChart.getData().add(series);

                // Style the bar chart
                styleBarChart();
            });
        });

        task.setOnFailed(e -> {
            logger.error("Failed to load bar chart", task.getException());
            Platform.runLater(() -> {
                barChart.getData().clear();
                // Add placeholder data
                XYChart.Series<String, Number> errorSeries = new XYChart.Series<>();
                errorSeries.setName("No Data Available");
                errorSeries.getData().add(new XYChart.Data<>("Error", 0));
                barChart.getData().add(errorSeries);
            });
        });

        new Thread(task).start();
    }

    private void styleBarChart() {
        Platform.runLater(() -> {
            String color = "car".equals(currentBarChartType) ? "#6d8196" : "#ffa500";

            // Apply styling to all bars with the correct color
            barChart.lookupAll(".chart-bar").forEach(node -> {
                CSSManager.clearStyles(node);
                CSSManager.applyChartBar(node);
                // Apply the specific color based on current chart type
                node.setStyle("-fx-bar-fill: " + color + ";");
            });

            // Set chart background
            CSSManager.applyBarChart(barChart);
        });
    }

    private void loadAreaChart() {
        if (areaChart == null)
            return;

        Task<List<XYChart.Data<String, Number>>> task = new Task<>() {
            @Override
            protected List<XYChart.Data<String, Number>> call() throws Exception {
                List<XYChart.Data<String, Number>> revenueData = new ArrayList<>();
                String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

                try (Connection con = DatabaseConnectionManager.getInstance().getConnection()) {
                    // Get paid revenue for each month (like manager) - considers installments
                    String revenueQuery = """
                            SELECT 
                                MONTH(o.order_date) as month_num,
                                COALESCE(SUM(o.paid_amount), 0) as total_revenue
                            FROM orders o 
                            WHERE YEAR(o.order_date) = ? 
                            GROUP BY MONTH(o.order_date)
                            ORDER BY MONTH(o.order_date)
                            """;

                    try (PreparedStatement ps = con.prepareStatement(revenueQuery)) {
                        ps.setInt(1, currentYear);

                        // Initialize all months with 0 revenue
                        Map<Integer, Double> monthRevenue = new HashMap<>();
                        for (int i = 1; i <= 12; i++) {
                            monthRevenue.put(i, 0.0);
                        }

                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                int month = rs.getInt("month_num");
                                double revenue = rs.getDouble("total_revenue");
                                monthRevenue.put(month, revenue);
                            }
                        }

                        // Convert to chart data
                        for (int i = 1; i <= 12; i++) {
                            String monthName = monthNames[i - 1];
                            double revenue = monthRevenue.get(i);
                            revenueData.add(new XYChart.Data<>(monthName, revenue));
                        }
                    }
                }

                return revenueData;
            }
        };

        task.setOnSucceeded(e -> {
            List<XYChart.Data<String, Number>> revenueData = task.getValue();

            Platform.runLater(() -> {
                areaChart.getData().clear();
                areaChart.setAnimated(false);
                areaChart.setLegendVisible(false);
                areaChart.setCreateSymbols(true);

                XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
                revenueSeries.setName("Monthly Revenue");
                revenueSeries.getData().addAll(revenueData);

                areaChart.getData().add(revenueSeries);

                // Style the area chart
                styleAreaChart();

                // Hide chart temporarily while smoothing
                areaChart.setOpacity(0);

                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(100));
                pause.setOnFinished(ev -> applySmoothCurves());
                pause.play();
            });
        });

        task.setOnFailed(e -> {
            logger.error("Failed to load revenue area chart", task.getException());
            Platform.runLater(() -> {
                areaChart.getData().clear();
                // Add placeholder data to show chart is not working
                XYChart.Series<String, Number> errorSeries = new XYChart.Series<>();
                errorSeries.setName("No Data Available");
                errorSeries.getData().add(new XYChart.Data<>("Error", 0));
                areaChart.getData().add(errorSeries);
            });
        });

        new Thread(task).start();
    }

    private void styleAreaChart() {
        Platform.runLater(() -> {
            CSSManager.applyAdminOverviewChart(areaChart);

            areaChart.lookupAll(".chart-series-area-fill").forEach(node -> {
                CSSManager.applyChartSeriesAreaFill(node);
            });

            areaChart.lookupAll(".chart-series-area-line").forEach(node -> {
                CSSManager.applyChartSeriesAreaLine(node);
            });

            areaChart.lookupAll(".chart-area-symbol").forEach(node -> {
                CSSManager.applyChartLineSymbol(node);
            });
        });
    }

    private void applySmoothCurves() {
        if (areaChart == null)
            return;

        areaChart.applyCss();
        areaChart.layout();

        // Check if paths are ready, if not, retry after a short delay
        var linePaths = areaChart.lookupAll(".chart-series-area-line");
        var fillPaths = areaChart.lookupAll(".chart-series-area-fill");

        if (linePaths.isEmpty() || fillPaths.isEmpty()) {
            // Paths not ready yet, try again after 50ms
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(50));
            pause.setOnFinished(e -> applySmoothCurves());
            pause.play();
            return;
        }

        java.util.List<javafx.scene.shape.PathElement> smoothedLineElements = new java.util.ArrayList<>();

        linePaths.forEach(node -> {
            if (node instanceof javafx.scene.shape.Path) {
                javafx.scene.shape.Path path = (javafx.scene.shape.Path) node;
                smoothPath(path);
                smoothedLineElements.addAll(new java.util.ArrayList<>(path.getElements()));
            }
        });

        fillPaths.forEach(node -> {
            if (node instanceof javafx.scene.shape.Path && !smoothedLineElements.isEmpty()) {
                javafx.scene.shape.Path fillPath = (javafx.scene.shape.Path) node;
                var originalFillElements = new java.util.ArrayList<>(fillPath.getElements());

                double baselineY = 0;
                double startX = 0;
                double endX = 0;

                for (var element : originalFillElements) {
                    if (element instanceof javafx.scene.shape.LineTo) {
                        javafx.scene.shape.LineTo lt = (javafx.scene.shape.LineTo) element;
                        baselineY = Math.max(baselineY, lt.getY());
                    }
                }

                if (smoothedLineElements.get(0) instanceof javafx.scene.shape.MoveTo) {
                    javafx.scene.shape.MoveTo firstMove = (javafx.scene.shape.MoveTo) smoothedLineElements.get(0);
                    startX = firstMove.getX();
                }

                var lastElement = smoothedLineElements.get(smoothedLineElements.size() - 1);
                if (lastElement instanceof javafx.scene.shape.CubicCurveTo) {
                    javafx.scene.shape.CubicCurveTo lastCurve = (javafx.scene.shape.CubicCurveTo) lastElement;
                    endX = lastCurve.getX();
                }

                fillPath.getElements().clear();

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

                fillPath.getElements().add(new javafx.scene.shape.LineTo(endX, baselineY));
                fillPath.getElements().add(new javafx.scene.shape.LineTo(startX, baselineY));
                fillPath.getElements().add(new javafx.scene.shape.ClosePath());
            }
        });

        areaChart.setOpacity(1);
    }

    private void smoothPath(javafx.scene.shape.Path path) {
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

    private void loadCarPieChart() {
        if (pieCar == null)
            return;

        Task<ObservableList<PieChart.Data>> task = new Task<>() {
            @Override
            protected ObservableList<PieChart.Data> call() throws Exception {
                ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

                try (Connection con = DatabaseConnectionManager.getInstance().getConnection()) {
                    LocalDate startDate = LocalDate.of(currentYear, currentMonth, 1);
                    LocalDate endDate = startDate.plusMonths(1);

                    // Get car sales by model name
                    String query = "SELECT CONCAT(c.model_name, ' ', COALESCE(c.trim_name, '')) as car_model, " +
                            "COUNT(od.detail_id) as qty " +
                            "FROM orders o " +
                            "JOIN order_details od ON o.order_id = od.order_id " +
                            "JOIN cars c ON od.car_id = c.car_id " +
                            "WHERE o.order_date >= ? AND o.order_date < ? AND od.car_id IS NOT NULL " +
                            "GROUP BY c.car_id, c.model_name, c.trim_name " +
                            "ORDER BY qty DESC";

                    try (PreparedStatement ps = con.prepareStatement(query)) {
                        ps.setString(1, startDate.toString());
                        ps.setString(2, endDate.toString());
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                String modelName = rs.getString("car_model").trim();
                                int qty = rs.getInt("qty");
                                pieData.add(new PieChart.Data(modelName, qty));
                            }
                        }
                    }
                }

                if (pieData.isEmpty()) {
                    pieData.add(new PieChart.Data("No Sales", 1));
                }

                return pieData;
            }
        };

        task.setOnSucceeded(e -> pieCar.setData(task.getValue()));
        task.setOnFailed(e -> logger.error("Failed to load car pie chart", task.getException()));
        new Thread(task).start();
    }

    private void loadPartPieChart() {
        if (piePart == null)
            return;

        Task<ObservableList<PieChart.Data>> task = new Task<>() {
            @Override
            protected ObservableList<PieChart.Data> call() throws Exception {
                ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

                try (Connection con = DatabaseConnectionManager.getInstance().getConnection()) {
                    LocalDate startDate = LocalDate.of(currentYear, currentMonth, 1);
                    LocalDate endDate = startDate.plusMonths(1);

                    // Get part sales by part name
                    String query = "SELECT cp.part_name, SUM(od.qty) as qty " +
                            "FROM orders o " +
                            "JOIN order_details od ON o.order_id = od.order_id " +
                            "JOIN car_parts cp ON od.part_id = cp.part_id " +
                            "WHERE o.order_date >= ? AND o.order_date < ? AND od.part_id IS NOT NULL " +
                            "GROUP BY cp.part_id, cp.part_name " +
                            "ORDER BY qty DESC";

                    try (PreparedStatement ps = con.prepareStatement(query)) {
                        ps.setString(1, startDate.toString());
                        ps.setString(2, endDate.toString());
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                String partName = rs.getString("part_name");
                                int qty = rs.getInt("qty");
                                pieData.add(new PieChart.Data(partName, qty));
                            }
                        }
                    }
                }

                if (pieData.isEmpty()) {
                    pieData.add(new PieChart.Data("No Sales", 1));
                }

                return pieData;
            }
        };

        task.setOnSucceeded(e -> piePart.setData(task.getValue()));
        task.setOnFailed(e -> logger.error("Failed to load part pie chart", task.getException()));
        new Thread(task).start();
    }

    private void loadTargetProgress() {
        Task<Map<String, Object>> task = new Task<>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                Map<String, Object> data = new HashMap<>();

                try (Connection con = DatabaseConnectionManager.getInstance().getConnection()) {
                    LocalDate startDate = LocalDate.of(currentYear, currentMonth, 1);
                    LocalDate endDate = startDate.plusMonths(1);

                    // Get total targets from all managers for the current month
                    String targetQuery = "SELECT target FROM user_target ut " +
                            "JOIN user_info ui ON ut.user_id = ui.user_id " +
                            "WHERE ui.user_role = 'manager' AND ui.user_status = 1 " +
                            "AND ut.effective_date = ?";

                    int totalCarTarget = 0;
                    int totalPartTarget = 0;

                    try (PreparedStatement ps = con.prepareStatement(targetQuery)) {
                        ps.setString(1, startDate.toString());
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                String target = rs.getString("target");
                                if (target != null && target.contains("cars-") && target.contains("parts-")) {
                                    // Parse target format: "cars-X,parts-Y"
                                    String[] parts = target.split(",");
                                    for (String part : parts) {
                                        if (part.startsWith("cars-")) {
                                            totalCarTarget += Integer.parseInt(part.substring(5));
                                        } else if (part.startsWith("parts-")) {
                                            totalPartTarget += Integer.parseInt(part.substring(6));
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Get actual car sales quantity for the month
                    String carSalesQuery = "SELECT COUNT(d.detail_id) as qty " +
                            "FROM orders o JOIN order_details d ON o.order_id = d.order_id " +
                            "WHERE o.order_date >= ? AND o.order_date < ? AND d.car_id IS NOT NULL";
                    int actualCarSales = 0;
                    try (PreparedStatement ps = con.prepareStatement(carSalesQuery)) {
                        ps.setString(1, startDate.toString());
                        ps.setString(2, endDate.toString());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                actualCarSales = rs.getInt("qty");
                            }
                        }
                    }

                    // Get actual part sales quantity for the month
                    String partSalesQuery = "SELECT COALESCE(SUM(d.qty), 0) as qty " +
                            "FROM orders o JOIN order_details d ON o.order_id = d.order_id " +
                            "WHERE o.order_date >= ? AND o.order_date < ? AND d.part_id IS NOT NULL";
                    int actualPartSales = 0;
                    try (PreparedStatement ps = con.prepareStatement(partSalesQuery)) {
                        ps.setString(1, startDate.toString());
                        ps.setString(2, endDate.toString());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                actualPartSales = rs.getInt("qty");
                            }
                        }
                    }

                    data.put("totalCarTarget", totalCarTarget);
                    data.put("totalPartTarget", totalPartTarget);
                    data.put("actualCarSales", actualCarSales);
                    data.put("actualPartSales", actualPartSales);
                }

                return data;
            }
        };

        task.setOnSucceeded(e -> {
            Map<String, Object> data = task.getValue();
            int totalCarTarget = (int) data.getOrDefault("totalCarTarget", 0);
            int totalPartTarget = (int) data.getOrDefault("totalPartTarget", 0);
            int actualCarSales = (int) data.getOrDefault("actualCarSales", 0);
            int actualPartSales = (int) data.getOrDefault("actualPartSales", 0);

            // Display target summary in center
            totalTargetLbl.setText(String.format("Cars: %d | Parts: %d", totalCarTarget, totalPartTarget));

            // Calculate progress percentages
            double carProgressPercent = totalCarTarget > 0 ? Math.min((actualCarSales * 100.0) / totalCarTarget, 100.0) : 0;
            double partProgressPercent = totalPartTarget > 0 ? Math.min((actualPartSales * 100.0) / totalPartTarget, 100.0) : 0;

            // Format labels with sold/target and percentage, plus excess if over target
            updateTargetLabel(carTargetProgressLbl, actualCarSales, totalCarTarget, carProgressPercent);
            updateTargetLabel(partTargetProgressLbl, actualPartSales, totalPartTarget, partProgressPercent);

            // Debug output
            System.out.println("=== Target Progress Debug ===");
            System.out.println("Car Target: " + totalCarTarget + ", Actual: " + actualCarSales + " (" + carProgressPercent + "%)");
            System.out.println("Part Target: " + totalPartTarget + ", Actual: " + actualPartSales + " (" + partProgressPercent + "%)");

            // Update circles to show target progress
            // Both circles are independent - each shows progress toward their own target
            // Outer circle (car): shows car progress toward car target
            // Inner circle (part): shows part progress toward part target
            updateTargetProgress(carRevenueCircle, carProgressPercent, 70.0);
            updateTargetProgress(partRevenueCircle, partProgressPercent, 54.0);
        });

        task.setOnFailed(e -> logger.error("Failed to load target progress", task.getException()));
        new Thread(task).start();
    }

    private void updateTargetLabel(Label label, int actual, int target, double progressPercent) {
        if (target == 0) {
            label.setText("No Target Set");
            label.setGraphic(null);
            label.setContentDisplay(ContentDisplay.TEXT_ONLY);
            return;
        }

        String baseTextValue = String.format("%d/%d • %.1f%%", actual, target, progressPercent);
        Text baseText = new Text(baseTextValue);
        CSSManager.applyAdminLabelValue(baseText);

        TextFlow flow = new TextFlow(baseText);
        flow.setTextAlignment(TextAlignment.CENTER);

        if (actual > target) {
            int excess = actual - target;
            Text extraText = new Text(String.format(" (+%d)", excess));
            CSSManager.applyPercentagePositive(extraText);
            flow.getChildren().add(extraText);
        }

        label.setText(null);
        label.setGraphic(flow);
        label.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    private void updateTargetProgress(Circle circle, double percent, double radius) {
        if (circle == null)
            return;

        if (!circle.getStyle().contains("-fx-stroke-line-cap")) {
            circle.setStyle(circle.getStyle() + "; -fx-stroke-line-cap: round;");
        }

        double circumference = 2 * Math.PI * radius;
        double clampedPercent = Math.max(0, Math.min(percent, 100));

        var dashArray = circle.getStrokeDashArray();
        if (dashArray.size() != 2 || Math.abs(dashArray.get(0) - circumference) > 0.1) {
            dashArray.clear();
            dashArray.addAll(circumference, circumference);
        }

        double targetOffset = circumference - (clampedPercent / 100.0) * circumference;

        System.out.println("Target Progress - Circle radius: " + radius + ", percent: " + percent + "%");
        System.out.println("  Circumference: " + circumference + ", Target offset: " + targetOffset);

        animateCircleProgress(circle, circumference, targetOffset);

        circle.setRotate(-90);
    }

    private void animateCircleProgress(Circle circle, double circumference, double targetOffset) {
        Double previousOffset = (Double) circle.getProperties().get("progressPrevOffset");
        if (previousOffset == null) {
            previousOffset = circumference;
        }

        circle.setStrokeDashOffset(previousOffset);

        Timeline existing = (Timeline) circle.getProperties().get("progressTimeline");
        if (existing != null) {
            existing.stop();
        }

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(circle.strokeDashOffsetProperty(), previousOffset)),
                new KeyFrame(Duration.seconds(1.2),
                        new KeyValue(circle.strokeDashOffsetProperty(), targetOffset, Interpolator.EASE_OUT))
        );

        timeline.setOnFinished(e -> circle.getProperties().put("progressPrevOffset", targetOffset));
        circle.getProperties().put("progressTimeline", timeline);
        timeline.play();
    }
    @FXML
    void clickCarbtn(ActionEvent event) {
        currentBarChartType = "car";
        carbtn.getStyleClass().add("active");
        partbtn.getStyleClass().remove("active");
        loadBarChart();
    }

    @FXML
    void clickPartbtn(ActionEvent event) {
        currentBarChartType = "part";
        partbtn.getStyleClass().add("active");
        carbtn.getStyleClass().remove("active");
        loadBarChart();
    }
}
