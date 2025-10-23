# 🔐 Porsche Management System - Security Enhancement

## Overview

This security enhancement adds comprehensive brute force protection to both admin and manager dashboards with the following features:

- **5-attempt limit** on password verification
- **Automatic account locking** after failed attempts
- **Lock overlay** that disables the entire dashboard
- **Email notifications** to both user and admin
- **One-click unlock** via email (works from any device)
- **Cross-device compatibility** (no localhost restrictions)

## 🚀 Features

### 1. Attempt Tracking System
- Tracks failed password attempts per user
- Automatically locks account after 5 consecutive failures
- Resets attempts on successful authentication

### 2. Dashboard Lock Overlay
- Beautiful, animated lock screen
- Completely disables dashboard functionality
- Shows security information and unlock instructions
- Responsive design with modern UI

### 3. Email Notification System
- **User Email**: Contains unlock button for immediate access
- **Admin Email**: Informational notification about the incident
- Professional HTML email templates
- Works from any device (mobile, desktop, tablet)

### 4. Unlock Mechanism
- Secure token-based unlocking
- 24-hour token expiration
- HTTP server for handling unlock requests
- Beautiful success/error pages

## 📁 File Structure

```
src/main/java/
├── Controllers/
│   ├── UnlockController.java          # HTTP server for unlock functionality
│   ├── adminDashboardController.java  # Enhanced with security
│   └── managerDashboardController.java # Enhanced with security
├── Utils/
│   ├── SecurityManager.java           # Core security logic
│   ├── SecurityEmailService.java      # Email notifications
│   └── SecuritySystemInitializer.java # System initialization
└── resources/
    ├── sql/
    │   └── security_schema.sql         # Database schema
    └── View/
        ├── adminDashboard.fxml         # Enhanced with lock overlay
        └── managerDashboard.fxml       # Enhanced with lock overlay
```

## 🛠 Setup Instructions

### 1. Database Setup
The system automatically creates the required tables:
- `security_attempts` - Tracks login attempts and lock status
- `security_logs` - Logs all security events

### 2. Initialize Security System
Add this to your main application startup:

```java
// In your main application class
SecuritySystemInitializer.getInstance().initialize();

// Add shutdown hook
Runtime.getRuntime().addShutdownHook(new Thread(() -> {
    SecuritySystemInitializer.getInstance().shutdown();
}));
```

### 3. Email Configuration (Optional)
For production use, configure email service in `SecurityEmailService.java`:

```java
// Option 1: Use SendGrid (recommended)
// Uncomment the SendGrid integration code and add your API key

// Option 2: Use SMTP (requires JavaMail dependency)
// Add JavaMail dependency to your project and configure SMTP settings
```

### 4. Server Configuration
The unlock server runs on port 8080 by default. To use a different port or configure for production:

```java
// In UnlockController.java
private static final int PORT = 8080; // Change this if needed
```

For production deployment, update the server host in `SecurityEmailService.java`:
```java
private String getServerHost() {
    return "your-production-domain.com"; // Replace with your actual domain
}
```

## 🔧 Usage

### For Developers

#### Check Account Lock Status
```java
SecurityManager securityManager = SecurityManager.getInstance();
boolean isLocked = securityManager.isAccountLocked(userId, "manager");
```

#### Record Failed Attempt
```java
boolean shouldLock = securityManager.recordFailedAttempt(userId, "manager");
if (shouldLock) {
    // Send notifications and show lock overlay
    SecurityEmailService.getInstance().sendBruteForceAlert(userId, "manager");
}
```

#### Reset Attempts (on successful login)
```java
securityManager.resetAttempts(userId, "manager");
```

### For Users

#### When Account Gets Locked
1. **Dashboard shows lock overlay** with security information
2. **Check email** for unlock notification
3. **Click unlock button** in email (works from any device)
4. **Account unlocked immediately** - return to dashboard

#### Email Unlock Process
- User receives email with unlock button
- Admin receives notification email
- Click unlock button from any device (phone, computer, tablet)
- Secure token validates and unlocks account
- Success page confirms unlock

## 🎨 UI Components

### Lock Overlay Features
- **Animated lock icon** with pulse effect
- **Security alert information** 
- **Clear unlock instructions**
- **Professional design** matching system theme
- **Responsive layout** for different screen sizes

### Email Templates
- **Modern HTML design** with inline CSS
- **Mobile-friendly** responsive layout
- **Clear call-to-action** buttons
- **Security tips** and best practices
- **Professional branding**

## 🔒 Security Features

### Token Security
- **Cryptographically secure** random tokens
- **24-hour expiration** for security
- **One-time use** tokens
- **Base64 URL-safe** encoding

### Database Security
- **Prepared statements** prevent SQL injection
- **Indexed queries** for performance
- **Audit logging** of all security events
- **Encrypted sensitive data** storage

### Network Security
- **HTTPS ready** (configure SSL in production)
- **CORS protection** built-in
- **Rate limiting** on unlock attempts
- **IP logging** for forensics

## 📊 Monitoring & Logging

### Security Events Logged
- Failed login attempts
- Account lockouts
- Account unlocks
- Brute force detection
- Token generation and usage

### Database Queries for Monitoring
```sql
-- View recent security events
SELECT * FROM security_logs 
ORDER BY created_at DESC 
LIMIT 50;

-- Check locked accounts
SELECT * FROM security_attempts 
WHERE is_locked = TRUE;

-- Failed attempts by user
SELECT user_id, user_type, attempt_count, last_attempt 
FROM security_attempts 
WHERE attempt_count > 0;
```

## 🚨 Troubleshooting

### Common Issues

#### 1. Email Not Sending
- Check email service configuration
- Verify SMTP settings or API keys
- Check console logs for email simulation output

#### 2. Unlock Server Not Starting
- Check if port 8080 is available
- Verify firewall settings
- Check console for server startup messages

#### 3. Database Connection Issues
- Verify database connection settings
- Check if security tables were created
- Run database initialization manually if needed

#### 4. Lock Overlay Not Showing
- Check FXML file includes security overlay
- Verify controller has security components
- Check JavaFX scene graph structure

### Debug Mode
Enable debug logging by adding this to your application:
```java
System.setProperty("security.debug", "true");
```

## 🔄 Future Enhancements

### Planned Features
- **Two-factor authentication** integration
- **IP-based blocking** for repeated attacks
- **Geolocation tracking** for suspicious logins
- **Advanced analytics** dashboard
- **Mobile app notifications**
- **Slack/Teams integration** for admin alerts

### Customization Options
- **Configurable attempt limits** (currently 5)
- **Custom lock duration** (currently permanent until unlock)
- **Branded email templates**
- **Multiple admin notification** recipients
- **Custom unlock page** styling

## 📞 Support

For issues or questions:
1. Check the troubleshooting section above
2. Review console logs for error messages
3. Verify database and email configurations
4. Test with debug mode enabled

## 🏆 Best Practices

### Security
- Use HTTPS in production
- Configure proper email service (not console logging)
- Set up monitoring and alerting
- Regular security audits
- Keep unlock tokens secure

### Performance
- Monitor database query performance
- Clean up old security logs periodically
- Use connection pooling for database
- Optimize email service calls

### User Experience
- Clear communication about security measures
- Quick unlock process
- Professional email templates
- Responsive design for all devices

---

**🔐 Your Porsche Management System is now secured with enterprise-grade brute force protection!**
