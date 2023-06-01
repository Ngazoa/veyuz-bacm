package com.akouma.veyuzwebapp;

import com.akouma.veyuzwebapp.graphique.Fenetre;
import com.akouma.veyuzwebapp.property.FileStorageProperties;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.awt.*;

@SpringBootApplication
@EnableConfigurationProperties({
		FileStorageProperties.class
})

public class VeyuzwebappApplication extends Fenetre {

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

		var ctx = new SpringApplicationBuilder(VeyuzwebappApplication.class)
				.headless(true).run(args);


		EventQueue.invokeLater(() -> {

			var ex = ctx.getBean(VeyuzwebappApplication.class);
			ex.setVisible(true);
		});
	}
}
