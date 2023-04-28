package com.akouma.veyuzwebapp.configuration;

import com.akouma.veyuzwebapp.utils.MailConstant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("email-smtp.us-east-1.amazonaws.com");
        mailSender.setPort(587);
        mailSender.setUsername(MailConstant.MY_GMAIL_EMAIL);
        mailSender.setPassword(MailConstant.MY_GMAIL_PASSWORD);

        Properties properties = mailSender.getJavaMailProperties();
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls", "true");
        properties.put("mail.debug", "true");

        return mailSender;
    }
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
