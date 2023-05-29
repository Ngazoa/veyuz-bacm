package com.akouma.veyuzwebapp.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

//    @Bean
//    public JavaMailSender getJavaMailSender() {
//        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
//       // mailSender.setHost("veyuzweb.azurewebsites.net");
//        mailSender.setHost("emmaberanger2@gmail.com");
//        mailSender.setPort(587);
//
//        mailSender.setUsername("emmaberanger2@gmail.com");
//        mailSender.setPassword("luceyqjpakntkwjm");
//
//        Properties props = mailSender.getJavaMailProperties();
//        props.put("mail.transport.protocol", "smtp");
//        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.starttls.enable", "true"); // enable TLS
//        props.put("mail.debug", "true");
//
//        return mailSender;
//    }
//    @Bean
//    public AmazonSimpleEmailService amazonSimpleEmailService() {
//
//        return AmazonSimpleEmailServiceClientBuilder.standard()
//                .withCredentials(new ProfileCredentialsProvider("pratikpoc"))
//                .withRegion(Regions.US_EAST_1)
//                .build();
//    }
//
//    @Bean
//    public MailSender mailSender(
//            AmazonSimpleEmailService amazonSimpleEmailService) {
//        return new SimpleEmailServiceMailSender(amazonSimpleEmailService);
//    }
}
