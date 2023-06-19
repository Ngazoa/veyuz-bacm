/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.akouma.veyuzwebapp.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.Collection;
import java.util.Date;

/**
 *
 * @author Sensei237
 */
@Data
@DynamicUpdate
@Entity
@Table(name = "veyuz_domiciliation")
public class Domiciliation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Collection<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(Collection<Transaction> transactions) {
        this.transactions = transactions;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Banque getBanque() {
        return banque;
    }

    public void setBanque(Banque banque) {
        this.banque = banque;
    }

    public Devise getDevise() {
        return devise;
    }

    public void setDevise(Devise devise) {
        this.devise = devise;
    }

    public TypeDeTransaction getTypeDeTransaction() {
        return typeDeTransaction;
    }

    public void setTypeDeTransaction(TypeDeTransaction typeDeTransaction) {
        this.typeDeTransaction = typeDeTransaction;
    }

    public Beneficiaire getBeneficiaire() {
        return beneficiaire;
    }

    public void setBeneficiaire(Beneficiaire beneficiaire) {
        this.beneficiaire = beneficiaire;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public float getMontant() {
        return montant;
    }

    public void setMontant(float montant) {
        this.montant = montant;
    }

    public float getMontantRestant() {
        return montantRestant;
    }

    public void setMontantRestant(float montantRestant) {
        this.montantRestant = montantRestant;
    }

    public Date getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(Date dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public boolean isImportation() {
        return isImportation;
    }

    public void setImportation(boolean importation) {
        isImportation = importation;
    }

    @OneToMany(mappedBy = "domiciliation")
    @JsonBackReference
    private Collection<Transaction> transactions;
    
    @JoinColumn(name = "client_id", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    @JsonManagedReference
    private Client client;

    @JoinColumn(name = "banque_id", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    @JsonManagedReference
    private Banque banque;
    
    @JoinColumn(name = "devise_id", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    @JsonManagedReference
    private Devise devise;
    
    @JoinColumn(name = "type_de_transaction_id", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    @JsonManagedReference
    private TypeDeTransaction typeDeTransaction;

    @JoinColumn(name = "beneficiaire_id", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    @JsonManagedReference
    private Beneficiaire beneficiaire;

    @Column(nullable = false, unique = true, length = 100)
    private String reference;

    @Column(nullable = false)
    private Date dateCreation;

    private float montant;

    private float montantRestant;

    @Column(nullable = false)
    private Date dateExpiration;

    private boolean isImportation; // Permet de connaitre le type de domiciliation

    private  String referenceInterne;


    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Domiciliation)) {
            return false;
        }
        Domiciliation other = (Domiciliation) object;
        return (this.id != null || other.id == null) && (this.id == null || this.id.equals(other.id));
    }

    @Override
    public String toString() {
        return beneficiaire.getName() + " " + montant + " " + devise.getName() + " - " + reference;
    }
    
}
