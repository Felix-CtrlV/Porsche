package Controllers;

import Model.car;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.util.Duration;

import java.util.List;

public class staffShopingCartController {

    @FXML private ListView<String> cartListView;
    @FXML private Label totalPriceLabel;
    @FXML private ScrollPane scrollPane;

    private ObservableList<String> cartItems = FXCollections.observableArrayList();
    private double totalPrice = 0;

    @FXML
    public void initialize() {
        if (scrollPane != null) {
            scrollPane.setOnScroll(event -> {
                double deltaY = event.getDeltaY() * 0.002;
                double targetVvalue = scrollPane.getVvalue() - deltaY;
                targetVvalue = Math.min(Math.max(targetVvalue, 0), 1);

                Timeline timeline = new Timeline();
                KeyValue kv = new KeyValue(scrollPane.vvalueProperty(), targetVvalue, Interpolator.EASE_BOTH);
                KeyFrame kf = new KeyFrame(Duration.millis(300), kv);
                timeline.getKeyFrames().add(kf);
                timeline.play();
            });
        }
    }

    public void addToCart(car selectedCar, String accessories) {
        String item = selectedCar.getModelid() + " - " + selectedCar.getColor() + " (" + accessories + ")";
        cartItems.add(item);
        totalPrice += selectedCar.getCurrent_price();
        cartListView.setItems(cartItems);
        totalPriceLabel.setText(String.format("$%.2f", totalPrice));
    }

    public void addAccessory(String name, double price) {
        String item = name + " - Accessory";
        cartItems.add(item);
        totalPrice += price;
        cartListView.setItems(cartItems);
        totalPriceLabel.setText(String.format("$%.2f", totalPrice));
    }

    public void removeFromCart(int index) {
        if (index >= 0 && index < cartItems.size()) {
            cartItems.remove(index);
        }
    }

    public void clearCart() {
        cartItems.clear();
        totalPrice = 0;
        totalPriceLabel.setText("$0.00");
    }

    public double getTotalPrice() { return totalPrice; }
    public List<String> getCartItems() { return cartItems; }
}
