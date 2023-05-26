package com.akouma.veyuzwebapp.repository;

import com.akouma.veyuzwebapp.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;


@Repository
@Transactional
public interface TransactionRepository extends CrudRepository<Transaction, Long> {
    long countByBanqueAndAgenceAndStatut(Banque banque, Agence agence, int statut);

    List<Transaction>findByBanqueAndAgenceAndStatut(Banque banque, Agence agence, int statut,Pageable pageable);
    long countByBanqueAndAgence(Banque banque, Agence agence);

    Page<Transaction> findAll(Pageable pageable);

    Page<Transaction> findByBanqueAndClientOrderByDateCreationDesc(Banque banque, Client client, Pageable pageable);

    Page<Transaction> findByBanqueOrderByDateCreationDesc(Banque banque, Pageable pageable);

    Page<Transaction> findByBanqueAndClientAndDomiciliationOrderByDateCreationDesc(Banque banque, Client client, Domiciliation domiciliation, Client client1, Pageable pageable);

    Iterable<Transaction> findByBanqueAndDateCreationAfterOrderByDateCreationDesc(Banque banque, Date date);

    Iterable<Transaction> findByBanqueAndDateCreationBeforeOrderByDateCreationDesc(Banque banque, Date date);

    Iterable<Transaction> findByBanqueAndDateCreationBetweenOrderByDateCreationDesc(Banque banque, Date dateInf, Date dateSup);

    Page<Transaction> findByBanqueAndStatutOrderByDateCreationAsc(Banque banque, int status, Pageable pageable);

    Page<Transaction> findByBanqueAndClientAndStatutOrderByDateCreationDesc(Banque banque, Client client, int status, Pageable pageable);
    Page<Transaction> findByBanqueAndStatutOrderByDateCreationDesc(Banque banque,  int status, Pageable pageable);

    Iterable<Transaction> findByBanqueOrderByDateCreationDesc(Banque banque);

    Iterable<Transaction> findByBanqueAndClientOrderByDateCreationDesc(Banque banque, Client client);

    List<Transaction> findByBanqueAndClientAndTypeDeTransaction_IsImportAndHasSaction(Banque banque, Client client, boolean isImport, boolean hasSaction);

    Page<Transaction> findByBanqueOrderByDateCreationAsc(Banque banque, Pageable pageable);

    Iterable<Transaction> findByBanqueAndClientAndStatutNot(Banque banque, Client client, int statut);

    Iterable<Transaction> findByBanqueEqualsAndClientEqualsAndStatutNot(Banque banque, Client client, int statut);

    Page<Transaction> findByBanqueAndTypeDeTransactionIsImport(Banque banque, boolean isImport, Pageable pageable);

    Page<Transaction> findByBanqueAndClientAndTypeDeTransactionIsImport(Banque banque, Client client, boolean isImport, Pageable pageable);

    Page<Transaction> findByBanqueAndHasFilesOrderByDateCreationDesc(Banque banque, boolean hasFiles, Pageable pageable);
    Page<Transaction> findByBanqueAndAppUserAndHasFilesOrderByDateCreationDesc(Banque banque,AppUser user, boolean hasFiles, Pageable pageable);

    Page<Transaction> findByBanqueAndClientAndHasFilesOrderByDateCreationDesc(Banque banque, Client client, boolean hasFiles, Pageable pageable);
    Page<Transaction> findByBanqueAndAgenceAndHasFilesOrderByDateCreationDesc(Banque banque, Agence client, boolean hasFiles, Pageable pageable);

    Page<Transaction> findByBanqueAndStatutAndHasFilesOrderByDateCreationDesc(Banque banque, int statut, boolean hasFiles, Pageable pageable);

    Iterable<Transaction> findByBanqueAndHasFiles(Banque banque, boolean hasFiles);

    Iterable<Transaction> findByBanqueAndClientAndHasFiles(Banque banque, Client client, boolean hasFiles);

    Iterable<Transaction> findByBanqueAndHasFilesAndStatut(Banque banque, boolean hasFiles, int state);

