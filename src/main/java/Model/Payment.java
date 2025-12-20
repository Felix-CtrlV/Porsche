package Model;

import java.time.LocalDateTime;

public class Payment {
    private Integer orderId;
    private Integer customerId;
    private String customerName;
    private Double totalAmount;
    private Double downPayment;
    private Double monthlyPayment;
    private Integer monthsRemaining;
    private Double paidAmount;
    private Double remainingAmount;
    private String status;
    private LocalDateTime lastPaymentAt;
    private Integer months;
    private Double apr;

    // Getters and setters
    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public Double getDownPayment() { return downPayment; }
    public void setDownPayment(Double downPayment) { this.downPayment = downPayment; }

    public Double getMonthlyPayment() { return monthlyPayment; }
    public void setMonthlyPayment(Double monthlyPayment) { this.monthlyPayment = monthlyPayment; }

    public Integer getMonthsRemaining() { return monthsRemaining; }
    public void setMonthsRemaining(Integer monthsRemaining) { this.monthsRemaining = monthsRemaining; }

    public Double getPaidAmount() { return paidAmount; }
    public void setPaidAmount(Double paidAmount) { this.paidAmount = paidAmount; }

    public Double getRemainingAmount() { return remainingAmount; }
    public void setRemainingAmount(Double remainingAmount) { this.remainingAmount = remainingAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getLastPaymentAt() { return lastPaymentAt; }
    public void setLastPaymentAt(LocalDateTime lastPaymentAt) { this.lastPaymentAt = lastPaymentAt; }

    public Integer getMonths() { return months; }
    public void setMonths(Integer months) { this.months = months; }

    public Double getApr() { return apr; }
    public void setApr(Double apr) { this.apr = apr; }

    @Override
    public String toString() {
        return "Payment{" +
                "orderId=" + orderId +
                ", customerId=" + customerId +
                ", customerName='" + customerName + '\'' +
                ", totalAmount=" + totalAmount +
                ", downPayment=" + downPayment +
                ", monthlyPayment=" + monthlyPayment +
                ", monthsRemaining=" + monthsRemaining +
                ", paidAmount=" + paidAmount +
                ", remainingAmount=" + remainingAmount +
                ", status='" + status + '\'' +
                ", lastPaymentAt=" + lastPaymentAt +
                ", months=" + months +
                ", apr=" + apr +
                '}';
    }
}