package com.akouma.veyuzwebapp.validator;

import com.akouma.veyuzwebapp.form.BanqueForm;
import com.akouma.veyuzwebapp.form.BeneficiaireForm;
import com.akouma.veyuzwebapp.form.ClientForm;
import com.akouma.veyuzwebapp.model.AppUser;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class BeneficiaireValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == BeneficiaireForm.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        BeneficiaireForm benefForm = (BeneficiaireForm) target;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "NotEmpty.userForm.nom");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "kycFile", "NotEmpty.userForm.nom");

    }
}
