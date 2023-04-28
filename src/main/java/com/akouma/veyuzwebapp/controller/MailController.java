package com.akouma.veyuzwebapp.controller;

import com.akouma.veyuzwebapp.form.ImportFileForm;
import com.akouma.veyuzwebapp.model.Banque;
import com.akouma.veyuzwebapp.service.MailService;
import com.akouma.veyuzwebapp.service.TransactionService;
import com.akouma.veyuzwebapp.utils.CheckSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

@Controller
public class MailController {
    String letter = "";
    @Autowired
    HttpSession session;
    @Autowired
    private MailService mailService;
    @Autowired
    private TransactionService transactionService;

    public MailController(HttpSession session) {
        this.session = session;
    }


//    @GetMapping("/mail-form")
//    public String getMailForm(Model mdl, HttpServletRequest request) {
//
//        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
//        if (!CheckSession.checkSessionData(session)) {
//            return "redirect:/";
//        }
//
//        Banque banque = (Banque) session.getAttribute("banque");
//
//        Lettre mailtplate = mailService.getLettreMiseEnDemeurBanque(banque);
//
//        letter =  this.Replaceletter(mailtplate.getContent());
//        mdl.addAttribute("LTitle", ("lettre  "+mailtplate.getType()).toUpperCase());
//        mdl.addAttribute("mailTemplate", html2text(Replaceletter(letter)));
//        return "email-template";
//    }

    //fonction pou remplaccer les parametres de la lettre
//    public String Replaceletter(String word ){
//        Transaction transaction;
//        word= word.replaceAll("\\]", "");
//        word=  word.replaceAll("\\[", "");
//        word=word.replaceAll("NomClient","Benito Benito");
//        word= word.replaceAll("Montant",""+"1000 000000");
//        word= word.replaceAll("DateLimite",""+"14-58-2025");
//        word= word.replaceAll("NomBanque",""+"SGB");
//        word= word.replaceAll("NomFournisseur",""+"GUINNESS CAMEROUN");
//        return word;
//    }
//
//    public static String html2text(String html) {
//        return Jsoup.parse(html).text();
//    }
//

    @Secured({"ROLE_SUPERADMIN"})
    @GetMapping("/send-mise-en-demeure")
    public String showUploadMiseEnDemeureFileForm(Model model) {

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session)) {
            return "redirect:/";
        }

        Banque banque = (Banque) session.getAttribute("banque");

        model.addAttribute("uploadFileMiseEnDemeure", true);
        model.addAttribute("importFileForm", new ImportFileForm(banque));
        model.addAttribute("banque", banque);
        model.addAttribute("importUri", "/import-send-mise-en-demeure");
        model.addAttribute("dash", "params");
        model.addAttribute("setItem", "md");

        return "parametres";
    }

}
