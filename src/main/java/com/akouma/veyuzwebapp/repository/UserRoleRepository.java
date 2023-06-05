package com.akouma.veyuzwebapp.repository;

import com.akouma.veyuzwebapp.model.UserRole;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepository extends CrudRepository<UserRole, Long> {

}
