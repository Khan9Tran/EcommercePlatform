package com.hkteam.ecommerce_platform;

import java.util.Objects;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class EcommercePlatformApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();

        final String POSTGRES_DB = "POSTGRES_DB";
        final String POSTGRES_USER = "POSTGRES_USER";
        final String POSTGRES_PASSWORD = "POSTGRES_PASSWORD";
        final String HOST = "HOST";
        final String PORT = "PORT";
        final String JWT_SIGNER_KEY = "JWT_SIGNER_KEY";

        System.setProperty(POSTGRES_DB, Objects.requireNonNull(dotenv.get(POSTGRES_DB)));
        System.setProperty(POSTGRES_USER, Objects.requireNonNull(dotenv.get(POSTGRES_USER)));
        System.setProperty(POSTGRES_PASSWORD, Objects.requireNonNull(dotenv.get(POSTGRES_PASSWORD)));
        System.setProperty(HOST, Objects.requireNonNull(dotenv.get(HOST)));
        System.setProperty(PORT, Objects.requireNonNull(dotenv.get(PORT)));
        System.setProperty(JWT_SIGNER_KEY, Objects.requireNonNull(dotenv.get(JWT_SIGNER_KEY)));

        SpringApplication.run(EcommercePlatformApplication.class, args);
    }
}
