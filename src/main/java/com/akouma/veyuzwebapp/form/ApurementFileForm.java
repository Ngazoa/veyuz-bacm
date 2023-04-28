package com.akouma.veyuzwebapp.form;

import com.akouma.veyuzwebapp.model.ApurementFichierManquant;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * Cette classe represente le formulaire pour uploader un fichier manquant pour un apurement
 */
@Data
public class ApurementFileForm {

    private ApurementFichierManquant fichierManquant;

    private MultipartFile file;

}
