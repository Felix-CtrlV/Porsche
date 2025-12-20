package Controllers.java.Utils;

import Controllers.java.Database.DatabaseConnectionManager;
import Controllers.java.Model.CarConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class SessionStaff {
    private static final Logger logger = LoggerFactory.getLogger(SessionStaff.class);

    public enum Role {
        ADMIN("admin"),
        MANAGER("manager"),
        STAFF("staff");

        private final String dbValue;

        Role(String dbValue) {
            this.dbValue = dbValue;
        }

        public String getDbValue() {
            return dbValue;
        }

        public static Role fromString(String role) {
            if (role == null) return STAFF;

            switch (role.toLowerCase()) {
                case "admin": return ADMIN;
                case "manager": return MANAGER;
                case "staff": return STAFF;
                default: return STAFF;
            }
        }

        @Override
        public String toString() {
            return dbValue;
        }
    }

    private CarConfiguration currentCarConfig;
    private final Map<String, AccessoryItem> accessories = new HashMap<>();
    private List<Integer> selectedAccessoryIds = new ArrayList<>();
    private List<CustomizationRecord> pendingCustomizations = new ArrayList<>();
    private String lastSelectedModel;

    private int userId;
    private String userName;
    private String userMrc;
    private Role userRole;
    private String password;
    private String userEmail;
    private String userAddress;
    private String userPhone;
    private LocalDate dob;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean userStatus;
    private String userPhoto;
    private String reason;
    private LocalDateTime loginTime;
    private LocalDateTime checkInTime;

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

        @Override
        public String toString() {
            return String.format("AccessoryItem{name='%s', price=%.2f, quantity=%d}", name, price, quantity);
        }
    }

    public static class CustomizationRecord {
        private String type;
        private String value;
        private double price;

        public CustomizationRecord(String type, String value, double price) {
            this.type = type;
            this.value = value;
            this.price = price;
        }

        public String getType() { return type; }
        public String getValue() { return value; }
        public double getPrice() { return price; }
    }

    private SessionStaff() {}

    private static class Holder {
        private static final SessionStaff INSTANCE = new SessionStaff();
    }

    public static SessionStaff getInstance() {
        return Holder.INSTANCE;
    }

    public String getLastSelectedModel() {
        return lastSelectedModel;
    }

    public void setLastSelectedModel(String lastSelectedModel) {
        this.lastSelectedModel = lastSelectedModel;
    }

    public static void startSession(int userId, String userName, String userMrc, String userRole,
                                    String password, String userEmail, String userAddress,
                                    String userPhone, LocalDate dob, LocalDate startDate,
                                    LocalDate endDate, boolean userStatus, String userPhoto, String reason) {
        SessionStaff instance = getInstance();
        instance.userId = userId;
        instance.userName = userName;
        instance.userMrc = userMrc;
        instance.userRole = Role.fromString(userRole);
        instance.password = password;
        instance.userEmail = userEmail;
        instance.userAddress = userAddress;
        instance.userPhone = userPhone;
        instance.dob = dob;
        instance.startDate = startDate;
        instance.endDate = endDate;
        instance.userStatus = userStatus;
        instance.userPhoto = userPhoto;
        instance.reason = reason;
        instance.loginTime = LocalDateTime.now();
        instance.checkInTime = LocalDateTime.now();

        if (instance.userRole == Role.STAFF) {
            logStaffCheckIn(instance.userId, instance.checkInTime);
        }

        logger.info("Session started: userId={}, userName={}, role={}, time={}",
                userId, userName, instance.userRole, instance.loginTime);
    }

    public static void startSession(int userId, String userName, String userRole) {
        SessionStaff instance = getInstance();
        instance.userId = userId;
        instance.userName = userName;
        instance.userRole = Role.fromString(userRole);
        instance.loginTime = LocalDateTime.now();
        instance.checkInTime = LocalDateTime.now();
        instance.userStatus = true;

        if (instance.userRole == Role.STAFF) {
            logStaffCheckIn(instance.userId, instance.checkInTime);
        }

        logger.info("Session started: userId={}, userName={}, role={}",
                userId, userName, instance.userRole);
    }

    public static void startSession(int userId, String userName, String userRole,
                                    String userEmail, String userPhone, LocalDate dob) {
        SessionStaff instance = getInstance();
        instance.userId = userId;
        instance.userName = userName;
        instance.userRole = Role.fromString(userRole);
        instance.userEmail = userEmail;
        instance.userPhone = userPhone;
        instance.dob = dob;
        instance.loginTime = LocalDateTime.now();
        instance.checkInTime = LocalDateTime.now();
        instance.userStatus = true;

        if (instance.userRole == Role.STAFF) {
            logStaffCheckIn(instance.userId, instance.checkInTime);
        }

        logger.info("Session started: userId={}, userName={}, role={}, email={}",
                userId, userName, instance.userRole, userEmail);
    }

    private static void logStaffCheckIn(int staffId, LocalDateTime checkInTime) {
        String checkSql = "SELECT COUNT(*) FROM user_attendance WHERE user_id = ? AND DATE(check_in) = CURRENT_DATE";
        String insertSql = "INSERT INTO user_attendance (user_id, check_in) VALUES (?, ?)";
        String updateSql = "UPDATE user_attendance SET check_in = ? WHERE user_id = ? AND DATE(check_in) = CURRENT_DATE";

        try (Connection con = DatabaseConnectionManager.getInstance().getConnection()) {

            boolean hasRecordToday = false;
            try (PreparedStatement checkPs = con.prepareStatement(checkSql)) {
                checkPs.setInt(1, staffId);
                ResultSet rs = checkPs.executeQuery();
                if (rs.next()) {
                    hasRecordToday = rs.getInt(1) > 0;
                }
            }

            if (hasRecordToday) {
                try (PreparedStatement updatePs = con.prepareStatement(updateSql)) {
                    updatePs.setTimestamp(1, java.sql.Timestamp.valueOf(checkInTime));
                    updatePs.setInt(2, staffId);
                    int rowsUpdated = updatePs.executeUpdate();
                    if (rowsUpdated > 0) {
                        logger.info("Updated check-in time for staff ID: {} at {}", staffId, checkInTime);
                    }
                }
            } else {
                try (PreparedStatement insertPs = con.prepareStatement(insertSql)) {
                    insertPs.setInt(1, staffId);
                    insertPs.setTimestamp(2, java.sql.Timestamp.valueOf(checkInTime));
                    int rowsInserted = insertPs.executeUpdate();
                    if (rowsInserted > 0) {
                        logger.info("Logged check-in for staff ID: {} at {}", staffId, checkInTime);
                    }
                }
            }

        } catch (SQLException e) {
            logger.error("Failed to log check-in for staff ID: {}", staffId, e);
        }
    }

    public static void syncFromSession() {
        Session session = Session.getInstance();
        if (session != null) {
            SessionStaff instance = getInstance();
            instance.userId = session.getUserid();
            instance.userName = session.getUsername();
            instance.userRole = Role.fromString(session.getRole());
            instance.password = session.getPassword();
            instance.userEmail = session.getEmail();
            instance.userPhone = session.getPhone();
            instance.userAddress = session.getAddress();
            instance.dob = session.getDob();
            instance.loginTime = LocalDateTime.now();
            instance.checkInTime = LocalDateTime.now();
            instance.userStatus = true;

            if (instance.userRole == Role.STAFF) {
                logStaffCheckIn(instance.userId, instance.checkInTime);
            }

            logger.info("Session synced from Session class: userId={}, userName={}, role={}",
                    instance.userId, instance.userName, instance.userRole);
        } else {
            logger.warn("No Session found to sync from");
        }
    }

    public static void handleLogout() {
        SessionStaff instance = getInstance();

        if (instance.userId == 0) {
            logger.warn("No active session found. Logout aborted.");
            return;
        }

        LocalDateTime logoutTime = LocalDateTime.now();

        if (instance.userRole == Role.STAFF) {
            logStaffCheckOut(instance.userId, logoutTime);
        }

        logger.info("User {} (ID: {}) logged out at {}", instance.userName, instance.userId, logoutTime);
        clearSession();

        Session.clearSession();
    }

    private static void logStaffCheckOut(int staffId, LocalDateTime checkOutTime) {
        String sql = "UPDATE user_attendance SET check_out = ? WHERE user_id = ? AND DATE(check_in) = CURRENT_DATE AND check_out IS NULL";

        try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, java.sql.Timestamp.valueOf(checkOutTime));
            ps.setInt(2, staffId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Logged check-out for staff ID: {} at {}", staffId, checkOutTime);
                calculateAndLogHoursWorked(staffId, checkOutTime.toLocalDate());
            } else {
                logger.warn("No check-in record found for staff ID: {} to update check-out", staffId);
            }

        } catch (SQLException e) {
            logger.error("Failed to log check-out for staff ID: {}", staffId, e);
        }
    }

    private static void calculateAndLogHoursWorked(int staffId, LocalDate attendanceDate) {
        String sql = "SELECT check_in, check_out FROM user_attendance WHERE user_id = ? AND DATE(check_in) = ? AND check_out IS NOT NULL";

        try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, staffId);
            ps.setDate(2, java.sql.Date.valueOf(attendanceDate));

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                LocalDateTime checkIn = rs.getTimestamp("check_in").toLocalDateTime();
                LocalDateTime checkOut = rs.getTimestamp("check_out").toLocalDateTime();

                long minutesWorked = java.time.Duration.between(checkIn, checkOut).toMinutes();
                long hours = minutesWorked / 60;
                long minutes = minutesWorked % 60;

                logger.info("Staff ID: {} worked {} hours {} minutes on {}",
                        staffId, hours, minutes, attendanceDate);
            }

        } catch (SQLException e) {
            logger.error("Failed to calculate hours worked for staff ID: {}", staffId, e);
        }
    }

    public static void clearSession() {
        SessionStaff instance = getInstance();
        instance.currentCarConfig = null;
        instance.accessories.clear();
        instance.selectedAccessoryIds.clear();
        instance.pendingCustomizations.clear();
        instance.lastSelectedModel = null;
        instance.userId = 0;
        instance.userName = null;
        instance.userMrc = null;
        instance.userRole = null;
        instance.password = null;
        instance.userEmail = null;
        instance.userAddress = null;
        instance.userPhone = null;
        instance.dob = null;
        instance.startDate = null;
        instance.endDate = null;
        instance.userStatus = false;
        instance.userPhoto = null;
        instance.reason = null;
        instance.loginTime = null;
        instance.checkInTime = null;
        logger.info("SessionStaff cleared");
    }

    public static String getLoggedInStaffName() {
        SessionStaff instance = getInstance();
        if (instance.userName != null) {
            return instance.userName;
        }

        Session session = Session.getInstance();
        if (session != null) {
            return session.getUsername();
        }

        return null;
    }

    public static String getLoggedInStaffRole() {
        SessionStaff instance = getInstance();
        if (instance.userRole != null) {
            return instance.userRole.getDbValue();
        }

        Session session = Session.getInstance();
        if (session != null) {
            return session.getRole();
        }

        return "staff";
    }

    public static Role getLoggedInStaffRoleEnum() {
        SessionStaff instance = getInstance();
        if (instance.userRole != null) {
            return instance.userRole;
        }

        Session session = Session.getInstance();
        if (session != null) {
            return Role.fromString(session.getRole());
        }

        return Role.STAFF;
    }

    public static int getLoggedInStaffId() {
        SessionStaff instance = getInstance();
        if (instance.userId != 0) {
            return instance.userId;
        }

        Session session = Session.getInstance();
        if (session != null) {
            return session.getUserid();
        }

        return 0;
    }

    public static boolean isSessionActive() {
        SessionStaff instance = getInstance();
        Session session = Session.getInstance();

        boolean sessionStaffActive = instance.userId != 0 && instance.userName != null && instance.userRole != null;
        boolean sessionActive = session != null && session.getUserid() != 0 && session.getUsername() != null;

        if (sessionActive && !sessionStaffActive) {
            syncFromSession();
            return true;
        }

        return sessionStaffActive || sessionActive;
    }

    public static boolean isAdmin() {
        return getLoggedInStaffRoleEnum() == Role.ADMIN;
    }

    public static boolean isManager() {
        Role role = getLoggedInStaffRoleEnum();
        return role == Role.MANAGER || role == Role.ADMIN;
    }

    public static boolean isStaff() {
        return getLoggedInStaffRoleEnum() != null;
    }

    public static boolean isRegularStaff() {
        Role role = getLoggedInStaffRoleEnum();
        return role == Role.STAFF;
    }

    public static boolean isActiveUser() {
        SessionStaff instance = getInstance();
        return instance.userStatus && (instance.endDate == null || instance.endDate.isAfter(LocalDate.now()));
    }

    public static void saveSessionData() {
        SessionStaff instance = getInstance();
        logger.debug("Session data saved for user: {}", instance.userName);
    }

    public static String getSessionInfo() {
        SessionStaff instance = getInstance();
        Session session = Session.getInstance();

        if (instance.userId != 0) {
            return String.format("SessionStaff{userId=%d, userName='%s', userRole=%s, loginTime=%s}",
                    instance.userId, instance.userName, instance.userRole, instance.loginTime);
        } else if (session != null) {
            return String.format("Session{userId=%d, userName='%s', userRole=%s}",
                    session.getUserid(), session.getUsername(), session.getRole());
        } else {
            return "No active session";
        }
    }

    public void setCarConfiguration(CarConfiguration config) {
        this.currentCarConfig = config;
        logger.debug("Car configuration set for user: {}", userName);
    }

    public CarConfiguration getCarConfiguration() {
        return currentCarConfig;
    }

    public void addAccessory(String id, String name, double price, int quantity) {
        if (quantity <= 0) {
            logger.warn("Attempted to add accessory '{}' with invalid quantity: {}", name, quantity);
            return;
        }

        accessories.merge(id, new AccessoryItem(name, price, quantity),
                (existing, newItem) -> {
                    existing.quantity += newItem.quantity;
                    logger.debug("Updated accessory '{}' quantity to: {}", name, existing.quantity);
                    return existing;
                });

        logger.debug("Added accessory '{}' (ID: {}) with quantity: {}", name, id, quantity);
    }

    public void updateAccessoryQuantity(String id, int quantity) {
        if (quantity < 0) {
            logger.warn("Attempted to set negative quantity for accessory ID: {}", id);
            return;
        }

        AccessoryItem item = accessories.get(id);
        if (item != null) {
            if (quantity == 0) {
                accessories.remove(id);
                logger.debug("Removed accessory '{}' (ID: {}) due to zero quantity", item.name, id);
            } else {
                item.quantity = quantity;
                logger.debug("Updated accessory '{}' (ID: {}) quantity to: {}", item.name, id, quantity);
            }
        } else {
            logger.warn("Tried to update accessory '{}' which does not exist.", id);
        }
    }

    public void removeAccessory(String id) {
        AccessoryItem removed = accessories.remove(id);
        if (removed != null) {
            logger.debug("Removed accessory '{}' (ID: {})", removed.name, id);
        } else {
            logger.warn("Tried to remove non-existent accessory ID: {}", id);
        }
    }

    public Map<String, AccessoryItem> getAccessories() {
        return new HashMap<>(accessories);
    }

    public void clearAccessories() {
        int count = accessories.size();
        accessories.clear();
        logger.debug("Cleared {} accessories from cart", count);
    }

    public void clearCart() {
        this.currentCarConfig = null;
        int accessoryCount = accessories.size();
        this.accessories.clear();
        this.selectedAccessoryIds.clear();
        this.pendingCustomizations.clear();
        logger.info("Shopping cart cleared - car configuration and {} accessories removed", accessoryCount);
    }

    public int getTotalAccessoryCount() {
        return accessories.values().stream().mapToInt(item -> item.quantity).sum();
    }

    public int getTotalAccessoryQuantity() {
        return accessories.values().stream().mapToInt(item -> item.quantity).sum();
    }

    public double getTotalAccessoryPrice() {
        return accessories.values().stream().mapToDouble(AccessoryItem::getTotal).sum();
    }

    public int getUniqueAccessoryCount() {
        return accessories.size();
    }

    public boolean hasAccessories() {
        return !accessories.isEmpty();
    }

    public boolean hasCarConfiguration() {
        return currentCarConfig != null;
    }

    public void setSelectedAccessories(List<Integer> accessoryIds) {
        this.selectedAccessoryIds = new ArrayList<>(accessoryIds);
        logger.debug("Set {} selected accessory IDs", accessoryIds.size());
    }

    public List<Integer> getSelectedAccessories() {
        return new ArrayList<>(selectedAccessoryIds);
    }

    public void addSelectedAccessory(int accessoryId) {
        selectedAccessoryIds.add(accessoryId);
        logger.debug("Added accessory ID: {} to selected accessories", accessoryId);
    }

    public void addSelectedAccessoryWithQuantity(int accessoryId, int quantity) {
        for (int i = 0; i < quantity; i++) {
            selectedAccessoryIds.add(accessoryId);
        }
        logger.debug("Added {} of accessory ID: {} to selected accessories", quantity, accessoryId);
    }

    public void removeSelectedAccessory(int accessoryId) {
        selectedAccessoryIds.remove(Integer.valueOf(accessoryId));
        logger.debug("Removed accessory ID: {} from selected accessories", accessoryId);
    }

    public void clearSelectedAccessories() {
        int count = selectedAccessoryIds.size();
        selectedAccessoryIds.clear();
        logger.debug("Cleared {} selected accessory IDs", count);
    }

    public boolean isAccessorySelected(int accessoryId) {
        return selectedAccessoryIds.contains(accessoryId);
    }

    public int getAccessoryQuantity(int accessoryId) {
        return (int) selectedAccessoryIds.stream()
                .filter(id -> id == accessoryId)
                .count();
    }

    public List<CustomizationRecord> getPendingCustomizations() {
        return new ArrayList<>(pendingCustomizations);
    }

    public void setPendingCustomizations(List<CustomizationRecord> pendingCustomizations) {
        this.pendingCustomizations = new ArrayList<>(pendingCustomizations);
    }

    public void addPendingCustomization(CustomizationRecord customization) {
        this.pendingCustomizations.add(customization);
    }

    public void clearPendingCustomizations() {
        this.pendingCustomizations.clear();
    }

    public int getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserMrc() {
        return userMrc;
    }

    public Role getUserRole() {
        return userRole;
    }

    public String getPassword() {
        return password;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public LocalDate getDob() {
        return dob;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public boolean isUserStatus() {
        return userStatus;
    }

    public String getUserPhoto() {
        return userPhoto;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserMrc(String userMrc) {
        this.userMrc = userMrc;
    }

    public void setUserRole(Role userRole) {
        this.userRole = userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = Role.fromString(userRole);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void setUserStatus(boolean userStatus) {
        this.userStatus = userStatus;
    }

    public void setUserPhoto(String userPhoto) {
        this.userPhoto = userPhoto;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public static boolean hasPermission(String requiredPermission) {
        if (!isSessionActive()) return false;

        Role userRole = getLoggedInStaffRoleEnum();
        if (userRole == null) return false;

        switch (requiredPermission.toUpperCase()) {
            case "ADMIN":
                return userRole == Role.ADMIN;
            case "MANAGER":
                return userRole == Role.MANAGER || userRole == Role.ADMIN;
            case "STAFF":
                return userRole == Role.STAFF || userRole == Role.MANAGER || userRole == Role.ADMIN;
            default:
                return false;
        }
    }

    public static boolean hasPermission(Role requiredRole) {
        if (!isSessionActive()) return false;

        Role userRole = getLoggedInStaffRoleEnum();
        if (userRole == null) return false;

        switch (requiredRole) {
            case ADMIN:
                return userRole == Role.ADMIN;
            case MANAGER:
                return userRole == Role.MANAGER || userRole == Role.ADMIN;
            case STAFF:
                return userRole == Role.STAFF || userRole == Role.MANAGER || userRole == Role.ADMIN;
            default:
                return false;
        }
    }

    public static boolean isSessionExpired() {
        SessionStaff instance = getInstance();
        if (instance.loginTime == null) {
            if (Session.getInstance() != null) {
                return false;
            }
            return true;
        }

        LocalDateTime expiryTime = instance.loginTime.plusHours(8);
        boolean expired = LocalDateTime.now().isAfter(expiryTime);

        if (expired) {
            logger.info("Session expired for user: {}. Login time: {}, Current time: {}",
                    instance.userName, instance.loginTime, LocalDateTime.now());
        }

        return expired;
    }

    public static boolean isSessionExpired(long hours) {
        SessionStaff instance = getInstance();
        if (instance.loginTime == null) return true;

        LocalDateTime expiryTime = instance.loginTime.plusHours(hours);
        return LocalDateTime.now().isAfter(expiryTime);
    }

    public static void refreshSession() {
        SessionStaff instance = getInstance();
        if (instance.userId != 0) {
            instance.loginTime = LocalDateTime.now();
            logger.debug("Session refreshed for user: {}", instance.userName);
        }
    }

    public static boolean isUserAccountActive() {
        SessionStaff instance = getInstance();
        return instance.userStatus &&
                (instance.endDate == null || instance.endDate.isAfter(LocalDate.now()));
    }

    public static long getSessionDurationMinutes() {
        SessionStaff instance = getInstance();
        if (instance.loginTime == null) return 0;

        return java.time.Duration.between(instance.loginTime, LocalDateTime.now()).toMinutes();
    }

    public static boolean validateSessionForOperation(String operation) {
        if (!isSessionActive()) {
            logger.warn("Attempted operation '{}' with no active session", operation);
            return false;
        }

        if (!isUserAccountActive()) {
            logger.warn("Attempted operation '{}' with inactive user account", operation);
            return false;
        }

        if (isSessionExpired()) {
            logger.warn("Attempted operation '{}' with expired session", operation);
            return false;
        }

        return true;
    }

    public static void recordManualCheckIn(int staffId) {
        LocalDateTime checkInTime = LocalDateTime.now();
        logStaffCheckIn(staffId, checkInTime);
    }

    public static void recordManualCheckOut(int staffId) {
        LocalDateTime checkOutTime = LocalDateTime.now();
        logStaffCheckOut(staffId, checkOutTime);
    }

    public static boolean hasCheckedInToday(int staffId) {
        String sql = "SELECT COUNT(*) FROM user_attendance WHERE user_id = ? AND DATE(check_in) = CURRENT_DATE";

        try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, staffId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            logger.error("Failed to check if staff ID: {} has checked in today", staffId, e);
        }

        return false;
    }

    public static LocalDateTime getTodaysCheckInTime(int staffId) {
        String sql = "SELECT check_in FROM user_attendance WHERE user_id = ? AND DATE(check_in) = CURRENT_DATE";

        try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, staffId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getTimestamp("check_in").toLocalDateTime();
            }

        } catch (SQLException e) {
            logger.error("Failed to get today's check-in time for staff ID: {}", staffId, e);
        }

        return null;
    }

    @Override
    public String toString() {
        return String.format(
                "SessionStaff{userId=%d, userName='%s', userRole=%s, userEmail='%s', loginTime=%s, active=%s}",
                userId, userName, userRole, userEmail, loginTime, userStatus
        );
    }
}