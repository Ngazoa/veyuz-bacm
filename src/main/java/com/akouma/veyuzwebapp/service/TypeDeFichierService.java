package com.akouma.veyuzwebapp.service;

import com.akouma.veyuzwebapp.model.TypeDeFichier;
import com.akouma.veyuzwebapp.repository.TypeDeFichierRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Data
@Service
public class TypeDeFichierService {

    @Autowired
    private TypeDeFichierRepository typeDeFichierRepository;

    public TypeDeFichierRepository getTypeDeFichierRepository() {
        return typeDeFichierRepository;
    }

    public void setTypeDeFichierRepository(TypeDeFichierRepository typeDeFichierRepository) {
        this.typeDeFichierRepository = typeDeFichierRepository;
    }

    public Iterable<TypeDeFichier> getAll() {
        return typeDeFichierRepository.findAll();
    }

    public TypeDeFichier save(TypeDeFichier typeDeFichier) {
        return typeDeFichierRepository.save(typeDeFichier);
    }

    public TypeDeFichier updateStatus(Long id, boolean status) {
        TypeDeFichier typeDeFichier = typeDeFichierRepository.findById(id).get();
        typeDeFichier.setObligatoire(status);
        return typeDeFichierRepository.save(typeDeFichier);
    }
}
