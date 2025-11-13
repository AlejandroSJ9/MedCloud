package com.medcloud.app;

import com.medcloud.app.domain.enums.RoleName;
import com.medcloud.app.domain.service.EpsService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MedCloudApplication {

	public static void main(String[] args) {
		SpringApplication.run(MedCloudApplication.class, args);
	}

	@Bean
	public CommandLineRunner updateEpsRole(EpsService epsService) {
		return args -> {
			try {
				epsService.updateRole("sanitas@correo.com", RoleName.EPS);
				System.out.println("Role updated successfully for user sanitas@correo.com");
			} catch (Exception e) {
				System.err.println("Error updating role: " + e.getMessage());
			}
		};
	}

}
