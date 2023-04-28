package com.akouma.veyuzwebapp.form;

import com.akouma.veyuzwebapp.model.AppUser;
import com.akouma.veyuzwebapp.model.Banque;
import com.akouma.veyuzwebapp.model.Client;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ClientForm {
    private Banque banque;
    private String denomination;
    private String numeroContribuable;
    private String telephone;
    private MultipartFile kycFile;
    private Client client;
    private String dateNaissanceStr;
    private String confirmPassword;
    private String codeVeyuz;
    private String typeClient;

    private MultipartFile avatar;

    public ClientForm(Banque banque, String denomination, String numeroContribuable, String telephone, MultipartFile kycFile, AppUser appUser) {
        this.banque = banque;
        this.denomination = denomination;
        this.numeroContribuable = numeroContribuable;
        this.telephone = telephone;
        this.kycFile = kycFile;
        this.appUser = appUser;
    }

    public ClientForm(Client client) {
        this.client = client;
        this.denomination = client.getDenomination();
        this.codeVeyuz = client.getCodeVeyuz();
        this.typeClient = client.getTypeClient();
        this.telephone = client.getTelephone();
        this.numeroContribuable = client.getNumeroContribuable();
        this.appUser = client.getUser();

    }

    private AppUser appUser;

    public  ClientForm(){}

    public ClientForm(Banque banque) {
        this.banque = banque;
    }

}
