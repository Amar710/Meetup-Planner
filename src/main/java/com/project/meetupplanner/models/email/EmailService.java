package com.project.meetupplanner.models.email;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailService {

    private JavaMailSender javaMailSender;

    // Sender name and email address
    private final String senderName = "MeetUp-Planner Team";
    private final String senderEmail = "no-reply@meetup-planner.com"; 

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendEmail(String recipientEmail, String subject, String message) {
        SimpleMailMessage emailMessage = new SimpleMailMessage();
        emailMessage.setTo(recipientEmail);
        emailMessage.setFrom(senderName + " <" + senderEmail + ">"); 
        emailMessage.setSubject(subject);
        emailMessage.setText(message);

        javaMailSender.send(emailMessage);
    }
}



