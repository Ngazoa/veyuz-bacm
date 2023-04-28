package com.akouma.veyuzwebapp.form;

import com.akouma.veyuzwebapp.model.Banque;
import com.akouma.veyuzwebapp.model.Client;
import lombok.Data;

import java.sql.Date;

@Data
public class SearchTransactionForm {

    private String date1;
    private String date2;
    private Banque banque;
    private Client client;

}
