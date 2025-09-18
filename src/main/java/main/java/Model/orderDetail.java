package main.java.Model;

public class orderDetail {
    private int detailid, orderid, customizeid, carid, partid, quantity, price;
    public orderDetail(){}
    public orderDetail(int detailid, int orderid, int customizeid, int carid, int partid, int quantity, int price){
        this.detailid = detailid;
        this.orderid = orderid;
        this.customizeid = customizeid;
        this.carid = carid;
        this.partid = partid;
        this.quantity = quantity;
        this.price = price;
    }

    public int getDetailid() {
        return detailid;
    }

    public void setDetailid(int detailid) {
        this.detailid = detailid;
    }

    public int getOrderid() {
        return orderid;
    }

    public void setOrderid(int orderid) {
        this.orderid = orderid;
    }

    public int getCustomizeid() {
        return customizeid;
    }

    public void setCustomizeid(int customizeid) {
        this.customizeid = customizeid;
    }

    public int getCarid() {
        return carid;
    }

    public void setCarid(int carid) {
        this.carid = carid;
    }

    public int getPartid() {
        return partid;
    }

    public void setPartid(int partid) {
        this.partid = partid;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
