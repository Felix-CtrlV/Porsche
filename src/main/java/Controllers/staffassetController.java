package Controllers;

import DAO.AccessoryDAO;
import Model.Accessory;
import Utils.SessionStaff;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.*;

public class staffassetController {
    private static final Logger logger = LoggerFactory.getLogger(staffassetController.class);

    @FXML private ImageView sliderImage;
    @FXML private HBox sliderIndicators;
    @FXML private Label sliderTitle, sliderDescription;
    @FXML private ToggleButton allCategoryButton, accessoriesButton, equipmentButton;
    @FXML private VBox productsContainer;
    @FXML private Label cartCountLabel, cartItemCountLabel;
    @FXML private Button viewCartButton, clearCartButton, checkoutButton, closeCartButton, backButton;
    @FXML private VBox cartItemsContainer;
    @FXML private Label subtotalLabel, taxLabelCart, totalLabel;
    @FXML private StackPane cartModalOverlay;
    @FXML private ToggleGroup categoryToggleGroup;

    private int currentSlide = 0;
    private Timeline autoSlideTimeline;
    private final DecimalFormat currencyFormat = new DecimalFormat("$#,##0");
    private final List<SliderItem> sliderItems = new ArrayList<>();
    private final List<Accessory> allAccessories = new ArrayList<>();
    private final List<Accessory> currentAccessories = new ArrayList<>();
    private AccessoryDAO accessoryDAO;

    private static class SliderItem {
        String imagePath, title, description;
        SliderItem(String imagePath, String title, String description) {
            this.imagePath = imagePath;
            this.title = title;
            this.description = description;
        }
    }

    @FXML
    public void initialize() {
        logger.info("=== Initializing staffassetController ===");

        categoryToggleGroup = new ToggleGroup();
        allCategoryButton.setToggleGroup(categoryToggleGroup);
        accessoriesButton.setToggleGroup(categoryToggleGroup);
        equipmentButton.setToggleGroup(categoryToggleGroup);
        allCategoryButton.setSelected(true);

        accessoryDAO = new AccessoryDAO();

        Platform.runLater(() -> {
            loadAccessoriesFromDatabase();
            initializeSliderData();
            setupSlider();
            setupCategoryFilters();
            setupCartButtons();
            setupBackButton();
            displayProducts("all");
            updateCartDisplay();
        });
    }

    private void loadAccessoriesFromDatabase() {
        try {
            logger.info("Loading accessories from database...");
            allAccessories.clear();
            List<Accessory> loadedAccessories = accessoryDAO.getAllAccessories();

            if (loadedAccessories == null) {
                logger.error("accessoryDAO.getAllAccessories() returned NULL!");
                return;
            }

            logger.info("Database returned {} accessories", loadedAccessories.size());

            for (Accessory acc : loadedAccessories) {
                logger.debug("Loaded: ID={}, Name={}, Price={}, Photo={}",
                        acc.getPartId(), acc.getPartName(), acc.getPrice(), acc.getPartPhoto());
                allAccessories.add(acc);
            }

            logger.info("Successfully loaded {} accessories into memory", allAccessories.size());

        } catch (Exception e) {
            logger.error("EXCEPTION while loading accessories from database!", e);
            e.printStackTrace();
        }
    }

    private void initializeSliderData() {
        logger.info("Initializing slider data with {} accessories", allAccessories.size());
        sliderItems.clear();

        int count = Math.min(4, allAccessories.size());
        for (int i = 0; i < count; i++) {
            Accessory acc = allAccessories.get(i);
            sliderItems.add(new SliderItem(
                    acc.getPartPhoto(),
                    acc.getPartName(),
                    acc.getDescription()
            ));
            logger.debug("Added slider item: {}", acc.getPartName());
        }

        if (sliderItems.isEmpty()) {
            logger.warn("No accessories available, using fallback slider");
            sliderItems.add(new SliderItem("/Image/placeholder_car.png", "No Products", "Check back later"));
        }

        logger.info("Slider initialized with {} items", sliderItems.size());
    }

