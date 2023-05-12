package com.akouma.veyuzwebapp.controller;

import com.akouma.veyuzwebapp.dto.ClientDto;
import com.akouma.veyuzwebapp.form.DomiciliationForm;
import com.akouma.veyuzwebapp.form.ImportFileForm;
import com.akouma.veyuzwebapp.model.AppUser;
import com.akouma.veyuzwebapp.model.Banque;
import com.akouma.veyuzwebapp.model.Client;
import com.akouma.veyuzwebapp.model.Domiciliation;
import com.akouma.veyuzwebapp.service.*;
import com.akouma.veyuzwebapp.utils.CheckSession;
import com.akouma.veyuzwebapp.utils.CryptoUtils;
import com.akouma.veyuzwebapp.validator.DomiciliationValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

@Controller
public class DomiciliationController {

    private final int max = 10;
    private final int page = 1;
    @Autowired
    HttpSession session;
    @Autowired
    private DomiciliationService domiciliationService;
    @Autowired
    private TypeDeTransactionService typeDeTransactionService;
    @Autowired
    private DeviseService deviseService;
    @Autowired
    private DomiciliationValidator domiciliationValidator;
    @Autowired
    private UserService userService;
    @Autowired
    private ClientService clientService;
    @Autowired
    private CryptoUtils cryptoUtils;

    public DomiciliationController(HttpSession session) {
        this.session = session;
    }

    @InitBinder
    protected void initBinder(WebDataBinder dataBinder) {
        Object target = dataBinder.getTarget();
        if (target == null) {
            return;
        }

        if (target.getClass() == DomiciliationForm.class) {
            dataBinder.setValidator(domiciliationValidator);
        }
    }


