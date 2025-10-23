package DAO;

import Database.DatabaseConnectionManager;
import Model.Accessory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AccessoryDAO {
    private static final Logger logger = LoggerFactory.getLogger(AccessoryDAO.class);

    public List<Accessory> getAllAccessories() {
        List<Accessory> accessories = new ArrayList<>();
        String sql = "SELECT part_id, part_name, description, part_city, price, part_status, part_photo " +
                "FROM car_parts ORDER BY part_name";

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Accessory accessory = new Accessory(
                        rs.getInt("part_id"),
                        rs.getString("part_name"),
                        rs.getString("description"),
                        rs.getString("part_city"),
                        rs.getDouble("price"),
                        rs.getInt("part_status"),
                        rs.getString("part_photo")
                );
                accessories.add(accessory);
            }

        } catch (SQLException e) {
            logger.error("Failed to retrieve all accessories from database", e);
        }
        return accessories;
    }

    public Accessory getAccessoryById(int id) {
        String sql = "SELECT part_id, part_name, description, part_city, price, part_status, part_photo " +
                "FROM car_parts WHERE part_id = ?";

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Accessory(
                        rs.getInt("part_id"),
                        rs.getString("part_name"),
                        rs.getString("description"),
                        rs.getString("part_city"),
                        rs.getDouble("price"),
                        rs.getInt("part_status"),
                        rs.getString("part_photo")
                );
            }

        } catch (SQLException e) {
            logger.error("Failed to retrieve accessory with ID: " + id, e);
        }
        return null;
    }

    public List<Accessory> getAccessoriesByCategory(String category) {
        List<Accessory> accessories = new ArrayList<>();
        String sql = "SELECT part_id, part_name, description, part_city, price, part_status, part_photo " +
                "FROM car_parts WHERE part_city ILIKE ? ORDER BY part_name";

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + category + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Accessory accessory = new Accessory(
                        rs.getInt("part_id"),
                        rs.getString("part_name"),
                        rs.getString("description"),
                        rs.getString("part_city"),
                        rs.getDouble("price"),
                        rs.getInt("part_status"),
                        rs.getString("part_photo")
                );
                accessories.add(accessory);
            }

        } catch (SQLException e) {
            logger.error("Failed to retrieve accessories with category: " + category, e);
        }
        return accessories;
    }
}