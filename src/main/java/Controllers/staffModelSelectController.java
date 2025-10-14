package Controllers;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class staffModelSelectController implements Initializable {

    @FXML
    private ScrollPane scrollPane;
    @FXML
    private FlowPane flow_Pane;

    @FXML
    private Button back_btn;
    @FXML
    private Button theme_toggle_btn;

    private boolean isDarkMode = false;
    private List<ModelData> availableModels;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupScrollPane();
        loadModels();
        populateModelCards();
        animateCardsOnLoad();
    }

    private void setupScrollPane() {
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.viewportBoundsProperty().addListener((observable, oldValue, newValue) -> {
            flow_Pane.setPrefWrapLength(newValue.getWidth());
        });
    }

    private void loadModels() {
        availableModels = new ArrayList<>();
        availableModels.add(new ModelData("911 Carrera", "The icon. Forever.", "3.0L Twin-Turbo Flat-6", "379 HP", "4.0s", "293 km/h", "From €120,125", "/images/911-carrera.png"));
        availableModels.add(new ModelData("911 Turbo S", "Performance elevated.", "3.8L Twin-Turbo Flat-6", "640 HP", "2.7s", "330 km/h", "From €230,894", "/images/911-turbo-s.png"));
        availableModels.add(new ModelData("Taycan", "Soul, electrified.", "Electric Dual Motor", "up to 761 HP", "2.8s", "260 km/h", "From €90,607", "/images/taycan.png"));
        availableModels.add(new ModelData("Cayenne", "Unmistakable presence.", "3.0L Turbo V6", "340 HP", "6.2s", "245 km/h", "From €79,822", "/images/cayenne.png"));
        availableModels.add(new ModelData("Panamera", "Gran Turismo perfected.", "2.9L Twin-Turbo V6", "330 HP", "5.6s", "263 km/h", "From €95,628", "/images/panamera.png"));
        availableModels.add(new ModelData("Macan", "Compact. Dynamic. Sporty.", "2.0L Turbo I4", "265 HP", "6.6s", "227 km/h", "From €60,944", "/images/macan.png"));
        availableModels.add(new ModelData("718 Cayman", "Mid-engine precision.", "2.0L Turbo Flat-4", "300 HP", "5.1s", "275 km/h", "From €62,231", "/images/718-cayman.png"));
        availableModels.add(new ModelData("718 Boxster", "Open-top exhilaration.", "2.0L Turbo Flat-4", "300 HP", "5.1s", "275 km/h", "From €64,559", "/images/718-boxster.png"));
    }

    private void populateModelCards() {
        flow_Pane.getChildren().clear();
        for (ModelData model : availableModels) {
            VBox card = createModelCard(model);
            card.setOpacity(0);
            flow_Pane.getChildren().add(card);
        }
    }

    private VBox createModelCard(ModelData model) {
        VBox card = new VBox();
        card.getStyleClass().add("model-card");
        card.setPrefWidth(340);
        card.setPrefHeight(420);

        VBox imageContainer = new VBox();
        imageContainer.getStyleClass().add("model-card-image-container");
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setPrefHeight(240);

        try {
            ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream(model.imagePath)));
            imageView.setFitWidth(340);
            imageView.setFitHeight(240);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            imageView.getStyleClass().add("model-card-image");
            imageContainer.getChildren().add(imageView);
        } catch (Exception e) {
            Label placeholder = new Label("IMAGE");
            placeholder.setStyle("-fx-font-size: 48px; -fx-text-fill: #E5E5E5;");
            imageContainer.getChildren().add(placeholder);
        }

        VBox content = new VBox();
        content.getStyleClass().add("model-card-content");
        content.setSpacing(8);
        content.setPadding(new Insets(24));

        Label title = new Label(model.name);
        title.getStyleClass().add("model-card-title");

        Label subtitle = new Label(model.tagline);
        subtitle.getStyleClass().add("model-card-subtitle");

        VBox specs = new VBox();
        specs.getStyleClass().add("model-card-specs");
        specs.setSpacing(6);
        specs.getChildren().add(createSpecRow("ENGINE", model.engine));
        specs.getChildren().add(createSpecRow("POWER", model.power));
        specs.getChildren().add(createSpecRow("0-100 km/h", model.acceleration));
        specs.getChildren().add(createSpecRow("TOP SPEED", model.topSpeed));

        Line divider = new Line(0, 0, 292, 0);
        divider.getStyleClass().add("model-card-divider");
        VBox.setMargin(divider, new Insets(12, 0, 12, 0));

        Label price = new Label(model.price);
        price.getStyleClass().add("model-card-price");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button selectBtn = new Button("CONFIGURE");
        selectBtn.getStyleClass().add("model-card-button");
        selectBtn.setMaxWidth(Double.MAX_VALUE);
        selectBtn.setOnAction(e -> handleModelSelection(e));

        content.getChildren().addAll(title, subtitle, specs, divider, price, spacer, selectBtn);
        card.getChildren().addAll(imageContainer, content);
        addCardHoverEffect(card);
        return card;
    }

    private HBox createSpecRow(String label, String value) {
        HBox row = new HBox();
        row.getStyleClass().add("model-card-spec-row");
        row.setSpacing(8);
        row.setAlignment(Pos.CENTER_LEFT);

        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("model-card-spec-label");
        labelNode.setPrefWidth(100);

        Label valueNode = new Label(value);
        valueNode.getStyleClass().add("model-card-spec-value");

        row.getChildren().addAll(labelNode, valueNode);
        return row;
    }

    private void addCardHoverEffect(VBox card) {
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(300), card);
        scaleUp.setToX(1.02);
        scaleUp.setToY(1.02);
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(300), card);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);
        TranslateTransition slideUp = new TranslateTransition(Duration.millis(300), card);
        slideUp.setToY(-8);
        TranslateTransition slideDown = new TranslateTransition(Duration.millis(300), card);
        slideDown.setToY(0);
        card.setOnMouseEntered(e -> {
            scaleUp.play();
            slideUp.play();
        });
        card.setOnMouseExited(e -> {
            scaleDown.play();
            slideDown.play();
        });
    }

    private void animateCardsOnLoad() {
        int delay = 0;
        for (Node node : flow_Pane.getChildren()) {
            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), node);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.setDelay(Duration.millis(delay));

            TranslateTransition slideIn = new TranslateTransition(Duration.millis(500), node);
            slideIn.setFromY(30);
            slideIn.setToY(0);
            slideIn.setDelay(Duration.millis(delay));

            new ParallelTransition(fadeIn, slideIn).play();
            delay += 80;
        }
    }

    private void handleModelSelection(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/View/staffCustomize.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goBack(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/View/staffCars.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        if (isDarkMode) {
            back_btn.getScene().getRoot().getStyleClass().add("dark-mode");
            theme_toggle_btn.setText("☀");
        } else {
            back_btn.getScene().getRoot().getStyleClass().remove("dark-mode");
            theme_toggle_btn.setText("☾");
        }
        RotateTransition rotate = new RotateTransition(Duration.millis(400), theme_toggle_btn);
        rotate.setByAngle(360);
        rotate.play();
    }

    private static class ModelData {
        String name, tagline, engine, power, acceleration, topSpeed, price, imagePath;
        public ModelData(String name, String tagline, String engine, String power, String acceleration, String topSpeed, String price, String imagePath) {
            this.name = name;
            this.tagline = tagline;
            this.engine = engine;
            this.power = power;
            this.acceleration = acceleration;
            this.topSpeed = topSpeed;
            this.price = price;
            this.imagePath = imagePath;
        }
    }
}