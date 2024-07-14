package com.hkteam.ecommerce_platform;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Objects;

@SpringBootApplication
public class EcommercePlatformApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load();

		final String POSTGRES_DB = "POSTGRES_DB";
		final String POSTGRES_USER = "POSTGRES_USER";
		final String POSTGRES_PASSWORD = "POSTGRES_PASSWORD";
		final String HOST = "HOST";
		final String PORT = "PORT";

		System.setProperty(POSTGRES_DB, Objects.requireNonNull(dotenv.get(POSTGRES_DB)));
		System.setProperty(POSTGRES_USER, Objects.requireNonNull(dotenv.get(POSTGRES_USER)));
		System.setProperty(POSTGRES_PASSWORD, Objects.requireNonNull(dotenv.get(POSTGRES_PASSWORD)));
		System.setProperty(HOST, Objects.requireNonNull(dotenv.get(HOST)));
		System.setProperty(PORT, Objects.requireNonNull(dotenv.get(PORT)));

		SpringApplication.run(EcommercePlatformApplication.class, args);
	}

}
