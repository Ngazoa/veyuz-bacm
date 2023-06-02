package com.akouma.veyuzwebapp.restcontroller;

import com.akouma.veyuzwebapp.form.ImportFileForm;
import com.akouma.veyuzwebapp.model.*;
import com.akouma.veyuzwebapp.service.*;
import com.akouma.veyuzwebapp.utils.MailConstant;
import com.akouma.veyuzwebapp.utils.Upload;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
public class MiseEnDemeureController {

    @Autowired
    TemplateEngine templateEngine;
      @Autowired
      FileStorageService fileStorageService;
    @Autowired
    private ClientService clientService;

    @Autowired
    private UserService userService;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private AppRoleService appRoleService;
    @Autowired
    private UserRoleService userRoleService;

    private void sendMail(String htmlMessage, String email) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        boolean multipart = true;
        MimeMessageHelper helper = new MimeMessageHelper(message, multipart, "utf-8");
        message.setContent(htmlMessage, "text/html");
        helper.setTo(email);
        helper.setSubject("Mise en demeure");

        mailSender.send(message);
    }


    @GetMapping("/test-mise-en-demeure")
    public ResponseEntity<?> test() throws IOException {

        String htmlContent = "";
        String file_root = "/test.xlsx";
        HashMap<String, Object> data = extractFileData(file_root);
        Context ctx = new Context();
        Collection<String> emails = (Collection<String>) data.get("emails");
        HashMap<String, Collection<HashMap<String, Object>>> appurementData = (HashMap<String, Collection<HashMap<String, Object>>>) data.get("data");
        System.out.println(emails);
        System.out.println("=========================================================================================");
        System.out.println("Size : " + emails.size());
        System.out.println("************************************************\nDebut\n********************************************");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String dateJour = sdf.format(new Date());
        for (String email : emails) {
            System.out.println(email + "\n --------------------------------------------------");
            AppUser user = userService.getUserByEmail(email);
            if (user != null && user.getClient() != null) {
                Collection<HashMap<String, Object>> item = appurementData.get(email);
                ctx.setVariable("data", item);
                ctx.setVariable("dateJour", dateJour);
                ctx.setVariable("client", user.getClient());
                htmlContent += templateEngine.process("lettre/misedemeure_2", ctx);
                System.out.println(data.get(email));System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
            }

        }

        return new ResponseEntity<>(htmlContent, HttpStatus.OK);
    }

    @PostMapping("/import-send-mise-en-demeure")
    public ResponseEntity<?> sendMiseEndemeures(@ModelAttribute ImportFileForm importFileForm, HttpServletRequest request) throws IOException, MessagingException {

        Upload u = new Upload();
        String results = "";
        String uploadRootPath = "/tmp";
        String file_root = u.uploadFile(importFileForm.getFile(), fileStorageService, uploadRootPath,request);
        String htmlContent = "";
        HashMap<String, Object> data = extractFileData(file_root);
        Context ctx = new Context();
        Collection<String> emails = (Collection<String>) data.get("emails");
        Collection<HashMap<String, Object>> clients = (Collection<HashMap<String, Object>>) data.get("clients");
        HashMap<String, Collection<HashMap<String, Object>>> appurementData = (HashMap<String, Collection<HashMap<String, Object>>>) data.get("data");
        System.out.println(emails);
        System.out.println("=========================================================================================");
        System.out.println("Size : " + emails.size());
        System.out.println("************************************************\nDebut\n********************************************");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String dateJour = sdf.format(new Date());
        boolean isOk = false;
        if (!emails.isEmpty()) {
            for (String email : emails) {
                System.out.println(email + "\n --------------------------------------------------");
                AppUser user = userService.getUserByEmail(email);
                if (user != null && user.getClient() != null) {
                    Collection<HashMap<String, Object>> item = appurementData.get(email);
                    ctx.setVariable("data", item);
                    ctx.setVariable("dateJour", dateJour);
                    ctx.setVariable("denominationClient", user.getClient().getDenomination());
                    ctx.setVariable("adresseClient", user.getAdresse());
                    ctx.setVariable("nomPrenom", "");
                    ctx.setVariable("banque", importFileForm.getBanque());
                    htmlContent = templateEngine.process("lettre/misedemeure_2", ctx);
                    results += htmlContent;
//                    sendMail(htmlContent, email);
                }

                System.out.println(data.get(email));System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
            }
            isOk = true;
        }

        if (importFileForm.getSaveCustomer()) {
//            saveCustomers(clients, importFileForm.getBanque());
        }

        HashMap<String, Object> response = new HashMap<>();
        response.put("isOk", isOk);
        response.put("results", results);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Transactional
    private void saveCustomers(Collection<HashMap<String, Object>> clients, Banque banque) {
        for (HashMap<String, Object> item : clients) {
            AppUser appUser = userService.getUserByEmail((String) item.get("email"));
            Client client = clientService.getClientByReference((String) item.get("referenceClient"));
            if (appUser == null && client == null) {
                System.out.println(item.get("email") + "   ||   " + item.get("referenceClient"));
                appUser = new AppUser();
                appUser.setEmail((String) item.get("email"));
                appUser.setGender((String) item.get("genre"));
                appUser.setNom((String) item.get("nom"));
                appUser.setPrenom((String) item.get("prenom"));
                appUser.setDateNaissance((Date) item.get("dateNaissance"));
                appUser.setLieuNaissance((String) item.get("lieuNaissance"));
                appUser.setTelephone1((String) item.get("telephone1"));
                appUser.setEnable(true);
                appUser.setUserName((String) item.get("telephone1"));
                appUser.setPassword(new BCryptPasswordEncoder().encode("client1234"));
                appUser = userService.saveUser(appUser);
                client = new Client();
                client.setReference((String) item.get("referenceClient"));
                client.setDenomination((String) item.get("denomination"));
                client.setNumeroContribuable((String) item.get("numeroContribuable"));
                client.setTelephone((String) item.get("telephone"));
                client.setTypeClient((String) item.get("typeClient"));
                boolean existe = true;
                while (existe) {
                    String codeVeyuz = Upload.generateVeyuzCode();
                    if (clientService.getByCode(codeVeyuz)) {
                        existe = false;
                        client.setCodeVeyuz(codeVeyuz);
                    }
                }
                client.setUser(appUser);
            }

            if (client != null) {
                if (client.getBanques() == null || client.getBanques().isEmpty()) {
                    Collection<Banque> banques = new ArrayList<>();
                    banques.add(banque);
                    client.setBanques(banques);
                }else if(!client.getBanques().contains(banque)){
                    client.getBanques().add(banque);
                }
                Client saved =  clientService.saveClient(client);
                if (appUser != null) {
                    appUser.setClient(saved);
//                    userService.saveUser(appUser);
                    AppRole appRoleClient = appRoleService.getRoleByName("ROLE_CLIENT");

                }
            }
        }
    }

    private HashMap<String, Object> extractFileData(String file_root) throws IOException {
        FileInputStream inputStream = new FileInputStream(Upload.UPLOAD_DIR + "/tmp/" + file_root);
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        XSSFSheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.iterator();
        int line = 0;
        HashMap<String, Collection<HashMap<String, Object>>> data = new HashMap<>();
        Collection<String> emails = new ArrayList<>();
        Collection<HashMap<String, Object>> clients = new ArrayList<>();
        // On parcourt chaque ligne du fichier
        while (rowIterator.hasNext()) {
            if (line > 1) {
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                HashMap<String, Object> client = new HashMap<>();
                HashMap<String, Object> lineData = new HashMap<>();
                // On recupere les donnees de chaque ligne du fichier
                int cmp = 0;
                String emailClient = null;
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    Object value;
                    switch (cell.getCellType()) {
                        case STRING:
                            value = cell.getStringCellValue();
                            break;
                        case BLANK:
                            value = null;
                            break;
                        case BOOLEAN:
                            value = cell.getBooleanCellValue();
                            break;
                        case NUMERIC:
                            value = cell.getNumericCellValue();
                            break;
                        default:
                            value = cell.getDateCellValue();
                            break;
                    }
                    switch (cmp) {
                        case 0:lineData.put("reference", value);break;
                        case 1:
                            value = cell.getDateCellValue();
                            lineData.put("date", value);break;
                        case 2:lineData.put("montant", value);break;
                        case 3:lineData.put("beneficiaire", value);break;
                        case 4:
                            value = cell.getDateCellValue();
                            lineData.put("delai", value);break;
                        case 5:lineData.put("documents", value);break;
                        case 6:lineData.put("email", value);break;
                        case 7:client.put("referenceClient", value);break;
                        case 8:client.put("denomination", value);break;
                        case 9:client.put("numeroContribuable", value);break;
                        case 10:
                            Double val = Double.valueOf(cell.getNumericCellValue());
                            client.put("telephone", val.toString());break;
                        case 11:client.put("typeClient", value);break;
                        case 12:
                            Double val1 = Double.valueOf(cell.getNumericCellValue());
                            client.put("telephone1", val1.toString());break;
                        case 13:client.put("nom", value);break;
                        case 14:client.put("prenom", value);break;
                        case 15:
                            value = cell.getDateCellValue();
                            client.put("dateNaissance", value);break;
                        case 16:client.put("lieuNaissance", value);
                        case 17:client.put("genre", value);break;
                    }
                    if (cmp == 6) {
                        emailClient = (String) value;
                    }
                    cmp ++;
                    System.out.print(value + "   |   ");
                }
                System.out.println("\n__________________________________________________________________");
                if (!lineData.isEmpty()) {
                    if (emailClient != null) {
                        if (data.containsKey(emailClient)) {
                            data.get(emailClient).add(lineData);
                        }else {
                            Collection item = new ArrayList<>();
                            item.add(lineData);
                            data.put(emailClient, item);
                            emails.add(emailClient);
                        }
                        client.put("email", emailClient);
                        clients.add(client);
                    }
                }
            }
            line++;
        }

        HashMap<String, Object> response = new HashMap<>();
        response.put("emails", emails);
        response.put("data", data);
        response.put("clients", clients);

        return response;
    }

}
