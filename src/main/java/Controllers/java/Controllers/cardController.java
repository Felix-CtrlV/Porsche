package Controllers.java.Controllers;

import Controllers.UserPhotoResolver;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class cardController {

    @FXML
    private Label StaffId;

    @FXML
    private Label StaffName;

    @FXML
    private Circle ActiveColor;

    @FXML
    private ImageView StaffImage;

    private int staffId;

    private static final Map<String, Image> IMAGE_CACHE = new ConcurrentHashMap<>();

    public void setData(int id, String name, String isActive, String imagePath) {
        this.staffId = id;
        StaffId.setText(String.valueOf(id));
        StaffName.setText(name);
        ActiveColor.setFill((isActive.toLowerCase().equals("active")) ? Color.web("#10b981") : Color.web("#ef4444"));

        if (StaffImage != null) {
            StaffImage.setImage(loadImage(imagePath));
            applyCircularClip(StaffImage);
        }
    }

    public int getStaffId() {
        return staffId;
    }

    private Image loadImage(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {
            return null;
        }

        String key = imagePath.trim();

        // Allow legacy numeric IDs by resolving via photo resolver
        Optional<String> resolvedPath = UserPhotoResolver.resolve(key);
        if (resolvedPath.isPresent()) {
            key = resolvedPath.get();
        }

        Image cached = IMAGE_CACHE.get(key);
        if (cached != null) {
            return cached;
        }

        Image resolved = resolveImage(key);
        if (resolved != null && !resolved.isError()) {
            IMAGE_CACHE.put(key, resolved);
            return resolved;
        }

        return null;
    }

    private void applyCircularClip(ImageView imageView) {
        double radius = Math.min(imageView.getFitWidth(), imageView.getFitHeight()) / 2.0;
        Circle clip = new Circle(radius, radius, radius);
        imageView.setClip(clip);
    }

    private Image resolveImage(String key) {
        Image image = tryLoadResourceImage(key);
        if (image != null && !image.isError()) {
            return image;
        }

        image = tryLoadFileOrUri(key);
        if (image != null && !image.isError()) {
            return image;
        }

        return null;
    }

    private Image tryLoadResourceImage(String path) {
        String normalized = path;
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }

        Image image = loadResource(normalized);
        if (image != null && !image.isError()) {
            return image;
        }

        // Try common extensions if missing
        if (!normalized.matches(".*\\.(png|jpg|jpeg)$")) {
            for (String ext : new String[]{".png", ".jpg", ".jpeg"}) {
                image = loadResource(normalized + ext);
                if (image != null && !image.isError()) {
                    return image;
                }
            }
        }
        return null;
    }

    private Image loadResource(String resourcePath) {
        URL resource = getClass().getResource(resourcePath);
        if (resource == null) {
            return null;
        }
        return new Image(resource.toExternalForm(), false);
    }

    private Image tryLoadFileOrUri(String imagePath) {
        try {
            if (imagePath.startsWith("http://") || imagePath.startsWith("https://") || imagePath.startsWith("file:")) {
                return new Image(imagePath, false);
            }

            File file = new File(imagePath);
            if (file.exists()) {
                return new Image(file.toURI().toString(), false);
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
