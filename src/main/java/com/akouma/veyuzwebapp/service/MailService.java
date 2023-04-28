package com.akouma.veyuzwebapp.service;

import com.akouma.veyuzwebapp.model.Banque;
import com.akouma.veyuzwebapp.model.Lettre;
import com.akouma.veyuzwebapp.repository.MailRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Data
@Service
public class MailService {

    @Autowired
    private MailRepository mailRepository;

    @Autowired
    private LettreService lettreService;

    public MailRepository getMailRepository() {
        return mailRepository;
    }

    public void setMailRepository(MailRepository mailRepository) {
        this.mailRepository = mailRepository;
    }

    public Lettre getLettreMiseEnDemeurBanque(Banque banque){
      Lettre letrre=  lettreService.getBankLetter(banque,"mise-en-demeure");
        return letrre;
    }
}
