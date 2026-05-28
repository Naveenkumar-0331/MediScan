package com.mediscan.mediscan_ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
@EnableJpaRepositories(basePackages = "com.mediscan.mediscan_ai.repository.mysql")
@EnableMongoRepositories(basePackages = "com.mediscan.mediscan_ai.repository.mongodb")
public class MediscanAiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MediscanAiApplication.class, args);
	}
}