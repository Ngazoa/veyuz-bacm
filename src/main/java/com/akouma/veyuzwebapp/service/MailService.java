package com.akouma.veyuzwebapp.service;

import com.akouma.veyuzwebapp.model.Banque;
import com.akouma.veyuzwebapp.model.Lettre;
import com.akouma.veyuzwebapp.repository.MailRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Data
@Service
public class MailService {

    @Autowired
    private MailRepository mailRepository;

    @Autowired
    private LettreService lettreService;
    @Autowired
    private JavaMailSender emailSender;

    public void sendSimpleMessage(
            String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }

    public void setMailRepository(MailRepository mailRepository) {
        this.mailRepository = mailRepository;
    }

    public Lettre getLettreMiseEnDemeurBanque(Banque banque) {
        Lettre letrre = lettreService.getBankLetter(banque, "mise-en-demeure");
        return letrre;
    }
}
