package com.akouma.veyuzwebapp.restcontroller;

import com.akouma.veyuzwebapp.model.Client;
import com.akouma.veyuzwebapp.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

public class ClientRestController {
    @Autowired
    ClientService clientService;

    @PostMapping("/save-client")
    Client saveRestClient(@ModelAttribute Client client) {
        return clientService.saveClient(client);
    }
}
