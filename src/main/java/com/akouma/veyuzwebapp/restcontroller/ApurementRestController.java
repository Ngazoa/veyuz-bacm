package com.akouma.veyuzwebapp.restcontroller;

import com.akouma.veyuzwebapp.form.ApurementFileForm;
import com.akouma.veyuzwebapp.form.ImportFile;
import com.akouma.veyuzwebapp.model.*;
import com.akouma.veyuzwebapp.service.*;
import com.akouma.veyuzwebapp.utils.StatusTransaction;
import com.akouma.veyuzwebapp.utils.Upload;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.servlet.http.HttpServletRequest;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
public class ApurementRestController {
    @Autowired
    FileStorageService fileStorageService;
    @Autowired
    private ApurementService apurementService;
    @Autowired
    private ApurementFichierManquantService fichierManquantService;
    @Autowired
    private ClientService clientService;
    @Autowired
    private UserService userService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private NotificationService notificationService;

    @Autowired
    TemplateEngine templateEngine;

    /**
     * Cette fonction permet de renvoyer la liste des fichiers uploader par le client
     * @param apurement
     * @return
     */
    @GetMapping("/rest-apurements/{id}/files")
    public ResponseEntity<?> getFichiersEnvoyes(@PathVariable("id") Apurement apurement, Principal principal) {

        String data = null;
        boolean hasData = false;
        AppUser loggedUser = userService.getLoggedUser(principal);
        if (apurement.getFichiersManquants() != null && !apurement.getFichiersManquants().isEmpty()){
            data = "";
            for (ApurementFichierManquant f : apurement.getFichiersManquants()) {
                if (f.getFile() != null && f.isForApurement()) {
                    hasData = true;

                    String v = "/rest-apurements/file/"+ f.getId() +"/false";
                    if (!f.getIsValidated()) {
                        v = "/rest-apurements/file/"+ f.getId() +"/true";
                    }
                    String fLink = "/downloadFile/fichiers_transactions/" + f.getFile();

                    data += "<p class=\"form-control fichier-apurement mb-3\">"+
                            "<a href='"+ fLink +"'>"+
                            "<i class=\"align-middle\" data-feather=\"file-text\"></i>"+
                            "<span>" + f.getFileName() + "</span>"+
                            "</a>";
                    if (loggedUser != null && loggedUser.getClient() == null) {
                        String title = f.getIsValidated() ? "Invalider le fichier" : "Valider le fichier";
                        data += "<a class='valider' title='"+title+"' href='" + v + "' style='float:right;'>";
                        data += f.getIsValidated() ? "<i class='fas fa-times-circle text-danger'></i>" : "<i class='fas fa-check-circle text-success'></i>";
                        data += "</a>";
                    }

                    data += "</p>";
                }
            }
        }
        HashMap<String, Object> response = new HashMap<>();
        response.put("data", data);
        response.put("hasData", hasData);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Cette fonction permet de recuperer le fichier permettant l'envoi des mises en demeure
     * @param importFile
     * @return
     */
    @PostMapping("/rest-apurements/mise-demeure")
    public ResponseEntity<?> sendMisesEnDemeure(@ModelAttribute ImportFile importFile, HttpServletRequest request) {
        String results = null;
        try {
            Upload u = new Upload();
            String fileName = Upload.UPLOAD_DIR + "/tmp/" + u.uploadFile(importFile.getFile(),fileStorageService, "tmp",request);
            Collection<Collection<HashMap<String, Object>>> workbookData = fetchData(fileName);
            addApurements(workbookData, importFile.getBanque());
            results = sendMail(workbookData, importFile.getBanque());
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Une erreur est survenue pendant l'upload du fichier", HttpStatus.BAD_REQUEST);
        }

        HashMap<String, Object> response = new HashMap<>();
        response.put("results", results);
        response.put("isOk", true);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/rest-apurements/{id}/files-forms")
    public ResponseEntity<?> getFilesForms(@PathVariable("id") Apurement apurement) {
        boolean hasData = false;
        String forms = null;
        if (apurement.getIsApured()) {
            forms = "Ce dossier est cloturé !";
        }
        else if (apurement.getFichiersManquants() != null && !apurement.getFichiersManquants().isEmpty()) {
            forms = "<div style='max-height: 300px;overflow:auto;'>";
            for (ApurementFichierManquant f : apurement.getFichiersManquants()) {
                if (f.isForApurement() && !f.getIsValidated()) {
                    hasData = true;
                    forms += "<form method='POST' action='/rest-apurements/save-file' enctype='multipart/form-data'>" +
                            "<input type='hidden' name='fichierManquant' value='"+f.getId()+"'>" +
                            "<label class='form-label'>"+f.getFileName().toUpperCase()+"</label>" +
                            "<div class='input-group mb-4'>" +
                            "<input type='file' name='file' class='form-control'>";
                    if (f.getFile() != null) {
                        String fLink = "/downloadFile/fichiers_transactions/" + f.getFile();
                        forms += "<a target='_blank' title='Old file' href='"+fLink+"'><i class='fas fa-eye'></i> view</a>";
                    }
                    forms += "</div>" +
                            "</form>";
                }
            }
            forms += "</div><div class='mb-3 mt-3'><button type='button' id='addFilesButton' class='btn btn-primary'>Enregistrer</button></div>" +
                    "<div id='result' class='mt-1'></div>";
        }

        HashMap<String, Object> response = new HashMap<>();
        response.put("data", forms);
        response.put("hasData", hasData);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Cette fonction permet d'uploader les fichiers des apurements
     * @param fileForm
     * @return
     */
    @PostMapping("/rest-apurements/save-file")
    public ResponseEntity<?> saveFileApurement(@ModelAttribute ApurementFileForm fileForm, HttpServletRequest request) {
        String message = null;
        boolean isUpload = false;
        try {
            Upload upload = new Upload();
            if (fileForm.getFile() != null) {
                String fileName = upload.uploadFile(fileForm.getFile(),fileStorageService, "fichiers_transactions",request);
                if (fileName != null && fileForm.getFichierManquant() != null) {
//                    System.out.println("I -"+fileName+" II- "+fileForm.getFichierManquant());
                    fileForm.getFichierManquant().setFile(fileName);
                    fileForm.getFichierManquant().setIsValidated(false);
                    ApurementFichierManquant apurementFichierManquant = fichierManquantService.saveFichierManquant(fileForm.getFichierManquant());
                    if (apurementFichierManquant != null) {
                        Apurement apurement = apurementFichierManquant.getApurement();
                        apurement.setStatus(StatusTransaction.APUREMENT_HAS_FILES);
                        apurementService.saveApurement(apurement);
                    }
                    message = fileForm.getFichierManquant().getFileName() + " a été déplacé et transmis";
                    isUpload = true;
                }else {
                    message = fileForm.getFichierManquant().getFileName() + " n'a pas pu être déplacé et transmis";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Une erreur est survenue pendant le deplacement du fichier " + fileForm.getFichierManquant().getFileName(), HttpStatus.BAD_REQUEST);
        }

        HashMap<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("isUpload", isUpload);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Cette fonction permet de valider un fichier envoye par un client
     * @param fichier
     * @param validated
     * @return
     */
    @GetMapping("/rest-apurements/file/{id}/{validate}")
    public ResponseEntity<?> validerFichier(@PathVariable("id") ApurementFichierManquant fichier, @PathVariable("validate") boolean validated, Authentication authentication) {
        HashMap<String, Object> response = new HashMap<>();

        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TRADE_DESK")) ||
            authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN"))) {
            response.put("isOk", false);
            response.put("message", "Vous n'avez pas les authorisations nécessaires pour effectuer cette opération");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        fichier.setIsValidated(validated);
        fichier.getApurement().setIsApured(false);
        fichierManquantService.saveFichierManquant(fichier);
        apurementService.saveApurement(fichier.getApurement());
        // On envoie une notification
        if (!validated) {
            Notification notification = new Notification();
            Transaction t = transactionService.getTransactionByReference(fichier.getApurement().getReferenceTransaction());
            notification.setMessage("Le fichier " + fichier.getFileName() + " que vous avez deposé pour la transaction dont la reference est " + fichier.getApurement().getReferenceTransaction() + " a été rejeté !");
            notification.setUtilisateur(t.getClient().getUser());
            notification.setRead(false);
            notificationService.save(notification);
        }

        String v = "/rest-apurements/file/"+ fichier.getId() +"/false";
        String fa = "<i class='fas fa-times-circle text-danger'></i>";
        String title = "Invalider le fichier";
        String message = "Fichier validé !";
        if (!validated) {
            message = "Fichier rejeté !";
            v = "/rest-apurements/file/"+ fichier.getId() +"/true";
            fa = "<i class='fas fa-check-circle text-success'></i>";
            title = "Valider le fichier";
        }


        response.put("message", message);
        response.put("isOk", true);
        response.put("href", v);
        response.put("fa", fa);
        response.put("title", title);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Cette fonction permet de marquer un dossier comme apure
     * @param apurement
     * @return
     */
    @GetMapping("/rest-apurements/{id}/apurer")
    public ResponseEntity<?> apurer(@PathVariable("id")Apurement apurement, Authentication authentication) {
        System.out.println("\n\n\n==============================\nOn veut apurer\n=======================================");
        HashMap<String, Object> response = new HashMap<>();

        if(!(authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TRADE_DESK")) ||
                authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERADMIN")))) {
            response.put("isOk", false);
            response.put("message", "Vous n'avez pas les authorisations nécessaires pour effectuer cette opération");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        String msg = "Dossier apuré !";
        String list = "<ul>";
        boolean isCompleted = true;
        for (ApurementFichierManquant fm : apurement.getFichiersManquants()) {
            if (fm.isForApurement() && fm.getFile() == null) {
                isCompleted = false;
                list += "<li>" + fm.getFileName() + "</li>";
            }
        }
        if (!isCompleted) {
            list += "</ul>";
            msg = "Impossible d'apurer ce dossier car les fichiers suivants sont encore manquants. <br>"+list;
        }

        if (isCompleted) {
            apurement.setIsApured(true);
            apurement.setStatus(StatusTransaction.APUREMENT_IS_VALIDATED);
            for (ApurementFichierManquant fm : apurement.getFichiersManquants()) {
                fm.setIsValidated(true);
                Transaction transaction = transactionService.getTransactionByReference(apurement.getReferenceTransaction());
                if (transaction != null) {
                    Date now = new Date();
                    transaction.setDateApurement(now);
                    transaction.setApured(true);
                    transaction.setStatut(StatusTransaction.VALIDATED);
                    transactionService.saveTransaction(transaction);
                }
                fichierManquantService.saveFichierManquant(fm);
            }
            apurementService.saveApurement(apurement);
        }

        response.put("message", msg);
        response.put("isOk", isCompleted);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Cette fonction permet de proroger la date d'expiration de l'apurement
     * @param apurement
     * @param dateStr
     * @return
     */
    @GetMapping("/change-expiration-apurement-date/{id}/{date}")
    public ResponseEntity<?> changeTransactionExpirationDate(@PathVariable("id") Apurement apurement, @PathVariable("date") String dateStr) {
        HashMap<String, Object> hashMap = new HashMap<>();
        String errorMessage = null;
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            errorMessage = "Vous devez saisir une date valide !";
        }
        boolean isChanged = false;
        Long nbJoursRestant = null;
        int maxExpirationDays = StatusTransaction.DEFAULT_DELAY_TRANSACTION;
        Transaction transaction = transactionService.getTransactionByReference(apurement.getReferenceTransaction());
        if (apurement.getCountEditExpirationDate() < 3) {
            if (transaction != null) {
                if (transaction.getTypeDeTransaction().getType() != null && transaction.getTypeDeTransaction().getType().equals(StatusTransaction.IMP_BIENS)){
                    maxExpirationDays = StatusTransaction.DELAY_TRANSACTION_IMPORTATION_BIENS;
                } else if (transaction.getTypeDeTransaction().getType() != null && transaction.getTypeDeTransaction().getType().equals(StatusTransaction.IMP_SERVICES)){
                    maxExpirationDays = StatusTransaction.DELAY_TRANSACTION_IMPORTATION_SERVICES;
                }
                if (date.getTime() > apurement.getDateExpiration().getTime()) {
                    Calendar dateDuJour = Calendar.getInstance();
                    long diff = date.getTime() - dateDuJour.getTime().getTime();
                    long nbJours = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                    if (nbJours > 0 && nbJours <= maxExpirationDays) {
                        nbJoursRestant = maxExpirationDays - nbJours;
                        apurement.setDateExpiration(date);
                        apurement.setCountEditExpirationDate(apurement.getCountEditExpirationDate()+1);
                        apurementService.saveApurement(apurement);
                        isChanged = true;
                        hashMap.put("dateText", new SimpleDateFormat("dd-MM-yyyy").format(date));
                        hashMap.put("dataValue", new SimpleDateFormat("yyyy-MM-dd").format(date));
                    }
                } else {
                    errorMessage = "La date de prorogation doit etre superieure a la date d'expiration";
                }
            } else {
                errorMessage = "La transaction liée à cet apurement n'existe pas !";
            }
        }else {
            errorMessage = "Vous ne pouvez plus proroger cette apurement car cette transaction a déjà été prorogée " + apurement.getCountEditExpirationDate() + " fois ! Le quota de prorogation est atteint.";
        }

        hashMap.put("errorMessage", errorMessage);
        hashMap.put("isChanged", isChanged);
        hashMap.put("nbJours", nbJoursRestant);
        hashMap.put("delay", new SimpleDateFormat("yyyy-MM-dd").format(transaction.getDelay()));
        return new ResponseEntity<>(hashMap, HttpStatus.OK);
    }

    @GetMapping("/add-apurement-effective-date/{id}/{date}")
    public ResponseEntity<?> setApurementEffectiveDate(@PathVariable("id") Apurement apurement, @PathVariable("date") String dateStr) {
        HashMap<String, Object> hashMap = new HashMap<>();
        String errorMessage = null;
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            errorMessage = "Vous devez saisir une date valide !";
        }
        boolean isChanged = false;

        Transaction transaction = apurement.getTransaction();

        if (date != null && transaction != null && transaction.getDomiciliation() != null) {
            // On commence par nous rassurer que la domiciliation a un montant suffisant
            float newAmontDomiciliation = transaction.getDomiciliation().getMontantRestant() - transaction.getMontant();
            if (newAmontDomiciliation < 0) {
                hashMap.put("errorMessage", "Opération impossible le montant de la domiciliation est insuffisant");
                hashMap.put("isChanged", isChanged);
                return new ResponseEntity<>(hashMap, HttpStatus.OK);
            }
            // on determine la date d'expiration
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
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

            // On décrémente la domiciliation
            transaction.getDomiciliation().setMontantRestant(newAmontDomiciliation);

            // On met à jour l'apurement
            apurement.setDateExpiration(calendar.getTime());
            apurement.setStatus(StatusTransaction.APUREMENT_WAITING_FILES);
            apurement.setDateEffective(date);
            apurementService.saveApurement(apurement);
            isChanged = true;
            hashMap.put("dateText", new SimpleDateFormat("dd-MM-yyyy").format(date));
            hashMap.put("dataValue", new SimpleDateFormat("yyyy-MM-dd").format(date));
        }
        else if (errorMessage == null && transaction != null && transaction.getDomiciliation() == null) {
            errorMessage = "Action imposible! Cette transaction n'utilise aucune domiciliation";
        }

        hashMap.put("errorMessage", errorMessage);
        hashMap.put("isChanged", isChanged);
        return new ResponseEntity<>(hashMap, HttpStatus.OK);
    }


    /**
     * Cette fonction permet d'afficher la liste des fichiers manquant pour un dossier d'apurement
     * @param apurement
     * @return
     */
    @GetMapping("/rest-apurements/{id}/fichiers-manquants")
    public ResponseEntity<?> afficherFichiersManquants(@PathVariable("id") Apurement apurement) {
        String data = null;
        boolean hasData = false;
        if (apurement.getFichiersManquants() != null && !apurement.getFichiersManquants().isEmpty()){
            data = "";
            for (ApurementFichierManquant f : apurement.getFichiersManquants()) {
                if (f.getFile() == null && f.isForApurement()) {
                    hasData = true;
                    data += "<li>" + f.getFileName() + "</li>";
                }
            }
        }
        HashMap<String, Object> response = new HashMap<>();
        if (apurement.getIsApured()) {
            data = "Ce dossier est apuré !";
            hasData = true;
        }
        response.put("data", data);
        response.put("hasData", hasData);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/rest-rejeter-apurement/{id}/rejeter")
    public ResponseEntity<?> rejeterApurement(@PathVariable("id") Apurement apurement, @RequestParam("motif") String motifRejet) {
        System.out.println("=================================================================");
        System.out.println(motifRejet);
        System.out.println("=================================================================");
        HashMap<String, Object> response = new HashMap<>();
        if (apurement.getIsApured()) {
            response.put("error", true);
            response.put("message", "Cette transaction est déjà apurée ! Vous ne pouvez plus la rejetée.");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        System.out.println("=================================================================");
        System.out.println(motifRejet);
        System.out.println("=================================================================");

        apurement.setStatus(StatusTransaction.APUREMENT_REJETER);
        apurement.setMotifRejet(motifRejet);

        apurementService.saveApurement(apurement);

        response.put("error", false);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/rest-annuler-apurement/{id}/annuler")
    public ResponseEntity<?> annulerApurement(@PathVariable("id") Apurement apurement) {
        HashMap<String, Object> response = new HashMap<>();
        if (apurement.getIsApured()) {
            response.put("error", true);
            response.put("message", "Cette transaction est déjà apurée ! Vous ne pouvez plus l'annulé.");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        if (apurement.getStatus() == StatusTransaction.APUREMENT_ANULER) {
            response.put("error", true);
            response.put("message", "Cette transaction est déjà annulé !");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        apurement.setStatus(StatusTransaction.APUREMENT_ANULER);
        apurement.getTransaction().setStatut(StatusTransaction.APUREMENT_ANULER);
        apurementService.saveApurement(apurement);
        transactionService.saveTransaction(apurement.getTransaction());

        response.put("error", false);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/rest-restaurer-apurement/{id}/restaurer")
    public ResponseEntity<?> restaurerApurement(@PathVariable("id") Apurement apurement) {
        HashMap<String, Object> response = new HashMap<>();
        if (apurement.getIsApured()) {
            response.put("error", true);
            response.put("message", "Cette transaction est déjà apurée ! Vous ne pouvez plus l'annulé.");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        if (apurement.getStatus() == StatusTransaction.APUREMENT_ANULER) {
            response.put("error", true);
            response.put("message", "Cette transaction est déjà annulé ! Vous ne pouvez plus la restaurer");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }

        apurement.setStatus(StatusTransaction.APUREMENT_WAITING_DATE);
        apurement.getTransaction().setStatut(StatusTransaction.VALIDATED);
        apurementService.saveApurement(apurement);
        transactionService.saveTransaction(apurement.getTransaction());

        response.put("error", false);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Cette fonction permet d'extraire les donnees dans le fichier excel
     * @param fileName
     * @return
     * @throws IOException
     */
    private Collection<Collection<HashMap<String, Object>>> fetchData(String fileName) throws IOException {
        //Read XSL file
        FileInputStream inputStream = new FileInputStream(fileName);
        // Get the workbook instance for XLS file
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        int nombreDeClasseur = workbook.getNumberOfSheets();
        Collection<Collection<HashMap<String, Object>>> workbookData = new ArrayList<>();
        for (int i=0; i< nombreDeClasseur; i++) {
            XSSFSheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            Collection<HashMap<String, Object>> sheetData = new ArrayList<>();
            int r = 0;
            while (rowIterator.hasNext()) {
                if (r > 0) {
                    Row row = rowIterator.next();
                    Iterator<Cell> cellIterator = row.cellIterator();
                    int col = 0;
                    HashMap<String, Object> rowData = new HashMap<>();
                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();
                        Object cellValue;
                        switch (cell.getCellType()) {
                            case NUMERIC:
                                cellValue = cell.getNumericCellValue();
                                break;
                            case BOOLEAN:
                                cellValue = cell.getBooleanCellValue();
                                break;
                            case BLANK:
                            case _NONE:
                                cellValue = null;
                                break;
                            case STRING:
                                cellValue = cell.getStringCellValue();
                                break;
                            default:
                                cellValue = cell.getDateCellValue();
                                break;

                        }

                        switch (col) {
                            case 0:
                                rowData.put("marche", cellValue);
                                break;
                            case 1:
                                rowData.put("fdc", cellValue);
                                break;
                            case 2:
                                rowData.put("donneurOrdre", cellValue);
                                break;
                            case 3:
                                if (cellValue != null) {
                                    cellValue = cell.getDateCellValue();
                                }
                                rowData.put("dateOuverture", cellValue);
                                break;
                            case 4:
                                rowData.put("annee", cellValue);
                                break;
                            case 5:
                                rowData.put("mois", cellValue);
                                break;
                            case 6:
                                rowData.put("ndos", cellValue);
                                break;
                            case 7:
                                rowData.put("montant", cellValue);
                                break;
                            case 8:
                                rowData.put("ageClient", cellValue);
                                break;
                            case 9:
                                rowData.put("devise", cellValue);
                                break;
                            case 10:
                                rowData.put("mdev", cellValue);
                                break;
                            case 11:
                                rowData.put("tdev", cellValue);
                                break;
                            case 12:
                                rowData.put("motifTransaction", cellValue);
                                break;
                            case 13:
                                rowData.put("documentsManquants", ((String) cellValue).split(","));
                                break;
                            case 14:
                                rowData.put("delais", cellValue);
                                break;
                            case 15:
                                rowData.put("referenceInterne", cellValue);
                                break;
                            case 16:
                                rowData.put("beneficiaire", cellValue);
                                break;
                            case 17:
                                rowData.put("referenceClient", cellValue);
                                break;
                            case 18:
                                rowData.put("gestionnaire", cellValue);
                                break;

                        }

                        col++;
                    }
                    sheetData.add(rowData);
                }
                r++;
            }
            if (!sheetData.isEmpty()) {
                workbookData.add(sheetData);
            }

        }

        return workbookData;
    }

    /**
     * Cette fonction permet d'enregistrer les apurements
     * @param workbookData
     * @param banque
     * @return
     */
    private HashMap<String, Object> addApurements(Collection<Collection<HashMap<String, Object>>> workbookData, Banque banque) {
        String message = "";
        boolean hasError = false;
        for (Collection<HashMap<String, Object>> data : workbookData) {
            for (HashMap<String, Object> item : data) {
                // On verifie que le client existe
                Client client = clientService.getClientByReference((String) item.get("referenceClient"));
                Apurement apurement = apurementService.getApurementByReferenceTransaction((String) item.get("ndos"));
                if (client != null) {
                    String[] documentsManquants = (String[]) item.get("documentsManquants");
                    if (apurement == null) {
                        // On crée l'apurement
                        apurement = new Apurement();
                        apurement.setBanque(banque);
                        apurement.setClient(client);
                        apurement.setBeneficiaire((String) item.get("beneficiaire"));
                        apurement.setDevise((String) item.get("devise"));
                        apurement.setReferenceTransaction((String) item.get("ndos"));
                        Double fdc = (Double) item.get("fdc");
                        apurement.setFdc(String.valueOf(fdc));
                        apurement.setDateOuverture((Date) item.get("dateOuverture"));
                        Double montant = (Double) item.get("montant");
                        apurement.setMontant(montant.floatValue());
                        apurement.setIsApured(false);
                        apurement.setMotif((String) item.get("motifTransaction"));
                        apurement.setMarche((String) item.get("marche"));

                        Collection<ApurementFichierManquant> fichierManquants = new ArrayList<>();

                        for (String fmName : documentsManquants) {
                            ApurementFichierManquant fichierManquant = new ApurementFichierManquant();
                            fichierManquant.setFileName(fmName);
                            fichierManquant.setIsValidated(false);
                            fichierManquants.add(fichierManquant);
                            fichierManquant.setApurement(apurement);
                        }
                        if (!fichierManquants.isEmpty()) {
                            apurement.setFichiersManquants(fichierManquants);
                        }
                    }
                    else {
                        for (String d : documentsManquants) {
                            boolean exist = false;
                            for (ApurementFichierManquant fm : apurement.getFichiersManquants()) {
                                if (d.equals(fm.getFileName())) {
                                    exist = true;
                                }
                            }
                            if (!exist) {
                                ApurementFichierManquant afm = new ApurementFichierManquant();
                                afm.setApurement(apurement);
                                afm.setIsValidated(false);
                                afm.setFileName(d);
                                fichierManquantService.saveFichierManquant(afm);
                            }
                        }
                    }
                    apurement.setIsApured(false);
                    Apurement saved = apurementService.saveApurement(apurement);
                }
                else {
                    hasError = true;
                    message += "<p>Le client dont la reference est "+ item.get("referenceClient") +" n'existe pas dans veyuz</p>";
                }
            }
        }

        HashMap<String, Object> result = new HashMap<>();
        result.put("message", message);
        result.put("hasError", hasError);

        return result;
    }

    /**
     * Cette fonction enverra les emails
     * @param workbookData
     * @param banque
     * @return
     */
    private String sendMail(Collection<Collection<HashMap<String, Object>>> workbookData, Banque banque) {
        Collection<String> emails = new ArrayList<>();
        Collection<HashMap<String, Object>> data = new ArrayList<>();
        HashMap<String, Collection<Object>> transactionList = new HashMap<>();
        for (Collection<HashMap<String, Object>> d : workbookData) {
            for (HashMap<String, Object> item : d) {
                String refClient = (String) item.get("referenceClient");
                Client client = clientService.getClientByReference(refClient);
                if (client != null) {
                    String email = client.getUser().getEmail();
                    if (!transactionList.containsKey(email)) {
                        Collection<Object> apurements = new ArrayList<>();
                        apurements.add(item);
                        transactionList.put(email, apurements);
                        emails.add(email);
                    }else {
                        transactionList.get(email).add(item);
                    }
                }
            }
        }

        Context ctx = new Context();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String dateJour = sdf.format(new Date());
        String htmlContent = null;
        String results = "";
        for (String e : emails) {
            AppUser user = userService.getUserByEmail(e);
            ctx.setVariable("data", transactionList.get(e));
            ctx.setVariable("dateJour", dateJour);
            ctx.setVariable("denominationClient", user.getClient().getDenomination());
            ctx.setVariable("adresseClient", user.getAdresse());
            ctx.setVariable("nomPrenom", "");
            ctx.setVariable("banque", banque);
            htmlContent = templateEngine.process("lettre/misedemeure_3", ctx);
            results += htmlContent;
//            sendMail(htmlContent, e);
        }

        return results;
    }

}

















