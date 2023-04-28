package com.akouma.veyuzwebapp.controller;


import com.akouma.veyuzwebapp.model.AppUser;
import com.akouma.veyuzwebapp.model.Notification;
import com.akouma.veyuzwebapp.service.NotificationService;
import com.akouma.veyuzwebapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class NotificationController {
    @Autowired
    UserService userService;
    @Autowired
    private NotificationService notificationService;

    @GetMapping("/getAllnotification")
    public String getNotificationsNotRead(Model model, Principal principal) {
        AppUser loggedUser = userService.getLoggedUser(principal);
        Iterable<Notification> totalNoticationNonLues = notificationService.getAllNotifications(loggedUser);
        long nbre = totalNoticationNonLues.spliterator().getExactSizeIfKnown();

        model.addAttribute("nbreNotif", nbre);
        model.addAttribute("message", totalNoticationNonLues);
        return "notification";
    }
}
