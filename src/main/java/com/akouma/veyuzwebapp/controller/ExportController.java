package com.akouma.veyuzwebapp.controller;

import com.akouma.veyuzwebapp.model.*;
import com.akouma.veyuzwebapp.service.ApurementService;
import com.akouma.veyuzwebapp.service.DomiciliationService;
import com.akouma.veyuzwebapp.service.TransactionService;
import com.akouma.veyuzwebapp.utils.StatusTransaction;
import com.akouma.veyuzwebapp.utils.Upload;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

@Controller
public class ExportController {

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private DomiciliationService domiciliationService;
    @Autowired
    private ApurementService apurementService;

    @Autowired
    private ServletContext servletContext;

    @Secured("ROLE_ADMIN")
    @GetMapping("/{id}-export/transaction")
    @ResponseBody
    public ResponseEntity<?> exportTransaction(
            @RequestParam("type") String type,
            @RequestParam(value = "dateMin", required = false) String dateMin,
            @RequestParam(value = "dateMax", required = false) String dateMax,
            @RequestParam(value = "client", required = false) Client client,
            @RequestParam(value = "statut", required = false) String statut,
            @PathVariable("id") Banque banque) throws ParseException, IOException {
        Date date1 = null;
        Date date2 = null;
        if (dateMin != null || dateMax != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if (dateMin != null && dateMin != "") {
                date1 = new Date(sdf.parse(dateMin).getTime());
            }
            if (dateMax != null && dateMax != "") {
                date2 = new Date(sdf.parse(dateMax).getTime());
            }
        }

        Iterable<Transaction> transactions = transactionService.getBanqueTransaction(banque, true, client, date1, date2, statut);
        String fileName = null;

        if (type.equals("excel")) {
            fileName = generateInExcel(transactions);

        }
        else if (type.equals("pdf")) {
            fileName = generateInPDF(transactions);
        }

        Path path = Paths.get(fileName);
        byte[] bytes = Files.readAllBytes(path);
        ByteArrayResource resource = new ByteArrayResource(bytes);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename="+path.getFileName().toString())
                .contentLength(bytes.length)
                .body(resource);
    }

    @Secured({"ROLE_ADMIN","ROLE_TREASURY_OPS","ROLE_AGENCE","ROLE_TRADE_DESK"})
    @GetMapping("/{id}-export/domiciliations")
    @ResponseBody
    public ResponseEntity<?> exportDomiciliations(@PathVariable("id") Banque banque) throws IOException {

        // On recupere la liste des domiciliations
        Iterable<Domiciliation> domiciliations = domiciliationService.getAllBanqueDomiciliations(banque);

        // DEFINITION DE L'ENTETE DU FICHIER
        Collection<String> headers = new ArrayList<>();
        headers.add("Reference client");
        headers.add("Raison sociale");
        headers.add("Type de domiciliations");
        headers.add("Date creation");
        headers.add("montant");
        headers.add("devise");
        headers.add("Date expiration");
        headers.add("Beneficiaire");

        // DEFINITION DES DONNEES A AFFICHER
        Collection<Collection<Object>> data = new ArrayList<>();
        for (Domiciliation d : domiciliations) {
            Float montant = d.getMontant();
            Collection<Object> item = new ArrayList<>();
            item.add(d.getClient().getReference());
            item.add(d.getClient().getDenomination());
            item.add(d.getTypeDeTransaction().getName());
            item.add(new SimpleDateFormat("dd/MM/yyyy").format(d.getDateCreation()));
            item.add(montant.toString());
            item.add(d.getDevise().getCode());
            item.add(new SimpleDateFormat("dd/MM/yyyy").format(d.getDateCreation()));
            item.add(d.getBeneficiaire().getName());

            data.add(item);
        }

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("domiciliations");

        // STYLE POUR L'ENTETE DU FICHIER
        HSSFCellStyle headerStyle = createStyleForTitle(workbook);

        renderHeader(headers, headerStyle, sheet);
        renderSheetData(data, sheet);

        return getFile(workbook, "domiciliations");
    }

