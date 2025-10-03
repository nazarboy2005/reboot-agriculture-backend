package com.hackathon.agriculture_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {MailSenderAutoConfiguration.class})
@EnableScheduling
public class AgricultureBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgricultureBackendApplication.class, args);
	}

}
