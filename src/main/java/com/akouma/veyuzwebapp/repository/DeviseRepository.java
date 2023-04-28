package com.akouma.veyuzwebapp.repository;

import com.akouma.veyuzwebapp.model.Devise;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviseRepository extends CrudRepository<Devise, Long> {
    Devise findFirstByCode(String code);
}
