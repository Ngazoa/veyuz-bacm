package com.akouma.veyuzwebapp.service;

import com.akouma.veyuzwebapp.form.DomiciliationForm;
import com.akouma.veyuzwebapp.form.ImportFileForm;
import com.akouma.veyuzwebapp.model.*;
import com.akouma.veyuzwebapp.repository.*;
import com.akouma.veyuzwebapp.utils.CryptoUtils;
import com.akouma.veyuzwebapp.utils.Upload;
import lombok.Data;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

@Data
@Service
public class DomiciliationService {

    @Autowired
    FileStorageService fileStorageService;
    @Autowired
    HttpSession session;
    @Autowired
    private DomiciliationRepository domiciliationRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private TypeDeTransactionRepository typeDeTransactionRepository;
    @Autowired
    private BeneficiaireRepository beneficiaireRepository;
    @Autowired
    private DeviseRepository deviseRepository;
    @Autowired
    private CryptoUtils cryptoUtils;

    public DomiciliationRepository getDomiciliationRepository() {
        return domiciliationRepository;
    }

    public void setDomiciliationRepository(DomiciliationRepository domiciliationRepository) {
        this.domiciliationRepository = domiciliationRepository;
    }

    public ClientRepository getClientRepository() {
        return clientRepository;
    }

    public void setClientRepository(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public TypeDeTransactionRepository getTypeDeTransactionRepository() {
        return typeDeTransactionRepository;
    }

    public void setTypeDeTransactionRepository(TypeDeTransactionRepository typeDeTransactionRepository) {
        this.typeDeTransactionRepository = typeDeTransactionRepository;
    }

    public BeneficiaireRepository getBeneficiaireRepository() {
        return beneficiaireRepository;
    }

    public void setBeneficiaireRepository(BeneficiaireRepository beneficiaireRepository) {
        this.beneficiaireRepository = beneficiaireRepository;
    }

    public DeviseRepository getDeviseRepository() {
        return deviseRepository;
    }

    public void setDeviseRepository(DeviseRepository deviseRepository) {
        this.deviseRepository = deviseRepository;
    }

    public Page<Domiciliation> getDomiciliations(Banque banque, int maxResults, int page) {
        return domiciliationRepository.findByBanqueOrderByDateCreationAsc(banque, PageRequest.of(page, maxResults));
    }

    public Page<Domiciliation> getDomiciliationsClientBanque(Banque banque, Client client, int maxResults, int page) {
        return domiciliationRepository.findByBanqueAndClientOrderByDateCreationDesc(banque, client, PageRequest.of(page, maxResults));
    }

    public Iterable<Domiciliation> getDomiciliationsClient(Banque banque, Client client) {
        return domiciliationRepository.findByBanqueAndClientOrderByDateCreationDesc(banque, client);
    }

    public Domiciliation saveDomiciliation(Domiciliation domiciliation) {
        return domiciliationRepository.save(domiciliation);
    }

    @Transactional
    public String importData(ImportFileForm importFile, HttpServletRequest request) throws IOException {
        Upload u = new Upload();
        String uploadRootPath = "/tmp";
        String file_root = u.uploadFile(importFile.getFile(), fileStorageService, uploadRootPath, request);
        Banque banque = (Banque) session.getAttribute("banque");

        String msg = null;
        FileInputStream inputStream = new FileInputStream(Upload.UPLOAD_DIR + "/tmp/" + file_root);
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        XSSFSheet sheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = sheet.iterator();

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Iterator<Cell> cellIterator = row.cellIterator();
            Domiciliation domiciliation = new Domiciliation();
            domiciliation.setBanque(importFile.getBanque());

            Date dateCreation = row.getCell(0).getDateCellValue();
            Date dateExpiration = row.getCell(1).getDateCellValue();
            Double isImportation = row.getCell(2).getNumericCellValue();
            Float montant = (float) row.getCell(3).getNumericCellValue();
            Float montantRestant = (float) row.getCell(4).getNumericCellValue();
            String refDomiciliation = row.getCell(5).getStringCellValue();
            String refClient = row.getCell(6).getStringCellValue();
            String codeDevise = row.getCell(7).getStringCellValue();
            String codeTypeTransaction = row.getCell(8).getStringCellValue();
            String refBeneficiaire = row.getCell(9).getStringCellValue();
            String nomBeneficiaire = row.getCell(10).getStringCellValue();
            Date dateCreationBenef = row.getCell(11).getDateCellValue();

            int cmp = 0;

            if (dateCreation == null || dateExpiration == null || isImportation == null || montant == null || montantRestant == null ||
                    refDomiciliation == null || refClient == null || codeDevise == null || codeTypeTransaction == null ||
                    refBeneficiaire == null || nomBeneficiaire == null || dateCreationBenef == null) {
                if (msg == null) {
                    msg = "Les lignes suivantes n'ont pas pu etre importées, Verifiez que toutes les donnees sont presentes et sont bien formatees <br>";
                }
                msg += "Ligne <b>" + cmp + "</b> impossible de sauvegarder";

            } else {
                Client client = clientRepository.findFirstByReference(refClient);
                System.out.println(client);
                Devise devise = deviseRepository.findFirstByCode(codeDevise);
                TypeDeTransaction typeDeTransaction = typeDeTransactionRepository.findFirstByCode(codeTypeTransaction);
                System.out.println("*****" + refBeneficiaire);
                Beneficiaire beneficiaire = beneficiaireRepository.findFirstByReference(refBeneficiaire);
                if (beneficiaire == null && client != null) {
                    beneficiaire = new Beneficiaire();
                    beneficiaire.setDateCreation(dateCreationBenef);
                    beneficiaire.setReference(refBeneficiaire);
                    beneficiaire.setName(nomBeneficiaire);
                    beneficiaire.setBanque(banque);
                    beneficiaire = beneficiaireRepository.save(beneficiaire);
                }

                if (client == null || montant == null || montantRestant == null || typeDeTransaction == null || beneficiaire == null || devise == null || dateCreation == null || dateExpiration == null) {

                    if (msg == null) {
                        msg = "Les lignes suivantes n'ont pas pu etre importées, Verifiez que toutes les donnees sont presentes et sont bien formatees <br>";
                    }
                    msg += "Ligne <b>" + cmp + "</b> impossible de sauvegarder";
                } else {
                    boolean isImp = isImportation > 0;
                    domiciliation.setImportation(isImp);
                    domiciliation.setDateExpiration(dateExpiration);
                    domiciliation.setMontantRestant(montantRestant);
                    domiciliation.setDevise(devise);
                    domiciliation.setDateCreation(dateCreation);
                    domiciliation.setTypeDeTransaction(typeDeTransaction);
                    domiciliation.setBeneficiaire(beneficiaire);
                    domiciliation.setClient(client);
                    domiciliation.setReference(refDomiciliation);
                    domiciliation.setMontant(montant);
                    domiciliationRepository.save(domiciliation);
                }
            }
        }

        return msg;
    }

