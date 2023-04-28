package com.akouma.veyuzwebapp.restcontroller;

import com.akouma.veyuzwebapp.model.AppUser;
import com.akouma.veyuzwebapp.model.Notification;
import com.akouma.veyuzwebapp.service.NotificationService;
import com.akouma.veyuzwebapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.HashMap;

@RestController
public class NotificationRestController {
    @Autowired
    UserService userService;
    @Autowired
    private NotificationService notificationService;

    @GetMapping("/getNotificationNonLues")
    public HashMap getNotificationsNotRead(Model model, Principal principal) {

        Iterable<Notification> totalNoticationNonLues = notificationService.getUserNotificationsNotreaded(principal);
        long nbre = totalNoticationNonLues.spliterator().getExactSizeIfKnown();

        String lastNotif = "";
        int i = 0;
        for (Notification n : totalNoticationNonLues) {
            String href = "#";
            if (n.getHref() != null) {
                href = n.getHref();
            }
            lastNotif += "<a href='" + href + "' class='list-group-item'>" +
                    "<div class='row g-0 align-items-center'>" +
                    "<div class='col-2'>" +
                    "<i class='text-danger' data-feather='alert-circle'></i>" +
                    "</div>" +
                    "<div class='col-10'>" +
                    "<div class='text-dark'>" +
                    new SimpleDateFormat("dd/MM/yyyy à H:m:s").format(n.getDateCreation()) + "</div>" +
                    "<div class='text-muted small mt-1'>" + n.getMessage() + "</div>" +
                    "<div class='text-muted small mt-1'>Non lu</div>" +
                    "</div>" +
                    "</div>" +
                    "</a>";
            if (i >= 3) {
                break;
            }
            i++;
        }


        HashMap<String, Object> response = new HashMap<>();
        response.put("nbreNotif", nbre);
        response.put("messageNonLues", totalNoticationNonLues);
        response.put("lastNotif", lastNotif);
  return  response;
    }
}
