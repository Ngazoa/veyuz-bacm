package com.akouma.veyuzwebapp.controller;

import com.akouma.veyuzwebapp.model.Banque;
import com.akouma.veyuzwebapp.model.Lettre;
import com.akouma.veyuzwebapp.model.Transaction;
import com.akouma.veyuzwebapp.service.BanqueService;
import com.akouma.veyuzwebapp.service.LettreService;
import com.akouma.veyuzwebapp.utils.CheckSession;
import com.akouma.veyuzwebapp.utils.pdf.LettreEngagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
public class LettreController {
    @Autowired
    HttpSession session;
    @Autowired
    private BanqueService banqueService;
//    @Autowired
//    private PdfGenerateServiceImpl pdfGenerateService;
    @Autowired
    private LettreService lettreService;
    @Autowired
    private LettreEngagement lettreEngagement;

    public LettreController(HttpSession session) {
        this.session = session;
    }

    @GetMapping("/mise-en-demeure")
    public String getMisenDemeure(Model model, HttpServletRequest request) {

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session)) {
            return "redirect:/";
        }

        Banque banque = (Banque) session.getAttribute("banque");

        model.addAttribute("demeuredatas", lettreService.getBankLetter(banque, "mise-en-demeure"));
        model.addAttribute("banque", banque);
        model.addAttribute("letterForm", "mise-demeure");
        return "parametres";
    }


    @GetMapping("/lettre-engagement")
    public String getLettre(Model model, HttpServletRequest request) {

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session)) {
            return "redirect:/";
        }

        Banque banque = (Banque) session.getAttribute("banque");
        model.addAttribute("dash", "params");

        model.addAttribute("lettreedatas", lettreService.getBankLetter(banque, "engagement"));

        model.addAttribute("banque", banque);
        model.addAttribute("letterForm", "engagement");
        return "parametres";
    }

    @PostMapping("/save-lettre")
    public String saveLettre(@RequestParam("contenu") String lettreContaint, @RequestParam("type") String type, Model model, HttpServletRequest request) {


        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session)) {
            return "redirect:/";
        }

        Banque banque = (Banque) request.getSession().getAttribute("banque");
        Lettre lettre = new Lettre();
        lettre.setBanque(banque);
        lettre.setContent(lettreContaint);
        lettre.setType(type);

        lettreService.lettreSave(lettre, banque, type);
        String dtas = "";
        if (type == "engagement") {
            dtas = "lettreedatas";
        } else {
            dtas = "demeuredatas";
        }

        model.addAttribute(dtas, lettreService.getBankLetter(banque, type));
        session.setAttribute("dash", "params");

        model.addAttribute("banque", banque);
        model.addAttribute("letterForm", type);
        return "parametres";
    }

    @GetMapping("/generate-pdf-lettre-engagement")

    public String genratedPdfLetter(HttpServletRequest request, Model model) {

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session)) {
            return "redirect:/";
        }

        Banque banque = (Banque) session.getAttribute("banque");

        Map<String, Object> data = new HashMap<>();
        Lettre ltrr = lettreService.getBankLetter(banque, "egagement");

        data.put("lettre", ltrr);

        model.addAttribute("dash", "params");

        // pdfGenerateService.generatePdfFile("lettre",data , "letrre.pdf");

        return "lettre/mise-demeure";

    }

    public String Replaceletter(String word) {
        Transaction transaction;
        word = word.replaceAll("\\]", "");
        word = word.replaceAll("\\[", "");
        word = word.replaceAll("NomClient", "Benito Benito");
        word = word.replaceAll("Montant", "" + "1000 000000");
        word = word.replaceAll("DateLimite", "" + "14-58-2025");
        word = word.replaceAll("NomBanque", "" + "SGB");
        word = word.replaceAll("NomFournisseur", "" + "GUINNESS CAMEROUN");
        return word;
    }


    /**
     * ON APPELLE CETTE FONCTION TOUT LORSQUE L'UTILISATEUR VEUT TELECHARGER LA LETTRE D'ENGAGEMENT LIEE A SA TRANSACTION.
     *
     * @param transaction
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    @GetMapping("/{id}-lettre-engagement")
    public ResponseEntity<?> getLetter(
            @PathVariable("id") Transaction transaction,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        byte[] bytes = null;

        if (transaction.getTypeDeTransaction().getType() != null && transaction.getTypeDeTransaction().getType().equals("biens")) {
            bytes = lettreEngagement.getImportationBiens(transaction, request, response);
        } else if (transaction.getTypeDeTransaction().getType() != null && transaction.getTypeDeTransaction().getType().equals("services")) {
            bytes = lettreEngagement.getImportationServices(transaction, request, response);
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }
}
