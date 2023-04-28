package com.akouma.veyuzwebapp.controller;

import com.akouma.veyuzwebapp.form.TypeTransactionForm;
import com.akouma.veyuzwebapp.model.Banque;
import com.akouma.veyuzwebapp.model.TypeDeTransaction;
import com.akouma.veyuzwebapp.service.TypeDeFichierService;
import com.akouma.veyuzwebapp.service.TypeDeTransactionService;
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
public class TypeTransactionController {

    @Autowired
    private TypeDeTransactionService typeDeTransactionService;
    @Autowired
    private TypeDeFichierService typeDeFichierService;

    @Autowired
    HttpSession session;

    public TypeTransactionController(HttpSession session) {
        this.session = session;
    }

    @GetMapping("/type-transactions")
    public String index(Model model, @PathVariable(value = "id", required = false) TypeDeTransaction typeDeTransaction) {

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session)) {
            return "redirect:/";
        }

        if (typeDeTransaction == null) {
            typeDeTransaction = new TypeDeTransaction();
        }

        Banque banque = (Banque) session.getAttribute("banque");

        model.addAttribute("typeTransactionForm", new TypeTransactionForm(typeDeTransaction));
        model.addAttribute("banque", banque);
        model.addAttribute("isTypeTransaction", true);
        model.addAttribute("setItem", "tdt");
        model.addAttribute("types", typeDeTransactionService.getAllTypesTransaction());
        model.addAttribute("typesFichiersFournir", typeDeFichierService.getAll());
        model.addAttribute("dash","client");

        return "parametres";
    }

    @PostMapping("/post-type-transaction")
    public String post(@ModelAttribute TypeTransactionForm typeTransactionForm) {

        System.out.println(typeTransactionForm.get().getTypeDeFichiers());
        typeDeTransactionService.save(typeTransactionForm.get());

        return "redirect:/type-transactions";
    }
}
