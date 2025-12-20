package Controllers;

import DAO.PaymentDAO;
import DAO.InstallmentDAO;
import Model.Payment;
import Model.Installment;
import Utils.DarkModeManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class PaymentDetailsController {
    @FXML private TableView<Payment> paymentsTable;
    @FXML private TableColumn<Payment, Integer> colOrderId;
    @FXML private TableColumn<Payment, Integer> colCustomerId;
    @FXML private TableColumn<Payment, String> colCustomerName;
    @FXML private TableColumn<Payment, Double> colTotal;
    @FXML private TableColumn<Payment, Double> colDown;
    @FXML private TableColumn<Payment, Double> colMonthly;
    @FXML private TableColumn<Payment, Integer> colMonthsRem;
    @FXML private TableColumn<Payment, Double> colPaid;
    @FXML private TableColumn<Payment, Double> colRemain;
    @FXML private TableColumn<Payment, String> colStatus;
    @FXML private TableColumn<Payment, String> colLastPay;
    @FXML private TextField orderIdInput;
    @FXML private TextField searchField;

    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final InstallmentDAO installmentDAO = new InstallmentDAO();
    private final ObservableList<Payment> items = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (paymentsTable != null && paymentsTable.getScene() != null) {
            DarkModeManager.getInstance().registerScene(paymentsTable.getScene());
        }
        setupTable();
        loadAll();
    }

    private void setupTable() {
        colOrderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        colCustomerId.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        colCustomerName.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colDown.setCellValueFactory(new PropertyValueFactory<>("downPayment"));
        colMonthly.setCellValueFactory(new PropertyValueFactory<>("monthlyPayment"));
        colMonthsRem.setCellValueFactory(new PropertyValueFactory<>("monthsRemaining"));
        colPaid.setCellValueFactory(new PropertyValueFactory<>("paidAmount"));
        colRemain.setCellValueFactory(new PropertyValueFactory<>("remainingAmount"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colLastPay.setCellValueFactory(cell -> {
            if (cell.getValue().getLastPaymentAt() == null) return new javafx.beans.property.SimpleStringProperty("-");
            return new javafx.beans.property.SimpleStringProperty(cell.getValue().getLastPaymentAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        });
        paymentsTable.setItems(items);
        paymentsTable.getColumns().forEach(col -> col.setResizable(false));
        paymentsTable.prefWidthProperty().bind(((VBox)paymentsTable.getParent()).widthProperty());
        paymentsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
    }

    private void loadAll() {
        items.clear();
        List<Payment> allPayments = paymentDAO.searchByCustomerName("");
        items.addAll(allPayments);
    }

    @FXML
    private void onSearch() {
        String q = searchField.getText();
        if (q == null || q.trim().isEmpty()) {
            loadAll();
            return;
        }
        List<Payment> list = paymentDAO.searchByCustomerName(q.trim());
        items.setAll(list);
    }

    @FXML
    private void onRefresh() {
        onSearch();
    }

    @FXML
    private void onRecordMonthly() {
        String text = orderIdInput.getText();
        if (text == null || text.trim().isEmpty()) {
            alert("Validation", "Enter Order ID to record payment");
            return;
        }
        try {
            int orderId = Integer.parseInt(text.trim());
            Payment p = paymentDAO.getByOrderId(orderId);
            if (p == null) {
                alert("Not Found", "No payment plan found for Order ID: " + orderId);
                return;
            }

            // Get next installment number
            List<Installment> pendingInstallments = installmentDAO.getPendingInstallments(orderId);
            if (pendingInstallments.isEmpty()) {
                alert("Info", "All installments are already paid.");
                return;
            }

            int nextInstallmentNumber = pendingInstallments.get(0).getInstallmentNumber();

            boolean ok = paymentDAO.recordMonthlyPaymentWithInstallment(orderId, p.getMonthlyPayment(), nextInstallmentNumber);
            if (ok) {
                alert("Success", "Recorded installment #" + nextInstallmentNumber + ". Remaining: " + Math.max(0, p.getRemainingAmount() - p.getMonthlyPayment()));
                onSearch();
            } else {
                alert("Error", "Failed to record payment.");
            }
        } catch (NumberFormatException ex) {
            alert("Validation", "Order ID must be a number");
        }
    }

    private void alert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}