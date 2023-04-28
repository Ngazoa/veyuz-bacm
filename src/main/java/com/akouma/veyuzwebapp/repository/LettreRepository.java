package com.akouma.veyuzwebapp.repository;

import com.akouma.veyuzwebapp.model.Banque;
import com.akouma.veyuzwebapp.model.Lettre;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface LettreRepository extends CrudRepository<Lettre, Long> {
    @Query("select  l from Lettre l where l.banque=:bank and l.type =:type")
    Lettre  findLettreByBanque( @Param("type") String type, @Param("bank") Banque bank);
//
//    @Query("UPDATE   Lettre  SET  content = :containt WHERE type = :type AND banque_id = :bank")
//    void updateLetterBank(@Param("etat") String type, @Param("containt") Long user, @Param("bank") Long bank);


}
