package com.akouma.veyuzwebapp.service.ImplService;

import com.akouma.veyuzwebapp.model.Banque;
import com.akouma.veyuzwebapp.repository.*;
import com.akouma.veyuzwebapp.service.ImportService;
import com.akouma.veyuzwebapp.utils.ImportExcelFileUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImportServiceImpl implements ImportService {

    private final ImportExcelFileUtil importExcelFileUtilSrviceMethod;
    private final ClientRepository clientRepository;
    private final BeneficiaireRepository beneficiaireRepository;
    private final TransactionRepository transactionRepository;
    private final AgenceRepository agenceRepository;
    private final DomiciliationRepository domiciliationRepository;

    public ImportServiceImpl(ImportExcelFileUtil importExcelFileUtilServiceMethod, ClientRepository
            clientRepository, BeneficiaireRepository beneficiaireRepository,
                             TransactionRepository transactionRepository, AgenceRepository agenceRepository, DomiciliationRepository domiciliationRepository) {
        this.importExcelFileUtilSrviceMethod = importExcelFileUtilServiceMethod;
        this.clientRepository = clientRepository;
        this.beneficiaireRepository = beneficiaireRepository;
        this.transactionRepository = transactionRepository;
        this.agenceRepository = agenceRepository;
        this.domiciliationRepository = domiciliationRepository;
    }

    @Override
    public boolean importProcessing(Banque banque, MultipartFile multipartFile, String type) {
        if (type.equalsIgnoreCase("CLIENT")) {
            try {
                clientRepository.saveAll(importExcelFileUtilSrviceMethod.readExcelFileClient(multipartFile, banque));
                return true;
            } catch (Exception e) {
                System.out.println("=====>  " + e);
            }
        } else if (type.equalsIgnoreCase("AGENCE")) {
            try {
                agenceRepository.saveAll(importExcelFileUtilSrviceMethod.readExcelFileAgency(multipartFile, banque));
                return true;
            } catch (Exception e) {
                System.out.println("=====>  " + e);
            }
        } else if (type.equalsIgnoreCase("DOMICILIATION")) {
            try {
                domiciliationRepository.saveAll(importExcelFileUtilSrviceMethod.readExcelFileDomiciliation(multipartFile, banque));
                return true;
            } catch (Exception e) {
                System.out.println("=====>  " + e);
            }
        } else if (type.equalsIgnoreCase("BENEFICIAIRE")) {
            try {
                beneficiaireRepository.saveAll(importExcelFileUtilSrviceMethod.readExcelBeneficiaireFile(multipartFile, banque));
                return true;
            } catch (Exception e) {
                System.out.println("=====>  " + e);
            }
        } else if (type.equalsIgnoreCase("TRANSACTION")) {
            try {
                transactionRepository.saveAll(importExcelFileUtilSrviceMethod.readExcelFileTransactionClient(multipartFile, banque));
                return true;
            } catch (Exception e) {
                System.out.println("=====>  " + e);
            }
        }
        return false;
    }
}
