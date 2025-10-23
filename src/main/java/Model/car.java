package Model;

public class car {
    private int carid;
    private int modelid;
    private int quantity;
    private String color;
    private int production_year;
    private Double current_price;
    private String status;

    public car() {}

    public car(int carid, int modelid, int quantity, String color, int production_year, Double current_price, String status) {
        this.carid = carid;
        this.modelid = modelid;
        this.quantity = quantity;
        this.color = color;
        this.production_year = production_year;
        this.current_price = current_price;
        this.status = status;
    }

    public int getCarid() { return carid; }
    public void setCarid(int carid) { this.carid = carid; }
    public int getModelid() { return modelid; }
    public void setModelid(int modelid) { this.modelid = modelid; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public int getProduction_year() { return production_year; }
    public void setProduction_year(int production_year) { this.production_year = production_year; }
    public Double getCurrent_price() { return current_price; }
    public void setCurrent_price(Double current_price) { this.current_price = current_price; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}