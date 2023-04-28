package com.akouma.veyuzwebapp.model;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;


@Transactional
public class AppUserDetails  implements UserDetails {
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Collection<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(Collection<Notification> notifications) {
        this.notifications = notifications;
    }

    public Collection<UserRole> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(Collection<UserRole> userRoles) {
        this.userRoles = userRoles;
    }

    public Banque getBanque() {
        return banque;
    }

    public void setBanque(Banque banque) {
        this.banque = banque;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public boolean isEnable() {
        return isEnable;
    }

    public void setEnable(boolean enable) {
        isEnable = enable;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public Date getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(Date dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getLieuNaissance() {
        return lieuNaissance;
    }

    public void setLieuNaissance(String lieuNaissance) {
        this.lieuNaissance = lieuNaissance;
    }

    public String getTelephone1() {
        return telephone1;
    }

    public void setTelephone1(String telephone1) {
        this.telephone1 = telephone1;
    }

    public String getTelephone2() {
        return telephone2;
    }

    public void setTelephone2(String telephone2) {
        this.telephone2 = telephone2;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setAuthorities(List<GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    private Long id;

    private Collection<Notification> notifications;

    private Collection<UserRole> userRoles;

    private Banque banque;

    private Client client;

    private String email;

    private String password;

    private String avatar;

    private boolean isEnable;

    private String nom;

    private String prenom;

    private Date dateNaissance;

    private String lieuNaissance;

    private String telephone1;

    private String telephone2;

    private String adresse;

    private String gender;

    private String userName;

    public AppUser getAppUser() {
        return appUser;
    }

    public void setAppUser(AppUser appUser) {
        this.appUser = appUser;
    }

    private AppUser appUser;

    private List<GrantedAuthority> authorities;

    public AppUserDetails() {}

    public AppUserDetails(AppUser appUser) {
        id = appUser.getId();

        this.appUser = appUser;

        notifications = appUser.getNotifications();

        userRoles = appUser.getUserRoles();

        banque = appUser.getBanque();

        client = appUser.getClient();

        email = appUser.getEmail();

        password = appUser.getPassword();

        avatar = appUser.getAvatar();

        isEnable = appUser.isEnable();

        nom = appUser.getNom();

        prenom = appUser.getPrenom();

        dateNaissance = appUser.getDateNaissance();

        lieuNaissance = appUser.getLieuNaissance();

        telephone1 = appUser.getTelephone1();

        telephone2 = appUser.getTelephone2();

        adresse = appUser.getAdresse();

        gender = appUser.getGender();

        userName = appUser.getUserName();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return userName;
    }


    public Banque getUserBanque() {
        return banque;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isEnable;
    }

    public void addAuthorities(List<GrantedAuthority> authorities) {
        this.authorities = authorities;
    }


}
