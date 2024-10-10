package com.spring.soft.usermanagement.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwk.Jwk;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.security.interfaces.RSAPublicKey;

@Component
public class JwtTokenExtractor {

    private final String issuer;
    private final JwkProvider jwkProvider;

    public JwtTokenExtractor(
            @Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}") String issuerUri,
            @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") String jwkSetUri) throws Exception {
        this.issuer = issuerUri;
        this.jwkProvider = new UrlJwkProvider(new URL(jwkSetUri));
    }

    public String extractEmailFromToken(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            Jwk jwk = jwkProvider.get(jwt.getKeyId());
            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);

            jwt = JWT.require(algorithm)
                    .withIssuer(issuer)
                    .build()
                    .verify(token);

            return jwt.getClaim("email").asString();
        } catch (JWTVerificationException exception) {
            throw new RuntimeException("Invalid JWT token", exception);
        } catch (Exception e) {
            throw new RuntimeException("Error processing JWT token", e);
        }
    }
}
