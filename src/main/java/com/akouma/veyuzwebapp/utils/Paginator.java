package com.akouma.veyuzwebapp.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

@Component
public class Paginator {

    @Autowired
    private TemplateEngine templateEngine;

    public String getPagination(int nbPages, int currentPage, String uri) {
        Context context = new Context();
        String pagination = null;
        if (nbPages > 1) {
            Map<String, Object> data = new HashMap<>();
            int[] pages = new int[nbPages];
            for(int i = 0; i < nbPages; i++) {
                pages[i] = i;
            }
            data.put("pages", pages);
            data.put("current", currentPage+1);
            data.put("uri", uri);
            context.setVariables(data);
            pagination = templateEngine.process("pagination", context);
        }

        return pagination;
    }
}
