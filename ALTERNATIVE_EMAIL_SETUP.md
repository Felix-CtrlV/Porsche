# Alternative Email Setup Options

## Option 1: Outlook/Hotmail (Easier Setup)
Update these lines in `OTPService.java`:

```java
private static final String SMTP_HOST = "smtp-mail.outlook.com";
private static final String SMTP_PORT = "587";
private static final String SENDER_EMAIL = "your-email@outlook.com";
private static final String SENDER_PASSWORD = "your-outlook-password";
```

## Option 2: Yahoo Mail
```java
private static final String SMTP_HOST = "smtp.mail.yahoo.com";
private static final String SMTP_PORT = "587";
private static final String SENDER_EMAIL = "your-email@yahoo.com";
private static final String SENDER_PASSWORD = "your-yahoo-password";
```

## Option 3: Custom SMTP Server
If you have access to any SMTP server:
```java
private static final String SMTP_HOST = "your-smtp-server.com";
private static final String SMTP_PORT = "587";
private static final String SENDER_EMAIL = "your-email@domain.com";
private static final String SENDER_PASSWORD = "your-password";
```

## Option 4: Test with Gmail (Temporary)
If you want to test immediately, you can try enabling "Less Secure Apps" in Gmail:
1. Go to: https://myaccount.google.com/security
2. Turn ON "Less secure app access"
3. Use your regular password `kaung273`

**Note:** This is less secure and Google may disable this feature.
