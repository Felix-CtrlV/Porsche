package Model;

import Model.car;
public class CarConfiguration {
    private car carData;
    private String modelName;
    private String modelImagePath;
    private String frameNumber;
    private String fuelType;
    private String description;
    private CustomizationOption selectedWheel;
    private CustomizationOption selectedColor;
    private CustomizationOption selectedInterior;
    private double basePrice;

    public CarConfiguration() {
        this.carData = new car();
    }

    public CarConfiguration(car existingCar) {
        this.carData = existingCar;
        this.basePrice = existingCar.getCurrent_price();
    }

    public car getCarData() {
        return carData;
    }

    public void setCarData(car carData) {
        this.carData = carData;
        if (carData != null) {
            this.basePrice = carData.getCurrent_price();
        }
    }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public double getBasePrice() { return basePrice; }
    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
        if (carData != null) {
            carData.setCurrent_price(basePrice);
        }
    }

    public String getModelImagePath() { return modelImagePath; }
    public void setModelImagePath(String modelImagePath) { this.modelImagePath = modelImagePath; }

    public String getFrameNumber() { return frameNumber; }
    public void setFrameNumber(String frameNumber) { this.frameNumber = frameNumber; }

    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public CustomizationOption getSelectedWheel() { return selectedWheel; }
    public void setSelectedWheel(CustomizationOption selectedWheel) {
        this.selectedWheel = selectedWheel;
    }

    public CustomizationOption getSelectedColor() {
        return selectedColor;
    }

    public void setSelectedColor(CustomizationOption selectedColor) {
        this.selectedColor = selectedColor;

        if (carData != null && selectedColor != null) {
            carData.setColor(selectedColor.getName());
        }
    }

    public CustomizationOption getSelectedInterior() { return selectedInterior; }
    public void setSelectedInterior(CustomizationOption selectedInterior) {
        this.selectedInterior = selectedInterior;
    }

    public double getTotalPrice() {
        double total = basePrice;
        if (selectedWheel != null && !selectedWheel.isStandard()) {
            total += selectedWheel.getPrice();
        }
        if (selectedColor != null && !selectedColor.isStandard()) {
            total += selectedColor.getPrice();
        }
        if (selectedInterior != null && !selectedInterior.isStandard()) {
            total += selectedInterior.getPrice();
        }
        return total;
    }

    public String getFormattedTotalPrice() {
        return String.format("$%,d", (int) getTotalPrice());
    }

    public void updateCarPrice() {
        if (carData != null) {
            carData.setCurrent_price(getTotalPrice());
        }
    }

    public int getCarId() {
        return carData != null ? carData.getCarid() : 0;
    }

    public int getModelId() {
        return carData != null ? carData.getModelid() : 0;
    }

    public int getProductionYear() {
        return carData != null ? carData.getProduction_year() : 0;
    }

    public String getStatus() {
        return carData != null ? carData.getStatus() : "";
    }

    public int getQuantity() {
        return carData != null ? carData.getQuantity() : 0;
    }
}