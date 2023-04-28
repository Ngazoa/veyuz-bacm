package com.akouma.veyuzwebapp.form;

import com.akouma.veyuzwebapp.model.Banque;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ImportFile {

    private MultipartFile file;

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public Banque getBanque() {
        return banque;
    }

    public void setBanque(Banque banque) {
        this.banque = banque;
    }

    private Banque banque;

    public ImportFile() {

    }

    public ImportFile(Banque banque) {
        this.banque = banque;
    }
}
