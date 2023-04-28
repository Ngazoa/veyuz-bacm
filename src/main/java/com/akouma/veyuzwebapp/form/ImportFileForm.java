package com.akouma.veyuzwebapp.form;

import com.akouma.veyuzwebapp.model.Banque;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ImportFileForm {

    private MultipartFile file;

    private Banque banque;

    private Boolean saveCustomer;

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


    public ImportFileForm() {

    }

    public ImportFileForm(Banque banque) {
        this.banque = banque;
    }
}
