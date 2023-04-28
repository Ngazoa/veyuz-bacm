package com.akouma.veyuzwebapp.restcontroller;

import com.akouma.veyuzwebapp.model.Banque;
import com.akouma.veyuzwebapp.model.Client;
import com.akouma.veyuzwebapp.model.Devise;
import com.akouma.veyuzwebapp.model.Transaction;
import com.akouma.veyuzwebapp.service.DeviseService;
import com.akouma.veyuzwebapp.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

/**
 * Cette classe contient les methodes utiles pour generer les diagrammes
 */
@RestController
public class ChartRestController {

    @Autowired
    private DeviseService deviseService;
    @Autowired
    private TransactionService transactionService;

    @GetMapping({"/build-circular-chart/{banque_id}", "/build-circular-chart/{banque_id}/{client_id}"})
    public ResponseEntity<?> circularChart(@PathVariable("banque_id")Banque banque, @PathVariable(value = "client_id", required = false)Client client) {
        HashMap<String, Object> response = new HashMap<>();
        Collection<HashMap<String, Object>> devises = deviseService.getAll(banque, client);
        String[] labels = new String[devises.size()];
        String[] bgColor = new String[devises.size()];
        Long[] data = new Long[devises.size()];
        int i = 0;
        for (HashMap<String, Object> d : devises) {
            labels[i] = ((Devise) d.get("devise")).getCode();
            data[i] = (Long) d.get("nbTransactions");
            bgColor[i] = (String) d.get("bgColor");
            i++;
        }

        response.put("labels", labels);
        response.put("bgColors", bgColor);
        response.put("data", data);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping({"/build-bar-chart/{banque_id}", "/build-bar-chart/{banque_id}/{client_id}"})
    public ResponseEntity<?> barChart(@PathVariable("banque_id")Banque banque, @PathVariable(value = "client_id", required = false)Client client, @RequestParam(value = "devise", required = false) Devise devise) throws ParseException {
        HashMap<String, Object> response = getChartData(banque, client, devise);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping({"/build-line-chart/{banque_id}", "/build-line-chart/{banque_id}/{client_id}"})
    public ResponseEntity<?> lineChart(@PathVariable("banque_id")Banque banque, @PathVariable(value = "client_id", required = false)Client client, @RequestParam(value = "devise", required = false) Devise devise) throws ParseException {
        HashMap<String, Object> response = getChartData(banque, client, devise);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private HashMap<String, Object> getChartData(Banque banque, Client client, Devise devise) throws ParseException {
        HashMap<String, Object> data = new HashMap<>();
        // On recupere la date du jour
        int nbJours = 10;
        String[] labels = new String[nbJours];
        Long[] values = new Long[nbJours];

        for (int i = 0; i < nbJours; i++) {
            Calendar dateJour = Calendar.getInstance();
            dateJour.add(Calendar.DATE, -1*(nbJours - 1 - i));
            String dateMaxStr = new SimpleDateFormat("yyyy-MM-dd").format(dateJour.getTime());
            Date dateJourMax = new SimpleDateFormat("yyyy-MM-dd H:m:s").parse(dateMaxStr+" 23:59:59");
            Date dateJourMin = new SimpleDateFormat("yyyy-MM-dd H:m:s").parse(dateMaxStr+" 00:00:00");

            Iterable<Transaction> transactions = transactionService.getByBetweenDate(banque, client, dateJourMin, dateJourMax, devise);
            String jour = new SimpleDateFormat("dd/MM/yyyy").format(dateJour.getTime());
            labels[i] = jour;
            values[i] = transactions.spliterator().estimateSize();
        }
        data.put("data", values);
        data.put("labels", labels);

        return data;
    }

}
