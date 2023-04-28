package com.akouma.veyuzwebapp.repository;

import com.akouma.veyuzwebapp.model.AppRole;
import com.akouma.veyuzwebapp.model.AppUser;
import com.akouma.veyuzwebapp.model.Client;
import com.akouma.veyuzwebapp.model.UserRole;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepository extends CrudRepository<UserRole, Long> {
    UserRole findFirstByAppUserAndAppRole(AppUser appUser, AppRole appRole);

    Iterable<UserRole> findByAppUser(AppUser appUser);

}
