package com.akouma.veyuzwebapp.controller;

import com.akouma.veyuzwebapp.model.AppUser;
import com.akouma.veyuzwebapp.model.Banque;
import com.akouma.veyuzwebapp.service.BanqueService;
import com.akouma.veyuzwebapp.service.MailService;
import com.akouma.veyuzwebapp.service.UserService;
import com.akouma.veyuzwebapp.utils.CheckSession;
import com.akouma.veyuzwebapp.utils.PasswordGenerator;
import com.akouma.veyuzwebapp.utils.ReferenceGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Controller
public class MainController {

    @Autowired
    HttpSession session;
    @Autowired
    private UserService userService;
    @Autowired
    private BanqueService banqueService;
    @Autowired
    private MailService mailService;

    public MainController(HttpSession session) {

        this.session = session;
    }

    @GetMapping("/")
    public String welcomePage(Model model, Principal principal) {
        List<Banque> banquesList = null;
        String message = null;
        AppUser appUser = null;
        if (principal != null) {
            appUser = userService.getLoggedUser(principal);
            this.session.setAttribute("loggedUser", appUser);
            if (appUser == null) {
                return "redirect:/404";
            }
            message = "Bienvenue " + appUser.getNom() + " " + appUser.getPrenom();
            model.addAttribute("user", appUser);
            session.setAttribute("userConnecte", appUser);
            if (appUser.getClient() != null) {
                banquesList = (List<Banque>) appUser.getClient().getBanques();

            } else {
                if (appUser.getBanque() != null) {
                    banquesList = new ArrayList<>();
                    banquesList.add(appUser.getBanque());
                } else {
                    return "redirect:/veyuz";
                }
            }
            this.session.setAttribute("loggedUser", appUser);

        } else {
            return "error/404";
        }
        this.session.setAttribute("banqueList", appUser.getBanque());

//        if (banquesList.size() == 1) {
//            Optional<Banque> banque = banquesList.stream().findFirst();
//            if (banque != null) {
//                return "redirect:/dashboard-banque-" + banque.get().getId();
//            }
//        }
//
        model.addAttribute("message", message);
//        model.addAttribute("banquesList", banquesList);
        Random random = new Random();
        int code = 10000 + random.nextInt(90000); // Génère un nombre aléatoire entre 10000 et 99999
        String authCode = userService.generateCodeConnexion(code);

        appUser.setCodeAuthentication(authCode);
        LocalDateTime localDateTime = LocalDateTime.now();
        appUser.setDateCodeAuthentication(localDateTime);
        appUser.setStatusCodeAuth(true);
        userService.saveUser(appUser);

        mailService.sendSimpleMessage(appUser.getEmail(), "Code de connexion ", "" +
                "Bienvenue a vous  et votre code de connexion est  : " + code);
        return "code-authentication";
    }

    @PostMapping("/valide-code")
    public String manageCodeAuht(@RequestParam("code") String code, Principal principal, Model model) {
        Banque banque = (Banque) session.getAttribute("banqueList");
        AppUser appUser = userService.getLoggedUser(principal);
        AppUser userCode = userService.getUserByEmail(appUser.getEmail());
        PasswordEncoder codeEncoder = new BCryptPasswordEncoder();

        boolean delay = new ReferenceGenerator().isWithinTenMinutes(userCode.getDateCodeAuthentication());

        if (userCode.isStatusCodeAuth() && delay && codeEncoder.matches(code,
                userCode.getCodeAuthentication())) {
            if (banque != null) {
                appUser.setStatusCodeAuth(false);
                userService.saveUser(appUser);
                return "redirect:/dashboard-banque-" + banque.getId();
            }
        }
        model.addAttribute("errorMessage", "Une erreur est survenue");
        return "code-authentication";
    }

    @GetMapping("/reinitialiser")
    public String reinitialser(Model model) {
        return "reset-password";
    }


    @PostMapping("/reinitialiser-pass")
    public String reinitialserSendMail(@RequestParam String email, Model model) {
        try {
            AppUser appUser = userService.getUserByEmail(email);
            if (appUser == null) {
                model.addAttribute("errorMessage", "Utilisateur non reconnu");
            } else {
                String pass = PasswordGenerator.generatePassword();

                PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
                String encryptedPassword = passwordEncoder.encode(pass);
                appUser.setPassword(encryptedPassword);
                userService.saveUser(appUser);

                mailService.sendSimpleMessage(email, "Reinitialisation pass ", " Votre Mot de passe a ete bien reinitialise et le nouveau est : " + pass);
                model.addAttribute("flashMessage", "Mot de passe reinitialise avec succes . Veuillez " + "vous connecter a votre boite mail et recuperer le nouveau pass pour vous connecter");

                return "loginPage";
            }

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Erreur survenue , impossible de continuer pour l' instant");
        }

        return "reset-password";
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
        // mailService.sendSimpleMessage("akouma.net@gmail.com","Belka TobBY","Jesus m'aime");
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
    public String registerNewPassewor(Model model, Principal principal, @RequestParam("inputPasswordCurrent") String oldpassword, @RequestParam("inputPasswordNew") String newPaasowrd, @RequestParam("inputPasswordNew2") String confirmPassword) {
        AppUser useConnecte = userService.getLoggedUser(principal);

        model.addAttribute("dash", "params");

        if (!newPaasowrd.equals(confirmPassword)) {
            String msg = "Mot de passe mal confirmé ";
            model.addAttribute("passwordForm", "password");
            model.addAttribute("errorMessage", msg);

            return "parametres";
        }
        boolean etatmodifi = userService.changeuserPassword(useConnecte, oldpassword, newPaasowrd);
        if (!etatmodifi) {
            String msg = "Ancien mot de passe incorrecte ";
            model.addAttribute("passwordForm", "password");
            model.addAttribute("errorMessage", msg);
            return "parametres";
        }

        String msg = "Mot de passe modifié avec succès";
        model.addAttribute("passwordForm", "password");
        model.addAttribute("flashMessage", msg);

        return "parametres";
    }

}
