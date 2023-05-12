package com.akouma.veyuzwebapp.validator;

import com.akouma.veyuzwebapp.form.DomiciliationForm;
import com.akouma.veyuzwebapp.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class DomiciliationValidator implements Validator {
    @Autowired
    private DomiciliationService domiciliationService;
    @Autowired
    private BeneficiaireService beneficiaireService;
    @Autowired
    private TypeDeTransactionService typeDeTransactionService;
    @Autowired
    private DeviseService deviseService;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == DomiciliationForm.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        DomiciliationForm domiciliationForm = (DomiciliationForm) target;

        // Check the fields of AppUserForm.
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "importation", "NotEmpty.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "dateCreationStr", "NotEmpty.empty");
//        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "montant", "NotEmpty.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "reference", "NotEmpty.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "client", "NotEmpty.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "beneficiaire", "NotEmpty.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "typeDeTransaction", "NotEmpty.empty");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "devise", "NotEmpty.empty");

        if (!errors.hasFieldErrors("reference")) {
            if (!domiciliationService.getByReference(domiciliationForm.getReference())) {
                errors.rejectValue("reference", "Duplicate.domiciliationForm.reference");
            }
        }


        if (!errors.hasFieldErrors("client")) {
            if (!domiciliationForm.getBanque().getBeneficiaires().contains(domiciliationForm.getBeneficiaire())) {
                errors.rejectValue("beneficiaire", "NotYourBeneficiaire.domiciliationForm.client");
            }
        }

        if (!errors.hasFieldErrors("montant")) {
            if (domiciliationForm.getMontant() <= 0) {
                errors.rejectValue("montant", "Zero.domiciliationForm.montant");
            }
        }

    }
}
