package com.aptech.projectmgmt.service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MailService {

    public void sendEmail(String toEmail, String subject, String body) {
        Properties mailProps = loadMailProperties();
        String host = mailProps.getProperty("smtp.host", "smtp.gmail.com");
        String port = mailProps.getProperty("smtp.port", "587");
        String username = firstNonBlank(mailProps.getProperty("smtp.username"));
        String password = firstNonBlank(mailProps.getProperty("smtp.password"));
        String from = mailProps.getProperty("smtp.from", username);

        if (username == null || password == null) {
            throw new RuntimeException("Thieu cau hinh SMTP trong application.properties");
        }

        Properties sessionProps = new Properties();
        sessionProps.put("mail.smtp.auth", "true");
        sessionProps.put("mail.smtp.starttls.enable", "true");
        sessionProps.put("mail.smtp.host", host);
        sessionProps.put("mail.smtp.port", port);

        Session session = Session.getInstance(sessionProps, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Khong the gui email: " + e.getMessage(), e);
        }
    }

    public boolean sendEmailQuietly(String toEmail, String subject, String body) {
        try {
            sendEmail(toEmail, subject, body);
            return true;
        } catch (RuntimeException ex) {
            System.err.println("[Mail] " + ex.getMessage());
            return false;
        }
    }

    private Properties loadMailProperties() {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            System.err.println("Khong the doc application.properties: " + e.getMessage());
        }
        return props;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }
}
