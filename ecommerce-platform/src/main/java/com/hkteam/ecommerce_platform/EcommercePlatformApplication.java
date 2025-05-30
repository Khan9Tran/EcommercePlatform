package com.hkteam.ecommerce_platform;

import java.util.Objects;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
@EnableFeignClients
@EnableRetry(proxyTargetClass = true)
@EnableAsync
@EnableCaching
public class EcommercePlatformApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();

        final String POSTGRES_DB = "POSTGRES_DB";
        final String POSTGRES_USER = "POSTGRES_USER";
        final String POSTGRES_PASSWORD = "POSTGRES_PASSWORD";
        final String HOST = "HOST";
        final String DB_PORT = "DB_PORT";
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

        // Google
        final String GOOGLE_CLIENT_ID = "GOOGLE_CLIENT_ID";
        final String GOOGLE_CLIENT_SECRET = "GOOGLE_CLIENT_SECRET";
        final String GOOGLE_REDIRECT_URI = "GOOGLE_REDIRECT_URI";

        // facebook
        final String FACEBOOK_CLIENT_ID = "FACEBOOK_CLIENT_ID";
        final String FACEBOOK_CLIENT_SECRET = "FACEBOOK_CLIENT_SECRET";
        final String FACEBOOK_REDIRECT_URI = "FACEBOOK_REDIRECT_URI";

        // VNPAY
        final String VN_PAY_PAY_URL = "VN_PAY_PAY_URL";
        final String VN_PAY_TMN_CODE = "VN_PAY_TMN_CODE";
        final String VN_PAY_SECRET_KEY = "VN_PAY_SECRET_KEY";
        final String VN_PAY_RETURN_URL = "VN_PAY_RETURN_URL";
        final String VN_PAY_VERSION = "VN_PAY_VERSION";
        final String VN_PAY_COMMAND = "VN_PAY_COMMAND";
        final String VN_PAY_ORDER_TYPE = "VN_PAY_ORDER_TYPE";

        //REDIS
        final String REDIS_HOST = "REDIS_HOST";
        final String REDIS_PORT = "REDIS_PORT";
        final String REDIS_PASSWORD = "REDIS_PASSWORD";
        final String REDIS_DB = "REDIS_DB";
        final String REDIS_USERNAME = "REDIS_USERNAME";

        //ES
        final String ES_HOST = "ES_HOST";
        final String ES_PORT = "ES_PORT";
        final String ES_USERNAME = "ES_USERNAME";
        final String ES_PASSWORD = "ES_PASSWORD";

        //RABBITMQ
        final String RABBITMQ_HOST = "RABBITMQ_HOST";
        final String RABBITMQ_PORT = "RABBITMQ_PORT";
        final String RABBITMQ_VHOST = "RABBITMQ_VHOST";
        final String RABBITMQ_DEFAULT_USER = "RABBITMQ_DEFAULT_USER";
        final String RABBITMQ_DEFAULT_PASS = "RABBITMQ_DEFAULT_PASS";

        //CORS
        final String ALLOWED_ORIGINS = "ALLOWED_ORIGINS";
        final String FRONTEND_URL = "FRONTEND_URL";
        final String OPEN_API_URL = "OPEN_API_URL";
        final String BACKEND_URL = "BACKEND_URL";

        System.setProperty(POSTGRES_DB, Objects.requireNonNull(dotenv.get(POSTGRES_DB)));
        System.setProperty(POSTGRES_USER, Objects.requireNonNull(dotenv.get(POSTGRES_USER)));
        System.setProperty(POSTGRES_PASSWORD, Objects.requireNonNull(dotenv.get(POSTGRES_PASSWORD)));
        System.setProperty(HOST, Objects.requireNonNull(dotenv.get(HOST)));
        System.setProperty(DB_PORT, Objects.requireNonNull(dotenv.get(DB_PORT)));
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
        System.setProperty(RABBITMQ_DEFAULT_USER, Objects.requireNonNull(dotenv.get(RABBITMQ_DEFAULT_USER)));
        System.setProperty(RABBITMQ_DEFAULT_PASS, Objects.requireNonNull(dotenv.get(RABBITMQ_DEFAULT_PASS)));
        System.setProperty(FACEBOOK_CLIENT_ID, Objects.requireNonNull(dotenv.get(FACEBOOK_CLIENT_ID)));
        System.setProperty(FACEBOOK_CLIENT_SECRET, Objects.requireNonNull(dotenv.get(FACEBOOK_CLIENT_SECRET)));
        System.setProperty(FACEBOOK_REDIRECT_URI, Objects.requireNonNull(dotenv.get(FACEBOOK_REDIRECT_URI)));
        System.setProperty(VN_PAY_PAY_URL, Objects.requireNonNull(dotenv.get(VN_PAY_PAY_URL)));
        System.setProperty(VN_PAY_TMN_CODE, Objects.requireNonNull(dotenv.get(VN_PAY_TMN_CODE)));
        System.setProperty(VN_PAY_SECRET_KEY, Objects.requireNonNull(dotenv.get(VN_PAY_SECRET_KEY)));
        System.setProperty(VN_PAY_RETURN_URL, Objects.requireNonNull(dotenv.get(VN_PAY_RETURN_URL)));
        System.setProperty(VN_PAY_VERSION, Objects.requireNonNull(dotenv.get(VN_PAY_VERSION)));
        System.setProperty(VN_PAY_COMMAND, Objects.requireNonNull(dotenv.get(VN_PAY_COMMAND)));
        System.setProperty(VN_PAY_ORDER_TYPE, Objects.requireNonNull(dotenv.get(VN_PAY_ORDER_TYPE)));
        System.setProperty(REDIS_HOST, Objects.requireNonNull(dotenv.get(REDIS_HOST)));
        System.setProperty(REDIS_PORT, Objects.requireNonNull(dotenv.get(REDIS_PORT)));
        System.setProperty(ES_HOST, Objects.requireNonNull(dotenv.get(ES_HOST)));
        System.setProperty(ES_PORT, Objects.requireNonNull(dotenv.get(ES_PORT)));
        System.setProperty(RABBITMQ_HOST, Objects.requireNonNull(dotenv.get(RABBITMQ_HOST)));
        System.setProperty(RABBITMQ_PORT, Objects.requireNonNull(dotenv.get(RABBITMQ_PORT)));
        System.setProperty(ALLOWED_ORIGINS, Objects.requireNonNull(dotenv.get(ALLOWED_ORIGINS)));
        System.setProperty(FRONTEND_URL, Objects.requireNonNull(dotenv.get(FRONTEND_URL)));
        System.setProperty(OPEN_API_URL, Objects.requireNonNull(dotenv.get(OPEN_API_URL)));
        System.setProperty(BACKEND_URL, Objects.requireNonNull(dotenv.get(BACKEND_URL)));
        System.setProperty(RABBITMQ_VHOST, Objects.requireNonNull(dotenv.get(RABBITMQ_VHOST)));
        System.setProperty(REDIS_PASSWORD, Objects.requireNonNull(dotenv.get(REDIS_PASSWORD)));
        System.setProperty(REDIS_DB, Objects.requireNonNull(dotenv.get(REDIS_DB)));
        System.setProperty(REDIS_USERNAME, Objects.requireNonNull(dotenv.get(REDIS_USERNAME)));
        System.setProperty(ES_USERNAME, Objects.requireNonNull(dotenv.get(ES_USERNAME)));
        System.setProperty(ES_PASSWORD, Objects.requireNonNull(dotenv.get(ES_PASSWORD)));
        SpringApplication.run(EcommercePlatformApplication.class, args);
    }
}
