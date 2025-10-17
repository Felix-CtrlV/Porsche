package Controllers;

import Database.Porsche_DB;
import javafx.scene.chart.XYChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for loading chart data using the fixed getSalesChartData procedure
 */
public class ChartDataHelper {
    
    private static final Logger logger = LoggerFactory.getLogger(ChartDataHelper.class);
    
    /**
     * Data class to hold chart data points
     */
    public static class ChartDataPoint {
        private final String label;
        private final double revenue;
        
        public ChartDataPoint(String label, double revenue) {
            this.label = label;
            this.revenue = revenue;
        }
        
        public String getLabel() { return label; }
        public double getRevenue() { return revenue; }
    }
    
    /**
     * Load chart data using the fixed getSalesChartData procedure
     * 
     * @param managerId The manager's user ID
     * @param month The month (1-12)
     * @param year The year
     * @param period The period type: "Daily", "Weekly", or "Monthly"
     * @return List of chart data points
     */
    public static List<ChartDataPoint> loadChartData(int managerId, int month, int year, String period) {
        List<ChartDataPoint> dataPoints = new ArrayList<>();
        Connection tempCon = null;
        CallableStatement cs = null;
        ResultSet rs = null;
        
        try {
            // Establish database connection
            Porsche_DB db = new Porsche_DB();
            tempCon = db.connect();
            
            // Call the fixed stored procedure
            cs = tempCon.prepareCall("CALL getSalesChartData(?, ?, ?, ?)");
            cs.setInt(1, managerId);
            cs.setInt(2, month);
            cs.setInt(3, year);
            cs.setString(4, period);
            
            rs = cs.executeQuery();
            
            while (rs.next()) {
                String periodLabel = rs.getString("period_label");
                double revenue = rs.getDouble("revenue");
                
                dataPoints.add(new ChartDataPoint(periodLabel, revenue));
                
                logger.debug("Chart data point: {} = ${}", periodLabel, String.format("%.2f", revenue));
            }
            
            logger.info("Loaded {} chart data points for period: {}, month: {}, year: {}", 
                       dataPoints.size(), period, month, year);
            
        } catch (SQLException e) {
            logger.error("Error loading chart data from getSalesChartData procedure", e);
        } finally {
            // Clean up resources
            try {
                if (rs != null) rs.close();
                if (cs != null) cs.close();
                if (tempCon != null) tempCon.close();
            } catch (SQLException e) {
                logger.error("Error closing database resources in loadChartData", e);
            }
        }
        
        return dataPoints;
    }
    
    /**
     * Create a JavaFX chart series from the chart data points
     * 
     * @param dataPoints List of chart data points
     * @param seriesName Name for the chart series
     * @return XYChart.Series ready to be added to a chart
     */
    @SuppressWarnings("unchecked")
    public static XYChart.Series<String, Number> createChartSeries(List<ChartDataPoint> dataPoints, String seriesName) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(seriesName);
        
        for (ChartDataPoint point : dataPoints) {
            series.getData().add(new XYChart.Data<>(point.getLabel(), point.getRevenue()));
        }
        
        return series;
    }
}
