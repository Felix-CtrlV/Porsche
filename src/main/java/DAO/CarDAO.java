package DAO;

import Database.DatabaseConnectionManager;
import Model.car;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CarDAO {

    public List<car> getAllCars() {
        List<car> cars = new ArrayList<>();
        String sql = "SELECT * FROM cars";

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                car c = new car();
                c.setCarid(rs.getInt("car_id"));
                c.setModelid(rs.getInt("model_id"));
                c.setColor(rs.getString("color"));
                c.setQuantity(rs.getInt("quantity"));
                c.setProduction_year(rs.getInt("production_year"));
                c.setCurrent_price(rs.getDouble("base_price"));
                c.setStatus(rs.getString("status"));
                cars.add(c);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cars;
    }

    public car getCarById(int id) {
        String sql = "SELECT * FROM cars WHERE car_id = ?";
        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                car c = new car();
                c.setCarid(rs.getInt("car_id"));
                c.setModelid(rs.getInt("model_id"));
                c.setColor(rs.getString("color"));
                c.setQuantity(rs.getInt("quantity"));
                c.setProduction_year(rs.getInt("production_year"));
                c.setCurrent_price(rs.getDouble("base_price"));
                c.setStatus(rs.getString("status"));
                return c;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void saveConfiguration(int carId, String color, String wheel, String interior, double totalPrice) {
        String sql = "INSERT INTO configurations (car_id, color, wheel, interior, total_price) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, carId);
            ps.setString(2, color);
            ps.setString(3, wheel);
            ps.setString(4, interior);
            ps.setDouble(5, totalPrice);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
