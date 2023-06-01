package com.akouma.veyuzwebapp.service;

import com.akouma.veyuzwebapp.model.Banque;
import com.akouma.veyuzwebapp.model.Lettre;
import com.akouma.veyuzwebapp.model.Mail;
import com.akouma.veyuzwebapp.repository.MailRepository;
import com.akouma.veyuzwebapp.utils.CryptoUtils;
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

    @Autowired
    private CryptoUtils cryptoUtils;

    public void sendSimpleMessage(
            String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        try {
            emailSender.send(message);
            Mail mail=new Mail();
            mail.setMessage(cryptoUtils.encrypt(text));
            mail.setReceiver(to);
            mail.setSubject(cryptoUtils.encrypt(subject));
            mailRepository.save(mail);
        }catch (Exception e){
            e.getStackTrace();
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
