package com.akouma.veyuzwebapp.utils.pdf;

import com.akouma.veyuzwebapp.model.Transaction;
import com.itextpdf.html2pdf.HtmlConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class LettreEngagement {

    @Autowired
    private TemplateEngine templateEngine;

    public byte[] getImportationBiens(Transaction transaction, HttpServletRequest request, HttpServletResponse response) throws IOException {



        WebContext context = new WebContext(request, response, request.getServletContext());

        context.setVariable("date", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
        context.setVariable("nomBeneficiaire", transaction.getBeneficiaire().getName());
        context.setVariable("nomTypeTransaction", transaction.getTypeDeTransaction().getName());

        context.setVariable("referenceDomiciliation", transaction.getDomiciliation().getReference());
        context.setVariable("dateDomiciliation", new SimpleDateFormat("dd/mm/yyyy").format(transaction.getDomiciliation().getDateCreation()));
        context.setVariable("montantTransaction", transaction.getMontant());
        context.setVariable("devise", transaction.getDevise().getName() + " (" + transaction.getDevise().getCode() + ")");

        // Il faut demander à quoi renvoie ces parametres
        // Il faut demander à quoi renvoie ces parametres
        context.setVariable("numeroFacture", ".................................................");
        context.setVariable("dateFacture", "........../.........../............");
        context.setVariable("SGSNumber", "......................................");
        context.setVariable("dateSGS", "........../.........../............`");
        context.setVariable("chez", "......................................................................................................................................");

        String htmlLetter = templateEngine.process("lettre/importationbiens", context);

        ByteArrayOutputStream target = new ByteArrayOutputStream();

        HtmlConverter.convertToPdf(htmlLetter, target);

        byte[] bytes = target.toByteArray();

        return bytes;

    }

    public byte[] getImportationServices(Transaction transaction, HttpServletRequest request, HttpServletResponse response) throws IOException {

        WebContext context = new WebContext(request, response, request.getServletContext());

        context.setVariable("date", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
        context.setVariable("nomBeneficiaire", transaction.getBeneficiaire().getName());
        context.setVariable("nomTypeTransaction", transaction.getTypeDeTransaction().getName());

        context.setVariable("referenceDomiciliation", transaction.getDomiciliation().getReference());
        context.setVariable("dateDomiciliation", new SimpleDateFormat("dd/mm/yyyy").format(transaction.getDomiciliation().getDateCreation()));
        context.setVariable("montantTransaction", transaction.getMontant());
        context.setVariable("devise", transaction.getDevise().getName() + " (" + transaction.getDevise().getCode() + ")");

        // Il faut demander à quoi renvoie ces parametres
        context.setVariable("numeroFacture", "..........................................");
        context.setVariable("dateFacture", "........../.........../............");
        context.setVariable("SGSNumber", "...............................");
        context.setVariable("dateSGS", "........../.........../............`");
        context.setVariable("chez", "......................................................................................................................................");

        String htmlLetter = templateEngine.process("lettre/importationbiens", context);

        ByteArrayOutputStream target = new ByteArrayOutputStream();

        HtmlConverter.convertToPdf(htmlLetter, target);

        byte[] bytes = target.toByteArray();

        return bytes;

    }

}
