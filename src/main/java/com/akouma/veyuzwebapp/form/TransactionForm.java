package com.akouma.veyuzwebapp.form;

import com.akouma.veyuzwebapp.model.*;
import com.akouma.veyuzwebapp.service.DeviseService;
import com.akouma.veyuzwebapp.service.TypeDeTransactionService;
import com.akouma.veyuzwebapp.utils.StatusTransaction;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Transient;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
public class TransactionForm {

    @Autowired
    private DeviseService deviseService;
    @Autowired
    private TypeDeTransactionService typeDeTransactionService;

    public DeviseService getDeviseService() {
        return deviseService;
    }

    public void setDeviseService(DeviseService deviseService) {
        this.deviseService = deviseService;
    }

    public TypeDeTransactionService getTypeDeTransactionService() {
        return typeDeTransactionService;
    }

    public void setTypeDeTransactionService(TypeDeTransactionService typeDeTransactionService) {
        this.typeDeTransactionService = typeDeTransactionService;
    }

    public Banque getBanque() {
        return banque;
    }

    public void setBanque(Banque banque) {
        this.banque = banque;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Beneficiaire getBeneficiaire() {
        return beneficiaire;
    }

    public void setBeneficiaire(Beneficiaire beneficiaire) {
        this.beneficiaire = beneficiaire;
    }

    public String getBeneficiaireName() {
        String beneficiaireName = null;
        if (beneficiaire != null) {
            beneficiaireName = beneficiaire.getName();
        }
        return beneficiaireName;
    }

    public TypeDeTransaction getTypeDeTransaction() {
        return typeDeTransaction;
    }

    public void setTypeDeTransaction(TypeDeTransaction typeDeTransaction) {
        this.typeDeTransaction = typeDeTransaction;
    }

    public String getTypeDeTransactionName() {
        String typeDeTransactionName = null;
        if (typeDeTransaction != null) {
            typeDeTransactionName = typeDeTransaction.getName();
        }
        return typeDeTransactionName;
    }

    public Domiciliation getDomiciliation() {
        return domiciliation;
    }

    public void setDomiciliation(Domiciliation domiciliation) {
        this.domiciliation = domiciliation;
    }

    public String getMontantMax() {
        String montantMax = null;
        if (domiciliation != null) {
            montantMax = "Montant maximun autorisé " + (domiciliation.getMontantRestant() * (1 + 10 / 100));
        }
        return montantMax;
    }

    public Devise getDevise() {
        return devise;
    }

    public void setDevise(Devise devise) {
        this.devise = devise;
    }

    public String getDeviseName() {
        String deviseName = null;
        if (devise != null) {
            deviseName = devise.getName() + "(" + devise.getCode() + ")" ;
        }
        return deviseName;
    }

    public float getMontant() {
        montant = TransformTextMontantToFloat(montantTexte);
        return montant;
    }

    public void setMontant(String montant) {
        this.montant = TransformTextMontantToFloat(montant);

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    private Banque banque;
    private Client client;
    private Beneficiaire beneficiaire;
    private TypeDeTransaction typeDeTransaction;
    private Domiciliation domiciliation;
    private Devise devise;
    //    private List<TypeDeTransaction> typeDeTransactions;
    private float montant;
    private String montantTexte;
    //    private List<Devise> devises;
    private String type;
    //    private String typeDomiciliation;
//    private String typeNormal;
    private Transaction transaction;

    private String reference;

    private String dateTransactionStr;
    private String motif;

    @Transient
    private Set<DomiciliationTransaction> domiciliationTransactions;

    Boolean useManyDomiciliations = false;


    public TransactionForm() {
    }

    public TransactionForm(Banque banque, Client client, String type) {
        this.banque = banque;
        this.client = client;
        this.type = type;
    }

    public TransactionForm(Transaction transaction, String type) {
        banque = transaction.getBanque();
        beneficiaire = transaction.getBeneficiaire();
        devise = transaction.getDevise();
        montantTexte = "" + transaction.getMontant();
        domiciliation = transaction.getDomiciliation();
        client = transaction.getClient();
        typeDeTransaction = transaction.getTypeDeTransaction();
        this.transaction = transaction;
        useManyDomiciliations = transaction.getUseManyDomiciliations();

        reference = transaction.getReference();
        Date date = transaction.getDateTransaction();
        if (date == null) {
            date = transaction.getDateCreation();
        }
        dateTransactionStr = new SimpleDateFormat("dd-MM-yyyy").format(date);
        this.type = type;
        motif = transaction.getMotif();
    }


    private float TransformTextMontantToFloat(String montant) {
        if (montant != null) {
            montant = montantTexte.trim();
            return Float.parseFloat(montant.replaceAll(",", ""));
        }
        return 0;
    }

}
