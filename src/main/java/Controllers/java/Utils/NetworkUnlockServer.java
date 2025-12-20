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
import java.util.Enumeration;

/**
 * Simple HTTP server for cross-device unlock functionality
 * Designed to work reliably across devices on the same network
 */
public class NetworkUnlockServer {
    private static NetworkUnlockServer instance;
    private HttpServer server;
    private static final int PORT = 9999; // Different port to avoid conflicts
    private String serverIP;
    
    private NetworkUnlockServer() {}
    
    public static NetworkUnlockServer getInstance() {
        if (instance == null) {
            instance = new NetworkUnlockServer();
        }
        return instance;
    }
    
    public void startServer() {
        try {
            // Get the best network IP address
            serverIP = getBestNetworkIP();
            
            // Create server that binds to all interfaces
            server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
            
            // Add unlock endpoint
            server.createContext("/unlock", new UnlockHandler());
            server.createContext("/test", new TestHandler());
            server.createContext("/", new RootHandler());
            
            server.setExecutor(null);
            server.start();
            
            System.out.println("🌐 Network Unlock Server Started Successfully!");
            System.out.println("📍 Server IP: " + serverIP);
            System.out.println("🔗 Local Access: http://localhost:" + PORT + "/unlock?token=...");
            System.out.println("📱 Network Access: http://" + serverIP + ":" + PORT + "/unlock?token=...");
            System.out.println("🧪 Test URL: http://" + serverIP + ":" + PORT + "/test");
            
        } catch (IOException e) {
            System.err.println("❌ Failed to start Network Unlock Server: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void stopServer() {
        if (server != null) {
            server.stop(0);
            System.out.println("🛑 Network Unlock Server stopped");
        }
    }
    
    public String getUnlockURL(String token) {
        if (serverIP != null) {
            return "http://" + serverIP + ":" + PORT + "/unlock?token=" + token;
        }
        return "http://localhost:" + PORT + "/unlock?token=" + token;
    }
    
    /**
     * Get the best network IP address for cross-device access
     */
    private String getBestNetworkIP() {
        try {
            // Try to find a non-loopback, non-virtual network interface
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                
                // Skip loopback and inactive interfaces
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }
                
                // Skip virtual interfaces (VirtualBox, VMware, etc.)
                String name = networkInterface.getName().toLowerCase();
                if (name.contains("virtual") || name.contains("vmware") || name.contains("vbox")) {
                    continue;
                }
                
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    
                    // We want IPv4, non-loopback, non-link-local addresses
                    if (address instanceof Inet4Address && 
                        !address.isLoopbackAddress() && 
                        !address.isLinkLocalAddress()) {
                        
                        String ip = address.getHostAddress();
                        System.out.println("🔍 Found network IP: " + ip + " on interface: " + networkInterface.getName());
                        return ip;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error detecting network IP: " + e.getMessage());
        }
        
        // Fallback to localhost
        return "localhost";
    }
    
    /**
     * Handle unlock requests
     */
    private static class UnlockHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            
            if ("GET".equals(method)) {
                handleUnlockRequest(exchange);
            } else {
                sendResponse(exchange, 405, createErrorPage("Method Not Allowed", "Only GET requests are supported."));
            }
        }
        
        private void handleUnlockRequest(HttpExchange exchange) throws IOException {
            URI requestURI = exchange.getRequestURI();
            Map<String, String> params = parseQuery(requestURI.getQuery());
            String token = params.get("token");
            
            if (token == null || token.trim().isEmpty()) {
                sendResponse(exchange, 400, createErrorPage("Invalid Request", "No unlock token provided."));
                return;
            }
            
            // Try to unlock the account
            Utils.SecurityManager securityManager = SecurityManager.getInstance();
            boolean unlocked = securityManager.unlockAccount(token);
            
            if (unlocked) {
                sendResponse(exchange, 200, createSuccessPage());
            } else {
                sendResponse(exchange, 400, createErrorPage("Unlock Failed", "The unlock token is invalid or has expired."));
            }
        }
    }
    
