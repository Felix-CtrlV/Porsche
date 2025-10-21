package Utils;

import Database.DatabaseConnectionManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Simple HTTP server to handle dashboard unlock requests from email
 */
public class UnlockServer {
    private static UnlockServer instance;
    private HttpServer server;
    private static final int PORT = 8080;


    private UnlockServer() {
    }

    public static synchronized UnlockServer getInstance() {
        if (instance == null) {
            instance = new UnlockServer();
        }
        return instance;
    }

    /**
     * Starts the unlock server
     */
    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
            server.createContext("/unlock", new UnlockHandler());
            server.setExecutor(null);
            server.start();
            System.out.println("Unlock server started on port " + PORT);
        } catch (IOException e) {
            System.err.println("Failed to start unlock server: " + e.getMessage());
        }
    }

    /**
     * Stops the unlock server
     */
    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("Unlock server stopped");
        }
    }

    /**
     * Handler for unlock requests
     */
    static class UnlockHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String token = null;
            String userId = null;

            // Parse query parameters
            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    String[] pair = param.split("=");
                    if (pair.length == 2) {
                        if (pair[0].equals("token")) {
                            token = pair[1];
                        } else if (pair[0].equals("userId")) {
                            userId = pair[1];
                        }
                    }
                }
            }

            String response;
            int statusCode;

            if (token != null && userId != null) {
                boolean unlocked = unlockDashboard(userId, token);
                if (unlocked) {
                    response = getSuccessPage();
                    statusCode = 200;
                } else {
                    response = getErrorPage("Invalid or expired unlock link");
                    statusCode = 400;
                }
            } else {
                response = getErrorPage("Missing parameters");
                statusCode = 400;
            }

            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(statusCode, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private boolean unlockDashboard(String userId, String token) {
            try (Connection con = DatabaseConnectionManager.getInstance().getConnection()) {
                // First, verify the token exists and is not expired
                String checkSql = "SELECT id FROM security_locks WHERE user_id = ? AND lock_token = ? AND created_at > DATE_SUB(NOW(), INTERVAL 1 HOUR) AND unlock_status = FALSE";
                try (PreparedStatement checkPs = con.prepareStatement(checkSql)) {
                    checkPs.setInt(1, Integer.parseInt(userId));
                    checkPs.setString(2, token);

                    try (ResultSet rs = checkPs.executeQuery()) {
                        if (rs.next()) {
                            // Token is valid, update unlock status
                            String updateSql = "UPDATE security_locks SET unlock_status = TRUE, unlocked_at = NOW() WHERE user_id = ? AND lock_token = ?";
                            try (PreparedStatement updatePs = con.prepareStatement(updateSql)) {
                                updatePs.setInt(1, Integer.parseInt(userId));
                                updatePs.setString(2, token);
                                int updated = updatePs.executeUpdate();
                                return updated > 0;
                            }
                        }
                    }
                }
            } catch (SQLException | NumberFormatException e) {
                e.printStackTrace();
            }
            return false;
        }

        private String getSuccessPage() {
            return "<!DOCTYPE html>" +
                    "<html>" +
                    "<head>" +
                    "    <meta charset='UTF-8'>" +
                    "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                    "    <title>Dashboard Unlocked</title>" +
                    "    <style>" +
                    "        * { margin: 0; padding: 0; box-sizing: border-box; }" +
                    "        body { font-family: 'Segoe UI', Arial, sans-serif; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); min-height: 100vh; display: flex; align-items: center; justify-content: center; }" +
                    "        .container { background: white; border-radius: 20px; padding: 60px 40px; text-align: center; box-shadow: 0 20px 60px rgba(0,0,0,0.3); max-width: 500px; }" +
                    "        .icon { font-size: 80px; margin-bottom: 20px; animation: bounce 1s ease-in-out; }" +
                    "        @keyframes bounce { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-20px); } }" +
                    "        h1 { color: #10b981; font-size: 32px; margin-bottom: 15px; }" +
                    "        p { color: #6b7280; font-size: 16px; line-height: 1.6; margin-bottom: 30px; }" +
                    "        .success-box { background: #d1fae5; border: 2px solid #10b981; border-radius: 10px; padding: 20px; margin: 20px 0; }" +
                    "        .success-box p { color: #065f46; margin: 0; font-weight: 600; }" +
                    "        .note { font-size: 14px; color: #9ca3af; margin-top: 20px; }" +
                    "    </style>" +
                    "</head>" +
                    "<body>" +
                    "    <div class='container'>" +
                    "        <div class='icon'>🔓</div>" +
                    "        <h1>Dashboard Unlocked!</h1>" +
                    "        <p>Your dashboard has been successfully unlocked. You can now return to the application and continue working.</p>" +
                    "        <div class='success-box'>" +
                    "            <p>✓ Security verification complete</p>" +
                    "        </div>" +
                    "        <p class='note'>You can safely close this window and return to your dashboard.</p>" +
                    "    </div>" +
                    "</body>" +
                    "</html>";
        }

        private String getErrorPage(String errorMessage) {
            return "<!DOCTYPE html>" +
                    "<html>" +
                    "<head>" +
                    "    <meta charset='UTF-8'>" +
                    "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                    "    <title>Unlock Failed</title>" +
                    "    <style>" +
                    "        * { margin: 0; padding: 0; box-sizing: border-box; }" +
                    "        body { font-family: 'Segoe UI', Arial, sans-serif; background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%); min-height: 100vh; display: flex; align-items: center; justify-content: center; }" +
                    "        .container { background: white; border-radius: 20px; padding: 60px 40px; text-align: center; box-shadow: 0 20px 60px rgba(0,0,0,0.3); max-width: 500px; }" +
                    "        .icon { font-size: 80px; margin-bottom: 20px; }" +
                    "        h1 { color: #ef4444; font-size: 32px; margin-bottom: 15px; }" +
                    "        p { color: #6b7280; font-size: 16px; line-height: 1.6; margin-bottom: 30px; }" +
                    "        .error-box { background: #fee2e2; border: 2px solid #ef4444; border-radius: 10px; padding: 20px; margin: 20px 0; }" +
                    "        .error-box p { color: #991b1b; margin: 0; font-weight: 600; }" +
                    "        .note { font-size: 14px; color: #9ca3af; margin-top: 20px; }" +
                    "    </style>" +
                    "</head>" +
                    "<body>" +
                    "    <div class='container'>" +
                    "        <div class='icon'>⚠️</div>" +
                    "        <h1>Unlock Failed</h1>" +
                    "        <p>" + errorMessage + "</p>" +
                    "        <div class='error-box'>" +
                    "            <p>✕ Unable to verify unlock request</p>" +
                    "        </div>" +
                    "        <p class='note'>Please contact your system administrator if you continue to experience issues.</p>" +
                    "    </div>" +
                    "</body>" +
                    "</html>";
        }
    }
}
