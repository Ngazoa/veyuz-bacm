package com.akouma.veyuzwebapp.service;

import com.akouma.veyuzwebapp.form.ImportFileForm;
import com.akouma.veyuzwebapp.model.*;
import com.akouma.veyuzwebapp.repository.*;
import com.akouma.veyuzwebapp.utils.StatusTransaction;
import com.akouma.veyuzwebapp.utils.Upload;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class TransactionService {

    @Autowired
    FileStorageService fileStorageService;
    @Autowired
    DeviseRepository deviseRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private TypeDeTransactionRepository typeDeTransactionRepository;
    @Autowired
    private BeneficiaireRepository beneficiaireRepository;
    @Autowired
    private DomiciliationRepository domiciliationRepository;

    public TransactionRepository getTransactionRepository() {
        return transactionRepository;
    }

    public void setTransactionRepository(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public ClientRepository getClientRepository() {
        return clientRepository;
    }

    public void setClientRepository(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public TypeDeTransactionRepository getTypeDeTransactionRepository() {
        return typeDeTransactionRepository;
    }

    public void setTypeDeTransactionRepository(TypeDeTransactionRepository typeDeTransactionRepository) {
        this.typeDeTransactionRepository = typeDeTransactionRepository;
    }

    public BeneficiaireRepository getBeneficiaireRepository() {
        return beneficiaireRepository;
    }

    public void setBeneficiaireRepository(BeneficiaireRepository beneficiaireRepository) {
        this.beneficiaireRepository = beneficiaireRepository;
    }

    public DomiciliationRepository getDomiciliationRepository() {
        return domiciliationRepository;
    }

    public void setDomiciliationRepository(DomiciliationRepository domiciliationRepository) {
        this.domiciliationRepository = domiciliationRepository;
    }

    public Optional<Transaction> getTransaction(final Long id) {

        return transactionRepository.findById(id);
    }

    public Transaction saveTransaction(Transaction transaction) {
        Transaction savedTransaction = transactionRepository.save(transaction);
        return savedTransaction;
    }

    public void deleteTransaction(final Transaction transaction) {
        if (transaction.getStatut() == StatusTransaction.WAITING)
            transactionRepository.delete(transaction);
    }

    public Iterable<Transaction> getTransactions(Banque banque) {
        return transactionRepository.findByBanqueOrderByDateCreationDesc(banque);
    }

    public Iterable<Transaction> getTransactionsClient(Banque banque, Client client) {
        return transactionRepository.findByBanqueAndClientOrderByDateCreationDesc(banque, client);
    }

    public List<Transaction> checkIfHasNotTransactionPendingTransaction(Banque banque, Client client) {
        return transactionRepository.
                findByBanqueAndClientAndTypeDeTransaction_IsImportAndHasSaction(banque, client,
                        true, true);
    }


    // ===============================================================================================================|
    //                              GESTION DES REQUETES LIEES A L'AFFICHAGE DES TRANSACTION                          |
    // ===============================================================================================================|

    /**
     * Cette fonction permet de recuperer la liste des transactions effectuees dans une banque donnee
     *
     * @param banque
     * @param maxResults
     * @param page
     * @return
     */
    public Page<Transaction> getAllTransactionsForBanque(Banque banque, int maxResults, int page) {
        return transactionRepository.findByBanqueAndHasFilesOrderByDateCreationDesc(banque, true, PageRequest.of(page, maxResults));
    }

    public Page<Transaction> getAllTransactionsForBanqueAgence(Banque banque, Agence user, int maxResults, int page) {
        return transactionRepository.findByBanqueAndAgenceAndHasFilesOrderByDateCreationDesc(banque, user, true, PageRequest.of(page, maxResults));
    }

    /**
     * Cette fonction permet de recuperer les transactions effectuees avant une date donnee dans une banque
     *
     * @param banque
     * @param date
     * @param maxResults
     * @param page
     * @return
     */
    public Iterable<Transaction> getAllTransactionsForBanqueBeforeDate(Banque banque, Date date, int maxResults, int page) {
        return transactionRepository.findByBanqueAndDateCreationBeforeOrderByDateCreationDesc(banque, date);
    }

    public boolean checkReference(String ref) {
        Transaction transaction
                = transactionRepository.findByReference(ref);
        if (transaction == null) {
            return false;
        }
        return true;
    }

    public String generateTransactionNumber(Banque banque) {
        // Get the current year
        LocalDate date = LocalDate.now();
        int year = date.getYear();
        String currentYear = String.valueOf(year).substring(2);
        // Retrieve the last transaction for the current year from the database
        Optional<Transaction> lastTransaction = transactionRepository.findFirstByBanqueAndReferenceIsNotNullOrderByIdDesc(banque);
        long lastTransactionLenth = transactionRepository.countByBanqueAndReferenceIsNotNull(banque);
        // Initialize the serial number
        long serialNumber = 1;

        // Increment the serial number if a last transaction exists for the current year
        if (lastTransaction.isPresent()) {
            Transaction lastTransactionEntity = lastTransaction.get();
            long lastSerialNumber = lastTransactionEntity.getId();

            String lstref=lastTransactionEntity.getReference();
            lstref = lstref.replaceAll("[^0-9]", "");

            String lastTwoDigits = lstref.substring(lstref.length() - 2);

            // Check if the current year is different from the year of the last transaction
            if (currentYear.trim().equals(lastTwoDigits.trim())) {
                serialNumber = lastTransactionLenth + 1;
            }
        }

        // Format the serial number with leading zeros
        String formattedSerialNumber = String.format("%04d", serialNumber);

        // Generate the transaction number by concatenating the formatted serial number with the last 2 digits of the current year
        String transactionNumber = formattedSerialNumber + currentYear;
        System.out.println("*** "+transactionNumber);

        return "TT"+transactionNumber+"BA";
    }

    /**
     * Cette fonction permet de recuperer les transactions effectuees apres une date donnee dans une banque
     *
     * @param banque
     * @param date
     * @param maxResults
     * @param page
     * @return
     */
    public Iterable<Transaction> getAllTransactionsForBanqueAfterDate(Banque banque, Date date, int maxResults, int page) {
        return transactionRepository.findByBanqueAndDateCreationAfterOrderByDateCreationDesc(banque, date);
    }

    /**
     * Cette fonction permet de lister toutes les transactions en deux dates dans une banque donnee
     *
     * @param banque
     * @param dateInf
     * @param dateSup
     * @param maxResults
     * @param page
     * @return
     */
    public Iterable<Transaction> getAllTransactionsForBanqueBetweenDates(Banque banque, Date dateInf, Date dateSup, int maxResults, int page) {
        return transactionRepository.findByBanqueAndDateCreationBetweenOrderByDateCreationDesc(banque, dateInf, dateSup);
    }

    /**
     * Cette fonction permet de recuperer la liste des transactions effectuees par un client
     * dans une banque
     *
     * @param banque
     * @param client
     * @param maxResults
     * @param page
     * @return
     */
    public Page<Transaction> getPageableTransactionsForClient(Banque banque, Client client, int maxResults, int page) {
        return transactionRepository.findByBanqueAndClientOrderByDateCreationDesc(banque, client, PageRequest.of(page, maxResults));
    }

    /**
     * Cette fonction permet de recuperer les transactions effectuées par un client dans une banque donnee en
     * utilisant un domiciliation donnee.
     *
     * @param banque
     * @param client
     * @param domiciliation
     * @param maxResults
     * @param page
     * @return
     */
    public Page<Transaction> getTransactionsClientByDomiciliation(Banque banque, Client client, Domiciliation domiciliation, int maxResults, int page) {
        return transactionRepository.findByBanqueAndClientAndDomiciliationOrderByDateCreationDesc(banque, client, domiciliation, client, PageRequest.of(page, maxResults));
    }

    public boolean changeTransactionStatus(Transaction transaction, int status) {
        boolean isChanged;
        switch (status) {
            case StatusTransaction.WAITING:
            case StatusTransaction.MACKED:
            case StatusTransaction.CHECKED:
            case StatusTransaction.VALIDATED:
            case StatusTransaction.REJECTED:

            case StatusTransaction.SENDBACK_MACKER:
            case StatusTransaction.SENDBACK_CUSTOMER:
                transaction.setStatut(status);
                isChanged = true;
                break;
            default:
                isChanged = false;
        }
        return isChanged;
    }

    public long getcountByAgenceAndStatut(Banque banque,Agence agence,int statut){
        return transactionRepository.countByBanqueAndAgenceAndStatut(banque,agence,statut);
    }

    public long getcountByAgence(Banque banque,Agence agence){
        return transactionRepository.countByBanqueAndAgence(banque,agence);
    }

    public Page<Transaction> getTransactionsByStatus(Banque banque, String status, int max, int page, Client client,
                                                     Agence agence) {
        int statut = -2;
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
            case StatusTransaction.SENDBACK_MACKER_STR:
                statut = StatusTransaction.SENDBACK_MACKER;
                break;
            case StatusTransaction.SENDBACK_CUSTOMER_STR:
                statut = StatusTransaction.SENDBACK_CUSTOMER;
                break;
            case StatusTransaction.TOPS1_STR:
                statut = StatusTransaction.TRANSMIS_MAKER_2;
                break;
            case StatusTransaction.TRESORERIE_STR:
                statut = StatusTransaction.TRANSMIS_TRESORERIE;
                break;
            case StatusTransaction.TOPS2_STR:
                statut = StatusTransaction.TRANSMIS_CHECKER_2;
                break;
            case "sendback":
                statut = 10;
                break;
            default:
                statut = -2;
                break;
        }
        if (client == null) {
            if (statut == 10) {
                return transactionRepository.findByBanqueAndHasFilesAndStatutOrStatutOrderByDelayDesc(banque, true, StatusTransaction.SENDBACK_CUSTOMER, StatusTransaction.SENDBACK_MACKER, PageRequest.of(page, max));
            } else {
                if (agence != null) {
                    return transactionRepository.findByBanqueAndAgenceAndStatutOrderByDateCreationDesc(banque, agence, statut, PageRequest.of(page, max));
                }
                return transactionRepository.findByBanqueAndStatutOrderByDateCreationDesc(banque, statut, PageRequest.of(page, max));
            }
        }
        if (statut == 10) {
            statut = StatusTransaction.SENDBACK_CUSTOMER;
//            return transactionRepository.findByBanqueAndClientAndStatutOrderByDelayDesc(banque, client, StatusTransaction.SENDBACK_CUSTOMER, PageRequest.of(page, max));
        }
        if (agence != null) {
            return transactionRepository.findByBanqueAndAgenceAndStatutOrderByDateCreationDesc(banque, agence, statut, PageRequest.of(page, max));
        }

        return transactionRepository.findByBanqueAndClientAndStatutOrderByDateCreationDesc(banque, client, statut, PageRequest.of(page, max));
    }

    private HashMap<String, Object> formatTransactions(Iterable<Transaction> all) {
        Collection<HashMap<String, Object>> appurements = new ArrayList<>();
        Calendar dateDuJour = Calendar.getInstance();

        for (Transaction t : all) {
            HashMap<String, Object> map = new HashMap<String, Object>();
//            long diff = dateDuJour.getTime().getTime() - t.getDateCreation().getTime();
            long diff = t.getDelay().getTime() - dateDuJour.getTime().getTime();
            int nbJourAttente = 20;
            if (t.getTypeDeTransaction().getType() != null) {
                if (t.getTypeDeTransaction().getType().equals(StatusTransaction.IMP_BIENS)) {
                    nbJourAttente = StatusTransaction.DELAY_TRANSACTION_IMPORTATION_BIENS;
                } else if (t.getTypeDeTransaction().getType().equals(StatusTransaction.IMP_SERVICES)) {
                    nbJourAttente = StatusTransaction.DELAY_TRANSACTION_IMPORTATION_SERVICES;
                }
            }
            Long nbJrsRestant = nbJourAttente - TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
            String state = "En cours d'apurement";
            int etat = 0;
            if (t.getStatut() == StatusTransaction.VALIDATED) {
                state = "Apurée";
                etat = 1;
            } else {
                if (dateDuJour.getTime().getTime() > t.getDelay().getTime()) {
                    state = "Non apurée";
                    etat = -1;
                }
            }


            if (nbJrsRestant < 0) {
                nbJrsRestant = Long.valueOf(0);
                state = "Expirée";
            }
            String nbj = String.valueOf(nbJrsRestant);
            if (t.getStatut() == StatusTransaction.VALIDATED) {
                nbj = "-";
            }
            map.put("transaction", t);
            map.put("nbJrsRestant", nbj);
            map.put("state", state);
            map.put("etat", etat);
            appurements.add(map);
        }
        HashMap<String, Object> maps = new HashMap<>();
        maps.put("appurements", appurements);

        return maps;
    }

    private HashMap<String, Object> formatTransactions(Page<Transaction> all) {
        Collection<Transaction> transactions = all.getContent();
        Collection<HashMap<String, Object>> appurements = new ArrayList<>();
        Calendar dateDuJour = Calendar.getInstance();

        for (Transaction t : transactions) {
            HashMap<String, Object> map = new HashMap<String, Object>();
//            long diff = dateDuJour.getTime().getTime() - t.getDateCreation().getTime();
            Date delay = t.getDelay() == null ? new Date() : t.getDelay();
            long diff = delay.getTime() - dateDuJour.getTime().getTime();
            int nbJourAttente = 20;
            if (t.getTypeDeTransaction().getType() != null) {
                if (t.getTypeDeTransaction().getType().equals(StatusTransaction.IMP_BIENS)) {
                    nbJourAttente = StatusTransaction.DELAY_TRANSACTION_IMPORTATION_BIENS;
                } else if (t.getTypeDeTransaction().getType().equals(StatusTransaction.IMP_SERVICES)) {
                    nbJourAttente = StatusTransaction.DELAY_TRANSACTION_IMPORTATION_SERVICES;
                }
            }
            Long nbJrsRestant = nbJourAttente - TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
            String state = "En cours d'apurement";
            int etat = 0;
            if (t.getStatut() == StatusTransaction.VALIDATED) {
                state = "Apurée";
                etat = 1;
            } else {
                Date delai = t.getDelay() == null ? new Date() : t.getDelay();
                if (dateDuJour.getTime().getTime() > delai.getTime()) {
                    state = "Non apurée";
                    etat = -1;
                }
            }


            if (nbJrsRestant < 0) {
                nbJrsRestant = Long.valueOf(0);
                state = "Expirée";
            }
            String nbj = String.valueOf(nbJrsRestant);
            if (t.getStatut() == StatusTransaction.VALIDATED) {
                nbj = "-";
            }
            map.put("transaction", t);
            map.put("nbJrsRestant", nbj);
            map.put("state", state);
            map.put("etat", etat);
            appurements.add(map);
        }
        HashMap<String, Object> maps = new HashMap<>();
        maps.put("pageable", all);
        maps.put("appurements", appurements);
        maps.put("nbElements", all.getTotalElements());
        maps.put("currentPage", all.getNumber() + 1);
        maps.put("nbPages", all.getTotalPages());

        return maps;
    }

    public HashMap<String, Object> getTransactionsAsReporting(Banque banque, int max, int page) {
        Page<Transaction> all = getAllTransactionsForBanque(banque, max, page);

        return formatTransactions(all);
    }

    public HashMap<String, Object> getTransactionsAsReporting(Banque banque, Client client, int max, int page) {
        Page<Transaction> all = transactionRepository.findByBanqueAndClientOrderByDateCreationDesc(banque, client, PageRequest.of(page, max));

        return formatTransactions(all);
    }

    public HashMap<String, Object> getApurements(Banque banque, boolean isImport, int max, int page) {
        Page<Transaction> transactions = transactionRepository.findByBanqueAndTypeDeTransactionIsImport(banque, isImport, PageRequest.of(page, max));

        return formatTransactions(transactions);
    }

    public HashMap<String, Object> getApurements(Banque banque, Client client, boolean isImport, int max, int page) {
        Page<Transaction> transactions = transactionRepository.findByBanqueAndClientAndTypeDeTransactionIsImport(banque, client, isImport, PageRequest.of(page, max));

        return formatTransactions(transactions);
    }

    public HashMap<String, Object> getAllApurements(Banque banque, boolean isImport) {
        Iterable<Transaction> transactions = transactionRepository.findByBanqueAndTypeDeTransactionIsImport(banque, isImport);

        return formatTransactions(transactions);
    }

    public String importData(ImportFileForm importFile, HttpServletRequest request) throws IOException {
        Upload u = new Upload();
        String uploadRootPath = "/tmp";
        String file_root = u.uploadFile(importFile.getFile(), fileStorageService, uploadRootPath, request);

        String msg = "";

        //Read XSL file
        FileInputStream inputStream = new FileInputStream(Upload.UPLOAD_DIR + "/tmp/" + file_root);

        // Get the workbook instance for XLS file
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);

        // Get first sheet from the workbook
        XSSFSheet sheet = workbook.getSheetAt(0);

        // Get iterator to all the rows in current sheet
        Iterator<Row> rowIterator = sheet.iterator();

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            // Get iterator to all cells of current row
            Iterator<Cell> cellIterator = row.cellIterator();
            Transaction transaction = new Transaction();
            transaction.setBanque(importFile.getBanque());
            int cmp = 0;
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                switch (cmp) {
                    case 0:
                        transaction.setClient(clientRepository.findFirstByReference(cell.getStringCellValue()));
                        break;
                    case 1:
                        transaction.setReference(cell.getStringCellValue());
                        break;
                    case 2:
                        transaction.setMontant((float) cell.getNumericCellValue());
                        break;
                    case 3:
                        transaction.setDevise(deviseRepository.findFirstByCode(cell.getStringCellValue()));
                        break;
                    case 4:
                        transaction.setStatut((int) cell.getNumericCellValue());
                        break;
                    case 5:
                        transaction.setTypeDeTransaction(typeDeTransactionRepository.findFirstByCode(cell.getStringCellValue()));
                        break;
                    case 6:
                        transaction.setBeneficiaire(beneficiaireRepository.findFirstByReference(cell.getStringCellValue()));
                        break;
                    case 7:
                        transaction.setDomiciliation(domiciliationRepository.findFirstByReference(cell.getStringCellValue()));
                        break;
                    case 8:
                        transaction.setDateCreation(cell.getDateCellValue());
                        break;
                }
                cmp++;
            }

            if (true) {
                transactionRepository.save(transaction);
            } else {
                msg += "Impossible de sauvegarder cette transaction";
            }
        }

        return msg;

    }

    public Iterable<Transaction> getNonValidatedAndRejectedClientTransactions(Banque banque, Client client, int statut) {
        return transactionRepository.findByBanqueEqualsAndClientEqualsAndStatutNot(banque, client, statut);
    }

    public Page<Transaction> getPageableTransactionsHasFilesForClient(Banque banque, Client client, boolean hasFiles, int max, Integer page) {
        return transactionRepository.findByBanqueAndClientAndHasFilesOrderByDateCreationDesc(banque, client, hasFiles, PageRequest.of(page, max));
    }

    public Page<Transaction> getPageableTransactionsHasFilesForAgence(Banque banque, Agence client, boolean hasFiles, int max, Integer page) {
        return transactionRepository.findByBanqueAndAgenceAndHasFilesOrderByDateCreationDesc(banque, client, hasFiles, PageRequest.of(page, max));
    }

    /**
     * On utilise cette fonction pour la generation des fichiers
     *
     * @param banque
     * @param hasFiles
     * @param client
     * @param dateMin
     * @param dateMax
     * @param statut
     * @return
     */
    public Iterable<Transaction> getBanqueTransaction(Banque banque, boolean hasFiles, @Nullable Client client, @Nullable Date dateMin, @Nullable Date dateMax, @Nullable String statut) {
        Iterable<Transaction> transactions = null;
        int state = 0;
        if (statut != null) {
            switch (statut) {
                case StatusTransaction.WAITING_STR:
                    state = StatusTransaction.WAITING;
                    break;
                case StatusTransaction.MACKED_STR:
                    state = StatusTransaction.MACKED;
                    break;
                case StatusTransaction.VALIDATED_STR:
                    state = StatusTransaction.VALIDATED;
                    break;
                case StatusTransaction.REJECTED_STR:
                    state = StatusTransaction.REJECTED;
                    break;
                case StatusTransaction.CHECKED_STR:
                    state = StatusTransaction.CHECKED;
                    break;
            }
        }

        if (client == null && dateMin == null && dateMax == null && statut == null) {
            transactions = transactionRepository.findByBanqueAndHasFiles(banque, hasFiles);
        } else if (client != null && statut == null) {
            transactions = transactionRepository.findByBanqueAndClientAndHasFiles(banque, client, hasFiles);
        } else if (client != null && statut != null) {
            transactions = transactionRepository.findByBanqueAndClientAndHasFilesAndStatut(banque, client, hasFiles, state);
        } else if (statut != null && dateMax == null && dateMin == null && client == null) {
            transactions = transactionRepository.findByBanqueAndHasFilesAndStatut(banque, hasFiles, state);
        } else if (dateMin != null && dateMax != null && client == null && statut == null) {
//            findByBanqueAndDateCreationBetweenOrderByDateCreationDesc
            transactions = transactionRepository.findByBanqueAndHasFilesAndDateCreationBetween(banque, hasFiles, dateMin, dateMax);
        } else if (dateMin != null && dateMax != null && client == null && statut != null) {
            transactions = transactionRepository.findByBanqueAndHasFilesAndStatutAndDateCreationBetween(banque, hasFiles, state, dateMin, dateMax);
        } else if (client == null && statut != null && dateMin != null && dateMax == null) {

        } else if (client == null && statut == null && dateMin != null && dateMax == null) {

        } else if (client == null && statut != null && dateMin == null && dateMax != null) {

        } else if (client == null && statut == null && dateMin == null && dateMax != null) {

        }


        return transactions;
    }

    public HashMap<String, Object> getByCustomProps(Banque banque, int max, Integer page, String deviseCode, String typeCode, String statut, String date1Str, String date2Str) throws ParseException {
        Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(date1Str);
        Date date2 = new SimpleDateFormat("yyyy-MM-dd").parse(date2Str);
        Devise devise = null;
        if (deviseCode != null) {
            devise = deviseRepository.findFirstByCode(deviseCode);
        }
        TypeDeTransaction typeDeTransaction = null;
        if (typeCode != null) {
            typeDeTransaction = typeDeTransactionRepository.findFirstByCode(typeCode);
        }
        int state = -5;
        switch (statut) {
            case StatusTransaction.CHECKED_STR:
                state = StatusTransaction.CHECKED;
                break;
            case StatusTransaction.MACKED_STR:
                state = StatusTransaction.MACKED;
                break;
            case StatusTransaction.WAITING_STR:
                state = StatusTransaction.WAITING;
                break;
            case StatusTransaction.VALIDATED_STR:
                state = StatusTransaction.VALIDATED;
                break;
        }

        Page<Transaction> transactions;

        if (devise == null && typeDeTransaction == null && state != -5) {
            System.out.println("Recherche 1");
            transactions = transactionRepository.findByBanqueAndHasFilesAndStatutAndTypeDeTransactionIsImportAndDateCreationBetween(banque, true, state, true, date1, date2, PageRequest.of(page, max));
        } else if (devise != null && typeDeTransaction == null && state == -5) {
            System.out.println("Recherche 2");
            transactions = transactionRepository.findByBanqueAndHasFilesAndTypeDeTransactionIsImportAndDeviseAndDateCreationBetween(banque, true, true, devise, date1, date2, PageRequest.of(page, max));
        } else if (devise != null && typeDeTransaction != null && state == -5) {
            System.out.println("Recherche 3");
            transactions = transactionRepository.findByBanqueAndHasFilesAndTypeDeTransactionIsImportAndDeviseAndTypeDeTransactionAndDateCreationBetween(banque, true, true, devise, typeDeTransaction, date1, date2, PageRequest.of(page, max));
        } else if (devise != null && typeDeTransaction == null && state != -5) {
            System.out.println("Recherche 4");
            transactions = transactionRepository.findByBanqueAndHasFilesAndTypeDeTransactionIsImportAndDeviseAndStatutAndDateCreationBetween(banque, true, true, devise, state, date1, date2, PageRequest.of(page, max));
        } else if (devise != null && typeDeTransaction != null && state != -5) {
            System.out.println("Recherche 5");
            transactions = transactionRepository.findByBanqueAndHasFilesAndTypeDeTransactionIsImportAndDeviseAndTypeDeTransactionAndStatutAndDateCreationBetween(banque, true, true, devise, typeDeTransaction, state, date1, date2, PageRequest.of(page, max));
        } else if (devise == null && typeDeTransaction != null && state != -5) {
            System.out.println("Recherche 6");
            transactions = transactionRepository.findByBanqueAndHasFilesAndTypeDeTransactionIsImportAndTypeDeTransactionAndStatutAndDateCreationBetween(banque, true, true, typeDeTransaction, state, date1, date2, PageRequest.of(page, max));
        } else if (devise == null && typeDeTransaction != null && state == -5) {
            System.out.println("Recherche 7");
            transactions = transactionRepository.findByBanqueAndHasFilesAndTypeDeTransactionIsImportAndTypeDeTransactionAndDateCreationBetween(banque, true, true, typeDeTransaction, date1, date2, PageRequest.of(page, max));
        } else {
            System.out.println("Recherche 8");
            transactions = transactionRepository.findByBanqueAndHasFilesAndTypeDeTransactionIsImportAndDateCreationBetween(banque, true, true, date1, date2, PageRequest.of(page, max));
        }

        return formatTransactions(transactions);
    }

    public Transaction getTransactionByReference(String reference) {
        return transactionRepository.findFirstByReference(reference);
    }

    public Iterable<Transaction> getByDate(Banque banque, Client client, Date date) {
        if (client == null) {
            return transactionRepository.findByBanqueAndDateCreationAndHasFilesTrueAndStatutNot(banque, date, StatusTransaction.REJECTED);
        }
        return transactionRepository.findByBanqueAndClientAndDateCreationAndHasFilesTrueAndStatutNot(banque, client, date, StatusTransaction.REJECTED);
    }

    public Iterable<Transaction> getByBetweenDate(Banque banque, Client client, Date dateJourMin, Date dateJourMax, Devise devise) {
        if (client == null) {
            if (devise != null) {
                return transactionRepository.findByBanqueAndHasFilesTrueAndStatutNotAndDateCreationBetweenAndDevise(banque, StatusTransaction.REJECTED, dateJourMin, dateJourMax, devise);
            }
            return transactionRepository.findByBanqueAndHasFilesTrueAndStatutNotAndDateCreationBetween(banque, StatusTransaction.REJECTED, dateJourMin, dateJourMax);
        }
        if (devise != null) {
            return transactionRepository.findByBanqueAndClientAndHasFilesTrueAndStatutNotAndDateCreationBetweenAndDevise(banque, client, StatusTransaction.REJECTED, dateJourMin, dateJourMax, devise);
        }
        return transactionRepository.findByBanqueAndClientAndHasFilesTrueAndStatutNotAndDateCreationBetween(banque, client, StatusTransaction.REJECTED, dateJourMin, dateJourMax);
    }


}