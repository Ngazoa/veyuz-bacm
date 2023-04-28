package com.akouma.veyuzwebapp.form;

import com.akouma.veyuzwebapp.model.Fichier;
import com.akouma.veyuzwebapp.model.Transaction;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;

@Data
public class FilesTransactionUploadForm {

    private MultipartFile[] files;

    private Transaction trans;

    public MultipartFile[] getFiles() {
        return files;
    }

    public void setFiles(MultipartFile[] files) {
        this.files = files;
    }

    public Transaction getTrans() {
        return trans;
    }

    public void setTrans(Transaction trans) {
        this.trans = trans;
    }

    public Collection<FichierForm> getFichierForms() {
        return fichierForms;
    }

    public void setFichierForms(Collection<FichierForm> fichierForms) {
        this.fichierForms = fichierForms;
    }

    private Collection<FichierForm> fichierForms;

}
