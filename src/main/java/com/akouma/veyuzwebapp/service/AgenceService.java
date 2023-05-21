package com.akouma.veyuzwebapp.service;

import com.akouma.veyuzwebapp.model.Agence;
import com.akouma.veyuzwebapp.repository.AgenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AgenceService {

    @Autowired
    AgenceRepository agenceRepository;

    public Iterable<Agence> findAllAgences() {
        return agenceRepository.findAll();
    }
}
