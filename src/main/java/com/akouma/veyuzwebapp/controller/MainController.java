package com.akouma.veyuzwebapp.controller;

import com.akouma.veyuzwebapp.model.AppUser;
import com.akouma.veyuzwebapp.model.Banque;
import com.akouma.veyuzwebapp.service.BanqueService;
import com.akouma.veyuzwebapp.service.UserService;
import com.akouma.veyuzwebapp.utils.CheckSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class MainController {

    @Autowired
    HttpSession session;
    @Autowired
    private UserService userService;
    @Autowired
    private BanqueService banqueService;

    public MainController(HttpSession session) {

        this.session = session;
    }

    @GetMapping("/")
    public String welcomePage(
            Model model, Principal principal) {
        List<Banque> banquesList = null;
        String message = null;
        if (principal != null) {
            AppUser appUser = userService.getLoggedUser(principal);
            this.session.setAttribute("loggedUser", appUser);
            if (appUser == null) {
                return "redirect:/404";
            }
            message = "Bienvenue " + appUser.getNom() + " " + appUser.getPrenom();
            model.addAttribute("user", appUser);
            session.setAttribute("userConnecte", appUser);
            if (appUser.getClient() != null) {
                banquesList = (List<Banque>) appUser.getClient().getBanques();
                System.out.println("Je suis un client !");
            } else {
                if (appUser.getBanque() != null) {
                    banquesList = new ArrayList<>();
                    banquesList.add(appUser.getBanque());
                    System.out.println("Je suis administrateur d'une banque");

                } else {
                    System.out.println("Je suis le super utilisateur");
                    return "redirect:/veyuz";
                }
            }
            this.session.setAttribute("loggedUser", appUser);
        } else {
            return "error/404";
        }

        if (banquesList.size() == 1) {
            Optional<Banque> banque = banquesList.stream().findFirst();
            if (banque != null) {
                return "redirect:/dashboard-banque-" + banque.get().getId();
            }
        }

        model.addAttribute("message", message);
        model.addAttribute("banquesList", banquesList);

        return "welcomePage";
    }

    @GetMapping("/parametres")
    public String showParametreView(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/404";
        }

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session)) {
            return "redirect:/";
        }

        Banque banque = (Banque) session.getAttribute("banque");

        model.addAttribute("profile", true);
        model.addAttribute("user", userService.getLoggedUser(principal));
        model.addAttribute("banque", banque);
        model.addAttribute("dash", "params");

        return "parametres";
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String loginPage(Model model) {

        return "loginPage";
    }

    @RequestMapping(value = "/logoutSuccessful", method = RequestMethod.GET)
    public String logoutSuccessfulPage(Model model) {
        model.addAttribute("title", "Logout");
        return "redirect:/login";
    }

    @GetMapping("/500")
    public String accessDenied500(Model model, Principal principal) {

        return "500Page";
    }

    @GetMapping("/404")
    public String accessDenied404(Model model, Principal principal) {

        return "404Page";
    }

    @RequestMapping(value = "/403", method = RequestMethod.GET)
    public String accessDenied(Model model, Principal principal) {

        return "403Page";
    }

    @RequestMapping(value = {"/logout"}, method = RequestMethod.GET)
    public String fetchSignoutSite(HttpServletRequest request, HttpServletResponse response) {

        HttpSession session = request.getSession(false);
        SecurityContextHolder.clearContext();

        session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        //  Ssi nous retrouvons le cookies enregidtree dans le systeme
        for (Cookie cookie : request.getCookies()) {
            cookie.setMaxAge(0);
        }

        return "redirect:/login?logout";
    }

    @GetMapping("/get-password-form")
    public String getMotPasseForm(Model model) {
        model.addAttribute("passwordForm", "password");
        model.addAttribute("dash", "params");
        model.addAttribute("setItem", "pass");
        return "parametres";
    }

    @PostMapping("/Editpassword")
    public String registerNewPassewor(Model model, Principal principal,
                                      @RequestParam("inputPasswordCurrent") String oldpassword,
                                      @RequestParam("inputPasswordNew") String newPaasowrd,
                                      @RequestParam("inputPasswordNew2") String confirmPassword) {
        AppUser useConnecte = userService.getLoggedUser(principal);
        System.out.println(newPaasowrd + "---------------------- 1" + confirmPassword);
        model.addAttribute("dash", "params");

        if (!newPaasowrd.equals(confirmPassword)) {
            String msg = "Mot de passe mal confirmé ";
            model.addAttribute("passwordForm", "password");
            model.addAttribute("errorMessage", msg);
            System.out.println("---------------------- 11");

            return "parametres";
        }
        boolean etatmodifi = userService.changeuserPassword(useConnecte, oldpassword, newPaasowrd);
        if (!etatmodifi) {
            String msg = "Ancien mot de passe incorrecte ";
            model.addAttribute("passwordForm", "password");
            model.addAttribute("errorMessage", msg);
            System.out.println("---------------------- 111 -- " + useConnecte.getPassword());

            return "parametres";
        }

        String msg = "Mot de passe modifié avec succès";
        model.addAttribute("passwordForm", "password");
        model.addAttribute("flashMessage", msg);
        System.out.println("---------------------- 1111");

        return "parametres";
    }

}
