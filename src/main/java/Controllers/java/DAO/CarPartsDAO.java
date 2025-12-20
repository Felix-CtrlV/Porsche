package Controllers.java.DAO;

import Database.DatabaseConnectionManager;
import Model.CarPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CarPartsDAO {

    private static final Logger logger = LoggerFactory.getLogger(CarPartsDAO.class);

    public List<CarPart> getAllParts() {
        List<CarPart> parts = new ArrayList<>();
        String sql = "SELECT part_id, part_name, for_car, description, part_qty, price, part_status, part_photo FROM car_parts ORDER BY part_name";

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                CarPart part = new CarPart(
                        rs.getInt("part_id"),
                        rs.getString("part_name"),
                        rs.getInt("for_car"),
                        rs.getString("description"),
                        rs.getInt("part_qty"),
                        rs.getDouble("price"),
                        rs.getInt("part_status"),
                        rs.getString("part_photo")
                );
                parts.add(part);
            }

        } catch (SQLException e) {
            logger.error("Failed to retrieve car parts", e);
        }

        return parts;
    }

    public CarPart getPartById(int id) {
        String sql = "SELECT part_id, part_name, for_car, description, part_qty, price, part_status, part_photo FROM car_parts WHERE part_id = ?";

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new CarPart(
                        rs.getInt("part_id"),
                        rs.getString("part_name"),
                        rs.getInt("for_car"),
                        rs.getString("description"),
                        rs.getInt("part_qty"),
                        rs.getDouble("price"),
                        rs.getInt("part_status"),
                        rs.getString("part_photo")
                );
            }

        } catch (SQLException e) {
            logger.error("Failed to retrieve part by ID " + id, e);
        }

        return null;
    }

    public List<CarPart> getPartsByCarId(int carId) {
        List<CarPart> parts = new ArrayList<>();
        String sql = "SELECT part_id, part_name, for_car, description, part_qty, price, part_status, part_photo FROM car_parts WHERE for_car = ?";

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, carId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                CarPart part = new CarPart(
                        rs.getInt("part_id"),
                        rs.getString("part_name"),
                        rs.getInt("for_car"),
                        rs.getString("description"),
                        rs.getInt("part_qty"),
                        rs.getDouble("price"),
                        rs.getInt("part_status"),
                        rs.getString("part_photo")
                );
                parts.add(part);
            }

        } catch (SQLException e) {
            logger.error("Failed to retrieve parts for car ID: " + carId, e);
        }

        return parts;
    }

    public boolean updatePartQuantity(int partId, int newQuantity) {
        String sql = "UPDATE car_parts SET part_qty = ? WHERE part_id = ?";

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, newQuantity);
            ps.setInt(2, partId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Updated part quantity for part ID: {} to {}", partId, newQuantity);
                return true;
            }

        } catch (SQLException e) {
            logger.error("Failed to update part quantity for ID: " + partId, e);
        }
        return false;
    }

    public int getPartQuantity(int partId) {
        String sql = "SELECT part_qty FROM car_parts WHERE part_id = ?";

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, partId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("part_qty");
            }

        } catch (SQLException e) {
            logger.error("Failed to get part quantity for ID: " + partId, e);
        }
        return 0;
    }

    public boolean decreasePartQuantity(int partId) {
        String sql = "UPDATE car_parts SET part_qty = part_qty - 1 WHERE part_id = ? AND part_qty > 0";

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, partId);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Decreased part quantity for part ID: {}", partId);
                return true;
            }

        } catch (SQLException e) {
            logger.error("Failed to decrease part quantity for ID: " + partId, e);
        }
        return false;
    }
}