    /**
     * Handle test requests to verify server is working
     */
    private static class TestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String testPage = createTestPage();
            sendResponse(exchange, 200, testPage);
        }
    }
    
    /**
     * Handle root requests
     */
    private static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String welcomePage = createWelcomePage();
            sendResponse(exchange, 200, welcomePage);
        }
    }
    
    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
    
    private static String createSuccessPage() {
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
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        min-height: 100vh; 
                        display: flex; 
                        align-items: center; 
                        justify-content: center;
                        padding: 20px;
                    }
                    .container { 
                        background: white; 
                        border-radius: 20px; 
                        padding: 40px; 
                        text-align: center; 
                        box-shadow: 0 20px 40px rgba(0,0,0,0.1);
                        max-width: 500px;
                        width: 100%;
                        animation: slideUp 0.5s ease-out;
                    }
                    @keyframes slideUp {
                        from { opacity: 0; transform: translateY(30px); }
                        to { opacity: 1; transform: translateY(0); }
                    }
                    .success-icon { 
                        font-size: 80px; 
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
                        font-size: 32px;
                        font-weight: 700;
                    }
                    p { 
                        color: #6b7280; 
                        margin-bottom: 30px; 
                        line-height: 1.6;
                        font-size: 18px;
                    }
                    .close-button {
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        border: none;
                        padding: 15px 40px;
                        border-radius: 12px;
                        font-size: 18px;
                        font-weight: bold;
                        cursor: pointer;
                        transition: transform 0.2s, box-shadow 0.2s;
                    }
                    .close-button:hover {
                        transform: translateY(-2px);
                        box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);
                    }
                    .network-info {
                        background: #f0f9ff;
                        border: 1px solid #bae6fd;
                        border-radius: 12px;
                        padding: 20px;
                        margin: 20px 0;
                        font-size: 14px;
                        color: #0369a1;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="success-icon">🔓</div>
                    <h1>Account Unlocked!</h1>
                    <p>Your account has been successfully unlocked. You can now log in to the Porsche Management System.</p>
                    
                    <div class="network-info">
                        <strong>✅ Network Unlock Successful</strong><br>
                        Your account is now accessible from all devices.
                    </div>
                    
                    <button class="close-button" onclick="window.close()">Close Window</button>
                    
                    <p style="margin-top: 20px; font-size: 14px; color: #9ca3af;">
                        You can safely close this window and return to the login page.
                    </p>
                </div>
            </body>
            </html>
            """;
    }
    
    private static String createErrorPage(String title, String message) {
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
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        background: linear-gradient(135deg, #dc2626 0%, #991b1b 100%);
                        min-height: 100vh; 
                        display: flex; 
                        align-items: center; 
                        justify-content: center;
                        padding: 20px;
                    }
                    .container { 
                        background: white; 
                        border-radius: 20px; 
                        padding: 40px; 
                        text-align: center; 
                        box-shadow: 0 20px 40px rgba(0,0,0,0.1);
                        max-width: 500px;
                        width: 100%%;
                        animation: slideUp 0.5s ease-out;
                    }
                    @keyframes slideUp {
                        from { opacity: 0; transform: translateY(30px); }
                        to { opacity: 1; transform: translateY(0); }
                    }
                    .error-icon { 
                        font-size: 80px; 
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
                        font-size: 32px;
                        font-weight: 700;
                    }
                    p { 
                        color: #6b7280; 
                        margin-bottom: 30px; 
                        line-height: 1.6;
                        font-size: 18px;
                    }
                    .close-button {
                        background: #6b7280;
                        color: white;
                        border: none;
                        padding: 15px 40px;
                        border-radius: 12px;
                        font-size: 18px;
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
                    
                    <button class="close-button" onclick="window.close()">Close Window</button>
                    
                    <p style="margin-top: 20px; font-size: 14px; color: #9ca3af;">
                        If you continue to experience issues, please contact support.
                    </p>
                </div>
            </body>
            </html>
            """, title, title, message);
    }
    
    private static String createTestPage() {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Network Unlock Server - Test</title>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body { 
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        background: linear-gradient(135deg, #10b981 0%, #059669 100%);
                        min-height: 100vh; 
                        display: flex; 
                        align-items: center; 
                        justify-content: center;
                        padding: 20px;
                    }
                    .container { 
                        background: white; 
                        border-radius: 20px; 
                        padding: 40px; 
                        text-align: center; 
                        box-shadow: 0 20px 40px rgba(0,0,0,0.1);
                        max-width: 600px;
                        width: 100%;
                    }
                    h1 { color: #10b981; margin-bottom: 20px; font-size: 36px; }
                    .status { font-size: 64px; margin-bottom: 20px; }
                    p { color: #6b7280; margin-bottom: 15px; line-height: 1.6; }
                    .info-box {
                        background: #f0fdf4;
                        border: 1px solid #bbf7d0;
                        border-radius: 12px;
                        padding: 20px;
                        margin: 20px 0;
                        text-align: left;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="status">✅</div>
                    <h1>Server Working!</h1>
                    <p><strong>Network Unlock Server is running successfully</strong></p>
                    
                    <div class="info-box">
                        <h3 style="color: #059669; margin-bottom: 10px;">🌐 Network Status</h3>
                        <p><strong>✅ Server is accessible from this device</strong></p>
                        <p><strong>✅ Cross-device communication enabled</strong></p>
                        <p><strong>✅ Ready to handle unlock requests</strong></p>
                    </div>
                    
                    <p style="font-size: 14px; color: #9ca3af;">
                        If you can see this page from another device, the network unlock system is working correctly.
                    </p>
                </div>
            </body>
            </html>
            """;
    }
    
    private static String createWelcomePage() {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Porsche Network Unlock Server</title>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body { 
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        min-height: 100vh; 
                        display: flex; 
                        align-items: center; 
                        justify-content: center;
                        padding: 20px;
                    }
                    .container { 
                        background: white; 
                        border-radius: 20px; 
                        padding: 40px; 
                        text-align: center; 
                        box-shadow: 0 20px 40px rgba(0,0,0,0.1);
                        max-width: 600px;
                        width: 100%;
                    }
                    h1 { color: #667eea; margin-bottom: 20px; font-size: 36px; }
                    .logo { font-size: 64px; margin-bottom: 20px; }
                    p { color: #6b7280; margin-bottom: 15px; line-height: 1.6; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="logo">🔐</div>
                    <h1>Porsche Security System</h1>
                    <p><strong>Network Unlock Server</strong></p>
                    <p>This server handles secure account unlock requests across your network.</p>
                    <p style="margin-top: 30px; font-size: 14px; color: #9ca3af;">
                        Server is running and ready to process unlock requests.
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
