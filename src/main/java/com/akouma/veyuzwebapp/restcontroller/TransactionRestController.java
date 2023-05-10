package com.akouma.veyuzwebapp.restcontroller;

import com.akouma.veyuzwebapp.form.ImportFileForm;
import com.akouma.veyuzwebapp.form.RejeterTransactionForm;
import com.akouma.veyuzwebapp.form.ReportingSearchForm;
import com.akouma.veyuzwebapp.form.TransactionForm;
import com.akouma.veyuzwebapp.model.*;
import com.akouma.veyuzwebapp.service.*;
import com.akouma.veyuzwebapp.utils.CryptoUtils;
import com.akouma.veyuzwebapp.utils.StatusTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
public class TransactionRestController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ActionTransactionService actionTransactionService;

    @Autowired
    private FichierService fichierService;
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AppRoleService roleService;

    @Autowired
    private ApurementService apurementService;
    @Autowired
    private ApurementFichierManquantService apurementFichierManquantService;

    @Autowired
    private UserRoleService userRoleService;
    @Autowired
    private CryptoUtils cryptoUtils;

    @Transactional
    @GetMapping("/rest-transaction-change-status/{id}/{user_id}/{status}")
    public ResponseEntity<?> changeTransactionStatus(
            @PathVariable("id") Transaction transaction,
            @PathVariable("user_id") AppUser appUser,

            @PathVariable("status") String status,
            RedirectAttributes redirectAttributes
    ) {

        int statut;
        String message = null;
        switch (status) {
            case StatusTransaction.CHECKED_STR:
                statut = StatusTransaction.CHECKED;
                break;
            case StatusTransaction.MACKED_STR:
                statut = StatusTransaction.MACKED;
                break;
            case StatusTransaction.REJECTED_STR:
                statut = StatusTransaction.REJECTED;
                break;
            case StatusTransaction.VALIDATED_STR:
                statut = StatusTransaction.VALIDATED;
                break;
            case StatusTransaction.WAITING_STR:
                statut = StatusTransaction.WAITING;
                break;
            case StatusTransaction.SENDBACK_CUSTOMER_STR:
                statut = StatusTransaction.SENDBACK_CUSTOMER;
                break;
            case StatusTransaction.SENDBACK_MACKER_STR:
                statut = StatusTransaction.SENDBACK_MACKER;
                break;
            default:
                statut = StatusTransaction.INVALID_STATUS;
                break;
        }
        boolean canChange = false;
        if (statut != StatusTransaction.INVALID_STATUS) {
            // ON checked d'abord la transaction et apres on la macke
            if (statut == StatusTransaction.MACKED || statut == StatusTransaction.CHECKED) {
                if (statut == StatusTransaction.MACKED) {
                    canChange = true;
                } else {
                    ActionTransaction last = actionTransactionService.getLastAction(transaction);
                    if (last != null && !last.getAppUser().equals(appUser)) {
                        canChange = true;
                    } else {
                        message = "Vous ne pouver plus modifier cette transaction";
                    }
                }
            } else {
                canChange = transaction.getStatut() != StatusTransaction.VALIDATED;
            }

            if (canChange) {
                message = "Transaction Approuvée et transmis pour validation définitive";
                if (statut == StatusTransaction.CHECKED) {
                    statut = StatusTransaction.VALIDATED;
                    message = "Transaction validée !";
                    for (Fichier f : transaction.getFichiers()) {
                        f.setValidated(true);
                        fichierService.saveFichier(f);
                    }
                }
                transaction.setStatut(statut);
                ActionTransaction action = new ActionTransaction();
                action.setAction(status);
                action.setAppUser(appUser);
                action.setTransaction(transaction);
                transactionService.saveTransaction(transaction);
                actionTransactionService.saveActionTransaction(action);
            }
        }

        HashMap<String, Object> response = new HashMap<>();
        response.put("isChange", canChange);

        redirectAttributes.addFlashAttribute("flashMessage", message);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/macked-transaction/{id}/{user_id}/{status}")
    public ResponseEntity<?> approuverTransactionLikeMacker(
            @PathVariable("id") Transaction transaction,
            @PathVariable("user_id") AppUser appUser,
            @PathVariable("status") String status, Authentication authentication) throws Exception {

        HashMap<String, Object> response = new HashMap<>();
        String message = "Vous ne pouvez pas effectuer cette operation";
        boolean isChange = false;

        // Le user doit avoir le role ROLE_MACKER et la transaction doit etre a WAITING
        if (status.equals(StatusTransaction.MACKED_STR)) {
            System.out.println("premier test ok");
            if (transaction.getStatut() == StatusTransaction.WAITING && transaction.getBanque().equals(appUser.getBanque())) {

                // AppRole roleMacker = roleService.getRoleByName("ROLE_MACKER");
                if (authentication.getAuthorities().stream().
                        anyMatch(a -> a.getAuthority().equals("ROLE_MACKER"))) {
//
//                    UserRole userRole = userRoleService.getUserRole(appUser, roleMacker);
//                    if (userRole != null) {

                    transaction.setStatut(StatusTransaction.MACKED);
                    transactionService.saveTransaction(transaction);
                    this.saveTransactionAndAction(transaction, appUser, status, null);
                    Notification notification = new Notification();
                    notification.setMessage("Une de vos transactions a été Approuvée et transmise pour le Trade desk Maker");
                    notification.setRead(false);
                    notification.setUtilisateur(transaction.getClient().getUser());
                    notification.setHref("/transaction-" + CryptoUtils.encrypt(transaction.getId()) + "/details");
                    notificationService.save(notification);
                    isChange = true;
                    message = "L'operation a ete prise en compte ! la transaction a ete transmise pour le Trade desk Maker .";

                }
            }
        }

        response.put("message", message);
        response.put("isChange", isChange);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @GetMapping("/check-transaction/{id}/{user_id}/{status}")
    public ResponseEntity<?> approuverTransactionLikeChecker(
            @PathVariable("id") Transaction transaction,
            @PathVariable("user_id") AppUser appUser,
            @PathVariable("status") String status, Authentication authentication) throws Exception {

        HashMap<String, Object> response = new HashMap<>();
        String message = "Vous ne pouvez pas effectuer cette operation Car il se peut que vous ne disposez pas des autorisations necessaires";
        boolean isChange = false;


        // Le user doit avoir le role ROLE_MACKER et la transaction doit etre a WAITING
        if (transaction.getBanque().equals(appUser.getBanque())) {

            if (authentication.getAuthorities().stream().
                    anyMatch(a -> a.getAuthority().equals("ROLE_CHECKER"))
                    && transaction.getStatut() == StatusTransaction.MACKED) {
                transaction.setRenvoye(false);

                transaction.setStatut(StatusTransaction.CHECKED);
                transactionService.saveTransaction(transaction);
                this.saveTransactionAndAction(transaction, appUser, status,
                        "transmise au Treasory Ops Maker Pour traitement");
                isChange = true;
      System.out.println("******* CHECKER"+transaction.getStatut());
                message = "L'opération a été transmise au Treasory Ops Maker Pour traitement";
                Notification notification = new Notification();
                notification.setMessage("La transaction  du client " + transaction.getClient().toString() + " a été validée et transmise au Treasory Ops Maker ");
                notification.setRead(false);
                notification.setUtilisateur(transaction.getAppUser());
                notification.setHref("/transaction-" + CryptoUtils.encrypt(transaction.getId()) + "/details");
                notificationService.save(notification);

            } else if (authentication.getAuthorities().stream().
                    anyMatch(a -> a.getAuthority().equals("ROLE_MACKER"))
                    && transaction.getStatut() == StatusTransaction.SENDBACK_CUSTOMER) {
//
//                    UserRole userRole = userRoleService.getUserRole(appUser, roleMacker);
//                    if (userRole != null) {

                transaction.setStatut(StatusTransaction.MACKED);
                transactionService.saveTransaction(transaction);
                this.saveTransactionAndAction(transaction, appUser, status, "La transaction a ete transmise pour le Trade desk Maker");
                Notification notification = new Notification();
                notification.setMessage("Une de vos transactions a été Approuvée et transmise pour le Trade desk Maker");
                notification.setRead(false);
                notification.setUtilisateur(transaction.getClient().getUser());
                notification.setHref("/transaction-" + CryptoUtils.encrypt(transaction.getId()) + "/details");
                notificationService.save(notification);
                isChange = true;
                message = "L'operation a ete prise en compte ! la transaction a ete transmise pour le Trade desk Maker .";


            } else if (authentication.getAuthorities().stream().
                    anyMatch(a -> a.getAuthority().equals("ROLE_CHECKER"))
                    && transaction.getStatut() == StatusTransaction.SENDBACK_MACKER) {
                transaction.setStatut(StatusTransaction.CHECKED);
                transactionService.saveTransaction(transaction);
                this.saveTransactionAndAction(transaction, appUser, status, "La transaction a ete transmise pour le Trade desk Maker");
                Notification notification = new Notification();
                notification.setMessage("Une de vos transactions a été Approuvée et transmise pour le Trade desk Maker");
                notification.setRead(false);
                notification.setUtilisateur(transaction.getClient().getUser());
                notification.setHref("/transaction-" + CryptoUtils.encrypt(transaction.getId()) + "/details");
                notificationService.save(notification);
                isChange = true;
                message = "L'operation a ete prise en compte ! la transaction a ete transmise pour le Trade desk Maker .";

            } else if (authentication.getAuthorities().stream().
                    anyMatch(a -> a.getAuthority().equals("ROLE_MAKER_TO"))
                    && transaction.getStatut() == StatusTransaction.CHECKED) {
                transaction.setRenvoye(false);
                System.out.println("******* Trae M"+transaction.getStatut());

                transaction.setStatut(StatusTransaction.TRANSMIS_MAKER_2);
                transactionService.saveTransaction(transaction);
                this.saveTransactionAndAction(transaction, appUser, status,
                        "transmise au Treasory Ops Checker Pour validation");
                isChange = true;
                //message = "L'operation a ete prise en compte ! la transaction a été approuvée et validée définitivement. Puis transmise pour apurement !";
                message = "L'opération a été transmise au Treasory Ops Checker Pour validation";
                Notification notification = new Notification();
                notification.setMessage("La transaction  du client " + transaction.getClient().toString() + " a été validée et transmise au Treasory Ops Cherker ");
                notification.setRead(false);
                notification.setUtilisateur(transaction.getAppUser());
                notification.setHref("/transaction-" + CryptoUtils.encrypt(transaction.getId()) + "/details");
                notificationService.save(notification);


            } else if (authentication.getAuthorities().stream().
                    anyMatch(a -> a.getAuthority().equals("ROLE_CHECKER_TO"))
                    && transaction.getStatut() == StatusTransaction.TRANSMIS_MAKER_2) {
                transaction.setRenvoye(false);
                System.out.println("******* Treasury C"+transaction.getStatut());

                if (transaction.getTypeFinancement() != null) {
                    transaction.setStatut(StatusTransaction.TRANSMIS_TRESORERIE);
                    transactionService.saveTransaction(transaction);
                    this.saveTransactionAndAction(transaction, appUser, status,
                            "transmise pour validation de la tresorerie");
                    isChange = true;

                    message = "L'opération a été transmise pour validation de la tresorerie";
                    Notification notification = new Notification();
                    notification.setMessage("La transaction  du client " + transaction.getClient().toString() + " a été validée et pour validation de la tresorerie ");
                    notification.setRead(false);
                    notification.setUtilisateur(transaction.getAppUser());
                    notification.setHref("/transaction-" + CryptoUtils.encrypt(transaction.getId()) + "/details");
                    notificationService.save(notification);
                } else {
                    message = "Veuillez saisir le type de Préfinancement";
                }
            }
        }

        response.put("message", message);
        response.put("isChange", isChange);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

//    @GetMapping("/check-transaction/{id}/{user_id}/{status}")
//    public ResponseEntity<?> approuverTransactionLikeChecker(
//            @PathVariable("id") Transaction transaction,
//            @PathVariable("user_id") AppUser appUser,
//            @PathVariable("status") String status) {
//
//        HashMap<String, Object> response = new HashMap<>();
//        String message = "Vous ne pouvez pas effectuer cette operation";
//        boolean isChange = false;
//
////        if (transaction.getReference() == null || transaction.getDateTransaction() == null) {
////            response.put("message", "Vous devez d'abord ajouter une reference et une date a cette transaction pour poursuivre !");
////            response.put("isChange", false);
////            return new ResponseEntity<>(response, HttpStatus.OK);
////        }
//
//        // Le user doit avoir le role ROLE_MACKER et la transaction doit etre a WAITING
//        if (status.equals(StatusTransaction.CHECKED_STR)) {
//            if (transaction.getStatut() == StatusTransaction.MACKED && transaction.getBanque().equals(appUser.getBanque())) {
//                AppRole roleChecker = roleService.getRoleByName("ROLE_CHECKER");
//                if (roleChecker != null) {
//                    UserRole userRole = userRoleService.getUserRole(appUser, roleChecker);
//                    if (userRole != null) {
//                        transaction.setStatut(StatusTransaction.TRANSMIS_MAKER_2);
//                        transactionService.saveTransaction(transaction);
//                        this.saveTransactionAndAction(transaction, appUser, status, null);
//                        isChange = true;
//                        //message = "L'operation a ete prise en compte ! la transaction a été approuvée et validée définitivement. Puis transmise pour apurement !";
//                        message = "L'opération a été transmise à la trésorerie !";
//                        Notification notification = new Notification();
//                        notification.setMessage("Une de vos transactions a été validée et transmise a la tresorerie ");
//                        notification.setRead(false);
//                        notification.setUtilisateur(transaction.getClient().getUser());
//                        notification.setHref("/transaction-"+transaction.getId()+"/details");
//                        notificationService.save(notification);
////                        if (transaction.getTypeDeTransaction().isImport()) {
////                            // Je deplace la transaction en question dans la table apurement
////                            removeFromApurement(transaction);
////                        }
//                    }
//                }
//            }
//        }
//
//        response.put("message", message);
//        response.put("isChange", isChange);
//        return new ResponseEntity<>(response, HttpStatus.OK);
//    }

    @PostMapping("/send-back-to-customer/{id}/{user_id}/{status}")
    public ResponseEntity<?> renvoyerAuClient(
            @PathVariable("id") Transaction transaction,
            @PathVariable("user_id") AppUser appUser,
            @PathVariable("status") String status,
            @ModelAttribute RejeterTransactionForm rejeterTransactionForm) throws Exception {

        HashMap<String, Object> response = new HashMap<>();
        String message = "Vous ne pouvez pas effectuer cette operation";
        boolean isChange = false;
        String commentaire = rejeterTransactionForm.getMotif();

        // Le user doit avoir le role ROLE_MACKER et la transaction doit etre a WAITING
        if (status.equals(StatusTransaction.SENDBACK_CUSTOMER_STR)) {
            if ((transaction.getStatut() == StatusTransaction.WAITING || transaction.getStatut() == StatusTransaction.SENDBACK_MACKER) && transaction.getBanque().equals(appUser.getBanque())) {
                AppRole roleMacker = roleService.getRoleByName("ROLE_MACKER");
                if (roleMacker != null) {
                    UserRole userRole = userRoleService.getUserRole(appUser, roleMacker);
                    if (userRole != null) {
                        transaction.setStatut(StatusTransaction.SENDBACK_CUSTOMER);
                        this.saveTransactionAndAction(transaction, appUser, status, commentaire);
                        isChange = true;
                        message = "L'operation a ete prise en compte ! la transaction a été renvoyée au client pour complement.";
                        Notification notification = new Notification();
                        notification.setMessage("Une de vos transactions  vous a été renvoyée pour complément ! Voir l'historique en cliquant sur le lien precedent pour connaitre les motifs de ce renvoie.");
                        notification.setRead(false);
                        notification.setHref("/transaction-" + CryptoUtils.encrypt(transaction.getId()) + "/details");
                        notification.setUtilisateur(transaction.getClient().getUser());
                        notificationService.save(notification);
                    }
                }
            }
        }

        response.put("message", message);
        response.put("isChange", isChange);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/send-back-to-macker/{id}/{user_id}/{status}")
    public ResponseEntity<?> renvoyerAuMacker(
            @PathVariable("id") Transaction transaction,
            @PathVariable("user_id") AppUser appUser,
            @PathVariable("status") String status,
            @ModelAttribute RejeterTransactionForm rejeterTransactionForm, Authentication authentication) throws Exception {

        HashMap<String, Object> response = new HashMap<>();
        String message = "Vous ne pouvez pas effectuer cette operation";
        boolean isChange = false;
        String commentaire = rejeterTransactionForm.getMotif();

        // Le user doit avoir le role ROLE_MACKER et la transaction doit etre a WAITING
        if (transaction.getBanque().equals(appUser.getBanque())) {

            if (authentication.getAuthorities().stream().
                    anyMatch(a -> a.getAuthority().equals("ROLE_CHECKER"))
                    && transaction.getStatut() == StatusTransaction.MACKED) {
                transaction.setRenvoye(true);
                transaction.setStatut(StatusTransaction.SENDBACK_MACKER);
                this.saveTransactionAndAction(transaction, appUser, status, commentaire);
                isChange = true;
                message = "L'operation a ete prise en compte ! la transaction a été renvoyée au client pour complement.";
                Notification notification = new Notification();
                notification.setMessage("La transaction  du client " + transaction.getClient().toString() + " a été renvoye au trade pour validation");
                notification.setRead(false);
                notification.setUtilisateur(transaction.getAppUser());
                notification.setHref("/transaction-" + CryptoUtils.encrypt(transaction.getId()) + "/details");
                notificationService.save(notification);

            } else if (authentication.getAuthorities().stream().
                    anyMatch(a -> a.getAuthority().equals("ROLE_MAKER_TO"))
                    && transaction.getStatut() == StatusTransaction.CHECKED) {

                transaction.setStatut(StatusTransaction.MACKED);
                transaction.setRenvoye(true);
                transactionService.saveTransaction(transaction);
                this.saveTransactionAndAction(transaction, appUser, status,
                        "Transaction a été renvoyée par le treasury Ops Maker");
                isChange = true;
                //message = "L'operation a ete prise en compte ! la transaction a été approuvée et validée définitivement. Puis transmise pour apurement !";
                message = "La Transaction  a été renvoyée par le treasury Ops Maker";
                Notification notification = new Notification();
                notification.setMessage("La transaction  du client " + transaction.getClient().toString() + " a été renvoye au trade pour validation");
                notification.setRead(false);
                notification.setUtilisateur(transaction.getAppUser());
                notification.setHref("/transaction-" + CryptoUtils.encrypt(transaction.getId()) + "/details");
                notificationService.save(notification);


            } else if (authentication.getAuthorities().stream().
                    anyMatch(a -> a.getAuthority().equals("ROLE_CHECKER_TO"))
                    && transaction.getStatut() == StatusTransaction.TRANSMIS_MAKER_2) {
                transaction.setRenvoye(true);
                transaction.setStatut(StatusTransaction.CHECKED);
                transactionService.saveTransaction(transaction);
                this.saveTransactionAndAction(transaction, appUser, status,
                        "Transaction renvoyee par le treasury Ops Checker");
                isChange = true;
                //message = "L'operation a ete prise en compte ! la transaction a été approuvée et validée définitivement. Puis transmise pour apurement !";
                message = "La Transaction  a ete renvoyee par le treasury Ops Checker";
                Notification notification = new Notification();
                notification.setMessage("La transaction  du client " + transaction.getClient().toString() + " a été renvoyee au trade pour validation");
                notification.setRead(false);
                notification.setUtilisateur(transaction.getAppUser());
                notification.setHref("/transaction-" + CryptoUtils.encrypt(transaction.getId()) + "/details");
                notificationService.save(notification);


            }
        }
        response.put("message", message);
        response.put("isChange", isChange);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/valider-transaction/{id}/{user_id}/{status}")
    public ResponseEntity<?> validerTransaction(
            @PathVariable("id") Transaction transaction,
            @PathVariable("user_id") AppUser appUser,
            @PathVariable("status") String status) throws Exception {

        HashMap<String, Object> response = new HashMap<>();
        String message = "Vous ne pouvez pas effectuer cette operation";
        boolean isChange = false;

        if (transaction.getReference() == null || transaction.getDateTransaction() == null) {
            response.put("message", "Vous devez d'abord ajouter une reference et une date a cette transaction pour poursuivre !");
            response.put("isChange", isChange);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        // Le user doit avoir le role ROLE_MACKER et la transaction doit etre a WAITING
        if (status.equals(StatusTransaction.VALIDATED_STR)) {
            if ((transaction.getStatut() == StatusTransaction.MACKED || transaction.getStatut() == StatusTransaction.SENDBACK_CHECKER) && transaction.getBanque().equals(appUser.getBanque())) {
                AppRole roleChecker = roleService.getRoleByName("ROLE_CHECKER");
                if (roleChecker != null) {
                    UserRole userRole = userRoleService.getUserRole(appUser, roleChecker);
                    if (userRole != null) {
                        transaction.setStatut(StatusTransaction.VALIDATED);
                        this.saveTransactionAndAction(transaction, appUser, status, null);
                        isChange = true;
                        message = "L'operation a ete prise en compte ! la transaction a été approuvée et validée.";
                        Notification notification = new Notification();
                        notification.setMessage("la transaction a été validée ! Maintenant encours d'apurement");
                        notification.setRead(false);
                        notification.setHref("/transaction-" + CryptoUtils.encrypt(transaction.getId()) + "/details");
                        notification.setUtilisateur(transaction.getClient().getUser());
                        notificationService.save(notification);
                        if (transaction.getTypeDeTransaction().isImport()) {
                            // Je deplace la transaction en question dans la table apurement
                            removeFromApurement(transaction);
                        }
                    }
                }
            }
        }

        response.put("message", message);
        response.put("isChange", isChange);
        return new ResponseEntity<>(response, HttpStatus.OK);
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

    @GetMapping("/valider-{id}-apurement")
    public ResponseEntity<?> apureeTransaction(@PathVariable("id") Transaction transaction) {
        transaction.setApured(true);
        transaction.setStatut(StatusTransaction.VALIDATED);
        transactionService.saveTransaction(transaction);

        HashMap<String, Object> response = new HashMap<>();
        response.put("message", "L'operation a été prise en compte");
        response.put("isChange", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private void saveTransactionAndAction(Transaction transaction, AppUser appUser, String status, String commentaire) {
        transactionService.saveTransaction(transaction);

        ActionTransaction action = new ActionTransaction();
        action.setAction(status);
        action.setAppUser(appUser);
        action.setTransaction(transaction);
        action.setCommentaire(commentaire);
        actionTransactionService.saveActionTransaction(action);
    }

    @PostMapping("/rest-reporting")
    public HashMap<String, Object> getReporting(ReportingSearchForm reportingSearchForm) {
        HashMap<String, Object> appurements = transactionService.getTransactionsAsReporting(reportingSearchForm.getBanque(), reportingSearchForm.getMax(), reportingSearchForm.getPage());

        return appurements;
    }

    @GetMapping("/delete-transaction-{id}")
    public HashMap<String, Object> deleteTransaction(@PathVariable("id") Transaction transaction) {
        HashMap<String, Object> response = new HashMap<String, Object>();
        transactionService.deleteTransaction(transaction);
        response.put("ok", true);
        return response;
    }

    @PostMapping({
            "/rest-save-transaction",
            "/rest-save-transaction-client"
    })
    public HashMap saveTransaction(@ModelAttribute TransactionForm transactionForm) {
        HashMap<String, Object> response = new HashMap<String, Object>();
        Transaction transaction = new Transaction();
        if (transactionForm.getTransaction() != null) {
            // on cree une nouvelle transaction
            System.out.println("La transaction doit etre modifier");
            transaction = transactionForm.getTransaction();
        }
        if (transaction.getReference() == null) {
            Date date = new Date();
            String reference = "T-" + date.getTime();
            transaction.setReference(reference);
        }
        if (transactionForm.getDomiciliation() != null) {
            float reste = transactionForm.getDomiciliation().getMontantRestant() - transactionForm.getMontant();
            transactionForm.getDomiciliation().setMontantRestant(reste);
        }
        transaction.setStatut(StatusTransaction.WAITING);
        transaction.setBanque(transactionForm.getBanque());
        transaction.setClient(transactionForm.getClient());
        transaction.setDevise(transactionForm.getDevise());
        transaction.setTypeDeTransaction(transactionForm.getTypeDeTransaction());
        transaction.setMontant(transactionForm.getMontant());
        transaction.setDomiciliation(transactionForm.getDomiciliation());
        transaction.setBeneficiaire(transactionForm.getBeneficiaire());
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
                transaction.getFichiers().add(fichier);
            }
        }
        Transaction saved = transactionService.saveTransaction(transaction);

        response.put("isSaved", true);
        ModelAndView redirectUrl = new ModelAndView("redirect:/import-files-transaction/" + saved.getId());
        System.out.println(redirectUrl);
        response.put("redirectUrl", redirectUrl);

        return response;
    }

    @PostMapping("/rest-rejeter-transaction")
    public HashMap rejeterTransaction(@ModelAttribute RejeterTransactionForm form, Principal principal) {
        HashMap<String, Object> response = new HashMap<String, Object>();
        if (form.getMotif() == null) {
            response.put("rejected", false);
        } else {
            AppUser loggedUser = ((AppUserDetails) principal).getAppUser();
            Transaction transaction = form.getTransaction();
            transaction.setStatut(StatusTransaction.REJECTED);
            ActionTransaction action = new ActionTransaction();
            action.setCommentaire(form.getMotif());
            action.setTransaction(transaction);
            action.setAppUser(loggedUser);

            transactionService.saveTransaction(transaction);
            actionTransactionService.saveActionTransaction(action);

            response.put("rejected", true);
        }
        return response;
    }

    @PostMapping("/rest-post-import-transaction")
    public String importTransactions(
            @ModelAttribute ImportFileForm importFile,
            RedirectAttributes redirectAttributes, HttpServletRequest request) throws IOException {
        String msg = transactionService.importData(importFile, request);

        redirectAttributes.addFlashAttribute(msg);

        return "redirect:/transactions/banque-" + importFile.getBanque().getId();

    }

    @GetMapping("/change-expiration-transaction-date/{id}/{date}")
    public ResponseEntity<?> changeTransactionExpirationDate(@PathVariable("id") Transaction transaction, @PathVariable("date") String dateStr) throws ParseException {
        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
        boolean isChanged = false;
        Long nbJoursRestant = null;
        int maxExpirationDays = 20;
        if (transaction.getTypeDeTransaction().getType() != null && transaction.getTypeDeTransaction().getType().equals(StatusTransaction.IMP_BIENS)) {
            maxExpirationDays = StatusTransaction.DELAY_TRANSACTION_IMPORTATION_BIENS;
        } else if (transaction.getTypeDeTransaction().getType() != null && transaction.getTypeDeTransaction().getType().equals(StatusTransaction.IMP_SERVICES)) {
            maxExpirationDays = StatusTransaction.DELAY_TRANSACTION_IMPORTATION_SERVICES;
        }
        if (date.getTime() > transaction.getDelay().getTime()) {
            Calendar dateDuJour = Calendar.getInstance();
            long diff = date.getTime() - dateDuJour.getTime().getTime();
            long nbJours = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
            if (nbJours > 0 && nbJours <= maxExpirationDays) {
                nbJoursRestant = maxExpirationDays - nbJours;
                transaction.setDelay(date);
                transactionService.saveTransaction(transaction);
                isChanged = true;
            }
        }
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("isChanged", isChanged);
        hashMap.put("nbJours", nbJoursRestant);
        hashMap.put("delay", new SimpleDateFormat("yyyy-MM-dd").format(transaction.getDelay()));
        return new ResponseEntity<>(hashMap, HttpStatus.OK);
    }

    @GetMapping("/rest-controller-{id}-beneficiaires-transactions")
    public HashMap<String, Object> getBeneficiaireTransactions(@PathVariable("id") Beneficiaire beneficiaire) {
        HashMap<String, Object> response = new HashMap<>();
        String transactions = "<table class='table'><thead>" +
                "<tr>" +
                "<th>Reference</th>" +
                "<th>Date</th>" +
                "<th >Montant</th>" +
                "<th>Type</th>" +
                "<th>Action</th>" +
                "</tr></thead><tbody>";
        String message = null;
        boolean hasError = false;
        for (Transaction t : beneficiaire.getTransactions()) {
            transactions += "<tr>" +
                    "<td>" + t.getReference() + "</td>" +
                    "<td>" + t.getDateCreation() + "</td>" +
                    "<td class=\"montant_format\">" + t.getMontant() + "</td>" +
                    "<td>" + t.getTypeDeTransaction().getName() + "</td>" +
                    "<td></td>" +
                    "</tr>";
        }
        transactions += "</tbody></table>";

        System.out.println(beneficiaire.getTransactions());

        if (beneficiaire.getTransactions() == null || beneficiaire.getTransactions().isEmpty()) {
            message = "Aucune transaction trouvée !";
            hasError = true;
        }

        response.put("transactions", transactions);
        response.put("hasError", hasError);
        response.put("message", message);

        return response;
    }

}
