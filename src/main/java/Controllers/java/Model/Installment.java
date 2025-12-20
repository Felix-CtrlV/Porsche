package Controllers.java.Model;

import java.time.LocalDate;

public class Installment {
    private int installmentId;
    private int orderId;
    private int installmentNumber;
    private LocalDate dueDate;
    private double installmentAmount;
    private boolean isFinished;

    public Installment() {}

    public Installment(int orderId, int installmentNumber, LocalDate dueDate, double installmentAmount, boolean isFinished) {
        this.orderId = orderId;
        this.installmentNumber = installmentNumber;
        this.dueDate = dueDate;
        this.installmentAmount = installmentAmount;
        this.isFinished = isFinished;
    }

    public Installment(int installmentId, int orderId, int installmentNumber, LocalDate dueDate, double installmentAmount, boolean isFinished) {
        this.installmentId = installmentId;
        this.orderId = orderId;
        this.installmentNumber = installmentNumber;
        this.dueDate = dueDate;
        this.installmentAmount = installmentAmount;
        this.isFinished = isFinished;
    }

    public int getInstallmentId() { return installmentId; }
    public void setInstallmentId(int installmentId) { this.installmentId = installmentId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getInstallmentNumber() { return installmentNumber; }
    public void setInstallmentNumber(int installmentNumber) { this.installmentNumber = installmentNumber; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public double getInstallmentAmount() { return installmentAmount; }
    public void setInstallmentAmount(double installmentAmount) { this.installmentAmount = installmentAmount; }

    public boolean isFinished() { return isFinished; }
    public void setFinished(boolean finished) { isFinished = finished; }

    public String getStatus() {
        LocalDate today = LocalDate.now();
        if (isFinished) {
            return "PAID";
        } else if (dueDate.isBefore(today)) {
            return "OVERDUE";
        } else {
            return "PENDING";
        }
    }

    @Override
    public String toString() {
        return "Installment{" +
                "installmentId=" + installmentId +
                ", orderId=" + orderId +
                ", installmentNumber=" + installmentNumber +
                ", dueDate=" + dueDate +
                ", installmentAmount=" + installmentAmount +
                ", isFinished=" + isFinished +
                '}';
    }
}