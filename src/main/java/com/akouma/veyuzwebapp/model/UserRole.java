package com.akouma.veyuzwebapp.model;

import lombok.Data;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.Collection;

@Data
@Transactional
@Entity
@Table(name = "veyuz_user_role")
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    private String description;


}
