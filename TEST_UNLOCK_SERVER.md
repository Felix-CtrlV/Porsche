# 🧪 Test Unlock Server

## ✅ **Quick Test Steps**

### **1. Start Your Application**
- Run your Porsche Management System
- Look for this message in console:
```
🌐 Unlock server started on all interfaces, port 8080
📱 Access from other devices: http://[your-ip]:8080/unlock?token=...
```

### **2. Test from Same Computer**
Open browser and go to:
```
http://localhost:8080/unlock?token=test123
```

**Expected Result:** Beautiful unlock page with "Unlock Failed" message (token is invalid, but page loads)

### **3. Find Your IP Address**
Open Command Prompt and run:
```cmd
ipconfig
```
Look for your IPv4 address (e.g., 192.168.137.12)

### **4. Test from Phone/Tablet**
Connect phone to same WiFi network, open browser:
```
http://192.168.137.12:8080/unlock?token=test123
```
(Replace with your actual IP)

**Expected Result:** Same beautiful unlock page loads on mobile device

## 🔥 **If Step 4 Fails (Can't Reach Site)**

### **Most Common Fix: Windows Firewall**
1. **Windows Key + R** → type `firewall.cpl` → Enter
2. Click **"Allow an app or feature through Windows Defender Firewall"**
3. Click **"Change settings"** → **"Allow another app..."**
4. Browse to Java (e.g., `C:\Program Files\Java\jdk-21\bin\java.exe`)
5. Check **Private** and **Public** networks
6. Click **OK**
7. **Restart your Java application**
8. Try Step 4 again

### **Alternative: Allow Port 8080**
1. **Windows Key + R** → type `wf.msc` → Enter
2. **Inbound Rules** → **New Rule...**
3. **Port** → **TCP** → **8080**
4. **Allow the connection**
5. Check all network types
6. Name: "Porsche Unlock Server"

## 🎯 **Real Test with Security System**

### **Trigger Account Lock:**
1. Go to Manager Dashboard
2. Settings → View Profile  
3. Enter wrong password 5 times
4. Account gets locked

### **Check Email:**
- Should receive email with unlock link
- Link format: `http://[your-ip]:8080/unlock?token=abc123...`

### **Test Unlock from Phone:**
- Open email on phone
- Click unlock link
- Should open beautiful unlock page
- Click "Unlock My Account"
- Should show success message

## 🌐 **Network Requirements**

### **Same Network:**
- Computer and phone must be on same WiFi
- Corporate networks might block device-to-device communication

### **IP Address Types:**
- ✅ `192.168.x.x` - Home/office network
- ✅ `10.x.x.x` - Corporate network  
- ✅ `172.16.x.x` - Private network
- ❌ `127.x.x.x` - Localhost only

## 🔧 **Troubleshooting Commands**

### **Test Server Response:**
```cmd
curl http://localhost:8080/unlock?token=test
```

### **Test Network Access:**
```cmd
curl http://192.168.137.12:8080/unlock?token=test
```

### **Check if Port is Open:**
```cmd
netstat -an | findstr :8080
```
Should show: `TCP    0.0.0.0:8080    0.0.0.0:0    LISTENING`

---

**The unlock server is now configured for cross-device access! 📱🌐**
