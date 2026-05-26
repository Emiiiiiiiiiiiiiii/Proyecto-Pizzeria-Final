package com.pizzas.autenticacion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class AutenticacionApplication {

	public static void main(String[] args) {
		SpringApplication.run(AutenticacionApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
    	return new RestTemplate();
	}

}
