package com.akouma.veyuzwebapp.restcontroller;

import com.akouma.veyuzwebapp.model.Banque;
import com.akouma.veyuzwebapp.model.Beneficiaire;
import com.akouma.veyuzwebapp.model.Client;
import com.akouma.veyuzwebapp.model.Domiciliation;
import com.akouma.veyuzwebapp.repository.BanqueRepository;
import com.akouma.veyuzwebapp.repository.BeneficiaireRepository;
import com.akouma.veyuzwebapp.service.DomiciliationService;
import com.akouma.veyuzwebapp.service.FichierService;
import com.akouma.veyuzwebapp.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;

@RestController
public class DomiciliationRestController {

    @Autowired
    private DomiciliationService domiciliationService;

    @Autowired
    private FichierService fichierService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private BeneficiaireRepository beneficiaireRepository;

    @GetMapping("/domiciliation-infos/{id}")
    public HashMap getTypeTransactionAnddevise(@PathVariable("id") Domiciliation domiciliation) {
        HashMap<String, Object> list = new HashMap<String, Object>();
        list.put("domiciliation", domiciliation);
        list.put("devise", domiciliation.getDevise());
        list.put("typeDeTransaction", domiciliation.getTypeDeTransaction());

        boolean isFinish = false;
        Calendar currentDate = Calendar.getInstance();
        if (domiciliation.getMontantRestant() <= 0 || domiciliation.getDateExpiration().getTime() < currentDate.getTime().getTime()) {
            isFinish = true;
        }
        list.put("isFinish", isFinish);
        return list;
    }

    @GetMapping("/get-client/{id}")
    public Collection<Beneficiaire> getClient(@PathVariable("id") Client client,Banque banque) {

        return beneficiaireRepository.findByBanque(banque);
    }



}
