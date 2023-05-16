package com.akouma.veyuzwebapp.repository;

import com.akouma.veyuzwebapp.model.AppUser;
import com.akouma.veyuzwebapp.model.Banque;
import com.akouma.veyuzwebapp.model.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientRepository extends CrudRepository<Client, Long> {

    List<Client> findByBanques_BanqueOrderByUser_NomAsc(Banque banque, Sort sort);

    List<Client> findDistinctByUser_Client_TelephoneAndBanques_Clients_DenominationAndUser_Client_UserAllIgnoreCaseOrderByBanques_Clients_DenominationAsc(String telephone, String denomination, AppUser user, Sort sort);
    Page<Client> findDistinctReferenceByBanques(Banque banque, Pageable pageable);

    Client findFirstByReference(String reference);

    Client  findFirstByOrderByIdDesc();

    Client findFirstByCodeVeyuz(String codeVeyuz);

    List<Client> findDistinctReferenceByBanques(Banque banque);
    Client findByTelephone(String name);
    Iterable<Client> findByDenominationLike(String name);

    List<Client> findAll(Specification<Client> spec);

    List<Client> findByUser_BanqueLikeOrTelephoneContainingOrDenominationContainingOrReferenceContaining(
            Banque banque, String telephone, String denomination, String reference);



}
