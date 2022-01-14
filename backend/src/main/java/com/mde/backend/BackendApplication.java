package com.mde.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.io.File;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
// for testing consumption of REST apis
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import com.mde.backend.domain.DataQualityDTO;


@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}
	

	
	
	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) { // initialize database
		return args -> {
			   		};
	}
	
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

	
	

}
