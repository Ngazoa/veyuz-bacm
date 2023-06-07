package com.akouma.veyuzwebapp.repository;

import com.akouma.veyuzwebapp.model.BanqueCorrespondante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BanqueCorrespondanteRepository extends JpaRepository<BanqueCorrespondante, Long> {
    List<BanqueCorrespondante> findAllByEnabled( boolean enabled);

}