package com.akouma.veyuzwebapp.repository;

import com.akouma.veyuzwebapp.model.Apurement;
import com.akouma.veyuzwebapp.model.Banque;
import com.akouma.veyuzwebapp.model.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface ApurementRepositoy extends CrudRepository<Apurement, Long> {
    Page<Apurement> findByBanque(Banque banque, Pageable pageable);

    Apurement findFistByReferenceTransaction(String referenceTransaction);

    Page<Apurement> findByBanqueAndClient(Banque banque, Client client, Pageable pageable);

    Page<Apurement> findByBanqueAndIsApured(Banque banque, boolean isApured, Pageable pageable);

    Page<Apurement> findByBanqueAndClientAndIsApuredOrderByIsApuredAsc(Banque banque, Client client, boolean isApured, Pageable pageable);
    Iterable<Apurement> findByBanqueAndClientAndIsApuredOrderByIsApuredAsc(Banque banque, Client client, boolean isApured);

    Page<Apurement> findByBanqueAndIsApuredAndDateExpirationIsNotNullOrderByIsApuredAsc(Banque banque, boolean isApured, Pageable pageable);

    Iterable<Apurement> findByIsApuredAndIsExpired(boolean b, boolean b1);

    Iterable<Apurement> findByIsApuredAndIsExpiredAndDateExpirationNot(boolean isApured, boolean isExpired, Date o);

    Iterable<Apurement> findByIsApuredAndIsExpiredAndDateExpirationIsNotNull(boolean isApured, boolean isExpired);

    Iterable<Apurement> findByIsApuredFalseAndIsExpiredFalseAndDateExpirationIsNotNull();

    Page<Apurement> findByIsApuredFalseAndIsExpiredTrueAndBanque(Banque banque, Pageable pageable);

    Page<Apurement> findByIsApuredFalseAndIsExpiredTrueAndBanqueAndClient(Banque banque, Client client, Pageable pageable);

    Iterable<Apurement> findByBanqueAndIsApuredFalseAndIsExpiredFalseAndDateExpirationIsNotNull(Banque banque);

    Iterable<? extends Apurement> findByBanqueAndClientAndIsApuredFalseAndIsExpiredFalseAndDateExpirationIsNotNull(Banque banque, Client client);

    Iterable<Apurement> findByIsApuredFalseAndIsExpiredFalse();

    Iterable<Apurement> findByBanqueAndIsApuredFalseAndIsExpiredFalse(Banque banque);

    Page<Apurement> findByBanqueAndIsApuredOrderByIsApuredAsc(Banque banque, boolean isApured, Pageable pageable);
    Iterable<Apurement> findByBanqueAndIsApuredOrderByIsApuredAsc(Banque banque, boolean isApured);

    Iterable<? extends Apurement> findByBanqueAndClientAndIsApuredFalseAndIsExpiredFalse(Banque banque, Client client);

    Iterable<Apurement> findByBanqueAndAndClientAndReferenceTransactionLikeOrderByDateOuvertureDesc(Banque banque, Client client, String reference);

    Iterable<Apurement> findByBanqueAndReferenceTransactionLikeOrderByDateOuvertureDesc(Banque banque, String reference);

    Iterable<Apurement> findByBanqueAndAndClientAndReferenceTransactionContainingOrderByDateOuvertureDesc(Banque banque, Client client, String reference);

    Iterable<Apurement> findByBanqueAndReferenceTransactionContainingOrderByDateOuvertureDesc(Banque banque, String reference);
}
