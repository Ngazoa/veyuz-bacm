package com.akouma.veyuzwebapp.repository;

import com.akouma.veyuzwebapp.model.Apurement;
import com.akouma.veyuzwebapp.model.ApurementFichierManquant;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApurementFichierManquantRepository extends CrudRepository<ApurementFichierManquant, Long> {
    ApurementFichierManquant findFirstByApurementAndFileName(Apurement apurement, String name);
}
