package com.akouma.veyuzwebapp.service;

import com.akouma.veyuzwebapp.model.Banque;
import com.akouma.veyuzwebapp.repository.BanqueRepository;
import com.akouma.veyuzwebapp.repository.UserRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Data
@Service
public class BanqueService {

    @Autowired
    private BanqueRepository banqueRepository;

    public Iterable<Banque> getbanques() {
        return banqueRepository.findAll();
    }

    public Optional<Banque> getBanque(final Long id) {
        return banqueRepository.findById(id);
    }

    public BanqueRepository getBanqueRepository() {
        return banqueRepository;
    }

    public void setBanqueRepository(BanqueRepository banqueRepository) {
        this.banqueRepository = banqueRepository;
    }

    public Banque save(Banque banque) {
        Banque savedBanque = banqueRepository.save(banque);

        return savedBanque;
    }

    public void delete(final Long id) {
        banqueRepository.deleteById(id);
    }

    public Banque addBanque(Banque banque) {

        return banqueRepository.save(banque);
    }

    @Data
    @Service
    public static class UserService {

        @Autowired
        private UserRepository userRepository;

    }
}