    @Secured({"ROLE_MACKER", "ROLE_CHECKER", "ROLE_AGENCE", "ROLE_CHECKER_TO", "ROLE_MAKER_TO"})
    @GetMapping({"/domiciliations", "/domiciliations/page={page}"})
    public String showDomiciliations(
            @PathVariable(value = "page", required = false) Integer page,
            Authentication authentication,
            Model model, Principal principal) {

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session) || principal == null) {
            return "redirect:/";
        }
        if (page != null && page <= 0) {
            return "error/404";
        }

        Banque banque = (Banque) session.getAttribute("banque");
        if (page == null) {
            page = this.page;
        }
        page--;

        AppUser loggedUser = userService.getLoggedUser(principal);

        Client client = loggedUser.getClient();
        if (authentication.getAuthorities().contains("ROLE_CLIENT")) {
            if (loggedUser.getClient() == null || loggedUser.getClient().getBanques().isEmpty() || !loggedUser.getClient().getBanques().contains(banque)) {
                return "error/403";
            }
        }

        Page domiciliations;
        if (client == null) {
            domiciliations = domiciliationService.getDomiciliations(banque, max, page);
        } else {
            domiciliations = domiciliationService.getDomiciliationsClientBanque(banque, client, max, page);

        }

        int nbPages = domiciliations.getTotalPages();
        if (nbPages > 1) {
            int[] pages = new int[nbPages];
            for (int i = 0; i < nbPages; i++) {
                pages[i] = i + 1;
            }
            model.addAttribute("pages", pages);
        }

        System.out.println(domiciliations.getNumber());
        model.addAttribute("currentPage", domiciliations.getNumber() + 1);
        model.addAttribute("nbPages", nbPages);
        model.addAttribute("nbElements", domiciliations.getTotalElements());

        model.addAttribute("domiciliations", domiciliations);
        model.addAttribute("banque", banque);
        model.addAttribute("client", client);
        model.addAttribute("activeMenu", 5);
        model.addAttribute("actiSub", null);
        model.addAttribute("uri", "/domiciliations/page={page}");
        model.addAttribute("dash", "domi");


        return "domiciliation_list";
    }

    @Secured({"ROLE_MACKER", "ROLE_CHECKER", "ROLE_AGENCE", "ROLE_CHECKER_TO", "ROLE_MAKER_TO"})
    @GetMapping("/domiciliation-{id}/details")
    public String showDomiciliationDetails(
            @PathVariable("id") Domiciliation domiciliation,
            Model model, RedirectAttributes redirectAttributes,
            Principal principal, Authentication authentication) {

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session) || principal == null) {
            return "redirect:/";
        }

        Client client = null;
        AppUser loggedUser = userService.getLoggedUser(principal);

        if (authentication.getAuthorities().contains("ROLE_CLIENT")) {
            client = loggedUser.getClient();
            if (client == null || !domiciliation.getClient().equals(client)) {
                return "error/403";
            }
        }

        if (authentication.getAuthorities().contains("ROLE_ADMIN") && (domiciliation.getBanque() == null || !domiciliation.getBanque().equals(loggedUser.getBanque()))) {
            return "error/403";
        }

        model.addAttribute("domiciliation", domiciliation);
        model.addAttribute("dash", "domi");

        return "domiciliation_details";
    }

    @Secured({"ROLE_CLIENT", "ROLE_ADMIN", "ROLE_SUPERADMIN"})
    @GetMapping({"/domiciliations/new"})
    public String showDomiciliationForm(
            Model model,
            Principal principal, Authentication authentication) {

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session) || principal == null) {
            return "redirect:/";
        }

        Banque banque = (Banque) session.getAttribute("banque");

        AppUser loggedUser = userService.getLoggedUser(principal);
        Client client = loggedUser.getClient();

        System.out.println(authentication.getAuthorities());
        // SI TON COMPTE A LE ROLE DE CLIENT ET QUE TON COMPTE NE FAIT PAS DE REFERENCE A L'ENTITE CLIENT ON REVOIE UNE ERREUR 403
        if (authentication.getAuthorities().contains("ROLE_CLIENT")) {
            if (client == null) {
                return "error/403";
            }
        }

        // SI TON COMPTE RENVOIE A UN COMPTE ADMIN OU SUPERADMIN ET QUE TU N'ES LIE A AUCUNE BANQUE ALORS ON RENVOIE UNE ERREUR 403
        if (authentication.getAuthorities().contains("ROLE_ADMIN") || authentication.getAuthorities().contains("ROLE_SUPERADMIN")) {
            if (!banque.equals(loggedUser.getBanque())) {
                return "error/403";
            }
        }
        DomiciliationForm domiciliationForm = new DomiciliationForm(banque);
        model.addAttribute("domiciliationForm", domiciliationForm);
        setModelData(model, banque, client);
        model.addAttribute("dash", "domiciliation");
