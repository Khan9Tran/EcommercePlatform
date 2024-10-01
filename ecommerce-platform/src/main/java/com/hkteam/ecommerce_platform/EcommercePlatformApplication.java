package com.hkteam.ecommerce_platform;

import java.util.Objects;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class EcommercePlatformApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();

        final String POSTGRES_DB = "POSTGRES_DB";
        final String POSTGRES_USER = "POSTGRES_USER";
        final String POSTGRES_PASSWORD = "POSTGRES_PASSWORD";
        final String HOST = "HOST";
        final String PORT = "PORT";
        final String JWT_SIGNER_KEY = "JWT_SIGNER_KEY";

        // Mail
        final String JWT_MAIL_KEY = "JWT_MAIL_KEY";
        final String MAIL_USER = "MAIL_USER";
        final String MAIL_PASSWORD = "MAIL_PASSWORD";

        // Cloudinary
        final String CLOUD_NAME = "CLOUD_NAME";
        final String API_KEY = "API_KEY";
        final String API_SECRET = "API_SECRET";

        //Google
        final String GOOGLE_CLIENT_ID = "GOOGLE_CLIENT_ID";
        final String GOOGLE_CLIENT_SECRET
                = "GOOGLE_CLIENT_SECRET";
        final String GOOGLE_REDIRECT_URI = "GOOGLE_REDIRECT_URI";

        System.setProperty(POSTGRES_DB, Objects.requireNonNull(dotenv.get(POSTGRES_DB)));
        System.setProperty(POSTGRES_USER, Objects.requireNonNull(dotenv.get(POSTGRES_USER)));
        System.setProperty(POSTGRES_PASSWORD, Objects.requireNonNull(dotenv.get(POSTGRES_PASSWORD)));
        System.setProperty(HOST, Objects.requireNonNull(dotenv.get(HOST)));
        System.setProperty(PORT, Objects.requireNonNull(dotenv.get(PORT)));
        System.setProperty(JWT_SIGNER_KEY, Objects.requireNonNull(dotenv.get(JWT_SIGNER_KEY)));
        System.setProperty(CLOUD_NAME, Objects.requireNonNull(dotenv.get(CLOUD_NAME)));
        System.setProperty(API_KEY, Objects.requireNonNull(dotenv.get(API_KEY)));
        System.setProperty(API_SECRET, Objects.requireNonNull(dotenv.get(API_SECRET)));
        System.setProperty(JWT_MAIL_KEY, Objects.requireNonNull(dotenv.get(JWT_MAIL_KEY)));
        System.setProperty(MAIL_USER, Objects.requireNonNull(dotenv.get(MAIL_USER)));
        System.setProperty(MAIL_PASSWORD, Objects.requireNonNull(dotenv.get(MAIL_PASSWORD)));
        System.setProperty(GOOGLE_CLIENT_ID, Objects.requireNonNull(dotenv.get(GOOGLE_CLIENT_ID)));
        System.setProperty(GOOGLE_CLIENT_SECRET, Objects.requireNonNull(dotenv.get(GOOGLE_CLIENT_SECRET)));
        System.setProperty(GOOGLE_REDIRECT_URI, Objects.requireNonNull(dotenv.get(GOOGLE_REDIRECT_URI)));
        SpringApplication.run(EcommercePlatformApplication.class, args);
    }
}
