package com.akouma.veyuzwebapp.repository;

import com.akouma.veyuzwebapp.model.Banque;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BanqueRepository extends CrudRepository<Banque, Long> {
}
