package com.akouma.veyuzwebapp.utils;

import com.akouma.veyuzwebapp.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Calendar;
import java.util.Random;

public class Upload {

    //String absolutePath = context.getRealPath("resources/upload");
    public static String UPLOAD_DIR = "upload";
    @Autowired
    ServletContext context;
    //public  final String UPLOAD_DIR = context.getRealPath("resources/upload");
    String uri = "" + ServletUriComponentsBuilder.fromCurrentContextPath();

    public static final String generateVeyuzCode() {
        String codeVeyuz = "V-";
        String[] alphabet = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "W", "X", "Y", "Z"};
        Random random = new Random();
        int i = random.nextInt(25);
        int cmp = 1 + random.nextInt(999);
        if (cmp < 10) {
            codeVeyuz += "00" + cmp + alphabet[i];
        } else if (cmp < 100) {
            codeVeyuz += "0" + cmp + alphabet[i];
        } else {
            codeVeyuz += cmp + alphabet[i];
        }

        return codeVeyuz;
    }

    public final String uploadFile(MultipartFile file, FileStorageService fileStorageService, String upload_dir, HttpServletRequest request) throws IOException {

        //String fileName = fileStorageService.storeFile(file,upload_dir);
        UPLOAD_DIR = "" + fileStorageService.PathFileStore();
        //UPLOAD_DIR="upload";


        File uploadDir = new File(UPLOAD_DIR + "/" + upload_dir);

        uploadDir.mkdirs();
        if (file.isEmpty()) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        String fileName = calendar.getTimeInMillis() + "-" + file.getOriginalFilename().replace(" ", "-");
        String uploadFilePath = uploadDir + "/" + fileName;
        Path path = Paths.get(uploadFilePath);

        try {
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] bytes = file.getBytes();
        Files.write(path, bytes);

        System.out.println("YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY - " + path);

        return fileName;
    }
}
