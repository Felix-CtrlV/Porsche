package Controllers;

import Database.DatabaseConnectionManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.shape.Circle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;

public class adminOverviewController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(adminOverviewController.class);

    @FXML
    private Label totalSalesLbl, partsSoldLbl, servicesLbl, revenueLbl;
    @FXML
    private Label totalSalesValueLbl, partsValueLbl, servicesValueLbl, revenueGrowthLbl;
    @FXML
    private Label totalRevenueLbl, carRevenueDistLbl, partRevenueDistLbl;
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

    private LocalDate today = LocalDate.now();
    private int currentMonth;
    private int currentYear;
    private boolean updatingDateBox = false;
    private String currentBarChartType = "car";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentMonth = today.getMonthValue();
        currentYear = today.getYear();

        setupMonthYear();
        updateMonthBox();
        updateDateControls();
        
        loadAllData();
    }

    private void setupMonthYear() {
        yearBox.getItems().clear();
        for (int y = 2020; y <= today.getYear(); y++) {
            yearBox.getItems().add(y);
        }
        yearBox.setValue(currentYear);

        monthBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (updatingDateBox || newVal == null) return;
            int selectedMonth = Month.valueOf(newVal.toUpperCase(Locale.ENGLISH)).getValue();
            if (selectedMonth != currentMonth) {
                currentMonth = selectedMonth;
                updateDateControls();
                loadAllData();
            }
        });

        yearBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (updatingDateBox || newVal == null) return;
            if (newVal != currentYear) {
                currentYear = newVal;
                updateMonthBox();
                updateDateControls();
                loadAllData();
            }
        });
    }

    private void updateMonthBox() {
        updatingDateBox = true;
        List<String> months = new ArrayList<>();
        
        // Limit months to current month if viewing current year
        int maxMonth = (currentYear == today.getYear()) ? today.getMonthValue() : 12;
        
        for (int i = 1; i <= maxMonth; i++) {
            months.add(Month.of(i).getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
        }
        monthBox.setItems(FXCollections.observableArrayList(months));
        
        // If current month exceeds max available, reset to max
        if (currentMonth > maxMonth) {
            currentMonth = maxMonth;
        }
        if (currentMonth < 1) {
            currentMonth = 1;
        }
        monthBox.setValue(Month.of(currentMonth).getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
        updatingDateBox = false;
    }

    private void updateDateControls() {
        boolean isCurrentYear = currentYear == today.getYear();
        boolean isCurrentMonth = isCurrentYear && currentMonth == today.getMonthValue();
        
        NextMonthbtn.setDisable(isCurrentMonth);
        NextYearbtn.setDisable(isCurrentYear);
        NextMonthbtn.setVisible(!isCurrentMonth);
        NextYearbtn.setVisible(!isCurrentYear);
    }

    private void loadAllData() {
        loadTopPaneMetrics();
        loadBarChart();
        loadAreaChart();
        loadCarPieChart();
        loadPartPieChart();
        loadRevenueDistribution();
    }

    private void loadTopPaneMetrics() {
        Task<Map<String, Object>> task = new Task<>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                Map<String, Object> metrics = new HashMap<>();
                
                try (Connection con = DatabaseConnectionManager.getInstance().getConnection()) {
                    LocalDate startDate = LocalDate.of(currentYear, currentMonth, 1);
                    LocalDate endDate = startDate.plusMonths(1);
                    
                    // Simplified: Get total orders and revenue
                    String orderQuery = "SELECT COUNT(DISTINCT o.order_id) as qty, COALESCE(SUM(d.total_price), 0) as amount " +
                            "FROM orders o JOIN order_details d ON o.order_id = d.order_id " +
                            "WHERE o.order_date >= ? AND o.order_date < ?";
                    try (PreparedStatement ps = con.prepareStatement(orderQuery)) {
                        ps.setString(1, startDate.toString());
                        ps.setString(2, endDate.toString());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                metrics.put("carQty", rs.getInt("qty"));
                                metrics.put("carAmount", rs.getDouble("amount"));
                                // Use same values for parts and custom for now
                                metrics.put("partQty", 0);
                                metrics.put("partAmount", 0.0);
                                metrics.put("customQty", 0);
                                metrics.put("customAmount", 0.0);
                            }
                        }
                    }
                    
                    // Get previous month revenue for growth calculation
                    LocalDate prevStartDate = startDate.minusMonths(1);
                    LocalDate prevEndDate = startDate;
                    String prevRevenueQuery = "SELECT COALESCE(SUM(d.total_price), 0) as amount " +
                            "FROM orders o JOIN order_details d ON o.order_id = d.order_id " +
                            "WHERE o.order_date >= ? AND o.order_date < ?";
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
            double prevRevenue = (double) metrics.getOrDefault("prevRevenue", 0.0);
            
            double totalRevenue = carAmount + partAmount + customAmount;
            
            totalSalesLbl.setText(carQty + " SOLD");
            totalSalesValueLbl.setText(String.format("$%.0f", carAmount));
            partsSoldLbl.setText(partQty + " SOLD");
            partsValueLbl.setText(String.format("$%.0f", partAmount));
            servicesLbl.setText(customQty + " DONE");
            servicesValueLbl.setText(String.format("$%.0f", customAmount));
            revenueLbl.setText(String.format("$%.0f", totalRevenue));
            
            if (prevRevenue > 0) {
                double growth = ((totalRevenue - prevRevenue) / prevRevenue) * 100;
                revenueGrowthLbl.setText(String.format("%+.1f%% Growth", growth));
            } else {
                revenueGrowthLbl.setText("N/A");
            }
        });
        
        task.setOnFailed(e -> logger.error("Failed to load top pane metrics", task.getException()));
        new Thread(task).start();
    }

    private void loadBarChart() {
        if (barChart == null) return;
        
        Task<Map<String, double[]>> task = new Task<>() {
            @Override
            protected Map<String, double[]> call() throws Exception {
                Map<String, double[]> data = new HashMap<>();
                double[] values = new double[12];
                
                try (Connection con = DatabaseConnectionManager.getInstance().getConnection()) {
                    for (int month = 1; month <= 12; month++) {
                        LocalDate startDate = LocalDate.of(currentYear, month, 1);
                        LocalDate endDate = startDate.plusMonths(1);
                        
                        String query = "SELECT COALESCE(SUM(d.total_price), 0) as total " +
                                "FROM orders o JOIN order_details d ON o.order_id = d.order_id " +
                                "WHERE o.order_date >= ? AND o.order_date < ?";
                        
                        try (PreparedStatement ps = con.prepareStatement(query)) {
                            ps.setString(1, startDate.toString());
                            ps.setString(2, endDate.toString());
                            try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                    values[month - 1] = rs.getDouble("total");
                                }
                            }
                        }
                    }
                }
                
                data.put(currentBarChartType, values);
                return data;
            }
        };
        
        task.setOnSucceeded(e -> {
            Map<String, double[]> data = task.getValue();
            barChart.getData().clear();
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Monthly Revenue");
            
            String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            double[] values = data.get(currentBarChartType);
            
            for (int i = 0; i < 12; i++) {
                series.getData().add(new XYChart.Data<>(monthNames[i], values[i]));
            }
            
            barChart.getData().add(series);
        });
        
        task.setOnFailed(e -> logger.error("Failed to load bar chart", task.getException()));
        new Thread(task).start();
    }

    private void loadAreaChart() {
        if (areaChart == null) return;
        
        Task<Map<Integer, Double>> task = new Task<>() {
            @Override
            protected Map<Integer, Double> call() throws Exception {
                Map<Integer, Double> dailyRevenue = new HashMap<>();
                
                try (Connection con = DatabaseConnectionManager.getInstance().getConnection()) {
                    LocalDate startDate = LocalDate.of(currentYear, currentMonth, 1);
                    LocalDate endDate = startDate.plusMonths(1);
                    
                    String query = "SELECT DAY(o.order_date) as day, COALESCE(SUM(d.total_price), 0) as total " +
                            "FROM orders o JOIN order_details d ON o.order_id = d.order_id " +
                            "WHERE o.order_date >= ? AND o.order_date < ? " +
                            "GROUP BY DAY(o.order_date) ORDER BY day";
                    
                    try (PreparedStatement ps = con.prepareStatement(query)) {
                        ps.setString(1, startDate.toString());
                        ps.setString(2, endDate.toString());
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                dailyRevenue.put(rs.getInt("day"), rs.getDouble("total"));
                            }
                        }
                    }
                }
                
                return dailyRevenue;
            }
        };
        
        task.setOnSucceeded(e -> {
            Map<Integer, Double> dailyRevenue = task.getValue();
            areaChart.getData().clear();
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Total Revenue");
            
            int daysInMonth = LocalDate.of(currentYear, currentMonth, 1).lengthOfMonth();
            for (int day = 1; day <= daysInMonth; day++) {
                double revenue = dailyRevenue.getOrDefault(day, 0.0);
                series.getData().add(new XYChart.Data<>(String.valueOf(day), revenue));
            }
            
            areaChart.getData().add(series);
        });
        
        task.setOnFailed(e -> logger.error("Failed to load area chart", task.getException()));
        new Thread(task).start();
    }

    private void loadCarPieChart() {
        if (pieCar == null) return;
        
        Task<ObservableList<PieChart.Data>> task = new Task<>() {
            @Override
            protected ObservableList<PieChart.Data> call() throws Exception {
                ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
                
                try (Connection con = DatabaseConnectionManager.getInstance().getConnection()) {
                    LocalDate startDate = LocalDate.of(currentYear, currentMonth, 1);
                    LocalDate endDate = startDate.plusMonths(1);
                    
                    // Get top customers by order count
                    String query = "SELECT c.customer_name, COUNT(o.order_id) as qty " +
                            "FROM orders o JOIN customer_info c ON o.customer_id = c.customer_id " +
                            "WHERE o.order_date >= ? AND o.order_date < ? " +
                            "GROUP BY c.customer_name ORDER BY qty DESC LIMIT 5";
                    
                    try (PreparedStatement ps = con.prepareStatement(query)) {
                        ps.setString(1, startDate.toString());
                        ps.setString(2, endDate.toString());
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                pieData.add(new PieChart.Data(rs.getString("customer_name"), rs.getInt("qty")));
                            }
                        }
                    }
                }
                
                if (pieData.isEmpty()) {
                    pieData.add(new PieChart.Data("No Data", 1));
                }
                
                return pieData;
            }
        };
        
        task.setOnSucceeded(e -> pieCar.setData(task.getValue()));
        task.setOnFailed(e -> logger.error("Failed to load car pie chart", task.getException()));
        new Thread(task).start();
    }

    private void loadPartPieChart() {
        if (piePart == null) return;
        
        Task<ObservableList<PieChart.Data>> task = new Task<>() {
            @Override
            protected ObservableList<PieChart.Data> call() throws Exception {
                ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
                
                try (Connection con = DatabaseConnectionManager.getInstance().getConnection()) {
                    LocalDate startDate = LocalDate.of(currentYear, currentMonth, 1);
                    LocalDate endDate = startDate.plusMonths(1);
                    
                    // Get order status distribution
                    String query = "SELECT o.order_status, COUNT(o.order_id) as qty " +
                            "FROM orders o " +
                            "WHERE o.order_date >= ? AND o.order_date < ? " +
                            "GROUP BY o.order_status ORDER BY qty DESC";
                    
                    try (PreparedStatement ps = con.prepareStatement(query)) {
                        ps.setString(1, startDate.toString());
                        ps.setString(2, endDate.toString());
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                pieData.add(new PieChart.Data(rs.getString("order_status"), rs.getInt("qty")));
                            }
                        }
                    }
                }
                
                if (pieData.isEmpty()) {
                    pieData.add(new PieChart.Data("No Data", 1));
                }
                
                return pieData;
            }
        };
        
        task.setOnSucceeded(e -> piePart.setData(task.getValue()));
        task.setOnFailed(e -> logger.error("Failed to load part pie chart", task.getException()));
        new Thread(task).start();
    }

    private void loadRevenueDistribution() {
        Task<Map<String, Double>> task = new Task<>() {
            @Override
            protected Map<String, Double> call() throws Exception {
                Map<String, Double> revenue = new HashMap<>();
                
                try (Connection con = DatabaseConnectionManager.getInstance().getConnection()) {
                    LocalDate startDate = LocalDate.of(currentYear, currentMonth, 1);
                    LocalDate endDate = startDate.plusMonths(1);
                    
                    String query = "SELECT COALESCE(SUM(d.total_price), 0) as amount " +
                            "FROM orders o JOIN order_details d ON o.order_id = d.order_id " +
                            "WHERE o.order_date >= ? AND o.order_date < ?";
                    try (PreparedStatement ps = con.prepareStatement(query)) {
                        ps.setString(1, startDate.toString());
                        ps.setString(2, endDate.toString());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                double total = rs.getDouble("amount");
                                revenue.put("car", total * 0.7);  // Simulated split
                                revenue.put("part", total * 0.3);
                            }
                        }
                    }
                }
                
                return revenue;
            }
        };
        
        task.setOnSucceeded(e -> {
            Map<String, Double> revenue = task.getValue();
            double carRevenue = revenue.getOrDefault("car", 0.0);
            double partRevenue = revenue.getOrDefault("part", 0.0);
            double total = carRevenue + partRevenue;
            
            totalRevenueLbl.setText(String.format("$%.0f", total));
            
            if (total > 0) {
                double carPercent = (carRevenue / total) * 100;
                double partPercent = (partRevenue / total) * 100;
                
                carRevenueDistLbl.setText(String.format("$%.0f • %.1f%%", carRevenue, carPercent));
                partRevenueDistLbl.setText(String.format("$%.0f • %.1f%%", partRevenue, partPercent));
                
                updateCircleProgress(carRevenueCircle, carPercent, 70.0);
                updateCircleProgress(partRevenueCircle, partPercent, 54.0);
            } else {
                carRevenueDistLbl.setText("$0 • 0%");
                partRevenueDistLbl.setText("$0 • 0%");
                updateCircleProgress(carRevenueCircle, 0, 70.0);
                updateCircleProgress(partRevenueCircle, 0, 54.0);
            }
        });
        
        task.setOnFailed(e -> logger.error("Failed to load revenue distribution", task.getException()));
        new Thread(task).start();
    }

    private void updateCircleProgress(Circle circle, double percent, double radius) {
        if (circle == null) return;
        double circumference = 2 * Math.PI * radius;
        double dashLength = (percent / 100.0) * circumference;
        circle.getStrokeDashArray().setAll(dashLength, circumference);
        circle.setRotate(-90);
    }

    @FXML
    void clickNextMonth(ActionEvent event) {
        if (currentMonth == 12) {
            currentMonth = 1;
            currentYear++;
            yearBox.setValue(currentYear);
        } else {
            currentMonth++;
        }
        updateMonthBox();
        updateDateControls();
        loadAllData();
    }

    @FXML
    void clickPreviousMonth(ActionEvent event) {
        if (currentMonth == 1) {
            currentMonth = 12;
            currentYear--;
            yearBox.setValue(currentYear);
        } else {
            currentMonth--;
        }
        updateMonthBox();
        updateDateControls();
        loadAllData();
    }

    @FXML
    void clickNextYear(ActionEvent event) {
        currentYear++;
        yearBox.setValue(currentYear);
        updateMonthBox();
        updateDateControls();
        loadAllData();
    }

    @FXML
    void clickPreviousYear(ActionEvent event) {
        currentYear--;
        yearBox.setValue(currentYear);
        updateMonthBox();
        updateDateControls();
        loadAllData();
    }

    @FXML
    void clickCarbtn(ActionEvent event) {
        currentBarChartType = "car";
        loadBarChart();
    }
    
    @FXML
    void clickPartbtn(ActionEvent event) {
        currentBarChartType = "part";
        loadBarChart();
    }
}
