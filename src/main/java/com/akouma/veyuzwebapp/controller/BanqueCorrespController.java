package com.akouma.veyuzwebapp.controller;


import com.akouma.veyuzwebapp.model.BanqueCorrespondante;
import com.akouma.veyuzwebapp.repository.BanqueCorrespondanteRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/banquecorrespondant")
public class BanqueCorrespController {

    private final BanqueCorrespondanteRepository banqueCorrespondanteRepository;

    public BanqueCorrespController(BanqueCorrespondanteRepository banqueCorrespondante) {
        this.banqueCorrespondanteRepository = banqueCorrespondante;
    }

    @GetMapping("/all")
    public String listBanqueCorrespondantes(Model model, @RequestParam(value = "banquecorrespondant", required = false) BanqueCorrespondante id) {

        Iterable<BanqueCorrespondante> banqueCorrespondantes = banqueCorrespondanteRepository.findAllByEnabled(true);
        BanqueCorrespondante banqueCorrespondante = new BanqueCorrespondante();
        model.addAttribute("banquecorrespondants", banqueCorrespondantes);
        model.addAttribute("banque", "banque");

        if (id != null) {
            model.addAttribute("banquecorrespondant", id);
        } else {
            model.addAttribute("banquecorrespondant", banqueCorrespondante);
        }
        return "banque-correspondante";
    }

    @PostMapping
    public String createOrUpdateAgence(@ModelAttribute("banquecorrespondant") BanqueCorrespondante banqueCorrespondante) {
        BanqueCorrespondante bqc=new BanqueCorrespondante();

        if(banqueCorrespondante.getId()==null){
           bqc.setName(banqueCorrespondante.getName());
            bqc.setEnabled(true);
            banqueCorrespondanteRepository.save(bqc);
        }else{
            banqueCorrespondante.setEnabled(true);
            banqueCorrespondanteRepository.save(banqueCorrespondante);
        }
        return "redirect:/banquecorrespondant/all";
    }

    @GetMapping("/edit/{id}")
    public String showEditAgenceForm(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        BanqueCorrespondante banqueCorrespondante = banqueCorrespondanteRepository.getById(id);
        redirectAttributes.addAttribute("banquecorrespondant", banqueCorrespondante);
        return "redirect:/banquecorrespondant/all";
    }

    @GetMapping("/delete/{id}")
    public String deleteAgence(@PathVariable("id") Long id) {
        BanqueCorrespondante banqueCorrespondante = banqueCorrespondanteRepository.getById(id);
        banqueCorrespondante.setEnabled(false);
        banqueCorrespondanteRepository.save(banqueCorrespondante);
        return "redirect:/banquecorrespondant/all";
    }
}