    Iterable<Transaction> findByBanqueAndHasFilesAndDateCreationBetween(Banque banque, boolean hasFiles, Date dateMin, Date dateMax);

    Iterable<Transaction> findByBanqueAndHasFilesAndStatutAndDateCreationBetween(Banque banque, boolean hasFiles, int state, Date dateMin, Date dateMax);

    Iterable<Transaction> findByBanqueAndClientAndHasFilesAndStatut(Banque banque, Client client, boolean hasFiles, int state);

    Iterable<Transaction> findByBanqueAndTypeDeTransactionIsImport(Banque banque, boolean isImport);

    Page<Transaction> findByBanqueAndHasFilesAndStatutAndTypeDeTransactionIsImportAndDateCreationBetween(Banque banque, boolean b, int state, boolean b1, Date date1, Date date2, Pageable pageable);

    Page<Transaction> findByBanqueAndHasFilesAndTypeDeTransactionIsImportAndDeviseAndDateCreationBetween(Banque banque, boolean b, boolean b1, Devise devise, Date date1, Date date2, Pageable pageable);

    Page<Transaction> findByBanqueAndHasFilesAndTypeDeTransactionIsImportAndDeviseAndTypeDeTransactionAndDateCreationBetween(Banque banque, boolean b, boolean b1, Devise devise, TypeDeTransaction typeDeTransaction, Date date1, Date date2,Pageable pageable);

    Page<Transaction> findByBanqueAndHasFilesAndTypeDeTransactionIsImportAndDeviseAndStatutAndDateCreationBetween(Banque banque, boolean b, boolean b1, Devise devise, int state, Date date1, Date date2, Pageable pageable);

    Page<Transaction> findByBanqueAndHasFilesAndTypeDeTransactionIsImportAndDeviseAndTypeDeTransactionAndStatutAndDateCreationBetween(Banque banque, boolean b, boolean b1, Devise devise, TypeDeTransaction typeDeTransaction, int state, Date date1, Date date2, Pageable pageable);

    Page<Transaction> findByBanqueAndHasFilesAndTypeDeTransactionIsImportAndTypeDeTransactionAndDateCreationBetween(Banque banque, boolean b, boolean b1, TypeDeTransaction typeDeTransaction, Date date1, Date date2, Pageable pageable);

    Page<Transaction> findByBanqueAndHasFilesAndTypeDeTransactionIsImportAndTypeDeTransactionAndStatutAndDateCreationBetween(Banque banque, boolean b, boolean b1, TypeDeTransaction typeDeTransaction, int state, Date date1, Date date2, Pageable pageable);

    Page<Transaction> findByBanqueAndHasFilesAndTypeDeTransactionIsImportAndDateCreationBetween(Banque banque, boolean b, boolean b1, Date date1, Date date2, Pageable pageable);


    // =======================================================================================

    Iterable<Transaction> findByBanqueAndHasFilesAndStatutAndTypeDeTransactionIsImportAndDateCreationBetween(Banque banque, boolean b, int state, boolean b1, Date date1, Date date2);

    Iterable<Transaction> findByBanqueAndHasFilesAndTypeDeTransactionIsImportAndDeviseAndDateCreationBetween(Banque banque, boolean b, boolean b1, Devise devise, Date date1, Date date2);

    Iterable<Transaction> findByBanqueAndHasFilesAndTypeDeTransactionIsImportAndDeviseAndTypeDeTransactionAndDateCreationBetween(Banque banque, boolean b, boolean b1, Devise devise, TypeDeTransaction typeDeTransaction, Date date1, Date date2);

    Iterable<Transaction> findByBanqueAndHasFilesAndTypeDeTransactionIsImportAndDeviseAndStatutAndDateCreationBetween(Banque banque, boolean b, boolean b1, Devise devise, int state, Date date1, Date date2);

    Iterable<Transaction> findByBanqueAndHasFilesAndTypeDeTransactionIsImportAndDeviseAndTypeDeTransactionAndStatutAndDateCreationBetween(Banque banque, boolean b, boolean b1, Devise devise, TypeDeTransaction typeDeTransaction, int state, Date date1, Date date2);

