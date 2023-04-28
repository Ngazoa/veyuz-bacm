package com.akouma.veyuzwebapp.service;

import com.akouma.veyuzwebapp.model.Apurement;
import com.akouma.veyuzwebapp.model.ApurementFichierManquant;
import com.akouma.veyuzwebapp.repository.ApurementFichierManquantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApurementFichierManquantService {

    @Autowired
    private ApurementFichierManquantRepository afmr;

    public ApurementFichierManquant saveFichierManquant(ApurementFichierManquant fichierManquant) {
        return afmr.save(fichierManquant);
    }

    public ApurementFichierManquant getFichierManquant(Apurement apurement, String name) {
        return afmr.findFirstByApurementAndFileName(apurement, name);
    }
}
