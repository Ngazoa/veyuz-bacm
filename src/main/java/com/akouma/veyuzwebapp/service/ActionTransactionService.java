package com.akouma.veyuzwebapp.service;

import com.akouma.veyuzwebapp.model.ActionTransaction;
import com.akouma.veyuzwebapp.model.Transaction;
import com.akouma.veyuzwebapp.repository.ActionTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActionTransactionService {

    @Autowired
    private ActionTransactionRepository actionTransactionRepository;

    public ActionTransaction getLastAction(Transaction transaction) {
        return actionTransactionRepository.findTopByTransactionOrderByDateCreationDesc(transaction);
    }

    public ActionTransactionRepository getActionTransactionRepository() {
        return actionTransactionRepository;
    }

    public void setActionTransactionRepository(ActionTransactionRepository actionTransactionRepository) {
        this.actionTransactionRepository = actionTransactionRepository;
    }

    public ActionTransaction saveActionTransaction(ActionTransaction actionTransaction) {
        return actionTransactionRepository.save(actionTransaction);
    }

    public Iterable<ActionTransaction> getActionsTransaction(Transaction transaction) {
        return actionTransactionRepository.findByTransactionOrderByDateCreationDesc(transaction);
    }
}
