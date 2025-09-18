package Model;

public class overviewLine {

    private String dayLabel;
    private double totalSales;

    public overviewLine(String dayLabel, double totalSales) {
        this.dayLabel = dayLabel;
        this.totalSales = totalSales;
    }

    public String getDayLabel() {
        return dayLabel;
    }

    public double getTotalSales() {
        return totalSales;
    }

}
