package com.akouma.veyuzwebapp.form;

import com.akouma.veyuzwebapp.model.Fichier;
import com.akouma.veyuzwebapp.model.Transaction;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FichierForm {
    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public Fichier getFichier() {
        return fichier;
    }
    public Fichier getMutliFichier() {
        return fichier;
    }
    public void setFichier(Fichier fichier) {
        this.fichier = fichier;
    }

    private MultipartFile file;
    private Fichier fichier;

    private String fileTitle;
    private Transaction transaction;

    public FichierForm(Fichier fichier) {
        this.fichier = fichier;
        this.fileTitle = fichier.getFileTitle();
        this.transaction = fichier.getTransaction();
    }

    public FichierForm() {

    }
}
