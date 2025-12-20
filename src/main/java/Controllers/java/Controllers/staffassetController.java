package Controllers.java.Controllers;

import DAO.CarPartsDAO;
import Model.CarPart;
import Utils.SessionStaff;
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
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class staffassetController {
    @FXML private VBox productsContainer;
    @FXML private Button backButton;
    @FXML private Button viewCartButton;
    @FXML private Label cartCountLabel;
    @FXML private ToggleButton allCategoryButton;
    @FXML private ToggleButton accessoriesButton;
    @FXML private ToggleButton equipmentButton;
    @FXML private StackPane cartModal;
    @FXML private VBox cartItemsContainer;
    @FXML private Label cartSubtotalLabel;
    @FXML private Label cartTotalLabel;
    @FXML private Button closeCartButton;
    @FXML private Button continueShoppingButton;
    @FXML private Button checkoutButtonModal;

    private static final Logger logger = LoggerFactory.getLogger(staffassetController.class);
    private List<CarPart> allParts;
    private ToggleGroup categoryToggleGroup;

    public void initialize() {
        loadParts();
        setupBackButton();
        setupViewCartButton();
        setupCategoryToggleGroup();
        setupCartModalButtons();
        loadCartFromSession();
        updateCartCount();
    }

    private void loadCartFromSession() {
        List<Integer> sessionAccessories = SessionStaff.getInstance().getSelectedAccessories();
    }

    private void setupBackButton() {
        if (backButton != null) {
            backButton.setOnAction(event -> {
                try {
                    goBack();
                } catch (IOException e) {
                    logger.error("Error going back", e);
                }
            });
        }
    }

    private void setupViewCartButton() {
        if (viewCartButton != null) {
            viewCartButton.setOnAction(event -> showCartModal());
        }
    }

    private void setupCartModalButtons() {
        if (closeCartButton != null) {
            closeCartButton.setOnAction(event -> hideCartModal());
        }
        if (continueShoppingButton != null) {
            continueShoppingButton.setOnAction(event -> hideCartModal());
        }
        if (checkoutButtonModal != null) {
            checkoutButtonModal.setOnAction(event -> proceedToCheckout());
        }
    }

    private void setupCategoryToggleGroup() {
        categoryToggleGroup = new ToggleGroup();
        allCategoryButton.setToggleGroup(categoryToggleGroup);
        accessoriesButton.setToggleGroup(categoryToggleGroup);
        equipmentButton.setToggleGroup(categoryToggleGroup);
        allCategoryButton.setSelected(true);
        categoryToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                filterPartsByCategory((ToggleButton) newValue);
            }
        });
    }

    private void filterPartsByCategory(ToggleButton selectedButton) {
        if (allParts == null) return;
        List<CarPart> filteredParts = new ArrayList<>();
        String category = selectedButton.getText();
        for (CarPart part : allParts) {
            if ("ALL".equals(category)) {
                filteredParts.add(part);
            } else if ("ACCESSORIES".equals(category)) {
                if (isAccessory(part)) {
                    filteredParts.add(part);
                }
            } else if ("EQUIPMENT".equals(category)) {
                if (isEquipment(part)) {
                    filteredParts.add(part);
                }
            }
        }
        displayParts(filteredParts);
    }

    private boolean isAccessory(CarPart part) {
        String name = part.getPartName().toLowerCase();
        String desc = part.getDescription().toLowerCase();
        return name.contains("wheel") || name.contains("rim") || name.contains("tire") ||
                name.contains("spoiler") || name.contains("body kit") || name.contains("exhaust") ||
                name.contains("light") || name.contains("grille") || name.contains("mirror") ||
                desc.contains("wheel") || desc.contains("rim") || desc.contains("tire") ||
                desc.contains("spoiler") || desc.contains("body kit") || desc.contains("exhaust") ||
                desc.contains("light") || desc.contains("grille") || desc.contains("mirror");
    }

    private boolean isEquipment(CarPart part) {
        String name = part.getPartName().toLowerCase();
        String desc = part.getDescription().toLowerCase();
        return name.contains("tool") || name.contains("jack") || name.contains("stand") ||
                name.contains("cleaner") || name.contains("polish") || name.contains("cover") ||
                name.contains("charger") || name.contains("compressor") || name.contains("kit") ||
                desc.contains("tool") || desc.contains("jack") || desc.contains("stand") ||
                desc.contains("cleaner") || desc.contains("polish") || desc.contains("cover") ||
                desc.contains("charger") || desc.contains("compressor") || desc.contains("kit");
    }

    private void loadParts() {
        try {
            CarPartsDAO dao = new CarPartsDAO();
            allParts = dao.getAllParts();
            if (allParts.isEmpty()) {
                showEmptyState();
                return;
            }
            displayParts(allParts);
        } catch (Exception e) {
            showErrorState();
        }
    }

    private void displayParts(List<CarPart> parts) {
        productsContainer.getChildren().clear();
        if (parts.isEmpty()) {
            showEmptyState();
            return;
        }
        HBox currentRow = new HBox(20);
        currentRow.setPadding(new Insets(0, 0, 20, 0));
        currentRow.setAlignment(Pos.CENTER);
        for (int i = 0; i < parts.size(); i++) {
            Node card = createPartCard(parts.get(i));
            currentRow.getChildren().add(card);
            if ((i + 1) % 3 == 0 || i == parts.size() - 1) {
                productsContainer.getChildren().add(currentRow);
                if (i < parts.size() - 1) {
                    currentRow = new HBox(20);
                    currentRow.setPadding(new Insets(0, 0, 20, 0));
                    currentRow.setAlignment(Pos.CENTER);
                }
            }
        }
    }

    private Node createPartCard(CarPart part) {
        VBox card = new VBox();
        card.setSpacing(12);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 20; -fx-border-radius: 12; -fx-background-radius: 12;");
        card.getStyleClass().add("product-card");

        ImageView img = new ImageView();
        img.setFitWidth(200);
        img.setFitHeight(120);
        img.setPreserveRatio(true);

        try {
            if (part.getPartPhoto() != null && !part.getPartPhoto().isEmpty()) {
                img.setImage(new Image(new FileInputStream(part.getPartPhoto())));
            } else {
                loadPlaceholderImage(img);
            }
        } catch (Exception e) {
            loadPlaceholderImage(img);
        }

        VBox imageContainer = new VBox(img);
        imageContainer.setStyle("-fx-background-color: #F8F8F8; -fx-background-radius: 8; -fx-padding: 16; -fx-alignment: center;");
        imageContainer.getStyleClass().add("product-image-container");

        Label name = new Label(part.getPartName());
        name.getStyleClass().add("product-name");
        name.setWrapText(true);
        name.setMaxWidth(200);

        Label desc = new Label(part.getDescription());
        desc.getStyleClass().add("product-description");
        desc.setWrapText(true);
        desc.setMaxWidth(200);
        desc.setMaxHeight(40);

        Label price = new Label(String.format("$%.2f", part.getPrice()));
        price.getStyleClass().add("product-price");

        Label stock = new Label("In Stock: " + part.getPartQty());
        stock.setStyle("-fx-text-fill: #757575; -fx-font-size: 12px;");

        HBox quantityBox = new HBox(10);
        quantityBox.setAlignment(Pos.CENTER_LEFT);

        Label quantityLabel = new Label("Qty:");
        quantityLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px; -fx-font-weight: 600;");

        Spinner<Integer> quantitySpinner = new Spinner<>(1, part.getPartQty(), 1);
        quantitySpinner.setEditable(true);
        quantitySpinner.getStyleClass().add("quantity-spinner");
        quantitySpinner.setPrefWidth(80);

        Button addToCartBtn = new Button("ADD TO CART");
        addToCartBtn.getStyleClass().add("add-to-cart-button");

        addToCartBtn.setOnMouseEntered(e -> {
            addToCartBtn.setStyle("-fx-background-color: #B8001A; -fx-text-fill: #FFFFFF; -fx-font-size: 12px; -fx-font-weight: 700; -fx-letter-spacing: 0.6px; -fx-padding: 10 20; -fx-background-radius: 8;");
        });

        addToCartBtn.setOnMouseExited(e -> {
            addToCartBtn.setStyle("-fx-background-color: #E60023; -fx-text-fill: #FFFFFF; -fx-font-size: 12px; -fx-font-weight: 700; -fx-letter-spacing: 0.6px; -fx-padding: 10 20; -fx-background-radius: 8;");
        });

        addToCartBtn.setOnAction(event -> {
            int quantity = quantitySpinner.getValue();
            addToCart(part, quantity);
        });

        card.getChildren().addAll(
                imageContainer,
                name,
                desc,
                price,
                stock,
                quantityBox,
                addToCartBtn
        );

        quantityBox.getChildren().addAll(quantityLabel, quantitySpinner);

        return card;
    }

    private void loadPlaceholderImage(ImageView img) {
        try {
            img.setImage(new Image(new FileInputStream("src/Assets/Images/placeholder.png")));
        } catch (Exception e) {
            logger.error("Error loading placeholder image", e);
        }
    }

    private void showEmptyState() {
        productsContainer.getChildren().clear();
        Label emptyLabel = new Label("No parts available");
        emptyLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 16px;");
        productsContainer.getChildren().add(emptyLabel);
    }

    private void showErrorState() {
        productsContainer.getChildren().clear();
        Label errorLabel = new Label("Error loading parts. Please try again.");
        errorLabel.setStyle("-fx-text-fill: #FF0000; -fx-font-size: 16px;");
        productsContainer.getChildren().add(errorLabel);
    }

    private void addToCart(CarPart part, int quantity) {
        SessionStaff session = SessionStaff.getInstance();
        for (int i = 0; i < quantity; i++) {
            session.addSelectedAccessoryWithQuantity(part.getPartId(), 1);
        }
        updateCartCount();
    }

    private void updateCartCount() {
        SessionStaff session = SessionStaff.getInstance();
        int totalItems = session.getSelectedAccessories().size();
        if (cartCountLabel != null) {
            cartCountLabel.setText(String.valueOf(totalItems));
        }
    }

    private void showCartModal() {
        updateCartModalContent();
        if (cartModal != null) {
            cartModal.setVisible(true);
            cartModal.setManaged(true);
        }
    }

    private void hideCartModal() {
        if (cartModal != null) {
            cartModal.setVisible(false);
            cartModal.setManaged(false);
        }
    }

    private void updateCartModalContent() {
        if (cartItemsContainer == null) return;

        cartItemsContainer.getChildren().clear();
        SessionStaff session = SessionStaff.getInstance();
        List<Integer> accessoryIds = session.getSelectedAccessories();

        if (accessoryIds.isEmpty()) {
            Label emptyLabel = new Label("Your cart is empty");
            emptyLabel.getStyleClass().add("empty-cart-message");
            cartItemsContainer.getChildren().add(emptyLabel);
            updateCartTotals(0);
            checkoutButtonModal.setDisable(true);
            return;
        }

        checkoutButtonModal.setDisable(false);

        Map<Integer, Integer> itemQuantities = new HashMap<>();
        for (int accessoryId : accessoryIds) {
            itemQuantities.put(accessoryId, itemQuantities.getOrDefault(accessoryId, 0) + 1);
        }

        double subtotal = 0;

        for (Map.Entry<Integer, Integer> entry : itemQuantities.entrySet()) {
            int accessoryId = entry.getKey();
            int quantity = entry.getValue();
            CarPart part = findPartById(accessoryId);
            if (part != null) {
                HBox cartItem = createCartItem(part, quantity);
                cartItemsContainer.getChildren().add(cartItem);
                subtotal += part.getPrice() * quantity;
            }
        }

        updateCartTotals(subtotal);
    }

    private CarPart findPartById(int partId) {
        if (allParts == null) return null;
        for (CarPart part : allParts) {
            if (part.getPartId() == partId) {
                return part;
            }
        }
        return null;
    }

    private HBox createCartItem(CarPart part, int quantity) {
        HBox itemBox = new HBox(15);
        itemBox.getStyleClass().add("cart-item");
        itemBox.setAlignment(Pos.CENTER_LEFT);

        ImageView img = new ImageView();
        img.setFitWidth(60);
        img.setFitHeight(60);
        img.setPreserveRatio(true);

        try {
            if (part.getPartPhoto() != null && !part.getPartPhoto().isEmpty()) {
                img.setImage(new Image(new FileInputStream(part.getPartPhoto())));
            } else {
                loadPlaceholderImage(img);
            }
        } catch (Exception e) {
            loadPlaceholderImage(img);
        }

        VBox imageContainer = new VBox(img);
        imageContainer.getStyleClass().add("cart-item-image");

        VBox infoBox = new VBox(5);
        infoBox.getStyleClass().add("cart-item-info");

        Label nameLabel = new Label(part.getPartName());
        nameLabel.getStyleClass().add("cart-item-name");

        Label priceLabel = new Label(String.format("$%.2f", part.getPrice()));
        priceLabel.getStyleClass().add("cart-item-price");

        HBox quantityControlBox = new HBox(10);
        quantityControlBox.setAlignment(Pos.CENTER_LEFT);

        Label quantityTextLabel = new Label("Qty:");
        quantityTextLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");

        Label quantityValueLabel = new Label(String.valueOf(quantity));
        quantityValueLabel.getStyleClass().add("cart-item-quantity");

        Button removeButton = new Button("Remove");
        removeButton.getStyleClass().add("remove-item-button");
        removeButton.setOnAction(event -> {
            SessionStaff.getInstance().removeSelectedAccessory(part.getPartId());
            updateCartCount();
            updateCartModalContent();
        });

        quantityControlBox.getChildren().addAll(quantityTextLabel, quantityValueLabel, removeButton);
        infoBox.getChildren().addAll(nameLabel, priceLabel, quantityControlBox);
        HBox.setHgrow(infoBox, javafx.scene.layout.Priority.ALWAYS);
        itemBox.getChildren().addAll(imageContainer, infoBox);

        return itemBox;
    }

    private void updateCartTotals(double subtotal) {
        if (cartSubtotalLabel != null) {
            cartSubtotalLabel.setText(String.format("$%.2f", subtotal));
        }
        if (cartTotalLabel != null) {
            cartTotalLabel.setText(String.format("$%.2f", subtotal));
        }
    }

    private void proceedToCheckout() {
        SessionStaff session = SessionStaff.getInstance();
        if (session.getSelectedAccessories().isEmpty()) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/staffShopingcart.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) viewCartButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.show();
        } catch (IOException e) {
            showErrorMessage("Failed to proceed to checkout");
        }
    }

    private void goBack() throws IOException {
        FXMLLoader.load(getClass().getResource("/View/staffWelcome.fxml"));
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/View/staffWelcome.fxml"))));
        stage.setFullScreen(true);
        stage.show();
    }

    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}