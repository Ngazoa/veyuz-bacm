package com.akouma.veyuzwebapp.controller;

import com.akouma.veyuzwebapp.form.FichierForm;
import com.akouma.veyuzwebapp.form.FilesTransactionUploadForm;
import com.akouma.veyuzwebapp.form.SearchTransactionForm;
import com.akouma.veyuzwebapp.model.Fichier;
import com.akouma.veyuzwebapp.model.Transaction;
import com.akouma.veyuzwebapp.repository.TransactionRepository;
import com.akouma.veyuzwebapp.service.FichierService;

import com.akouma.veyuzwebapp.service.TransactionService;
import com.akouma.veyuzwebapp.utils.CheckSession;
import com.akouma.veyuzwebapp.utils.CryptoUtils;
import com.akouma.veyuzwebapp.utils.StatusTransaction;
import com.itextpdf.layout.renderer.CellRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Controller
public class FichierController {

    private static final String UPLOAD_DIR = System.getProperty("user.home") + "/test";

    @Autowired
    private FichierService fichierService;
    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    HttpSession session;

    @Autowired
    private CryptoUtils cryptoUtils;

    public FichierController(HttpSession session) {
        this.session = session;
    }

    @Secured({"ROLE_MAKER_TO","ROLE_ADMIN", "ROLE_MACKER","ROLE_SUPERADMIN", "ROLE_SUPERUSER","ROLE_TRADE_DESK","ROLE_CHECKER","ROLE_AGENCE"})
    @GetMapping("/import-files/{id}/transaction")
    public String getFormAddFilesToTransaction(@PathVariable("id") String transact, Model model) throws Exception {
        Transaction transaction =transactionRepository.findById(cryptoUtils.decrypt(String.valueOf(transact))).orElse(null);

               // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
               if (!CheckSession.checkSessionData(session)) {
                   return "redirect:/";
               }
               if (transaction.getStatut() != StatusTransaction.WAITING && transaction.getStatut() != StatusTransaction.SENDBACK_CUSTOMER) {
                   return "error/403";
               }
               FilesTransactionUploadForm filesTransactionUploadForm = new FilesTransactionUploadForm();
               Collection<FichierForm> fichierForms = new ArrayList<>();
               for (Fichier f : transaction.getFichiers()) {
                   FichierForm fichierForm = new FichierForm(f);
                   fichierForms.add(fichierForm);
               }
               filesTransactionUploadForm.setFichierForms(fichierForms);
               model.addAttribute("fichierForm", new FichierForm());
               model.addAttribute("transaction", transaction);
               model.addAttribute("filesTransactionUploadForm", filesTransactionUploadForm);
               model.addAttribute("fileslist", transaction.getFichiers());
               model.addAttribute("banque", transaction.getBanque());
               model.addAttribute("client", transaction.getClient());
               String id=cryptoUtils.encrypt(transaction.getId());
               String lettreUri = "/" +id  + "-lettre-engagement";
               model.addAttribute("lettreUri", lettreUri);

        return "transaction_files_form";
    }

    @Secured({"ROLE_CLIENT","ROLE_TREASURY_OPS","ROLE_AGENCE","ROLE_TRADE_DESK"})
    @PostMapping("/upload-transaction-files")
    public String multiUploadFileMode(@ModelAttribute FilesTransactionUploadForm filesTransactionUploadForm) {

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session)) {
            return "redirect:/";
        }
        String result = null;
        try {
            this.saveUploadedFiles(filesTransactionUploadForm.getFiles());
        } catch (Exception e) {
            e.printStackTrace();
        }

        String modelAndView = "redirect:/transaction-" + filesTransactionUploadForm.getTrans().getId() + "/details";
        System.out.println(modelAndView);
        return modelAndView;
    }

    private void saveUploadedFiles(MultipartFile[] files) throws IOException {
        File uploadDir = new File(UPLOAD_DIR);
        uploadDir.mkdirs();
        StringBuilder sb = new StringBuilder();
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }
            System.out.println(file);
            String uploadFilePath = UPLOAD_DIR + "/" + file.getOriginalFilename();
            byte[] bytes = file.getBytes();
            Path path = Paths.get(uploadFilePath);
            Files.write(path, bytes);
            System.out.println(uploadFilePath);
            sb.append(uploadFilePath).append("<br/>");
        }
    }
}