    Iterable<Transaction> findByBanqueAndHasFilesAndTypeDeTransactionIsImportAndTypeDeTransactionAndDateCreationBetween(Banque banque, boolean b, boolean b1, TypeDeTransaction typeDeTransaction, Date date1, Date date2);

    Iterable<Transaction> findByBanqueAndHasFilesAndTypeDeTransactionIsImportAndTypeDeTransactionAndStatutAndDateCreationBetween(Banque banque, boolean b, boolean b1, TypeDeTransaction typeDeTransaction, int state, Date date1, Date date2);

    Iterable<Transaction> findByBanqueAndHasFilesAndTypeDeTransactionIsImportAndDateCreationBetween(Banque banque, boolean b, boolean b1, Date date1, Date date2);

    Page<Transaction> findByBanqueAndHasFilesAndStatutOrStatutOrderByDelayDesc(Banque banque, boolean b, int sendbackCustomer, int sendbackMacker, Pageable pageable);

    Page<Transaction> findByBanqueAndClientAndStatutOrStatutOrderByDelayDesc(Banque banque, Client client, int sendbackCustomer, int sendbackMacker, Pageable pageable);

    Transaction findFirstByReference(String reference);

    Iterable<Transaction> findByBanqueAndHasFilesAndDevise(Banque banque, boolean b, Devise devise);

    Iterable<Transaction> findByBanqueAndClientAndHasFilesAndDevise(Banque banque, Client client, boolean b, Devise devise);
    Iterable<Transaction> findByBanqueAndAppUserAndHasFilesAndDevise(Banque banque, AppUser client, boolean b, Devise devise);

    Iterable<Transaction> findByBanqueAndClient(Banque banque, Client client);

    Iterable<Transaction> findByBanque(Banque banque);

    Optional<Transaction> findFirstByBanqueOrderByIdDesc(Banque banque);

    Optional<Transaction> findFirstByBanqueAndReferenceIsNotNullOrderByIdDesc(Banque banque);

    long countByBanqueAndReferenceIsNotNull(Banque banque);

    Iterable<Transaction> findByBanqueAndClientAndHasFilesTrue(Banque banque, Client client);

    Iterable<Transaction> findByBanqueAndHasFilesTrue(Banque banque);

    Iterable<Transaction> findByBanqueAndDateCreationAndHasFilesTrueAndStatutNot(Banque banque, Date date, int rejected);

    Iterable<Transaction> findByBanqueAndClientAndDateCreationAndHasFilesTrueAndStatutNot(Banque banque, Client client, Date date, int rejected);

    Iterable<Transaction> findByBanqueAndHasFilesTrueAndStatutNotAndDateCreationBetween(Banque banque, int rejected, Date dateJourMin, Date dateJourMax);

    Iterable<Transaction> findByBanqueAndClientAndHasFilesTrueAndStatutNotAndDateCreationBetween(Banque banque, Client client, int rejected, Date dateJourMin, Date dateJourMax);

    Iterable<Transaction> findByBanqueAndClientAndHasFilesTrueAndStatutNotAndDateCreationBetweenAndDevise(Banque banque, Client client, int rejected, Date dateJourMin, Date dateJourMax, Devise devise);

    Iterable<Transaction> findByBanqueAndHasFilesTrueAndStatutNotAndDateCreationBetweenAndDevise(Banque banque, int rejected, Date dateJourMin, Date dateJourMax, Devise devise);

    Page<Transaction> findByBanqueAndClientAndStatutOrderByDelayDesc(Banque banque, Client client, int sendbackCustomer, Pageable pageable);

    @Override
    Optional<Transaction> findById(Long aLong);

    @Override
    boolean existsById(Long aLong);

    Page<Transaction> findByBanqueAndAgenceAndStatutOrderByDateCreationDesc(Banque banque, Agence agence, int statut, Pageable pageable);

    Transaction findByReference(String ref);
}
