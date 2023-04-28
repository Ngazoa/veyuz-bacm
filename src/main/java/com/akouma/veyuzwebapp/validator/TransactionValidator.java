package com.akouma.veyuzwebapp.validator;

import com.akouma.veyuzwebapp.form.TransactionForm;
import com.akouma.veyuzwebapp.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
public class TransactionValidator implements Validator {
    @Autowired
    private TransactionService transactionService;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == TransactionForm.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        TransactionForm transactionForm = (TransactionForm) target;

        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "montantTexte", "NotEmpty.empty");

        if (!errors.hasFieldErrors("montantTexte")) {
            if (transactionForm.getDomiciliation() != null) {
                Float montantMax = transactionForm.getDomiciliation().getMontantRestant() * (1 + 10 / 100);
                Float montantSaisie = transactionForm.getMontant();
                if (montantSaisie > montantMax) {
                    errors.rejectValue("montantTexte", "NotEmpty.empty");
                    transactionForm.setMontantTexte(String.valueOf(montantSaisie));
                }
            } else {
                transactionForm.setMontantTexte(String.valueOf(transactionForm.getMontant()));

            }
        }
    }
}
