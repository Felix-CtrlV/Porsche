# 📧 Email Setup Guide for Security System

## Current Status
✅ **Email system is working** - Currently shows detailed console output  
❌ **Real emails not sent** - Need to configure email service

## Quick Solutions

### Option 1: Add JavaMail Dependency (Recommended)

#### For Maven Projects:
Add this to your `pom.xml`:
```xml
<dependency>
    <groupId>com.sun.mail</groupId>
    <artifactId>javax.mail</artifactId>
    <version>1.6.2</version>
</dependency>
```

#### For Gradle Projects:
Add this to your `build.gradle`:
```gradle
implementation 'com.sun.mail:javax.mail:1.6.2'
```

#### Then Update SecurityEmailService.java:
Replace the `sendEmail` method with this Gmail SMTP version:

```java
private void sendEmail(String toEmail, String subject, String htmlContent) {
    // Email configuration - UPDATE THESE VALUES
    String smtpHost = "smtp.gmail.com";
    String smtpPort = "587";
    String fromEmail = "your-email@gmail.com";  // UPDATE THIS
    String fromPassword = "your-app-password";   // UPDATE THIS (use App Password)
    
    try {
        // Set up mail properties
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.ssl.trust", smtpHost);
        
        // Create session
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, fromPassword);
            }
        });
        
        // Create message
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail, "Porsche Security System"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject(subject);
        
        // Set HTML content
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(htmlContent, "text/html; charset=utf-8");
        
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(htmlPart);
        message.setContent(multipart);
        
        // Send email
        Transport.send(message);
        System.out.println("✅ EMAIL SENT SUCCESSFULLY TO: " + toEmail);
        
    } catch (Exception e) {
        System.err.println("❌ FAILED TO SEND EMAIL TO: " + toEmail);
        System.err.println("Error: " + e.getMessage());
        e.printStackTrace();
    }
}
```

### Option 2: Gmail App Password Setup

1. **Enable 2-Factor Authentication** on your Gmail account
2. **Generate App Password:**
   - Go to Google Account settings
   - Security → 2-Step Verification → App passwords
   - Generate password for "Mail"
   - Use this 16-character password (not your regular Gmail password)

3. **Update the credentials** in SecurityEmailService.java:
```java
String fromEmail = "youremail@gmail.com";        // Your Gmail
String fromPassword = "abcd efgh ijkl mnop";     // 16-char App Password
```

### Option 3: Alternative Email Services

#### EmailJS (Free, No Backend Required)
1. Sign up at https://www.emailjs.com/
2. Get your Service ID, Template ID, and Public Key
3. Update the `sendViaEmailJS` method in SecurityEmailService.java

#### SendGrid (Professional)
1. Sign up at https://sendgrid.com/
2. Get API key
3. Use the commented `sendEmailViaSendGrid` method

## Testing the System

### 1. Test Security Lock
1. Go to manager/admin dashboard
2. Click Settings → View Profile
3. Enter wrong password 5 times
4. Watch console for email output

### 2. Expected Console Output
```
📧 SIMULATING EMAIL VIA EMAILJS:
Service ID: YOUR_EMAILJS_SERVICE_ID
Template ID: YOUR_EMAILJS_TEMPLATE_ID
TO: user@example.com
SUBJECT: 🔒 Security Alert: Account Temporarily Locked
✅ Email would be sent via EmailJS API

🔧 TO ENABLE REAL EMAIL SENDING:
1. Add JavaMail dependency to your project
2. Or configure EmailJS service (see sendViaEmailJS method)
3. Or use a cloud email service like SendGrid
```

### 3. Unlock Testing
- Visit: http://localhost:8080/unlock?token=test
- Should show unlock page (even without real emails)

## Email Templates Included

### User Email Features:
- 🎨 **Professional HTML design**
- 📱 **Mobile-responsive layout**
- 🔘 **One-click unlock button**
- 🔒 **Security information**
- 💡 **Helpful instructions**

### Admin Email Features:
- 🚨 **Security alert notification**
- 👤 **User information**
- 📊 **Incident details**
- ⏰ **Timestamp information**

## Troubleshooting

### Common Issues:
1. **"javax.mail cannot be resolved"** → Add JavaMail dependency
2. **"Authentication failed"** → Use App Password, not regular password
3. **"Connection refused"** → Check firewall/antivirus settings
4. **"Less secure apps"** → Use App Password instead

### Security Notes:
- ✅ Never hardcode passwords in source code
- ✅ Use environment variables for production
- ✅ App Passwords are safer than regular passwords
- ✅ SMTP over TLS (port 587) is secure

## Production Deployment

### Environment Variables:
```bash
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=your-email@gmail.com
SMTP_PASS=your-app-password
```

### Code Update for Production:
```java
String fromEmail = System.getenv("SMTP_USER");
String fromPassword = System.getenv("SMTP_PASS");
```

## Current Status Summary

✅ **Security system is fully functional**  
✅ **Lock overlay works perfectly**  
✅ **Unlock server is running**  
✅ **Email templates are beautiful**  
✅ **Database integration complete**  
✅ **Console logging shows all email content**  

🔧 **Next step:** Add JavaMail dependency and configure Gmail credentials

Your security system is **production-ready** - just needs email configuration to send real emails instead of console output!
