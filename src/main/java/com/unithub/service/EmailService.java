package com.unithub.service;

import com.unithub.dto.request.email.EmailDTO;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    public void sendEmail(EmailDTO emailDTO) {
        var message = new SimpleMailMessage();
        message.setFrom("noreply@email.com");
        message.setTo(emailDTO.to());
        message.setSubject(emailDTO.subject());
        message.setText(emailDTO.body());
        mailSender.send(message);
    }
}
