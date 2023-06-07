package com.akouma.veyuzwebapp.controller;

import com.akouma.veyuzwebapp.dto.ClientDto;
import com.akouma.veyuzwebapp.form.ClientForm;
import com.akouma.veyuzwebapp.form.ImportFileForm;
import com.akouma.veyuzwebapp.model.Agence;
import com.akouma.veyuzwebapp.model.AppUser;
import com.akouma.veyuzwebapp.model.Banque;
import com.akouma.veyuzwebapp.model.Client;
import com.akouma.veyuzwebapp.service.AgenceService;
import com.akouma.veyuzwebapp.service.ClientService;
import com.akouma.veyuzwebapp.service.PaysService;
import com.akouma.veyuzwebapp.service.UserService;
import com.akouma.veyuzwebapp.utils.CheckSession;
import com.akouma.veyuzwebapp.utils.CryptoUtils;
import com.akouma.veyuzwebapp.validator.ClientValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.annotation.Secured;
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
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ClientController {

    private final int max = 10;
    private final int page = 1;
    @Autowired
    HttpSession session;
    @Autowired
    private ClientService clientService;
    @Autowired
    private ClientValidator clientValidator;
    @Autowired
    private PaysService paysService;
    @Autowired
    private UserService userService;
    @Autowired
    private CryptoUtils cryptoUtils;
    @Autowired
    private AgenceService agenceService;

    public ClientController(HttpSession session) {
        this.session = session;
    }

    @InitBinder
    protected void initBinder(WebDataBinder dataBinder) {
        Object target = dataBinder.getTarget();
        if (target == null) {
            return;
        }

        if (target.getClass() == ClientForm.class) {
            System.out.println("Dans le binder");
            dataBinder.setValidator(clientValidator);
        }
    }

    @GetMapping({"/clients", "/clients/page={page}", "/clients/{agence}/page={page}", "/clients/{agence}"})
    public String showClientsList(
            @PathVariable(value = "page", required = false) Integer page,
            @PathVariable(required = false, value = "agence") Agence agence,
            Model model, Principal principal) {

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session)) {
            return "redirect:/";
        }
        AppUser loggedUser = userService.getLoggedUser(principal);

        Banque banque = (Banque) session.getAttribute("banque");

        if (page != null && page <= 0) {
            return "error/404";
        }

        if (page == null) {
            page = this.page;
        }
        page--;
        Page pagesClients = null;
        if (loggedUser.getAgence() != null) {
            pagesClients = clientService.findAllClientAgence(loggedUser.getAgence(), banque, 50, page);

        } else {
            pagesClients = clientService.getClients(banque, 50, page);

        }

        int nbPages = pagesClients.getTotalPages();
        if (nbPages > 1) {
            int[] pages = new int[nbPages];
            for (int i = 0; i < nbPages; i++) {
                pages[i] = i + 1;
                System.out.println(i);
            }
            model.addAttribute("pages", pages);
        }
        model.addAttribute("clientTransactionUri", "/transactions/{client_id}-client");
        model.addAttribute("initierTransactionUri", "/transaction/new/{client_id}/client/{type}");
        List<Client> clientList = pagesClients.getContent();
        List<ClientDto> clientDtos = clientList.stream().map(
                client -> {
                    ClientDto client1 = new ClientDto();
                    client1.setTelephone(client.getTelephone());
                    try {
                        client1.setId(cryptoUtils.encrypt(client.getId()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
//                    client1.setUser(client.getUser());
                    client1.setDomiciliations(client.getDomiciliations());
                    client1.setKyc(client.getKyc());
                    client1.setDenomination(client.getDenomination());
                    client1.setReference(client.getReference());
                    client1.setNumeroContribuable(client.getNumeroContribuable());
                    client1.setTypeClient(client.getTypeClient());
                    client1.setAgence(client.getAgence());
                    return client1;
                }
        ).collect(Collectors.toList());
        model.addAttribute("clients", clientDtos);
        model.addAttribute("nbPages", nbPages);
        model.addAttribute("currentPage", pagesClients.getNumber() + 1);
        model.addAttribute("banque", banque);
        model.addAttribute("user", userService.getLoggedUser(principal));
        model.addAttribute("showClientList", true);
        model.addAttribute("profile", null);
        if (agence != null) {
            model.addAttribute("uri", "/clients/" + agence.getId() + "/page={page}");

        } else {
            model.addAttribute("uri", "/clients/page={page}");

        }
        model.addAttribute("nbElements", pagesClients.getTotalElements());

        model.addAttribute("dash", "client");
        model.addAttribute("setItem", "clients");

        return "parametres";
    }

    @PostMapping("/search-clients-veyuz")
    public String searcheClientsList(
            Model model, Principal principal, @RequestParam("telephone") String telephone) {

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session)) {
            return "redirect:/";
        }

        Banque banque = (Banque) session.getAttribute("banque");

        Iterable<ClientDto> Clients = clientService.searchClients(banque, telephone);

        model.addAttribute("clientTransactionUri", "/transactions/{client_id}-client");
        model.addAttribute("initierTransactionUri", "/transaction/new/{client_id}/client/{type}");

        model.addAttribute("clients", Clients);
        model.addAttribute("banque", banque);
        model.addAttribute("user", userService.getLoggedUser(principal));
        model.addAttribute("showClientList", true);
        model.addAttribute("profile", null);
        model.addAttribute("uri", "/clients/page={page}");
        model.addAttribute("dash", "params");

        return "parametres";
    }

    @PostMapping("/search-clients-veyuz2")
    public String searcheClientsListbyName(
            Model model, Principal principal, @RequestParam("telephone") String telephone) {

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session)) {
            return "redirect:/";
        }
        Iterable<ClientDto> Clients = null;
        Banque banque = (Banque) session.getAttribute("banque");
        //     AppUser loggedUser = userService.getLoggedUser(principal);

