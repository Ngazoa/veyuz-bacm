/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.akouma.veyuzwebapp.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
@Table(name = "veyuz_type_de_transaction")
public class TypeDeTransaction {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToMany(mappedBy = "typeDeTransactions")
    @JsonBackReference
    private Collection<TypeDeFichier> typeDeFichiers;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "typeDeTransaction")
    @JsonBackReference
    private Collection<Transaction> transactions;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "typeDeTransaction")
    @JsonBackReference
    private Collection<Domiciliation> domiciliations;

    @Column(unique = true, length = 150, nullable = false)
    private String name;

    @Column(nullable = false, length = 20, unique = true)
    private String code;

    @Column(nullable = false)
    private boolean isImport;

    private String type;

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TypeDeTransaction)) {
            return false;
        }
        TypeDeTransaction other = (TypeDeTransaction) object;
        return (this.id != null || other.id == null) && (this.id == null || this.id.equals(other.id));
    }

    @Override
    public String toString() {
        return "name = " + name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Collection<TypeDeFichier> getTypeDeFichiers() {
        return typeDeFichiers;
    }

    public void setTypeDeFichiers(Collection<TypeDeFichier> typeDeFichiers) {
        this.typeDeFichiers = typeDeFichiers;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
