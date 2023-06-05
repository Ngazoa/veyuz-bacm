/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.akouma.veyuzwebapp.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author Sensei237
 */

@Transactional
@DynamicUpdate
@Entity
@Table(name = "veyuz_user", uniqueConstraints = { @UniqueConstraint(columnNames = {"client_id"})})
public class AppUser {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(fetch = FetchType.EAGER,cascade = CascadeType.ALL, mappedBy = "utilisateur")
    @JsonBackReference
    private Collection<Notification> notifications;

    @JoinColumn(name = "banque_id", referencedColumnName = "id")
    @ManyToOne
    @JsonManagedReference
    private Banque banque;
    
    @JoinColumn(name = "client_id", referencedColumnName = "id")
    @OneToOne (cascade = CascadeType.ALL)
    @JsonBackReference
    private Client client;

    @Column(name = "email", length = 50, nullable = false, unique = true)
    private String email;

    @JoinColumn(name = "agence_id", referencedColumnName = "id")
    @ManyToOne
    private Agence agence;

    private String password;

    private String avatar;

    private boolean isEnable;

    private String nom;

    private String prenom;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "veyuz_user_app_roles",
            joinColumns = @JoinColumn(name = "app_user_id"),
            inverseJoinColumns = @JoinColumn(name = "app_roles_id"))
    private Set<AppRole> appRoles = new LinkedHashSet<>();

    @JsonBackReference

    public void setId(Long id) {
        this.id = id;
    }

    public Collection<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(Collection<Notification> notifications) {
        this.notifications = notifications;
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

    public String getPassword() {
        return password;
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

    private Date dateNaissance;

    private String lieuNaissance;

    @Column(nullable = false, unique = true, length = 50)
    private String telephone1;

    @Column(length = 50)
    private String telephone2;

    private String adresse;

    private String gender;
    private String codeAuthentication;
    private LocalDateTime dateCodeAuthentication;
    private boolean statusCodeAuth;

    @Column(nullable = false, unique = true, length = 25)
    private String userName;

    public Long getId() {
        return id;
    }

    public Agence getAgence() {
        return agence;
    }

    public void setAgence(Agence agence) {
        this.agence = agence;
    }

    public Set<AppRole> getAppRoles() {
        return appRoles;
    }

    public void setAppRoles(Set<AppRole> appRoles) {
        this.appRoles = appRoles;
    }

    public String getCodeAuthentication() {
        return codeAuthentication;
    }

    public void setCodeAuthentication(String codeAuthentication) {
        this.codeAuthentication = codeAuthentication;
    }

    public LocalDateTime getDateCodeAuthentication() {
        return dateCodeAuthentication;
    }

    public void setDateCodeAuthentication(LocalDateTime dateCodeAuthentication) {
        this.dateCodeAuthentication = dateCodeAuthentication;
    }

    public boolean isStatusCodeAuth() {
        return statusCodeAuth;
    }

    public void setStatusCodeAuth(boolean statusCodeAuth) {
        this.statusCodeAuth = statusCodeAuth;
    }
}