    private void setupSlider() {
        updateSlide();
        createSliderIndicators();
        autoSlideTimeline = new Timeline(new KeyFrame(Duration.seconds(4), e -> nextSlide()));
        autoSlideTimeline.setCycleCount(Timeline.INDEFINITE);
        autoSlideTimeline.play();
    }

    private void createSliderIndicators() {
        sliderIndicators.getChildren().clear();
        for (int i = 0; i < sliderItems.size(); i++) {
            Circle indicator = new Circle(6);
            indicator.getStyleClass().add("slider-indicator");
            if (i == currentSlide) indicator.getStyleClass().add("active");
            final int slideIndex = i;
            indicator.setOnMouseClicked(e -> {
                currentSlide = slideIndex;
                updateSlide();
            });
            sliderIndicators.getChildren().add(indicator);
        }
    }

    private void updateSlide() {
        if (sliderItems.isEmpty()) {
            logger.warn("No slider items to display");
            return;
        }

        SliderItem item = sliderItems.get(currentSlide);
        logger.debug("Updating slide to: {}", item.title);

        try (InputStream stream = getClass().getResourceAsStream(item.imagePath)) {
            if (stream != null) {
                sliderImage.setImage(new Image(stream));
                logger.debug("Loaded slider image: {}", item.imagePath);
            } else {
                logger.warn("Slider image not found: {}", item.imagePath);
                sliderImage.setImage(null);
            }
        } catch (Exception e) {
            logger.error("Failed to load slider image: " + item.imagePath, e);
            sliderImage.setImage(null);
        }

        sliderTitle.setText(item.title);
        sliderDescription.setText(item.description);
        createSliderIndicators();
    }

    private void nextSlide() {
        currentSlide = (currentSlide + 1) % sliderItems.size();
        updateSlide();
    }

