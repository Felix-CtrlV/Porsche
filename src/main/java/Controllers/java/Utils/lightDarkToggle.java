package Controllers.java.Utils;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Parent;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.util.Duration;

public class lightDarkToggle {
    private static lightDarkToggle instance;

    private  final BooleanProperty darkModeProperty = new SimpleBooleanProperty(false);

    public BooleanProperty darkModeProperty() {
        return darkModeProperty;
    }

    public boolean isDarkMode() {
        return darkModeProperty.get();
    }
    private lightDarkToggle() {
    }
    public static lightDarkToggle getInstance() {
        if (instance == null) instance = new lightDarkToggle();
        return instance;
    }


    private  Parent parent = null;
    private  String lighCss = null;
    private String darkCss = null;

    public void register(Parent root, String lightCss, String darkCss) {
        this.parent = root;
        this.lighCss = lightCss;
        this.darkCss = darkCss;
        darkModeProperty.setValue(false);
        root.getStylesheets().clear();
        String css = darkModeProperty.get() ? darkCss : lightCss;
        root.getStylesheets().add(getClass().getResource(css).toExternalForm());
    }

    public void toggleTheme() {
        darkModeProperty.set(!darkModeProperty.get());
        applyThemeSmooth();
    }

    private void applyThemeSmooth() {
        String css = darkModeProperty.get() ? darkCss : lighCss;

        Region region = new Region();
        region.setPrefSize(parent.getBoundsInLocal().getWidth(), parent.getBoundsInLocal().getHeight());
        region.setManaged(false);
        region.setMouseTransparent(true);
        region.setOpacity(0);

        if(parent instanceof Pane pane) {
           pane.getChildren().add(region);
            region.toFront();
        }

        GaussianBlur blur = new GaussianBlur(0);
        parent.setEffect(blur);

        Timeline blurIn = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(blur.radiusProperty(), 0, Interpolator.EASE_BOTH),
                        new KeyValue(region.opacityProperty(), 0, Interpolator.EASE_BOTH)
                ),
                new KeyFrame(Duration.millis(250),
                        new KeyValue(blur.radiusProperty(), 5, Interpolator.EASE_BOTH),
                        new KeyValue(region.opacityProperty(), 0.25, Interpolator.EASE_BOTH)
                )

        );
        blurIn.setOnFinished(event -> {
            parent.getStylesheets().clear();
            parent.getStylesheets().add(getClass().getResource(css).toExternalForm());

            Timeline blurOut = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(blur.radiusProperty(), 5, Interpolator.EASE_BOTH),
                            new KeyValue(region.opacityProperty(), 0.25, Interpolator.EASE_BOTH)
                    ),
                    new KeyFrame(Duration.millis(250),
                            new KeyValue(blur.radiusProperty(), 0, Interpolator.EASE_BOTH),
                            new KeyValue(region.opacityProperty(), 0, Interpolator.EASE_BOTH)
                    )
            );
            blurOut.setOnFinished(e -> {
                parent.setEffect(null);
                if (parent instanceof Pane p) {
                    p.getChildren().remove(region);
                }
            });
            blurOut.play();
        });
        blurIn.play();
    }
}
