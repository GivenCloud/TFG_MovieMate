package com.moviemate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MoviemateBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoviemateBackendApplication.class, args);
	}

}
