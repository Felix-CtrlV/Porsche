package Controllers.java.Utils;

import javafx.scene.Scene;

import java.util.ArrayList;
import java.util.List;

public class DarkModeManager {
    private static DarkModeManager instance;
    private boolean isDarkMode = false;
    private List<Scene> registeredScenes = new ArrayList<>();

    private DarkModeManager() {}

    public static DarkModeManager getInstance() {
        if (instance == null) {
            instance = new DarkModeManager();
        }
        return instance;
    }

    public void registerScene(Scene scene) {
        if (!registeredScenes.contains(scene)) {
            registeredScenes.add(scene);
            applyTheme(scene);
        }
    }

    public void toggleDarkMode() {
        isDarkMode = !isDarkMode;
        applyThemeToAll();
    }

    public void setDarkMode(boolean darkMode) {
        if (this.isDarkMode != darkMode) {
            this.isDarkMode = darkMode;
            applyThemeToAll();
        }
    }

    public boolean isDarkMode() {
        return isDarkMode;
    }

    private void applyThemeToAll() {
        for (Scene scene : registeredScenes) {
            applyTheme(scene);
        }
    }

    private void applyTheme(Scene scene) {
        if (scene != null && scene.getRoot() != null) {
            if (isDarkMode) {
                scene.getRoot().getStyleClass().add("dark-mode");
            } else {
                scene.getRoot().getStyleClass().remove("dark-mode");
            }
        }
    }

    public void unregisterScene(Scene scene) {
        registeredScenes.remove(scene);
    }
}