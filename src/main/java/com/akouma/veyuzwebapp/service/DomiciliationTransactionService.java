package com.akouma.veyuzwebapp.service;

import com.akouma.veyuzwebapp.model.Domiciliation;
import com.akouma.veyuzwebapp.model.DomiciliationTransaction;
import com.akouma.veyuzwebapp.repository.DomiciliationTransactionRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Data
@Service
public class DomiciliationTransactionService {
    @Autowired
    private DomiciliationTransactionRepository domiciliationTransactionRepository;

    public DomiciliationTransaction save(DomiciliationTransaction domiciliationTransaction) {
        DomiciliationTransaction dt = domiciliationTransactionRepository.save(domiciliationTransaction);

        return dt;
    }

    public void delete(DomiciliationTransaction dt) {
        domiciliationTransactionRepository.delete(dt);
    }

    public List<DomiciliationTransaction> findByDomiciliation(Domiciliation domiciliation) {
        return domiciliationTransactionRepository.findByDomiciliation(domiciliation);
    }
}
