package com.example.solveitwebsitebackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SolveItWebsiteBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SolveItWebsiteBackendApplication.class, args);
	}

}
