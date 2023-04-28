package com.akouma.veyuzwebapp.repository;

import com.akouma.veyuzwebapp.model.Pays;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaysRepository extends CrudRepository<Pays, Long> {

}
