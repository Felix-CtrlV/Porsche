package Model;

public class car {
    private int carid;
    private int modelid;
    private String category;
    private double current_price;

    public car(int carid, int modelid, String category, double current_price) {
        this.carid = carid;
        this.modelid = modelid;
        this.category = category;
        this.current_price = current_price;
    }

    public int getCarid() { return carid; }
    public int getModelid() { return modelid; }
    public String getCategory() { return category; }
    public double getCurrent_price() { return current_price; }

    public void setCurrent_price(double price) { this.current_price = price; }

    @Override
    public String toString() {
        return String.format("Car{id=%d, model=%d, cat=%s, price=%.2f}", carid, modelid, category, current_price);
    }
}
