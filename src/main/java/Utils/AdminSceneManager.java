package Utils;

import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;
import java.io.IOException;

/**
 * Admin Scene Manager utility class for applying admin light mode CSS
 * to scenes and connecting controllers to the consolidated CSS file.
 */
public class AdminSceneManager {
    
    /**
     * Apply admin light mode CSS to a scene
     * This method should be called when creating admin-related scenes
     * 
     * @param scene The scene to apply CSS to
     */
    public static void applyAdminLightMode(Scene scene) {
        // Add the consolidated admin light mode CSS
        scene.getStylesheets().add(
            AdminSceneManager.class.getResource("/CSS/admin_light_mode.css").toExternalForm()
        );
    }
    
    /**
     * Load an FXML file and apply admin light mode CSS
     * This is a convenience method for loading admin FXML files with CSS applied
     * 
     * @param fxmlPath The path to the FXML file
     * @return The loaded Parent with CSS applied
     * @throws IOException If the FXML file cannot be loaded
     */
    public static Parent loadAdminFXML(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(AdminSceneManager.class.getResource(fxmlPath));
        Parent root = loader.load();
        
        // The CSS will be applied when the scene is created
        return root;
    }
    
    /**
     * Create a scene with admin light mode CSS applied
     * 
     * @param root The root node for the scene
     * @param width The width of the scene
     * @param height The height of the scene
     * @return A new Scene with admin light mode CSS applied
     */
    public static Scene createAdminScene(Parent root, double width, double height) {
        Scene scene = new Scene(root, width, height);
        applyAdminLightMode(scene);
        return scene;
    }
    
    /**
     * Create a scene with admin light mode CSS applied (no size specified)
     * 
     * @param root The root node for the scene
     * @return A new Scene with admin light mode CSS applied
     */
    public static Scene createAdminScene(Parent root) {
        Scene scene = new Scene(root);
        applyAdminLightMode(scene);
        return scene;
    }
}
