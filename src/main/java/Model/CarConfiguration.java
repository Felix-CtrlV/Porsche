package Model;

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
        this.basePrice = existingCar.getPrice();
    }

    public car getCarData() {
        return carData;
    }

    public void setCarData(car carData) {
        this.carData = carData;
        if (carData != null) {
            this.basePrice = carData.getPrice();
        }
    }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public double getBasePrice() { return basePrice; }
    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
        if (carData != null) {
            carData.setPrice(basePrice);
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
            carData.setCarColor(selectedColor.getName());
        }
    }

    public CustomizationOption getSelectedInterior() { return selectedInterior; }
    public void setSelectedInterior(CustomizationOption selectedInterior) {
        this.selectedInterior = selectedInterior;

        if (carData != null && selectedInterior != null) {
            carData.setInteriorColor(selectedInterior.getName());
        }
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
            carData.setPrice(getTotalPrice());
        }
    }

    public int getCarId() {
        return carData != null ? carData.getCarId() : 0;
    }

    public String getCarColor() {
        return carData != null ? carData.getCarColor() : "";
    }

    public String getInteriorColor() {
        return carData != null ? carData.getInteriorColor() : "";
    }

    public int getProductionYear() {
        return carData != null ? carData.getProductionYear() : 0;
    }

    public String getStatus() {
        return carData != null ? carData.getCarStatus() : "";
    }

    public String getTrimName() {
        return carData != null ? carData.getTrimName() : "";
    }

    public long getCarSpeed() {
        return carData != null ? carData.getCarSpeed() : 0;
    }

    public String getCarQty() {
        return carData != null ? carData.getCarQty() : "0";
    }

    public String getCarPhoto() {
        return carData != null ? carData.getCarPhoto() : "";
    }

    public String getCarDescription() {
        return carData != null ? carData.getCarDescription() : "";
    }
}