package com.akouma.veyuzwebapp.dto;

import com.akouma.veyuzwebapp.model.*;
import lombok.Data;

import java.util.Collection;
import java.util.Date;

@Data
public class TransactionDto {
    private String id;
    private Collection<Fichier> fichiers;
    private Client client;
    private Beneficiaire beneficiaire;
    private Banque banque;
    private Devise devise;
    private Domiciliation domiciliation;
    private TypeDeTransaction typeDeTransaction;
    private String reference;
    private float montant;
    private Date dateCreation;
    private Date dateTransaction;
    private Date lastUpdateAt;
    private int statut;
    private Date delay;
    private boolean hasFiles;

    private boolean isApured;
    private Date dateApurement;
    private Date dateMiseEnDemeure;
    private boolean hasSaction;
    private String motif;

    private String taux;
    private Date dateValeur;

    private AppUser appUser;

    private TypeFinancement typeFinancement;

    private Boolean isRenvoye;

}
