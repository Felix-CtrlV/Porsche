package Controllers;

import Database.DatabaseConnectionManager;
import Model.car;
import Utils.SessionStaff;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class staffCarcardController {
    private static final Logger logger = LoggerFactory.getLogger(staffCarcardController.class);

    @FXML
    private ImageView model_image;

    @FXML
    private Label model_name;

    @FXML
    private Label model_price;

    private String imagePath;
    private String carName;
    private String carPrice;
    private int carId;
    private car selectedCar;

    public void setCarData(String name, String imagePath, String price) {
        this.carName = name;
        this.imagePath = imagePath;
        this.carPrice = price;

        if (model_name != null) model_name.setText(name);
        if (model_price != null) model_price.setText(price);

        if (imagePath != null && model_image != null) {
            loadImage(imagePath);
        }
    }

    public void setCarDataFromDatabase(int carId) {
        this.carId = carId;
        loadCarFromDatabase(carId);
    }

    public void setCarData(car car) {
        if (car == null) {
            logger.error("Car object is null!");
            return;
        }

        this.selectedCar = car;
        this.carId = car.getCarId();
        this.carName = car.getFullName();
        this.carPrice = "$" + String.format("%,.0f", car.getPrice());
        this.imagePath = car.getCarPhoto();

        logger.info("Setting car data - ID: {}, Name: {}, Price: {}, ImagePath: {}", carId, carName, carPrice, imagePath);

        if (model_name != null) {
            model_name.setText(carName);
            logger.debug("Set model name to: {}", carName);
        } else {
            logger.warn("model_name label is null!");
        }

        if (model_price != null) {
            model_price.setText(carPrice);
            logger.debug("Set model price to: {}", carPrice);
        } else {
            logger.warn("model_price label is null!");
        }

        if (model_image != null) {
            if (imagePath != null && !imagePath.isEmpty()) {
                loadImage(imagePath);
            } else {
                logger.warn("Image path is null or empty for car: {}", carName);
                loadPlaceholderImage();
            }
        } else {
            logger.warn("model_image ImageView is null!");
        }
    }

    private void loadCarFromDatabase(int carId) {
        String sql = "SELECT car_id, model_name, trim_name, car_color, interior_color, fuel_type, car_status, " +
                "car_speed, production_year, car_city, price, car_description, car_photo " +
                "FROM cars WHERE car_id = ?";

        try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, carId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                this.carId = rs.getInt("car_id");
                String modelName = rs.getString("model_name");
                String trimName = rs.getString("trim_name");
                this.carName = modelName + (trimName != null ? " " + trimName : "");
                double price = rs.getDouble("price");
                this.carPrice = "$" + String.format("%,.2f", price);
                this.imagePath = rs.getString("car_photo");

                this.selectedCar = new car(
                        rs.getInt("car_id"),
                        rs.getString("model_name"),
                        rs.getString("trim_name"),
                        rs.getString("car_color"),
                        rs.getString("interior_color"),
                        rs.getString("fuel_type"),
                        rs.getString("car_status"),
                        rs.getLong("car_speed"),
                        rs.getInt("production_year"),
                        rs.getString("car_city"),
                        price,
                        rs.getString("car_description"),
                        rs.getString("car_photo")
                );

                if (model_name != null) model_name.setText(carName);
                if (model_price != null) model_price.setText(carPrice);
                if (imagePath != null && model_image != null) {
                    loadImage(imagePath);
                }

                logger.info("Loaded car from database: {} (ID: {})", carName, carId);
            } else {
                logger.warn("No car found with ID: {}", carId);
            }

        } catch (SQLException e) {
            logger.error("Failed to load car from database with ID: " + carId, e);
        }
    }

    private void loadImage(String imagePath) {
        logger.info("Attempting to load image from path: {}", imagePath);

        try (InputStream stream = getClass().getResourceAsStream(imagePath)) {
            if (stream != null) {
                Image image = new Image(stream);
                model_image.setImage(image);
                logger.info("Image loaded successfully: {}", imagePath);
            } else {
                logger.warn("Image stream is null for path: {}. Trying placeholder.", imagePath);
                loadPlaceholderImage();
            }
        } catch (Exception e) {
            logger.error("Exception while loading image: " + imagePath, e);
            loadPlaceholderImage();
        }
    }

    private void loadPlaceholderImage() {
        String placeholderPath = "/Image/placeholder_car.png";
        logger.info("Loading placeholder image: {}", placeholderPath);

        try (InputStream stream = getClass().getResourceAsStream(placeholderPath)) {
            if (stream != null) {
                Image image = new Image(stream);
                model_image.setImage(image);
                logger.info("Placeholder image loaded successfully");
            } else {
                logger.error("Placeholder image not found at: {}", placeholderPath);
            }
        } catch (Exception e) {
            logger.error("Failed to load placeholder image", e);
        }
    }

    @FXML
    private void handleCardClick(MouseEvent event) {
        logger.info("Selected Car: {} | Price: {} | ID: {}", carName, carPrice, carId);

        if (selectedCar != null) {
            logger.debug("Car object available for customization: {}", selectedCar.getModelName());
        }
    }

    @FXML
    public void redirectToCustomize(ActionEvent actionEvent) {
        try {
            if (selectedCar != null) {
                logger.info("Navigating to customize page with car: {}", selectedCar.getModelName());
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/staffCustomize.fxml"));
            Parent root = loader.load();

            staffCustomizeController controller = loader.getController();
            if (controller != null && selectedCar != null) {
                controller.setSelectedCar(selectedCar);
            }

            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 1376, 768);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            logger.error("Unable to load staffCustomize.fxml", e);
            e.printStackTrace();
        }
    }

    public String getCarName() {
        return carName;
    }

    public String getCarPrice() {
        return carPrice;
    }

    public int getCarId() {
        return carId;
    }

    public car getSelectedCar() {
        return selectedCar;
    }

    public String getImagePath() {
        return imagePath;
    }
}