package com.akouma.veyuzwebapp.repository;

import com.akouma.veyuzwebapp.model.TypeDeTransaction;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeDeTransactionRepository extends CrudRepository<TypeDeTransaction, Long> {
    TypeDeTransaction findFirstByCode(String code);

    Iterable<TypeDeTransaction> findByIsImport(boolean isImport);

}