    private void setupCategoryFilters() {
        categoryToggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == allCategoryButton) displayProducts("all");
            else if (newVal == accessoriesButton) displayProducts("accessories");
            else if (newVal == equipmentButton) displayProducts("equipment");
        });
    }

    private void displayProducts(String category) {
        logger.info("Displaying products for category: {}", category);
        logger.info("Total accessories available: {}", allAccessories.size());

        productsContainer.getChildren().clear();
        currentAccessories.clear();

        for (Accessory accessory : allAccessories) {
            String accCategory = accessory.getCategoryFromCity();
            logger.debug("Accessory: {} | Category: {} | Matches filter: {}",
                    accessory.getPartName(), accCategory, category.equals("all") || accCategory.equals(category));

            if (category.equals("all") || accCategory.equals(category)) {
                currentAccessories.add(accessory);
                VBox card = createProductCard(accessory);
                productsContainer.getChildren().add(card);
            }
        }

        logger.info("Displayed {} products for category: {}", currentAccessories.size(), category);

        if (currentAccessories.isEmpty()) {
            logger.warn("No products to display!");
        }
    }

    private VBox createProductCard(Accessory accessory) {
        VBox card = new VBox(15);
        card.getStyleClass().add("product-card");
        card.setPadding(new Insets(15));

        HBox contentBox = new HBox(15);
        contentBox.setAlignment(Pos.CENTER_LEFT);

        VBox imageContainer = new VBox();
        imageContainer.getStyleClass().add("product-image-container");
        imageContainer.setPrefSize(120, 120);
        imageContainer.setAlignment(Pos.CENTER);

        try (InputStream stream = getClass().getResourceAsStream(accessory.getPartPhoto())) {
            if (stream != null) {
                ImageView imgView = new ImageView(new Image(stream));
                imgView.setFitWidth(120);
                imgView.setFitHeight(120);
                imgView.setPreserveRatio(true);
                imageContainer.getChildren().add(imgView);
                logger.debug("Loaded product image: {}", accessory.getPartPhoto());
            } else {
                logger.warn("Product image not found: {}", accessory.getPartPhoto());
                Label imagePlaceholder = new Label("📦");
                imagePlaceholder.setStyle("-fx-font-size: 48px;");
                imageContainer.getChildren().add(imagePlaceholder);
            }
        } catch (Exception e) {
            logger.error("Failed to load product image: " + accessory.getPartPhoto(), e);
            Label imagePlaceholder = new Label("📦");
            imagePlaceholder.setStyle("-fx-font-size: 48px;");
            imageContainer.getChildren().add(imagePlaceholder);
        }

        VBox detailsBox = new VBox(6);
        detailsBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(detailsBox, javafx.scene.layout.Priority.ALWAYS);

        Label nameLabel = new Label(accessory.getPartName());
        nameLabel.getStyleClass().add("product-name");

        Label descLabel = new Label(accessory.getDescription());
        descLabel.getStyleClass().add("product-description");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(400);

        Label categoryLabel = new Label(accessory.getCategoryFromCity().toUpperCase());
        categoryLabel.getStyleClass().add("product-category");

        detailsBox.getChildren().addAll(nameLabel, descLabel, categoryLabel);

        VBox controlsBox = new VBox(10);
        controlsBox.setAlignment(Pos.CENTER_RIGHT);
        controlsBox.setMinWidth(200);

        Label priceLabel = new Label(currencyFormat.format(accessory.getPrice()));
        priceLabel.getStyleClass().add("product-price");

        HBox quantityBox = new HBox(8);
        quantityBox.setAlignment(Pos.CENTER_RIGHT);
        Label qtyLabel = new Label("Qty:");
        qtyLabel.getStyleClass().add("quantity-label");
        Spinner<Integer> quantitySpinner = new Spinner<>(1, 99, 1);
        quantitySpinner.getStyleClass().add("quantity-spinner");
        quantitySpinner.setPrefWidth(60);
        quantitySpinner.setEditable(true);
        quantityBox.getChildren().addAll(qtyLabel, quantitySpinner);

        Button addButton = new Button("ADD TO CART");
        addButton.getStyleClass().add("add-to-cart-button");
        addButton.setPrefWidth(150);
        addButton.setOnAction(e -> addToCart(accessory, quantitySpinner.getValue()));

        controlsBox.getChildren().addAll(priceLabel, quantityBox, addButton);

        contentBox.getChildren().addAll(imageContainer, detailsBox, controlsBox);
        card.getChildren().add(contentBox);
        return card;
    }

    private void addToCart(Accessory accessory, int quantity) {
        SessionStaff.getInstance().addAccessory(
                String.valueOf(accessory.getPartId()),
                accessory.getPartName(),
                accessory.getPrice(),
                quantity
        );
        updateCartDisplay();
        logger.info("Added to cart: {} x{}", accessory.getPartName(), quantity);

        Button sourceButton = null;
        for (Node node : productsContainer.getChildren()) {
            if (node instanceof VBox) {
                VBox card = (VBox) node;
                for (Node child : card.getChildren()) {
                    if (child instanceof HBox) {
                        HBox contentBox = (HBox) child;
                        for (Node innerChild : contentBox.getChildren()) {
                            if (innerChild instanceof VBox) {
                                VBox controlsBox = (VBox) innerChild;
                                for (Node control : controlsBox.getChildren()) {
                                    if (control instanceof Button && ((Button) control).getText().equals("ADD TO CART")) {
                                        sourceButton = (Button) control;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (sourceButton != null) {
            showAddedAnimation(sourceButton);
        }
    }

    private void showAddedAnimation(Button button) {
        String originalText = button.getText();
        button.setText("✓ ADDED");
        button.setDisable(true);

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1.5), e -> {
            button.setText(originalText);
            button.setDisable(false);
        }));
        timeline.play();
    }

    private void setupCartButtons() {
        if (viewCartButton != null) {
            viewCartButton.setOnAction(e -> openCartModal());
        }
        if (closeCartButton != null) {
            closeCartButton.setOnAction(e -> closeCartModal());
        }
        if (clearCartButton != null) {
            clearCartButton.setOnAction(e -> clearCart());
        }
        if (checkoutButton != null) {
            checkoutButton.setOnAction(e -> proceedToCheckout());
        }
    }

    private void setupBackButton() {
        if (backButton != null) {
            backButton.setOnAction(e -> goBack());
        }
    }

    private void goBack() {
        navigateTo("/View/staffWelcome.fxml");
    }

    private void openCartModal() {
        updateCartItemsDisplay();
        cartModalOverlay.setVisible(true);
        cartModalOverlay.setManaged(true);
    }

    private void closeCartModal() {
        cartModalOverlay.setVisible(false);
        cartModalOverlay.setManaged(false);
    }

    private void clearCart() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Cart");
        alert.setHeaderText("Are you sure you want to clear all items from the cart?");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            SessionStaff.getInstance().clearAccessories();
            updateCartDisplay();
            updateCartItemsDisplay();
            logger.info("Cart cleared");
        }
    }

    private void proceedToCheckout() {
        closeCartModal();
        navigateTo("/View/staffShopingCart.fxml");
    }

    private void updateCartDisplay() {
        int totalItems = SessionStaff.getInstance().getAccessories().values().stream()
                .mapToInt(item -> item.quantity)
                .sum();

        if (cartCountLabel != null) {
            cartCountLabel.setText(String.valueOf(totalItems));
            cartCountLabel.setVisible(totalItems > 0);
        }
    }

    private void updateCartItemsDisplay() {
        if (cartItemsContainer == null) return;

        cartItemsContainer.getChildren().clear();
        Map<String, SessionStaff.AccessoryItem> accessories = SessionStaff.getInstance().getAccessories();

        if (accessories.isEmpty()) {
            Label emptyLabel = new Label("Your cart is empty");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666; -fx-padding: 40px;");
            cartItemsContainer.getChildren().add(emptyLabel);
            updateCartTotals(0, 0, 0);
            return;
        }

        double subtotal = 0;

        for (SessionStaff.AccessoryItem item : accessories.values()) {
            HBox itemRow = createCartItemRow(item);
            cartItemsContainer.getChildren().add(itemRow);
            subtotal += item.price * item.quantity;
        }

        double tax = subtotal * 0.08;
        double total = subtotal + tax;
        updateCartTotals(subtotal, tax, total);

        if (cartItemCountLabel != null) {
            int totalItems = accessories.values().stream().mapToInt(i -> i.quantity).sum();
            cartItemCountLabel.setText(totalItems + " item" + (totalItems != 1 ? "s" : ""));
        }
    }

    private HBox createCartItemRow(SessionStaff.AccessoryItem item) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));
        row.getStyleClass().add("cart-item-row");

        Label nameLabel = new Label(item.name);
        nameLabel.getStyleClass().add("cart-item-name");
        nameLabel.setMinWidth(200);

        Label qtyLabel = new Label("x" + item.quantity);
        qtyLabel.getStyleClass().add("cart-item-quantity");
        qtyLabel.setMinWidth(50);

        Label priceLabel = new Label(currencyFormat.format(item.price * item.quantity));
        priceLabel.getStyleClass().add("cart-item-price");
        priceLabel.setMinWidth(80);

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button removeButton = new Button("×");
        removeButton.getStyleClass().add("cart-remove-button");
        removeButton.setOnAction(e -> {
            SessionStaff.getInstance().removeAccessory(item.name);
            updateCartDisplay();
            updateCartItemsDisplay();
        });

        row.getChildren().addAll(nameLabel, spacer, qtyLabel, priceLabel, removeButton);
        return row;
    }

    private void updateCartTotals(double subtotal, double tax, double total) {
        if (subtotalLabel != null) subtotalLabel.setText(currencyFormat.format(subtotal));
        if (taxLabelCart != null) taxLabelCart.setText(currencyFormat.format(tax));
        if (totalLabel != null) totalLabel.setText(currencyFormat.format(total));
    }

    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1300, 850);
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Failed to navigate");
            alert.setContentText("Could not load: " + fxmlPath);
            alert.showAndWait();
        }
    }
}