    @Secured({"ROLE_ADMIN","ROLE_TREASURY_OPS","ROLE_AGENCE","ROLE_TRADE_DESK"})
    @GetMapping("/{id}-export/apurements")
    @ResponseBody
    public ResponseEntity<?> exportApurements(
            @PathVariable("id") Banque banque,
            @RequestParam(value = "devise", required = false) String deviseCode,
            @RequestParam(value = "typeDeTransaction", required = false) String typeCode,
            @RequestParam(value = "statut", required = false) String statut,
            @RequestParam(value = "date1Str", required = false) String date1Str,
            @RequestParam(value = "date2Str", required = false) String date2Str
    ) throws IOException, ParseException {
        int max = 1000000;
        boolean isSearch = deviseCode != null || typeCode != null || statut != null || (date1Str != null && date2Str != null);
        // On recupere la liste des apurements
        HashMap<String, Object> apurements;
        if (isSearch) {
            apurements = transactionService.getByCustomProps(banque, max, 0, deviseCode, typeCode, statut, date1Str, date2Str);
        }else {
            apurements = transactionService.getApurements(banque, true, max, 0);
        }
        System.out.println(apurements);
        // DEFINITION DE L'ENTETE DU FICHIER
        Collection<String> headers = new ArrayList<>();
        headers.add("Reference client");
        headers.add("Raison sociale");
        headers.add("Téléphone");
        headers.add("beneficiaire");
        headers.add("Type de transaction");
        headers.add("Reference transaction");
        headers.add("Montant");
        headers.add("Devise");
        headers.add("Reference Domiciliation");
        headers.add("Date de création");
        headers.add("Statut");
        headers.add("Nombre de jours restant");
        headers.add("Fichiers manquants");

        // DEFINITION DES DONNEES A AFFICHER
        Collection<Collection<Object>> data = new ArrayList<>();
        for (HashMap<String, Object> ap : (Collection<HashMap<String, Object>>) apurements.get("appurements")){
            Collection<Object> item = new ArrayList<>();
            Transaction t = (Transaction) ap.get("transaction");
            Float montant = t.getMontant();

            String fichiersManquants = "";
            for (Fichier f : t.getFichiers()) {
                if (f.getFileName() == null) {

                    fichiersManquants += f.getTypeDeFichier() != null ? f.getTypeDeFichier().getName() + "; " : f.getFileTitle() + "; ";
                }
            }
            String referenceDomiciliattion="";
            if(t.getDomiciliation()!=null){
                referenceDomiciliattion = t.getDomiciliation().getReference();
            }

            item.add(t.getClient().getReference());
            item.add(t.getClient().getDenomination());
            item.add(t.getClient().getTelephone());
            item.add(t.getBeneficiaire().getName());
            item.add(t.getTypeDeTransaction().getName());
            item.add(t.getReference());
            item.add(montant.toString());
            item.add(t.getDevise().getCode());
            String referenceDomiciliation = "";
            if (t.getDomiciliation() != null) {
                referenceDomiciliation = t.getDomiciliation().getReference();
            }
            item.add(referenceDomiciliation);
            item.add(new SimpleDateFormat("dd/MM/yyyy").format(t.getDateCreation()));
            item.add(ap.get("state"));
            item.add(ap.get("nbJrsRestant"));
            item.add(fichiersManquants);

            data.add(item);
        }

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("domiciliations");

        // STYLE POUR L'ENTETE DU FICHIER
        HSSFCellStyle headerStyle = createStyleForTitle(workbook);

        System.out.println(data);

        renderHeader(headers, headerStyle, sheet);
        renderSheetData(data, sheet);

        return getFile(workbook, "appurements");
    }

