package Model;

public class inventory {
    private int id,qty;
    private String name,status;
    private double price;

    //for inventory of manger view
    public inventory(int id, int qty, String name, double price) {
        this.id = id;
        this.qty = qty;
        this.name = name;
        if(qty >0){
            this.status = "On";
        }else{
            this.status = "Out";
        }
        this.price = price;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
