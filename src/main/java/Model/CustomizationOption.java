package Model;

import javafx.scene.image.Image;

public class CustomizationOption {
    private final String name;
    private final double price;
    private final Image image;
    private final boolean isStandard;

    public CustomizationOption(String name, double price, Image image, boolean isStandard) {
        this.name = name;
        this.price = price;
        this.image = image;
        this.isStandard = isStandard;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public Image getImage() {
        return image;
    }

    public boolean isStandard() {
        return isStandard;
    }

    public String getFormattedPrice() {
        if (isStandard) {
            return "STANDARD";
        }
        return String.format("+ $%,d", (int) price);
    }
}
