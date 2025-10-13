package Model;

import java.util.Date;

public class managerOrderView {
    private Integer no,order_id,totalQty;
    private String cus_name,is_installmenat,carsandparts_name,carsandparts_qty,carsandparts_perprice,staff_name;


    private double total_amount,payed_amount,remain_amount;
    private Date order_date,due_date;

    public managerOrderView(){}
    public managerOrderView(int no, int order_id, String cus_name, Date order_date, double total_amount, String is_installmenat, String carsandparts_name, String  carsandparts_qty, String  carsandparts_perprice, double payed_amount, double remain_amount, Date due_date) {
        this.no = no;
        this.order_id = order_id;
        this.cus_name = cus_name;
        this.is_installmenat = is_installmenat;
        this.carsandparts_name = carsandparts_name;
        this.carsandparts_qty = carsandparts_qty;
        this.carsandparts_perprice = carsandparts_perprice;
        this.total_amount = total_amount;
        this.payed_amount = payed_amount;
        this.remain_amount = remain_amount;
        this.order_date = order_date;
        this.due_date = due_date;
    }

    //for manager order management
    public managerOrderView(Integer order_id, Date order_date, String cus_name, String staff_name, Integer totalQty, double total_amount, String is_installmenat, double payed_amount, double remain_amount, Date due_date, String carsandparts_name, String carsandparts_qty, String carsandparts_perprice) {
        this.order_id = order_id;
        this.order_date = order_date;
        this.cus_name = cus_name;
        this.staff_name = staff_name;
        this.totalQty = totalQty;
        this.total_amount = total_amount;
        this.is_installmenat = is_installmenat;
        this.payed_amount = payed_amount;
        this.remain_amount = remain_amount;
        this.due_date = due_date;
        this.carsandparts_name = carsandparts_name;
        this.carsandparts_qty = carsandparts_qty;
        this.carsandparts_perprice = carsandparts_perprice;
    }

    public Integer getTotalQty() {
        return totalQty;
    }

    public void setTotalQty(Integer totalQty) {
        this.totalQty = totalQty;
    }

    public void setOrder_id(Integer order_id) {
        this.order_id = order_id;
    }

    public Integer getNo() {
        return no;
    }

    public void setNo(Integer no) {
        this.no = no;
    }

    public String getStaff_name() {
        return staff_name;
    }

    public void setStaff_name(String staff_name) {
        this.staff_name = staff_name;
    }

    public String[] getCarsandparts_name() {
        if (carsandparts_name == null || carsandparts_name.isEmpty()) {
            return new String[0];
        }
        String[] result = carsandparts_name.split(",");
        for (int i = 0; i < result.length; i++) {
            result[i] = result[i].trim();
        }
        return result;
    }

    public String[] getCarsandparts_perprice() {
        if (carsandparts_perprice == null || carsandparts_perprice.isEmpty()) {
            return new String[0];
        }
        String[] price = this.carsandparts_perprice.split(",");
        for (int i = 0; i < price.length; i++) {
            price[i] = price[i].trim();
        }
        return price;
    }
    public String[] getCarsandparts_qty() {
        if (carsandparts_qty == null || carsandparts_qty.isEmpty()) {
            return new String[0];
        }
        String[] qty = carsandparts_qty.split(",");
        for (int i = 0; i < qty.length; i++) {
            qty[i] = qty[i].trim();
        }
        return qty;
    }

    public void setCarsandparts_perprice(String carsandparts_perprice) {
        this.carsandparts_perprice = carsandparts_perprice;
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



    public void setCarsandparts_name(String carsandparts_name) {
        this.carsandparts_name = carsandparts_name;
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