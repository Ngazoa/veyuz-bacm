package com.akouma.veyuzwebapp.restcontroller;

import com.akouma.veyuzwebapp.model.*;
import com.akouma.veyuzwebapp.repository.BanqueRepository;
import com.akouma.veyuzwebapp.repository.BeneficiaireRepository;
import com.akouma.veyuzwebapp.service.DomiciliationService;
import com.akouma.veyuzwebapp.service.DomiciliationTransactionService;
import com.akouma.veyuzwebapp.service.FichierService;
import com.akouma.veyuzwebapp.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

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

    @Autowired
    private DomiciliationTransactionService domiciliationTransactionService;

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

    @GetMapping("/domiciliation-transaction")
    public ResponseEntity<?> saveDomiciliationTransaction(@RequestParam(value = "montant") Float montant, @RequestParam(value = "transactionId") Transaction transaction, @RequestParam(value = "domiciliationId") Domiciliation domiciliation){
        HashMap<String, Object> response = new HashMap<String, Object>();

        DomiciliationTransaction dt = new DomiciliationTransaction();
        dt.setDomiciliation(domiciliation);
        dt.setTransaction(transaction);
        dt.setMontant(montant);

        domiciliationTransactionService.save(dt);

        response.put("hasSave", true);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/rest-domiciliations/{client_id}/{devise_id}/{beneficiaire_id}/{typeDeTransaction_id}")
    public ResponseEntity<?> findBy(@PathVariable("client_id") Client client, @PathVariable("devise_id") Devise devise, @PathVariable("beneficiaire_id") Beneficiaire beneficiaire, @PathVariable("typeDeTransaction_id") TypeDeTransaction typeDeTransaction) {
        List<Domiciliation> domiciliations = domiciliationService.findBy(typeDeTransaction, beneficiaire, devise, client);
        boolean isEmpty = domiciliations.isEmpty();
        HashMap<String, Object> response = new HashMap<String, Object>();
        response.put("isEmpty", isEmpty);

        String table = "<table class='table table-striped'>" +
                "<thead>" +
                "<tr>" +
                "<th>#</th><th>Reference</th><th>Montant Disponible</th><th>Montant à débiter</th>" +
                "</tr>" +
                "</thead>" +
                "<tbody>";
        int cmp = 0;
        for (Domiciliation domiciliation : domiciliations) {
            table += "<tr>" +
                    "<td><input type='checkbox' value='" + domiciliation.getId() + "' name='domiciliationTransactions.domiciliation'></td>" +
                    "<td>" + domiciliation.getReference() + "</td>" +
                    "<td>" + domiciliation.getMontantRestant() + "</td>" +
                    "<td><input class='form-control' type='number' name='domiciliationTransactions.montant'>" +
                    "<div><input type='hidden' name='domiciliationTransactions.transaction'></div>" +
                    "</td>" +
                    "</tr>";
            cmp++;
        }

        response.put("table", table);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/get-client/{id}")
    public Collection<Beneficiaire> getClient(@PathVariable("id") Client client,Banque banque) {

        return beneficiaireRepository.findByBanque(banque);
    }



}
