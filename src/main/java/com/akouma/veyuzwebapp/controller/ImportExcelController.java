package com.akouma.veyuzwebapp.controller;

import com.akouma.veyuzwebapp.model.Banque;
import com.akouma.veyuzwebapp.service.ImplService.ImportServiceImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.security.Principal;

@Controller
@RequestMapping("/import")
public class ImportExcelController {
    private final ImportServiceImpl importService;
    private final HttpSession httpSession;
    public ImportExcelController(ImportServiceImpl importService, HttpSession httpSession) {
        this.importService = importService;
        this.httpSession = httpSession;
    }

    /*
     * order importation beneficiary->agency->client->dome->transaction
     */
    @PostMapping
    public String importBeneficiaire(Model model, @RequestParam("file") MultipartFile file
            , @RequestParam("type") String type, Principal principal) {

        Banque banque= (Banque) httpSession.getAttribute("banque");

        boolean response=importService.importProcessing(banque,file,type);
        if(response){
            model.addAttribute("flashMessage","Importation "+type+" reussie avec succes");
        }else {
            model.addAttribute("errorMessage","Importation "+type+" Echouée ... Veuillez vous rassurer de respecter le format");
        }
        return "parametres";
    }

}
