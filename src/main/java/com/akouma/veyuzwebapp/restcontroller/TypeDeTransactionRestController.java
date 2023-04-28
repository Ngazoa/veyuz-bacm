package com.akouma.veyuzwebapp.restcontroller;


import com.akouma.veyuzwebapp.model.TypeDeTransaction;
import com.akouma.veyuzwebapp.service.TypeDeTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;

@RestController
public class TypeDeTransactionRestController {

    @Autowired
    private TypeDeTransactionService typeDeTransactionService;

    @GetMapping("/rest-get-type-transactions/{type}")
    public ResponseEntity<?> getTypeDeTransactionsByType(@PathVariable("type") String type) {

        boolean isImport = type.equals("importation");

        List<TypeDeTransaction> typeDeTransactions = (List<TypeDeTransaction>) typeDeTransactionService.getTypesTransactionByIsImport(isImport);
        boolean hasData = false;
        String data = null;
        if (typeDeTransactions != null && !typeDeTransactions.isEmpty()) {
            hasData = true;
            data = "<option>Selectionner un element dans la liste</option>";
            for (TypeDeTransaction t : typeDeTransactions) {
                data += "<option value='" + t.getId() + "'>" + t.getName() + "</option>";
            }
        }
        HashMap<String, Object> response = new HashMap<>();
        response.put("hasData", hasData);
        response.put("data", data);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
