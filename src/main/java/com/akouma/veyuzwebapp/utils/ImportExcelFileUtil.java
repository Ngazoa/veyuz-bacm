package com.akouma.veyuzwebapp.utils;

import com.akouma.veyuzwebapp.form.ImportFileForm;
import com.akouma.veyuzwebapp.model.*;
import com.akouma.veyuzwebapp.repository.*;
import com.akouma.veyuzwebapp.service.AgenceService;
import com.akouma.veyuzwebapp.service.ClientService;
import com.akouma.veyuzwebapp.service.FileStorageService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Component
public class ImportExcelFileUtil {
    private final     FileStorageService fileStorageService;

    private final   ClientRepository clientRepository;

    private final  DeviseRepository deviseRepository;

    private final   TransactionRepository transactionRepository;

    private final  TypeDeTransactionRepository typeDeTransactionRepository;

    private final   BeneficiaireRepository beneficiaireRepository;

    private final   DomiciliationRepository domiciliationRepository;

    private final  ClientService clientService;

    private final  AgenceService agenceService;

    public ImportExcelFileUtil(FileStorageService fileStorageService,
                               ClientRepository clientRepository,
                               DeviseRepository deviseRepository,
                               TransactionRepository transactionRepository,
                               TypeDeTransactionRepository typeDeTransactionRepository,
                               BeneficiaireRepository beneficiaireRepository,
                               DomiciliationRepository domiciliationRepository,
                               ClientService clientService, AgenceService agenceService) {
        this.fileStorageService = fileStorageService;
        this.clientRepository = clientRepository;
        this.deviseRepository = deviseRepository;
        this.transactionRepository = transactionRepository;
        this.typeDeTransactionRepository = typeDeTransactionRepository;
        this.beneficiaireRepository = beneficiaireRepository;
        this.domiciliationRepository = domiciliationRepository;
        this.clientService = clientService;
        this.agenceService = agenceService;
    }