    @Secured({"ROLE_ADMIN","ROLE_TREASURY_OPS","ROLE_AGENCE","ROLE_TRADE_DESK"})
    @GetMapping("/{id}-export/apurments")
    public ResponseEntity<?> exportApurementsVersion2(
            @PathVariable("id") Banque banque,
            @RequestParam(value = "devise", required = false) String deviseCode,
            @RequestParam(value = "typeDeTransaction", required = false) String typeCode,
            @RequestParam(value = "statut", required = false) String statut,
            @RequestParam(value = "date1Str", required = false) String date1Str,
            @RequestParam(value = "date2Str", required = false) String date2Str,
            @RequestParam(value = "apured", required = false) Boolean isApured,
            @RequestParam(value = "expired", required = false) Boolean isExpired
    ) throws IOException, ParseException{
        int max = 1000000;
        boolean isSearch = false;
        Iterable<Apurement> apurements;
        if (isExpired != null) {
            apurements = apurementService.getExpiredApurements(banque, null, max, 0, false).getContent();
        }
        else if (isApured != null && isApured) {
            apurements = apurementService.getApurements(banque, null, max, 0, true).getContent();
        }
        else {
            apurements = apurementService.getNonApuredAndExpiredApurements(banque);
        }
        Collection<String> headers = new ArrayList<>();
        headers.add("Reference client");
        headers.add("Raison sociale");
        headers.add("Téléphone");
        headers.add("beneficiaire");
        headers.add("Type de transaction");
        headers.add("Reference transaction");
        headers.add("Montant");
        headers.add("Devise");
        headers.add("Reference Domiciliation");
        headers.add("Date d'ouverture");
        headers.add("Date delai");
        headers.add("Statut");
        headers.add("Fichiers manquants");
        // DEFINITION DES DONNEES A AFFICHER
        Collection<Collection<Object>> data = new ArrayList<>();
        for (Apurement ap :  apurements){
            Collection<Object> item = new ArrayList<>();
            Transaction t = transactionService.getTransactionByReference(ap.getReferenceTransaction());
            Float montant = ap.getMontant();

            String fichiersManquants = "";
            for (ApurementFichierManquant f : ap.getFichiersManquants()) {
                if (f.getFile() == null && f.isForApurement()) {
                    fichiersManquants += f.getFileName() + "; ";
                }
            }
            String referenceDomiciliation = "";
            if(t != null && t.getDomiciliation() != null){
                referenceDomiciliation = t.getDomiciliation().getReference();
            }

            String typeTransaction = "";
            if (t != null && t.getTypeDeTransaction() != null) {
                typeTransaction = t.getTypeDeTransaction().getName();
            }
            String state = ap.getIsApured() ? "Apuré" : "En cours";
            item.add(ap.getClient().getReference());
            item.add(ap.getClient().getDenomination());
            item.add(ap.getClient().getTelephone());
            item.add(ap.getBeneficiaire());
            item.add(typeTransaction);
            item.add(ap.getReferenceTransaction());
            item.add(montant.toString());
            item.add(ap.getDevise());
            item.add(referenceDomiciliation);
            String dateOuverture = ap.getDateOuverture() != null ? new SimpleDateFormat("dd/MM/yyyy").format(ap.getDateOuverture()) : "";
            item.add(dateOuverture);
            String dateExpiration = "";
            if (ap.getDateExpiration() != null) {
                dateExpiration = new SimpleDateFormat("dd/MM/yyyy").format(ap.getDateExpiration());
            }
            item.add(dateExpiration);
            item.add(state);
            item.add(fichiersManquants);

            data.add(item);
        }
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("apurements");
        // STYLE POUR L'ENTETE DU FICHIER
        HSSFCellStyle headerStyle = createStyleForTitle(workbook);
        renderHeader(headers, headerStyle, sheet);
        renderSheetData(data, sheet);

        return getFile(workbook, "appurements");
    }

    private String  generateInPDF(Iterable<Transaction> transactions) {

        return null;
    }

