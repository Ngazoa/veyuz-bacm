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
@Table(name = "veyuz_type_de_fichier")
public class TypeDeFichier {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @JoinTable(name = "veyuz_type_de_fichier_veyuz_type_de_transaction", joinColumns = {
        @JoinColumn(name = "veyuz_type_de_fichier_id", referencedColumnName = "id", nullable = false)}, inverseJoinColumns = {
        @JoinColumn(name = "veyuz_type_de_transaction_id", referencedColumnName = "id", nullable = false)})
    @ManyToMany
    @JsonManagedReference
    private Collection<TypeDeTransaction> typeDeTransactions;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "typeDeFichier")
    @JsonBackReference
    private Collection<Fichier> fichiers;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    private boolean isObligatoireForApurement;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Collection<TypeDeTransaction> getTypeDeTransactions() {
        return typeDeTransactions;
    }

    public void setTypeDeTransactions(Collection<TypeDeTransaction> typeDeTransactions) {
        this.typeDeTransactions = typeDeTransactions;
    }

    public Collection<Fichier> getFichiers() {
        return fichiers;
    }

    public void setFichiers(Collection<Fichier> fichiers) {
        this.fichiers = fichiers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private boolean isObligatoire;

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TypeDeFichier)) {
            return false;
        }
        TypeDeFichier other = (TypeDeFichier) object;
        return (this.id != null || other.id == null) && (this.id == null || this.id.equals(other.id));
    }

    @Override
    public String toString() {
        return "com.example.demo.model.TypeDeFichier[ id=" + id + " ]";
    }
    
}
