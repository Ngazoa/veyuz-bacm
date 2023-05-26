package com.akouma.veyuzwebapp;

import com.akouma.veyuzwebapp.property.FileStorageProperties;
import io.github.cdimascio.dotenv.Dotenv;
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
		Dotenv dotenv = Dotenv.load();

		// Retrieve database connection properties
		String dbUrl = dotenv.get("DB_URL");
		String dbUsername = dotenv.get("DB_USERNAME");
		String dbPassword = dotenv.get("DB_PASSWORD");

		// Set database connection properties as system properties
		System.setProperty("DB_URL", dbUrl);
		System.setProperty("DB_USERNAME", dbUsername);
		System.setProperty("DB_PASSWORD", dbPassword);
		SpringApplication.run(VeyuzwebappApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		System.out.println(encoder.encode("123400"));
	}
}
