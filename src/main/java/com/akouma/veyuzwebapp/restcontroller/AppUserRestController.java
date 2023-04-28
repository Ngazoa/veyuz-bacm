package com.akouma.veyuzwebapp.restcontroller;

import com.akouma.veyuzwebapp.form.UserRoleForm;
import com.akouma.veyuzwebapp.model.AppRole;
import com.akouma.veyuzwebapp.model.AppUser;
import com.akouma.veyuzwebapp.model.UserRole;
import com.akouma.veyuzwebapp.service.AppRoleService;
import com.akouma.veyuzwebapp.service.UserRoleService;
import com.akouma.veyuzwebapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
public class AppUserRestController {

    @Autowired
    private UserService userService;

    @Autowired
    private AppRoleService appRoleService;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    TemplateEngine templateEngine;

    @PostMapping("/configure/user/access")
    public ResponseEntity<?> configUserAccess(@ModelAttribute UserRoleForm userRoleForm) {

        userRoleService.addUserRoles(userRoleForm);
        HashMap<String, Object> response = new HashMap<>();
        response.put("isOk", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/form/user/{id}/access")
    public ResponseEntity<?> getRolesForm(@PathVariable("id") AppUser appUser) {

        Map<String, Object> data = new HashMap<>();
        Iterable<AppRole> roles = appRoleService.getRoles();
        Collection<UserRole> userRoles = appUser.getUserRoles();
        String htmlContent = "<input type='hidden' name='appUser' value='" + appUser.getId() +"'>";
        for (AppRole role : roles) {
            if (role.isVisible()) {
                String checked = "";
                UserRole ur = userRoleService.getUserRole(appUser, role);
                if (ur != null) {
                    checked = "checked=true";
                }
                String classAdmin = "";
                String readOnly = "";
                System.out.println(role.getRoleName());
                if (role.getRoleName().equals("ROLE_ADMIN")) {
                    classAdmin = "roleAdmin";
                    checked = "checked=true";
                    readOnly = "readOnly=true";
                }
                htmlContent += "<div class='mb-3'>" +
                        "<label class='form-check inputRole'>" +
                        "<input type='checkbox' " + readOnly + " class='form-check-input " + classAdmin + "' name='appRoles' " + checked + " value='" + role.getId() + "'>" +
                        "<span class='form-check-label'>" + role.getRoleDescription() + "</span>" +
                        "</label>" +
                        "</div>";
            }
        }

        data.put("data", htmlContent);

        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping({"/enable/user/{id}/active", "/enable/user/{id}/block"})
    public ResponseEntity<?> enableUserApp(@PathVariable("id") AppUser appUser) {
        appUser.setEnable(!appUser.isEnable());
        HashMap<String, Object> response = new HashMap<>();
        AppUser saved = userService.saveUser(appUser);
        response.put("isOk", true);
        String message = saved.isEnable() ? "Le compte a été activé !" : "Le compte a été bloqué !";
        response.put("message", message);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
