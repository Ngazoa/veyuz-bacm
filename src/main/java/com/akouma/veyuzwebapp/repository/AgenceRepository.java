package com.akouma.veyuzwebapp.repository;

import com.akouma.veyuzwebapp.model.Agence;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgenceRepository  extends CrudRepository<Agence, Long>  {
    Iterable<Agence> findAllByStatus(boolean status);


}
