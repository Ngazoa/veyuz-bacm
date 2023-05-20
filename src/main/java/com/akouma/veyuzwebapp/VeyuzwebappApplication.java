package com.akouma.veyuzwebapp;

import com.akouma.veyuzwebapp.property.FileStorageProperties;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@EnableConfigurationProperties({
		FileStorageProperties.class
})

public class VeyuzwebappApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(VeyuzwebappApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		System.out.println(encoder.encode("123400"));
	}
}
