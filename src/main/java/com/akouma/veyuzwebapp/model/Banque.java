/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.akouma.veyuzwebapp.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Sensei237
 */
@Data
@DynamicUpdate
@Entity
@Table(name = "veyuz_banque")
public class Banque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "banque_id")
    private Banque banque;

    public Collection<Client> getClients() {
        return clients;
    }

    public void setClients(Collection<Client> clients) {
        this.clients = clients;
    }

    public Collection<Lettre> getLettres() {
        return lettres;
    }

    public void setLettres(Collection<Lettre> lettres) {
        this.lettres = lettres;
    }


    public Collection<AppUser> getUsers() {
        return users;
    }

    public void setUsers(Collection<AppUser> users) {
        this.users = users;
    }

    public Collection<Domiciliation> getDomiciliations() {
        return domiciliations;
    }

    public void setDomiciliations(Collection<Domiciliation> domiciliations) {
        this.domiciliations = domiciliations;
    }

    public Pays getPays() {
        return pays;
    }

    public void setPays(Pays pays) {
        this.pays = pays;
    }

    public Collection<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(Collection<Transaction> transactions) {
        this.transactions = transactions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumeroContribuable() {
        return numeroContribuable;
    }

    public void setNumeroContribuable(String numeroContribuable) {
        this.numeroContribuable = numeroContribuable;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getSigle() {
        return sigle;
    }

    public void setSigle(String sigle) {
        this.sigle = sigle;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }


    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    @ManyToMany(mappedBy = "banques")
    @JsonIgnore
    private Collection<Client> clients;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "banque")
    @JsonBackReference
    private Collection<Lettre> lettres;

    @OneToMany(mappedBy = "banque")
    @JsonBackReference
    private Collection<AppUser> users;

    @OneToMany(mappedBy = "banque")
    @JsonBackReference
    private Collection<Domiciliation> domiciliations;


    @JoinColumn(name = "pays_id", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    @JsonManagedReference
    private Pays pays;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "banque")
    @JsonBackReference
    private Collection<Transaction> transactions;

    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String numeroContribuable;

    private String logo;

    @Column(nullable = false, unique = true, length = 50)
    private String sigle;

    @Column(nullable = false, unique = true, length = 50, name = "contact")
    private String telephone;

    @Column(nullable = false, unique = true, length = 50)
    private String email;

    private boolean enable;

    @Column(name = "description", length = 10000, nullable = true)
    private String description;
    @Fetch(FetchMode.SUBSELECT)
    @JsonIgnore
    @OneToMany(mappedBy = "banque", orphanRemoval = true,fetch = FetchType.EAGER)
    private List<Beneficiaire> beneficiaires = new ArrayList<>();

    public List<Beneficiaire> getBeneficiaires() {
        return beneficiaires;
    }

    public void setBeneficiaires(List<Beneficiaire> beneficiaires) {
        this.beneficiaires = beneficiaires;
    }

    public Banque getBanque() {
        return banque;
    }

    public void setBanque(Banque banque) {
        this.banque = banque;
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
        if (!(object instanceof Banque)) {
            return false;
        }
        Banque other = (Banque) object;
        return (this.id != null || other.id == null) && (this.id == null || this.id.equals(other.id));
    }

    @Override
    public String toString() {
        return "com.example.demo.model.Banque[ id=" + id + " ]";
    }
    
}
