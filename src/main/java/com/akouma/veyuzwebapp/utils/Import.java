package com.akouma.veyuzwebapp.utils;

import com.akouma.veyuzwebapp.form.ImportFileForm;

import com.akouma.veyuzwebapp.model.Transaction;
import com.akouma.veyuzwebapp.repository.*;
import com.akouma.veyuzwebapp.service.FileStorageService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

@Component
public class Import {
    @Autowired
    FileStorageService fileStorageService;
    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private DeviseRepository deviseRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TypeDeTransactionRepository typeDeTransactionRepository;

    @Autowired
    private BeneficiaireRepository beneficiaireRepository;

    @Autowired
    private DomiciliationRepository domiciliationRepository;


    /**
     * CETTE FONCTION NOUS PERMET D'ANALYSER LE FICHIER EXCEL UTILISE PAR LA BANQUE POUR ENVOYER
     * LES LETTRES DE MISE EN DEMEURE. L'OBJECTIF EST DONC DE RECUPERER TOUTES LES DONNEES QUI
     * FIGURENT DANS CE FICHIER ET DE CONSTRUIRE LES APUREMENTS QUI DOIVENT ETRE GERER PAR
     * LE CLIENT CONCERNE
     * @param importFile
     * @return
     * @throws IOException
     */
    public String importApurements(ImportFileForm importFile, HttpServletRequest request) throws IOException {
        Upload u = new Upload();
        String uploadRootPath = "/tmp";
        String file_root = u.uploadFile(importFile.getFile(),fileStorageService, uploadRootPath,request);

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
                    case 0: transaction.setClient(clientRepository.findFirstByReference(cell.getStringCellValue())); break;
                    case 1: transaction.setReference(cell.getStringCellValue()); break;
                    case 2 :transaction.setMontant((float) cell.getNumericCellValue()); break;
                    case 3 :transaction.setDevise(deviseRepository.findFirstByCode(cell.getStringCellValue())); break;
                    case 4: transaction.setStatut((int) cell.getNumericCellValue()); break;
                    case 5: transaction.setTypeDeTransaction(typeDeTransactionRepository.findFirstByCode(cell.getStringCellValue())); break;
                    case 6: transaction.setBeneficiaire(beneficiaireRepository.findFirstByReference(cell.getStringCellValue())); break;
                    case 7: transaction.setDomiciliation(domiciliationRepository.findFirstByReference(cell.getStringCellValue())); break;
                    case 8: transaction.setDateCreation(cell.getDateCellValue()); break;
                }
                cmp++;
            }

            if (true){
                transactionRepository.save(transaction);
            }

            else {
                msg += "Imposible de sauvegarder cette transaction";
            }
        }

        return msg;

    }
}
