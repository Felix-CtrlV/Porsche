package Model;

public class overviewBar {

    private String month;
    private double carValue, partValue;

    public overviewBar(){}

    public overviewBar(String month, double carValue, double partValue){
        this.month = month;
        this.carValue = carValue;
        this.partValue = partValue;
    }

    public String getMonth() {
        return month;
    }

    public double getCarValue() {
        return carValue;
    }

    public double getPartValue() {
        return partValue;
    }
}
