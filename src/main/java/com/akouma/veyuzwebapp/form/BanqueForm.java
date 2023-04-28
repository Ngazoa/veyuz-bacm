package com.akouma.veyuzwebapp.form;

import com.akouma.veyuzwebapp.model.Banque;
import com.akouma.veyuzwebapp.model.Pays;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class BanqueForm {

    private Pays pays;

    private String name;

    private String numeroContribuable;

    private MultipartFile logoFile;

    private String sigle;

    private String telephone;

    private String email;

    private boolean enable;

    private Long banqueId;

    private String logo;
    private String description;

    private Banque banque;

    public BanqueForm(){}

    public BanqueForm(Banque banque) {
        this.banqueId = banque.getId();
        this.enable = banque.isEnable();
        this.email = banque.getEmail();
        this.logo = banque.getLogo();
        this.telephone = banque.getTelephone();
        this.sigle = banque.getSigle();
        this.pays = banque.getPays();
        this.name = banque.getName();
        this.numeroContribuable = banque.getNumeroContribuable();
        this.banque = banque;
        this.description = banque.getDescription();

    }
}
