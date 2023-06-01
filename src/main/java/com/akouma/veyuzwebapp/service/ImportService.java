package com.akouma.veyuzwebapp.service;

import com.akouma.veyuzwebapp.model.Banque;
import org.springframework.web.multipart.MultipartFile;

public interface ImportService {

    boolean importProcessing(Banque banque, MultipartFile multipartFile,String type);
}
