package Model;

public class Accessory {
    private int partId;
    private String partName;
    private String description;
    private String partCity;
    private double price;
    private int partStatus;
    private String partPhoto;

    public Accessory() {}

    public Accessory(int partId, String partName, String description, String partCity,
                     double price, int partStatus, String partPhoto) {
        this.partId = partId;
        this.partName = partName;
        this.description = description;
        this.partCity = partCity;
        this.price = price;
        this.partStatus = partStatus;
        this.partPhoto = partPhoto;
    }

    public int getPartId() {
        return partId;
    }

    public void setPartId(int partId) {
        this.partId = partId;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPartCity() {
        return partCity;
    }

    public void setPartCity(String partCity) {
        this.partCity = partCity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getPartStatus() {
        return partStatus;
    }

    public void setPartStatus(int partStatus) {
        this.partStatus = partStatus;
    }

    public String getPartPhoto() {
        return partPhoto;
    }

    public void setPartPhoto(String partPhoto) {
        this.partPhoto = partPhoto;
    }

    public String getCategoryFromCity() {
        if (partCity == null) return "accessories";
        String city = partCity.toLowerCase();
        if (city.contains("equipment") || city.contains("tool")) {
            return "equipment";
        }
        return "accessories";
    }
}