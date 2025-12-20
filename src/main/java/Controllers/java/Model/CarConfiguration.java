package Controllers.java.Model;

import Model.CustomizationOption;
import Model.car;

public class CarConfiguration {
    private Model.car baseCar;
    private Model.CustomizationOption selectedColor;
    private Model.CustomizationOption selectedInterior;

    private String modelName;
    private String modelImagePath;
    private String frameNumber;
    private String fuelType;
    private String description;
    private double basePrice;
    private double totalPrice;

    public CarConfiguration() {
        this.basePrice = 0;
        this.totalPrice = 0;
    }

    public CarConfiguration(Model.car baseCar) {
        this.baseCar = baseCar;
        this.basePrice = baseCar.getPrice();
        this.totalPrice = basePrice;
        this.modelName = baseCar.getFullName();
        this.modelImagePath = baseCar.getCarPhoto();
        this.frameNumber = String.valueOf(baseCar.getCarId());
        this.fuelType = baseCar.getFuelType();
        this.description = baseCar.getCarDescription();
    }

    public void updateCarPrice() {
        double optionTotal = 0;

        if (selectedColor != null && selectedColor.getPrice() > 0) {
            optionTotal += selectedColor.getPrice();
        }
        if (selectedInterior != null && selectedInterior.getPrice() > 0) {
            optionTotal += selectedInterior.getPrice();
        }

        this.totalPrice = this.basePrice + optionTotal;
    }

    public Model.car getBaseCar() { return baseCar; }

    public void setBaseCar(car baseCar) {
        this.baseCar = baseCar;
        this.basePrice = baseCar.getPrice();
        this.totalPrice = basePrice;
    }

    public Model.CustomizationOption getSelectedColor() { return selectedColor; }

    public void setSelectedColor(Model.CustomizationOption selectedColor) {
        this.selectedColor = selectedColor;
        updateCarPrice();
    }

    public Model.CustomizationOption getSelectedInterior() { return selectedInterior; }

    public void setSelectedInterior(CustomizationOption selectedInterior) {
        this.selectedInterior = selectedInterior;
        updateCarPrice();
    }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public String getModelImagePath() { return modelImagePath; }
    public void setModelImagePath(String modelImagePath) { this.modelImagePath = modelImagePath; }

    public String getFrameNumber() { return frameNumber; }
    public void setFrameNumber(String frameNumber) { this.frameNumber = frameNumber; }

    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getBasePrice() { return basePrice; }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
        updateCarPrice();
    }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getFormattedTotalPrice() {
        return String.format("$%,d", (int) totalPrice);
    }

    public int getCarId() {
        return baseCar != null ? baseCar.getCarId() : 0;
    }

    public String getSelectedOptionsSummary() {
        StringBuilder summary = new StringBuilder();

        if (selectedColor != null) {
            summary.append("Color: ").append(selectedColor.getName());
            if (selectedColor.getPrice() > 0) {
                summary.append(" (+$").append((int)selectedColor.getPrice()).append(")");
            }
            summary.append("\n");
        }

        if (selectedInterior != null) {
            summary.append("Interior: ").append(selectedInterior.getName());
            if (selectedInterior.getPrice() > 0) {
                summary.append(" (+$").append((int)selectedInterior.getPrice()).append(")");
            }
        }

        return summary.toString();
    }
}