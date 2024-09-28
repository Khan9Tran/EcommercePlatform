package com.hkteam.ecommerce_platform.util;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public class JwtUtils {

    @Value("${mail.secretKey}")
    String SECRET;

    @Value("${mail.valid-duration}")
    long EXPIRATION_TIME;

    public String generateToken(String subject, String tokenId) throws JOSEException {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .jwtID(tokenId)
                .subject(subject)
                .issuer("HKTeam")
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + (EXPIRATION_TIME * 1000)))
                .build();

        JWSSigner signer = new MACSigner(SECRET.getBytes());
        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS512), claims);
        signedJWT.sign(signer);

        return signedJWT.serialize();
    }
}
