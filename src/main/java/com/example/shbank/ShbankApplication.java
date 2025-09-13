package com.example.shbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ShbankApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShbankApplication.class, args);
	}

}
