package com.akouma.veyuzwebapp.repository;


import java.util.Map;

    public interface PdfGenerateRepositoy {
        void generatePdfFile(String templateName, Map<String, Object> data, String pdfFileName);
    }