package com.akouma.veyuzwebapp.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.Collection;
import java.util.Date;

@Data
@DynamicUpdate
@Entity
@Table(name = "veyuz_apurement")
public class Apurement {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference_transaction", unique = true, nullable = false, length = 50)
    private String referenceTransaction;

    @JoinColumn(name = "client_id", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    @JsonManagedReference
    private Client client;

    @JoinColumn(name = "banque_id", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    @JsonManagedReference
    private Banque banque;
    private String beneficiaire;
    private Float montant;
    private String marche;
    private String fdc;
    private String devise;
    private String motif;
    private Date dateOuverture;
    private Date dateExpiration;
    private Boolean isApured = false;
    private Boolean isExpired = false;
    private int countEditExpirationDate;

    private Date dateMiseEnDemmeure;

    @CreationTimestamp
    private Date dateCreation;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "apurement")
    @JsonBackReference
    private Collection<ApurementFichierManquant> fichiersManquants;

}
