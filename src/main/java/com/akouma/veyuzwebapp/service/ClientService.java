package com.akouma.veyuzwebapp.service;

import com.akouma.veyuzwebapp.VeyuzwebappApplication;
import com.akouma.veyuzwebapp.dto.ClientDto;
import com.akouma.veyuzwebapp.form.ClientForm;
import com.akouma.veyuzwebapp.form.ImportFileForm;
import com.akouma.veyuzwebapp.model.*;
import com.akouma.veyuzwebapp.repository.AppRoleRepository;
import com.akouma.veyuzwebapp.repository.ClientRepository;
import com.akouma.veyuzwebapp.repository.UserRepository;
import com.akouma.veyuzwebapp.repository.UserRoleRepository;
import com.akouma.veyuzwebapp.utils.CryptoUtils;
import com.akouma.veyuzwebapp.utils.Upload;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import javax.servlet.http.HttpServletRequest;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@Data
@Service
public class ClientService {


    private static String UPLOAD_DIR = "/kyc";
    @Autowired
    FileStorageService fileStorageService;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CryptoUtils cryptoUtils;
    @Autowired
    private AppRoleRepository appRoleRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;

    public ClientRepository getClientRepository() {
        return clientRepository;
    }

    public void setClientRepository(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AppRoleRepository getAppRoleRepository() {
        return appRoleRepository;
    }

    public void setAppRoleRepository(AppRoleRepository appRoleRepository) {
        this.appRoleRepository = appRoleRepository;
    }

    public UserRoleRepository getUserRoleRepository() {
        return userRoleRepository;
    }

    public void setUserRoleRepository(UserRoleRepository userRoleRepository) {
        this.userRoleRepository = userRoleRepository;
    }

    public Optional<Client> getClient(final Long id) {
        return clientRepository.findById(id);
    }

    public Page<Client> getClients(Banque banque, int maxResults, int page) {
        return clientRepository.findDistinctReferenceByBanques(banque, PageRequest.of(page, maxResults));
    }

    public long countClientAgence(Agence agence) {
        return clientRepository.countByAgence(agence);
    }

    public Page<Client>findAllClientAgence(Agence agence,Banque banque,int maxResults, int page) {
        return clientRepository.findByBanquesAndAgenceOrderByUser_NomAsc(banque,agence, PageRequest.of(page, maxResults));
    }

    public Iterable<ClientDto> searchClients(Banque banque, String nom) {
        List<Client> clienRenvoye = new ArrayList<>();
        Client clientFound = clientRepository.findByTelephone(nom);
        if (clientFound != null) {
            clienRenvoye.add(clientFound);
        }
        return clienRenvoye.stream().map(
                client -> {
                    ClientDto client1 = new ClientDto();
                    client1.setTelephone(client.getTelephone());
                    try {
                        client1.setId(CryptoUtils.encrypt(client.getId()));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    client1.setUser(client.getUser());
                    client1.setDomiciliations(client.getDomiciliations());
                    client1.setKyc(client.getKyc());
                    client1.setDenomination(client.getDenomination());
                    client1.setReference(client.getReference());
                    client1.setNumeroContribuable(client.getNumeroContribuable());
                    client1.setTypeClient(client.getTypeClient());
                    client1.setNiu(client.getNiu() == null ? client.getNumeroContribuable() : client1.getNiu());
                    if (client.getNiu() == null) {
                        saveClient(client);
                    }
                    return client1;
                }
        ).collect(Collectors.toList());
    }


    public Client saveClient(Client client) {
        if (client.getReference() == null || client.getReference() == "") {
            client.setReference("C-" + new Date().getTime() + "-VEYUZ");
        }
        client.setNiu(client.getNumeroContribuable());

        return clientRepository.save(client);
    }

    public List<ClientDto> searchClientsByName(Banque banque, String query) {
        List<Client> clients = clientRepository.
                findByUser_BanqueLikeOrTelephoneContainingOrDenominationContainingOrReferenceContaining(banque,
                        query, query, query);
        return clients.stream().map(
                client -> {
                    ClientDto client1 = new ClientDto();
                    client1.setTelephone(client.getTelephone());
                    try {
                        client1.setId(CryptoUtils.encrypt(client.getId()));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    client1.setUser(client.getUser());
                    client1.setDomiciliations(client.getDomiciliations());
                    client1.setKyc(client.getKyc());
                    client1.setDenomination(client.getDenomination());
                    client1.setReference(client.getReference());
                    client1.setNumeroContribuable(client.getNumeroContribuable());
                    client1.setTypeClient(client.getTypeClient());
                    client1.setNiu(client.getNiu() == null ? client.getNumeroContribuable() : client1.getNiu());
                    if (client.getNiu() == null) {
                        saveClient(client);
                    }
                    return client1;
                }
        ).collect(Collectors.toList());

    }

//    public List<Client> searchClientsAgenceByName(Banque banque,Agence agence, String query) {
//        List<Client> clients = clientRepository.
//                findByUser_AgenceAndBanqueLikeOrTelephoneContainingOrDenominationContainingOrReferenceContaining(
//                        agence,banque,
//                        query, query, query);
//        return clients;
//
//    }


    public void setEnable(boolean isEnable, Client client) {
        client.getUser().setEnable(isEnable);
        clientRepository.save(client);
    }

    public Client findById(long id) {
        Optional<Client> client = clientRepository.findById(id);
        if (client.isEmpty()) {
            return null;
        }
        return client.get();
    }

    @Transactional
    public Client saveClientForm(ClientForm clientForm, HttpServletRequest request) throws ParseException, IOException {
        Upload u = new Upload();
        String uploadRootPath = "/kyc";

        Client client = new Client();
        if (clientForm.getClient() != null) {
            client = clientForm.getClient();
        }
        if (client.getReference() == null) {
            client.setReference("C-" + new Date().getTime() + "-VEYUZ");
        }
        System.out.println("====>1");
        String codeVeyuz = client.getCodeVeyuz();

        if (client.getCodeVeyuz() == null || client.getCodeVeyuz().trim() == "") {
            boolean existe = true;
            while (existe) {
                codeVeyuz = Upload.generateVeyuzCode();
                if (clientRepository.findFirstByCodeVeyuz(codeVeyuz) == null) {
                    existe = false;
                }
            }
        }
        client.setCodeVeyuz(codeVeyuz);
        client.setTypeClient(clientForm.getTypeClient());
        client.setDenomination(clientForm.getDenomination());
        client.setTelephone(clientForm.getTelephone());
        client.setNumeroContribuable(clientForm.getNumeroContribuable());
        String kyc = u.uploadFile(clientForm.getKycFile(), fileStorageService, uploadRootPath, request);
        client.setKyc(kyc);

        Collection<Banque> banques = client.getBanques();
        if (banques == null) {
            banques = new ArrayList<>();
        }
        if (!banques.contains(clientForm.getBanque())) {
            banques.add(clientForm.getBanque());
        }
        client.setBanques(banques);
        client.setAgence(clientForm.getAgence());

        Client savedClient = saveClient(client);

//        AppUser appUser = clientForm.getAppUser();

//        String avatar = u.uploadFile(clientForm.getAvatar(),fileStorageService, "/avatar",request);

//        AppUser appUser = new AppUser();
//        appUser.setAvatar(avatar);
//        appUser.setEnable(false);
//        appUser.setClient(savedClient);

//        userRepository.save(appUser);

//        appUser.getUserRoles().add(userRole);

        return savedClient;
    }

    public void importData(ImportFileForm importFile, Banque banque, HttpServletRequest request) throws IOException {

        Upload u = new Upload();
        String uploadRootPath = "/tmp";
        String file_root = u.uploadFile(importFile.getFile(), fileStorageService, uploadRootPath, request);
        //Read XSL file
        String msg = null;
        FileInputStream inputStream = new FileInputStream(UPLOAD_DIR + "/tmp/" + file_root);
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        XSSFSheet sheet = workbook.getSheetAt(0);

        // Get iterator to all the rows in current sheet
        Iterator<Row> rowIterator = sheet.iterator();

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            // Get iterator to all cells of current row
            Iterator<Cell> cellIterator = row.cellIterator();
            Client client = clientRepository.findFirstByReference(row.getCell(0).getStringCellValue());
            if (client == null) {
                client = new Client();
                Collection<Banque> banques = new ArrayList<>();
                banques.add(importFile.getBanque());
                client.setBanques(banques);
                AppUser appUser = new AppUser();
                appUser.setEnable(true);

                appUser.setPassword(new BCryptPasswordEncoder().encode("c1234"));
                int cmp = 0;
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    switch (cmp) {
                        case 0:
                            Client client1 = clientRepository.findFirstByReference(cell.getStringCellValue());
                            if (client1 != null) {
                                client = client1;
                                if (client.getBanques() == null || client.getBanques().isEmpty()) {
                                    client.setBanques(banques);
                                } else {
                                    client.getBanques().add(importFile.getBanque());
                                }
                            } else {
                                client.setReference(cell.getStringCellValue());
                            }

                            break;
                        case 1:
                            appUser.setNom(cell.getStringCellValue());
                            break;
                        case 2:
                            appUser.setPrenom(cell.getStringCellValue());
                            break;
                        case 3:
                            appUser.setDateNaissance(cell.getDateCellValue());
                            break;
                        case 4:
                            appUser.setLieuNaissance(cell.getStringCellValue());
                            break;
                        case 5:
                            appUser.setGender(cell.getStringCellValue());
                            break;
                        case 6:
                            appUser.setTelephone1(cell.getStringCellValue());
                            break;
                        case 7:
                            appUser.setTelephone2(cell.getStringCellValue());
                            break;
                        case 8:
                            appUser.setAdresse(cell.getStringCellValue());
                            break;
                        case 9:
                            appUser.setEmail(cell.getStringCellValue());
                            break;
                        case 10:
                            if (client == null || client.getId() == null) {
                                client.setDenomination(cell.getStringCellValue());
                            }
                            break;
                        case 11:
                            if (client == null || client.getId() == null) {
                                client.setNumeroContribuable(cell.getStringCellValue());
                                client.setNiu(cell.getStringCellValue());
                            }
                            break;
                        case 12:
                            if (client == null || client.getId() == null) {
                                client.setTelephone(cell.getStringCellValue());
                            }
                            break;
                        case 13:
                            if (client == null || client.getId() == null) {
                                client.setTypeClient(cell.getStringCellValue());
                            }
                            break;
                    }
                    cmp++;
                }

                boolean existe = true;
                while (existe) {
                    String codeVeyuz = Upload.generateVeyuzCode();
                    if (clientRepository.findFirstByCodeVeyuz(codeVeyuz) == null) {
                        client.setCodeVeyuz(codeVeyuz);
                        existe = false;
                    }
                }

                appUser.setUserName(appUser.getEmail().replace("@", "").replace(".", ""));
                client.setUser(appUser);
                client.setTelephone(row.getCell(6).getStringCellValue());
                appUser.setClient(client);

                Client savedClient = clientRepository.save(client);
                appUser.setClient(savedClient);
                appUser.setBanque(banque);
                AppUser savedAppUser = userRepository.save(appUser);

                AppRole appRoleClient = appRoleRepository.findFirstByRoleName("ROLE_CLIENT");
                UserRole role = new UserRole();
//                role.setAppUser(appUser);
//                role.setAppRole(appRoleClient);

                UserRole userRole = userRoleRepository.save(role);
            } else {
                if (client.getBanques().contains(importFile.getBanque())) {
                    client.getBanques().add(importFile.getBanque());
                    clientRepository.save(client);
                }
            }
        }
    }

    public boolean getByCode(String codeVeyuz) {
        Client client = clientRepository.findFirstByCodeVeyuz(codeVeyuz);
        return client == null;
    }

    public Iterable<Client> getClients() {
        return clientRepository.findAll();
    }

    public List<ClientDto> getClients(Banque banque) {
        return clientRepository.findDistinctReferenceByBanques(banque).stream().map(
                client -> {
                    ClientDto client1 = new ClientDto();
                    client1.setTelephone(client.getTelephone());
                    try {
                        client1.setId(CryptoUtils.encrypt(client.getId()));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    client1.setUser(client.getUser());
                    client1.setDomiciliations(client.getDomiciliations());
                    client1.setKyc(client.getKyc());
                    client1.setDenomination(client.getDenomination());
                    client1.setReference(client.getReference());
                    client1.setNumeroContribuable(client.getNumeroContribuable());
                    client1.setTypeClient(client.getTypeClient());
                    client1.setNiu(client.getNiu() == null ? client.getNumeroContribuable() : client1.getNiu());
                    if (client.getNiu() == null) {
                        saveClient(client);
                    }
                    return client1;
                }
        ).collect(Collectors.toList());
    }

    public ClientDto getClient(Client client) {
        if (client == null) {
            return null;
        }
        ClientDto client1 = new ClientDto();
        client1.setTelephone(client.getTelephone());
        try {
            client1.setId(CryptoUtils.encrypt(client.getId()));

        } catch (Exception e) {
            e.printStackTrace();
        }
        client1.setUser(client.getUser());
        client1.setDomiciliations(client.getDomiciliations());
        client1.setKyc(client.getKyc());
        client1.setDenomination(client.getDenomination());
        client1.setReference(client.getReference());
        client1.setNumeroContribuable(client.getNumeroContribuable());
        client1.setTypeClient(client.getTypeClient());
        client1.setNiu(client.getNiu() == null ? client.getNumeroContribuable() : client1.getNiu());
        return client1;
    }

    public Client getClientByReference(String referenceClient) {
        return clientRepository.findFirstByReference(referenceClient);
    }

    public long count(Banque banque) {
        if (banque == null) {
            return clientRepository.count();
        }
        List<ClientDto> clients = getClients(banque);
        return clients.size();
    }

    public Client findByNiu(String niu) {
        return clientRepository.findByNiu(niu).orElseThrow(
                () -> new NoSuchElementException("Le client dont le NIU est " + niu + " n'existe pas!")
        );
    }

    public void delete(Client client) {
        clientRepository.delete(client);
    }

    public class ClientSpecifications {
        static Specification<Client> searchClients(Banque banque, String telephone, String denomination, String reference) {
            return (root, query, builder) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(builder.equal(root.get("user").get("banque"), banque));

                if (telephone != null && !telephone.isEmpty()) {
                    predicates.add(builder.like(root.get("telephone"), "%" + telephone));
                }

                if (denomination != null && !denomination.isEmpty()) {
                    predicates.add(builder.like(root.get("denomination"), "%" + denomination));
                }

                if (reference != null && !reference.isEmpty()) {
                    predicates.add(builder.like(root.get("reference"), "%" + reference));
                }

                return builder.and(predicates.toArray(new Predicate[0]));
            };
        }

    }
}
