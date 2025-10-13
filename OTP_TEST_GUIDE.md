# OTP System Test Guide

## Current Configuration Status:
- ✅ Email: kaungswan59@gmail.com
- ❌ Password: kaung273 (This is a regular password, not an App Password)

## Issue Identified:
The password "kaung273" is a regular Gmail password, but Gmail requires an **App Password** for SMTP authentication when 2-Factor Authentication is enabled.

## Solution:

### Step 1: Enable 2-Factor Authentication
1. Go to https://myaccount.google.com/security
2. Click "2-Step Verification"
3. Follow the setup process

### Step 2: Generate App Password
1. Go to https://myaccount.google.com/apppasswords
2. Select "Mail" as the app
3. Select "Other" as device, enter "Porsche System"
4. Copy the 16-character password (format: xxxx xxxx xxxx xxxx)

### Step 3: Update OTPService.java
Replace line 25 in `src/main/java/Utils/OTPService.java`:
```java
private static final String SENDER_PASSWORD = "your-16-character-app-password";
```

### Step 4: Test the System
1. Run the application
2. Login as admin
3. Click the change password option
4. Enter current password
5. Check if OTP is sent to email

## Debug Information:
- Check application logs for detailed error messages
- Look for "SMTP Configuration" and "Authenticating with email" messages
- If authentication fails, the issue is with the App Password

## Alternative: Use a Different Email Provider
If Gmail setup is complex, you can use:
- Outlook/Hotmail
- Yahoo Mail
- Or any SMTP provider

Just update the SMTP_HOST and SMTP_PORT in OTPService.java accordingly.
