package com.akouma.veyuzwebapp.service;

import com.akouma.veyuzwebapp.model.Agence;
import com.akouma.veyuzwebapp.model.AppRole;
import com.akouma.veyuzwebapp.model.AppUser;
import com.akouma.veyuzwebapp.repository.AgenceRepository;
import com.akouma.veyuzwebapp.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.*;

@Service
public class AgenceService {

    private final AgenceRepository agenceRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final AppRoleService appRole;

    public AgenceService(AgenceRepository agenceRepository, UserRepository userRepository
            , UserService userService, AppRoleService appRole) {
        this.agenceRepository = agenceRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.appRole = appRole;
    }

    public Iterable<Agence> findAllAgences() {
        return agenceRepository.findAllByStatus(true);
    }

    public Iterable<Agence> getAllAgences() {
        return agenceRepository.findAllByStatus(true);
    }

    public Agence getAgenceById(Long id) {
        return agenceRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Agence not found with id: " + id));
    }

    public Agence findByCode(String code) {
        return agenceRepository.findByCode(code).orElseThrow(
                () -> new NoSuchElementException("Agence not found with code: " + code)
        );
    }

    public void saveAgence(Agence agence) {
        Agence agency = agenceRepository.save(agence);
        for (AppUser a :
                agency.getAppUsers()) {
            Set<AppRole> userRoles = new HashSet<>();
            userRoles.add(appRole.getRoleByName("ROLE_AGENCE"));
            a.setAgence(agency);
            a.setAppRoles(userRoles);
            userRepository.save(a);
        }
    }

    public void deleteAgence(Long id) {
        Optional<Agence> agence = agenceRepository.findById(id);
        if (agence.isEmpty()) {
            return;
        }
        Agence agence1 = agence.get();
        agence1.setStatus(false);
        agenceRepository.save(agence1);
    }
}