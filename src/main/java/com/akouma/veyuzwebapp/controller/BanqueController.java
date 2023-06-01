package com.akouma.veyuzwebapp.controller;

import com.akouma.veyuzwebapp.dto.TransactionDto;
import com.akouma.veyuzwebapp.form.BanqueForm;
import com.akouma.veyuzwebapp.form.UserForm;
import com.akouma.veyuzwebapp.form.UserRoleForm;
import com.akouma.veyuzwebapp.model.AppUser;
import com.akouma.veyuzwebapp.model.Banque;
import com.akouma.veyuzwebapp.model.Client;
import com.akouma.veyuzwebapp.model.Transaction;
import com.akouma.veyuzwebapp.repository.ApurementRepositoy;
import com.akouma.veyuzwebapp.service.*;
import com.akouma.veyuzwebapp.utils.CheckSession;
import com.akouma.veyuzwebapp.utils.CryptoUtils;
import com.akouma.veyuzwebapp.utils.StatusTransaction;
import com.akouma.veyuzwebapp.utils.Upload;
import com.akouma.veyuzwebapp.validator.AppUserValidator;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class BanqueController {

    @Autowired
    FileStorageService fileStorageService;
    @Autowired
    DeviseService deviseService;
    @Autowired
    HttpSession session;
    @Autowired
    private BanqueService banqueService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private ApurementService apurementService;
    @Autowired
    private ClientService clientService;
    @Autowired
    private PaysService paysService;
    @Autowired
    private AppUserValidator userValidator;
    @Autowired
    private ApurementRepositoy apurementRepositoy;

    @Autowired
    private UserService userService;

    @Autowired

    public BanqueController(HttpSession session) {
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

    @Secured({"ROLE_SUPERUSER"})
    @GetMapping("/veyuz")
    public String index(Model model) {

        model.addAttribute("banques", banqueService.getbanques());
        model.addAttribute("showBanqueForm", false);
        model.addAttribute("index", true);
        session.setAttribute("l", "h");

        return "bank";
    }

    @Secured({"ROLE_SUPERUSER"})
    @GetMapping({"/veyuz/banks", "/veyuz/{id}-banks"})
    public String showBanques(@PathVariable(value = "id", required = false) Client client, Model model) {
        String pageTitle = "Gestion des banques";
        if (client != null) {
            model.addAttribute("banques", client.getBanques());
            pageTitle += " / " + client.getDenomination();
        } else {
            model.addAttribute("banques", banqueService.getbanques());
        }
        model.addAttribute("showBanqueForm", false);
        model.addAttribute("showBanksList", true);

        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("l", "b");
        model.addAttribute("sl", "lb");
        model.addAttribute("blockBank", "/veyuz/banks/{id}/block");
        model.addAttribute("activeBank", "/veyuz/banks/{id}/active");
        model.addAttribute("editBank", "/veyuz/banks/{id}/edit");
        model.addAttribute("usersUri", "/veyuz/banks/{id}/users");
        return "bank";
    }

    @Secured({"ROLE_SUPERUSER"})
    @PostMapping("/veyuz/banks/save")
    public String saveBank(@ModelAttribute BanqueForm banqueForm, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) throws IOException {
        if (true) {
//            le formulaire est valide
            deplacerLeFichier(banqueForm, request);
        } else {
            return "edit_bank_form";
        }

        String msg = "La banque a ete enregistree !";

        redirectAttributes.addFlashAttribute("flashMessage", msg);

        return "redirect:/veyuz/banks";
    }

    private void deplacerLeFichier(BanqueForm banqueForm, HttpServletRequest request) throws IOException {
        Upload upload = new Upload();
        String logoName = upload.uploadFile(banqueForm.getLogoFile(), fileStorageService, "banque_logo", request);
        Banque banque = new Banque();

        if (banqueForm.getBanque() != null) {
            banque = banqueForm.getBanque();
        }
        banque.setEmail(banqueForm.getEmail());
        banque.setName(banqueForm.getName());
        banque.setEnable(banqueForm.isEnable());
        banque.setNumeroContribuable(banqueForm.getNumeroContribuable());
        if (logoName != null || !logoName.trim().isBlank()) {
            banque.setLogo(logoName);
        }
        banque.setPays(banqueForm.getPays());
        banque.setSigle(banqueForm.getSigle());
        banque.setTelephone(banqueForm.getTelephone());
        banque.setDescription(banqueForm.getDescription());
        banqueService.save(banque);
    }

    /**
     * @param banque
     * @param model
     * @return
     */
    @Secured({"ROLE_SUPERUSER"})
    @GetMapping({"/veyuz/banks/new", "/veyuz/banks/{id}/edit"})
    public String newBank(@PathVariable(value = "id", required = false) Banque banque, Model model) {
        String cardTitle = "Modifier la banque";
        if (banque == null) {
            banque = new Banque();
            cardTitle = "Nouvelle banque";
        }
        String pageTitle = "Gestion des banques";
        model.addAttribute("banqueForm", new BanqueForm(banque));
        model.addAttribute("banque", banque);
        model.addAttribute("countries", paysService.getPays());
        model.addAttribute("l", "b");
        model.addAttribute("sl", "nb");
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("cardTitle", cardTitle);

        return "bank";
    }

    @Secured({"ROLE_SUPERUSER"})
    @GetMapping("/veyuz/banks/{id}/users")
    public String showUsersList(@PathVariable("id") Banque banque, Model model) {

        model.addAttribute("banque", banque);
        model.addAttribute("users", userService.getBanqueUsers(banque));
        model.addAttribute("usersList", true);
        model.addAttribute("newUserFormUri", "/veyuz/banks/{id}/users/new");
        model.addAttribute("editUserFormUri", "/veyuz/banks/{id}/{user_id}/users/edit");
        model.addAttribute("configAccessUri", "/form/user/{id}/access");
        model.addAttribute("postUserAccessForm", "/configure/user/access");
        model.addAttribute("blockUserUri", "/enable/user/{id}/block");
        model.addAttribute("activeUserUri", "/enable/user/{id}/active");
        UserRoleForm userRoleForm = new UserRoleForm();
        model.addAttribute("userRoleForm", userRoleForm);

        return "bank";
    }

    @Secured({"ROLE_SUPERUSER"})
    @GetMapping({"/veyuz/banks/{id}/users/new", "/veyuz/banks/{id}/{user_id}/users/edit"})
    public String showAppUserForm(@PathVariable("id") Banque banque, @PathVariable(value = "user_id", required = false) AppUser appUser, Model model) {
        if (appUser == null) {
            appUser = new AppUser();
        }
        UserForm userForm = new UserForm(appUser);
        userForm.setBanque(banque);

        userForm.setBanque(banque);
        model.addAttribute("userForm", userForm);
        model.addAttribute("banque", banque);
        model.addAttribute("showEditUserForm", true);
        model.addAttribute("saveUserFormUri", "/veyuz/save/user");

        return "bank";
    }

    @Secured({"ROLE_SUPERUSER"})
    @PostMapping("/veyuz/save/user")
    public String saveUserAction(
            HttpServletRequest request,
            @ModelAttribute @Validated UserForm userForm,
            BindingResult result,
            Model model,
            final RedirectAttributes redirectAttributes) {

        model.addAttribute("banque", userForm.getBanque());
        model.addAttribute("showEditUserForm", true);
        model.addAttribute("saveUserFormUri", "/veyuz/save/user");
        if (result.hasErrors()) {
            return "bank";
        }
        AppUser newAppUser = null;
        try {
            newAppUser = userService.saveUser(userForm, request);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error: " + e.getMessage());
            return "bank";
        }

        String msg = "L'utilisateur a ete ajoute avec succes ";
        redirectAttributes.addFlashAttribute("flashMessage", msg);

        return "redirect:/veyuz/banks/" + newAppUser.getBanque().getId() + "/users";
    }

    @GetMapping({"/veyuz/banks/{id}/block", "/veyuz/banks/{id}/active"})
    public String blocBanque(@PathVariable("id") Banque banque, @PathVariable(value = "client_id", required = false) Client client, RedirectAttributes redirectAttributes) {

        boolean action = !banque.isEnable();
        banque.setEnable(action);
        banque = banqueService.save(banque);
        Object flashMessage = "La banque a été désactivée !";
        if (banque.isEnable()) {
            flashMessage = "La banque a été réactivée !";
        }
        String redirectUrl = "/veyuz/banks";
        if (client != null) {
            redirectUrl = "/veyuz/" + client.getId() + "-banks";
        }
        redirectAttributes.addFlashAttribute("flashMessage", flashMessage);

        return "redirect:" + redirectUrl;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model, Principal principal, Authentication authentication) {

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session)) {
            return "redirect:/";
        }

        Banque banque = (Banque) session.getAttribute("banque");

        // On recupere le user qui est conncecté
        AppUser loggedUser = userService.getLoggedUser(principal);
        if (loggedUser.getClient() != null) {
            // si c'est un client on alors on cherche les donnees propre au client pour son dashboard
            getCustomerHomeData(banque, loggedUser, model);
        } else {
            // C'est le dashboard admin qu'on va afficher
            getAdminHomeDatas(banque, model, authentication, principal);
        }

        AppUser appUser = (AppUser) session.getAttribute("userConnecte");
        model.addAttribute("user", appUser);
        model.addAttribute("banque", banque);
        model.addAttribute("dash", "dash");
        model.addAttribute("devs", deviseService.getAll());

        return "dashboard";
    }

    private void getAdminHomeDatas(Banque banque, Model model, Authentication authentication, Principal principal) {
        Long waiting;
        AppUser loggedUser = userService.getLoggedUser(principal);
        Page<Transaction> transactionPage=null;
        Long checked;
        if (loggedUser.getAgence()!=null) {
            waiting = transactionService.getTransactionsByStatus(banque, StatusTransaction.WAITING_STR, 10000, 0, null, loggedUser.getAgence()).getTotalElements();
            transactionPage = transactionService.getAllTransactionsForBanqueAgence
                    (banque,loggedUser.getAgence() ,10, 0);
        } else {
            waiting = transactionService.getTransactionsByStatus(banque, StatusTransaction.WAITING_STR, 10000, 0, null, null).getTotalElements();
            transactionPage = transactionService.
                    getAllTransactionsForBanque(banque, 10, 0);
        }
        Collection<HashMap<String, Object>> deviseDtoList = deviseService.getAll(banque, null);

        Long nbClient = clientService.count(banque);
        Long waitingAp;

        List<TransactionDto> transactionList = transactionPage.getContent().stream().map(
                transaction -> {
                    TransactionDto tdo = new TransactionDto();
                    tdo.setBeneficiaire(transaction.getBeneficiaire());
                    try {
                        tdo.setId(CryptoUtils.encrypt(transaction.getId()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    tdo.setClient(transaction.getClient());
                    tdo.setMontant(transaction.getMontant());
                    tdo.setStatut(transaction.getStatut());
                    tdo.setAppUser(transaction.getAppUser());
                    tdo.setDevise(transaction.getDevise());
                    return tdo;
                }
        ).collect(Collectors.toList());

        waitingAp = apurementService.getApurementscount(banque, null, false).spliterator().estimateSize();
        if (loggedUser.getAgence()!=null) {
            checked = transactionService.getTransactionsByStatus(banque, StatusTransaction.MACKED_STR, 10000, 0, null, loggedUser.getAgence()).getTotalElements();
        } else {
            checked = transactionService.getTransactionsByStatus(banque, StatusTransaction.MACKED_STR, 10000, 0, null, null).getTotalElements();

        }

        model.addAttribute("lastTransactions", transactionList);
        model.addAttribute("waiting", waiting);
        model.addAttribute("nbClient", nbClient);
        model.addAttribute("waitingAp", waitingAp);
        model.addAttribute("checked", checked);
        model.addAttribute("devises", deviseDtoList);
        model.addAttribute("circularChartUri", "/build-circular-chart/" + banque.getId());
        model.addAttribute("lineChartUri", "/build-line-chart/" + banque.getId());
        model.addAttribute("barChartUri", "/build-bar-chart/" + banque.getId());
    }

    private void getCustomerHomeData(Banque banque, AppUser loggedUser, Model model) {
        Long waiting = transactionService.getTransactionsByStatus(banque, StatusTransaction.WAITING_STR, 10000, 0, loggedUser.getClient(), null).getTotalElements();
        Long waitingAp;
//        for (Apurement ap : apurementService.getNonApuredAndExpiredApurements(banque, loggedUser.getClient())) {
//            waitingAp++;
//        }
//        Page<Apurement> apurements;
        boolean isApured = false;
        if (loggedUser.getClient() != null) {
            waitingAp = apurementService.getApurementscount(banque, loggedUser.getClient(), isApured).spliterator().estimateSize();
        } else {
            waitingAp = apurementService.getApurementscount(banque, null, isApured).spliterator().estimateSize();

        }

        Long checked = transactionService.getTransactionsByStatus(banque, StatusTransaction.MACKED_STR, 10000, 0, loggedUser.getClient(), null).getTotalElements();
        Long sendBack = transactionService.getTransactionsByStatus(banque, StatusTransaction.SENDBACK_CUSTOMER_STR, 10000, 0, loggedUser.getClient(), null).getTotalElements();

        model.addAttribute("lastTransactions", transactionService.getPageableTransactionsForClient(banque, loggedUser.getClient(), 5, 0));
        model.addAttribute("waiting", waiting);
        model.addAttribute("waitingAp", waitingAp);
        model.addAttribute("checked", checked);
        model.addAttribute("sendBack", sendBack);
        model.addAttribute("devises", deviseService.getAll(banque, loggedUser.getClient()));
        model.addAttribute("circularChartUri", "/build-circular-chart/" + banque.getId() + "/" + loggedUser.getClient().getId());
        model.addAttribute("lineChartUri", "/build-line-chart/" + banque.getId() + "/" + loggedUser.getClient().getId());
        model.addAttribute("barChartUri", "/build-bar-chart/" + banque.getId() + "/" + loggedUser.getClient().getId());
    }


    @GetMapping("/dashboard-banque-{id}")
    public String redirectToDashboard(@PathVariable("id") Banque banque, Model model, HttpServletRequest request) {
        String uploadRootPath = request.getServletContext().getRealPath("upload");
        //System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"+uploadRootPath);

        session.setAttribute("banque", banque);
        session.setAttribute("uploadPath", uploadRootPath);
        session.setAttribute("avatarPath", uploadRootPath + "/avatar/");
        session.setAttribute("banqueLogoPath", uploadRootPath + "/banque_logo/");
        session.setAttribute("transactionsFilesPath", uploadRootPath + "/fichiers_transactions/");
        session.setAttribute("kycFilesPath", uploadRootPath + "/kyc/");
        session.setAttribute("tmpFilesPath", uploadRootPath + "/tmp/");

        return "redirect:/dashboard";
    }

    @GetMapping("/veyuz-admini-bloc-bank-action-/banque-{id}")
    public String blocBanque(@PathVariable("id") Banque banque) {
        boolean action = !banque.isEnable();


        banque.setEnable(action);
        banqueService.save(banque);
        return "redirect:/banks";
    }

}
