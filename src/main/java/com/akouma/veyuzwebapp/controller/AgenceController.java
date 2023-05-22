package com.akouma.veyuzwebapp.controller;


import com.akouma.veyuzwebapp.model.Agence;
import com.akouma.veyuzwebapp.model.Banque;
import com.akouma.veyuzwebapp.service.AgenceService;
import com.akouma.veyuzwebapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/agency")
public class AgenceController {

    @Autowired
    HttpSession session;
    private final AgenceService agenceService;
    private final UserService userService;

    public AgenceController(AgenceService agenceService, UserService userService) {
        this.agenceService = agenceService;
        this.userService = userService;
    }

    @GetMapping("/all")
    public String listAgences(Model model,@RequestParam(value = "agence", required = false) Agence id) {
        Banque banque = (Banque) session.getAttribute("banque");

        Iterable<Agence> agences = agenceService.getAllAgences();
        Agence agence = new Agence();
        model.addAttribute("agences", agences);
        model.addAttribute("agency", "agency");
        model.addAttribute("users", userService.getBanqueUsers(banque));

        if(id!=null){
            model.addAttribute("agence",id);
        }else{
            model.addAttribute("agence", agence);
        }
        return "agence-list";
    }

    @GetMapping("/{id}")
    public String showAgenceDetails(@PathVariable("id") Long id, Model model) {
        return "redirect:/transactions/"+id+"-agence";
    }

    @GetMapping("/new")
    public String showAgenceForm(Model model) {
        Agence agence = new Agence();
        model.addAttribute("agence", agence);
        return "agence-form";
    }

    @PostMapping
    public String createOrUpdateAgence(@ModelAttribute("agence") Agence agence) {
        agenceService.saveAgence(agence);
        return "redirect:/agency/all";
    }

    @GetMapping("/edit/{id}")
    public String showEditAgenceForm(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Agence agence = agenceService.getAgenceById(id);
        redirectAttributes.addAttribute("agence", agence);
        return "redirect:/agency/all";
    }

    @GetMapping("/delete/{id}")
    public String deleteAgence(@PathVariable("id") Long id) {
        agenceService.deleteAgence(id);
        return "redirect:/agency/all";
    }
}


