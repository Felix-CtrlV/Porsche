package Model;

public class inventory {
   private int id,qty,productYear;
   private String name,description,intColor,extColor,fuelType,series,forCar,status,models,inventoryId,photo;
   private double price ;

   public inventory(){}

    //for car table
    public inventory(int id ,String inventoryId, String name, String extColor, String intColor, String fuelType, int productYear, int qty, double price, String status,String photo) {
        this.inventoryId = inventoryId;
        this.name = name;
        this.setSeries(name);
        this.setModels(name);
        this.intColor = intColor;
        this.extColor = extColor;
        this.fuelType = fuelType;
        this.productYear = productYear;
        this.qty = qty;
        this.price = price;
        this.status = status;
        this.photo = photo;
    }

    //for part table
    public inventory(int id ,String inventoryId, String name, String forCar, String description, int qty, double price, String status,String photo) {
       this.id = id;
        this.inventoryId = inventoryId;
        this.name = name;
        this.forCar = forCar;
        this.description = description;
        this.qty = qty;
        this.price = price;
        this.status = status;
        this.photo = photo;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(String inventoryId) {
        this.inventoryId = inventoryId;
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

    public String getSeries() {
        return series;
    }

    public void setSeries(String in) {
        String [] check = in.split(" ",2);
        this.series = check[0];
        if(check.length>1){
            setModels(check[1]);
            System.out.println(check[1]);
        }else {
            setModels(" ");
        }
    }

    public String getModels() {
        return models;
    }

    public void setModels(String in) {
        this.models = in;
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
