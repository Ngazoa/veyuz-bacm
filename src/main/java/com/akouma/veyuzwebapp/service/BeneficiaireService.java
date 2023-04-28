package com.akouma.veyuzwebapp.service;

import com.akouma.veyuzwebapp.form.BeneficiaireForm;
import com.akouma.veyuzwebapp.model.Banque;
import com.akouma.veyuzwebapp.model.Beneficiaire;
import com.akouma.veyuzwebapp.repository.BeneficiaireRepository;
import com.akouma.veyuzwebapp.utils.Upload;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@Data
@Service
public class BeneficiaireService {
    public BeneficiaireRepository getBeneficiaireRepository() {
        return beneficiaireRepository;
    }

    public void setBeneficiaireRepository(BeneficiaireRepository beneficiaireRepository) {
        this.beneficiaireRepository = beneficiaireRepository;
    }
    @Autowired
    FileStorageService fileStorageService;
    @Autowired
    private BeneficiaireRepository beneficiaireRepository;

    @Transactional
    public Beneficiaire saveBeneficiaire(BeneficiaireForm beneficiaireForm, HttpServletRequest request, Banque banque) throws IOException {

        Upload u = new Upload();
        String kyc = u.uploadFile(beneficiaireForm.getKycFile(), fileStorageService, "kyc/beneficires",request);
        Beneficiaire  beneficiaire = new Beneficiaire();
        beneficiaire.setBanque(beneficiaireForm.getBanque());
        beneficiaire.setName(beneficiaireForm.getName());
        beneficiaire.setDateCreation(new Date());
        beneficiaire.setClientId(1L);

        String reference = beneficiaireForm.getReference();
        if (reference == null || reference == "") {
            reference = "BENEF-" + new Date().getTime() + "C" + beneficiaireForm.getBanque().getId();

        }

        beneficiaire.setReference(reference);
        beneficiaire.setKyc(kyc);

        return saveBeneficiaire(beneficiaire);

    }

    public List<Beneficiaire> getBeneficiaire(Banque banque){
      return beneficiaireRepository.findByBanque(banque);
    }

    private Beneficiaire saveBeneficiaire(Beneficiaire beneficiaire) {
        try{
        return beneficiaireRepository.save(beneficiaire);
        } catch (Exception e) {
            e.printStackTrace();
        }
    return null;
    }
}
