package com.akouma.veyuzwebapp.repository;

import com.akouma.veyuzwebapp.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface DomiciliationRepository extends CrudRepository<Domiciliation, Long> {

    Page<Domiciliation> findByBanqueOrderByClientAsc(Banque banque, Pageable pageable);

    Page<Domiciliation> findByBanqueAndClientOrderByDateCreationDesc(Banque banque, Client client, Pageable pageable);
    Iterable<Domiciliation> findByBanqueAndClientOrderByDateCreationDesc(Banque banque, Client client);

    Domiciliation findFirstByReference(String reference);

    Page<Domiciliation> findByBanqueOrderByDateCreationAsc(Banque banque, Pageable pageable);

    Iterable<Domiciliation> findByBanque(Banque banque);

    List<Domiciliation> findByClientAndBeneficiaireAndDeviseAndTypeDeTransaction(Client client, Beneficiaire beneficiaire, Devise devise, TypeDeTransaction typeDeTransaction);

    List<Domiciliation> findByClientAndDevise(Client client, Devise devise);
}
