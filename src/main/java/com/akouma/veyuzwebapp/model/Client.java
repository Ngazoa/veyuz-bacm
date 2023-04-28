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

/**
 *
 * @author Sensei237
 */
@Data
@DynamicUpdate
@Entity
@Table(name = "veyuz_client")
public class Client {

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

    public Collection<Banque> getBanques() {
        return banques;
    }

    public void setBanques(Collection<Banque> banques) {
        this.banques = banques;
    }

    public Collection<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(Collection<Transaction> transactions) {
        this.transactions = transactions;
    }


    public Collection<Domiciliation> getDomiciliations() {
        return domiciliations;
    }

    public void setDomiciliations(Collection<Domiciliation> domiciliations) {
        this.domiciliations = domiciliations;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public String getKyc() {
        return kyc;
    }

    public void setKyc(String kyc) {
        this.kyc = kyc;
    }

    public String getNumeroContribuable() {
        return numeroContribuable;
    }

    public void setNumeroContribuable(String numeroContribuable) {
        this.numeroContribuable = numeroContribuable;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getDenomination() {
        return denomination;
    }

    public void setDenomination(String denomination) {
        this.denomination = denomination;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    @JoinTable(name = "veyuz_client_veyuz_banque", joinColumns = {
        @JoinColumn(name = "veyuz_client_id", referencedColumnName = "id", nullable = false)}, inverseJoinColumns = {
        @JoinColumn(name = "veyuz_banque_id", referencedColumnName = "id", nullable = false)})
    @ManyToMany
    @JsonBackReference
    private Collection<Banque> banques;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "beneficiaire")
    @JsonBackReference
    private Collection<Transaction> transactions;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "beneficiaire")

    @JsonBackReference
    private Collection<Apurement> apurements;

    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "client")
    @JsonBackReference
    private Collection<Domiciliation> domiciliations;
    
    @OneToOne(mappedBy = "client", cascade = CascadeType.ALL)
    @JsonManagedReference
    private AppUser user;

    private String kyc;

    @Column(nullable = false, unique = true, length = 100)
    private String numeroContribuable;

    @Column(nullable = false, unique = true, length = 100)
    private String telephone;

    private String denomination;

    @Column(nullable = false, unique = true, length = 100)
    private String reference;

    private String typeClient;

    @Column(nullable = false, unique = true, length = 100)
    private String codeVeyuz;


    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Client)) {
            return false;
        }
        Client other = (Client) object;
        return (this.id != null || other.id == null) && (this.id == null || this.id.equals(other.id));
    }

    @Override
    public String toString() {
        return "com.example.demo.model.Client[ id=" + id + " ]";
    }
    
}
