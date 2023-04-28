package com.akouma.veyuzwebapp.repository;

import com.akouma.veyuzwebapp.model.ActionTransaction;
import com.akouma.veyuzwebapp.model.Transaction;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActionTransactionRepository extends CrudRepository<ActionTransaction, Long> {

    ActionTransaction findTopByTransactionOrderByDateCreationDesc(Transaction transaction);

    Iterable<ActionTransaction> findByTransactionOrderByDateCreationDesc(Transaction transaction);

}
