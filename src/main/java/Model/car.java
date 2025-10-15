package Model;

public class car {
    private int carid;
    private int modelid;
    private String category;
    private String color;
    private double current_price;

    public car(int carid, int modelid, String category, double current_price) {
        this.carid = carid;
        this.modelid = modelid;
        this.category = category;
        this.current_price = current_price;
    }

    public car(int carid, int modelid, String category, String color, double current_price) {
        this.carid = carid;
        this.modelid = modelid;
        this.category = category;
        this.color = color;
        this.current_price = current_price;
    }

    public int getCarid() { return carid; }
    public int getModelid() { return modelid; }
    public String getCategory() { return category; }
    public String getColor() { return color; }
    public double getCurrent_price() { return current_price; }

    public void setColor(String color) { this.color = color; }
    public void setCurrent_price(double price) { this.current_price = price; }

    @Override
    public String toString() {
        return String.format("Car{id=%d, model=%d, cat=%s, color=%s, price=%.2f}", carid, modelid, category, color, current_price);
    }
}
