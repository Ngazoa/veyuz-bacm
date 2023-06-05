package com.akouma.veyuzwebapp;

import com.akouma.veyuzwebapp.graphique.Fenetre;
import com.akouma.veyuzwebapp.property.FileStorageProperties;
import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
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
		for (DotenvEntry entry : dotenv.entries()) {
			System.setProperty(entry.getKey(), entry.getValue());
		}

		String port = System.getProperty("APP_PORT");

		var ctx = new SpringApplicationBuilder(VeyuzwebappApplication.class)
				.headless(true).run(args);

		EventQueue.invokeLater(() -> {
			var ex = ctx.getBean(VeyuzwebappApplication.class);
			ex.setPort(port);
			ex.initComponents();
			ex.setVisible(true);
		});
	}
}
