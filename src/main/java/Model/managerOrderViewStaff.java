package Model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class managerOrderViewStaff {
    private Integer no,order_id;
    private String cus_name,is_installmenat,carsandparts_name,carsandparts_qty,carsandparts_perprice;

    private double total_amount,payed_amount,remain_amount;
    private Date order_date,due_date;

    public managerOrderViewStaff(){}
    public managerOrderViewStaff(int no, int order_id, String cus_name, Date order_date, double total_amount, String is_installmenat, String carsandparts_name, String carsandparts_qty, double payed_amount, double remain_amount, Date due_date) {
        this.no = no;
        this.order_id = order_id;
        this.cus_name = cus_name;
        this.is_installmenat = is_installmenat;
        this.carsandparts_name = carsandparts_name;
        this.carsandparts_qty = carsandparts_qty;
        this.total_amount = total_amount;
        this.payed_amount = payed_amount;
        this.remain_amount = remain_amount;
        this.order_date = order_date;
        this.due_date = due_date;
    }



    public Double[] getCarsandparts_perprice() {
        String []price = this.carsandparts_perprice.split(",");
        Double[] result = new Double[price.length];

        for (int i =0; i<price.length;i++){
            result[i] = Double.parseDouble(price[i]);
        }
        return result;
    }

    public void setCarsandparts_perprice(String carsandparts_perprice) {
        this.carsandparts_perprice = carsandparts_perprice;
    }

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public int getOrder_id() {
        return order_id;
    }

    public void setOrder_id(int order_id) {
        this.order_id = order_id;
    }

    public String getCus_name() {
        return cus_name;
    }

    public void setCus_name(String cus_name) {
        this.cus_name = cus_name;
    }

    public String getIs_installmenat() {
        return is_installmenat;
    }

    public void setIs_installmenat(String is_installmenat) {
        this.is_installmenat = is_installmenat;
    }

    public String[] getCarsandparts_name() {
        String[] result = carsandparts_name.split(",");
        return  result;
    }

    public void setCarsandparts_name(String carsandparts_name) {
        this.carsandparts_name = carsandparts_name;
    }

    public Integer[] getCarsandparts_qty() {
        String [] qty = carsandparts_qty.split(",");
        Integer[] result = new Integer[qty.length];
        for(int i=0;i<qty.length;i++){
            result[i] = Integer.parseInt(qty[i]);
        }
        return result;
    }

    public void setCarsandparts_qty(String carsandparts_qty) {
        this.carsandparts_qty = carsandparts_qty;
    }

    public double getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(double total_amount) {
        this.total_amount = total_amount;
    }

    public double getPayed_amount() {
        return payed_amount;
    }

    public void setPayed_amount(double payed_amount) {
        this.payed_amount = payed_amount;
    }

    public double getRemain_amount() {
        return remain_amount;
    }

    public void setRemain_amount(double remain_amount) {
        this.remain_amount = remain_amount;
    }

    public Date getOrder_date() {
        return order_date;
    }

    public void setOrder_date(Date order_date) {
        this.order_date = order_date;
    }

    public Date getDue_date() {
        return due_date;
    }

    public void setDue_date(Date due_date) {
        this.due_date = due_date;
    }



}