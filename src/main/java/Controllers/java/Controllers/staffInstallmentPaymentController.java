package Controllers.java.Controllers;

import Controllers.java.DAO.InstallmentDAO;
import Controllers.java.DAO.OrderDAO;
import Controllers.java.DAO.PaymentDAO;
import Controllers.java.Model.CarConfiguration;
import Controllers.java.Model.Installment;
import Controllers.java.Model.Payment;
import Controllers.java.Utils.SessionStaff;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class staffInstallmentPaymentController implements Initializable {

    @FXML private Label orderIdLabel;
    @FXML private Label customerIdLabel;
    @FXML private Label carModelLabel;
    @FXML private Label totalAmountLabel;
    @FXML private Label totalInstallmentsLabel;
    @FXML private Label paidInstallmentsLabel;
    @FXML private Label remainingInstallmentsLabel;
    @FXML private Label summaryTotalAmountLabel;
    @FXML private Label paidAmountLabel;
    @FXML private Label remainingAmountLabel;
    @FXML private Label progressLabel;
    @FXML private Label nextDueDateLabel;

    @FXML private TableView<Installment> installmentsTable;
    @FXML private TableColumn<Installment, String> numberColumn;
    @FXML private TableColumn<Installment, String> dueDateColumn;
    @FXML private TableColumn<Installment, String> amountColumn;
    @FXML private TableColumn<Installment, String> statusColumn;
    @FXML private TableColumn<Installment, String> actionColumn;

    @FXML private Button backButton;
    @FXML private Button payAllButton;
    @FXML private Button refreshButton;

    private InstallmentDAO installmentDAO;
    private PaymentDAO paymentDAO;
    private OrderDAO orderDAO;
    private int currentOrderId;
    private int currentCustomerId;

    private ObservableList<Installment> installmentsData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        installmentDAO = new InstallmentDAO();
        paymentDAO = new PaymentDAO();
        orderDAO = new OrderDAO();

        setupTableColumns();
        setupEventHandlers();

        loadOrderData();
        refreshData();
    }

    private void setupTableColumns() {
        numberColumn.setCellValueFactory(new PropertyValueFactory<>("installmentNumber"));

        dueDateColumn.setCellValueFactory(cellData -> {
            LocalDate dueDate = cellData.getValue().getDueDate();
            return new SimpleStringProperty(dueDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        });

        amountColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(String.format("$%,.2f", cellData.getValue().getInstallmentAmount())));

        statusColumn.setCellValueFactory(cellData -> {
            boolean isFinished = cellData.getValue().isFinished();
            return new SimpleStringProperty(isFinished ? "PAID" : "PENDING");
        });

        actionColumn.setCellFactory(param -> new TableCell<Installment, String>() {
            private final Button payButton = new Button("Pay Now");

            {
                payButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
                payButton.setOnAction(event -> {
                    Installment installment = getTableView().getItems().get(getIndex());
                    if (!installment.isFinished()) {
                        processInstallmentPayment(installment);
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Installment installment = getTableView().getItems().get(getIndex());
                    if (installment.isFinished()) {
                        setGraphic(null);
                    } else {
                        setGraphic(payButton);
                    }
                }
            }
        });

        installmentsTable.setItems(installmentsData);
    }

    private void setupEventHandlers() {
        backButton.setOnAction(event -> goBackToCart());
        refreshButton.setOnAction(event -> refreshData());
        payAllButton.setOnAction(event -> payAllPendingInstallments());
    }

    private void loadOrderData() {
        CarConfiguration carConfig = SessionStaff.getInstance().getCarConfiguration();

        if (carConfig != null) {
            carModelLabel.setText(carConfig.getModelName());
            totalAmountLabel.setText(String.format("$%,.2f", carConfig.getTotalPrice()));
        }

        try {
            Payment latestPayment = getLatestInstallmentPayment();
            if (latestPayment != null) {
                currentOrderId = latestPayment.getOrderId();
                currentCustomerId = latestPayment.getCustomerId();

                orderIdLabel.setText(String.valueOf(currentOrderId));
                customerIdLabel.setText(String.valueOf(currentCustomerId));
            } else {
                showAlert("Information", "No installment payment records found.");
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to load order data: " + e.getMessage());
        }
    }

    private Payment getLatestInstallmentPayment() {
        try {
            List<Payment> payments = paymentDAO.getAllPayments();
            for (Payment payment : payments) {
                if (payment.getMonths() != null && payment.getMonths() > 0 && "ACTIVE".equals(payment.getStatus())) {
                    return payment;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void refreshData() {
        if (currentOrderId <= 0) {
            return;
        }

        try {
            List<Installment> installments = installmentDAO.getInstallmentsByOrderId(currentOrderId);
            installmentsData.setAll(installments);
            updateSummary(installments);

        } catch (Exception e) {
            showAlert("Error", "Failed to refresh data: " + e.getMessage());
        }
    }

    private void updateSummary(List<Installment> installments) {
        if (installments.isEmpty()) {
            clearSummary();
            return;
        }

        int total = installments.size();
        int paid = 0;
        double totalAmount = 0;
        double paidAmount = 0;
        LocalDate nextDueDate = null;

        for (Installment installment : installments) {
            totalAmount += installment.getInstallmentAmount();
            if (installment.isFinished()) {
                paid++;
                paidAmount += installment.getInstallmentAmount();
            } else {
                if (nextDueDate == null || installment.getDueDate().isBefore(nextDueDate)) {
                    nextDueDate = installment.getDueDate();
                }
            }
        }

        int remaining = total - paid;
        double remainingAmount = totalAmount - paidAmount;
        double progress = totalAmount > 0 ? (paidAmount / totalAmount) * 100 : 0;

        totalInstallmentsLabel.setText(String.valueOf(total));
        paidInstallmentsLabel.setText(String.valueOf(paid));
        remainingInstallmentsLabel.setText(String.valueOf(remaining));
        summaryTotalAmountLabel.setText(String.format("$%,.2f", totalAmount));
        paidAmountLabel.setText(String.format("$%,.2f", paidAmount));
        remainingAmountLabel.setText(String.format("$%,.2f", remainingAmount));
        progressLabel.setText(String.format("%.1f%%", progress));

        if (nextDueDate != null) {
            nextDueDateLabel.setText(nextDueDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        } else {
            nextDueDateLabel.setText("N/A");
        }
    }

    private void clearSummary() {
        totalInstallmentsLabel.setText("0");
        paidInstallmentsLabel.setText("0");
        remainingInstallmentsLabel.setText("0");
        summaryTotalAmountLabel.setText("$0.00");
        paidAmountLabel.setText("$0.00");
        remainingAmountLabel.setText("$0.00");
        progressLabel.setText("0%");
        nextDueDateLabel.setText("N/A");
    }

    private void processInstallmentPayment(Installment installment) {
        try {
            boolean success = installmentDAO.markInstallmentAsPaid(installment.getInstallmentId());

            if (success) {
                updatePaymentRecord(installment);
                refreshData();

                showAlert("Success",
                        "Installment payment processed successfully!\n\n" +
                                "Installment #" + installment.getInstallmentNumber() + "\n" +
                                "Amount: $" + String.format("%,.2f", installment.getInstallmentAmount()) + "\n" +
                                "Due Date: " + installment.getDueDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
                );
            } else {
                showAlert("Error", "Failed to process installment payment.");
            }

        } catch (Exception e) {
            showAlert("Error", "An error occurred while processing payment: " + e.getMessage());
        }
    }

    private void updatePaymentRecord(Installment installment) {
        try {
            Payment payment = paymentDAO.getByOrderId(currentOrderId);
            if (payment != null) {
                double newPaidAmount = (payment.getPaidAmount() != null ? payment.getPaidAmount() : 0) + installment.getInstallmentAmount();
                double newRemainingAmount = (payment.getRemainingAmount() != null ? payment.getRemainingAmount() : 0) - installment.getInstallmentAmount();
                int newMonthsRemaining = (payment.getMonthsRemaining() != null ? payment.getMonthsRemaining() : 0) - 1;

                payment.setPaidAmount(newPaidAmount);
                payment.setRemainingAmount(newRemainingAmount);
                payment.setMonthsRemaining(newMonthsRemaining);
                payment.setLastPaymentAt(java.time.LocalDateTime.now());

                if (newRemainingAmount <= 0) {
                    payment.setStatus("COMPLETED");
                }

                paymentDAO.updatePayment(payment);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void payAllPendingInstallments() {
        try {
            List<Installment> pendingInstallments = installmentDAO.getPendingInstallments(currentOrderId);
            if (pendingInstallments.isEmpty()) {
                showAlert("Information", "No pending installments to pay.");
                return;
            }

            double totalAmount = pendingInstallments.stream()
                    .mapToDouble(Installment::getInstallmentAmount)
                    .sum();

            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirm Bulk Payment");
            confirmation.setHeaderText("Pay All Pending Installments");
            confirmation.setContentText(
                    "Are you sure you want to pay all " + pendingInstallments.size() + " pending installments?\n" +
                            "Total Amount: $" + String.format("%,.2f", totalAmount)
            );

            if (confirmation.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                for (Installment installment : pendingInstallments) {
                    installmentDAO.markInstallmentAsPaid(installment.getInstallmentId());
                }

                updatePaymentRecordForBulkPayment(pendingInstallments);
                refreshData();

                showAlert("Success",
                        "All " + pendingInstallments.size() + " installments paid successfully!\n" +
                                "Total Amount: $" + String.format("%,.2f", totalAmount)
                );
            }

        } catch (Exception e) {
            showAlert("Error", "An error occurred while processing bulk payment: " + e.getMessage());
        }
    }

    private void updatePaymentRecordForBulkPayment(List<Installment> installments) {
        try {
            Payment payment = paymentDAO.getByOrderId(currentOrderId);
            if (payment != null) {
                double totalPaid = installments.stream()
                        .mapToDouble(Installment::getInstallmentAmount)
                        .sum();

                payment.setPaidAmount((payment.getPaidAmount() != null ? payment.getPaidAmount() : 0) + totalPaid);
                payment.setRemainingAmount((payment.getRemainingAmount() != null ? payment.getRemainingAmount() : 0) - totalPaid);
                payment.setMonthsRemaining((payment.getMonthsRemaining() != null ? payment.getMonthsRemaining() : 0) - installments.size());
                payment.setLastPaymentAt(java.time.LocalDateTime.now());

                if (payment.getRemainingAmount() != null && payment.getRemainingAmount() <= 0) {
                    payment.setStatus("COMPLETED");
                }

                paymentDAO.updatePayment(payment);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goBackToCart() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/View/staffShopingCart.fxml"));
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            showAlert("Navigation Error", "Failed to go back to shopping cart: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setOrderData(int orderId, int customerId) {
        this.currentOrderId = orderId;
        this.currentCustomerId = customerId;
        orderIdLabel.setText(String.valueOf(orderId));
        customerIdLabel.setText(String.valueOf(customerId));
        refreshData();
    }
}