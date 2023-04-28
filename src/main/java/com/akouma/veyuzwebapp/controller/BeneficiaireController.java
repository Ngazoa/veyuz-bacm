package com.akouma.veyuzwebapp.controller;

import com.akouma.veyuzwebapp.form.BeneficiaireForm;
import com.akouma.veyuzwebapp.model.Banque;
import com.akouma.veyuzwebapp.model.Beneficiaire;
import com.akouma.veyuzwebapp.model.Client;
import com.akouma.veyuzwebapp.service.BeneficiaireService;
import com.akouma.veyuzwebapp.utils.CheckSession;
import com.akouma.veyuzwebapp.validator.BeneficiaireValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
public class BeneficiaireController {

    @Autowired
    private BeneficiaireService beneficiaireService;

    @Autowired
    private BeneficiaireValidator beneficiaireValidator;

    @Autowired
    HttpSession session;

    public BeneficiaireController(HttpSession session) {
        this.session = session;
    }

    @InitBinder
    protected void initBinder(WebDataBinder dataBinder) {
        Object target = dataBinder.getTarget();
        if (target == null) {
            return;
        }
        System.out.println("Target=" + target);

        if (target.getClass() == BeneficiaireForm.class) {
            dataBinder.setValidator(beneficiaireValidator);
        }
    }


    @GetMapping({"/beneficiaires"})
    public String getClientBeneficiaires(
            Model model, RedirectAttributes redirectAttributes) {

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session)) {
            return "redirect:/";
        }

        Banque banque = (Banque) session.getAttribute("banque");
        BeneficiaireForm beneficiaireForm = new BeneficiaireForm();
        beneficiaireForm.setBanque(banque);

        model.addAttribute("banque", banque);
        model.addAttribute("beneficiaires", beneficiaireService.getBeneficiaire(banque));
        model.addAttribute("beneficiaireForm", beneficiaireForm);

        String msg = "La banque a ete enregistree !";
        redirectAttributes.addFlashAttribute("flashMessage", msg);

        return "beneficiaires";
    }

    @PostMapping("/post-beneficiaire")
    public String addBeneficiaire(
            @ModelAttribute @Validated BeneficiaireForm beneficiaireForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes, HttpServletRequest request) {

        Banque banque = (Banque) session.getAttribute("banque");

        // ON VERIFIE QUE LA BANQUE EST DANS LA SESSION AVANT DE CONTINUER
        if (!CheckSession.checkSessionData(session)) {
            return "redirect:/";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("banque", beneficiaireForm.getBanque());
            model.addAttribute("beneficiaires", beneficiaireForm.getBanque().getBeneficiaires());
            return "beneficiaires";
        }
        try {
            beneficiaireService.saveBeneficiaire(beneficiaireForm, request,banque);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error: " + e.getMessage());
            model.addAttribute("banque", beneficiaireForm.getBanque());
            model.addAttribute("beneficiaires", beneficiaireForm.getBanque().getBeneficiaires());
            return "beneficiaires";
        }

        String msg = "Le beneficiaire a ete ajouté dans la liste !!!";
        redirectAttributes.addFlashAttribute("flashMessage", msg);

        return "redirect:/beneficiaires";
    }
}
