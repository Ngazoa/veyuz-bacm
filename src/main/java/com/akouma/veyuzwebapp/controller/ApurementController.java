package com.akouma.veyuzwebapp.controller;

import com.akouma.veyuzwebapp.form.ImportFile;
import com.akouma.veyuzwebapp.model.*;
import com.akouma.veyuzwebapp.repository.ApurementRepositoy;
import com.akouma.veyuzwebapp.repository.TransFinanciereRepository;
import com.akouma.veyuzwebapp.service.ApurementService;
import com.akouma.veyuzwebapp.service.ClientService;
import com.akouma.veyuzwebapp.service.TransactionService;
import com.akouma.veyuzwebapp.service.UserService;
import com.akouma.veyuzwebapp.utils.CheckSession;
import com.akouma.veyuzwebapp.utils.JoursRestant;
import com.akouma.veyuzwebapp.utils.StatusTransaction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;


/**
 * Dans ce controller nous allons definir les differentes fonctions permettant de gerer les apurements en nous basant sur le modele
 * utilisant le fichier excel des mises en demeure.
 */
@Controller
public class ApurementController {

    private final int max = 25;
    private final int page = 1;
    private final ObjectMapper objectMapper;
    @Autowired
    HttpSession session;
    @Autowired
    private ApurementService apurementService;
    @Autowired
    private UserService userService;
    @Autowired
    private ClientService clientService;
    @Autowired
    private TransFinanciereRepository transFinanciereRepository;
    @Autowired
    private ApurementRepositoy apurementRepositoy;

    @Autowired
    private TransactionService transactionService;

    public ApurementController(HttpSession session, ObjectMapper objectMapper) {
        this.session = session;
        this.objectMapper = objectMapper;
    }

    @Secured({"ROLE_ADMIN", "ROLE_SUPERADMIN", "ROLE_SUPERUSER", "ROLE_CONTROLLER", "ROLE_CLIENT", "ROLE_TREASURY_OPS", "ROLE_AGENCE", "ROLE_TRADE_DESK"})
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

