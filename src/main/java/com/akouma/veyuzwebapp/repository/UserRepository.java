package com.akouma.veyuzwebapp.repository;

import com.akouma.veyuzwebapp.model.AppUser;
import com.akouma.veyuzwebapp.model.Banque;
import com.akouma.veyuzwebapp.model.Client;
import org.apache.tomcat.jni.User;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface UserRepository extends CrudRepository<AppUser, Long> {

    AppUser findFirstByEmail(String username);

    AppUser findFirstByUserName(String userName);

    @Query("select l  from AppUser l  where   l.id = :user AND l.banque = :bank")
    AppUser bloquerUserBanque(@Param("user") Long user, @Param("bank") Banque bank);

    Iterable<AppUser> findByBanqueAndClientOrderByNomAsc(Banque banque, Client client);
    Iterable<AppUser> findByNomLike(String name);
    List<Client> findAll(Specification<AppUser> spec);

}
