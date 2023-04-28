package com.akouma.veyuzwebapp.service;

import com.akouma.veyuzwebapp.model.AppUser;
import com.akouma.veyuzwebapp.model.Notification;
import com.akouma.veyuzwebapp.repository.NotificationRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Data
@Service
public class NotificationService {
    @Autowired
    private MailSender mailSender;

    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    UserService userService;

    public Notification getNotificationRepository(Notification notification) {
        return notificationRepository.save(notification);
    }

    public void setNotificationRepository(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }

    public Iterable<Notification> getUserNotificationsNotreaded(Principal user) {
        AppUser loggedUser = userService.getLoggedUser(user);
        if (loggedUser != null) {
            Iterable<Notification> UserNotifications = notificationRepository.findByUtilisateurIdAndIsReadOrderByDateCreationDesc(loggedUser.getId(), false);
            return UserNotifications;
        }
        return null;
    }

    public Iterable<Notification> getAllNotifications(AppUser user) {
        Iterable<Notification> ntf = notificationRepository.findByUtilisateurIdOrderByDateCreationDesc(user.getId());
        for (Notification n : ntf) {
            n.setRead(true);
            notificationRepository.save(n);
        }
        return ntf;
    }


    public void sendMailMessage(
            final SimpleMailMessage simpleMailMessage) {
        this.mailSender.send(simpleMailMessage);
    }

}