            if (loggedUser.getAgence() == null || authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"))
                    || authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TRADE_DESK"))
            ) {
                apurements = apurementService.getNotApuredTransactions(banque, null, max, page, isApured, StatusTransaction.APUREMENT_WAITING_FILES);
            } else if (loggedUser.getAgence() != null || authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_AGENCE"))) {
//                ON GERE LE PROFIL AGENCE
                System.out.println("Agence");
                apurements = apurementService.findByAgence(banque, loggedUser.getAgence(), max, page, isApured, StatusTransaction.APUREMENT_WAITING_FILES);
            }
        }

        apurements.forEach(apurement -> {
            apurement.setJoursRestant(JoursRestant.getJoursRestant(apurement));
        });

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

    @Secured({"ROLE_ADMIN", "ROLE_TRADE_DESK", "ROLE_SUPERADMIN", "ROLE_SUPERUSER", "ROLE_CONTROLLER"})
    @GetMapping({"/apurments-annules", "/apurments-annules/page={page}"})
    public String getApurementsAnnules(@RequestParam(value = "page", required = false) Integer page, Model model, Principal principal, Authentication authentication) {

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

        if (loggedUser.getAgence() == null || authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"))
                || authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TRADE_DESK"))
        ) {
            apurements = apurementService.getAllByStatusAndBanqueApurements(banque, StatusTransaction.APUREMENT_ANULER, max, page);
        }
//        else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_AGENCE"))) {
        else {
//          ON GERE LE PROFIL AGENCE
            apurements = apurementService.findByAgence(banque, loggedUser.getAgence(), max, page, false, StatusTransaction.APUREMENT_ANULER);
        }

        apurements.forEach(apurement -> {
            apurement.setJoursRestant(JoursRestant.getJoursRestant(apurement));
        });

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
        String uri = "/apurments-annules/page={page}";
        model.addAttribute("uri", uri);
        model.addAttribute("das", "apan");
        model.addAttribute("exportUri", "/" + banque.getId() + "-export/apurments?apured=false");
        model.addAttribute("searchUri", "/search-apurements");

        return "apurements/index";
    }

    @Secured({"ROLE_ADMIN", "ROLE_TRADE_DESK", "ROLE_SUPERADMIN", "ROLE_SUPERUSER", "ROLE_CONTROLLER"})
    @GetMapping({"/trans-finaciere-attente", "/trans-finaciere-attente/page={page}"})
    public String getTransactonFinanciereApurement(@RequestParam(value = "page", required = false) Integer page, Model model, Principal principal, Authentication authentication) {

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

        if (loggedUser.getAgence() == null || authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"))
                || authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TRADE_DESK"))
        ) {
            apurements = apurementService.getAllByStatusAndBanqueApurements(banque, StatusTransaction.APUREMENT_ANULER, max, page);
        }
//        else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_AGENCE"))) {
        else {
//          ON GERE LE PROFIL AGENCE
            apurements = apurementService.findByAgence(banque, loggedUser.getAgence(), max, page, false, StatusTransaction.APUREMENT_ANULER);
        }

        apurements.forEach(apurement -> {
            apurement.setJoursRestant(JoursRestant.getJoursRestant(apurement));
        });

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
        String uri = "/apurments-annules/page={page}";
        model.addAttribute("uri", uri);
        model.addAttribute("das", "apan");
        model.addAttribute("exportUri", "/" + banque.getId() + "-export/apurments?apured=false");
        model.addAttribute("searchUri", "/search-apurements");

        return "apurements/index";
    }

    @Secured({"ROLE_ADMIN", "ROLE_TRADE_DESK", "ROLE_AGENCE", "ROLE_SUPERADMIN", "ROLE_SUPERUSER", "ROLE_CONTROLLER"})
    @GetMapping({"/apurments-rejetes", "/apurments-rejetes/page={page}"})
    public String getApurementsRejetes(@PathVariable(value = "page", required = false) Integer page, Model model, Principal principal, Authentication authentication) {

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

        if (loggedUser.getAgence() == null || authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"))
                || authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TRADE_DESK"))
        ) {
            apurements = apurementService.getAllByStatusAndBanqueApurements(banque, StatusTransaction.APUREMENT_REJETER, max, page);
        }
//        else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_AGENCE"))) {
        else {
//                ON GERE LE PROFIL AGENCE
            System.out.println("Agence");
            apurements = apurementService.findByAgence(banque, loggedUser.getAgence(), max, page, false, StatusTransaction.APUREMENT_REJETER);
        }

        apurements.forEach(apurement -> {
            apurement.setJoursRestant(JoursRestant.getJoursRestant(apurement));
        });

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
        String uri = "/apurments-rejetes/page={page}";
        model.addAttribute("uri", uri);
        model.addAttribute("das", "apr");
        model.addAttribute("exportUri", "/" + banque.getId() + "-export/apurments?apured=false");
        model.addAttribute("searchUri", "/search-apurements");

        return "apurements/index";
    }

    @Secured({"ROLE_ADMIN", "ROLE_TRADE_DESK", "ROLE_SUPERADMIN", "ROLE_SUPERUSER", "ROLE_CONTROLLER"})
    @GetMapping({"/apurments-treasury-approved", "/apurments-apurments-treasury-approved/page={page}"})
    public String getTransactionsValideesParLaTresorerie(@PathVariable(value = "page", required = false) Integer page, Model model, Principal principal, Authentication authentication) {

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
        apurements = apurementService.findByAgence(banque, loggedUser.getAgence(), max, page, false, StatusTransaction.APUREMENT_WAITING_DATE);
        if (loggedUser.getAgence() == null || authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"))
                || authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TRADE_DESK"))
        ) {
            apurements = apurementService.getApurementsTreasurySend(banque, max, page, isApured, StatusTransaction.APUREMENT_WAITING_DATE);
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
        String uri = "/apurments-treasury-approved/page={page}";
        model.addAttribute("uri", uri);
        model.addAttribute("das", "apwd");
        model.addAttribute("exportUri", "/" + banque.getId() + "-export/apurments?apured=false");
        model.addAttribute("searchUri", "/search-apurements");

        return "apurements/index";
    }


    @Secured({"ROLE_ADMIN", "ROLE_CLIENT", "ROLE_TREASURY_OPS", "ROLE_AGENCE", "ROLE_TRADE_DESK", "ROLE_SUPERADMIN", "ROLE_SUPERUSER", "ROLE_CONTROLLER"})
    @GetMapping({"/apurments-all", "/apurments-all/page={page}"})
    public String getAllApurements(@PathVariable(value = "page", required = false) Integer page, Model model, Principal principal, Authentication authentication) {

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
        boolean isApured = true;
        if (loggedUser.getClient() != null) {
            apurements = apurementService.getApurements(banque, loggedUser.getClient(), max, page, isApured);
        } else {
            if (loggedUser.getAgence() != null || authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_AGENCE"))) {
                apurements = apurementService.getAgenceApurements(banque, loggedUser.getAgence(), max, page, isApured);
            } else
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

    @Secured({"ROLE_SUPERADMIN", "ROLE_SUPERUSER", "ROLE_CONTROLLER"})
    @GetMapping({"apurments-expired", "apurments-expired/page={page}",})
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
        String uri = "/apurments-expired/page={page}";
        model.addAttribute("uri", uri);
        model.addAttribute("das", "ape");
        model.addAttribute("exportUri", "/" + banque.getId() + "-export/apurments?expired=true");
        model.addAttribute("searchUri", "/search-apurements");

        return "apurements/index";
    }

    @Secured({"ROLE_ADMIN", "ROLE_TREASURY_OPS", "ROLE_AGENCE", "ROLE_TRADE_DESK", "ROLE_SUPERADMIN", "ROLE_SUPERUSER", "ROLE_CONTROLLER"})
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

    @Secured({"ROLE_ADMIN", "ROLE_TREASURY_OPS", "ROLE_AGENCE", "ROLE_TRADE_DESK", "ROLE_SUPERADMIN", "ROLE_SUPERUSER", "ROLE_CONTROLLER"})
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

    @Secured({"ROLE_ADMIN", "ROLE_TREASURY_OPS", "ROLE_AGENCE", "ROLE_TRADE_DESK", "ROLE_SUPERADMIN", "ROLE_SUPERUSER", "ROLE_CONTROLLER"})
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

    @Secured({"ROLE_ADMIN", "ROLE_CLIENT", "ROLE_TREASURY_OPS", "ROLE_AGENCE", "ROLE_TRADE_DESK", "ROLE_SUPERADMIN", "ROLE_SUPERUSER", "ROLE_CONTROLLER"})
    @GetMapping({"/transactions-fina/rejet/operation", "/transactions-fina/rejet/operation/page={page}"})
    public String getTransactionsFinanRejet(@PathVariable(value = "page", required = false)
                                                    Integer page, Model model, Principal principal) {

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

        Page<TransFinanciere> transFinancieres;
        boolean isApured = true;

        transFinancieres = apurementService.getTransFinancierFrApurements(-111, max, page, isApured);

        model.addAttribute("apurements", transFinancieres.getContent());
        model.addAttribute("currentPage", transFinancieres.getNumber() + 1);
        model.addAttribute("nbPages", transFinancieres.getTotalPages());
        int nbPages = transFinancieres.getTotalPages();
        if (nbPages > 1) {
            int[] pages = new int[nbPages];
            for (int i = 0; i < nbPages; i++) {
                pages[i] = i + 1;
                System.out.println(i);
            }
            model.addAttribute("pages", pages);
        }

        model.addAttribute("dash", "transfinanciere");
        String uri = "/transactions-fina/rejet/operation/page={page}";
        model.addAttribute("uri", uri);
        model.addAttribute("das", "trf");
        model.addAttribute("exportUri", "/" + banque.getId() + "-export/apurments?apured=false");

        return "apurements/apureFinancier";
    }

    @Secured({"ROLE_ADMIN", "ROLE_CLIENT", "ROLE_TREASURY_OPS", "ROLE_AGENCE", "ROLE_TRADE_DESK", "ROLE_SUPERADMIN", "ROLE_SUPERUSER", "ROLE_CONTROLLER"})
    @GetMapping({"/transactions-financ/fin/operation", "/transactions-financ/fin/operation/page={page}"})
    public String getTransactionsFinanValidees(@PathVariable(value = "page", required = false)
                                                       Integer page, Model model, Principal principal) {

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

        Page<TransFinanciere> transFinancieres;
        boolean isApured = false;

        transFinancieres = apurementService.getTransFinancierFrApurements(111, max, page, isApured);

        model.addAttribute("apurements", transFinancieres.getContent());
        model.addAttribute("currentPage", transFinancieres.getNumber() + 1);
        model.addAttribute("nbPages", transFinancieres.getTotalPages());
        int nbPages = transFinancieres.getTotalPages();
        if (nbPages > 1) {
            int[] pages = new int[nbPages];
            for (int i = 0; i < nbPages; i++) {
                pages[i] = i + 1;
                System.out.println(i);
            }
            model.addAttribute("pages", pages);
        }

        model.addAttribute("dash", "transfinanciere");
        String uri = "/transactions-financ/fin/operation/page={page}";
        model.addAttribute("uri", uri);
        model.addAttribute("das", "trm");
        model.addAttribute("exportUri", "/" + banque.getId() + "-export/apurments?apured=false");

        return "apurements/apureFinancier";
    }

    @Secured({"ROLE_ADMIN", "ROLE_CLIENT", "ROLE_TREASURY_OPS", "ROLE_AGENCE", "ROLE_TRADE_DESK", "ROLE_SUPERADMIN", "ROLE_SUPERUSER", "ROLE_CONTROLLER"})
    @GetMapping({"/transactions-fin-attente", "/transactions-fin-attente/page={page}"})
    public String getTransactionsValidees(@PathVariable(value = "page", required = false)
                                                  Integer page, Model model, Principal principal) {

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

        Page<TransFinanciere> transFinancieres;
        boolean isApured = false;

        transFinancieres = apurementService.getTransFinancierFrApurements(-111, max, page, isApured);

        model.addAttribute("apurements", transFinancieres.getContent());
        model.addAttribute("currentPage", transFinancieres.getNumber() + 1);
        model.addAttribute("nbPages", transFinancieres.getTotalPages());
        int nbPages = transFinancieres.getTotalPages();
        if (nbPages > 1) {
            int[] pages = new int[nbPages];
            for (int i = 0; i < nbPages; i++) {
                pages[i] = i + 1;
                System.out.println(i);
            }
            model.addAttribute("pages", pages);
        }

        model.addAttribute("dash", "transfinanciere");
        String uri = "/transactions-fin-attente/page={page}";
        model.addAttribute("uri", uri);
        model.addAttribute("das", "trf");
        model.addAttribute("exportUri", "/" + banque.getId() + "-export/apurments?apured=false");

        return "apurements/apureFinancier";
    }

    @GetMapping("/save/date-debit/transFin")
    public String addDateEffectiveTranFin(@RequestParam("dateeffctive") String dateEffective,
                                          @RequestParam("transactions") String transa) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String decodedTransactions = java.net.URLDecoder.decode(transa, java.nio.charset.StandardCharsets.UTF_8);

        // Parse the transactions parameter as an array of strings
        String[] transactionArray;
        try {
            transactionArray = objectMapper.readValue(decodedTransactions, String[].class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "Error parsing transactions";
        }
        // Parse the input string to LocalDate
        List<String> trans = Arrays.asList(transactionArray);
        trans.forEach(t -> {
            TransFinanciere transFinanciere = transFinanciereRepository.getById(Long.valueOf(t));
            LocalDate date = LocalDate.parse(dateEffective, inputFormatter);
            transFinanciere.setStatus(111);
            transFinanciere.setDateEffective(Date.valueOf(date));

            transFinanciereRepository.save(transFinanciere);
        });

        return "redirect:/transactions-fin-attente";
    }

    @GetMapping("/annuler/trans/financiere")
    public String renvoyerTransaction(@RequestParam("id") Long id, @RequestParam(name = "motif",
            required = false) String motif) {

        TransFinanciere transFinanciere = transFinanciereRepository.getById(id);
        Transaction transaction = transFinanciere.getTransaction();
        transFinanciere.setRejected(true);
        transFinanciere.setMotifReject(motif);
        transaction.setStatut(StatusTransaction.WAITING);
        transaction.setMotif("Rejet Niveau final , avec message : " + motif);
        transaction.setRenvoye(true);

        transactionService.saveTransaction(transaction);
        transFinanciereRepository.save(transFinanciere);
        return "redirect:/transactions-fin-attente";
    }


    @GetMapping("/add-apurement-effective-date/multiples")
    public ModelAndView setApurementEffectiveDate(@RequestParam("transactions") String apurements,
                                                  @RequestParam("dateeffctive") String dateStr) {
        String decodedTransactions = java.net.URLDecoder.decode(apurements, java.nio.charset.StandardCharsets.UTF_8);

        String[] transactionArray = new String[0];
        try {
            transactionArray = objectMapper.readValue(decodedTransactions, String[].class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        HashMap<String, Object> hashMap = new HashMap<>();
        final String[] errorMessage = {null};
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDate date = LocalDate.parse(dateStr, inputFormatter);


        final boolean[] isChanged = {false};
        List<String> trans = Arrays.asList(transactionArray);
        LocalDateTime localDateTime = date.atStartOfDay();
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());

        java.util.Date finalDate =  Date.from(zonedDateTime.toInstant());
        trans.forEach(t -> {
            Apurement apurement = apurementRepositoy.findById(Long.valueOf(t)).get();
            Transaction transaction = apurement.getTransaction();

            if (finalDate != null && transaction != null && (transaction.getDomiciliation() != null || transaction.getUseManyDomiciliations())) {
                // On commence par nous rassurer que la domiciliation a un montant suffisant
                if (transaction.getUseManyDomiciliations()) {
                    for (DomiciliationTransaction dt : transaction.getDomiciliationTransactions()) {
                        float newAmontDomiciliation = dt.getDomiciliation().getMontantRestant() - dt.getMontant();
                        if (newAmontDomiciliation < 0) {
                            hashMap.put("errorMessage", "Opération impossible le montant de l'une des domiciliations utilisées est insuffisant");
                            hashMap.put("isChanged", false);
                        }
                        dt.getDomiciliation().setMontantRestant(newAmontDomiciliation);
                    }
                } else {
                    float newAmontDomiciliation = transaction.getDomiciliation().getMontantRestant() - transaction.getMontant();
                    if (newAmontDomiciliation < 0) {
                        hashMap.put("errorMessage", "Opération impossible le montant de la domiciliation est insuffisant");
                        hashMap.put("isChanged", false);
                    }
                    // On décrémente la domiciliation
                    transaction.getDomiciliation().setMontantRestant(newAmontDomiciliation);
                }
                // on determine la date d'expiration
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(finalDate);
                if (transaction.getTypeDeTransaction().getType() != null) {
                    if (transaction.getTypeDeTransaction().getType().equals(StatusTransaction.IMP_BIENS)) {
                        calendar.add(Calendar.DATE, StatusTransaction.DELAY_TRANSACTION_IMPORTATION_BIENS);
                    } else if (transaction.getTypeDeTransaction().getType().equals(StatusTransaction.IMP_SERVICES)) {
                        calendar.add(Calendar.DATE, StatusTransaction.DELAY_TRANSACTION_IMPORTATION_SERVICES);
                    } else {
                        calendar.add(Calendar.DATE, StatusTransaction.DELAY_TRANSACTION_IMPORTATION_BIENS);
                    }
                } else {
                    calendar.add(Calendar.DATE, StatusTransaction.DELAY_TRANSACTION_IMPORTATION_BIENS);
                }

                // On met à jour l'apurement
                apurement.setDateExpiration(calendar.getTime());
                apurement.setStatus(StatusTransaction.APUREMENT_WAITING_FILES);
                apurement.setDateEffective(finalDate);
                apurementService.saveApurement(apurement);
                isChanged[0] = true;
                hashMap.put("dateText", new SimpleDateFormat("dd-MM-yyyy").format(finalDate));
                hashMap.put("dataValue", new SimpleDateFormat("yyyy-MM-dd").format(finalDate));
            } else if (errorMessage[0] == null && transaction != null && transaction.getDomiciliation() == null) {
                errorMessage[0] = "Action impossible ! Cette transaction n'utilise aucune domiciliation";
            }
        });
        hashMap.put("errorMessage", errorMessage[0]);
        hashMap.put("isChanged", isChanged[0]);
        return new ModelAndView("apurements/index", hashMap);

    }

}
