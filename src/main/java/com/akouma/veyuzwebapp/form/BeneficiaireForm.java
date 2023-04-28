package com.akouma.veyuzwebapp.form;

import com.akouma.veyuzwebapp.model.Banque;
import com.akouma.veyuzwebapp.model.Beneficiaire;
import com.akouma.veyuzwebapp.model.Client;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class BeneficiaireForm {

    private String name;
    private String reference;
    private MultipartFile kycFile;
    private Banque banque;
    private Beneficiaire beneficiaire;

    public BeneficiaireForm() {}

    public BeneficiaireForm(Beneficiaire beneficiaire) {
        this.beneficiaire = beneficiaire;
        this.name = beneficiaire.getName();
        this.reference = beneficiaire.getReference();
    }

}
