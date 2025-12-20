package Controllers.java.Utils;

import Utils.SecurityManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Ultra-simple network unlock server that just works
 * No complex IP detection, no fancy features - just reliable cross-device unlock
 */
public class SimpleNetworkUnlock {
    private static SimpleNetworkUnlock instance;
    private HttpServer server;
    private static final int PORT = 8888;
    
    private SimpleNetworkUnlock() {}
    
    public static SimpleNetworkUnlock getInstance() {
        if (instance == null) {
            instance = new SimpleNetworkUnlock();
        }
        return instance;
    }
    
    public void start() {
        try {
            // Create server that listens on ALL network interfaces
            server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
            
            // Add handlers
            server.createContext("/unlock", new UnlockHandler());
            server.createContext("/", new HomeHandler());
            
            server.setExecutor(null);
            server.start();
            
            // Get and display IP addresses
            String localIP = getLocalIP();
            
            System.out.println("=================================");
            System.out.println("🌐 SIMPLE UNLOCK SERVER STARTED");
            System.out.println("=================================");
            System.out.println("📍 Port: " + PORT);
            System.out.println("🏠 Local: http://localhost:" + PORT);
            System.out.println("📱 Network: http://" + localIP + ":" + PORT);
            System.out.println("=================================");
            System.out.println("🧪 TEST FROM PHONE: http://" + localIP + ":" + PORT);
            System.out.println("=================================");
            
        } catch (IOException e) {
            System.err.println("❌ Failed to start unlock server: " + e.getMessage());
        }
    }
    
    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("🛑 Unlock server stopped");
        }
    }
    
    public String getUnlockURL(String token) {
        String ip = getLocalIP();
        return "http://" + ip + ":" + PORT + "/unlock?token=" + token;
    }
    
    private String getLocalIP() {
        try {
            // Simple approach: get local host IP
            return InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "localhost";
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
                    }
                    .container { 
                        max-width: 400px; 
                        margin: 0 auto; 
                        background: white; 
                        padding: 30px; 
                        border-radius: 10px;
                        box-shadow: 0 4px 8px rgba(0,0,0,0.1);
                    }
                    .icon { font-size: 60px; margin-bottom: 20px; }
                    h1 { color: #28a745; margin-bottom: 20px; }
                    p { color: #666; line-height: 1.5; }
                    .button { 
                        background: #28a745; 
                        color: white; 
                        padding: 10px 20px; 
                        border: none; 
                        border-radius: 5px; 
                        margin-top: 20px;
                        cursor: pointer;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="icon">✅</div>
                    <h1>Account Unlocked!</h1>
                    <p>Your account has been successfully unlocked. You can now log in to the system.</p>
                    <button class="button" onclick="window.close()">Close</button>
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
                    }
                    .container { 
                        max-width: 400px; 
                        margin: 0 auto; 
                        background: white; 
                        padding: 30px; 
                        border-radius: 10px;
                        box-shadow: 0 4px 8px rgba(0,0,0,0.1);
                    }
                    .icon { font-size: 60px; margin-bottom: 20px; }
                    h1 { color: #dc3545; margin-bottom: 20px; }
                    p { color: #666; line-height: 1.5; }
                    .button { 
                        background: #6c757d; 
                        color: white; 
                        padding: 10px 20px; 
                        border: none; 
                        border-radius: 5px; 
                        margin-top: 20px;
                        cursor: pointer;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="icon">❌</div>
                    <h1>Unlock Failed</h1>
                    <p>The unlock token is invalid or has expired. Please contact your administrator.</p>
                    <button class="button" onclick="window.close()">Close</button>
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
                <title>Unlock Server Test</title>
                <style>
                    body { 
                        font-family: Arial, sans-serif; 
                        text-align: center; 
                        padding: 50px;
                        background: #e8f4fd;
                    }
                    .container { 
                        max-width: 500px; 
                        margin: 0 auto; 
                        background: white; 
                        padding: 30px; 
                        border-radius: 10px;
                        box-shadow: 0 4px 8px rgba(0,0,0,0.1);
                    }
                    .icon { font-size: 60px; margin-bottom: 20px; }
                    h1 { color: #007bff; margin-bottom: 20px; }
                    p { color: #666; line-height: 1.5; margin-bottom: 15px; }
                    .status { 
                        background: #d4edda; 
                        border: 1px solid #c3e6cb; 
                        color: #155724; 
                        padding: 15px; 
                        border-radius: 5px; 
                        margin: 20px 0;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="icon">🌐</div>
                    <h1>Unlock Server Working!</h1>
                    <div class="status">
                        <strong>✅ Server is running and accessible</strong>
                    </div>
                    <p><strong>If you can see this page from your phone, the unlock system will work!</strong></p>
                    <p>The server is ready to handle unlock requests from any device on this network.</p>
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
