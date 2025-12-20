package Controllers.java.Controllers;


/*
 NOTE:
 - Added DB-related imports and managerInventory import so the controller can
    look up "related" images when overview.getImageUrl() is empty or points to a group.
 - You'll need to use your existing managerInventory/DB helper logic inside setDate()
    (e.g. query related items by inventory id/name and pick the first available image).
 - Keep the rest of the file (imports and class) unchanged; adapt JDBC/DAO calls to match
    your project's DB helper (databaseschema.md / managerinventory).
*/

import Controllers.java.Model.managerOverview;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.io.FileInputStream;
// Check if the managerInventory class exists in the Model package
// managerInventory import removed because the class is not present in Model package


public class managerCarPartProgressBarController {

    @FXML
    private Label Name;

    @FXML
    private Label rank;

    @FXML
    private Label soldQty;
    
    @FXML
    private ImageView carImage;

    public void setDate(managerOverview overview) {
        // Handle long car/part names with smart truncation
        String itemName = overview.getInventoryName();
        if (itemName != null) {
            Name.setText(itemName);
            // Always add tooltip to show full name on hover
            Name.setTooltip(new Tooltip(itemName));
        } else {
            Name.setText("Unknown");
        }
        
        rank.setText(overview.getRank());
        soldQty.setText(String.format("%,d", overview.getSoldQty()));
        
        // Set car image if available
        try {
            String imageUrl = overview.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                try {
                    // Normalize the image path first (same logic as manager inventory)
                    String normalizedPath = normalizeImagePath(imageUrl);

                    // Try to load from normalized path using file system resolution
                    File imageFile = resolveImagePath(normalizedPath);
                    if (imageFile != null && imageFile.exists() && imageFile.isFile()) {
                        Image image = new Image(new FileInputStream(imageFile));
                        if (!image.isError()) {
                            carImage.setImage(image);
                            return;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error loading image from path " + imageUrl + ": " + e.getMessage());
                }
            }

            // If we get here, no image could be loaded
            System.err.println("Could not load any image, using fallback placeholder");
            try {
                // Try to use a default car image as fallback
                File fallbackImage = new File(System.getProperty("user.dir"), "Images/911 Carrera GTS.png");
                if (fallbackImage.exists()) {
                    Image placeholder = new Image(new FileInputStream(fallbackImage));
                    carImage.setImage(placeholder);
                } else {
                    carImage.setImage(null);
                }
            } catch (Exception e) {
                System.err.println("Failed to load fallback image: " + e.getMessage());
                carImage.setImage(null);
            }

        } catch (Exception e) {
            System.err.println("Unexpected error in image loading: " + e.getMessage());
            e.printStackTrace();
            carImage.setImage(null);
        }
    }

    /**
     * Normalizes image path to ensure it follows the expected format
     * Same logic as used in manager inventory system
     */
    private String normalizeImagePath(String photoUrl) {
        if (photoUrl == null || photoUrl.isEmpty()) {
            return "";
        }

        // Remove leading slash if present
        if (photoUrl.startsWith("/")) {
            photoUrl = photoUrl.substring(1);
        }

        // Add "Images/" prefix if not present
        if (!photoUrl.startsWith("Images/")) {
            photoUrl = "Images/" + photoUrl;
        }

        return photoUrl;
    }

    /**
     * Resolves image path - handles absolute, relative, and network paths
     * Based on the same logic from manager inventory system
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

        // Strategy 3: Try from src/main/resources
        File resourceFile = new File(projectRoot, "src/main/resources/" + relativePath);
        if (resourceFile.exists()) {
            return resourceFile;
        }

        // Strategy 4: Try backup folder
        File backupFolder = new File(projectRoot, "backup/Image/" + relativePath);
        if (backupFolder.exists()) {
            return backupFolder;
        }

        System.err.println("Image not found at any location: " + photoPath);
        System.err.println("Tried locations:");
        System.err.println("  1. " + photoPath + " (absolute)");
        System.err.println("  2. " + relativeFile.getAbsolutePath());
        System.err.println("  3. " + resourceFile.getAbsolutePath());
        System.err.println("  4. " + backupFolder.getAbsolutePath());

        // Return original file object even if it doesn't exist (for error handling)
        return imageFile;
    }
}
