package com.akouma.veyuzwebapp.service;

import com.akouma.veyuzwebapp.model.Banque;
import com.akouma.veyuzwebapp.model.Client;
import com.akouma.veyuzwebapp.model.Devise;
import com.akouma.veyuzwebapp.model.Transaction;
import com.akouma.veyuzwebapp.repository.DeviseRepository;
import com.akouma.veyuzwebapp.repository.TransactionRepository;
import com.akouma.veyuzwebapp.utils.StatusTransaction;
import lombok.Data;
import org.apache.commons.math3.util.Precision;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

@Data
@Service
public class DeviseService {

    public DeviseRepository getDeviseRepository() {
        return deviseRepository;
    }

    public void setDeviseRepository(DeviseRepository deviseRepository) {
        this.deviseRepository = deviseRepository;
    }

    @Autowired
    private DeviseRepository deviseRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    public Iterable<Devise> getAll() {
        return deviseRepository.findAll();
    }

    public Collection<HashMap<String, Object>> getAll(Banque banque, Client client) {
        Iterable<Devise> devises = getAll();
        Iterable<Transaction> all;
        if (client == null) {
            all = transactionRepository.findByBanqueAndHasFilesTrue(banque);
        }
        else {
            all = transactionRepository.findByBanqueAndClientAndHasFilesTrue(banque, client);
        }

        long nb = all.spliterator().estimateSize();
        System.out.println("========================\nNombre total " + nb + "\n=========================");

        Collection<HashMap<String, Object>> response = new ArrayList<>();
        int i = 0;
        for (Devise devise : devises) {
            Iterable<Transaction> transactions;
            if (client == null) {
                transactions = transactionRepository.findByBanqueAndHasFilesAndDevise(banque, true, devise);
            }
            else {
                transactions = transactionRepository.findByBanqueAndClientAndHasFilesAndDevise(banque, client, true, devise);
            }
            long nbT = transactions.spliterator().estimateSize();
            HashMap<String, Object> item = new HashMap<>();
            float percent = ((float) nbT/nb) * 100;
            System.out.println("==============\n"+nbT+"/"+nb+" = "+percent+"\n=============");
            item.put("devise", devise);
            item.put("nbTransactions", nbT);
            item.put("textColor", StatusTransaction.TEXT_COLORS[i]);
            item.put("bgColor", StatusTransaction.BG_COLORS[i]);
            item.put("percent", Precision.round(percent, 2));
            item.put("percentStr", Precision.round(percent, 2)+"%");
            response.add(item);
            i++;
        }
        return response;
    }

}
