package com.akouma.veyuzwebapp.dto;

import com.akouma.veyuzwebapp.model.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientDto {

    private String id;
    private Collection<Banque> banques;
    private Collection<Transaction> transactions;
    private Collection<Apurement> apurements;
    private Collection<Domiciliation> domiciliations;
    private AppUser user;
    private Agence agence;
    private String kyc;
    private String numeroContribuable;
    private String telephone;
    private String denomination;
    private String reference;
    private String typeClient;
    private String codeVeyuz;
    private String niu;

}
