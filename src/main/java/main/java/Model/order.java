package main.java.Model;

import java.time.LocalDateTime;

public class order {
    private int orderid, userid, customerid;
    private LocalDateTime order_date;
    private String status;
    public order(){}
    public order(int orderid, int userid, int customerid, LocalDateTime order_date, String status){
        this.orderid = orderid;
        this.customerid = customerid;
        this.order_date = order_date;
        this.userid = userid;
        this.status = status;
    }

    public int getOrderid() {
        return orderid;
    }

    public void setOrderid(int orderid) {
        this.orderid = orderid;
    }

    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public int getCustomerid() {
        return customerid;
    }

    public void setCustomerid(int customerid) {
        this.customerid = customerid;
    }

    public LocalDateTime getOrder_date() {
        return order_date;
    }

    public void setOrder_date(LocalDateTime order_date) {
        this.order_date = order_date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
