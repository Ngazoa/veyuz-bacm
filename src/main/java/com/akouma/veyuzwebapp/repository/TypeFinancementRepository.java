package com.akouma.veyuzwebapp.repository;

import com.akouma.veyuzwebapp.model.TypeFinancement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TypeFinancementRepository extends JpaRepository<TypeFinancement, Long> {
    //List<TypeFinancement> findAll();
}