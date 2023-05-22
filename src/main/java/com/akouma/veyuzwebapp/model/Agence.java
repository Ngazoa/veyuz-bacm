package com.akouma.veyuzwebapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "veyuz_agences")
public class Agence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    private String label;

    private String address;
    private String phone;
    private boolean status;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "agence")
    private Collection<AppUser> appUsers;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "agence")
    private Collection<Client> clients;
}
