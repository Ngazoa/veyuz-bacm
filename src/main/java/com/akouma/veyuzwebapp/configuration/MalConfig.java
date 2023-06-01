package com.akouma.veyuzwebapp.configuration;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MalConfig {

    private final String dbUsername;
    private final String dbPassword;
    private final String host;
    private final int port;
    private final String enableTls;
    private final String enableAuth;

    public MalConfig() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        // Access your environment variables using dotenv.get("KEY")
        dbUsername = dotenv.get("usernameMail");
        dbPassword = dotenv.get("passwordMail");
        host = dotenv.get("hostMail");
        port = Integer.parseInt(dotenv.get("portMail"));
        enableTls=dotenv.get("enableTls");
        enableAuth=dotenv.get("enableAuth");
    }

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
       // mailSender.setHost("veyuzweb.azurewebsites.net");
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(dbUsername);
        mailSender.setPassword(dbPassword);
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", enableAuth);
        props.put("mail.smtp.starttls.enable", enableTls); // enable TLS
        props.put("mail.debug", "true");
        return mailSender;
    }

}
