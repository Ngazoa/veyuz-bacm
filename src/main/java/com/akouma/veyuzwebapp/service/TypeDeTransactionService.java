package com.akouma.veyuzwebapp.service;

import com.akouma.veyuzwebapp.model.TypeDeTransaction;
import com.akouma.veyuzwebapp.repository.TypeDeTransactionRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Data
@Service
public class TypeDeTransactionService {

    @Autowired
    private TypeDeTransactionRepository typeDeTransactionRepository;

    public TypeDeTransactionRepository getTypeDeTransactionRepository() {
        return typeDeTransactionRepository;
    }

    public void setTypeDeTransactionRepository(TypeDeTransactionRepository typeDeTransactionRepository) {
        this.typeDeTransactionRepository = typeDeTransactionRepository;
    }

    public Iterable<TypeDeTransaction> getAllTypesTransaction() {
        return typeDeTransactionRepository.findAll();
    }

    public Iterable<TypeDeTransaction> getTypesTransactionByIsImport(boolean isImport) {
        return typeDeTransactionRepository.findByIsImport(isImport);
    }

    public TypeDeTransaction save(TypeDeTransaction typeDeTransaction) {
        return typeDeTransactionRepository.save(typeDeTransaction);
    }
}
