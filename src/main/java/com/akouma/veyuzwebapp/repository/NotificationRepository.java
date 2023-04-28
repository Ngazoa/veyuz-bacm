package com.akouma.veyuzwebapp.repository;

import com.akouma.veyuzwebapp.model.AppUser;
import com.akouma.veyuzwebapp.model.Notification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface NotificationRepository extends CrudRepository<Notification, Long> {
    Iterable<Notification> findByUtilisateurIdAndIsReadOrderByDateCreationDesc(Long user,boolean isRead);
    Iterable<Notification> findByUtilisateurIdOrderByDateCreationDesc(Long user);
}