    public static Object getCellValue(Cell cell, Workbook workbook) {
        if (cell != null) {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return cell.getDateCellValue();
                    } else {
                        return cell.getNumericCellValue();
                    }
                case BOOLEAN:
                    return cell.getBooleanCellValue();
                case FORMULA:
                    FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                    CellValue cellValue = evaluator.evaluate(cell);
                    return cellValue.getNumberValue();
                case BLANK:
                    return " ";
                default:
                    return null;
            }
        }
        return null;
    }

    /**
     * CETTE FONCTION NOUS PERMET D'ANALYSER LE FICHIER EXCEL UTILISE PAR LA BANQUE POUR ENVOYER
     * LES LETTRES DE MISE EN DEMEURE. L'OBJECTIF EST DONC DE RECUPERER TOUTES LES DONNEES QUI
     * FIGURENT DANS CE FICHIER ET DE CONSTRUIRE LES APUREMENTS QUI DOIVENT ETRE GERER PAR
     * LE CLIENT CONCERNE
     *
     *
     */
    public String importApurements(ImportFileForm importFile, HttpServletRequest request) throws IOException {
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
                msg += "Imposible de sauvegarder cette transaction";
            }
        }
        return msg;

    }

    public List<Beneficiaire> readExcelBeneficiaireFile(MultipartFile file, Banque banque) {
        List<Beneficiaire> beneficiaryList = new ArrayList<>();

        try {
            InputStream inputStream = file.getInputStream();
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
        /*
        refence 0
        nom 1
        beneficiaire code 2
        */
            int pos = 0;
            for (Row row : sheet) {
                if (pos > 0) {
                    Beneficiaire beneficiaire = new Beneficiaire();
                    beneficiaire.setBanque(banque);
                    beneficiaire.setId(Long.valueOf(String.valueOf(getCellValue(row.getCell(2), workbook))));
                    beneficiaire.setName(String.valueOf(getCellValue(row.getCell(1), workbook)));
                    beneficiaire.setReference((String) getCellValue(row.getCell(0), workbook));
                    beneficiaryList.add(beneficiaire);
                }
                pos++;
            }
            workbook.close();
            inputStream.close();
        } catch (com.itextpdf.io.IOException | FileNotFoundException e) {
            System.out.println(" ERRROR " + e);
            return null;
            // handle the exception
        } catch (java.io.IOException e) {
            System.out.println("=====>  " + e);
        }
        return beneficiaryList;
    }

    public List<Domiciliation> readExcelFileDomiciliation(MultipartFile file, Banque banque) {
        List<Domiciliation> domiciliationList = new ArrayList<>();

        try {
            InputStream inputStream = file.getInputStream();
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            /*
             * code beneficiaire 0
             *id  client 1
             * code devise 2
             * montant 3
             * reference 4
             * dateCreation 5
             * false or true typeImportation 6
             *  code typeTransaction 7
             *
             * */
            int pos = 0;
            for (Row row : sheet) {
                if (pos > 0) {
                    Domiciliation domiciliation = new Domiciliation();
                    domiciliation.setClient(clientService.findById((Long) getCellValue(row.getCell(1), workbook)));
                    domiciliation.setBanque(banque);
                    domiciliation.setDevise(deviseRepository.findFirstByCode((String) getCellValue(row.getCell(2), workbook)));
                    domiciliation.setMontant((Float) getCellValue(row.getCell(3), workbook));
                    domiciliation.setReference(String.valueOf(getCellValue(row.getCell(4), workbook)));
                    domiciliation.setDateCreation((Date) getCellValue(row.getCell(5), workbook));
                    domiciliation.setImportation((Boolean) getCellValue(row.getCell(6), workbook));
                    domiciliation.setTypeDeTransaction(typeDeTransactionRepository.findFirstByCode((String) getCellValue(row.getCell(7), workbook)));
                    domiciliation.setBeneficiaire(beneficiaireRepository.findFirstByReference(String.valueOf(getCellValue(row.getCell(0), workbook))));
                    domiciliationList.add(domiciliation);
                }
                pos++;
            }
            workbook.close();
            inputStream.close();
        } catch (com.itextpdf.io.IOException | FileNotFoundException e) {
            System.out.println(" ERRROR " + e);
            return null;
            // handle the exception
        } catch (java.io.IOException e) {
            System.out.println("=====>  " + e);
        }
        return domiciliationList;
    }

    public List<Agence> readExcelFileAgency(MultipartFile file, Banque banque) {
        List<Agence> agencyArrayList = new ArrayList<>();

        try {
            InputStream inputStream = file.getInputStream();
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            /*
             * label 0
             * telephone 2
             * address 1
             * */
            int pos = 0;
            for (Row row : sheet) {
                if (pos > 0) {
                    Agence agence = new Agence();
                    agence.setLabel(String.valueOf(getCellValue(row.getCell(0), workbook)));
                    agence.setStatus(true);
                    agence.setPhone(String.valueOf(getCellValue(row.getCell(2), workbook)));
                    agence.setAddress(String.valueOf(getCellValue(row.getCell(1), workbook)));
                    agencyArrayList.add(agence);
                }
                pos++;
            }
            workbook.close();
            inputStream.close();
        } catch (com.itextpdf.io.IOException | FileNotFoundException e) {
            System.out.println(" ERRROR " + e);
            return null;
            // handle the exception
        } catch (java.io.IOException e) {
            System.out.println("=====>  " + e);
        }
        return agencyArrayList;
    }

    public List<Client> readExcelFileClient(MultipartFile file, Banque banque) {
        List<Client> clientList = new ArrayList<>();
        List<Banque> banqueList = new ArrayList<>();
        banqueList.add(banque);
        try {
            InputStream inputStream = file.getInputStream();
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            int pos = 0;
            /*
             * id agence 0
             * denomination 1
             * Numerocontribuable 2
             * telephone 3
             *
             * */

            for (Row row : sheet) {
                if (pos > 0) {
                    Client client = new Client();
                    client.setDenomination(String.valueOf(getCellValue(row.getCell(1), workbook)));
                    client.setBanques(banqueList);
                    client.setNumeroContribuable(String.valueOf(getCellValue(row.getCell(2), workbook)));
                    client.setTelephone(String.valueOf(getCellValue(row.getCell(3), workbook)));
                    client.setAgence(agenceService.getAgenceById((Long) getCellValue(row.getCell(0), workbook)));
                    clientList.add(client);
                }
                pos++;
            }
            workbook.close();
            inputStream.close();
        } catch (com.itextpdf.io.IOException | FileNotFoundException e) {
            System.out.println(" ERRROR " + e);
            return null;
            // handle the exception
        } catch (java.io.IOException e) {
            System.out.println("=====>  " + e);
        }
        return clientList;
    }

    public List<Transaction> readExcelFileTransactionClient(MultipartFile file, Banque banque) {
        List<Transaction> transactionList = new ArrayList<>();

        try {
            InputStream inputStream = file.getInputStream();
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            /*
             * id  agence 0
             * id  client 1
             * montant 2
             * code typeTransaction 3
             * dateCreation 4
             * code  beneficiaire 5
             *
             * */
            int pos = 0;
            for (Row row : sheet) {
                if (pos > 0) {
                    Transaction transaction = new Transaction();
                    transaction.setMontant(Float.parseFloat(String.valueOf(getCellValue(row.getCell(2), workbook))));
                    transaction.setAgence(agenceService.getAgenceById((Long) getCellValue(row.getCell(0), workbook)));
                    transaction.setStatut(0);
                    transaction.setClient(clientService.findById((Long) getCellValue(row.getCell(1), workbook)));
                    transaction.setBeneficiaire(beneficiaireRepository.findFirstByReference((String) getCellValue(row.getCell(5), workbook)));
                    transaction.setTypeDeTransaction(typeDeTransactionRepository.findFirstByCode(
                            String.valueOf(getCellValue(row.getCell(3), workbook))));
                    transaction.setDateCreation((Date) getCellValue(row.getCell(4), workbook));
                    transaction.setBanque(banque);
                    transactionList.add(transaction);
                }
                pos++;
            }
            workbook.close();
            inputStream.close();
        } catch (com.itextpdf.io.IOException | FileNotFoundException e) {
            System.out.println(" ERRROR " + e);
            return null;
            // handle the exception
        } catch (java.io.IOException e) {
            System.out.println("=====>  " + e);
        }
        return transactionList;
    }
}
