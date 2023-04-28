package com.akouma.veyuzwebapp.form;

import com.akouma.veyuzwebapp.model.Banque;
import com.akouma.veyuzwebapp.model.Client;
import com.akouma.veyuzwebapp.model.Devise;
import com.akouma.veyuzwebapp.model.TypeDeTransaction;
import lombok.Data;

import java.util.Date;

@Data
public class ReportingSearchForm {

    private Devise devise;
    private TypeDeTransaction typeDeTransaction;
    private String date1Str;
    private String date2Str;
    private Client client;
    private Banque banque;
    private Long statut;
    private int max;
    private int page;

}
