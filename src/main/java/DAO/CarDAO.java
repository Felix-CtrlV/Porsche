package DAO;

import Database.DatabaseConnectionManager;
import Model.car;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CarDAO {
    private static final Logger logger = LoggerFactory.getLogger(CarDAO.class);

    public List<car> getAllCars() {
        List<car> cars = new ArrayList<>();
        String sql = "SELECT car_id, model_name, trim_name, car_color, interior_color, fuel_type, " +
                "car_status, car_speed, production_year, car_qty, price, car_description, car_photo " +
                "FROM cars ORDER BY model_name";

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                car c = new car(
                        rs.getInt("car_id"),
                        rs.getString("model_name"),
                        rs.getString("trim_name"),
                        rs.getString("car_color"),
                        rs.getString("interior_color"),
                        rs.getString("fuel_type"),
                        rs.getString("car_status"),
                        parseCarSpeed(rs.getString("car_speed")),
                        rs.getInt("production_year"),
                        rs.getString("car_qty"),
                        rs.getDouble("price"),
                        rs.getString("car_description"),
                        rs.getString("car_photo")
                );
                cars.add(c);
            }

        } catch (SQLException e) {
            logger.error("Failed to retrieve all cars from database", e);
        }
        return cars;
    }

    public car getCarById(int id) {
        String sql = "SELECT car_id, model_name, trim_name, car_color, interior_color, fuel_type, " +
                "car_status, car_speed, production_year, car_qty, price, car_description, car_photo " +
                "FROM cars WHERE car_id = ?";

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new car(
                        rs.getInt("car_id"),
                        rs.getString("model_name"),
                        rs.getString("trim_name"),
                        rs.getString("car_color"),
                        rs.getString("interior_color"),
                        rs.getString("fuel_type"),
                        rs.getString("car_status"),
                        parseCarSpeed(rs.getString("car_speed")),
                        rs.getInt("production_year"),
                        rs.getString("car_qty"),
                        rs.getDouble("price"),
                        rs.getString("car_description"),
                        rs.getString("car_photo")
                );
            }

        } catch (SQLException e) {
            logger.error("Failed to retrieve car with ID: " + id, e);
        }
        return null;
    }

    public List<car> getCarsByStatus(String status) {
        List<car> cars = new ArrayList<>();
        String sql = "SELECT car_id, model_name, trim_name, car_color, interior_color, fuel_type, " +
                "car_status, car_speed, production_year, car_qty, price, car_description, car_photo " +
                "FROM cars WHERE car_status = ? ORDER BY model_name";

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                car c = new car(
                        rs.getInt("car_id"),
                        rs.getString("model_name"),
                        rs.getString("trim_name"),
                        rs.getString("car_color"),
                        rs.getString("interior_color"),
                        rs.getString("fuel_type"),
                        rs.getString("car_status"),
                        parseCarSpeed(rs.getString("car_speed")),
                        rs.getInt("production_year"),
                        rs.getString("car_qty"),
                        rs.getDouble("price"),
                        rs.getString("car_description"),
                        rs.getString("car_photo")
                );
                cars.add(c);
            }

        } catch (SQLException e) {
            logger.error("Failed to retrieve cars with status: " + status, e);
        }
        return cars;
    }

    public List<car> getCarsByModelName(String modelName) {
        List<car> cars = new ArrayList<>();

        String sql = "SELECT car_id, model_name, trim_name, car_color, interior_color, fuel_type, " +
                "car_status, car_speed, production_year, car_qty, price, car_description, car_photo " +
                "FROM cars WHERE model_name LIKE ? OR trim_name LIKE ? " +
                "ORDER BY model_name, trim_name";

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String searchPattern = "%" + modelName + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);

            logger.info("Searching for cars with pattern: {}", searchPattern);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                car c = new car(
                        rs.getInt("car_id"),
                        rs.getString("model_name"),
                        rs.getString("trim_name"),
                        rs.getString("car_color"),
                        rs.getString("interior_color"),
                        rs.getString("fuel_type"),
                        rs.getString("car_status"),
                        parseCarSpeed(rs.getString("car_speed")),
                        rs.getInt("production_year"),
                        rs.getString("car_qty"),
                        rs.getDouble("price"),
                        rs.getString("car_description"),
                        rs.getString("car_photo")
                );
                cars.add(c);
                logger.debug("Found car: {} {}", c.getModelName(), c.getTrimName());
            }

            logger.info("Found {} cars matching '{}'", cars.size(), modelName);

        } catch (SQLException e) {
            logger.error("Failed to retrieve cars with model name: " + modelName, e);
        }
        return cars;
    }

    public boolean updateCarStatus(int carId, String newStatus) {
        String sql = "UPDATE cars SET car_status = ? WHERE car_id = ?";

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setInt(2, carId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Updated car status for car ID: {} to {}", carId, newStatus);
                return true;
            }

        } catch (SQLException e) {
            logger.error("Failed to update car status for ID: " + carId, e);
        }
        return false;
    }

    public boolean updateCarQuantity(int carId, int newQuantity) {
        String sql = "UPDATE cars SET car_qty = ? WHERE car_id = ?";

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, newQuantity);
            ps.setInt(2, carId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Updated car quantity for car ID: {} to {}", carId, newQuantity);
                return true;
            }

        } catch (SQLException e) {
            logger.error("Failed to update car quantity for ID: " + carId, e);
        }
        return false;
    }

    public void saveConfiguration(int carId, String color, String interiorColor, String wheel, String interior, double totalPrice) {
        String sql = "INSERT INTO car_configurations (car_id, selected_color, selected_interior_color, selected_wheel, selected_interior, total_price, configuration_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, carId);
            ps.setString(2, color);
            ps.setString(3, interiorColor);
            ps.setString(4, wheel);
            ps.setString(5, interior);
            ps.setDouble(6, totalPrice);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Saved configuration for car ID: {}", carId);
            }

        } catch (SQLException e) {
            logger.error("Failed to save configuration for car ID: " + carId, e);
        }
    }

    public int insertCar(car newCar) {
        String sql = "INSERT INTO cars (model_name, trim_name, car_color, interior_color, fuel_type, " +
                "car_status, car_speed, production_year, car_qty, price, car_description, car_photo) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, newCar.getModelName());
            ps.setString(2, newCar.getTrimName());
            ps.setString(3, newCar.getCarColor());
            ps.setString(4, newCar.getInteriorColor());
            ps.setString(5, newCar.getFuelType());
            ps.setString(6, newCar.getCarStatus());
            ps.setLong(7, newCar.getCarSpeed());
            ps.setInt(8, newCar.getProductionYear());
            ps.setString(9, newCar.getCarQty());
            ps.setDouble(10, newCar.getPrice());
            ps.setString(11, newCar.getCarDescription());
            ps.setString(12, newCar.getCarPhoto());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    logger.info("Inserted new car with ID: {}", generatedId);
                    return generatedId;
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to insert new car", e);
        }
        return -1;
    }

    public boolean updateCar(car updatedCar) {
        String sql = "UPDATE cars SET model_name = ?, trim_name = ?, car_color = ?, interior_color = ?, " +
                "fuel_type = ?, car_status = ?, car_speed = ?, production_year = ?, car_qty = ?, " +
                "price = ?, car_description = ?, car_photo = ? WHERE car_id = ?";

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, updatedCar.getModelName());
            ps.setString(2, updatedCar.getTrimName());
            ps.setString(3, updatedCar.getCarColor());
            ps.setString(4, updatedCar.getInteriorColor());
            ps.setString(5, updatedCar.getFuelType());
            ps.setString(6, updatedCar.getCarStatus());
            ps.setLong(7, updatedCar.getCarSpeed());
            ps.setInt(8, updatedCar.getProductionYear());
            ps.setString(9, updatedCar.getCarQty());
            ps.setDouble(10, updatedCar.getPrice());
            ps.setString(11, updatedCar.getCarDescription());
            ps.setString(12, updatedCar.getCarPhoto());
            ps.setInt(13, updatedCar.getCarId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Updated car with ID: {}", updatedCar.getCarId());
                return true;
            }

        } catch (SQLException e) {
            logger.error("Failed to update car with ID: " + updatedCar.getCarId(), e);
        }
        return false;
    }

    public boolean deleteCar(int carId) {
        String sql = "DELETE FROM cars WHERE car_id = ?";

        try (Connection conn = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, carId);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Deleted car with ID: {}", carId);
                return true;
            }

        } catch (SQLException e) {
            logger.error("Failed to delete car with ID: " + carId, e);
        }
        return false;
    }

    private long parseCarSpeed(String speedString) {
        if (speedString == null || speedString.trim().isEmpty()) {
            return 0L;
        }
        try {
            String cleanSpeed = speedString.replace("km/h", "").trim();
            return Long.parseLong(cleanSpeed);
        } catch (NumberFormatException e) {
            logger.warn("Failed to parse car speed: '{}', using default 0", speedString);
            return 0L;
        }
    }
}