package com.akouma.veyuzwebapp.service;

import com.akouma.veyuzwebapp.model.Banque;
import com.akouma.veyuzwebapp.model.Lettre;
import com.akouma.veyuzwebapp.repository.LettreRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Data
@Service
public class LettreService {
    @Autowired
    private LettreRepository lettreRepository;

    public LettreRepository getLettreRepository() {
        return lettreRepository;
    }

    public void setLettreRepository(LettreRepository lettreRepository) {
        this.lettreRepository = lettreRepository;
    }



    public void lettreSave(Lettre lettre, Banque banque, String type) {
        Lettre ltr = getBankLetter(banque, type);
        if (ltr == null) {
            lettreRepository.save(lettre);
        } else {
            System.out.println(" Lettre trOUVEEE *************************************************");
            ltr.setContent(lettre.getContent());
            //lettreRepository.save(ltr);    //MISE A JOUR MODELE DE LETTRE
        }

    }

    public Lettre getBankLetter(Banque bq, String type) {
        return this.lettreRepository.findLettreByBanque(type, bq);
    }
}
