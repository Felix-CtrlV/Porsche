package Model;

public class managerOverview {
    private double revenue;
    private int carQty,partQty;
    private String monthDate; //Jun 1

    public managerOverview(){}

    public managerOverview(double revenue, int carQty, int partQty, String monthDate) {
        this.revenue = revenue;
        this.carQty = carQty;
        this.partQty = partQty;
        this.monthDate = monthDate;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }

    public int getCarQty() {
        return carQty;
    }

    public void setCarQty(int carQty) {
        this.carQty = carQty;
    }

    public int getPartQty() {
        return partQty;
    }

    public void setPartQty(int partQty) {
        this.partQty = partQty;
    }

    public String getMonthDate() {
        return monthDate;
    }

    public void setMonthDate(String monthDate) {
        this.monthDate = monthDate;
    }
}
