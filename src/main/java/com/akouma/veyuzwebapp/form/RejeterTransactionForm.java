package com.akouma.veyuzwebapp.form;

import com.akouma.veyuzwebapp.model.Transaction;
import lombok.Data;

@Data
public class RejeterTransactionForm {

    private String motif;
    private Transaction transaction;

    public RejeterTransactionForm(){}

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public RejeterTransactionForm(Transaction transaction) {
        this.transaction = transaction;
    }

}
