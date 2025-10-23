# 🌐 Unlock Server Network Troubleshooting Guide

## ✅ **Issue Fixed**
The unlock server now binds to all network interfaces (`0.0.0.0:8080`) instead of just localhost, making it accessible from other devices.

## 🔧 **Additional Steps to Enable Cross-Device Access**

### 1. **Windows Firewall Configuration**
The most common issue is Windows Firewall blocking incoming connections.

#### **Option A: Allow Java through Firewall (Recommended)**
1. Open **Windows Defender Firewall** 
2. Click **"Allow an app or feature through Windows Defender Firewall"**
3. Click **"Change settings"** → **"Allow another app..."**
4. Browse to your Java installation (e.g., `C:\Program Files\Java\jdk-xx\bin\java.exe`)
5. Check both **Private** and **Public** networks
6. Click **OK**

#### **Option B: Allow Port 8080 (Alternative)**
1. Open **Windows Defender Firewall** → **Advanced settings**
2. Click **"Inbound Rules"** → **"New Rule..."**
3. Select **Port** → **TCP** → **Specific local ports: 8080**
4. Select **Allow the connection**
5. Check all network types
6. Name it "Porsche Unlock Server"

### 2. **Network Discovery**
Ensure network discovery is enabled:
1. **Control Panel** → **Network and Sharing Center**
2. **Change advanced sharing settings**
3. Enable **Network discovery** for your current profile

### 3. **Find Your IP Address**
Run this command in Command Prompt to find your computer's IP:
```cmd
ipconfig
```
Look for **IPv4 Address** under your active network adapter (usually starts with 192.168.x.x or 10.x.x.x)

## 📱 **Testing the Fix**

### **From Your Computer:**
- Test: `http://localhost:8080/unlock?token=test`
- Should show the unlock page

### **From Another Device (Phone/Tablet):**
- Test: `http://[YOUR-IP]:8080/unlock?token=test`
- Replace `[YOUR-IP]` with your actual IP address
- Example: `http://192.168.137.12:8080/unlock?token=test`

## 🔍 **Troubleshooting Steps**

### **Step 1: Verify Server is Running**
Check console output when starting your app:
```
🌐 Unlock server started on all interfaces, port 8080
📱 Access from other devices: http://[your-ip]:8080/unlock?token=...
```

### **Step 2: Test Local Access First**
- Open browser on same computer
- Go to: `http://localhost:8080/unlock?token=test`
- Should see unlock page (may show "invalid token" but page loads)

### **Step 3: Test Network Access**
- Find your IP: `ipconfig` in Command Prompt
- From phone/tablet browser: `http://[YOUR-IP]:8080/unlock?token=test`

### **Step 4: Check Firewall**
If Step 3 fails, it's likely Windows Firewall:
- Follow firewall configuration steps above
- Restart your Java application
- Try Step 3 again

### **Step 5: Router/Network Issues**
If still not working:
- Ensure both devices are on same WiFi network
- Some corporate/guest networks block device-to-device communication
- Try mobile hotspot as test

## 🚨 **Security Considerations**

### **Production Deployment:**
- Consider using HTTPS instead of HTTP
- Implement rate limiting
- Add IP whitelisting if needed
- Use proper domain name instead of IP

### **Current Setup (Development):**
- ✅ Server accessible from local network
- ✅ Tokens expire after 24 hours
- ✅ One-time use tokens
- ✅ Beautiful responsive unlock pages

## 📧 **Email Link Format**
The email unlock links now use your actual IP address:
```
http://192.168.137.12:8080/unlock?token=abc123...
```

## ⚡ **Quick Test Commands**

### **Test from Command Prompt (same computer):**
```cmd
curl http://localhost:8080/unlock?token=test
```

### **Test from Command Prompt (network access):**
```cmd
curl http://192.168.137.12:8080/unlock?token=test
```

## 🎯 **Expected Results**

### **Success:**
- Beautiful unlock page loads
- Shows "Invalid token" message (expected for test token)
- Page is mobile-responsive

### **Failure:**
- "Site can't be reached" error
- Connection timeout
- → Check firewall settings

---

**The unlock server is now configured to accept connections from any device on your network!** 🌐📱
