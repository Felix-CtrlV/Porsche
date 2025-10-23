package Controllers;

import Utils.SecurityManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class UnlockController {
    private static UnlockController instance;
    private HttpServer server;
    private static final int PORT = 8080;
    
    private UnlockController() {}
    
    public static UnlockController getInstance() {
        if (instance == null) {
            instance = new UnlockController();
        }
        return instance;
    }
    
    public void startServer() {
        try {
            // Bind to all network interfaces (0.0.0.0) instead of just localhost
            server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
            server.createContext("/unlock", new UnlockHandler());
            server.setExecutor(null);
            server.start();
            System.out.println("🌐 Unlock server started on all interfaces, port " + PORT);
            System.out.println("📱 Access from other devices: http://[your-ip]:" + PORT + "/unlock?token=...");
        } catch (IOException e) {
            System.err.println("❌ Failed to start unlock server: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void stopServer() {
        if (server != null) {
            server.stop(0);
            System.out.println("Unlock server stopped");
        }
    }
    
    private static class UnlockHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            
            if ("GET".equals(method)) {
                handleUnlockRequest(exchange);
            } else {
                sendResponse(exchange, 405, "Method Not Allowed");
            }
        }
        
        private void handleUnlockRequest(HttpExchange exchange) throws IOException {
            URI requestURI = exchange.getRequestURI();
            Map<String, String> params = parseQuery(requestURI.getQuery());
            String token = params.get("token");
            
            if (token == null || token.trim().isEmpty()) {
                sendErrorPage(exchange, "Invalid Request", "No unlock token provided.");
                return;
            }
            
            SecurityManager securityManager = SecurityManager.getInstance();
            boolean unlocked = securityManager.unlockAccount(token);
            
            if (unlocked) {
                sendSuccessPage(exchange);
            } else {
                sendErrorPage(exchange, "Unlock Failed", "The unlock token is invalid or has expired.");
            }
        }
        
        private void sendSuccessPage(HttpExchange exchange) throws IOException {
            String html = createSuccessPage();
            sendResponse(exchange, 200, html);
        }
        
        private void sendErrorPage(HttpExchange exchange, String title, String message) throws IOException {
            String html = createErrorPage(title, message);
            sendResponse(exchange, 400, html);
        }
        
        private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(statusCode, response.getBytes().length);
            
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
        
        private String createSuccessPage() {
            return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Account Unlocked Successfully</title>
                    <style>
                        * { margin: 0; padding: 0; box-sizing: border-box; }
                        body { 
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
                            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                            min-height: 100vh; 
                            display: flex; 
                            align-items: center; 
                            justify-content: center;
                            padding: 20px;
                        }
                        .container { 
                            background: white; 
                            border-radius: 16px; 
                            padding: 40px; 
                            text-align: center; 
                            box-shadow: 0 20px 40px rgba(0,0,0,0.1);
                            max-width: 500px;
                            width: 100%;
                        }
                        .success-icon { 
                            font-size: 64px; 
                            margin-bottom: 20px; 
                            animation: bounce 2s infinite;
                        }
                        @keyframes bounce {
                            0%, 20%, 50%, 80%, 100% { transform: translateY(0); }
                            40% { transform: translateY(-10px); }
                            60% { transform: translateY(-5px); }
                        }
                        h1 { 
                            color: #10b981; 
                            margin-bottom: 15px; 
                            font-size: 28px;
                        }
                        p { 
                            color: #6b7280; 
                            margin-bottom: 30px; 
                            line-height: 1.6;
                            font-size: 16px;
                        }
                        .info-box {
                            background: #f0f9ff;
                            border: 1px solid #bae6fd;
                            border-radius: 8px;
                            padding: 20px;
                            margin: 20px 0;
                        }
                        .info-box h3 {
                            color: #0369a1;
                            margin-bottom: 10px;
                        }
                        .info-box ul {
                            text-align: left;
                            color: #374151;
                            padding-left: 20px;
                        }
                        .close-button {
                            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                            color: white;
                            border: none;
                            padding: 12px 30px;
                            border-radius: 8px;
                            font-size: 16px;
                            font-weight: bold;
                            cursor: pointer;
                            transition: opacity 0.3s;
                        }
                        .close-button:hover {
                            opacity: 0.9;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="success-icon">🔓</div>
                        <h1>Account Unlocked Successfully!</h1>
                        <p>Your account has been successfully unlocked. You can now log in to the Porsche Management System.</p>
                        
                        <div class="info-box">
                            <h3>🛡️ Security Reminder</h3>
                            <ul>
                                <li>Your failed login attempts have been reset</li>
                                <li>Please ensure you're using the correct password</li>
                                <li>Consider changing your password if you suspect unauthorized access</li>
                                <li>Contact your administrator if you continue to experience issues</li>
                            </ul>
                        </div>
                        
                        <button class="close-button" onclick="window.close()">Close This Window</button>
                        
                        <p style="margin-top: 20px; font-size: 14px; color: #9ca3af;">
                            You can safely close this window and return to the login page.
                        </p>
                    </div>
                    
                    <script>
                        // Auto-close after 10 seconds
                        setTimeout(() => {
                            window.close();
                        }, 10000);
                    </script>
                </body>
                </html>
                """;
        }
        
        private String createErrorPage(String title, String message) {
            return String.format("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>%s</title>
                    <style>
                        * { margin: 0; padding: 0; box-sizing: border-box; }
                        body { 
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
                            background: linear-gradient(135deg, #dc2626 0%, #991b1b 100%);
                            min-height: 100vh; 
                            display: flex; 
                            align-items: center; 
                            justify-content: center;
                            padding: 20px;
                        }
                        .container { 
                            background: white; 
                            border-radius: 16px; 
                            padding: 40px; 
                            text-align: center; 
                            box-shadow: 0 20px 40px rgba(0,0,0,0.1);
                            max-width: 500px;
                            width: 100%%;
                        }
                        .error-icon { 
                            font-size: 64px; 
                            margin-bottom: 20px; 
                            animation: shake 0.5s ease-in-out infinite alternate;
                        }
                        @keyframes shake {
                            0%% { transform: translateX(0); }
                            100%% { transform: translateX(5px); }
                        }
                        h1 { 
                            color: #dc2626; 
                            margin-bottom: 15px; 
                            font-size: 28px;
                        }
                        p { 
                            color: #6b7280; 
                            margin-bottom: 30px; 
                            line-height: 1.6;
                            font-size: 16px;
                        }
                        .info-box {
                            background: #fef2f2;
                            border: 1px solid #fecaca;
                            border-radius: 8px;
                            padding: 20px;
                            margin: 20px 0;
                        }
                        .info-box h3 {
                            color: #dc2626;
                            margin-bottom: 10px;
                        }
                        .info-box ul {
                            text-align: left;
                            color: #374151;
                            padding-left: 20px;
                        }
                        .close-button {
                            background: #6b7280;
                            color: white;
                            border: none;
                            padding: 12px 30px;
                            border-radius: 8px;
                            font-size: 16px;
                            font-weight: bold;
                            cursor: pointer;
                            transition: background-color 0.3s;
                        }
                        .close-button:hover {
                            background: #4b5563;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="error-icon">❌</div>
                        <h1>%s</h1>
                        <p>%s</p>
                        
                        <div class="info-box">
                            <h3>🔍 What can you do?</h3>
                            <ul>
                                <li>Check if the unlock link is correct and complete</li>
                                <li>Verify that the link hasn't expired (valid for 24 hours)</li>
                                <li>Contact your system administrator for assistance</li>
                                <li>Request a new unlock email if needed</li>
                            </ul>
                        </div>
                        
                        <button class="close-button" onclick="window.close()">Close This Window</button>
                        
                        <p style="margin-top: 20px; font-size: 14px; color: #9ca3af;">
                            If you continue to experience issues, please contact support.
                        </p>
                    </div>
                </body>
                </html>
                """, title, title, message);
        }
        
        private Map<String, String> parseQuery(String query) {
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
}
