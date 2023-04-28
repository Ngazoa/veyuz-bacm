package com.akouma.veyuzwebapp.repository;

import com.akouma.veyuzwebapp.model.TypeDeFichier;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeDeFichierRepository extends CrudRepository<TypeDeFichier, Long> {
}
