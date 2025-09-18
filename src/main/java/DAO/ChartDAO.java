package main.java.DAO;

import Model.overviewBar;
import Model.overviewLine;
import Model.overviewPie;

import java.sql.CallableStatement;
import java.sql.Connection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ChartDAO {

    private Connection con;

    public ChartDAO(Connection con) {
        this.con = con;
    }

    public List<overviewBar> getOverviewBar(int year, String choice) throws SQLException {
        List<overviewBar> result = new ArrayList<>();
        String sql = "{CALL overview_averagesales(?, ?)}";

        try (CallableStatement cs = con.prepareCall(sql)) {
            cs.setInt(1, year);
            cs.setString(2, choice);

            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    String month = rs.getString("month_name"); // instead of "month_num"
                    double carValue = "qty".equals(choice) ? rs.getDouble("car_qty") : rs.getDouble("car_sales");
                    double partValue = "qty".equals(choice) ? rs.getDouble("part_qty") : rs.getDouble("part_sales");
                    result.add(new overviewBar(month, carValue, partValue));
                }

            }
        }
        return result;
    }

    public List<overviewLine> getOverviewLine(String choice, String start, String end) throws SQLException {
        List<overviewLine> result = new ArrayList<>();
        String sql = "CALL overview_totalrevenue(?, ?, ?)";

        try (CallableStatement stmt = con.prepareCall(sql)) {
            stmt.setString(1, choice);
            stmt.setString(2, (start == null || start.isEmpty()) ? null : start);
            stmt.setString(3, (end == null || end.isEmpty()) ? null : end);

            boolean hasResultSet = stmt.execute();
            if (hasResultSet) {
                try (ResultSet rs = stmt.getResultSet()) {
                    while (rs.next()) {
                        String dayLabel = rs.getString("day_label");
                        double totalSales = rs.getDouble("total_sales");
                        result.add(new overviewLine(dayLabel, totalSales));
                    }
                }
            }
        }
        return result;
    }

    public List<overviewPie> getOverviewPie(String category) throws SQLException {
        List<overviewPie> result = new ArrayList<>();
        String sql = "call overview_piechart(?)";
            CallableStatement cs = con.prepareCall(sql);
            cs.setString(1, category);
                ResultSet rs = cs.executeQuery();
                while (rs.next()) {
                    String label = rs.getString(1);
                    double totalSale = rs.getDouble(2);
                    result.add(new overviewPie(label, totalSale));
                }
        return result;
    }


}
