package com.akouma.veyuzwebapp.repository;

import com.akouma.veyuzwebapp.model.Mail;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MailRepository extends CrudRepository<Mail, Long> {
}
