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
                    
                    // Get car sales (quantity and value)
                    String carQuery = "SELECT COUNT(d.detail_id) as qty, COALESCE(SUM(d.total_price), 0) as amount " +
                            "FROM orders o JOIN order_details d ON o.order_id = d.order_id " +
                            "WHERE o.order_date >= ? AND o.order_date < ? AND d.car_id IS NOT NULL";
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
                    
                    // Get part sales (quantity and value)
                    String partQuery = "SELECT SUM(d.qty) as qty, COALESCE(SUM(d.total_price), 0) as amount " +
                            "FROM orders o JOIN order_details d ON o.order_id = d.order_id " +
                            "WHERE o.order_date >= ? AND o.order_date < ? AND d.part_id IS NOT NULL";
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
                    
                    // Get customize sales (quantity and value)
                    String customQuery = "SELECT COUNT(d.detail_id) as qty, COALESCE(SUM(d.total_price), 0) as amount " +
                            "FROM orders o JOIN order_details d ON o.order_id = d.order_id " +
                            "WHERE o.order_date >= ? AND o.order_date < ? AND d.is_customize = 1";
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
                        
                        String query;
                        if ("car".equals(currentBarChartType)) {
                            // Count car sales quantity
                            query = "SELECT COUNT(d.detail_id) as qty " +
                                    "FROM orders o JOIN order_details d ON o.order_id = d.order_id " +
                                    "WHERE o.order_date >= ? AND o.order_date < ? AND d.car_id IS NOT NULL";
                        } else {
                            // Sum part sales quantity
                            query = "SELECT COALESCE(SUM(d.qty), 0) as qty " +
                                    "FROM orders o JOIN order_details d ON o.order_id = d.order_id " +
                                    "WHERE o.order_date >= ? AND o.order_date < ? AND d.part_id IS NOT NULL";
                        }
                        
                        try (PreparedStatement ps = con.prepareStatement(query)) {
                            ps.setString(1, startDate.toString());
                            ps.setString(2, endDate.toString());
                            try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                    values[month - 1] = rs.getDouble("qty");
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
            series.setName("car".equals(currentBarChartType) ? "Car Sales (Quantity)" : "Part Sales (Quantity)");
            
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
                Map<Integer, Double> monthlyRevenue = new HashMap<>();
                
                try (Connection con = DatabaseConnectionManager.getInstance().getConnection()) {
                    // Get revenue for each month of the selected year
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
                                    monthlyRevenue.put(month, rs.getDouble("total"));
                                }
                            }
                        }
                    }
                }
                
                return monthlyRevenue;
            }
        };
        
        task.setOnSucceeded(e -> {
            Map<Integer, Double> monthlyRevenue = task.getValue();
            areaChart.getData().clear();
            
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Revenue Analysis");
            
            String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
            for (int month = 1; month <= 12; month++) {
                double revenue = monthlyRevenue.getOrDefault(month, 0.0);
                series.getData().add(new XYChart.Data<>(monthNames[month - 1], revenue));
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
                    
                    // Get car sales by model name
                    String query = "SELECT CONCAT(cm.model_name, ' ', COALESCE(cm.trim_name, '')) as car_model, " +
                            "COUNT(od.detail_id) as qty " +
                            "FROM orders o " +
                            "JOIN order_details od ON o.order_id = od.order_id " +
                            "JOIN cars c ON od.car_id = c.car_id " +
                            "JOIN car_models cm ON c.model_id = cm.model_id " +
                            "WHERE o.order_date >= ? AND o.order_date < ? AND od.car_id IS NOT NULL " +
                            "GROUP BY cm.model_id, cm.model_name, cm.trim_name " +
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
        if (piePart == null) return;
        
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

    private void loadRevenueDistribution() {
        Task<Map<String, Object>> task = new Task<>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                Map<String, Object> data = new HashMap<>();
                
                try (Connection con = DatabaseConnectionManager.getInstance().getConnection()) {
                    LocalDate startDate = LocalDate.of(currentYear, currentMonth, 1);
                    LocalDate endDate = startDate.plusMonths(1);
                    
                    // Get car sales quantity and revenue
                    String carQuery = "SELECT COUNT(d.detail_id) as qty, COALESCE(SUM(d.total_price), 0) as amount " +
                            "FROM orders o JOIN order_details d ON o.order_id = d.order_id " +
                            "WHERE o.order_date >= ? AND o.order_date < ? AND d.car_id IS NOT NULL";
                    try (PreparedStatement ps = con.prepareStatement(carQuery)) {
                        ps.setString(1, startDate.toString());
                        ps.setString(2, endDate.toString());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                data.put("carQty", rs.getInt("qty"));
                                data.put("carRevenue", rs.getDouble("amount"));
                            }
                        }
                    }
                    
                    // Get part sales quantity and revenue
                    String partQuery = "SELECT SUM(d.qty) as qty, COALESCE(SUM(d.total_price), 0) as amount " +
                            "FROM orders o JOIN order_details d ON o.order_id = d.order_id " +
                            "WHERE o.order_date >= ? AND o.order_date < ? AND d.part_id IS NOT NULL";
                    try (PreparedStatement ps = con.prepareStatement(partQuery)) {
                        ps.setString(1, startDate.toString());
                        ps.setString(2, endDate.toString());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                data.put("partQty", rs.getInt("qty"));
                                data.put("partRevenue", rs.getDouble("amount"));
                            }
                        }
                    }
                }
                
                return data;
            }
        };
        
        task.setOnSucceeded(e -> {
            Map<String, Object> data = task.getValue();
            int carQty = (int) data.getOrDefault("carQty", 0);
            double carRevenue = (double) data.getOrDefault("carRevenue", 0.0);
            int partQty = (int) data.getOrDefault("partQty", 0);
            double partRevenue = (double) data.getOrDefault("partRevenue", 0.0);
            
            double totalRevenue = carRevenue + partRevenue;
            
            // Display total revenue in center
            totalRevenueLbl.setText(String.format("$%,.0f", totalRevenue));
            
            // Display revenue values with percentage of total
            if (totalRevenue > 0) {
                double carPercentOfTotal = (carRevenue / totalRevenue) * 100;
                double partPercentOfTotal = (partRevenue / totalRevenue) * 100;
                
                carRevenueDistLbl.setText(String.format("$%,.0f • %.1f%%", carRevenue, carPercentOfTotal));
                partRevenueDistLbl.setText(String.format("$%,.0f • %.1f%%", partRevenue, partPercentOfTotal));
            } else {
                carRevenueDistLbl.setText("$0 • 0%");
                partRevenueDistLbl.setText("$0 • 0%");
            }
            
            // Set monthly targets (these can be made configurable later)
            int carSalesTarget = 10;    // Target: 10 cars per month
            int partSalesTarget = 50;   // Target: 50 parts per month
            
            // Calculate percentage of target achieved based on quantity sold
            double carTargetPercent = carSalesTarget > 0 ? (carQty * 100.0 / carSalesTarget) : 0;
            double partTargetPercent = partSalesTarget > 0 ? (partQty * 100.0 / partSalesTarget) : 0;
            
            // Cap at 100% for circle display
            double carCirclePercent = Math.min(carTargetPercent, 100);
            double partCirclePercent = Math.min(partTargetPercent, 100);
            
            // Update circles to show progress toward sales target
            updateCircleProgress(carRevenueCircle, carCirclePercent, 70.0);
            updateCircleProgress(partRevenueCircle, partCirclePercent, 54.0);
        });
        
        task.setOnFailed(e -> logger.error("Failed to load revenue distribution", task.getException()));
        new Thread(task).start();
    }

    private void updateCircleProgress(Circle circle, double percent, double radius) {
        if (circle == null) return;
        
        // Calculate circumference
        double circumference = 2 * Math.PI * radius;
        
        // Calculate dash length based on percentage
        double dashLength = (percent / 100.0) * circumference;
        double gapLength = circumference - dashLength;
        
        // Set stroke dash array (filled portion, gap portion)
        circle.getStrokeDashArray().setAll(dashLength, gapLength);
        
        // Rotate to start from top (12 o'clock position)
        circle.setRotate(-90);
        
        // Set stroke dash offset to 0 to ensure consistent starting point
        circle.setStrokeDashOffset(0);
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
