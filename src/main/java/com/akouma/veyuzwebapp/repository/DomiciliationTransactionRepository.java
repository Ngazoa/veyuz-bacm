package com.akouma.veyuzwebapp.repository;

import com.akouma.veyuzwebapp.model.Domiciliation;
import com.akouma.veyuzwebapp.model.DomiciliationTransaction;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DomiciliationTransactionRepository  extends CrudRepository<DomiciliationTransaction, Long> {
    List<DomiciliationTransaction> findByDomiciliation(Domiciliation domiciliation);
}
