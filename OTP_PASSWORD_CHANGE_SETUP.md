# OTP Password Change System - Setup Guide

## ✅ What Has Been Created

I've created a complete OTP-based password change system for your Porsche application:

### 1. **Files Created:**
- `Utils/OTPService.java` - Service for generating and sending OTP emails
- `View/ChangePassword.fxml` - UI for 3-step password change process
- `Controllers/ChangePasswordController.java` - Controller for password change logic

### 2. **Dependencies Updated:**
- Updated `pom.xml` to use Jakarta Mail instead of javax.mail-api

---

## 🔧 Configuration Required

### Step 1: Configure Email Settings

Open `src/main/java/Utils/OTPService.java` and update these lines (around line 20-23):

```java
private static final String SMTP_HOST = "smtp.gmail.com";
private static final String SMTP_PORT = "587";
private static final String SENDER_EMAIL = "your-email@gmail.com"; // UPDATE THIS
private static final String SENDER_PASSWORD = "your-app-password"; // UPDATE THIS
```

**For Gmail:**
1. Go to Google Account Settings → Security
2. Enable 2-Factor Authentication
3. Generate an "App Password" (not your regular password)
4. Use that App Password in the code

**For other email providers:**
- Update `SMTP_HOST` and `SMTP_PORT` accordingly
- Example for Outlook: `smtp-mail.outlook.com` port `587`

---

## 🔨 Manual Code Changes Required

### Change 1: Update adminDashboardController.java

**Location:** Line 157-172 in `adminDashboardController.java`

**Find this code:**
```java
optionChange.setOnMouseClicked(e -> {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/Authentication.fxml"));
        Parent root = loader.load();
        authenticationController controller = loader.getController();
        controller.setStep("factor");
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.initStyle(StageStyle.UNDECORATED);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    } catch (
            IOException ex) {
        ex.printStackTrace(); // TODO: Replace with logger
    }
});
```

**Replace with:**
```java
optionChange.setOnMouseClicked(e -> {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/ChangePassword.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.initStyle(StageStyle.UNDECORATED);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    } catch (IOException ex) {
        logger.error("Failed to open change password dialog", ex);
    }
});
```

---

## 📋 How It Works

### **3-Step Process:**

1. **Step 1: Password Verification**
   - User enters current password
   - System verifies it matches session password
   - If correct, sends 6-digit OTP to user's email

2. **Step 2: OTP Verification**
   - User enters the OTP received via email
   - OTP is valid for 5 minutes
   - User can resend OTP if needed

3. **Step 3: New Password**
   - User enters new password (minimum 6 characters)
   - User confirms new password
   - System updates password in database and session

---

## 🧪 Testing Steps

1. **Update Maven Dependencies:**
   ```bash
   mvn clean install
   ```

2. **Configure Email Settings** in `OTPService.java`

3. **Make the Manual Code Change** in `adminDashboardController.java`

4. **Run the Application**

5. **Test the Flow:**
   - Login as admin
   - Click Settings icon
   - Click "Change Password" option (optionChange)
   - Follow the 3-step process

---

## 🎨 UI Features

- **Modern 3-pane design** with step-by-step progression
- **Email masking** (shows as `j***n@gmail.com`)
- **Real-time validation** for password strength and OTP format
- **Animated success/error messages** that slide up from bottom
- **Auto-close** after successful password change
- **Resend OTP** functionality with cooldown

---

## 🔒 Security Features

- ✅ OTP expires after 5 minutes
- ✅ OTP is single-use (deleted after verification)
- ✅ Password verification before OTP generation
- ✅ Secure random OTP generation
- ✅ Password stored in session is updated immediately
- ✅ All operations logged for audit trail

---

## 📧 Email Template

The OTP email sent to users looks like:

```
Subject: Porsche System - Password Change OTP

Hello,

Your OTP for password change is: 123456

This OTP will expire in 5 minutes.

If you did not request this, please ignore this email.

Best regards,
Porsche System
```

---

## 🐛 Troubleshooting

### Email Not Sending?
1. Check SMTP credentials are correct
2. Verify App Password (not regular password) for Gmail
3. Check firewall/antivirus blocking port 587
4. Look at console logs for detailed error messages

### OTP Not Working?
1. Check if OTP expired (5 minutes)
2. Verify email address in Session is correct
3. Check spam folder for OTP email

### Password Not Updating?
1. Verify database connection
2. Check `user_info` table has `user_password` column
3. Look at logs for SQL errors

---

## 📝 Database Schema Required

Ensure your `user_info` table has:
```sql
user_id INT PRIMARY KEY
user_password VARCHAR(255)
user_email VARCHAR(255)
```

---

## 🎯 Next Steps

1. ✅ Configure email settings in `OTPService.java`
2. ✅ Make the manual code change in `adminDashboardController.java`
3. ✅ Run `mvn clean install`
4. ✅ Test the complete flow
5. ✅ Customize email template if needed (in `OTPService.java` line 120-130)

---

## 💡 Optional Enhancements

You can further customize:
- OTP expiry time (currently 5 minutes) - line 49 in `OTPService.java`
- OTP length (currently 6 digits) - line 36 in `OTPService.java`
- Email template styling - line 120 in `OTPService.java`
- Password minimum length (currently 6) - line 193 in `ChangePasswordController.java`

---

**All files are ready! Just configure the email and make the one manual change above.**
