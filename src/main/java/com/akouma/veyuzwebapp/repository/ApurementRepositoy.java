package com.akouma.veyuzwebapp.repository;

import com.akouma.veyuzwebapp.model.*;
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

    Page<Apurement> findByBanqueAndIsApuredAndStatusOrderByDateOuvertureDesc(Banque banque, boolean isApured, int status, Pageable pageable);

    Iterable<Apurement> findByBanqueAndClientAndIsApuredOrderByIsApuredAsc(Banque banque, Client client, boolean isApured);

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

    Page<Apurement> findByBanqueAndClientAndIsApuredAndStatusOrderByDateOuvertureDesc(Banque banque, Client client, boolean isApured, int status, Pageable pageable);

    Page<Apurement> findByBanqueAndIsApuredAndStatusAndAppUserOrderByDateOuvertureDesc(Banque banque, boolean isApured, int status, AppUser appUser, Pageable pageable);

    Page<Apurement> findByBanqueAndIsApuredAndStatusGreaterThanEqualAndAppUserOrderByDateOuvertureDesc(Banque banque, boolean isApured, int status, AppUser appUser, Pageable pageable);

    Page<Apurement> findByBanqueAndIsApuredAndStatusGreaterThanEqualOrderByDateOuvertureDesc(Banque banque, boolean isApured, int status, Pageable pageable);

    Page<Apurement> findByBanqueAndStatusAndAppUser(Banque banque, int apurementRejeter, AppUser loggedUser, Pageable pageable);

    Page<Apurement> findByBanqueAndStatus(Banque banque, int apurementRejeter, Pageable pageable);

    Page<Apurement> findByBanqueAndIsApuredAndStatusGreaterThanEqualAndClientAgenceOrderByDateOuvertureDesc(Banque banque, boolean isApured, int status, Agence agence, Pageable of);

    Page<Apurement> findByBanqueAndIsApuredAndClientAgenceOrderByDateOuvertureDesc(Banque banque, boolean isApured, Agence agence, Pageable of);
}
