package com.akouma.veyuzwebapp.repository;

import com.akouma.veyuzwebapp.model.AppRole;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface AppRoleRepository extends CrudRepository<AppRole, Long> {

    AppRole findFirstByRoleName(String roleName);

}
