package com.akouma.veyuzwebapp.controller;

import com.akouma.veyuzwebapp.dto.TransactionDto;
import com.akouma.veyuzwebapp.form.*;
import com.akouma.veyuzwebapp.model.*;
import com.akouma.veyuzwebapp.repository.BanqueCorrespondanteRepository;
import com.akouma.veyuzwebapp.repository.TypeFinancementRepository;
import com.akouma.veyuzwebapp.service.*;
import com.akouma.veyuzwebapp.utils.*;
import com.akouma.veyuzwebapp.validator.TransactionValidator;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class TransactionController {

    private final int max = 25;
    private final int page = 1;
    @Autowired
    NotificationService notificationService;
    @Autowired
    HttpSession session;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private TypeDeTransactionService typeDeTransactionService;
    @Autowired
    private DeviseService deviseService;
    @Autowired
    private FichierService fichierService;
    @Autowired
    private ActionTransactionService actionTransactionService;
    @Autowired
    private ApurementService apurementService;
    @Autowired
    private UserService userService;
    @Autowired
    private ApurementFichierManquantService apurementFichierManquantService;
    @Autowired
    private TypeFinancementRepository typeFinancementRepository;
    @Autowired
    private TransactionValidator transactionValidator;
    @Autowired
    private ClientService clientService;
    @Autowired
    private AgenceService agenceService;

    @Autowired
    private BanqueCorrespondanteRepository banqueCorrespondanteRepository;

    @Autowired
    private DomiciliationTransactionService domiciliationTransactionService;


    public TransactionController(HttpSession session) {
        this.session = session;
    }

    @InitBinder
    protected void initBinder(WebDataBinder dataBinder) {
        Object target = dataBinder.getTarget();
        if (target == null) {
            return;
        }
        System.out.println("Target=" + target);

        if (target.getClass() == TransactionForm.class) {
            dataBinder.setValidator(transactionValidator);
        }
    }

    @GetMapping({"/transactions", "/transactions/page={page}","/transactions-{agence}"
            , "/transactions-{agence}/page={page}",})
    public String getTransactions(
            @PathVariable(value = "page", required = false) Integer page,
            Model model, Authentication authentication, Principal principal,
            @PathVariable(required = false,value = "agence") Long agenceId) {

        System.out.println("\n========================\nICI TRANSACTIONS\n===================");

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session) || principal == null) {
            return "redirect:/";
        }

        Banque banque = (Banque) session.getAttribute("banque");
        Page<Transaction> transactions = null;

        if (page == null || page <= 0) {
            page = this.page;
        }
        page--;

        Agence agence=null;
