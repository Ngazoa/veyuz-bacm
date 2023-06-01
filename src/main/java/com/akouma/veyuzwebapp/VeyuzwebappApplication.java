package com.akouma.veyuzwebapp;

import com.akouma.veyuzwebapp.graphique.Fenetre;
import com.akouma.veyuzwebapp.property.FileStorageProperties;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.awt.*;

@SpringBootApplication
@EnableConfigurationProperties({
		FileStorageProperties.class
})

public class VeyuzwebappApplication extends Fenetre {

	public static void main(String[] args) {
		var ctx = new SpringApplicationBuilder(VeyuzwebappApplication.class)
				.headless(true).run(args);

		EventQueue.invokeLater(() -> {

			var ex = ctx.getBean(VeyuzwebappApplication.class);
			ex.setVisible(true);
		});
	}
}
