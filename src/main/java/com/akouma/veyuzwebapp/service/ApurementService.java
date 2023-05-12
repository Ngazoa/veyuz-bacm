package com.akouma.veyuzwebapp.service;

import com.akouma.veyuzwebapp.model.AppUser;
import com.akouma.veyuzwebapp.model.Apurement;
import com.akouma.veyuzwebapp.model.Banque;
import com.akouma.veyuzwebapp.model.Client;
import com.akouma.veyuzwebapp.repository.ApurementRepositoy;
import com.akouma.veyuzwebapp.utils.StatusTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApurementService {

    @Autowired
    private ApurementRepositoy apurementRepositoy;


    public Page<Apurement> getApurements(Banque banque, Client client, int max, Integer page, boolean isApured) {
        if (client == null) {
            return apurementRepositoy.findByBanqueAndIsApuredOrderByIsApuredAsc(banque, isApured, PageRequest.of(page, max));
        }
        return apurementRepositoy.findByBanqueAndClientAndIsApuredOrderByIsApuredAsc(banque, client, isApured, PageRequest.of(page, max));
    }

    public Page<Apurement> getApurementsTreasurySend(Banque banque, int max, Integer page, boolean isApured, int status) {

        return apurementRepositoy.findByBanqueAndIsApuredAndStatusOrderByDateOuvertureDesc(banque, isApured, status, PageRequest.of(page, max));
    }

    public Iterable<Apurement> getApurementscount(Banque banque, Client client, boolean isApured) {
        if (client == null) {
            return apurementRepositoy.findByBanqueAndIsApuredOrderByIsApuredAsc(banque, isApured);
        }
        return apurementRepositoy.findByBanqueAndClientAndIsApuredOrderByIsApuredAsc(banque, client, isApured);
    }


    public Apurement getApurementByReferenceTransaction(String referenceTransaction) {
        return apurementRepositoy.findFistByReferenceTransaction(referenceTransaction);
    }

    public Iterable<Apurement> getbyReference(Banque banque, Client client, String reference) {
        if (client == null) {
            return apurementRepositoy.findByBanqueAndReferenceTransactionContainingOrderByDateOuvertureDesc(banque, reference);
        }

        return apurementRepositoy.findByBanqueAndAndClientAndReferenceTransactionContainingOrderByDateOuvertureDesc(banque, client, reference);
    }

    @Transactional
    public Apurement saveApurement(Apurement apurement) {
        return apurementRepositoy.save(apurement);
    }

    public Iterable<Apurement> getNonApuredAndExpiredApurements() {
        return apurementRepositoy.findByIsApuredFalseAndIsExpiredFalse();
    }

    public Page<Apurement> getExpiredApurements(Banque banque, Client client, int max, Integer page, boolean isApured) {
        if (client == null) {
            System.out.println("Je suis admin");
            return apurementRepositoy.findByIsApuredFalseAndIsExpiredTrueAndBanque(banque, PageRequest.of(page, max));
        }
        System.out.println("Je suis client");
        return apurementRepositoy.findByIsApuredFalseAndIsExpiredTrueAndBanqueAndClient(banque, client, PageRequest.of(page, max));
    }

    public Iterable<Apurement> getNonApuredAndExpiredApurements(Banque banque) {
        return apurementRepositoy.findByBanqueAndIsApuredFalseAndIsExpiredFalse(banque);
    }

    public Iterable<? extends Apurement> getNonApuredAndExpiredApurements(Banque banque, Client client) {
        return apurementRepositoy.findByBanqueAndClientAndIsApuredFalseAndIsExpiredFalse(banque, client);
    }

    public Page<Apurement> getApurementsHasFiles(Banque banque, Client client, int max, Integer page, boolean isApured, int status) {
        if (client == null) {
            return apurementRepositoy.findByBanqueAndIsApuredAndStatusOrderByDateOuvertureDesc(banque, isApured, status, PageRequest.of(page, max));
        }

        return apurementRepositoy.findByBanqueAndClientAndIsApuredAndStatusOrderByDateOuvertureDesc(banque, client, isApured, status, PageRequest.of(page, max));
    }

    public Page<Apurement> getApurementsHasFilesAgence(Banque banque, AppUser appUser, int max, Integer page, boolean isApured, int status) {
        return apurementRepositoy.findByBanqueAndIsApuredAndStatusAndAppUserOrderByDateOuvertureDesc(banque, isApured, status, appUser, PageRequest.of(page, max));
    }

    public Page<Apurement> getNotApuredTransactions(Banque banque, AppUser appUser, int max, Integer page, boolean isApured, int status) {
        if (appUser != null)
            return apurementRepositoy.findByBanqueAndIsApuredAndStatusGreaterThanEqualAndAppUserOrderByDateOuvertureDesc(banque, isApured, status, appUser, PageRequest.of(page, max));

        return apurementRepositoy.findByBanqueAndIsApuredAndStatusGreaterThanEqualOrderByDateOuvertureDesc(banque, isApured, status, PageRequest.of(page, max));
    }

    public Page<Apurement> getAllByStatusAndBanqueApurements(Banque banque, int status, int max, Integer page) {
        return apurementRepositoy.findByBanqueAndStatus(banque, status, PageRequest.of(page, max));
    }

    public Page<Apurement> getAllByBAnqueAndStatusAndUser(Banque banque, AppUser loggedUser, int status, int max, Integer page) {
        return apurementRepositoy.findByBanqueAndStatusAndAppUser(banque, status, loggedUser, PageRequest.of(page, max));
    }
}
