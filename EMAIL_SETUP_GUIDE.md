# Email Setup Guide for OTP System

## Gmail Configuration (Recommended)

### Step 1: Enable 2-Factor Authentication
1. Go to your Google Account settings
2. Navigate to Security → 2-Step Verification
3. Enable 2-Step Verification if not already enabled

### Step 2: Generate App Password
1. In Google Account settings, go to Security → App passwords
2. Select "Mail" as the app
3. Select "Other" as the device and enter "Porsche System"
4. Copy the generated 16-character password

### Step 3: Update OTPService.java
Open `src/main/java/Utils/OTPService.java` and update these lines:

```java
private static final String SENDER_EMAIL = "your-actual-email@gmail.com";
private static final String SENDER_PASSWORD = "your-16-character-app-password";
```

### Step 4: Test Configuration
Run the application and try the change password feature to test if OTP emails are sent.

## Alternative Email Providers

### Outlook/Hotmail
```java
private static final String SMTP_HOST = "smtp-mail.outlook.com";
private static final String SMTP_PORT = "587";
```

### Yahoo
```java
private static final String SMTP_HOST = "smtp.mail.yahoo.com";
private static final String SMTP_PORT = "587";
```

## Troubleshooting

### Common Issues:
1. **Authentication Failed**: Check if you're using App Password (not regular password)
2. **Connection Timeout**: Check internet connection and firewall settings
3. **SSL/TLS Issues**: Ensure your Java version supports TLS 1.2+

### Debug Steps:
1. Check the application logs for detailed error messages
2. Verify email credentials are correct
3. Test with a simple email client first
4. Check if your ISP blocks SMTP ports

## Security Notes:
- Never commit real email credentials to version control
- Use environment variables for production deployments
- Consider using a dedicated email service for production
