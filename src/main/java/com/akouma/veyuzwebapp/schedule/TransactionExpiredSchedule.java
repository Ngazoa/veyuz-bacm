package com.akouma.veyuzwebapp.schedule;

import com.akouma.veyuzwebapp.dto.ClientDto;
import com.akouma.veyuzwebapp.html.HtmlMail;
import com.akouma.veyuzwebapp.model.*;
import com.akouma.veyuzwebapp.service.*;
import com.akouma.veyuzwebapp.utils.StatusTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class TransactionExpiredSchedule {

    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss sss");

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private FichierService fichierService;
    @Autowired
    private HtmlMail htmlMail;
    @Autowired
    private ClientService clientService;
    @Autowired
    private BanqueService banqueService;
    @Autowired
    private ApurementService apurementService;

    private void sendMail(HashMap<String, Object> data, Client client) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        boolean multipart = true;
        MimeMessageHelper helper = new MimeMessageHelper(message, multipart, "utf-8");
        String htmlMessage = htmlMail.getMailTemplate("lettre/mise-demeure", data);
        System.out.println(htmlMessage);
        message.setContent(htmlMessage, "text/html");
        helper.setTo(client.getUser().getEmail());
        helper.setSubject("Mise en demeure");

        mailSender.send(message);
    }

    // ON EXECUTE LA TACHE TOUTES LES 24 HEURES
//    @Scheduled(initialDelay = 5*60*60*1000, fixedDelay = 24*60*60*1000)
//    @Scheduled(initialDelay = 60*60*1000, fixedDelay = 24*60*60*1000)
    public void VerifierExpirationTransaction() throws MessagingException {
        Iterable<Banque> banques = banqueService.getbanques();

        for (Banque banque : banques) {
            // on recupere la liste des clients de la banque
            List<ClientDto> clients = clientService.getClients(banque);
            // Pour chaque client on recupere la liste des transactions non validées et non rejete
            for (ClientDto clientDto : clients) {
                Client client= clientService.findById(Long.parseLong(clientDto.getId()));
                Iterable<Transaction> transactions = transactionService.getNonValidatedAndRejectedClientTransactions(banque, client, StatusTransaction.VALIDATED);
                    miseEndemeure(transactions, client, banque);
            }
        }

    }

