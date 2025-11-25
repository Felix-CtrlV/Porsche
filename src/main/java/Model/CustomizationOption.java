package Model;

import javafx.scene.image.Image;

public class CustomizationOption {
    private String name;
    private double price;
    private Image image;
    private boolean available;
    private String imagePath;
    private String category;

    public CustomizationOption() {
    }

    public CustomizationOption(String name, double price, Image image, boolean available) {
        this.name = name;
        this.price = price;
        this.image = image;
        this.available = available;
    }

    public CustomizationOption(String name, double price, String imagePath, boolean available, String category) {
        this.name = name;
        this.price = price;
        this.imagePath = imagePath;
        this.available = available;
        this.category = category;
    }

    public String getFormattedPrice() {
        if (price == 0) {
            return "Selected";
        } else {
            return String.format("+$%,.0f", price);
        }
    }

    public String getPriceDisplay() {
        if (price == 0) {
            return "Selected";
        } else {
            return String.format("$%,.0f", price);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return name + (price == 0 ? " (Selected)" : " (+$" + (int)price + ")");
    }
}