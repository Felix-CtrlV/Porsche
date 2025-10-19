package Controllers;

import java.io.File;
import java.net.URL;
import java.util.Optional;

/**
 * Resolves stored user_photo identifiers to actual resource or file paths.
 * Legacy databases may store numeric IDs referencing bundled assets.
 */
public final class UserPhotoResolver {

    private UserPhotoResolver() {}

    public static Optional<String> resolve(String photoValue) {
        if (photoValue == null || photoValue.isBlank()) {
            return Optional.empty();
        }

        String value = photoValue.trim();

        if (value.startsWith("http://") || value.startsWith("https://") || value.startsWith("file:")) {
            return Optional.of(value);
        }

        // Normalize backslashes
        value = value.replace("\\", "/");

        File resolved = resolveUsingInventoryStrategy(value);
        if (resolved != null) {
            if (resolved.exists()) {
                return Optional.of(resolved.toURI().toString());
            } else if (resolved.isAbsolute()) {
                return Optional.of(resolved.toURI().toString());
            }
        }

        String normalized = value.startsWith("/") ? value : "/" + value;
        URL resource = UserPhotoResolver.class.getResource(normalized);
        if (resource != null) {
            return Optional.of(resource.toExternalForm());
        }

        return Optional.of(value);
    }

    private static File resolveUsingInventoryStrategy(String photoPath) {
        if (photoPath == null || photoPath.trim().isEmpty()) {
            return null;
        }

        String value = photoPath.replace("\\", "/");

        if (value.startsWith("//")) {
            return new File(value);
        }

        File imageFile = new File(value);
        if (imageFile.isAbsolute() && imageFile.exists()) {
            return imageFile;
        }

        String projectRoot = System.getProperty("user.dir");
        String relativePath = value.startsWith("/") ? value.substring(1) : value;

        File relativeFile = new File(projectRoot, relativePath);
        if (relativeFile.exists()) {
            return relativeFile;
        }

        if (!relativePath.startsWith("Images/")) {
            File withImagesPrefix = new File(projectRoot, "Images/" + relativePath);
            if (withImagesPrefix.exists()) {
                return withImagesPrefix;
            }
        }

        File resourceFile = new File(projectRoot, "src/main/resources/" + relativePath);
        if (resourceFile.exists()) {
            return resourceFile;
        }

        File backupFolder = new File(projectRoot, "backup/Image/" + relativePath);
        if (backupFolder.exists()) {
            return backupFolder;
        }

        return imageFile;
    }
}
