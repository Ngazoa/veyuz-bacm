package com.akouma.veyuzwebapp.service;

import com.akouma.veyuzwebapp.model.Pays;
import com.akouma.veyuzwebapp.repository.PaysRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Data
@Service
public class PaysService {

    @Autowired
    private PaysRepository paysRepository;

    public PaysRepository getPaysRepository() {
        return paysRepository;
    }

    public void setPaysRepository(PaysRepository paysRepository) {
        this.paysRepository = paysRepository;
    }

    public Optional<Pays> getPays(final Long id) {
        return paysRepository.findById(id);
    }

    public Iterable<Pays> getPays() {
        return paysRepository.findAll();
    }

}
