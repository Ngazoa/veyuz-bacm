package com.akouma.veyuzwebapp.service;

import com.akouma.veyuzwebapp.model.Banque;
import com.akouma.veyuzwebapp.model.Lettre;
import com.akouma.veyuzwebapp.model.Mail;
import com.akouma.veyuzwebapp.repository.MailRepository;
import com.akouma.veyuzwebapp.utils.CryptoUtils;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Data
@Service
public class MailService {

    @Autowired
    private MailRepository mailRepository;

    @Autowired
    private LettreService lettreService;
    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private CryptoUtils cryptoUtils;

    @Autowired
    private Environment environment;

    public void sendSimpleMessage(
            String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom(Objects.requireNonNull(environment.getProperty("spring.mail.from")));
        try {
            emailSender.send(message);
            Mail mail=new Mail();
            mail.setMessage(cryptoUtils.encrypt(text));
            mail.setReceiver(to);
            mail.setSubject(cryptoUtils.encrypt(subject));
            mailRepository.save(mail);
        }catch (Exception e){
            System.out.println("********************************* "+e.getMessage());   ;
        }
    }

    public void setMailRepository(MailRepository mailRepository) {
        this.mailRepository = mailRepository;
    }

    public Lettre getLettreMiseEnDemeurBanque(Banque banque) {
        Lettre letrre = lettreService.getBankLetter(banque, "mise-en-demeure");
        return letrre;
    }
}
