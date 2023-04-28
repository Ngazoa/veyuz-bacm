package com.akouma.veyuzwebapp.validator;

import com.akouma.veyuzwebapp.form.UserForm;
import com.akouma.veyuzwebapp.model.AppUser;
import com.akouma.veyuzwebapp.service.UserService;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class AppUserValidator implements Validator {

    private final EmailValidator emailValidator = EmailValidator.getInstance();

    @Autowired
    private UserService userService;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == UserForm.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        UserForm appUserForm = (UserForm) target;

        // Check the fields of AppUserForm.
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "nom", "NotEmpty.userForm.nom");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "dateNaissanceStr", "NotEmpty.userForm.dateNaissanceStr");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "adresse", "NotEmpty.userForm.adresse");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "NotEmpty.userForm.email");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "NotEmpty.userForm.password");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "confirmPassword", "NotEmpty.userForm.confirmPassword");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "gender", "NotEmpty.userForm.gender");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "telephone1", "NotEmpty.userForm.telephone1");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "lieuNaissance", "NotEmpty.userForm.lieuNaissance");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userName", "NotEmpty.userForm.userName");

        if (!this.emailValidator.isValid(appUserForm.getEmail())) {
            // Invalid email.
            errors.rejectValue("email", "Pattern.userForm.email");
        } else if (appUserForm.getUserId() == null) {
            AppUser dbUser = userService.getUserByEmail(appUserForm.getEmail());
            if (dbUser != null) {
                // Email has been used by another account.
                errors.rejectValue("email", "Duplicate.userForm.email");
            }
        }

        if (!errors.hasFieldErrors("userName")) {
            AppUser dbUser = userService.getUserByUserName(appUserForm.getUserName());
            if (dbUser != null) {
                // Username is not available.
                errors.rejectValue("userName", "Duplicate.userForm.userName");
            }
        }

        if (!errors.hasErrors()) {
            if (!appUserForm.getConfirmPassword().equals(appUserForm.getPassword())) {
                errors.rejectValue("confirmPassword", "Match.userForm.confirmPassword");
            }
        }
    }
}
