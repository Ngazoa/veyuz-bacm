package com.akouma.veyuzwebapp.html;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
public class HtmlMail{

    @Autowired
    private TemplateEngine templateEngine;

    /**
     *
     * @param templateName Nom du template
     * @param data les donnees qui seront envoyées au template
     * @return on retourne le mail au format html
     */
    public String getMailTemplate(String templateName, Map<String, Object> data) {
        Context context = new Context();
        context.setVariables(data);
        String htmlContent = templateEngine.process(templateName, context);

        return htmlContent;
    }
}
