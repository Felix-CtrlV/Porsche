package Model;

public class overviewPie {

    private String label;
    private double totalSale;

    public overviewPie() {
    }

    public overviewPie(String label, double totalSale){
        this.label = label;
        this.totalSale = totalSale;
    }

    public String getLabel() {
        return label;
    }

    public double getTotalSale() {
        return totalSale;
    }

}
