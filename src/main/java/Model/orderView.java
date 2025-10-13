package Model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class orderView {
    private int orderId;
    private String customername;
    private String carName;
    private LocalDateTime orderDate;
    private String status;
    private String total;

    public orderView(int orderId, String customername, String carName, LocalDateTime orderDate, String status, String total) {
        this.orderId = orderId;
        this.customername = customername;
        this.carName = carName;
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

    public String getCarName() {
        return carName != null ? carName : "N/A";
    }

    public String getDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return orderDate.format(formatter);
    }


    public String getStatus() {
        return status;
    }

    public String getTotal() {
        return total;
    }
}
