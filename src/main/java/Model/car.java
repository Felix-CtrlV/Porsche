package Model;

public class car {
    private int carId;
    private String modelName;
    private String trimName;
    private String carColor;
    private String interiorColor;
    private String fuelType;
    private String carStatus;
    private long carSpeed;
    private int productionYear;
    private String carQty;
    private double price;
    private String carDescription;
    private String carPhoto;

    // Default constructor
    public car() {}

    // Full constructor
    public car(int carId, String modelName, String trimName, String carColor,
               String interiorColor, String fuelType, String carStatus,
               long carSpeed, int productionYear, String carQty,
               double price, String carDescription, String carPhoto) {
        this.carId = carId;
        this.modelName = modelName;
        this.trimName = trimName;
        this.carColor = carColor;
        this.interiorColor = interiorColor;
        this.fuelType = fuelType;
        this.carStatus = carStatus;
        this.carSpeed = carSpeed;
        this.productionYear = productionYear;
        this.carQty = carQty;
        this.price = price;
        this.carDescription = carDescription;
        this.carPhoto = carPhoto;
    }

    // Getters and Setters
    public int getCarId() {
        return carId;
    }

    public void setCarId(int carId) {
        this.carId = carId;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getTrimName() {
        return trimName;
    }

    public void setTrimName(String trimName) {
        this.trimName = trimName;
    }

    public String getCarColor() {
        return carColor;
    }

    public void setCarColor(String carColor) {
        this.carColor = carColor;
    }

    public String getInteriorColor() {
        return interiorColor;
    }

    public void setInteriorColor(String interiorColor) {
        this.interiorColor = interiorColor;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public String getCarStatus() {
        return carStatus;
    }

    public void setCarStatus(String carStatus) {
        this.carStatus = carStatus;
    }

    public long getCarSpeed() {
        return carSpeed;
    }

    public void setCarSpeed(long carSpeed) {
        this.carSpeed = carSpeed;
    }

    public int getProductionYear() {
        return productionYear;
    }

    public void setProductionYear(int productionYear) {
        this.productionYear = productionYear;
    }

    public String getCarQty() {
        return carQty;
    }

    public void setCarQty(String carQty) {
        this.carQty = carQty;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCarDescription() {
        return carDescription;
    }

    public void setCarDescription(String carDescription) {
        this.carDescription = carDescription;
    }

    public String getCarPhoto() {
        return carPhoto;
    }

    public void setCarPhoto(String carPhoto) {
        this.carPhoto = carPhoto;
    }

    // Helper method to get full display name
    public String getFullName() {
        if (trimName != null && !trimName.isEmpty()) {
            return modelName + " " + trimName;
        }
        return modelName;
    }

    @Override
    public String toString() {
        return "car{" +
                "carId=" + carId +
                ", modelName='" + modelName + '\'' +
                ", trimName='" + trimName + '\'' +
                ", carColor='" + carColor + '\'' +
                ", interiorColor='" + interiorColor + '\'' +
                ", fuelType='" + fuelType + '\'' +
                ", carStatus='" + carStatus + '\'' +
                ", carSpeed=" + carSpeed +
                ", productionYear=" + productionYear +
                ", carQty='" + carQty + '\'' +
                ", price=" + price +
                ", carDescription='" + carDescription + '\'' +
                ", carPhoto='" + carPhoto + '\'' +
                '}';
    }
}