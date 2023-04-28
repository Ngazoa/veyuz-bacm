package com.akouma.veyuzwebapp.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.Date;

@DynamicUpdate
@Data
@Entity
@Table(name = "veyuz_apurement_fichier_manquant")
public class ApurementFichierManquant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String fileName;

    private String file;

    private Boolean isValidated;

    private boolean isForApurement;

    @CreationTimestamp
    private Date dateCreation;

    private Date lastUpdate;

    @JoinColumn(name = "apurement_id", referencedColumnName = "id", nullable = false)
    @ManyToOne(optional = false)
    @JsonManagedReference
    private Apurement apurement;
}
