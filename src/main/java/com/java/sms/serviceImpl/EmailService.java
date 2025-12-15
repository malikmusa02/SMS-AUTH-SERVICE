package com.java.sms.serviceImpl;



import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service class responsible for sending simple text-based emails.
 */
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Constructs the EmailService with the required JavaMailSender.
     *
     * @param mailSender Spring's mail sender used for sending emails
     */
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


    /**
     * Sends a plain text email to the specified recipient.
     *
     * @param to      recipient email address
     * @param subject email subject
     * @param body    email body content
     */
    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}

