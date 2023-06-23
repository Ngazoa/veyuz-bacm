package com.akouma.veyuzwebapp.form;

import com.akouma.veyuzwebapp.model.Domiciliation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DomiciliationTransactionForm {

    private Domiciliation domiciliation;
    private Float montant;
}
