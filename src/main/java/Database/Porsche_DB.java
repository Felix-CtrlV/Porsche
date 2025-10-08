package Database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Legacy database connection class.
 * @deprecated Use DatabaseConnectionManager.getInstance().getConnection() instead.
 * This class is kept for backward compatibility but delegates to the connection pool.
 */
@Deprecated
public class Porsche_DB {
    private static final Logger logger = LoggerFactory.getLogger(Porsche_DB.class);
    Connection con;

    /**
     * @deprecated Use DatabaseConnectionManager.getInstance().getConnection()
     */
    @Deprecated
    public Connection connect() throws SQLException {
        con = DatabaseConnectionManager.getInstance().getConnection();
        logger.debug("Connection obtained from pool");
        return con;
    }

    /**
     * Closes the connection and returns it to the pool.
     */
    public void disconnect() throws SQLException {
        if (con != null && !con.isClosed()) {
            con.close();
            logger.debug("Connection returned to pool");
        }
    }
}
