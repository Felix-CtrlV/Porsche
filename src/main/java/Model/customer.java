package Model;

import java.time.LocalDateTime;

public class customer {
    private int customerid;
    private String customername, nrc, phone, address;
    private LocalDateTime created_at;
    public customer(){}
    public customer(int customerid, String customername, String nrc, String phone, String address, LocalDateTime created_at){
        this.customerid = customerid;
        this.customername = customername;
        this.nrc = nrc;
        this.phone = phone;
        this.address = address;
        this.created_at = created_at;
    }

    public int getCustomerid() {
        return customerid;
    }

    public void setCustomerid(int customerid) {
        this.customerid = customerid;
    }

    public String getCustomername() {
        return customername;
    }

    public void setCustomername(String customername) {
        this.customername = customername;
    }

    public String getNrc() {
        return nrc;
    }

    public void setNrc(String nrc) {
        this.nrc = nrc;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }
}
