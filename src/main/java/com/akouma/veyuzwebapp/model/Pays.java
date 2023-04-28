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
@Table(name = "veyuz_pays")
public class Pays {
    
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Collection<Banque> getBanques() {
        return banques;
    }

    public void setBanques(Collection<Banque> banques) {
        this.banques = banques;
    }

    @Basic(optional = false)
    @Column(name = "name", nullable = false, length = 100, unique = true)
    private String name;
    
    @Column(name = "code", length = 20, unique = true)
    private String code;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "pays")
    @JsonBackReference
    private Collection<Banque> banques;

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Pays)) {
            return false;
        }
        Pays other = (Pays) object;
        return (this.id != null || other.id == null) && (this.id == null || this.id.equals(other.id));
    }

    @Override
    public String toString() {
        return "com.example.demo.model.Pays[ id=" + id + " ]";
    }
    
}
