package com.akouma.veyuzwebapp.controller;

import com.akouma.veyuzwebapp.form.UserForm;
import com.akouma.veyuzwebapp.form.UserRoleForm;
import com.akouma.veyuzwebapp.model.AppRole;
import com.akouma.veyuzwebapp.model.AppUser;
import com.akouma.veyuzwebapp.model.Banque;
import com.akouma.veyuzwebapp.service.AppRoleService;
import com.akouma.veyuzwebapp.service.BanqueService;
import com.akouma.veyuzwebapp.service.UserRoleService;
import com.akouma.veyuzwebapp.service.UserService;
import com.akouma.veyuzwebapp.utils.CheckSession;
import com.akouma.veyuzwebapp.validator.AppUserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.List;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AppUserValidator userValidator;

    @Autowired
    private AppRoleService roleService;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    HttpSession session;

    public UserController(HttpSession session) {
        this.session = session;
    }

    @InitBinder
    protected void initBinder(WebDataBinder dataBinder) {
        Object target = dataBinder.getTarget();
        if (target == null) {
            return;
        }
        System.out.println("Target=" + target);

        if (target.getClass() == UserForm.class) {
            dataBinder.setValidator(userValidator);
        }
    }

    @Secured({"ROLE_SUPERADMIN", "ROLE_SUPERUSER"})
    @GetMapping({"/admin/add-user", "/admin/edit-{user_id}/user"})
    public String getUserForm(
            @PathVariable(value = "user_id", required = false) AppUser user,
            Model model, Principal principal) {

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session) || principal == null) {
            return "redirect:/";
        }

        Banque banque = (Banque) session.getAttribute("banque");
        AppUser loggedUser = userService.getLoggedUser(principal);
        if (!banque.equals(loggedUser.getBanque())) {
            return "error/403";
        }

        if (user == null) {
            user = new AppUser();
        }
        UserForm userForm = new UserForm(user);
        userForm.setBanque(banque);
        model.addAttribute("userForm", userForm);
        model.addAttribute("banque", banque);
        model.addAttribute("showEditForm", true);
        model.addAttribute("saveUserFormUri", "/admin-save-user");
        model.addAttribute("dash","admin");

        return "users";
    }

    @Secured({"ROLE_SUPERADMIN", "ROLE_SUPERUSER"})
    @PostMapping("/admin-save-user")
    public String saveUser(
            HttpServletRequest request,
            @ModelAttribute @Validated UserForm userForm,
            BindingResult result,
            Model model,
            final RedirectAttributes redirectAttributes) {

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session)) {
            return "redirect:/";
        }
        model.addAttribute("banque", userForm.getBanque());
        model.addAttribute("showEditForm", true);
        if (result.hasErrors()) {
            System.out.println("Nous sommes ici");
            return "users";
        }
        AppUser newAppUser = null;
        try {
            newAppUser = userService.saveUser(userForm, request);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error: " + e.getMessage());
            return "users";
        }


        String msg = "L'utilisateur a ete ajoute avec succes.";
        redirectAttributes.addFlashAttribute("flashMessage", msg);

//<<<<<<< HEAD
//
//        return "redirect:/admin-user-" + newAppUser.getId() + "/roles/add";
//=======
        return "redirect:/admin-users-list";

    }

    @Secured({"ROLE_SUPERADMIN", "ROLE_SUPERUSER"})
    @GetMapping("/admin-users-list")
    public String getUsersList(Model model, Principal principal, RedirectAttributes redirectAttributes) {

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session)) {
            return "redirect:/";
        }

        Banque banque = (Banque) session.getAttribute("banque");
        AppUser loggedUser = userService.getLoggedUser(principal);
        if (!banque.equals(loggedUser.getBanque())) {
            return "error/403";
        }
        model.addAttribute("banque", banque);
        model.addAttribute("users", userService.getBanqueUsers(banque));
        model.addAttribute("showList", true);

        model.addAttribute("newUserFormUri", "/admin/add-user");
        model.addAttribute("editUserFormUri", "/admin/{id}/edit-{user_id}/user");
        model.addAttribute("configAccessUri", "/form/user/{id}/access");
        model.addAttribute("postUserAccessForm", "/configure/user/access");
        model.addAttribute("blockUserUri", "/enable/user/{id}/block");
        model.addAttribute("activeUserUri", "/enable/user/{id}/active");
        model.addAttribute("dash","admin");

        return "users";
    }


    @Secured({"ROLE_SUPERADMIN", "ROLE_SUPERUSER"})
    @GetMapping("/admin-user-{id}/roles/add")
    public String configureUserRoles(@PathVariable("id") AppUser appUser, Model model, Principal principal) {

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session) || principal == null) {
            return "redirect:/";
        }

        Banque banque = (Banque) session.getAttribute("banque");
        AppUser loggedUser = userService.getLoggedUser(principal);
        if (!banque.equals(loggedUser.getBanque()) || !appUser.getBanque().equals(banque) || loggedUser.getBanque() == null) {
            return "error/403";
        }

        UserRoleForm rolesFom = new UserRoleForm();
        rolesFom.setAppUser(appUser);

        model.addAttribute("roles", roleService.getRoles());
        System.out.println(roleService.getRoles());
        model.addAttribute("user", appUser);
        model.addAttribute("rolesForm", rolesFom);
        model.addAttribute("dash","admin");

        model.addAttribute("banque", banque);

        return "roles_list";
    }


    @Secured({"ROLE_SUPERADMIN", "ROLE_SUPERUSER"})
    @PostMapping("/admin/save-user-roles")
    public String saveUserRoles(@ModelAttribute UserRoleForm userRoleForm, Model model, RedirectAttributes redirectAttributes) {

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session)) {
            return "redirect:/";
        }
        if (userRoleForm.getAppRoles().size() < 1) {
            return "roles_list";
        }

        userRoleService.addUserRoles(userRoleForm);

        redirectAttributes.addFlashAttribute("flashMessage", "Roles enregistrés");

        return "redirect:/admin-users-list";

    }

    @Secured({"ROLE_SUPERADMIN", "ROLE_SUPERUSER"})
    @GetMapping("/admin/block-active-bank-user-{id}/{etat}")
    public String boquerUser(@PathVariable("id") AppUser appUser, @PathVariable("etat") int etat, RedirectAttributes redirectAttributes, Principal principal) {

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session)) {
            return "redirect:/users";
        }

        Banque banque = (Banque) session.getAttribute("banque");
        AppUser loggedUser = userService.getLoggedUser(principal);
        if (!banque.equals(loggedUser.getBanque()) || !appUser.getBanque().equals(banque)) {
            return "error/403";
        }
        boolean action = etat == 1;

        userService.bloquerUserBank(appUser, action);

        String msg = "Vous avez bloqué " + appUser.getNom();
        if (action) {
            msg = "Vous avez activé " + appUser.getNom();
        }

        redirectAttributes.addFlashAttribute("flashMessage", msg);

        return "redirect:/admin-users-list";
    }

    @GetMapping("/getUserProfile")
    public String getUserProfile(Principal principal, Model model) {
        AppUser user = userService.getLoggedUser(principal);
        model.addAttribute("user", user);
        model.addAttribute("profile", "profile");
        model.addAttribute("dash", "params");
        model.addAttribute("setItem", "profile");
        return "profile";
    }

}
