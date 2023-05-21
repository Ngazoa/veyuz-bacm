package com.akouma.veyuzwebapp.repository;

import com.akouma.veyuzwebapp.model.Agence;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgenceRepository  extends CrudRepository<Agence, Long>  {


}
