package Controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class staffassetController {

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
    private final List<Product> allProducts = new ArrayList<>();
    private final List<Product> currentProducts = new ArrayList<>();
    private final Map<String, CartItem> shoppingCart = new HashMap<>();

    private static class SliderItem {
        String imagePath, title, description;
        SliderItem(String imagePath, String title, String description) {
            this.imagePath = imagePath;
            this.title = title;
            this.description = description;
        }
    }

    private static class Product {
        String id, name, category, description, imagePath;
        double price;
        Product(String id, String name, String category, double price, String description, String imagePath) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.price = price;
            this.description = description;
            this.imagePath = imagePath;
        }
    }

    private static class CartItem {
        Product product;
        int quantity;
        CartItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }
        double getTotal() { return product.price * quantity; }
    }

    @FXML
    public void initialize() {
        categoryToggleGroup = new ToggleGroup();
        allCategoryButton.setToggleGroup(categoryToggleGroup);
        accessoriesButton.setToggleGroup(categoryToggleGroup);
        equipmentButton.setToggleGroup(categoryToggleGroup);
        allCategoryButton.setSelected(true);

        initializeSliderData();
        initializeProducts();
        setupSlider();
        setupCategoryFilters();
        setupCartButtons();
        setupBackButton();
        displayProducts("all");
        updateCartDisplay();
    }

    private void initializeSliderData() {
        sliderItems.add(new SliderItem("/Image/Porsche Classic Car Cover.png", "Porsche Classic Car Cover", "Custom-fitted Car Cover"));
        sliderItems.add(new SliderItem("/Image/Illuminated Door Sill Guards.png", "Door Sill Guards", "Protects Your Door Edges"));
        sliderItems.add(new SliderItem("/Image/Porsche Design Child Seat.png", "Porsche Design Child Seat", "Safe and Comfortable"));
        sliderItems.add(new SliderItem("/Image/Porsche Cleaning and Care Kit.png", "Cleaning Kit", "Cleans That Goofy Ass Dirt"));
    }

    private void initializeProducts() {
        allProducts.add(new Product("acc001", "Porsche Classic Car Cover", "accessories", 299, "Protective car cover for your Porsche", "/Image/Porsche Classic Car Cover.png"));
        allProducts.add(new Product("acc002", "Illuminated Door Sill Guards", "accessories", 179, "Stylish door sill protection", "/Image/Illuminated Door Sill Guards.png"));
        allProducts.add(new Product("eq001", "Porsche Design Child Seat", "equipment", 599, "Safe child seat design", "/Image/Porsche Design Child Seat.png"));
        allProducts.add(new Product("eq002", "Porsche Cleaning and Care Kit", "equipment", 899, "147-piece tool set with premium case", "/Image/Porsche Cleaning and Care Kit.png"));
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
        if (sliderItems.isEmpty()) return;
        SliderItem item = sliderItems.get(currentSlide);
        try { sliderImage.setImage(new Image(getClass().getResourceAsStream(item.imagePath))); }
        catch (Exception e) { sliderImage.setImage(null); }
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
        productsContainer.getChildren().clear();
        currentProducts.clear();
        for (Product product : allProducts) {
            if (category.equals("all") || product.category.equals(category)) {
                currentProducts.add(product);
                productsContainer.getChildren().add(createProductCard(product));
            }
        }
    }

    private VBox createProductCard(Product product) {
        VBox card = new VBox(15);
        card.getStyleClass().add("product-card");
        card.setPadding(new Insets(15));

        HBox contentBox = new HBox(15);
        contentBox.setAlignment(Pos.CENTER_LEFT);

        VBox imageContainer = new VBox();
        imageContainer.getStyleClass().add("product-image-container");
        imageContainer.setPrefSize(120, 120);
        imageContainer.setAlignment(Pos.CENTER);

        try {
            ImageView imgView = new ImageView(new Image(getClass().getResourceAsStream(product.imagePath)));
            imgView.setFitWidth(120); imgView.setFitHeight(120); imgView.setPreserveRatio(true);
            imageContainer.getChildren().add(imgView);
        } catch (Exception e) {
            Label imagePlaceholder = new Label("📦");
            imagePlaceholder.setStyle("-fx-font-size: 48px;");
            imageContainer.getChildren().add(imagePlaceholder);
        }

        VBox detailsBox = new VBox(6);
        detailsBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(detailsBox, javafx.scene.layout.Priority.ALWAYS);

        Label nameLabel = new Label(product.name); nameLabel.getStyleClass().add("product-name");
        Label descLabel = new Label(product.description); descLabel.getStyleClass().add("product-description");
        descLabel.setWrapText(true); descLabel.setMaxWidth(400);
        Label categoryLabel = new Label(product.category.toUpperCase()); categoryLabel.getStyleClass().add("product-category");

        detailsBox.getChildren().addAll(nameLabel, descLabel, categoryLabel);

        VBox controlsBox = new VBox(10); controlsBox.setAlignment(Pos.CENTER_RIGHT); controlsBox.setMinWidth(200);
        Label priceLabel = new Label(currencyFormat.format(product.price)); priceLabel.getStyleClass().add("product-price");

        HBox quantityBox = new HBox(8); quantityBox.setAlignment(Pos.CENTER_RIGHT);
        Label qtyLabel = new Label("Qty:"); qtyLabel.getStyleClass().add("quantity-label");
        Spinner<Integer> quantitySpinner = new Spinner<>(1, 99, 1);
        quantitySpinner.getStyleClass().add("quantity-spinner"); quantitySpinner.setPrefWidth(60); quantitySpinner.setEditable(true);
        quantityBox.getChildren().addAll(qtyLabel, quantitySpinner);

        Button addButton = new Button("ADD TO CART"); addButton.getStyleClass().add("add-to-cart-button"); addButton.setPrefWidth(150);
        addButton.setOnAction(e -> addToCart(product, quantitySpinner.getValue()));
        controlsBox.getChildren().addAll(priceLabel, quantityBox, addButton);

        contentBox.getChildren().addAll(imageContainer, detailsBox, controlsBox);
        card.getChildren().add(contentBox);
        return card;
    }

    private void addToCart(Product product, int quantity) {
        shoppingCart.merge(product.id, new CartItem(product, quantity), (existing, newItem) -> { existing.quantity += newItem.quantity; return existing; });
        updateCartDisplay();
    }

    private void setupCartButtons() {
        viewCartButton.setOnAction(e -> openCartModal());
        clearCartButton.setOnAction(e -> clearCart());
        checkoutButton.setOnAction(e -> proceedToCheckout());
        closeCartButton.setOnAction(e -> closeCartModal());
    }

    private void setupBackButton() {
        backButton.setOnAction(e -> navigate(backButton, "/View/staffWelcome.fxml"));
    }

    private void openCartModal() {
        if (shoppingCart.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION); alert.setTitle("Empty Cart");
            alert.setHeaderText("Your cart is empty"); alert.setContentText("Add some items to your cart first!"); alert.showAndWait();
            return;
        }
        cartModalOverlay.setVisible(true); cartModalOverlay.setManaged(true);
    }

    @FXML
    private void closeCartModal() {
        cartModalOverlay.setVisible(false); cartModalOverlay.setManaged(false);
    }

    private void updateCartDisplay() {
        int totalItems = 0; double subtotal = 0;
        cartItemsContainer.getChildren().clear();

        for (CartItem item : shoppingCart.values()) {
            totalItems += item.quantity; subtotal += item.getTotal();
            HBox row = new HBox(15); row.setAlignment(Pos.CENTER_LEFT); row.getStyleClass().add("cart-item-row"); row.setPadding(new Insets(10));

            Label name = new Label(item.product.name); name.getStyleClass().add("cart-item-name"); name.setPrefWidth(400);
            Label qty = new Label("x" + item.quantity); qty.getStyleClass().add("cart-item-quantity"); qty.setPrefWidth(60);
            Label price = new Label(currencyFormat.format(item.getTotal())); price.getStyleClass().add("cart-item-price");
            Button remove = new Button("✕"); remove.getStyleClass().add("remove-item-button"); remove.setOnAction(e -> { shoppingCart.remove(item.product.id); updateCartDisplay(); });

            HBox spacer = new HBox(); HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            row.getChildren().addAll(name, qty, spacer, price, remove);
            cartItemsContainer.getChildren().add(row);
        }

        double tax = subtotal * 0.08; double total = subtotal + tax;
        cartCountLabel.setText(String.valueOf(totalItems)); cartItemCountLabel.setText(totalItems + " items");
        subtotalLabel.setText(currencyFormat.format(subtotal)); taxLabelCart.setText(currencyFormat.format(tax)); totalLabel.setText(currencyFormat.format(total));
    }

    private void clearCart() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Cart"); alert.setHeaderText("Clear Shopping Cart?"); alert.setContentText("Are you sure you want to remove all items?");
        alert.showAndWait().ifPresent(response -> { if (response == ButtonType.OK) { shoppingCart.clear(); updateCartDisplay(); closeCartModal(); } });
    }

    private void proceedToCheckout() {
        if (shoppingCart.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING); alert.setTitle("Empty Cart"); alert.setHeaderText("Your cart is empty"); alert.setContentText("Add items before checking out."); alert.showAndWait(); return;
        }
        closeCartModal();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/staffShopingCart.fxml"));
            Scene scene = new Scene(loader.load(), 1300, 850);
            staffShopingCartController controller = loader.getController();
            controller.setCameFromAsset(true);
            for (CartItem item : shoppingCart.values()) controller.addAccessory(item.product.name, item.getTotal());
            Stage stage = (Stage) checkoutButton.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException ex) { ex.printStackTrace(); }
    }

    private void navigate(Button source, String path) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Scene scene = new Scene(loader.load(), 1300, 850);
            Stage stage = (Stage) source.getScene().getWindow();
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException ex) { ex.printStackTrace(); }
    }
}
