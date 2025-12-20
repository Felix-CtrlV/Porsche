package Controllers.java.DAO;

import Model.Payment;
import Database.DatabaseConnectionManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {

    public List<Payment> searchByCustomerName(String customerNameQuery) {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT o.order_id, o.customer_id, c.customer_name, o.paid_amount as total_amount, " +
                "o.order_status as payment_status, o.order_date as last_payment_date, " +
                "o.is_installment, " +
                "(SELECT COUNT(*) FROM installment_list i WHERE i.order_id = o.order_id AND i.is_finished = 1) as payments_made, " +
                "(SELECT COUNT(*) FROM installment_list i WHERE i.order_id = o.order_id) as total_installments " +
                "FROM orders o " +
                "JOIN customer_info c ON o.customer_id = c.customer_id " +
                "WHERE c.customer_name LIKE ?";

        try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, "%" + customerNameQuery + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Payment payment = new Payment();
                payment.setOrderId(rs.getInt("order_id"));
                payment.setCustomerId(rs.getInt("customer_id"));
                payment.setCustomerName(rs.getString("customer_name"));
                payment.setTotalAmount(rs.getDouble("total_amount"));
                payment.setStatus(rs.getString("payment_status"));

                boolean isInstallment = rs.getBoolean("is_installment");
                if (isInstallment) {
                    int paymentsMade = rs.getInt("payments_made");
                    int totalInstallments = rs.getInt("total_installments");
                    double totalAmount = payment.getTotalAmount();

                    payment.setMonthsRemaining(totalInstallments - paymentsMade);
                    payment.setPaidAmount((paymentsMade * totalAmount) / totalInstallments);
                    payment.setRemainingAmount(totalAmount - payment.getPaidAmount());
                    payment.setMonthlyPayment(totalAmount / totalInstallments);
                } else {
                    payment.setMonthsRemaining(0);
                    payment.setPaidAmount(payment.getTotalAmount());
                    payment.setRemainingAmount(0.0);
                    payment.setMonthlyPayment(0.0);
                }

                Timestamp lastPaymentTS = rs.getTimestamp("last_payment_date");
                if (lastPaymentTS != null) {
                    payment.setLastPaymentAt(lastPaymentTS.toLocalDateTime());
                }

                list.add(payment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public Payment getByOrderId(int orderId) {
        String sql = "SELECT o.order_id, o.customer_id, c.customer_name, o.paid_amount as total_amount, " +
                "o.order_status as payment_status, o.order_date as last_payment_date, " +
                "o.is_installment, " +
                "(SELECT COUNT(*) FROM installment_list i WHERE i.order_id = o.order_id AND i.is_finished = 1) as payments_made, " +
                "(SELECT COUNT(*) FROM installment_list i WHERE i.order_id = o.order_id) as total_installments " +
                "FROM orders o " +
                "JOIN customer_info c ON o.customer_id = c.customer_id " +
                "WHERE o.order_id = ?";

        try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Payment payment = new Payment();
                payment.setOrderId(rs.getInt("order_id"));
                payment.setCustomerId(rs.getInt("customer_id"));
                payment.setCustomerName(rs.getString("customer_name"));
                payment.setTotalAmount(rs.getDouble("total_amount"));
                payment.setStatus(rs.getString("payment_status"));

                boolean isInstallment = rs.getBoolean("is_installment");
                if (isInstallment) {
                    int paymentsMade = rs.getInt("payments_made");
                    int totalInstallments = rs.getInt("total_installments");
                    double totalAmount = payment.getTotalAmount();

                    payment.setMonthsRemaining(totalInstallments - paymentsMade);
                    payment.setPaidAmount((paymentsMade * totalAmount) / totalInstallments);
                    payment.setRemainingAmount(totalAmount - payment.getPaidAmount());
                    payment.setMonthlyPayment(totalAmount / totalInstallments);
                } else {
                    payment.setMonthsRemaining(0);
                    payment.setPaidAmount(payment.getTotalAmount());
                    payment.setRemainingAmount(0.0);
                    payment.setMonthlyPayment(0.0);
                }

                Timestamp lastPaymentTS = rs.getTimestamp("last_payment_date");
                if (lastPaymentTS != null) {
                    payment.setLastPaymentAt(lastPaymentTS.toLocalDateTime());
                }

                return payment;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean recordMonthlyPayment(int orderId, double amount) {
        // For your current ENUM, we can only set to 'confirm' or 'pending'
        // Since a payment is being made, set to 'confirm'
        String sql = "UPDATE orders SET order_status = 'confirm' WHERE order_id = ?";

        try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean createPayment(Payment payment) {
        // For installment orders, create installment records
        if (payment.getMonths() > 0) {
            return createInstallmentPlan(payment.getOrderId(), payment.getTotalAmount(), payment.getMonths());
        } else {
            // For full payment, set status to 'confirm'
            String sql = "UPDATE orders SET order_status = 'confirm', paid_amount = ? WHERE order_id = ?";
            try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setDouble(1, payment.getTotalAmount());
                ps.setInt(2, payment.getOrderId());
                int affectedRows = ps.executeUpdate();
                return affectedRows > 0;

            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    public boolean createInstallmentPlan(int orderId, double totalAmount, int months) {
        Connection con = null;
        try {
            con = DatabaseConnectionManager.getInstance().getConnection();
            con.setAutoCommit(false);

            double monthlyAmount = totalAmount / months;

            // Create installment records
            String sql = "INSERT INTO installment_list (order_id, installment_number, due_date, installment_amount, is_finished) VALUES (?, ?, DATE_ADD(CURDATE(), INTERVAL ? MONTH), ?, 0)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (int i = 1; i <= months; i++) {
                    ps.setInt(1, orderId);
                    ps.setInt(2, i);
                    ps.setInt(3, i); // due date: current date + i months
                    ps.setDouble(4, monthlyAmount);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // Update order status to pending for installment (since not fully paid yet)
            String updateOrder = "UPDATE orders SET order_status = 'pending', is_installment = 1, paid_amount = 0 WHERE order_id = ?";
            try (PreparedStatement ps = con.prepareStatement(updateOrder)) {
                ps.setInt(1, orderId);
                ps.executeUpdate();
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }

    public boolean recordMonthlyPaymentWithInstallment(int orderId, double amount, int installmentNumber) {
        Connection con = null;
        try {
            con = DatabaseConnectionManager.getInstance().getConnection();
            con.setAutoCommit(false);

            // Mark installment as finished
            String sqlUpdateInstallment = "UPDATE installment_list SET is_finished = 1 WHERE order_id = ? AND installment_number = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlUpdateInstallment)) {
                ps.setInt(1, orderId);
                ps.setInt(2, installmentNumber);
                ps.executeUpdate();
            }

            // Update paid amount in orders table
            String updatePaidAmount = "UPDATE orders SET paid_amount = paid_amount + ? WHERE order_id = ?";
            try (PreparedStatement ps = con.prepareStatement(updatePaidAmount)) {
                ps.setDouble(1, amount);
                ps.setInt(2, orderId);
                ps.executeUpdate();
            }

            // Check if all installments are completed
            String sqlCheckCompletion = "SELECT COUNT(*) as pending FROM installment_list WHERE order_id = ? AND is_finished = 0";
            try (PreparedStatement ps = con.prepareStatement(sqlCheckCompletion)) {
                ps.setInt(1, orderId);
                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt("pending") == 0) {
                    // All installments paid, update order status to confirm
                    String sqlComplete = "UPDATE orders SET order_status = 'confirm' WHERE order_id = ?";
                    try (PreparedStatement psComplete = con.prepareStatement(sqlComplete)) {
                        psComplete.setInt(1, orderId);
                        psComplete.executeUpdate();
                    }
                }
                // If still pending installments, status remains 'pending' (default)
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }

    public boolean updatePayment(Payment payment) {
        String sql = "UPDATE orders SET paid_amount = ?, order_status = ? WHERE order_id = ?";

        try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDouble(1, payment.getPaidAmount());

            // FIXED: Validate and convert status to match ENUM
            String status = payment.getStatus();
            if (status != null) {
                if (!"confirm".equals(status) && !"pending".equals(status)) {
                    // Convert any other status to valid ENUM values
                    if ("completed".equals(status) || "confirmed".equals(status) || "COMPLETED".equals(status) || "CONFIRMED".equals(status)) {
                        status = "confirm";
                    } else {
                        status = "pending"; // default for any other status
                    }
                }
            } else {
                status = "pending"; // default if null
            }

            ps.setString(2, status);
            ps.setInt(3, payment.getOrderId());

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Payment> getAllPayments() {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT o.order_id, o.customer_id, c.customer_name, o.paid_amount as total_amount, " +
                "o.order_status as payment_status, o.order_date as last_payment_date, " +
                "o.is_installment, " +
                "(SELECT COUNT(*) FROM installment_list i WHERE i.order_id = o.order_id AND i.is_finished = 1) as payments_made, " +
                "(SELECT COUNT(*) FROM installment_list i WHERE i.order_id = o.order_id) as total_installments " +
                "FROM orders o " +
                "JOIN customer_info c ON o.customer_id = c.customer_id " +
                "ORDER BY o.order_id DESC";

        try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Payment payment = new Payment();
                payment.setOrderId(rs.getInt("order_id"));
                payment.setCustomerId(rs.getInt("customer_id"));
                payment.setCustomerName(rs.getString("customer_name"));
                payment.setTotalAmount(rs.getDouble("total_amount"));
                payment.setStatus(rs.getString("payment_status"));

                boolean isInstallment = rs.getBoolean("is_installment");
                if (isInstallment) {
                    int paymentsMade = rs.getInt("payments_made");
                    int totalInstallments = rs.getInt("total_installments");
                    double totalAmount = payment.getTotalAmount();

                    payment.setMonthsRemaining(totalInstallments - paymentsMade);
                    payment.setPaidAmount((paymentsMade * totalAmount) / totalInstallments);
                    payment.setRemainingAmount(totalAmount - payment.getPaidAmount());
                    payment.setMonthlyPayment(totalAmount / totalInstallments);
                    payment.setMonths(totalInstallments);
                } else {
                    payment.setMonthsRemaining(0);
                    payment.setPaidAmount(payment.getTotalAmount());
                    payment.setRemainingAmount(0.0);
                    payment.setMonthlyPayment(0.0);
                    payment.setMonths(0);
                }

                Timestamp lastPaymentTS = rs.getTimestamp("last_payment_date");
                if (lastPaymentTS != null) {
                    payment.setLastPaymentAt(lastPaymentTS.toLocalDateTime());
                }

                list.add(payment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Object[]> getInstallmentsByOrderId(int orderId) {
        List<Object[]> installments = new ArrayList<>();
        String sql = "SELECT installment_number, due_date, installment_amount, is_finished " +
                "FROM installment_list WHERE order_id = ? ORDER BY installment_number";

        try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Object[] installment = new Object[4];
                installment[0] = rs.getInt("installment_number");
                installment[1] = rs.getDate("due_date");
                installment[2] = rs.getDouble("installment_amount");
                installment[3] = rs.getBoolean("is_finished");
                installments.add(installment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return installments;
    }
}