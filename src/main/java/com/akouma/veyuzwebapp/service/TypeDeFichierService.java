package com.akouma.veyuzwebapp.service;

import com.akouma.veyuzwebapp.model.TypeDeFichier;
import com.akouma.veyuzwebapp.repository.TypeDeFichierRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Data
@Service
public class TypeDeFichierService {

    public TypeDeFichierRepository getTypeDeFichierRepository() {
        return typeDeFichierRepository;
    }

    public void setTypeDeFichierRepository(TypeDeFichierRepository typeDeFichierRepository) {
        this.typeDeFichierRepository = typeDeFichierRepository;
    }

    @Autowired
    private TypeDeFichierRepository typeDeFichierRepository;

    public Iterable<TypeDeFichier> getAll() {
        return typeDeFichierRepository.findAll();
    }

    public TypeDeFichier save(TypeDeFichier typeDeFichier) {
        return typeDeFichierRepository.save(typeDeFichier);
    }
}