//        if (loggedUser.getAgence() != null) {
//            Clients = clientService.searchClientsAgenceByName(banque, loggedUser.getAgence(), telephone);
//        } else {
        Clients = clientService.searchClientsByName(banque, telephone);
//        }

        model.addAttribute("clientTransactionUri", "/transactions/{client_id}-client");
        model.addAttribute("initierTransactionUri", "/transaction/new/{client_id}/client/{type}");

        model.addAttribute("clients", Clients);
        model.addAttribute("banque", banque);
        model.addAttribute("user", userService.getLoggedUser(principal));
        model.addAttribute("showClientList", true);
        model.addAttribute("profile", null);
        model.addAttribute("uri", "/clients/page={page}");
        model.addAttribute("dash", "params");

        return "parametres";
    }

    @Secured({"ROLE_MACKER", "ROLE_CHECKER", "ROLE_AGENCE", "ROLE_CHECKER_TO", "ROLE_MAKER_TO"})
    @GetMapping({"/clients/new", "/clients-{client_id}/edit"})
    public String newClient(
            @PathVariable(value = "client_id", required = false) String clt,
            Model model, Principal principal) throws Exception {
        Client client = null;
        if (clt != null) {
            client = clientService.findById(cryptoUtils.decrypt(clt));
        }
        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session)) {
            return "redirect:/";
        }

        Banque banque = (Banque) session.getAttribute("banque");
        AppUser loggedUser = userService.getLoggedUser(principal);

        if (loggedUser.getClient() == null && !banque.equals(loggedUser.getBanque())) {
            return "error/403";
        }
        ClientForm clientForm = new ClientForm();
        if (client != null) {
            clientForm = new ClientForm(client);
        }

        clientForm.setBanque(banque);
        setModelData(model, client, banque, principal);
        model.addAttribute("clientForm", clientForm);
        model.addAttribute("editClient", "editClient");
        model.addAttribute("dash", "client");
        model.addAttribute("setItem", "clients");
        model.addAttribute("agences", agenceService.findAllAgences());

        return "parametres";
    }

    @Secured({"ROLE_MACKER", "ROLE_CHECKER", "ROLE_AGENCE", "ROLE_CHECKER_TO", "ROLE_MAKER_TO"})
    @PostMapping("/banque-customers-save")
    public String saveClientOnBank(
            @ModelAttribute @Validated ClientForm clientForm,
            BindingResult result,
            Model model, Principal principal,
            RedirectAttributes redirectAttributes, HttpServletRequest request) {

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session) || principal == null) {
            return "redirect:/";
        }
        System.out.println("<<<<<====>13");

        model.addAttribute("dash", "params");

        Banque banque = (Banque) session.getAttribute("banque");
        AppUser loggedUser = userService.getLoggedUser(principal);

        if (!loggedUser.getBanque().equals(banque) || !banque.equals(clientForm.getBanque())) {
            return "error/403";
        }
        System.out.println("==>>>>>==>13");

        clientForm.setBanque(banque);
        model.addAttribute("banque", clientForm.getBanque());
        model.addAttribute("dash", "client");
        model.addAttribute("setItem", "clients");
        if (result.hasErrors()) {
            setModelData(model, clientForm.getClient(), clientForm.getBanque(), principal);
            return "parametres";
        }
        Client newClient = null;
        try {
            newClient = clientService.saveClientForm(clientForm, request);
        } catch (Exception e) {

            e.printStackTrace();
            setModelData(model, clientForm.getClient(), clientForm.getBanque(), principal);

            model.addAttribute("errorMessage", "Error: " + e.getMessage());
            return "parametres";
        }

        String msg = "Le client " + newClient.getDenomination() + " a été ajouté";
        redirectAttributes.addFlashAttribute("flashMessage", msg);

        return "redirect:/clients";
    }

    private void setModelData(Model model, Client client, Banque banque, Principal principal) {
        model.addAttribute("client", client);
        model.addAttribute("banque", banque);
        model.addAttribute("user", userService.getLoggedUser(principal));
        model.addAttribute("editClient", true);
    }

    @Secured({"ROLE_MACKER", "ROLE_CHECKER", "ROLE_AGENCE", "ROLE_CHECKER_TO", "ROLE_MAKER_TO"})
    @GetMapping("/clients-{id}/manage")
    public String blockClient(@PathVariable("id") String clt, Principal principal, RedirectAttributes redirectAttributes
            , Model model) throws Exception {

        Client client = clientService.findById(cryptoUtils.decrypt(clt));
        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session)) {
            return "redirect:/";
        }
        model.addAttribute("dash", "client");
        Banque banque = (Banque) session.getAttribute("banque");
        AppUser loggedUser = userService.getLoggedUser(principal);

        if (!loggedUser.getBanque().equals(banque) || client.getBanques().isEmpty() || !client.getBanques().contains(banque)) {
            return "error/403";
        }

        boolean isEnable = !client.getUser().isEnable();
        clientService.setEnable(isEnable, client);
        System.out.println(banque.getName());

        String msg = "Vous venez de bloquer le client " + client.getDenomination() + " !!!";
        if (isEnable) {
            msg = "Vous venez d'activer le compte du client " + client.getDenomination() + " !!!";
        }
        redirectAttributes.addFlashAttribute("flashMessage", msg);

        return "redirect:/clients";
    }

    /**
     * Fonction d'importation des csv
     *
     * @return
     */
    @Secured({"ROLE_MACKER", "ROLE_CHECKER", "ROLE_AGENCE", "ROLE_CHECKER_TO", "ROLE_MAKER_TO"})
    @PostMapping("/post-import-customers")
    public String importClients(
            @ModelAttribute @Validated ImportFileForm importFile,
            RedirectAttributes redirectAttributes, Model model, HttpServletRequest request) throws IOException {


        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session)) {
            return "redirect:/";
        }
        Banque banque = (Banque) session.getAttribute("banque");


        model.addAttribute("dash", "client");


        clientService.importData(importFile, banque, request);


        String msg = "Vos données ont été importées avec succes ";
        redirectAttributes.addFlashAttribute("flashMessage", msg);

        return "redirect:/clients";
    }

    @GetMapping("/import-clients")
    public String showImportClientForm(Model model) {

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session)) {
            return "redirect:/";
        }

        model.addAttribute("dash", "params");
        model.addAttribute("type", "client");
        model.addAttribute("isImport", true);
        model.addAttribute("importClient", true);
        model.addAttribute("postUri", "/import");
        model.addAttribute("formTitle", "importer les clients");
        model.addAttribute("dash", "client");
        model.addAttribute("setItem", "clients");
        return "parametres";
    }

}
