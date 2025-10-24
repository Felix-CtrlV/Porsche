package Utils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Local-only unlock server - works only on the same device
 * No network complexity, just localhost access
 */
public class LocalUnlockServer {
    private static LocalUnlockServer instance;
    private HttpServer server;
    private static final int PORT = 7777;
    
    private LocalUnlockServer() {}
    
    public static LocalUnlockServer getInstance() {
        if (instance == null) {
            instance = new LocalUnlockServer();
        }
        return instance;
    }
    
    public void start() {
        try {
            // Bind to all interfaces for network access
            server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
            
            server.createContext("/unlock", new UnlockHandler());
            server.createContext("/", new HomeHandler());
            
            server.setExecutor(null);
            server.start();
            
            // Get network IP for display
            String networkIP = "localhost";
            try {
                networkIP = java.net.InetAddress.getLocalHost().getHostAddress();
            } catch (Exception e) {
                // Keep localhost as fallback
            }
            
            System.out.println("===============================");
            System.out.println("🌐 NETWORK UNLOCK SERVER STARTED");
            System.out.println("===============================");
            System.out.println("🏠 Local: http://localhost:" + PORT);
            System.out.println("📱 Network: http://" + networkIP + ":" + PORT);
            System.out.println("🔗 Cross-device access enabled");
            System.out.println("===============================");
            
        } catch (IOException e) {
            System.err.println("❌ Failed to start local unlock server: " + e.getMessage());
        }
    }
    
    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("🛑 Local unlock server stopped");
        }
    }
    
    public String getUnlockURL(String token) {
        try {
            // Get the actual network IP for cross-device access
            String ip = java.net.InetAddress.getLocalHost().getHostAddress();
            return "http://" + ip + ":" + PORT + "/unlock?token=" + token;
        } catch (Exception e) {
            return "http://localhost:" + PORT + "/unlock?token=" + token;
        }
    }
    
    // Handle unlock requests
    private static class UnlockHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
            String token = params.get("token");
            
            if (token == null || token.trim().isEmpty()) {
                sendResponse(exchange, createErrorPage());
                return;
            }
            
            // Try to unlock
            boolean success = SecurityManager.getInstance().unlockAccount(token);
            
            if (success) {
                sendResponse(exchange, createSuccessPage());
            } else {
                sendResponse(exchange, createErrorPage());
            }
        }
    }
    
    // Handle home page
    private static class HomeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            sendResponse(exchange, createTestPage());
        }
    }
    
    private static void sendResponse(HttpExchange exchange, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
    
    private static String createSuccessPage() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Account Unlocked</title>
                <style>
                    body { 
                        font-family: Arial, sans-serif; 
                        text-align: center; 
                        padding: 50px;
                        background: #e8f5e8;
                        margin: 0;
                    }
                    .container { 
                        max-width: 500px; 
                        margin: 0 auto; 
                        background: white; 
                        padding: 40px; 
                        border-radius: 15px;
                        box-shadow: 0 8px 16px rgba(0,0,0,0.1);
                    }
                    .icon { font-size: 80px; margin-bottom: 20px; color: #28a745; }
                    h1 { color: #28a745; margin-bottom: 20px; font-size: 32px; }
                    p { color: #666; line-height: 1.6; font-size: 18px; margin-bottom: 30px; }
                    .button { 
                        background: #28a745; 
                        color: white; 
                        padding: 15px 30px; 
                        border: none; 
                        border-radius: 8px; 
                        font-size: 16px;
                        cursor: pointer;
                        transition: background 0.3s;
                    }
                    .button:hover { background: #218838; }
                    .info { 
                        background: #d4edda; 
                        border: 1px solid #c3e6cb; 
                        color: #155724; 
                        padding: 15px; 
                        border-radius: 8px; 
                        margin: 20px 0;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="icon">🔓</div>
                    <h1>Account Unlocked Successfully!</h1>
                    <p>Your account has been unlocked and you can now log in to the Porsche Management System.</p>
                    
                    <div class="info">
                        <strong>✅ Account Status: UNLOCKED</strong><br>
                        You can now return to the login screen and access your account.
                    </div>
                    
                    <button class="button" onclick="window.close()">Close Window</button>
                    
                    <p style="margin-top: 20px; font-size: 14px; color: #999;">
                        You can safely close this window and return to the login page.
                    </p>
                </div>
            </body>
            </html>
            """;
    }
    
    private static String createErrorPage() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Unlock Failed</title>
                <style>
                    body { 
                        font-family: Arial, sans-serif; 
                        text-align: center; 
                        padding: 50px;
                        background: #f8e8e8;
                        margin: 0;
                    }
                    .container { 
                        max-width: 500px; 
                        margin: 0 auto; 
                        background: white; 
                        padding: 40px; 
                        border-radius: 15px;
                        box-shadow: 0 8px 16px rgba(0,0,0,0.1);
                    }
                    .icon { font-size: 80px; margin-bottom: 20px; color: #dc3545; }
                    h1 { color: #dc3545; margin-bottom: 20px; font-size: 32px; }
                    p { color: #666; line-height: 1.6; font-size: 18px; margin-bottom: 30px; }
                    .button { 
                        background: #6c757d; 
                        color: white; 
                        padding: 15px 30px; 
                        border: none; 
                        border-radius: 8px; 
                        font-size: 16px;
                        cursor: pointer;
                        transition: background 0.3s;
                    }
                    .button:hover { background: #545b62; }
                    .info { 
                        background: #f8d7da; 
                        border: 1px solid #f5c6cb; 
                        color: #721c24; 
                        padding: 15px; 
                        border-radius: 8px; 
                        margin: 20px 0;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="icon">❌</div>
                    <h1>Unlock Failed</h1>
                    <p>The unlock token is invalid, expired, or has already been used.</p>
                    
                    <div class="info">
                        <strong>❌ Unable to unlock account</strong><br>
                        Please contact your administrator for assistance.
                    </div>
                    
                    <button class="button" onclick="window.close()">Close Window</button>
                </div>
            </body>
            </html>
            """;
    }
    
    private static String createTestPage() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Local Unlock Server</title>
                <style>
                    body { 
                        font-family: Arial, sans-serif; 
                        text-align: center; 
                        padding: 50px;
                        background: #e8f4fd;
                        margin: 0;
                    }
                    .container { 
                        max-width: 600px; 
                        margin: 0 auto; 
                        background: white; 
                        padding: 40px; 
                        border-radius: 15px;
                        box-shadow: 0 8px 16px rgba(0,0,0,0.1);
                    }
                    .icon { font-size: 80px; margin-bottom: 20px; color: #007bff; }
                    h1 { color: #007bff; margin-bottom: 20px; font-size: 36px; }
                    p { color: #666; line-height: 1.6; font-size: 18px; margin-bottom: 20px; }
                    .status { 
                        background: #d1ecf1; 
                        border: 1px solid #bee5eb; 
                        color: #0c5460; 
                        padding: 20px; 
                        border-radius: 8px; 
                        margin: 20px 0;
                    }
                    .local-only {
                        background: #fff3cd;
                        border: 1px solid #ffeaa7;
                        color: #856404;
                        padding: 15px;
                        border-radius: 8px;
                        margin: 20px 0;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="icon">🖥️</div>
                    <h1>Local Unlock Server</h1>
                    
                    <div class="status">
                        <strong>✅ Server is running successfully!</strong><br>
                        The unlock system is ready to handle requests.
                    </div>
                    
                    <div class="local-only">
                        <strong>🏠 LOCAL DEVICE ONLY</strong><br>
                        This server only works on this computer for security.
                    </div>
                    
                    <p>This is the Porsche Security System unlock server. It handles account unlock requests when users are locked out due to multiple failed login attempts.</p>
                    
                    <p style="font-size: 14px; color: #999; margin-top: 30px;">
                        Server running on localhost:7777
                    </p>
                </div>
            </body>
            </html>
            """;
    }
    
    private static Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return params;
    }
}
