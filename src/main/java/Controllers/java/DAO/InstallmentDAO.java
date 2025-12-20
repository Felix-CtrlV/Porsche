package Controllers.java.DAO;

import Database.DatabaseConnectionManager;
import Model.Installment;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InstallmentDAO {

    public List<Installment> getInstallmentsByOrderId(int orderId) {
        String sql = "SELECT installment_id, order_id, installment_number, due_date, installment_amount, is_finished FROM installment_list WHERE order_id = ? ORDER BY installment_number";
        List<Installment> installments = new ArrayList<>();

        try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Installment i = mapResultSetToInstallment(rs);
                installments.add(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return installments;
    }

    public boolean recordInstallment(int orderId, int installmentNumber, double amount, LocalDate dueDate) {
        String sql = "INSERT INTO installment_list (order_id, installment_number, due_date, installment_amount, is_finished) VALUES (?, ?, ?, ?, 0)";

        try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ps.setInt(2, installmentNumber);
            ps.setDate(3, Date.valueOf(dueDate));
            ps.setDouble(4, amount);

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Installment> getPendingInstallments(int orderId) {
        String sql = "SELECT installment_id, order_id, installment_number, due_date, installment_amount, is_finished FROM installment_list WHERE order_id = ? AND is_finished = 0 ORDER BY installment_number";
        List<Installment> installments = new ArrayList<>();

        try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Installment i = mapResultSetToInstallment(rs);
                installments.add(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return installments;
    }

    public boolean markInstallmentAsPaid(int installmentId) {
        String sql = "UPDATE installment_list SET is_finished = 1 WHERE installment_id = ?";

        try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, installmentId);
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getNextInstallmentNumber(int orderId) {
        String sql = "SELECT MAX(installment_number) as max_number FROM installment_list WHERE order_id = ?";

        try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("max_number") + 1;
            }
            return 1;

        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    public Installment getInstallmentById(int installmentId) {
        String sql = "SELECT installment_id, order_id, installment_number, due_date, installment_amount, is_finished FROM installment_list WHERE installment_id = ?";

        try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, installmentId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToInstallment(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean createInstallmentPlan(int orderId, double totalAmount, int numberOfInstallments, LocalDate firstDueDate, int intervalMonths) {
        Connection con = null;
        try {
            con = DatabaseConnectionManager.getInstance().getConnection();
            con.setAutoCommit(false);

            double installmentAmount = totalAmount / numberOfInstallments;

            clearInstallmentsForOrder(con, orderId);

            for (int i = 0; i < numberOfInstallments; i++) {
                LocalDate dueDate = firstDueDate.plusMonths(i * intervalMonths);
                boolean success = insertInstallment(con, orderId, i + 1, installmentAmount, dueDate);
                if (!success) {
                    con.rollback();
                    return false;
                }
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    e.addSuppressed(ex);
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<Installment> getOverdueInstallments() {
        String sql = "SELECT installment_id, order_id, installment_number, due_date, installment_amount, is_finished FROM installment_list WHERE due_date < ? AND is_finished = 0 ORDER BY due_date";
        List<Installment> installments = new ArrayList<>();

        try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(LocalDate.now()));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Installment i = mapResultSetToInstallment(rs);
                installments.add(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return installments;
    }

    public boolean updateInstallment(Installment installment) {
        String sql = "UPDATE installment_list SET due_date = ?, installment_amount = ?, is_finished = ? WHERE installment_id = ?";

        try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(installment.getDueDate()));
            ps.setDouble(2, installment.getInstallmentAmount());
            ps.setBoolean(3, installment.isFinished());
            ps.setInt(4, installment.getInstallmentId());

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteInstallment(int installmentId) {
        String sql = "DELETE FROM installment_list WHERE installment_id = ?";

        try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, installmentId);
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // NEW METHODS FOR PAYMENT MANAGEMENT
    public List<Installment> getAllInstallments() {
        String sql = "SELECT installment_id, order_id, installment_number, due_date, installment_amount, is_finished FROM installment_list ORDER BY order_id, installment_number";
        List<Installment> installments = new ArrayList<>();

        try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Installment i = mapResultSetToInstallment(rs);
                installments.add(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return installments;
    }

    public List<Installment> getInstallmentsByOrderIdRange(int startOrderId, int endOrderId) {
        String sql = "SELECT installment_id, order_id, installment_number, due_date, installment_amount, is_finished FROM installment_list WHERE order_id BETWEEN ? AND ? ORDER BY order_id, installment_number";
        List<Installment> installments = new ArrayList<>();

        try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, startOrderId);
            ps.setInt(2, endOrderId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Installment i = mapResultSetToInstallment(rs);
                installments.add(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return installments;
    }

    public double getTotalPendingAmount(int orderId) {
        String sql = "SELECT SUM(installment_amount) as total_pending FROM installment_list WHERE order_id = ? AND is_finished = 0";

        try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getDouble("total_pending");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public List<Installment> getInstallmentsWithCustomerInfo() {
        String sql = "SELECT il.installment_id, il.order_id, il.installment_number, il.due_date, il.installment_amount, il.is_finished, " +
                "oi.customer_name, oi.total_amount " +
                "FROM installment_list il " +
                "LEFT JOIN order_info oi ON il.order_id = oi.order_id " +
                "ORDER BY il.order_id, il.installment_number";

        List<Installment> installments = new ArrayList<>();

        try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Installment i = mapResultSetToInstallment(rs);
                installments.add(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return installments;
    }

    public int getTotalPendingCount() {
        String sql = "SELECT COUNT(*) as pending_count FROM installment_list WHERE is_finished = 0 AND due_date >= ?";

        try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(LocalDate.now()));
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("pending_count");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getTotalOverdueCount() {
        String sql = "SELECT COUNT(*) as overdue_count FROM installment_list WHERE is_finished = 0 AND due_date < ?";

        try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(LocalDate.now()));
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("overdue_count");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getTotalPaidCount() {
        String sql = "SELECT COUNT(*) as paid_count FROM installment_list WHERE is_finished = 1";

        try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("paid_count");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double getTotalRevenue() {
        String sql = "SELECT SUM(installment_amount) as total_revenue FROM installment_list WHERE is_finished = 1";

        try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble("total_revenue");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private void clearInstallmentsForOrder(Connection con, int orderId) throws SQLException {
        String sql = "DELETE FROM installment_list WHERE order_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.executeUpdate();
        }
    }

    private boolean insertInstallment(Connection con, int orderId, int installmentNumber, double amount, LocalDate dueDate) throws SQLException {
        String sql = "INSERT INTO installment_list (order_id, installment_number, due_date, installment_amount, is_finished) VALUES (?, ?, ?, ?, 0)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, installmentNumber);
            ps.setDate(3, Date.valueOf(dueDate));
            ps.setDouble(4, amount);

            return ps.executeUpdate() > 0;
        }
    }

    private Installment mapResultSetToInstallment(ResultSet rs) throws SQLException {
        Installment i = new Installment();
        i.setInstallmentId(rs.getInt("installment_id"));
        i.setOrderId(rs.getInt("order_id"));
        i.setInstallmentNumber(rs.getInt("installment_number"));

        Date dueDate = rs.getDate("due_date");
        if (dueDate != null) {
            i.setDueDate(dueDate.toLocalDate());
        }

        i.setInstallmentAmount(rs.getDouble("installment_amount"));
        i.setFinished(rs.getBoolean("is_finished"));
        return i;
    }
}