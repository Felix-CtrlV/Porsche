package Controllers.java.Model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class orderView {
    private int orderId;
    private String customername;
    private LocalDateTime orderDate;
    private String status;
    private String total;
    private String orderItems;

    public orderView(int orderId, String customername, LocalDateTime orderDate, String status, String total) {
        this.orderId = orderId;
        this.customername = customername;
        this.orderDate = orderDate;
        this.status = status;
        this.total = total;
    }

    public int getOrderId() {
        return orderId;
    }

    public String getCustomername() {
        return customername;
    }

    public LocalDateTime getDate() {
        return orderDate;
    }

    public String getStatus() {
        return status;
    }

    public String getTotal() {
        return total;
    }
    
    public String getOrderItems() {
        return orderItems;
    }
    
    public void setOrderItems(String orderItems) {
        this.orderItems = orderItems;
    }
}