//    @Scheduled(initialDelay = 10*1000, fixedDelay = 24*60*60*1000)
//    public void setExpiredApurement() {
//        Iterable<Apurement> apurements = apurementService.getNonApuredAndExpiredApurements();
//        Calendar dateJour = Calendar.getInstance();
//        for (Apurement ap : apurements) {
//            // On verifie si la date est passée
//            if (ap.getDateExpiration() != null && dateJour.getTime().getTime() > ap.getDateExpiration().getTime()) {
//                // On calcul le nombre de jours qui reste
//                Long diff = ap.getDateExpiration().getTime() - dateJour.getTime().getTime();
//                Long nbJoursRestant = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
//                if (ap.getCountEditExpirationDate() <= 0) {
//                    // Si on a jamais prorogé alors on ajoute les 8 jours de surcis
//                    nbJoursRestant += 8;
//                }
//                if (nbJoursRestant <= 0) {
//                    ap.setIsExpired(true);
//                    apurementService.saveApurement(ap);
//                }
//            }
//        }
//    }

    private void miseEndemeure(Iterable<Transaction> transactions, Client client, Banque banque) throws MessagingException {
        Date now = new Date();
        String nowString = df.format(now);
        System.out.println("Now is : " + nowString + " Debut de verification des transactions");
        Collection<HashMap<String, Object>> mailData = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t.getStatut() != StatusTransaction.REJECTED) {
                Date updateDate = t.getLastUpdateAt();
                if (t.getLastUpdateAt() == null) {
                    updateDate = t.getDateCreation();
                }

                Long diff = now.getTime() - updateDate.getTime();
                Long nbJoursEcoules = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

                long milisecondeDateExpiration = t.getDelay().getTime();
                Date dateExpiration = t.getDelay();

                Long diff2 = dateExpiration.getTime() - now.getTime();
                Long nbJoursRestant = TimeUnit.DAYS.convert(diff2, TimeUnit.MILLISECONDS);

                boolean isComplete = true;
                Collection<Fichier> fichiersManquant = new ArrayList<Fichier>();
                Iterable<Fichier> fichiers = fichierService.getFichiersTransaction(t);
                for (Fichier f : fichiers) {
                    if (f.getFileName() == null || !f.isValidated()) {
                        fichiersManquant.add(f);
                        isComplete = false;
                    }
                }

                if (!isComplete && (nbJoursEcoules >= 5 || nbJoursRestant <= 0)) {
                    // ON informe le client
                    if (nbJoursEcoules >= 5 && nbJoursRestant > 0) {
//                        Notification notification = new Notification();
//                        notification.setUtilisateur(t.getClient().getUser());
//                        notification.setRead(false);
//                        String lien = "transaction-"+t.getId()+"/details";
//                        notification.setHref(lien);
//                        String message = "Hi, il vous reste " + nbJoursRestant + " pour fournir toutes les pieces " +
//                                "justificatives de votre transaction.";
//                        notification.setMessage(message);
//                        notificationService.save(notification);
                    }

                    // Envoie de la mise en demeure au format html
                    if (nbJoursRestant <= 0) {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("transaction", t);
                        map.put("fichiers", fichiersManquant);
                        mailData.add(map);
                        t.setDateMiseEnDemeure(now);
                    }
                    t.setLastUpdateAt(now);
                    transactionService.saveTransaction(t);
                } else {
                    // On informe les admin afin qu'ils traitent la transaction
                    System.out.println("Notification envoyer aux admins par rapport a la transaction " + t.getReference());
                }
            }
        }
        // On envoie la mise en demeure uniquement lorsque le delai est arrive
        if (!mailData.isEmpty()) {
            HashMap<String, Object> maps = new HashMap<>();
            maps.put("transactions", mailData);
            maps.put("client", client);
            maps.put("banque", banque);
            sendMail(maps, client);
        }
    }

    /**
     * Cette fonction va nous permettre d'envoyer les mises en demeure aux clients si la date d'expiration est atteinte
     */
    @Scheduled(initialDelay = 24*60*60*1000, fixedDelay = 24*60*60*1000)
    public void envoiyerLesMisesEnDemeure() throws MessagingException {
        // On recupere tous les apurements qui non pas encore expires et qui ne sont pas encore apures
        Iterable<Apurement> apurements = apurementService.getNonApuredAndExpiredApurements();
        // On se rassure que la date de d'envoi de la mise endemeure (date d'expiration) est arrivee
        Calendar dateJour = Calendar.getInstance();
        Collection<String> emails = new ArrayList<>();
        HashMap<String, Client> clients = new HashMap<>();
        HashMap<String, Collection> data = new HashMap<>();
        for (Apurement ap : apurements) {
            Collection<String> documents = new ArrayList<>();
            for (ApurementFichierManquant f : ap.getFichiersManquants()) {
                if (!f.getIsValidated() && f.isForApurement())
                    documents.add(f.getFileName());
            }
            if (!documents.isEmpty()) {
                // On verifie si la date est passée
                if (dateJour.getTime().getTime() >= ap.getDateExpiration().getTime()) {
                    // On calcul le nombre de jours qui reste
                    Long diff = ap.getDateExpiration().getTime() - dateJour.getTime().getTime();
                    Long nbJoursRestant = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                    if (nbJoursRestant <= 0) {
                        // On doit envoyer la mise en demeure pour cet apurement
                        if (!emails.contains(ap.getClient().getUser().getEmail())) {
                            emails.add(ap.getClient().getUser().getEmail());
                            clients.put(ap.getClient().getUser().getEmail(), ap.getClient());
                        }
                        HashMap<String, Object> item = new HashMap<>();
                        item.put("ndos", ap.getReferenceTransaction());
                        item.put("dateOuverture", ap.getDateOuverture());
                        item.put("montant", ap.getMontant());
                        item.put("beneficiaire", ap.getBeneficiaire());
                        item.put("delais", ap.getDateExpiration());
                        item.put("documentsManquants", documents);
                        if (data.containsKey(ap.getClient().getUser().getEmail())) {
                            data.get(ap.getClient().getUser().getEmail()).add(item);
                        }else {
                            Collection apurementsList = new ArrayList();
                            apurementsList.add(item);
                            data.put(ap.getClient().getUser().getEmail(), apurementsList);
                        }
                    }
                }
            }
        }

        // On envoie de mise en demeure
        for (String email : emails) {
            MimeMessage message = mailSender.createMimeMessage();
            boolean multipart = true;
            MimeMessageHelper helper = new MimeMessageHelper(message, multipart, "utf-8");
            Map<String, Object> variables = new HashMap<>();
            variables.put("dateJour", dateJour);
            variables.put("denominationClient", clients.get(email).getDenomination());
            variables.put("adresse", clients.get(email).getUser().getAdresse());
            variables.put("data", data.get(email));
            String htmlMessage = htmlMail.getMailTemplate("lettre/mise-demeure", variables);
            System.out.println(htmlMessage);
            message.setContent(htmlMessage, "text/html");
            helper.setTo(email);
            helper.setSubject("Mise en demeure");
            mailSender.send(message);
        }
    }

}
