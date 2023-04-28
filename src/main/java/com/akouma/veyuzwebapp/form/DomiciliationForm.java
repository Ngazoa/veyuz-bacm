package com.akouma.veyuzwebapp.form;

import com.akouma.veyuzwebapp.model.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
public class DomiciliationForm {

    private Client client;

    private Banque banque;

    private Devise devise;

    private TypeDeTransaction typeDeTransaction;

    private Beneficiaire beneficiaire;

    private String reference;

    private Date dateCreation;

    private String dateCreationStr;

    private float montant;

    private float montantRestant;

    private Date dateExpiration;

    private boolean isImportation;

    private Domiciliation domiciliation;
    private String montantTexte;

    public float getMontant() {
        montant = TransformTextMontantToFloat(montantTexte);
        return montant;
    }

    public void setMontant(String montant) {
        this.montant = TransformTextMontantToFloat(montant);
    }
    public DomiciliationForm() {}

    public DomiciliationForm(Banque banque) {
        this.banque = banque;
    }

    public DomiciliationForm(Banque banque, Client client) {
        this.banque = banque;
        this.client = client;
    }

    public  DomiciliationForm(Domiciliation domiciliation) {
        this.client = domiciliation.getClient();
        this.banque = domiciliation.getBanque();
        this.devise = domiciliation.getDevise();
        this.typeDeTransaction = domiciliation.getTypeDeTransaction();
        this.beneficiaire = domiciliation.getBeneficiaire();
        this.reference = domiciliation.getReference();
        this.dateCreation = domiciliation.getDateCreation();
        this.montant = domiciliation.getMontant();
        this.montantRestant = domiciliation.getMontantRestant();
        this.dateExpiration = domiciliation.getDateExpiration();
        this.isImportation = domiciliation.isImportation();
        this.domiciliation = domiciliation;
        this.dateCreationStr = new SimpleDateFormat("dd-MM-yy").format(dateCreation);
    }
    private float TransformTextMontantToFloat(String montant) {
        if (montant != null) {
            montant = montantTexte.trim();
            return Float.parseFloat(montant.replaceAll(",", ""));
        }
        return 0;
    }
}