//        if(agenceId!=null){
//
//            agence=agenceService.getAgenceById(agenceId);
//            transactions = transactionService.getPageableTransactionsHasFilesForAgence(banque, agence, true, max, page);
//
//            SearchTransactionForm searchTransactionForm = new SearchTransactionForm();
//            searchTransactionForm.setBanque(banque);
//
//            String uri = "/transactions-"+agenceId+"/page={page}";
//            model.addAttribute("uri", uri);
//
//            addTransactionViewData(null, model, banque, transactions, searchTransactionForm, uri);
//            model.addAttribute("dash", "transaction");
//            model.addAttribute("das", "all");
//
//            return "transactionslist";
//
//        }

        AppUser loggedUser = userService.getLoggedUser(principal);
        // On verifie le role du user qui est connectee. si c'est un client alors on lui retourne la liste de ses transactions
        if (loggedUser.getClient() != null) {
            transactions = transactionService.getPageableTransactionsForClient(banque, loggedUser.getClient(), max, page);
            System.out.println("\nICI TRANSACTIONS DANS LE IF CLIENT\n===================");
        } else if (loggedUser.getAgence() != null) {
//            transactions = transactionService.getAllTransactionsForBanqueAgence(banque, loggedUser.getAgence(), max, page);
            transactions = transactionService.findByBanqueAndAgence(banque, loggedUser.getAgence(), max, page);
            System.out.println("\nICI TRANSACTIONS DANS LE ELSE IF AGENCE\n===================");
        } else {
            transactions = transactionService.getAllTransactionsForBanque(banque, max, page);
            System.out.println("\nICI TRANSACTIONS DANS LE ELSE\n===================");
        }
        SearchTransactionForm searchTransactionForm = new SearchTransactionForm();
        searchTransactionForm.setBanque(banque);

        String uri = "/transactions/page={page}";
        model.addAttribute("uri", uri);

        addTransactionViewData(null, model, banque, transactions, searchTransactionForm, uri);
        model.addAttribute("dash", "transaction");
        model.addAttribute("das", "all");

        return "transactionslist";
    }

    @Secured({"ROLE_MACKER", "ROLE_CHECKER", "ROLE_AGENCE", "ROLE_CHECKER_TO", "ROLE_MAKER_TO", "ROLE_SUPERADMIN", "ROLE_SUPERUSER", "ROLE_CONTROLLER"})
    @GetMapping({"/transactions/{agence_id}-agence", "/transactions/{agence_id}-agence/page={page}"})
    public String getTransactionsClient(
            @PathVariable("agence_id") Long id,
            @PathVariable(value = "page", required = false) Integer page,
            Model model, Principal principal) throws Exception {
        Agence agence = agenceService.getAgenceById(id);

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session) || principal == null) {
            return "redirect:/";
        }

        Banque banque = (Banque) session.getAttribute("banque");

        if (page == null || page <= 0) {
            page = this.page;
        }
        page--;

        Page<Transaction> transactions = transactionService.getPageableTransactionsHasFilesForAgence(banque, agence, true, max, page);

        String uri = "/transactions/" + agence.getId() + "-agence/page={page}";
        model.addAttribute("clients", clientService.countClientAgence(agence));
        model.addAttribute("transactionsCount", transactionService.getcountByAgence(banque,agence));
        model.addAttribute("transactionApur",
                transactionService.getcountByAgenceAndStatut(banque,agence, StatusTransaction.VALIDATED));
        model.addAttribute("transactionWait",
                transactionService.getcountByAgenceAndStatut(banque,agence, StatusTransaction.WAITING));
        addTransactionViewDataAgence(agence, model, banque, transactions, uri);
        model.addAttribute("dash", "transaction");
        return "agence-detail";
    }

    @Secured({"ROLE_MACKER", "ROLE_CHECKER", "ROLE_AGENCE", "ROLE_CHECKER_TO", "ROLE_MAKER_TO", "ROLE_SUPERADMIN", "ROLE_SUPERUSER", "ROLE_CONTROLLER"})
    @GetMapping({"/transactions/{client_id}-client", "/transactions/{client_id}-client/page={page}"})
    public String getTransactionsClient(
            @PathVariable("client_id") String clt,
            @PathVariable(value = "page", required = false) Integer page,
            Model model, Principal principal) throws Exception {
        Client client = clientService.findById(CryptoUtils.decrypt(clt));
        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session) || principal == null) {
            return "redirect:/";
        }

        Banque banque = (Banque) session.getAttribute("banque");

        if (page == null || page <= 0) {
            page = this.page;
        }
        page--;

        Page<Transaction> transactions = transactionService.getPageableTransactionsHasFilesForClient(banque, client, true, max, page);
        String id = CryptoUtils.encrypt(client.getId());

        SearchTransactionForm searchTransactionForm = new SearchTransactionForm();
        searchTransactionForm.setBanque(banque);
        searchTransactionForm.setClient(client);
        String uri = "/transactions/" + id + "-client/page={page}";
        model.addAttribute("uri", uri);
        model.addAttribute("showClientTransactionBtn", true);
        model.addAttribute("newTransactionLink", "/transaction/new/" + id + "/client/{type}");

        addTransactionViewData(client, model, banque, transactions, searchTransactionForm, uri);
        model.addAttribute("dash", "transaction");
        return "transactionslist";
    }

    @Secured({"ROLE_MACKER", "ROLE_CHECKER", "ROLE_AGENCE", "ROLE_CHECKER_TO", "ROLE_MAKER_TO", "ROLE_SUPERADMIN", "ROLE_SUPERUSER", "ROLE_CONTROLLER"})
    @GetMapping({"/transactions/{status}/page={page}", "/transactions/{status}",
            "/transactions-state/{agence}/{status}/page={page}","/transactions-state/{agence}/{status}"})
    public String getTransactionsByStatus(
            @PathVariable("status") String status,
            @PathVariable(value = "page", required = false) Integer page,
            @PathVariable(required = false,value = "agence") Long agenceId,
    Model model, Authentication authentication, Principal principal) {

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session) || principal == null) {
            return "redirect:/";
        }
        Page<Transaction> transactions;
        Banque banque = (Banque) session.getAttribute("banque");
        if (page == null || page <= 0) {
            page = this.page;
        }
        page--;
        Agence agence=null;
        if(agenceId!=null){
            agence=agenceService.getAgenceById(agenceId);
            transactions = transactionService.getTransactionsByStatus(banque, status, max, page, null,
                    agence);

            SearchTransactionForm searchTransactionForm = new SearchTransactionForm();
            searchTransactionForm.setBanque(banque);
            String uri = "/transactions-state/"+agenceId+"/" + status + "/page={page}";
            model.addAttribute("uri", uri);
            model.addAttribute("withStatus", status);
            addTransactionViewData(null, model, banque, transactions, searchTransactionForm, uri);
            model.addAttribute("das", status);
            model.addAttribute("dash", "transaction");
            return "transactionslist";
        }


        Client client = null;

        AppUser loggedUser = userService.getLoggedUser(principal);
        // On verifie le role du user qui est connectee. si c'est un client alors on lui retourne la liste de ses transactions
        if (loggedUser.getClient() != null) {
            if (loggedUser.getClient() == null || loggedUser.getClient().getBanques().isEmpty() || !loggedUser.getClient().getBanques().contains(banque)) {
                return "error/403";
            } else {
                client = loggedUser.getClient();
            }
        }

        if (loggedUser.getAgence()!=null) {
            transactions = transactionService.getTransactionsByStatus(banque, status, max, page, client, loggedUser.getAgence());

        } else {
            transactions = transactionService.getTransactionsByStatus(banque, status, max, page, client, null);

        }

        SearchTransactionForm searchTransactionForm = new SearchTransactionForm();
        searchTransactionForm.setBanque(banque);
        String uri = "/transactions/" + status + "/page={page}";
        model.addAttribute("uri", uri);
        model.addAttribute("withStatus", status);
        addTransactionViewData(client, model, banque, transactions, searchTransactionForm, uri);
        model.addAttribute("das", status);
        model.addAttribute("dash", "transaction");
        return "transactionslist";
    }


    @Secured({"ROLE_MACKER", "ROLE_CHECKER", "ROLE_AGENCE", "ROLE_CHECKER_TO", "ROLE_MAKER_TO", "ROLE_SUPERADMIN", "ROLE_SUPERUSER", "ROLE_CONTROLLER"})
    @PostMapping({ "/search-transactions-results","/search-transactions-results/{agence}" })
    public String getPeriodiqueTransactions(
            @ModelAttribute SearchTransactionForm searchTransactionForm,
            Model model, RedirectAttributes redirectAttributes, Principal principal) throws ParseException {

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session) || principal == null) {
            return "redirect:/";
        }

        List<Transaction> transactions = null;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date1 = null;
        if (searchTransactionForm.getDate1() != null && searchTransactionForm.getDate1() != "") {
            date1 = new Date(sdf.parse(searchTransactionForm.getDate1()).getTime());
        }
        Date date2 = null;
        if (searchTransactionForm.getDate2() != null && searchTransactionForm.getDate2() != "") {
            date2 = new Date(sdf.parse(searchTransactionForm.getDate2()).getTime());
        }
        String message = null;
        if (searchTransactionForm.getClient() == null) {

            if (date1 != null && date2 != null) {
                transactions = transactionService.getAllTransactionsForBanqueBetweenDates(
                        searchTransactionForm.getBanque(),
                        date1,
                        date2,
                        max, page
                );
                message = "Transactions effectuées entre le <b>" + date1 + "</b> et le <b>" + date2 + "</b>";

            } else if (date1 != null || date2 != null) {
                if (date2 != null && date1 == null) {
                    transactions = transactionService.getAllTransactionsForBanqueAfterDate(
                            searchTransactionForm.getBanque(),
                            date2,
                            max, page
                    );
                    message = "Transactions effectuées apres le <b>" + date2 + "</b>";
                    System.out.println("La date deux seulement est envoyee");
                } else if (date2 == null && date1 != null) {
                    transactions = transactionService.getAllTransactionsForBanqueBeforeDate(
                            searchTransactionForm.getBanque(),
                            date1,
                            max, page
                    );
                    message = "Transactions effectuées avant le <b>" + date1 + "</b>";
                }
            } else {


                String msg = "Vous devez saisir au moins une date pour effectuer une recherche !!!";
                redirectAttributes.addFlashAttribute("flashMessage", msg);

                return "redirect:/transactions";
            }
        }
        model.addAttribute("dash", "transaction");

        model.addAttribute("message", message);
        addTransactionViewData(searchTransactionForm.getClient(), model, searchTransactionForm.getBanque(), null, searchTransactionForm, null);
        assert transactions != null;
        model.addAttribute("transactions", fromEntitiesToDTO(transactions));
        return "transactionslist";
    }

    private List<TransactionDto> fromEntitiesToDTO(List<Transaction> transactions) {

        return transactions.stream().map(
                transaction -> {
                    TransactionDto tdo = new TransactionDto();
                    tdo.setBeneficiaire(transaction.getBeneficiaire());
                    try {
                        tdo.setId(CryptoUtils.encrypt(transaction.getId()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    tdo.setAppUser(transaction.getAppUser());
                    tdo.setClient(transaction.getClient());
                    tdo.setMontant(transaction.getMontant());
                    tdo.setDateTransaction(transaction.getDateTransaction());
                    tdo.setReference(transaction.getReference());
                    tdo.setTypeDeTransaction(transaction.getTypeDeTransaction());
                    tdo.setStatut(transaction.getStatut());
                    tdo.setDateCreation(transaction.getDateCreation());
                    tdo.setDevise(transaction.getDevise());
                    return tdo;
                }
        ).collect(Collectors.toList());
    }

    private void addTransactionViewDataAgence(Agence agence, Model model, Banque banque,
                                        Page<Transaction> transactions,
                                        String uri) {
        model.addAttribute("banque", banque);
        if (transactions != null) {
            int nbPages = transactions.getTotalPages();
            if (nbPages > 1) {
                int[] pages = new int[nbPages];
                for (int i = 0; i < nbPages; i++) {
                    pages[i] = i + 1;
                }
                model.addAttribute("pages", pages);
            }
            List<TransactionDto> transactionDtoList = transactions.getContent().stream().map(
                    transaction -> {
                        TransactionDto tdo = new TransactionDto();
                        tdo.setBeneficiaire(transaction.getBeneficiaire());
                        try {
                            tdo.setId(CryptoUtils.encrypt(transaction.getId()));

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        tdo.setAppUser(transaction.getAppUser());
                        tdo.setClient(transaction.getClient());
                        tdo.setMontant(transaction.getMontant());
                        tdo.setDateTransaction(transaction.getDateTransaction());
                        tdo.setReference(transaction.getReference());
                        tdo.setTypeDeTransaction(transaction.getTypeDeTransaction());
                        tdo.setStatut(transaction.getStatut());
                        tdo.setDateCreation(transaction.getDateCreation());
                        return tdo;
                    }
            ).collect(Collectors.toList());
            model.addAttribute("transactions", transactionDtoList);
            model.addAttribute("nbPages", transactions.getTotalPages());
            model.addAttribute("currentPage", transactions.getNumber() + 1);
            model.addAttribute("nbElements", transactions.getTotalElements());

        }
        model.addAttribute("importFile", new ImportFileForm(banque));
        model.addAttribute("activeMenu", 2);
        model.addAttribute("actiSub", 1);
        model.addAttribute("agence", agence);
        model.addAttribute("searchTransactionUri", "/search-transactions-results");
        model.addAttribute("dash", "transaction");

    }
    private void addTransactionViewData(Client client, Model model, Banque banque,
                                        Page<Transaction> transactions, SearchTransactionForm searchTransactionForm,
                                        String uri) {
        model.addAttribute("banque", banque);
        if (transactions != null) {
            int nbPages = transactions.getTotalPages();
            if (nbPages > 1) {
                int[] pages = new int[nbPages];
                for (int i = 0; i < nbPages; i++) {
                    pages[i] = i + 1;
                }
                model.addAttribute("pages", pages);
            }
            List<TransactionDto> transactionDtoList = transactions.getContent().stream().map(
                    transaction -> {
                        TransactionDto tdo = new TransactionDto();
                        tdo.setBeneficiaire(transaction.getBeneficiaire());
                        try {
                            tdo.setId(CryptoUtils.encrypt(transaction.getId()));

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        tdo.setAppUser(transaction.getAppUser());
                        tdo.setClient(transaction.getClient());
                        tdo.setMontant(transaction.getMontant());
                        tdo.setDateTransaction(transaction.getDateTransaction());
                        tdo.setReference(transaction.getReference());
                        tdo.setTypeDeTransaction(transaction.getTypeDeTransaction());
                        tdo.setStatut(transaction.getStatut());
                        tdo.setDateCreation(transaction.getDateCreation());
                        tdo.setDevise(transaction.getDevise());
                        return tdo;
                    }
            ).collect(Collectors.toList());
            model.addAttribute("transactions", transactionDtoList);
            model.addAttribute("nbPages", transactions.getTotalPages());
            model.addAttribute("currentPage", transactions.getNumber() + 1);
            model.addAttribute("nbElements", transactions.getTotalElements());

        }
        model.addAttribute("importFile", new ImportFileForm(banque));
        model.addAttribute("activeMenu", 2);
        model.addAttribute("actiSub", 1);
        model.addAttribute("filter", searchTransactionForm);
        model.addAttribute("client", client);
        model.addAttribute("searchTransactionUri", "/search-transactions-results");
        model.addAttribute("dash", "transaction");

    }

    // ==============================================================================


    @Secured({"ROLE_MACKER", "ROLE_CHECKER", "ROLE_AGENCE", "ROLE_CHECKER_TO", "ROLE_MAKER_TO"})
    @GetMapping({
            "/transaction/new/{type}",
    })
    public String getTransactionForm(@PathVariable(value = "type") String type,
                                     @PathVariable(value = "transaction_id", required = false) Transaction transaction,
                                     Model model, Principal principal) throws Exception {

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session) || principal == null) {
            return "redirect:/";
        }
        AppUser loggedUser = userService.getLoggedUser(principal);
        Banque banque = (Banque) session.getAttribute("banque");
        Client client = loggedUser.getClient();
        if (client == null) {
            if (loggedUser.getClient() == null) {
                return "error/403";
            }
        }
        setFormTransactionData(banque, client, type, transaction, model, false);
        model.addAttribute("dash", "transaction");
        model.addAttribute("das", "all");

        return "edit_transaction_form";
    }

    @Secured({"ROLE_ADMIN", "ROLE_CLIENT", "ROLE_TREASURY_OPS", "ROLE_AGENCE", "ROLE_TRADE_DESK"})
    @GetMapping(value = "/transaction/new/{client_id}/client/{type}")
    public String initierTransactionPourClient(@PathVariable("client_id") String clt,
        @PathVariable("type") String type,
        @PathVariable(value = "transaction_id", required = false) Transaction transaction,
        Model model, Principal principal, RedirectAttributes redirectAttributes,
        @RequestParam(value = "domiciliations", required = false) Boolean isManyDomiciliation
    ) throws Exception {

        Client client = clientService.findById(CryptoUtils.decrypt(clt));
        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session) || principal == null) {
            return "redirect:/";
        }
        Banque banque = (Banque) session.getAttribute("banque");

        if (client == null) {
            return "error/500";
        }
        if (type.equals("domiciliation")) {
            if (transactionService.checkIfHasNotTransactionPendingTransaction(banque, client).size() > 0) {
                // Means that custumer has some pending transactions
                String msg = "Attention, Il se peut que ce client  ait une transaction en cours" +
                        " non apurée . Priere de verifier ";
                redirectAttributes.addFlashAttribute("flashMessage", msg);
                return "redirect:/clients";
            }
        }
        boolean useManyDomiciliations = isManyDomiciliation != null;

        setFormTransactionData(banque, client, type, transaction, model, useManyDomiciliations);
        model.addAttribute("dash", "transaction");
        model.addAttribute("das", "all");

        model.addAttribute("useManyDomiciliations", useManyDomiciliations);

        return "edit_transaction_form";
    }

    @Secured({"ROLE_ADMIN", "ROLE_CLIENT", "ROLE_TREASURY_OPS", "ROLE_AGENCE", "ROLE_TRADE_DESK"})
    @GetMapping(value = "/transaction/{id}/edit")
    public String editTransaction(@PathVariable("id") String id, Model model, Principal principal, RedirectAttributes redirectAttributes) throws Exception {

        Transaction transaction = transactionService.getTransaction(CryptoUtils.decrypt(id)).get();
        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session) || principal == null) {
            return "redirect:/";
        }

        if (transaction.getStatut() != StatusTransaction.WAITING) {
            redirectAttributes.addFlashAttribute("flashMessage", "Cette transaction ne peut plus être modifiée !");
            return "/transaction-" + id + "/details";
        }

        Banque banque = (Banque) session.getAttribute("banque");
        String type = "normal";
        if (transaction.getDomiciliation() != null || transaction.getUseManyDomiciliations()) {
            type = "domiciliation";
        }

        boolean useManyDomiciliations = transaction.getUseManyDomiciliations();

        setFormTransactionData(banque, transaction.getClient(), type, transaction, model, useManyDomiciliations);
        model.addAttribute("dash", "transaction");
        model.addAttribute("das", "all");

        model.addAttribute("useManyDomiciliations", useManyDomiciliations);

        return "edit_transaction_form";
    }

    private void setFormTransactionData(Banque banque, Client client, String type, Transaction transaction, Model model, Boolean useManyDomiciliations) throws Exception {
        TransactionForm transactionForm;
        if (transaction != null) {
            transactionForm = new TransactionForm(transaction, type);
        } else {
            transactionForm = new TransactionForm(banque, client, type);
            transactionForm.setUseManyDomiciliations(useManyDomiciliations);
        }
        String id = CryptoUtils.encrypt(client.getId());

        transactionForm.setType(type);
        Iterable<TypeDeTransaction> typesTransaction = null;
        if (type.equalsIgnoreCase("domiciliation")) {
            String uri = "/transaction/new/" + id + "/client/{type}";
            model.addAttribute("newTransactionLink", uri);
            typesTransaction = typeDeTransactionService.getTypesTransactionByIsImport(true);
        } else {
            String uri = "/transaction/new/" + id + "/client/{type}";
            model.addAttribute("newTransactionLink", uri);

            typesTransaction = typeDeTransactionService.getTypesTransactionByIsImport(false);
        }
        model.addAttribute("listDevises", deviseService.getAll());
        model.addAttribute("typeTransactions", typesTransaction);
        model.addAttribute("listbeneficiaires", banque.getBeneficiaires());
        model.addAttribute("normal", StatusTransaction.TYPE_NORMAL);
        model.addAttribute("domiciliation", StatusTransaction.TYPE_DOMICILIATION);

        model.addAttribute("transactionForm", transactionForm);
        model.addAttribute("banque", banque);
        model.addAttribute("client", client);
        model.addAttribute("activeMenu", 2);
        model.addAttribute("actiSub", 1);
        model.addAttribute("type", type);
    }


    /**
     * @param transactionForm
     * @param redirectAttributes
     * @param principal
     * @return
     */
    @Secured({"ROLE_MACKER", "ROLE_CHECKER", "ROLE_AGENCE", "ROLE_CHECKER_TO", "ROLE_MAKER_TO"})
    @PostMapping("/save-transaction")
    public ModelAndView saveTransaction(
            @ModelAttribute @Validated TransactionForm transactionForm,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Principal principal, Model model) throws Exception {
        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session) || principal == null) {
            return new ModelAndView("redirect:/");
        }

        if (result.hasErrors()) {
            Iterable<TypeDeTransaction> typesTransaction = null;
            if (transactionForm.getType().equalsIgnoreCase("domiciliation")) {
                typesTransaction = typeDeTransactionService.getTypesTransactionByIsImport(true);
            } else {
                typesTransaction = typeDeTransactionService.getTypesTransactionByIsImport(false);
            }
            model.addAttribute("listDevises", deviseService.getAll());
            model.addAttribute("typeTransactions", typesTransaction);
            model.addAttribute("listbeneficiaires", transactionForm.getBanque().getBeneficiaires());
            model.addAttribute("normal", StatusTransaction.TYPE_NORMAL);
            model.addAttribute("domiciliation", StatusTransaction.TYPE_DOMICILIATION);
            model.addAttribute("banque", transactionForm.getBanque());
            model.addAttribute("client", transactionForm.getClient());
            model.addAttribute("dash", "transaction");
            model.addAttribute("das", "all");
            model.addAttribute("type", transactionForm.getType());
            String msg = "Une erreur s'est glissée dans votre formulaire de données . Erreur : " + result;

            return new ModelAndView("edit_transaction_form");
        }

        boolean isEditMode = false;
        Transaction transaction = new Transaction();
        if (transactionForm.getTransaction() != null) {
            // on cree une nouvelle transaction
            transaction = transactionForm.getTransaction();
            isEditMode = true;
        }

        transaction.setReference(transactionForm.getReference());
        // if (transactionForm.getDomiciliation() != null) {
        //     float reste = transactionForm.getDomiciliation().getMontantRestant() - transactionForm.getMontant();
        //     transactionForm.getDomiciliation().setMontantRestant(reste);
        // }
        transaction.setStatut(StatusTransaction.WAITING);
        transaction.setBanque(transactionForm.getBanque());
        transaction.setClient(transactionForm.getClient());
        transaction.setDevise(transactionForm.getDevise());
        transaction.setTypeDeTransaction(transactionForm.getTypeDeTransaction());
        transaction.setMontant(transactionForm.getMontant());
        transaction.setMontant(transactionForm.getMontant());
        transaction.setDomiciliation(transactionForm.getDomiciliation());
        transaction.setBeneficiaire(transactionForm.getBeneficiaire());
        transaction.setMotif(transactionForm.getMotif());
        transaction.setUseManyDomiciliations(transactionForm.getUseManyDomiciliations());
        transaction.setAgence(transactionForm.getClient().getAgence());
        transaction.setAppUser(userService.getLoggedUser(principal));// Permet de recuperer l'initiateur de l'operation

        if (transactionForm.getDateTransactionStr() != null) {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(transactionForm.getDateTransactionStr());

            transaction.setDateTransaction(date);
            // on determine la date d'expiration
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            if (transaction.getTypeDeTransaction().getType() != null) {
                if (transaction.getTypeDeTransaction().getType().equals(StatusTransaction.IMP_BIENS)) {
                    calendar.add(Calendar.DATE, StatusTransaction.DELAY_TRANSACTION_IMPORTATION_BIENS);
                } else if (transaction.getTypeDeTransaction().getType().equals(StatusTransaction.IMP_SERVICES)) {
                    calendar.add(Calendar.DATE, StatusTransaction.DELAY_TRANSACTION_IMPORTATION_SERVICES);
                } else {
                    calendar.add(Calendar.DATE, 20);
                }
            }

            transaction.setDelay(calendar.getTime());
        }

        for (TypeDeFichier tf : transactionForm.getTypeDeTransaction().getTypeDeFichiers()) {
            List<Fichier> f = null;
            if (transaction.getId() != null) {
                f = (List<Fichier>) fichierService.getFichiersTransaction(transaction, tf);
            }

            if (f == null || f.isEmpty()) {
                Fichier fichier = new Fichier();
                fichier.setTransaction(transaction);
                fichier.setTypeDeFichier(tf);
                fichier.setValidated(false);
                if (transaction.getFichiers() == null) {
                    transaction.setFichiers(new ArrayList<Fichier>());
                }
                transaction.getFichiers().add(fichier);
            }
        }
        Transaction saved = transactionService.saveTransaction(transaction);
        String id = CryptoUtils.encrypt(saved.getId());
        if (transaction.getUseManyDomiciliations() && isEditMode) {
            for (DomiciliationTransaction dt : transaction.getDomiciliationTransactions()) {
                domiciliationTransactionService.delete(dt);
            }
        }

        String lettreUri = "/" + id + "-lettre-engagement";

        String msg = "La transaction que vous avez initié a été prise en compte. Completer la en ajoutant tous les fichiers requis !!! <br>" +
                "Télécharger la lettre d'<a target='_blank' href=" + lettreUri + ">engagement ici</a> et incluez là dans la liste des documents requis.";
        redirectAttributes.addFlashAttribute("flashMessage", msg);

        return new ModelAndView("redirect:/import-files/" + id + "/transaction");
    }

    /**
     * @param transactionId
     * @param model
     * @param authentication
     * @param principal
     * @return
     */
    @GetMapping("/transaction-{id}/details")
    public String showTransactionDetails(@PathVariable("id") String transactionId, Model model, Authentication authentication, Principal principal) throws Exception {


        Transaction transaction = transactionService.getTransaction(CryptoUtils.decrypt(transactionId)).get();
        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session) || principal == null) {
            return "redirect:/";
        }

        AppUser loggedUser = userService.getLoggedUser(principal);
        if (authentication.getAuthorities().contains("ROLE_CLIENT") && !transaction.getClient().equals(loggedUser.getClient())) {
            return "error/403";
        }

        if (authentication.getAuthorities().contains("ROLE_ADMIN") && !transaction.getBanque().equals(loggedUser.getBanque())) {
            return "error/403";
        }

        if (transaction.getUseManyDomiciliations() != null && transaction.getUseManyDomiciliations()) {
            if (transaction.getDomiciliationTransactions().isEmpty()) {
                return "redirect:/import-files/" + transactionId + "/transaction";
            }
        }

        Iterable<ActionTransaction> actions = actionTransactionService.getActionsTransaction(transaction);

        model.addAttribute("M", StatusTransaction.MACKED);
        model.addAttribute("C", StatusTransaction.CHECKED);
        model.addAttribute("R", StatusTransaction.REJECTED);
        model.addAttribute("V", StatusTransaction.VALIDATED);
        model.addAttribute("W", StatusTransaction.WAITING);
        model.addAttribute("SC", StatusTransaction.SENDBACK_CUSTOMER);
        model.addAttribute("SM", StatusTransaction.SENDBACK_MACKER);

        model.addAttribute("SMSTR", StatusTransaction.SENDBACK_MACKER_STR);
        model.addAttribute("MSTR", StatusTransaction.MACKED_STR);
        model.addAttribute("VSTR", StatusTransaction.VALIDATED_STR);
        model.addAttribute("WSTR", StatusTransaction.WAITING_STR);
        model.addAttribute("CSTR", StatusTransaction.CHECKED_STR);
        model.addAttribute("SCSTR", StatusTransaction.SENDBACK_CUSTOMER_STR);
        model.addAttribute("RSTR", StatusTransaction.REJECTED_STR);
        model.addAttribute("transaction", transaction);
        model.addAttribute("banque", transaction.getBanque());
        model.addAttribute("client", transaction.getClient());
        model.addAttribute("rejetForm", new RejeterTransactionForm(transaction));
        model.addAttribute("activeMenu", 2);
        model.addAttribute("actiSub", 1);
        model.addAttribute("actions", actions);
        model.addAttribute("uri", "/import-files/" + CryptoUtils.encrypt(transaction.getId()) + "/transaction");
        model.addAttribute("typefinancement", typeFinancementRepository.findAll());
        model.addAttribute("banquecorrespondant", banqueCorrespondanteRepository.findAllByEnabled(true));
        model.addAttribute("dash", "transaction");
        model.addAttribute("das", "all");
        model.addAttribute("encryptedId", transactionId);

        return "transaction_details";
    }


    /**
     * @param form
     * @param redirectAttributes
     * @param principal
     * @return
     */
    @Transactional
    @Secured({"ROLE_CHECKER", "ROLE_MACKER", "ROLE_SUPERADMIN", "ROLE_TREASURY_OPS", "ROLE_AGENCE", "ROLE_TRADE_DESK"})
    @PostMapping("/rejeter-transaction")
    public String rejeterTransaction(
            @ModelAttribute RejeterTransactionForm form,
            RedirectAttributes redirectAttributes, Principal principal) throws Exception {

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session) || principal == null) {
            return "redirect:/";
        }

        AppUser appUser = userService.getLoggedUser(principal);

        HashMap<String, Object> response = new HashMap<String, Object>();
        if (form.getMotif() == null) {
            redirectAttributes.addFlashAttribute("fashMessage", "Impossible ! vous devez saisir un motif");
        } else {
            Transaction transaction = form.getTransaction();
            transaction.setStatut(StatusTransaction.REJECTED);
            ActionTransaction action = new ActionTransaction();
            action.setCommentaire(form.getMotif());
            action.setTransaction(transaction);

            action.setAppUser(appUser);
            transactionService.saveTransaction(transaction);
            actionTransactionService.saveActionTransaction(action);

//            Notification notification = new Notification();
//            notification.setMessage(form.getMotif());
//            notification.setUtilisateur(form.getTransaction().getClient().getUser());
//            notification.setRead(false);
//
//            notificationService.getNotificationRepository(notification);

//            Envoie de mail ici


            String msg = "La transaction a été rejétée ! Le client a ete notifié.";
            redirectAttributes.addFlashAttribute("flashMessage", msg);

            redirectAttributes.addFlashAttribute("message", "Transaction rejetée ! Le client a ete notifié");
        }
        String id = CryptoUtils.encrypt(form.getTransaction().getId());

        return "redirect:/transaction-" + id + "/details";
    }



    /**
     * SEULES LES TRANSACTIONS DE TYPES IMPORTATION SERONT LISTEES ICI
     *
     * @param page
     * @param model
     * @param authentication
     * @param principal
     * @return
     */
    @Secured({"ROLE_ADMIN", "ROLE_SUPERUSER", "ROLE_CLIENT", "ROLE_TREASURY_OPS", "ROLE_AGENCE", "ROLE_TRADE_DESK"})
    @GetMapping({"/appurements", "/appurements/page={page}"})
    public String showAppurementsList(
            @PathVariable(value = "page", required = false) Integer page,
            Model model, Authentication authentication, Principal principal) {

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session) || principal == null) {
            return "redirect:/";
        }

        Banque banque = (Banque) session.getAttribute("banque");
        AppUser loggedUser = userService.getLoggedUser(principal);
        if (authentication.getAuthorities().contains("ROLE_client") && (loggedUser.getClient() == null || !loggedUser.getClient().getBanques().contains(banque))) {
            return "error/403";
        } else if (authentication.getAuthorities().contains("ROLE_ADMIN") && !banque.equals(loggedUser.getBanque())) {
            return "error/403";
        }
        if (page == null || page <= 0) {
            page = this.page;
        }
        page--;
        HashMap<String, Object> appurements;
        if (loggedUser.getClient() == null) {
            appurements = transactionService.getApurements(banque, true, max, page);
        } else {
            appurements = transactionService.getApurements(banque, loggedUser.getClient(), true, max, page);
        }

        int nbPages = (int) appurements.get("nbPages");
        if (nbPages > 1) {
            int[] pages = new int[nbPages];
            for (int i = 0; i < nbPages; i++) {
                pages[i] = i + 1;
            }
            model.addAttribute("pages", pages);
        }

        model.addAttribute("currentPage", appurements.get("currentPage"));
        model.addAttribute("nbElements", appurements.get("nbElements"));
        model.addAttribute("nbPages", nbPages);
        model.addAttribute("appurements", appurements);
        model.addAttribute("banque", banque);
        model.addAttribute("activeMenu", 3);
        model.addAttribute("actiSub", null);
        model.addAttribute("uri", "/appurements/page={page}");
        model.addAttribute("dash", "apurement");

        model.addAttribute("show", "show");
        model.addAttribute("ValidateCode", StatusTransaction.VALIDATED);

        return "appurement_list";
    }


    @Secured({"ROLE_MACKER", "ROLE_CHECKER", "ROLE_AGENCE", "ROLE_CHECKER_TO", "ROLE_MAKER_TO", "ROLE_SUPERADMIN", "ROLE_TRADE_DESK", "ROLE_CONTROLLER"})
    @GetMapping({"/reporting", "/reporting/page={page}"})
    public String reporting(
            @PathVariable(value = "page", required = false) Integer page,
            @RequestParam(value = "devise", required = false) String deviseCode,
            @RequestParam(value = "typeDeTransaction", required = false) String typeCode,
            @RequestParam(value = "statut", required = false) String statut,
            @RequestParam(value = "date1Str", required = false) String date1Str,
            @RequestParam(value = "date2Str", required = false) String date2Str,
            HttpServletRequest request,
            Model model,
            Authentication authentication, Principal principal) throws ParseException {

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session)) {
            return "redirect:/";
        }

        Banque banque = (Banque) session.getAttribute("banque");
        AppUser loggedUser = userService.getLoggedUser(principal);
        if (authentication.getAuthorities().contains("ROLE_client") && (loggedUser.getClient() == null || !loggedUser.getClient().getBanques().contains(banque))) {
            return "error/403";
        } else if (authentication.getAuthorities().contains("ROLE_ADMIN") && !banque.equals(loggedUser.getBanque())) {
            return "error/403";
        }


        if (page == null || page <= 0) {
            page = this.page;
        }
        page--;

        ReportingSearchForm reportingSearchForm = new ReportingSearchForm();

        reportingSearchForm.setBanque(banque);

        // il y a la recherche
        Date date1 = null;
        Date date2 = null;
        boolean isSearch = false;
        if (deviseCode != null || typeCode != null || statut != null || (date1Str != null && date2Str != null)) {
            isSearch = true;
            model.addAttribute("devise", deviseCode);
            model.addAttribute("typeDeTransaction", typeCode);
            model.addAttribute("statut", statut);
            model.addAttribute("date1Str", date1Str);
            model.addAttribute("date2Str", date2Str);
        }

        HashMap<String, Object> appurements;
        if (loggedUser.getClient() == null) {
            if (isSearch) {
                appurements = transactionService.getByCustomProps(banque, max, page, deviseCode, typeCode, statut, date1Str, date2Str);
            } else {
                appurements = transactionService.getApurements(banque, true, max, page);
            }
        } else {
            appurements = transactionService.getTransactionsAsReporting(banque, loggedUser.getClient(), max, page);
        }

        int nbPages = (int) appurements.get("nbPages");
        if (nbPages > 1) {
            int[] pages = new int[nbPages];
            for (int i = 0; i < nbPages; i++) {
                pages[i] = i + 1;
            }
            model.addAttribute("pages", pages);
        }

        model.addAttribute("currentPage", appurements.get("currentPage"));
        model.addAttribute("nbElements", appurements.get("nbElements"));
        model.addAttribute("nbPages", nbPages);

        model.addAttribute("banque", banque);
        model.addAttribute("devises", deviseService.getAll());
        model.addAttribute("typesDeTransaction", typeDeTransactionService.getAllTypesTransaction());
        model.addAttribute("clients", banque.getClients());
        model.addAttribute("statuts", StatusTransaction.statuts);
        model.addAttribute("activeMenu", 4);
        model.addAttribute("actiSub", null);
        model.addAttribute("appurements", appurements);
        model.addAttribute("reportingSearchForm", reportingSearchForm);
        model.addAttribute("uri", "/reporting/page={page}");
        model.addAttribute("dash", "reporting");

        return "reporting";
    }


    @GetMapping({

            "/admin/export-transactions/{type}",
            "/admin/export-transactions/{type}/{client_id}/"})
    public String exportTransaction(
            @PathVariable("type") String type,
            @PathVariable(value = "client_id", required = false) Client client,
            RedirectAttributes redirectAttributes) throws IOException {

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session)) {
            return "redirect:/";
        }

        Banque banque = (Banque) session.getAttribute("banque");

        if (client != null && (client.getBanques().isEmpty() || !client.getBanques().contains(banque))) {
            return "error/403";
        }

        Iterable<Transaction> transactions;
        if (client == null) {
            transactions = transactionService.getTransactions(banque);
        } else {
            transactions = transactionService.getTransactionsClient(banque, client);
        }

        String flashMessage = null;

        if (type == "pdf") {
            genererTransactionsPdf(transactions);
        } else {
            flashMessage = genererTransactionsExcel(transactions);
        }

        redirectAttributes.addFlashAttribute("flashMessage", flashMessage);

        return "redirect:/transactions";
    }

    @GetMapping("/import-transactions")
    public String showImportTransactionFile(Model model, Principal principal) {

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session) || principal == null) {
            return "redirect:/";
        }

        model.addAttribute("type", "transaction");
        model.addAttribute("importTransaction", true);
        model.addAttribute("isImport", true);
        model.addAttribute("postUri", "/import");
        model.addAttribute("formTitle", "importer les transactions");
        model.addAttribute("dash", "params");
        model.addAttribute("setItem", "trans");

        return "parametres";
    }

    @Secured({"ROLE_MACKER", "ROLE_CHECKER", "ROLE_CHECKER_TO", "ROLE_MAKER_TO"})
    @PostMapping("/post-import-transaction")
    public String importTransactions(
            @ModelAttribute ImportFileForm importFile,
            Principal principal,
            final RedirectAttributes redirectAttributes, HttpServletRequest request) throws IOException {

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session) || principal == null) {
            return "redirect:/";
        }

        String msg = transactionService.importData(importFile, request);

        redirectAttributes.addFlashAttribute("flashMessage", msg);

        return "redirect:/transactions";

    }

    private void genererTransactionsPdf(Iterable<Transaction> transactions) {
    }

    private String genererTransactionsExcel(Iterable<Transaction> transactions) throws IOException {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("transactions");
        int i = 0;
        Cell cell;
        Row row;

        row = sheet.createRow(i);

        cell = row.createCell(0, CellType.STRING);
        cell.setCellValue("Reference client");

        cell = row.createCell(1, CellType.STRING);
        cell.setCellValue("Reference transaction");

        cell = row.createCell(2, CellType.STRING);
        cell.setCellValue("Montant de la transaction");

        cell = row.createCell(3, CellType.STRING);
        cell.setCellValue("Statut");

        cell = row.createCell(4, CellType.STRING);
        cell.setCellValue("Type de transaction");

        cell = row.createCell(5, CellType.STRING);
        cell.setCellValue("Reference domiciliation");

        cell = row.createCell(6, CellType.STRING);
        cell.setCellValue("Reference Beneficiaire");

        cell = row.createCell(7, CellType.STRING);
        cell.setCellValue("Date de creation");


        for (Transaction t : transactions) {
            i++;
            row = sheet.createRow(i);

            cell = row.createCell(0);
            cell.setCellValue(t.getClient().getReference());

            cell = row.createCell(1);
            cell.setCellValue(t.getReference());

            cell = row.createCell(2);
            cell.setCellValue(t.getMontant());

            cell = row.createCell(3);
            cell.setCellValue(t.getStatut());

            cell = row.createCell(4);
            cell.setCellValue(t.getTypeDeTransaction().getCode());

            cell = row.createCell(5);
            String d = null;
            if (t.getDomiciliation() != null) {
                d = t.getDomiciliation().getReference();
            }
            cell.setCellValue(d);

            cell = row.createCell(6);
            cell.setCellValue(t.getBeneficiaire().getReference());

            cell = row.createCell(7);
            cell.setCellValue(t.getDateCreation());
        }
        File file = new File(Upload.UPLOAD_DIR + "/tmp/imptransaction.xls");
        file.getParentFile().mkdirs();
        FileOutputStream outFile = new FileOutputStream(file);
        workbook.write(outFile);
        return file.getAbsolutePath();
    }

    @GetMapping("/set-transaction-{id}-reference-and-date")
    public String setReferenceAndDate(
            @PathVariable("id") Transaction transaction,
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "typefinancement", required = false, defaultValue = "0") long financement,
            @RequestParam(value = "taux", required = false) String taux,
            @RequestParam(value = "dateValeur", required = false) String dateValeur,
            @RequestParam(value = "batebeac", required = false) String batebeac,
            @RequestParam(value = "banquecorrespondant", required = false) Long banquecorrespondant,
            RedirectAttributes redirectAttributes, Principal principal, Authentication authentication) throws Exception {

        if (authentication.getAuthorities().stream().
                anyMatch(a -> a.getAuthority().equals("ROLE_CHECKER_TO")) || authentication.getAuthorities().stream().
                anyMatch(a -> a.getAuthority().equals("ROLE_MAKER_TO")) || authentication.getAuthorities().stream().
                anyMatch(a -> a.getAuthority().equals("ROLE_TREASURY"))) {
            if (financement != 0) {
                transaction.setTypeFinancement(typeFinancementRepository.getById(financement));
                transactionService.saveTransaction(transaction);

                String id = CryptoUtils.encrypt(transaction.getId());
                ActionTransaction actionTransaction = new ActionTransaction();
                actionTransaction.setTransaction(transaction);
                actionTransaction.setAction("Ajout du type de préfinancement ");
                AppUser loggedUser = userService.getLoggedUser(principal);
                actionTransaction.setAppUser(loggedUser);
                actionTransaction.setCommentaire("Ajout du type de préfinancement  effectué avec succès");
                actionTransactionService.saveActionTransaction(actionTransaction);
                return "redirect:/transaction-" + id + "/details";

            }
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        if (authentication.getAuthorities().stream().
                anyMatch(a -> a.getAuthority().equals("ROLE_CHECKER_TO"))) {
            if (batebeac != null) {
                transaction.setRefValeurBeac(batebeac);
                transactionService.saveTransaction(transaction);

                String id = CryptoUtils.encrypt(transaction.getId());
                ActionTransaction actionTransaction = new ActionTransaction();
                actionTransaction.setTransaction(transaction);
                actionTransaction.setAction("Ajout Reference Beac ");
                AppUser loggedUser = userService.getLoggedUser(principal);
                actionTransaction.setAppUser(loggedUser);
                actionTransaction.setCommentaire("Ajout referene Beac");
                actionTransactionService.saveActionTransaction(actionTransaction);
                return "redirect:/transaction-" + id + "/details";
            }
        }

        try {
            if (date == null || taux == null || dateValeur == null) {
                redirectAttributes.addFlashAttribute("flashMessage",
                        "Oops Une erreur est survenue , veuillez verifier que vous y etes autorise ou que vous avez bien renseignez les differentes dates");
            }
            Date dateOperationTransaction = new Date();
            Banque banque = (Banque) session.getAttribute("banque");

            String ref=transactionService.generateTransactionNumber(banque);
            while(transactionService.checkReference(ref)){
                ref=new ReferenceGenerator().generateReference();
            }
            BanqueCorrespondante bqCrd=banqueCorrespondanteRepository.findById(banquecorrespondant).get();
            transaction.setReference(ref);
            transaction.setDateTransaction(dateOperationTransaction);
            transaction.setStatut(StatusTransaction.VALIDATED);
            transaction.setTaux(taux);
            transaction.setBanqueCorrespondante(bqCrd);
            transaction.setDateValeur(sdf.parse(dateValeur));
            // on determine la date d'expiration
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateOperationTransaction);
            if (transaction.getTypeDeTransaction().getType() != null) {
                if (transaction.getTypeDeTransaction().getType().equals(StatusTransaction.IMP_BIENS)) {
                    calendar.add(Calendar.DATE, StatusTransaction.DELAY_TRANSACTION_IMPORTATION_BIENS);
                } else if (transaction.getTypeDeTransaction().getType().equals(StatusTransaction.IMP_SERVICES)) {
                    calendar.add(Calendar.DATE, StatusTransaction.DELAY_TRANSACTION_IMPORTATION_SERVICES);
                } else {
                    calendar.add(Calendar.DATE, 20);
                }
            }
            String message = "L'operation a ete prise en compte ! la transaction a été approuvée et validée par la trésorerie. Puis transmise pour apurement !";
            // message = "L'opération a été transmise à la trésorerie !";
//            Notification notification = new Notification();
//            notification.setMessage(message);
//            notification.setRead(false);
//            notification.setUtilisateur(transaction.getClient().getUser());
//            notification.setHref("/transaction-" + CryptoUtils.encrypt(transaction.getId()) + "/details");
//            notificationService.save(notification);
            transaction.setDelay(calendar.getTime());
            transaction.setCycleNormalAcheve(true);
            transactionService.saveTransaction(transaction);
            if (transaction.getTypeDeTransaction().isImport()) {
                // Je deplace la transaction en question dans la table apurement
                removeFromApurement(transaction);
            }

            ActionTransaction actionTransaction = new ActionTransaction();
            actionTransaction.setTransaction(transaction);
            actionTransaction.setAction("Date d'exécution et référence");
            AppUser loggedUser = userService.getLoggedUser(principal);
            actionTransaction.setAppUser(loggedUser);
            actionTransaction.setCommentaire("Ajout de la date de valeur de la transaction");
            actionTransactionService.saveActionTransaction(actionTransaction);
            redirectAttributes.addFlashAttribute("flashMessage", message);

        } catch (ParseException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("flashMessage", "Verifier que vous avez saisie une date valide");
        }
        String id = CryptoUtils.encrypt(transaction.getId());

        return "redirect:/transaction-" + id + "/details";
    }

    @Secured({"ROLE_MACKER", "ROLE_CHECKER", "ROLE_AGENCE", "ROLE_CHECKER_TO", "ROLE_MAKER_TO"})
    @GetMapping("/add-others-{id}-files")
    public String addOthersFiles(Principal principal, @PathVariable("id") Transaction transaction, @RequestParam("autresFichiers") String autresFichiers, RedirectAttributes redirectAttributes) throws Exception {

        if (autresFichiers == null || autresFichiers.equals("")) {
            redirectAttributes.addFlashAttribute("flashMessage", "Vous devez saisir les autres fichiers à ajouter");
        } else {
            String message = "Les fichiers suivants ont été ajoutés à la liste des fichiers à fournir pour l'apurement.<br><ul>";
            Apurement apurement = apurementService.getApurementByReferenceTransaction(transaction.getReference());
            for (String af : autresFichiers.split(";")) {
                if (!af.trim().equals("")) {
                    Fichier f = new Fichier();
                    f.setTransaction(transaction);
                    f.setValidated(false);
                    f.setForAurement(true);
                    f.setFileTitle(af.trim());
                    if (apurement != null) {
                        ApurementFichierManquant afm = new ApurementFichierManquant();
                        afm.setForApurement(true);
                        afm.setIsValidated(false);
                        afm.setFileName(af.trim());
                        afm.setApurement(apurement);
                        apurementFichierManquantService.saveFichierManquant(afm);
                    }
                    fichierService.saveFichier(f);
                    message += "<li>" + af + "</li>";
                }
            }
            message += "</ul>";
            redirectAttributes.addFlashAttribute("flashMessage", message);
            ActionTransaction actionTransaction = new ActionTransaction();
            actionTransaction.setTransaction(transaction);
            actionTransaction.setAction("Autres fichiers");
            AppUser loggedUser = userService.getLoggedUser(principal);
            actionTransaction.setAppUser(loggedUser);
            actionTransaction.setCommentaire(message);
            actionTransactionService.saveActionTransaction(actionTransaction);
        }
        String id = CryptoUtils.encrypt(transaction.getId());
        return "redirect:/transaction-" + id + "/details";
    }

    private void removeFromApurement(Transaction transaction) {
        Apurement apurement = apurementService.getApurementByReferenceTransaction(transaction.getReference());
        if (apurement == null) {
            apurement = new Apurement();
        }
        apurement.setReferenceTransaction(transaction.getReference());
        apurement.setIsApured(false);
        apurement.setClient(transaction.getClient());
        apurement.setBeneficiaire(transaction.getBeneficiaire().getName());
        apurement.setBanque(transaction.getBanque());
        apurement.setMontant(transaction.getMontant());
        apurement.setDateOuverture(transaction.getDateTransaction());
        apurement.setDevise(transaction.getDevise().getCode());
        apurement.setMotif(transaction.getMotif());
        apurement.setDateExpiration(transaction.getDelay());
        apurement.setStatus(StatusTransaction.APUREMENT_WAITING_DATE);
        apurement.setTransaction(transaction);
        apurement.setAppUser(transaction.getAppUser());
        Apurement savedApurement = apurementService.saveApurement(apurement);

        // on ajoute les fichiers de l'apurement
        for (Fichier f : transaction.getFichiers()) {
            String fileName = f.getTypeDeFichier() != null ? f.getTypeDeFichier().getName() : f.getFileTitle();
            ApurementFichierManquant fichierManquant = apurementFichierManquantService.getFichierManquant(apurement, fileName);
            if (fichierManquant == null) {
                fichierManquant = new ApurementFichierManquant();
                fichierManquant.setIsValidated(f.getFileName() != null && f.isValidated());
                fichierManquant.setFileName(fileName.toUpperCase());
                fichierManquant.setFile(f.getFileName());
                fichierManquant.setApurement(savedApurement);
                if (f.getTypeDeFichier() != null) {
                    fichierManquant.setForApurement(f.getTypeDeFichier().isObligatoireForApurement());
                } else {
                    fichierManquant.setForApurement(f.isForAurement());
                }
                apurementFichierManquantService.saveFichierManquant(fichierManquant);
            }
        }
    }

    @Secured({"ROLE_MACKER", "ROLE_CHECKER", "ROLE_AGENCE", "ROLE_CHECKER_TO", "ROLE_MAKER_TO", "ROLE_SUPERADMIN"})
    @GetMapping("/transactions/{id}/delete")
    public ModelAndView deleteTransaction(@PathVariable("id") String id, RedirectAttributes redirectAttributes) throws Exception {
        Transaction transaction = transactionService.getTransaction(CryptoUtils.decrypt(id)).get();

        if (transaction.getStatut() != StatusTransaction.WAITING) {
            redirectAttributes.addFlashAttribute("flashMessage", "Impossible de supprimer cette opération !");
            return new ModelAndView("redirect:/transactions");
        }

        for (DomiciliationTransaction dt : transaction.getDomiciliationTransactions()) {
            domiciliationTransactionService.delete(dt);
        }

        for (Fichier f : transaction.getFichiers()) {
            fichierService.delete(f);
        }

        transactionService.deleteTransaction(transaction);

        return new ModelAndView("redirect:/transactions");
    }
}
