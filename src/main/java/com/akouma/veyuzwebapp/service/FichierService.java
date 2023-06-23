package com.akouma.veyuzwebapp.service;

import com.akouma.veyuzwebapp.model.Fichier;
import com.akouma.veyuzwebapp.model.Transaction;
import com.akouma.veyuzwebapp.model.TypeDeFichier;
import com.akouma.veyuzwebapp.repository.FichierRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Data
@Service
public class FichierService {

    @Autowired
    private FichierRepository fichierRepository;

    public FichierRepository getFichierRepository() {
        return fichierRepository;
    }

    public void setFichierRepository(FichierRepository fichierRepository) {
        this.fichierRepository = fichierRepository;
    }

    public Iterable<Fichier> getFichiersTransaction(Transaction transaction, TypeDeFichier tf) {
        return fichierRepository.findByTransactionAndTypeDeFichier(transaction, tf);
    }

    public Fichier saveFichier(Fichier fichier) {
        return fichierRepository.save(fichier);
    }

    public Iterable<Fichier> getFichiersTransaction(Transaction t) {
        return fichierRepository.findByTransaction(t);
    }

    public void delete(Fichier f) {
        fichierRepository.delete(f);
    }
}
