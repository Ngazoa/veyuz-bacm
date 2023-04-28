package com.akouma.veyuzwebapp.service;

import com.akouma.veyuzwebapp.exception.MyFileNotFoundException;
import com.akouma.veyuzwebapp.property.FileStorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileStorageService {

  private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();
        //this.fileStorageLocation = request.getServletContext().getRealPath("upload");

        System.out.println("HHHHHHHHHHHHHHHHHHHHHHH "+fileStorageLocation);


//        try {
//            Files.createDirectories(this.fileStorageLocation);
//        } catch (Exception ex) {
//            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
//        }
    }
    public  Path PathFileStore(){
        return fileStorageLocation;
    }

//    public String storeFile(MultipartFile file,String position) {
//        // Normalize file name
//        Calendar calendar = Calendar.getInstance();
//        if(!file.getOriginalFilename().isEmpty()){
//        String fileName = calendar.getTimeInMillis() + "-" + file.getOriginalFilename().replace(" ", "-");
//
//        try {
//            // Check if the file's name contains invalid characters
//            if(fileName.contains("..")) {
//                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName+"/"+position);
//            }
//
//            // Copy file to the target location (Replacing existing file with the same name)
//            Path targetLocation = this.fileStorageLocation.resolve(position +"/"+ fileName);
//            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
//
//            return fileName;
//        } catch (IOException ex) {
//            throw new FileStorageException("Could not store file "  + position+". Please try again!", ex);
//        }
//        }
//        return null;
//    }


    public Resource loadFileAsResource(String fileName,String position) {
        try {
            Path filePath = this.fileStorageLocation.resolve( position + "/" + fileName).normalize();
          //  System.out.println("UUUURRRRRLLLLLLLLLLLLL   :    "+filePath);
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new MyFileNotFoundException("File not found " + fileName, ex);
        }
    }
}
