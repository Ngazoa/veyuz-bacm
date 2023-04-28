package com.akouma.veyuzwebapp.restcontroller;

import com.akouma.veyuzwebapp.form.FichierForm;
import com.akouma.veyuzwebapp.form.FilesTransactionUploadForm;
import com.akouma.veyuzwebapp.model.Fichier;
import com.akouma.veyuzwebapp.model.Transaction;
import com.akouma.veyuzwebapp.service.FichierService;
import com.akouma.veyuzwebapp.service.FileStorageService;
import com.akouma.veyuzwebapp.service.TransactionService;
import com.akouma.veyuzwebapp.utils.StatusTransaction;
import com.akouma.veyuzwebapp.utils.Upload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.function.EntityResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.HashMap;

@RestController
public class FichierRestController {

    @Autowired
    private FichierService fichierService;
    @Autowired
    private TransactionService transactionService;

    @Autowired
    TemplateEngine templateEngine;
    @Autowired
    private FileStorageService fileStorageService;
    @PostMapping("/rest-upload-transaction-files")
    public ResponseEntity<?> multiUploadFileMode(@ModelAttribute FichierForm fichierForm, HttpServletRequest request) {
        try {

            Upload upload = new Upload();
            if (fichierForm.getFichier() != null) {
                if (fichierForm.getFichier().getTransaction().getStatut() != StatusTransaction.VALIDATED && fichierForm.getFichier().getTransaction().getStatut() != StatusTransaction.REJECTED) {
                    Fichier fichier = fichierForm.getFichier();
                    String fileName = upload.uploadFile(fichierForm.getFile(),fileStorageService, "fichiers_transactions",request);
                    if (fileName != null && fichierForm.getFichier() != null) {
                        fichierForm.getFichier().setFileName(fileName);
                        fichierService.saveFichier(fichierForm.getFichier());
                        fichier.getTransaction().setHasFiles(true);
                        fichier.getTransaction().setStatut(StatusTransaction.WAITING);
                        transactionService.saveTransaction(fichier.getTransaction());
                    } else {
                        if (fichierForm.getFichier().getFileName() == null) {
                            return new ResponseEntity<>("<span class='text-danger>" + fichierForm.getFichier().getTypeDeFichier().getName() + " est encore vide</span><br>", HttpStatus.OK);
                        } else {
                            return new ResponseEntity<>("", HttpStatus.OK);
                        }
                    }
                } else {
                    return new ResponseEntity<>("Impossible d'ajouter le fichier ou de le modifier car la transaction est deja approuvée et validée</br>", HttpStatus.OK);
                }
            }
            else {
                if (fichierForm.getTransaction() != null && fichierForm.getTransaction().getStatut() != StatusTransaction.VALIDATED) {
                    Fichier fichier = new Fichier();
                    System.out.println(fichierForm.getFile());
                    String fileName = upload.uploadFile(fichierForm.getFile(), fileStorageService, "fichiers_transactions",request);
                    if (fileName != null) {
                        fichier.setFileName(fileName);
                        fichier.setFileTitle(fichierForm.getFileTitle());
                        fichier.setTransaction(fichierForm.getTransaction());
                        fichier.setTypeDeFichier(null);
                        fichier.setValidated(false);
                        fichierService.saveFichier(fichier);
                        fichier.getTransaction().setHasFiles(true);
                        fichier.getTransaction().setStatut(StatusTransaction.WAITING);
                        transactionService.saveTransaction(fichier.getTransaction());
                    } else {
                        if (fichierForm.getFileTitle() == null) {
                            return new ResponseEntity<>(fichierForm.getFileTitle() + " Error 1 : fichier vide<br>", HttpStatus.OK);
                        } else {
                            return new ResponseEntity<>("", HttpStatus.OK);
                        }
                    }
                } else {
                    if (fichierForm.getTransaction().getStatut() == StatusTransaction.VALIDATED) {
                        return new ResponseEntity<>("<span class='text-danger'>Impossible d'ajouter le fichier ou de le modifier car la transaction est deja approuvée et validée</span>", HttpStatus.OK);
                    } else {
                        return new ResponseEntity<>("<span class='text-danger'>Impossible d'ajouter le fichier ou de le modifier car la transaction a été rejetée !</span>", HttpStatus.OK);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (fichierForm.getFichier() != null && fichierForm.getFichier().getTypeDeFichier() != null) {
                return new ResponseEntity<>(fichierForm.getFichier().getTypeDeFichier().getName() + " Error 8 : " + e.getMessage(), HttpStatus.BAD_REQUEST);
            } else {
                return new ResponseEntity<>(fichierForm.getFileTitle() + " Error 9 : " + e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        }
        if (fichierForm.getFichier() != null && fichierForm.getFichier().getTypeDeFichier() != null) {
            return new ResponseEntity<>(fichierForm.getFichier().getTypeDeFichier().getName() + " Ajouté <br>", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("<span class='text-success'>" + fichierForm.getFileTitle() + " Ajouté </span><br>", HttpStatus.OK);
        }
    }

    @GetMapping("/build-other-file-form-{id}-transaction")
    public HashMap<String, Object> createOtherFileForm(@PathVariable("id") Transaction transaction) {
        String newForm = "<div class='alert alert-warning'><button class='btn-close text-danger remove-file' type='button' aria-label='close'></button>" +
                "<form style='width: 100%;' method=\"POST\" action=\"/rest-upload-transaction-files\" enctype=\"multipart/form-data\">" +
                "<input type=\"hidden\" value='" + transaction.getId() + "' name=\"transaction\">" +
                "<div class=\"mb-3\">" +
                "<label class=\"form-label\">Intitule du document</label>" +
                "<input type=\"text\" class=\"form-control mb-3\" required name=\"fileTitle\">" +
                "<div class=\"input-group\">" +
                "<input class=\"form-control\" type=\"file\" name=\"file\" required>" +
                "</div>" +
                "<input type=\"hidden\" value=\"\" name=\"fichier\">" +
                "<i class=\"loader float-right\"></i></div></form></div>";

        HashMap<String, Object> map = new HashMap<>();
        map.put("form", newForm);

        return map;
    }

    @GetMapping("/{id}-transaction/fichiers-manquants")
    public ResponseEntity<?> getFichiersManquants(@PathVariable("id") Transaction transaction) {
        HashMap<String, Object> response = new HashMap<>();
        String filesList = "";
        boolean existe = false;
        for (Fichier f : transaction.getFichiers()) {
            if (f.getFileName() == null) {
                filesList += "<li>" + f.getTypeDeFichier().getName() + "</li>";
                existe = true;
            }
        }
        if (!filesList.equals("")) {
            filesList = "<ul>" + filesList + "</ul>";
        }

        if (!existe) {
            filesList = "Aucun fichier manquant !!!<br>Tous les documents ont été fournis";
        }
        response.put("response", filesList);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
