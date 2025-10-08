package Utils;

import Database.DatabaseConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Helper class for handling logout operations.
 */
public class LogoutHelper {
    private static final Logger logger = LoggerFactory.getLogger(LogoutHelper.class);

    /**
     * Performs logout operation by calling the database logout procedure.
     * @param userId The ID of the user logging out
     * @return true if logout was successful, false otherwise
     */
    public static boolean performLogout(int userId) {
        try (Connection con = DatabaseConnectionManager.getInstance().getConnection();
             CallableStatement checkOut = con.prepareCall("call logout(?, ?)")) {
            
            checkOut.setInt(1, userId);
            checkOut.setString(2, String.valueOf(LocalDateTime.now()));
            checkOut.execute();
            
            logger.info("User {} logged out successfully", userId);
            Session.clearSession();
            return true;
        } catch (SQLException e) {
            logger.error("Failed to perform logout for user ID: " + userId, e);
            return false;
        }
    }
}
