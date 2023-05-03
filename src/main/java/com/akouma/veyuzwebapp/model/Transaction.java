/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.akouma.veyuzwebapp.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

/**
 * @author Sensei237
 */
@Data
@Entity
@DynamicUpdate
@Transactional
@Table(name = "veyuz_transaction")
public class Transaction {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "transaction")
    @JsonBackReference
    private Collection<Fichier> fichiers;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Collection<Fichier> getFichiers() {
        return fichiers;
    }

    public void setFichiers(Collection<Fichier> fichiers) {
        this.fichiers = fichiers;
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

    public Domiciliation getDomiciliation() {
        return domiciliation;
    }

    public void setDomiciliation(Domiciliation domiciliation) {
        this.domiciliation = domiciliation;
    }

    public TypeDeTransaction getTypeDeTransaction() {
        return typeDeTransaction;
    }

    public void setTypeDeTransaction(TypeDeTransaction typeDeTransaction) {
        this.typeDeTransaction = typeDeTransaction;
    }

    public String getReference() {
        if (reference == null) {
            return "..............";
        }
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public float getMontant() {
        return montant;
    }

    public void setMontant(float montant) {
        this.montant = montant;
    }


    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public int getStatut() {
        return statut;
    }

    public void setStatut(int statut) {
        this.statut = statut;
    }

    @JoinColumn(name = "client_id", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    @JsonManagedReference
    private Client client;

    @JoinColumn(name = "beneficiaire_id", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    @JsonManagedReference
    private Beneficiaire beneficiaire;

    @JoinColumn(name = "banque_id", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    @JsonManagedReference
    private Banque banque;

    @JoinColumn(name = "devise_id", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    @JsonManagedReference
    private Devise devise;

    @JoinColumn(name = "domiciliation_id", referencedColumnName = "id")
    @ManyToOne
    @JsonManagedReference
    private Domiciliation domiciliation;

    @JoinColumn(name = "type_de_transaction_id", referencedColumnName = "id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    @Fetch(FetchMode.JOIN)
    @JsonManagedReference
    private TypeDeTransaction typeDeTransaction;

    @Column(unique = true, length = 100)
    private String reference;

    private float montant;

    @CreationTimestamp
    private Date dateCreation;
    @Column
    private Date dateTransaction;
    @Column
    @CreationTimestamp
    private Date lastUpdateAt;
    @Column
    private int statut;
    @Column
    private Date delay;
    @Column
    private boolean hasFiles;
    @Column
    private boolean isApured;
    @Column
    private Date dateApurement;
    @Column
    private Date dateMiseEnDemeure;
    @Column
    private boolean hasSaction;
    @Column
    private String motif;

    private String taux;
    private Date dateValeur;

    private boolean renvoye;


    @ManyToOne
    @JoinColumn(name = "app_user_id", referencedColumnName = "id")
    private AppUser appUser;

    @ManyToOne
    @JsonManagedReference
    @JoinColumn(name = "type_financement_id")
    private TypeFinancement typeFinancement;

    public TypeFinancement getTypeFinancement() {
        return typeFinancement;
    }

    public void setTypeFinancement(TypeFinancement typeFinancement) {
        this.typeFinancement = typeFinancement;
    }

    public AppUser getAppUser() {
        return appUser;
    }

    public void setAppUser(AppUser appUser) {
        this.appUser = appUser;
    }



    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Transaction)) {
            return false;
        }
        Transaction other = (Transaction) object;
        return (this.id != null || other.id == null) && (this.id == null || this.id.equals(other.id));
    }

    @Override
    public String toString() {
        return "com.example.demo.model.Transaction[ id=" + id + " ]";
    }

    public void SetReference(@NotNull String reference) {
        this.reference = reference.toUpperCase();
    }

}
