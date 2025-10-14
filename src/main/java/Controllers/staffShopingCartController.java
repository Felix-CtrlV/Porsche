package Controllers;

import Model.car;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;

import java.util.List;

public class staffShopingCartController {

    @FXML private ListView<String> cartListView;
    @FXML private Label totalPriceLabel;

    private ObservableList<String> cartItems = FXCollections.observableArrayList();
    private double totalPrice = 0;

    public void addToCart(car selectedCar, String accessories) {
        String item = selectedCar.getModelid() + " - " + selectedCar.getColor() + " (" + accessories + ")";
        cartItems.add(item);
        totalPrice += selectedCar.getCurrent_price();
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
