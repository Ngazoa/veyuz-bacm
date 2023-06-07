package com.akouma.veyuzwebapp.repository;

import com.akouma.veyuzwebapp.model.Banque;
import com.akouma.veyuzwebapp.model.Beneficiaire;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BeneficiaireRepository extends CrudRepository<Beneficiaire, Long> {
    Beneficiaire findFirstByReference(String reference);

    List<Beneficiaire> findByBanque(Banque banque);

    Beneficiaire findByName(String codeBenef);
}
