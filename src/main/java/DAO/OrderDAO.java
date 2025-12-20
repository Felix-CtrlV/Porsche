package DAO;

import Database.DatabaseConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {
    private static final Logger logger = LoggerFactory.getLogger(OrderDAO.class);

    public static class OrderLine {
        public Integer carId;
        public Integer partId;
        public int quantity;
        public double totalPrice;

        public OrderLine(Integer carId, Integer partId, int quantity, double totalPrice) {
            this.carId = carId;
            this.partId = partId;
            this.quantity = quantity;
            this.totalPrice = totalPrice;
        }
    }

    public int createOrder(int customerId, boolean isInstallment, double paidAmount, List<OrderLine> lines, int userId) {
        logger.debug("Creating order for customer: {}, installment: {}, paid: {}, user: {}",
                customerId, isInstallment, paidAmount, userId);

        String orderStatus = isInstallment ? "pending" : "confirm";

        String insertOrderSql = "INSERT INTO orders (user_id, customer_id, order_date, order_status, is_installment, paid_amount) VALUES (?, ?, CURDATE(), ?, ?, ?)";
        String insertLineSql = "INSERT INTO order_details (order_id, is_customize, car_id, part_id, qty, total_price) VALUES (?, ?, ?, ?, ?, ?)";
        String insertCustomizationSql = "INSERT INTO customize (detail_id, customization_type, customization_value, customization_price) VALUES (?, ?, ?, ?)";

        Connection con = null;
        try {
            con = DatabaseConnectionManager.getInstance().getConnection();
            con.setAutoCommit(false);

            PreparedStatement psOrder = con.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS);
            psOrder.setInt(1, userId);
            psOrder.setInt(2, customerId);
            psOrder.setString(3, orderStatus);
            psOrder.setBoolean(4, isInstallment);
            psOrder.setDouble(5, paidAmount);
            int affectedRows = psOrder.executeUpdate();

            if (affectedRows == 0) {
                logger.error("Failed to insert order for customer: {}, user: {}", customerId, userId);
                con.rollback();
                return -1;
            }

            ResultSet generatedKeys = psOrder.getGeneratedKeys();
            if (!generatedKeys.next()) {
                logger.error("No generated keys returned for order");
                con.rollback();
                return -1;
            }
            int orderId = generatedKeys.getInt(1);
            logger.debug("Generated order ID: {}", orderId);

            PreparedStatement psLine = con.prepareStatement(insertLineSql, Statement.RETURN_GENERATED_KEYS);
            int lineCount = 0;

            for (OrderLine line : lines) {
                if (line.carId == null && line.partId == null) {
                    logger.warn("Skipping order line with both carId and partId null");
                    continue;
                }
                if (line.quantity <= 0) {
                    logger.warn("Skipping order line with invalid quantity: {}", line.quantity);
                    continue;
                }

                psLine.setInt(1, orderId);

                int customizeValue = 0;
                if (line.carId != null) {
                    customizeValue = getCustomizeValueFromSession();
                }
                psLine.setInt(2, customizeValue);

                if (line.carId != null) {
                    psLine.setInt(3, line.carId);
                    psLine.setNull(4, Types.INTEGER);
                    logger.debug("Adding car line - CarID: {}, Quantity: {}, Price: {}, Customize: {}",
                            line.carId, line.quantity, line.totalPrice, customizeValue);
                } else {
                    psLine.setNull(3, Types.INTEGER);
                    psLine.setInt(4, line.partId);
                    logger.debug("Adding part line - PartID: {}, Quantity: {}, Price: {}",
                            line.partId, line.quantity, line.totalPrice);
                }
                psLine.setInt(5, line.quantity);
                psLine.setDouble(6, line.totalPrice);
                psLine.addBatch();
                lineCount++;
            }

            if (lineCount > 0) {
                int[] batchResults = psLine.executeBatch();
                logger.debug("Executed batch with {} lines, results: {}", lineCount, batchResults.length);

                ResultSet lineKeys = psLine.getGeneratedKeys();
                int detailIdIndex = 0;

                while (lineKeys.next()) {
                    int detailId = lineKeys.getInt(1);
                    logger.debug("Generated detail ID: {} for line index: {}", detailId, detailIdIndex);

                    if (detailIdIndex < lines.size()) {
                        OrderLine currentLine = lines.get(detailIdIndex);
                        if (currentLine.carId != null) {
                            saveCustomizationsForCar(con, detailId);
                        }
                    }
                    detailIdIndex++;
                }
            } else {
                logger.warn("No valid order lines to insert");
            }

            con.commit();
            logger.info("Order created successfully - ID: {}, Customer: {}, User: {}, Installment: {}, Status: {}, Paid: {}",
                    orderId, customerId, userId, isInstallment, orderStatus, paidAmount);
            return orderId;

        } catch (SQLException e) {
            logger.error("SQL Error creating order for customer: {}, user: {}", customerId, userId, e);
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException rollbackEx) {
                    logger.error("Failed to rollback transaction", rollbackEx);
                }
            }
            return -1;
        } catch (Exception e) {
            logger.error("Unexpected error creating order for customer: {}, user: {}", customerId, userId, e);
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException rollbackEx) {
                    logger.error("Failed to rollback transaction", rollbackEx);
                }
            }
            return -1;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException e) {
                    logger.error("Error closing connection", e);
                }
            }
        }
    }

    private int getCustomizeValueFromSession() {
        Utils.SessionStaff session = Utils.SessionStaff.getInstance();
        List<Utils.SessionStaff.CustomizationRecord> customizations = session.getPendingCustomizations();

        if (customizations.isEmpty()) {
            return 0;
        }

        boolean hasColor = false;
        boolean hasInterior = false;

        for (Utils.SessionStaff.CustomizationRecord cust : customizations) {
            if ("c_color".equals(cust.getType())) {
                hasColor = true;
            } else if ("i_color".equals(cust.getType())) {
                hasInterior = true;
            }
        }

        if (hasColor && hasInterior) {
            return 1;
        } else if (hasColor) {
            return 1;
        } else if (hasInterior) {
            return 1;
        }

        return 0;
    }

    private void saveCustomizationsForCar(Connection con, int detailId) throws SQLException {
        Utils.SessionStaff session = Utils.SessionStaff.getInstance();
        List<Utils.SessionStaff.CustomizationRecord> customizations = session.getPendingCustomizations();

        if (customizations.isEmpty()) {
            logger.debug("No customizations to save for detail ID: {}", detailId);
            return;
        }

        String insertCustomizationSql = "INSERT INTO customize (detail_id, customization_type, customization_value, customization_price) VALUES (?, ?, ?, ?)";
        PreparedStatement psCustom = con.prepareStatement(insertCustomizationSql);

        int customizationCount = 0;
        for (Utils.SessionStaff.CustomizationRecord cust : customizations) {
            psCustom.setInt(1, detailId);
            psCustom.setString(2, cust.getType());
            psCustom.setString(3, cust.getValue());
            psCustom.setDouble(4, cust.getPrice());
            psCustom.addBatch();
            customizationCount++;
            logger.debug("Adding customization: {} - {} - ${} for detail ID: {}",
                    cust.getType(), cust.getValue(), cust.getPrice(), detailId);
        }

        if (customizationCount > 0) {
            int[] custResults = psCustom.executeBatch();
            logger.info("Saved {} customizations for detail ID: {}", custResults.length, detailId);
        }

        psCustom.close();
    }

    public boolean updateOrderStatus(int orderId, String status) {
        if (!status.equals("confirm") && !status.equals("pending")) {
            logger.error("Invalid order status: {}. Only 'confirm' or 'pending' allowed.", status);
            return false;
        }

        String sql = "UPDATE orders SET order_status = ? WHERE order_id = ?";

        try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, orderId);

            int affectedRows = ps.executeUpdate();
            logger.debug("Updated order {} status to {}, affected rows: {}", orderId, status, affectedRows);
            return affectedRows > 0;

        } catch (SQLException e) {
            logger.error("Error updating order status for order: {}", orderId, e);
            return false;
        }
    }

    public List<Integer> getOrdersByCustomerId(int customerId) {
        List<Integer> orderIds = new ArrayList<>();
        String sql = "SELECT order_id FROM orders WHERE customer_id = ? ORDER BY order_date DESC";

        try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                orderIds.add(rs.getInt("order_id"));
            }

            logger.debug("Found {} orders for customer: {}", orderIds.size(), customerId);

        } catch (SQLException e) {
            logger.error("Error getting orders for customer: {}", customerId, e);
        }
        return orderIds;
    }

    public static boolean testDatabaseConnection() {
        try (Connection con = DatabaseConnectionManager.getInstance().getConnection()) {
            logger.info("✅ Database connection successful");

            DatabaseMetaData metaData = con.getMetaData();
            ResultSet tables = metaData.getTables(null, null, "orders", null);
            if (tables.next()) {
                logger.info("✅ Orders table exists");
            } else {
                logger.error("❌ Orders table does not exist");
                return false;
            }

            tables = metaData.getTables(null, null, "order_details", null);
            if (tables.next()) {
                logger.info("✅ Order_details table exists");
            } else {
                logger.error("❌ Order_details table does not exist");
                return false;
            }

            tables = metaData.getTables(null, null, "customer_info", null);
            if (tables.next()) {
                logger.info("✅ Customer_info table exists");
            } else {
                logger.error("❌ Customer_info table does not exist");
                return false;
            }

            return true;
        } catch (SQLException e) {
            logger.error("Database connection test failed", e);
            return false;
        }
    }
}