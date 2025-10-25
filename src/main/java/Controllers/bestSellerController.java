package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import Model.managerOverview;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.time.YearMonth;

public class bestSellerController {

    @FXML
    private ImageView image;

    @FXML
    private Label name;

    @FXML
    private Label rank;

    @FXML
    private Label revenueRate;

    @FXML
    private Label soldRevenue;

    public void setData(managerOverview overview, int month, int year) {
        name.setText(overview.getStaffName());
        rank.setText(overview.getRank());

        // Sales performance data
        double sale = overview.getTotalSale();
        double psale = overview.getPrevTotalSale();

        soldRevenue.setText(String.format("%.2f", sale));

        double revenueRateValue = 0;
        if (psale != 0) {
            revenueRateValue = ((sale - psale) / psale) * 100;
        }
        revenueRate.setText(String.format("%.2f%% from last month", revenueRateValue));


        String photoPath = overview.getStaffPhoto();

        // Load staff image with proper path resolution
        if (photoPath != null && !photoPath.trim().isEmpty()) {
            File imageFile = resolveImagePath(photoPath);
            if (imageFile != null && imageFile.exists() && imageFile.isFile()) {
                try {
                    Image staffImage = new Image(new FileInputStream(imageFile));
                    image.setImage(staffImage);
                } catch (FileNotFoundException e) {
                    System.err.println("Staff image file not found: " + photoPath);
                    // Load default image
                    image.setImage(new Image(getClass().getResource("/Image/usericon.png").toExternalForm()));
                }
            } else {
                System.err.println("Staff image file does not exist: " + photoPath);
                // Load default image
                image.setImage(new Image(getClass().getResource("/Image/usericon.png").toExternalForm()));
            }
        } else {
            // Load default image
            image.setImage(new Image(getClass().getResource("/Image/usericon.png").toExternalForm()));
        }
        
        // Make image clickable
        image.setCursor(javafx.scene.Cursor.HAND);
        image.setPickOnBounds(true);
    }
    
    @FXML
    private void clickStaffImage(MouseEvent event) {
        handleImageSelection();
    }
    
    /**
     * Opens a file chooser to select a new image for the staff photo
     * Similar to managerInventoryController's image selection functionality
     */
    private void handleImageSelection() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Staff Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(image.getScene().getWindow());
        if (selectedFile != null) {
            try {
                Image newImage = new Image(new FileInputStream(selectedFile));
                image.setImage(newImage);
                
                // TODO: Save the new image path to database if needed
                // You can add database update logic here
                System.out.println("New image selected: " + selectedFile.getAbsolutePath());
                
            } catch (FileNotFoundException e) {
                System.err.println("Error loading selected image: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Resolves image path - handles absolute, relative, and network (UNC) paths
     * Supports:
     * - Network paths: \\ServerName\SharedFolder\Images\staff.png
     * - Absolute paths: D:\Porsche\Images\staff.png
     * - Relative paths: Images/staff.png or /Images/staff.png
     */
    private File resolveImagePath(String photoPath) {
        if (photoPath == null || photoPath.trim().isEmpty()) {
            return null;
        }
        
        // Normalize path - replace backslashes with forward slashes
        photoPath = photoPath.replace("\\", "/");
        
        // Handle network (UNC) paths: //ServerName/SharedFolder/...
        if (photoPath.startsWith("//")) {
            File networkFile = new File(photoPath);
            if (networkFile.exists()) {
                return networkFile;
            } else {
                System.err.println("Network path not accessible: " + photoPath);
                return networkFile; // Return anyway for error handling
            }
        }
        
        File imageFile = new File(photoPath);
        
        // If it's already an absolute path and exists, return it
        if (imageFile.isAbsolute() && imageFile.exists()) {
            return imageFile;
        }
        
        // Get the project root directory (where src folder is located)
        String projectRoot = System.getProperty("user.dir");
        
        // Remove leading slash if present for relative path construction
        String relativePath = photoPath.startsWith("/") ? photoPath.substring(1) : photoPath;
        
        // Strategy 1: Try from project root (most common: Images/filename.jpg)
        File relativeFile = new File(projectRoot, relativePath);
        if (relativeFile.exists()) {
            return relativeFile;
        }
        
        // Strategy 2: If path doesn't start with "Images/", try prepending it
        if (!relativePath.startsWith("Images/")) {
            File withImagesPrefix = new File(projectRoot, "Images/" + relativePath);
            if (withImagesPrefix.exists()) {
                return withImagesPrefix;
            }
        }
        
        // Strategy 3: Try src/main/resources/Image/ (for bundled resources)
        File resourceFile = new File(projectRoot, "src/main/resources/Image/" + relativePath);
        if (resourceFile.exists()) {
            return resourceFile;
        }
        
        // If nothing works, return the original file object for error handling
        System.err.println("Could not resolve image path: " + photoPath);
        return imageFile;
    }
}
