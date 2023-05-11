package com.akouma.veyuzwebapp.validator;

import com.akouma.veyuzwebapp.form.ClientForm;
import com.akouma.veyuzwebapp.form.UserForm;
import com.akouma.veyuzwebapp.model.AppUser;
import com.akouma.veyuzwebapp.service.ClientService;
import com.akouma.veyuzwebapp.service.UserService;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class ClientValidator implements Validator {
    private final EmailValidator emailValidator = EmailValidator.getInstance();

    @Autowired
    private UserService userService;

    @Autowired
    private ClientService clientService;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == ClientForm.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        ClientForm clientForm = (ClientForm) target;
        AppUser appUser = clientForm.getAppUser();
        // Check the fields of AppUserForm.
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "appUser.email", "NotEmpty.userForm.email");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "denomination", "NotEmpty.userForm.userName");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "telephone", "NotEmpty.userForm.userName");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "kycFile", "NotEmpty.userForm.userName");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "typeClient", "NotEmpty.userForm.userName");


        if (!this.emailValidator.isValid(appUser.getEmail())) {
            // Invalid email.
            errors.rejectValue("appUser.email", "Pattern.userForm.email");
        } else if (appUser.getId() == null) {
            AppUser dbUser = userService.getUserByEmail(appUser.getEmail());
            if (dbUser != null) {
                // Email has been used by another account.
                errors.rejectValue("appUser.email", "Duplicate.userForm.email");
            }
        }


        if (!errors.hasFieldErrors("typeClient")) {
            if (!clientForm.getTypeClient().equals("particulier") && !clientForm.getTypeClient().equals("entreprise")) {
                errors.rejectValue("typeClient", "TypeError.clientForm.typeClient");
            }
        }

    }
}
