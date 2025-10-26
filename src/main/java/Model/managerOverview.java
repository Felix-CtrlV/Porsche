package Model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class managerOverview {
    //bar chart (qty ) and area chart(revenue)
    private double revenue;
    private int carSoldQty,partSoldQty;
    private String monthDate; //Jun 1

    //for best_selling car and parts
    private int soldQty,targetQty,rank;
    private String inventoryName, imageUrl;

    //for best_Seller
    private int staffId,workHour,prevWorkHour;
    private String staffPhoto,staffName;
    private Double totalSale,prevTotalSale;

    public managerOverview(){}

    //for line and bar chart
    public managerOverview(double revenue, int carSoldQty, int partSoldQty, String monthDate) {
        this.revenue = revenue;
        this.carSoldQty = carSoldQty;
        this.partSoldQty = partSoldQty;
        this.monthDate = monthDate;
    }

    //for best_selling cars and parts
    public managerOverview(int rank, int targetQty, int soldQty, String inventoryName) {
        this.rank = rank;
        this.targetQty = targetQty;
        this.soldQty = soldQty;
        this.inventoryName = inventoryName;
        this.imageUrl = "";
    }
    
    public managerOverview(int rank, int targetQty, int soldQty, String inventoryName, String imageUrl) {
        this.rank = rank;
        this.targetQty = targetQty;
        this.soldQty = soldQty;
        this.inventoryName = inventoryName;
        this.imageUrl = imageUrl != null ? imageUrl : "";
    }

    //for best_seller
    public managerOverview(int rank ,int staffId, int workHour,int prevWorkHour, String staffPhoto, String staffName, Double totalSale,Double prevTotalSale) {
        this.rank = rank;
        this.staffId = staffId;
        this.workHour = workHour;
        this.staffPhoto = staffPhoto;
        this.staffName = staffName;
        this.totalSale = totalSale;
        this.prevWorkHour =prevWorkHour;
        this.prevTotalSale = prevTotalSale;
    }

    public int getPrevWorkHour() {
        return prevWorkHour;
    }

    public void setPrevWorkHour(int prevWorkHour) {
        this.prevWorkHour = prevWorkHour;
    }

    public Double getPrevTotalSale() {
        return prevTotalSale;
    }

    public void setPrevTotalSale(Double prevTotalSale) {
        this.prevTotalSale = prevTotalSale;
    }

    public Double getTotalSale() {
        return totalSale;
    }

    public void setTotalSale(Double totalSale) {
        this.totalSale = totalSale;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public String getStaffPhoto() {
        return staffPhoto;
    }

    public void setStaffPhoto(String staffPhoto) {
        this.staffPhoto = staffPhoto;
    }

    public int getWorkHour() {
        return workHour;
    }

    public void setWorkHour(int workHour) {
        this.workHour = workHour;
    }

    public int getStaffId() {
        return staffId;
    }

    public void setStaffId(int staffId) {
        this.staffId = staffId;
    }

    public String getInventoryName() {
        return inventoryName;
    }

    public void setInventoryName(String inventoryName) {
        this.inventoryName = inventoryName;
    }
    
    public String getImageUrl() {
        return imageUrl != null ? imageUrl : "";
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl != null ? imageUrl : "";
    }

    public String getRank() {
        String result ="";
        result = "#"+String.valueOf(this.rank);
        return result;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getTargetQty() {
        return targetQty;
    }

    public void setTargetQty(int targetQty) {
        this.targetQty = targetQty;
    }

    public int getSoldQty() {
        return soldQty;
    }

    public void setSoldQty(int soldQty) {
        this.soldQty = soldQty;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }

    public int getCarSoldQty() {
        return carSoldQty;
    }

    public void setCarSoldQty(int carSoldQty) {
        this.carSoldQty = carSoldQty;
    }

    public int getPartSoldQty() {
        return partSoldQty;
    }

    public void setPartSoldQty(int partSoldQty) {
        this.partSoldQty = partSoldQty;
    }

    public String getMonthDate() {
        return monthDate;
    }

    public void setMonthDate(Date date) {
        if (date != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("MMM d");
            this.monthDate = formatter.format(date);
        } else {
            this.monthDate = "Unknown";
        }
    }
}
