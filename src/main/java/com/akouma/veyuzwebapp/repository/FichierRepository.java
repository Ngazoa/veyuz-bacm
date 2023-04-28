package com.akouma.veyuzwebapp.repository;

import com.akouma.veyuzwebapp.model.Fichier;
import com.akouma.veyuzwebapp.model.Transaction;
import com.akouma.veyuzwebapp.model.TypeDeFichier;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FichierRepository extends CrudRepository<Fichier, Long> {

    Iterable<Fichier> findByTransactionAndTypeDeFichier(Transaction transaction, TypeDeFichier tf);

    Iterable<Fichier> findByTransaction(Transaction transaction);
}
