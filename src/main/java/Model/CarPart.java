package Model;

public class CarPart {

    private int partId;
    private String partName;
    private int forCar;
    private String description;
    private int partQty;
    private double price;
    private int partStatus;
    private String partPhoto;

    public CarPart(int partId, String partName, int forCar, String description,
                   int partQty, double price, int partStatus, String partPhoto) {
        this.partId = partId;
        this.partName = partName;
        this.forCar = forCar;
        this.description = description;
        this.partQty = partQty;
        this.price = price;
        this.partStatus = partStatus;
        this.partPhoto = partPhoto;
    }

    // Getters
    public int getPartId() { return partId; }
    public String getPartName() { return partName; }
    public int getForCar() { return forCar; }
    public String getDescription() { return description; }
    public int getPartQty() { return partQty; }
    public double getPrice() { return price; }
    public int getPartStatus() { return partStatus; }
    public String getPartPhoto() { return partPhoto; }

    // Setters
    public void setPartId(int partId) { this.partId = partId; }
    public void setPartName(String partName) { this.partName = partName; }
    public void setForCar(int forCar) { this.forCar = forCar; }
    public void setDescription(String description) { this.description = description; }
    public void setPartQty(int partQty) { this.partQty = partQty; }
    public void setPrice(double price) { this.price = price; }
    public void setPartStatus(int partStatus) { this.partStatus = partStatus; }
    public void setPartPhoto(String partPhoto) { this.partPhoto = partPhoto; }

    @Override
    public String toString() {
        return "CarPart{" +
                "partId=" + partId +
                ", partName='" + partName + '\'' +
                ", forCar=" + forCar +
                ", description='" + description + '\'' +
                ", partQty=" + partQty +
                ", price=" + price +
                ", partStatus=" + partStatus +
                ", partPhoto='" + partPhoto + '\'' +
                '}';
    }
}