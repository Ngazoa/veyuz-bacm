package com.akouma.veyuzwebapp.controller;

import com.akouma.veyuzwebapp.form.TypeFichierForm;
import com.akouma.veyuzwebapp.model.Banque;
import com.akouma.veyuzwebapp.model.TypeDeFichier;
import com.akouma.veyuzwebapp.service.TypeDeFichierService;
import com.akouma.veyuzwebapp.utils.CheckSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpSession;

@Controller
public class TypeFichierController {

    @Autowired
    HttpSession session;
    @Autowired
    private TypeDeFichierService typeDeFichierService;

    public TypeFichierController(HttpSession session) {
        this.session = session;
    }

    @GetMapping({"/type-fichiers", "/{id}-type-fichiers"})
    public String index(Model model, @PathVariable(value = "id", required = false) TypeDeFichier typeDeFichier) {

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session)) {
            return "redirect:/";
        }

        if (typeDeFichier == null) {
            typeDeFichier = new TypeDeFichier();
        }

        Banque banque = (Banque) session.getAttribute("banque");

        model.addAttribute("typeFichierForm", new TypeFichierForm(typeDeFichier));
        model.addAttribute("banque", banque);
        model.addAttribute("isTypeFichier", true);
        model.addAttribute("types", typeDeFichierService.getAll());
        model.addAttribute("dash", "client");
        model.addAttribute("setItem", "tdf");

        return "parametres";
    }

    @PostMapping("/post-type-fichier")
    public String post(@ModelAttribute TypeFichierForm typeFichierForm) {

        typeDeFichierService.save(typeFichierForm.get());

        return "redirect:/type-fichiers";
    }
}