//        model.addAttribute("domiciliation",domiciliationService.getDomiciliationsClient(banque,client));

        return "domiciliation_form";
    }

    @Secured({"ROLE_MACKER", "ROLE_CHECKER", "ROLE_AGENCE", "ROLE_CHECKER_TO", "ROLE_MAKER_TO"})
    @GetMapping({"/domiciliations/new/{id}"})
    public String showDomiciliationFormAd(
            Model model,
            @PathVariable("id") String id) throws Exception {
        Client client = clientService.findById(cryptoUtils.decrypt(id));
        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER

        Banque banque = (Banque) session.getAttribute("banque");

        // SI TON COMPTE A LE ROLE DE CLIENT ET QUE TON COMPTE NE FAIT PAS DE REFERENCE A L'ENTITE CLIENT ON REVOIE UNE ERREUR 403


        DomiciliationForm domiciliationForm = new DomiciliationForm(banque);
        model.addAttribute("domiciliationForm", domiciliationForm);
        setModelData(model, banque, client);
        model.addAttribute("dash", "domiciliation");
//        model.addAttribute("domiciliation",domiciliationService.getDomiciliationsClient(banque,client));

        return "domiciliation_form";
    }


    private void setModelData(Model model, Banque banque, Client client) {
        Collection<Domiciliation> domiciliations;
        if (client == null) {
            domiciliations = domiciliationService.getDomiciliations(banque, 15, 0).getContent();
        } else {
            domiciliations = client.getDomiciliations();
        }
        model.addAttribute("banque", banque);
        model.addAttribute("client", client);
        model.addAttribute("typesDeTransaction", typeDeTransactionService.getAllTypesTransaction());
        model.addAttribute("devises", deviseService.getAll());
        model.addAttribute("clients", clientService.getClients(banque));
        model.addAttribute("domiciliations", domiciliations);
        model.addAttribute("activeMenu", 5);
        model.addAttribute("actiSub", null);
        model.addAttribute("dash", "domiciliation");

    }


    @Secured({"ROLE_MACKER", "ROLE_CHECKER", "ROLE_AGENCE", "ROLE_CHECKER_TO", "ROLE_MAKER_TO"})
    @PostMapping("/save-domiciliation")
    public String saveDomiciliation(
            @ModelAttribute @Validated DomiciliationForm domiciliationForm,
            BindingResult result,
            Model model, RedirectAttributes redirectAttributes, Principal principal) throws Exception {

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session) || principal == null) {
            return "redirect:/";
        }


        if (result.hasErrors()) {
            System.out.println(result);
            setModelData(model, domiciliationForm.getBanque(),null);
            String msg = "Une erreur s'est glissée dans votre formulaire de données . Erreur : " + result;
            redirectAttributes.addFlashAttribute("flashMessage", msg);

            return "domiciliation_form";
        }

        if (domiciliationForm.getDomiciliation() == null || domiciliationForm.getDomiciliation().getId() == null) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, 365);
            domiciliationForm.setDateExpiration(calendar.getTime());
            domiciliationForm.setMontantRestant(domiciliationForm.getMontant());
            System.out.println(domiciliationForm.getDateExpiration());
        }
        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(domiciliationForm.getDateCreationStr());
        domiciliationForm.setDateCreation(date);

        Domiciliation saved = domiciliationService.saveDomiciliation(domiciliationForm);
        String redirectUri = "redirect:/domiciliation-" + saved.getId() + "/details";

        String msg = "La domiciliation que vous avez ajoute a été enregistrée avec success. Veuillez  ajouter la documentation necessaire ";
        redirectAttributes.addFlashAttribute("flashMessage", msg);
        model.addAttribute("dash", "domi");

        return redirectUri;
    }


    @Secured({"ROLE_MACKER", "ROLE_CHECKER", "ROLE_CHECKER_TO", "ROLE_MAKER_TO"})
    @GetMapping("/admin/domiciliations/import")
    public String showImportDomiciliationForm(
            Model model,
            RedirectAttributes redirectAttributes) {

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session)) {
            return "redirect:/";
        }

        Banque banque = (Banque) session.getAttribute("banque");

        model.addAttribute("banque", banque);
        model.addAttribute("isImport", true);
        model.addAttribute("postUri", "/post-import-domiciliations");
        model.addAttribute("importFileForm", new ImportFileForm(banque));
        model.addAttribute("formTitle", "importer les domiciliations");
        model.addAttribute("dash", "params");
        model.addAttribute("setItem", "domi");

        return "parametres";
    }


    @Secured({"ROLE_MACKER", "ROLE_CHECKER", "ROLE_CHECKER_TO", "ROLE_MAKER_TO"})
    @PostMapping("/post-import-domiciliations")
    public String importDomiciliations(
            @ModelAttribute ImportFileForm importFile,
            RedirectAttributes redirectAttributes, HttpServletRequest request) throws IOException {


        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session)) {
            return "redirect:/";
        }

        String msg = domiciliationService.importData(importFile, request);
        redirectAttributes.addFlashAttribute("flashMessage", msg);

        return "redirect:/domiciliations";

    }

}
