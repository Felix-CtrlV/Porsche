package DAO;

import Database.DatabaseConnectionManager;
import Model.car;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CarDAO {
    public List<car> getAllCars() {
        List<car> cars = new ArrayList<>();
        String query = "SELECT carid, modelid, category, current_price FROM cars";
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                cars.add(new car(
                        rs.getInt("carid"),
                        rs.getInt("modelid"),
                        rs.getString("category"),
                        rs.getDouble("current_price")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cars;
    }

    public car getCarById(int carid) {
        String query = "SELECT * FROM cars WHERE carid = ?";
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, carid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new car(
                        rs.getInt("carid"),
                        rs.getInt("modelid"),
                        rs.getString("category"),
                        rs.getDouble("current_price")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
