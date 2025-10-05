package Model;

public class inventory {
   private int id,qty,productYear;
   private String name,description,intColor,extColor,fuelType,model, trim,forCar,status;
   private double price ;

   public inventory(){}

    //for car
    public inventory(int id, String model, String trim, String intColor, String extColor, String fuelType, int productYear, int qty, double price) {
        this.id = id;
        this.model = model;
        this.trim = trim;
        this.intColor = intColor;
        this.extColor = extColor;
        this.fuelType = fuelType;
        this.productYear = productYear;
        this.qty = qty;
        this.price = price;

    }

    //for part
    public inventory(int id, String name, String forCar, String description, int qty, double price) {
        this.id = id;
        this.name = name;
        this.forCar = forCar;
        this.description = description;
        this.qty = qty;
        this.price = price;
    }

    //for table
    public inventory(int id, String name, int qty, double price, String status) {
        this.id = id;
        this.name = name;
        this.qty = qty;
        this.price = price;
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public int getProductYear() {
        return productYear;
    }

    public void setProductYear(int productYear) {
        this.productYear = productYear;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIntColor() {
        return intColor;
    }

    public void setIntColor(String intColor) {
        this.intColor = intColor;
    }

    public String getExtColor() {
        return extColor;
    }

    public void setExtColor(String extColor) {
        this.extColor = extColor;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getTrim() {
        return trim;
    }

    public void setTrim(String trim) {
        this.trim = trim;
    }

    public String getForCar() {
        return forCar;
    }

    public void setForCar(String forCar) {
        this.forCar = forCar;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
