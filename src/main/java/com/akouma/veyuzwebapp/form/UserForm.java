package com.akouma.veyuzwebapp.form;

import com.akouma.veyuzwebapp.model.AppUser;
import com.akouma.veyuzwebapp.model.Banque;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.Column;
import java.util.Date;

@Data
public class UserForm {

    private  String userName;

    private String email;

    private String password;

    private String confirmPassword;

    private String avatar;

    private MultipartFile avatarFile;

    private String nom;

    private String prenom;

    private Date dateNaissance;

    private String dateNaissanceStr;

    private String lieuNaissance;

    private String telephone1;

    private String telephone2;

    private String adresse;

    private String gender;

    private Long userId;

    private Banque banque;

    public UserForm() {
        this.email = null;
        this.password = null;
        this.avatar = null;
        this.nom = null;
        this.prenom = null;
        this.dateNaissance = null;
        this.lieuNaissance = null;
        this.telephone1 = null;
        this.telephone2 = null;
        this.adresse = null;
        this.gender = null;
        this.confirmPassword = null;
        this.userId = null;
        this.userName = null;
        this.banque = null;
    }

    public UserForm(String email, String password, String confirmPassword, String avatar, String nom, String prenom,
                    Date dateNaissance, String lieuNaissance, String telephone1, String telephone2,
                    String adresse, String gender, String userName, Banque banque, Long userId) {

        this.email = email;
        this.password = password;
        this.avatar = avatar;
        this.nom = nom;
        this.prenom = prenom;
        this.dateNaissance = dateNaissance;
        this.lieuNaissance = lieuNaissance;
        this.telephone1 = telephone1;
        this.telephone2 = telephone2;
        this.adresse = adresse;
        this.gender = gender;
        this.confirmPassword = confirmPassword;
        this.userId = userId;
        this.userName = userName;
        this.banque = banque;

    }

    public UserForm(AppUser appUser) {
        this.email = appUser.getEmail();
        this.password = appUser.getPassword();
        this.avatar = appUser.getAvatar();
        this.nom = appUser.getNom();
        this.prenom = appUser.getPrenom();
        this.dateNaissance = appUser.getDateNaissance();
        this.lieuNaissance = appUser.getLieuNaissance();
        this.telephone1 = appUser.getTelephone1();
        this.telephone2 = appUser.getTelephone2();
        this.adresse = appUser.getAdresse();
        this.confirmPassword = null;
        this.gender = appUser.getGender();
        this.userId = appUser.getId();
        this.userName = appUser.getUserName();
    }
}
