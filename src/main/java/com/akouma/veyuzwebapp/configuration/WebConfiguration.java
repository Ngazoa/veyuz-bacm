package com.akouma.veyuzwebapp.configuration;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfiguration implements WebMvcConfigurer {
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:validation");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
    private static final String[] CLASSPATH_RESOURCE_LOCATIONS = {
            "classpath:/META-INF/resources/",
            "classpath:/static/",  "classpath:/public/" };
    private static final String[] RESOURCE_LOCATIONS = {
            "/**" };

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler(RESOURCE_LOCATIONS)
                .addResourceLocations(CLASSPATH_RESOURCE_LOCATIONS);
    }
//    @Bean
//    public ClassLoaderTemplateResolver emailTemplat
//    eResolver() {
//        ClassLoaderTemplateResolver pdfTemplateResolver = new ClassLoaderTemplateResolver();
//        pdfTemplateResolver.setPrefix("pdf-templates/");
//        pdfTemplateResolver.setSuffix(".html");
//        pdfTemplateResolver.setTemplateMode("HTML5");
//        pdfTemplateResolver.setCharacterEncoding("UTF-8");
//        pdfTemplateResolver.setOrder(1);
//        return pdfTemplateResolver;
//    }
}