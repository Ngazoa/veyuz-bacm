package com.akouma.veyuzwebapp.controller;

import com.akouma.veyuzwebapp.form.ImportFile;
import com.akouma.veyuzwebapp.model.AppUser;
import com.akouma.veyuzwebapp.model.Apurement;
import com.akouma.veyuzwebapp.model.Banque;
import com.akouma.veyuzwebapp.model.Client;
import com.akouma.veyuzwebapp.service.ApurementService;
import com.akouma.veyuzwebapp.service.ClientService;
import com.akouma.veyuzwebapp.service.UserService;
import com.akouma.veyuzwebapp.utils.CheckSession;
import com.akouma.veyuzwebapp.utils.StatusTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.security.Principal;


/**
 * Dans ce controller nous allons definir les differentes fonctions permettant de gerer les apurements en nous basant sur le modele
 * utilisant le fichier excel des mises en demeure.
 */
@Controller
public class ApurementController {

    private final int max = 10;
    private final int page = 1;
    @Autowired
    HttpSession session;
    @Autowired
    private ApurementService apurementService;
    @Autowired
    private UserService userService;
    @Autowired
    private ClientService clientService;

    public ApurementController(HttpSession session) {
        this.session = session;
    }

    @Secured({"ROLE_ADMIN", "ROLE_CLIENT", "ROLE_TREASURY_OPS", "ROLE_AGENCE", "ROLE_TRADE_DESK"})
    @GetMapping({"/apurments", "/apurments/page={page}"})
    public String getApurements(@PathVariable(value = "page", required = false) Integer page, Model model, Principal principal, Authentication authentication) {

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session)) {
            return "redirect:/";
        }

        if (page != null && page <= 0) {
            return "error/404";
        }

        if (page == null) {
            page = this.page;
        }
        page--;

        Banque banque = (Banque) session.getAttribute("banque");
        AppUser loggedUser = userService.getLoggedUser(principal);

        Page<Apurement> apurements = null;
        boolean isApured = false;
        if (loggedUser.getClient() != null) {
            apurements = apurementService.getApurementsHasFiles(banque, loggedUser.getClient(), max, page, isApured, StatusTransaction.APUREMENT_WAITING_FILES);
        } else {
            if (authentication.getAuthorities().stream().anyMatch(a -> !a.getAuthority().equals("ROLE_AGENCE"))){
                apurements = apurementService.getApurementsHasFiles(banque, null, max, page, isApured, StatusTransaction.APUREMENT_WAITING_FILES);
            } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_AGENCE"))) {
//                ON GERE LE PROFIL AGENCE
                apurements = apurementService.getApurementsHasFilesAgence(banque, loggedUser, max, page, isApured, StatusTransaction.APUREMENT_WAITING_FILES);
            }

        }

        model.addAttribute("apurements", apurements.getContent());
        model.addAttribute("currentPage", apurements.getNumber() + 1);
        model.addAttribute("banque", banque);
        model.addAttribute("nbPages", apurements.getTotalPages());
        int nbPages = apurements.getTotalPages();
        if (nbPages > 1) {
            int[] pages = new int[nbPages];
            for (int i = 0; i < nbPages; i++) {
                pages[i] = i + 1;
                System.out.println(i);
            }
            model.addAttribute("pages", pages);
        }

        ImportFile importFile = new ImportFile();
        importFile.setBanque(banque);

        model.addAttribute("importFile", importFile);
        model.addAttribute("dash", "apurments");
        String uri = "/apurments/page={page}";
        model.addAttribute("uri", uri);
        model.addAttribute("das", "apw");
        model.addAttribute("exportUri", "/" + banque.getId() + "-export/apurments?apured=false");
        model.addAttribute("searchUri", "/search-apurements");

        return "apurements/index";
    }

    @Secured({"ROLE_ADMIN", "ROLE_TRADE_DESK"})
    @GetMapping({"/apurments-treasury-approved", "/apurments-apurments-treasury-approved/page={page}"})
    public String getTransactionsValideesParLaTresorerie(@PathVariable(value = "page", required = false) Integer page, Model model, Principal principal) {

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session)) {
            return "redirect:/";
        }

        if (page != null && page <= 0) {
            return "error/404";
        }

        if (page == null) {
            page = this.page;
        }
        page--;

        Banque banque = (Banque) session.getAttribute("banque");
        AppUser loggedUser = userService.getLoggedUser(principal);

        Page<Apurement> apurements;
        boolean isApured = false;
        apurements = apurementService.getApurementsTreasurySend(banque, max, page, isApured, StatusTransaction.APUREMENT_WAITING_DATE);

        model.addAttribute("apurements", apurements.getContent());
        model.addAttribute("currentPage", apurements.getNumber() + 1);
        model.addAttribute("banque", banque);
        model.addAttribute("nbPages", apurements.getTotalPages());
        int nbPages = apurements.getTotalPages();
        if (nbPages > 1) {
            int[] pages = new int[nbPages];
            for (int i = 0; i < nbPages; i++) {
                pages[i] = i + 1;
                System.out.println(i);
            }
            model.addAttribute("pages", pages);
        }

        ImportFile importFile = new ImportFile();
        importFile.setBanque(banque);

        model.addAttribute("importFile", importFile);
        model.addAttribute("dash", "apurments");
        String uri = "/apurments/page={page}";
        model.addAttribute("uri", uri);
        model.addAttribute("das", "apwd");
        model.addAttribute("exportUri", "/" + banque.getId() + "-export/apurments?apured=false");
        model.addAttribute("searchUri", "/search-apurements");

        return "apurements/index";
    }

    @Secured({"ROLE_ADMIN", "ROLE_CLIENT", "ROLE_TREASURY_OPS", "ROLE_AGENCE", "ROLE_TRADE_DESK"})
    @GetMapping({"/transactions-approuvees", "/transactions-approuvees/page={page}"})
    public String getTransactionsValidees(@PathVariable(value = "page", required = false) Integer page, Model model, Principal principal) {

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session)) {
            return "redirect:/";
        }

        if (page != null && page <= 0) {
            return "error/404";
        }

        if (page == null) {
            page = this.page;
        }
        page--;

        Banque banque = (Banque) session.getAttribute("banque");
        AppUser loggedUser = userService.getLoggedUser(principal);

        Page<Apurement> apurements;
        boolean isApured = false;
        if (loggedUser.getClient() != null) {
            apurements = apurementService.getApurements(banque, loggedUser.getClient(), max, page, isApured);
        } else {
            apurements = apurementService.getApurements(banque, null, max, page, isApured);
        }

        model.addAttribute("apurements", apurements.getContent());
        model.addAttribute("currentPage", apurements.getNumber() + 1);
        model.addAttribute("banque", banque);
        model.addAttribute("nbPages", apurements.getTotalPages());
        int nbPages = apurements.getTotalPages();
        if (nbPages > 1) {
            int[] pages = new int[nbPages];
            for (int i = 0; i < nbPages; i++) {
                pages[i] = i + 1;
                System.out.println(i);
            }
            model.addAttribute("pages", pages);
        }

        ImportFile importFile = new ImportFile();
        importFile.setBanque(banque);

        model.addAttribute("importFile", importFile);
        model.addAttribute("dash", "apurments");
        String uri = "/apurments/page={page}";
        model.addAttribute("uri", uri);
        model.addAttribute("das", "apw");
        model.addAttribute("exportUri", "/" + banque.getId() + "-export/apurments?apured=false");
        model.addAttribute("searchUri", "/search-apurements");

        return "apurements/index";
    }

    @Secured({"ROLE_ADMIN", "ROLE_CLIENT", "ROLE_TREASURY_OPS", "ROLE_AGENCE", "ROLE_TRADE_DESK"})
    @GetMapping({"/apurments-all", "/apurments-all/page={page}"})
    public String getAllApurements(@PathVariable(value = "page", required = false) Integer page, Model model, Principal principal) {

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session)) {
            return "redirect:/";
        }

        if (page != null && page <= 0) {
            return "error/404";
        }

        if (page == null) {
            page = this.page;
        }
        page--;

        Banque banque = (Banque) session.getAttribute("banque");
        AppUser loggedUser = userService.getLoggedUser(principal);

        Page<Apurement> apurements;
        boolean isApured = true;
        if (loggedUser.getClient() != null) {
            apurements = apurementService.getApurements(banque, loggedUser.getClient(), max, page, isApured);
        } else {
            apurements = apurementService.getApurements(banque, null, max, page, isApured);
        }

        model.addAttribute("apurements", apurements.getContent());
        model.addAttribute("currentPage", apurements.getNumber() + 1);
        model.addAttribute("banque", banque);
        model.addAttribute("nbPages", apurements.getTotalPages());
        int nbPages = apurements.getTotalPages();
        if (nbPages > 1) {
            int[] pages = new int[nbPages];
            for (int i = 0; i < nbPages; i++) {
                pages[i] = i + 1;
                System.out.println(i);
            }
            model.addAttribute("pages", pages);
        }

        ImportFile importFile = new ImportFile();
        importFile.setBanque(banque);

        model.addAttribute("importFile", importFile);
        model.addAttribute("dash", "apurments");
        String uri = "/apurments-all/page={page}";
        model.addAttribute("uri", uri);
        model.addAttribute("das", "apa");
        model.addAttribute("exportUri", "/" + banque.getId() + "-export/apurments?apured=true");
        model.addAttribute("searchUri", "/search-apurements");

        return "apurements/index";
    }

    @GetMapping({"apurments-expired", "apurments-expired/page={page}"})
    public String getAllExpiredApurements(Model model, Principal principal, @PathVariable(value = "page", required = false) Integer page) {
        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session)) {
            return "redirect:/";
        }

        if (page != null && page <= 0) {
            return "error/404";
        }

        if (page == null) {
            page = this.page;
        }
        page--;

        Banque banque = (Banque) session.getAttribute("banque");
        AppUser loggedUser = userService.getLoggedUser(principal);

        Page<Apurement> apurements;
        boolean isApured = false;

        apurements = apurementService.getExpiredApurements(banque, loggedUser.getClient(), max, page, isApured);

        model.addAttribute("apurements", apurements.getContent());
        model.addAttribute("currentPage", apurements.getNumber() + 1);
        model.addAttribute("banque", banque);
        model.addAttribute("nbPages", apurements.getTotalPages());
        int nbPages = apurements.getTotalPages();
        if (nbPages > 1) {
            int[] pages = new int[nbPages];
            for (int i = 0; i < nbPages; i++) {
                pages[i] = i + 1;
                System.out.println(i);
            }
            model.addAttribute("pages", pages);
        }

        ImportFile importFile = new ImportFile();
        importFile.setBanque(banque);

        model.addAttribute("importFile", importFile);
        model.addAttribute("dash", "apurments");
        String uri = "/apurments/page={page}";
        model.addAttribute("uri", uri);
        model.addAttribute("das", "ape");
        model.addAttribute("exportUri", "/" + banque.getId() + "-export/apurments?expired=true");
        model.addAttribute("searchUri", "/search-apurements");

        return "apurements/index";
    }

    @Secured({"ROLE_ADMIN", "ROLE_TREASURY_OPS", "ROLE_AGENCE", "ROLE_TRADE_DESK"})
    @GetMapping({"/apurments/{referenceClient}", "/apurments/{referenceClient}/page={page}"})
    public String getCustomerApurements(@PathVariable String referenceClient, @PathVariable("page") Integer page, Principal principal, Model model) {

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session)) {
            return "redirect:/";
        }


        if (page != null && page <= 0) {
            return "error/404";
        }

        if (page == null) {
            page = this.page;
        }
        page--;

        Banque banque = (Banque) session.getAttribute("banque");
        AppUser loggedUser = userService.getLoggedUser(principal);

        Client client = clientService.getClientByReference(referenceClient);
        if (client == null) {
            return "error/404";
        }
        boolean isApured = false;
        Page<Apurement> apurements = apurementService.getApurements(banque, client, 10000, page, isApured);

        model.addAttribute("apurements", apurements.get());
        model.addAttribute("currentPage", apurements.getNumber());
        model.addAttribute("banque", banque);
        model.addAttribute("client", client);
        int nbPages = apurements.getTotalPages();
        if (nbPages > 1) {
            int[] pages = new int[nbPages];
            for (int i = 0; i < nbPages; i++) {
                pages[i] = i + 1;
                System.out.println(i);
            }
            model.addAttribute("pages", pages);
        }
        model.addAttribute("das", "apw");
        model.addAttribute("searchUri", "/search-apurements");

        return "apurements/index";
    }

    @Secured({"ROLE_ADMIN", "ROLE_TREASURY_OPS", "ROLE_AGENCE", "ROLE_TRADE_DESK"})
    @GetMapping({"/apurments-all/{referenceClient}", "/apurments-all/{referenceClient}/page={page}"})
    public String getCustomerAllApurements(@PathVariable String referenceClient, @PathVariable("page") Integer page, Principal principal, Model model) {

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session)) {
            return "redirect:/";
        }


        if (page != null && page <= 0) {
            return "error/404";
        }

        if (page == null) {
            page = this.page;
        }
        page--;

        Banque banque = (Banque) session.getAttribute("banque");
        AppUser loggedUser = userService.getLoggedUser(principal);

        Client client = clientService.getClientByReference(referenceClient);
        if (client == null) {
            return "error/404";
        }
        boolean isApured = true;
        Page<Apurement> apurements = apurementService.getApurements(banque, client, 10000, page, isApured);

        model.addAttribute("apurements", apurements.get());
        model.addAttribute("currentPage", apurements.getNumber());
        model.addAttribute("banque", banque);
        model.addAttribute("client", client);
        int nbPages = apurements.getTotalPages();
        if (nbPages > 1) {
            int[] pages = new int[nbPages];
            for (int i = 0; i < nbPages; i++) {
                pages[i] = i + 1;
                System.out.println(i);
            }
            model.addAttribute("pages", pages);
        }
        model.addAttribute("das", "apa");

        return "apurements/index";
    }

    @GetMapping("/search-apurements")
    public String rechercherApurements(@RequestParam String reference, Principal principal, Model model) {
        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session)) {
            return "redirect:/";
        }
        Banque banque = (Banque) session.getAttribute("banque");
        AppUser loggedUser = userService.getLoggedUser(principal);
        Iterable<Apurement> apurements = apurementService.getbyReference(banque, loggedUser.getClient(), reference);

        model.addAttribute("apurements", apurements);
        model.addAttribute("banque", banque);
        model.addAttribute("dash", "apurments");
        model.addAttribute("das", "apw");
        model.addAttribute("searchUri", "/search-apurements");
        model.addAttribute("searchUri", "/search-apurements");

        return "apurements/index";
    }
}
