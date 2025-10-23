package Utils;

import Database.DatabaseConnectionManager;
import Model.CarConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class SessionStaff {
    private static final Logger logger = LoggerFactory.getLogger(SessionStaff.class);

    public enum Role { ADMIN, STAFF, MANAGER }

    private CarConfiguration currentCarConfig;
    private final Map<String, AccessoryItem> accessories = new HashMap<>();

    private int userId;
    private String username;
    private Role role;
    private String password;
    private String email;
    private String phone;
    private String address;
    private LocalDate dob;
    private LocalDateTime loginTime;

    public static class AccessoryItem {
        public String name;
        public double price;
        public int quantity;

        public AccessoryItem(String name, double price, int quantity) {
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }

        public double getTotal() {
            return price * quantity;
        }
    }

    private SessionStaff() {}

    private static class Holder {
        private static final SessionStaff INSTANCE = new SessionStaff();
    }

    public static SessionStaff getInstance() {
        return Holder.INSTANCE;
    }

    public static void startSession(int id, String name, Role role, String password,
                                    String email, String phone, String address, LocalDate dob) {
        SessionStaff instance = getInstance();
        instance.userId = id;
        instance.username = name;
        instance.role = role;
        instance.password = password;
        instance.email = email;
        instance.phone = phone;
        instance.address = address;
        instance.dob = dob;
        instance.loginTime = LocalDateTime.now();
        closePreviousSessionIfAny(id);
        if (role == Role.STAFF) {
            logStaffLogin(id, name, instance.loginTime);
        }
        logger.info("Session started: userId={}, username={}, role={}, time={}", id, name, role, instance.loginTime);
    }

    private static void closePreviousSessionIfAny(int userId) {
        String sql = "UPDATE staff_attendance SET logout_time = ? WHERE staff_id = ? AND logout_time IS NULL";
        try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to close previous session for staff ID: {}", userId, e);
        }
    }

    private static void logStaffLogin(int staffId, String staffName, LocalDateTime loginTime) {
        String sql = "INSERT INTO staff_attendance (staff_id, staff_name, login_time, attendance_date) VALUES (?, ?, ?, CURDATE())";
        try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            ps.setString(2, staffName);
            ps.setTimestamp(3, java.sql.Timestamp.valueOf(loginTime));
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to log attendance for staff: {}", staffName, e);
        }
    }

    public static void handleLogout() {
        SessionStaff instance = getInstance();
        if (instance.role == Role.STAFF) {
            logStaffLogout(instance.userId, LocalDateTime.now());
        }
        clearSession();
    }

    private static void logStaffLogout(int staffId, LocalDateTime logoutTime) {
        String sql = "UPDATE staff_attendance SET logout_time = ? WHERE staff_id = ? AND attendance_date = CURDATE() AND logout_time IS NULL ORDER BY login_time DESC LIMIT 1";
        try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, java.sql.Timestamp.valueOf(logoutTime));
            ps.setInt(2, staffId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to log logout for staff ID: {}", staffId, e);
        }
    }

    public static void clearSession() {
        SessionStaff instance = getInstance();
        instance.currentCarConfig = null;
        instance.accessories.clear();
        instance.userId = 0;
        instance.username = null;
        instance.role = null;
        instance.password = null;
        instance.email = null;
        instance.phone = null;
        instance.address = null;
        instance.dob = null;
        instance.loginTime = null;
    }

    public void setCarConfiguration(CarConfiguration config) {
        this.currentCarConfig = config;
    }

    public CarConfiguration getCarConfiguration() {
        return currentCarConfig;
    }

    public void addAccessory(String id, String name, double price, int quantity) {
        accessories.merge(id, new AccessoryItem(name, price, quantity),
                (existing, newItem) -> {
                    existing.quantity += newItem.quantity;
                    return existing;
                });
    }

    public void updateAccessoryQuantity(String id, int quantity) {
        AccessoryItem item = accessories.get(id);
        if (item != null) item.quantity = quantity;
        else logger.warn("Tried to update accessory '{}' which does not exist.", id);
    }

    public void removeAccessory(String id) {
        accessories.remove(id);
    }

    public Map<String, AccessoryItem> getAccessories() {
        return accessories;
    }

    public void clearAccessories() {
        accessories.clear();
    }

    public int getTotalAccessoryCount() {
        return accessories.values().stream().mapToInt(item -> item.quantity).sum();
    }

    public double getTotalAccessoryPrice() {
        return accessories.values().stream().mapToDouble(AccessoryItem::getTotal).sum();
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public Role getRole() {
        return role;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public LocalDate getDob() {
        return dob;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }
}