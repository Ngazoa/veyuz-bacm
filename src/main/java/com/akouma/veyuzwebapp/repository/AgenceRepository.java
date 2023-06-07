package com.akouma.veyuzwebapp.repository;

import com.akouma.veyuzwebapp.model.Agence;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgenceRepository  extends CrudRepository<Agence, Long>  {
    Iterable<Agence> findAllByStatus(boolean status);


    Optional<Agence> findByCode(String code);
}