    public Domiciliation saveDomiciliation(DomiciliationForm domiciliationForm) throws Exception {
        Domiciliation domiciliation = domiciliationForm.getDomiciliation();
        if (domiciliation == null || domiciliation.getId() == null) {
            domiciliation = new Domiciliation();
        }

        Client client=clientRepository.findById(CryptoUtils.decrypt(domiciliationForm.getClient())).orElse(null);
        domiciliation.setClient(client);
        domiciliation.setMontant(domiciliationForm.getMontant());
        domiciliation.setBeneficiaire(domiciliationForm.getBeneficiaire());
        domiciliation.setBanque(domiciliationForm.getBanque());
        domiciliation.setReference(domiciliationForm.getReference());
        domiciliation.setTypeDeTransaction(domiciliationForm.getTypeDeTransaction());
        domiciliation.setDevise(domiciliationForm.getDevise());
        domiciliation.setDateExpiration(domiciliationForm.getDateExpiration());
        domiciliation.setMontantRestant(domiciliationForm.getMontantRestant());
        domiciliation.setDateCreation(domiciliationForm.getDateCreation());
        domiciliation.setImportation(domiciliationForm.isImportation());

        return this.saveDomiciliation(domiciliation);
    }

    public boolean getByReference(String reference) {
        Domiciliation d = domiciliationRepository.findFirstByReference(reference);

        return d == null;
    }

    public Iterable<Domiciliation> getAllBanqueDomiciliations(Banque banque) {
        return domiciliationRepository.findByBanque(banque);
    }

    public Optional<Domiciliation> findById(Long id) {
        return Optional.of(domiciliationRepository.findById(id).orElseThrow(
                () -> new NoSuchElementException("Agence not found with id: " + id)
        ));
    }

    public void delete(Domiciliation domiciliation) {
        domiciliationRepository.delete(domiciliation);
    }
}