    private String generateInExcel(Iterable<Transaction> transactions) throws IOException {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("transactions");
        int i = 0;
        Cell cell;
        Row row;
        HSSFCellStyle style = createStyleForTitle(workbook);
        row = sheet.createRow(i);

        cell = row.createCell(0, CellType.STRING);
        cell.setCellValue("Reference client");
        cell.setCellStyle(style);
        cell = row.createCell(1, CellType.STRING);
        cell.setCellValue("Nom client");
        cell.setCellStyle(style);
        cell = row.createCell(2, CellType.STRING);
        cell.setCellValue("Téléphone");
        cell.setCellStyle(style);
        cell = row.createCell(3, CellType.STRING);
        cell.setCellValue("beneficiaire");
        cell.setCellStyle(style);
        cell = row.createCell(4, CellType.STRING);
        cell.setCellValue("Type de transaction");
        cell.setCellStyle(style);
        cell = row.createCell(5, CellType.STRING);
        cell.setCellValue("Reference transaction");
        cell.setCellStyle(style);
        cell = row.createCell(6, CellType.STRING);
        cell.setCellValue("Montant");
        cell.setCellStyle(style);
        cell = row.createCell(7, CellType.STRING);
        cell.setCellValue("Dévise");
        cell.setCellStyle(style);
        cell = row.createCell(8, CellType.STRING);
        cell.setCellValue("Reference Domiciliation");
        cell.setCellStyle(style);
        cell = row.createCell(9, CellType.STRING);
        cell.setCellValue("Date de création");
        cell.setCellStyle(style);
        cell = row.createCell(10, CellType.STRING);
        cell.setCellValue("Statut");
        cell.setCellStyle(style);


        for (Transaction t : transactions) {
            i++;
            row = sheet.createRow(i);

            cell = row.createCell(0);
            cell.setCellValue(t.getClient().getReference());
            cell = row.createCell(1);
            cell.setCellValue(t.getClient().getDenomination());
            cell = row.createCell(2);
            cell.setCellValue(t.getClient().getTelephone());
            cell = row.createCell(3);
            cell.setCellValue(t.getBeneficiaire().getName());
            cell = row.createCell(4);
            cell.setCellValue(t.getTypeDeTransaction().getName());
            cell = row.createCell(5);
            cell.setCellValue(t.getReference());
            cell = row.createCell(6);
            cell.setCellValue(t.getMontant());
            cell = row.createCell(7);
            cell.setCellValue(t.getDevise().getCode());
            cell = row.createCell(8);
            String d = null;
            if (t.getDomiciliation() != null) {
                d = t.getDomiciliation().getReference();
            }
            cell.setCellValue(d);

            cell = row.createCell(9);
            cell.setCellValue(new SimpleDateFormat("dd/MM/yyyy").format(t.getDateCreation()));

            String statut = "En cours d'apurement";
            if (t.getStatut() == StatusTransaction.VALIDATED) {
                statut = "Apurée";
            } else if (t.getDelay()!=null &&t.getDelay().getTime() < (new Date().getTime())) {
                statut = "Non apurée";
            }
            cell = row.createCell(10);
            cell.setCellValue(statut);
        }
        String file_name = new SimpleDateFormat("dd_MM_yyyy").format(new Date()) + "_imptransaction.xls";
        String filePath = Upload.UPLOAD_DIR+"/tmp/"+file_name;
        File file = new File(filePath);
        file.getParentFile().mkdirs();
        FileOutputStream outFile = new FileOutputStream(file);
        workbook.write(outFile);
        System.out.println("Created file : " + file.getAbsolutePath());
        return file.getAbsolutePath();
    }


    private ResponseEntity<?> getFile(HSSFWorkbook workbook, String defaultName) throws IOException {
        String file_name = new SimpleDateFormat("dd_MM_yyyy").format(new Date()) + "_" + defaultName + ".xls";
        String filePath = Upload.UPLOAD_DIR+"/tmp/"+file_name;
        File file = new File(filePath);
        file.getParentFile().mkdirs();
        FileOutputStream outFile = new FileOutputStream(file);
        workbook.write(outFile);
        String absolutePah = file.getAbsolutePath();

        Path path = Paths.get(absolutePah);
        byte[] bytes = Files.readAllBytes(path);
        ByteArrayResource resource = new ByteArrayResource(bytes);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename="+path.getFileName().toString())
                .contentLength(bytes.length)
                .body(resource);
    }

    private static HSSFCellStyle createStyleForTitle(HSSFWorkbook workbook) {
        HSSFFont font = workbook.createFont();
        font.setBold(true);
        HSSFCellStyle style = workbook.createCellStyle();
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.BLUE.index);
        style.setFont(font);
        return style;
    }

    /**
     * CETTE FONCTION PERMET D'ECRIRE LES DONNEES DANS LE FICHIER
     * @param data
     * @param sheet
     */
    private void renderSheetData(Collection<Collection<Object>> data, HSSFSheet sheet) {

        Row row;
        Cell cell;
        int i = 1;
        for (Collection<Object> line : data) {
            row = sheet.createRow(i);
            int j = 0;
            for (Object item : line) {
                cell = row.createCell(j);
                cell.setCellValue((String) item);
                j++;
            }
            i++;
        }
    }

    /**
     * CETTE FONCTION PERMET DE CREER L'ENTETE DU FICHIER EXCEL
     * @param headers
     * @param style
     * @param sheet
     */
    private void renderHeader(Collection<String> headers, HSSFCellStyle style, HSSFSheet sheet) {
        int i=0;
        Row row = sheet.createRow(i);
        Cell cell;
        for (String s : headers) {
            cell = row.createCell(i);
            cell.setCellValue(s);
            cell.setCellStyle(style);
            i++;
        }
    }
}
