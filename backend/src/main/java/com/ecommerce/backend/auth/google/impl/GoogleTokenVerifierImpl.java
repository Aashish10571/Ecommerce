package com.ecommerce.backend.auth.google.impl;

import com.ecommerce.backend.auth.exception.GoogleTokenInvalidException;
import com.ecommerce.backend.auth.google.GoogleTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Service
public class GoogleTokenVerifierImpl implements GoogleTokenVerifier {

    @Value("${google.client-id}")
    private String googleClientId;

    private GoogleIdTokenVerifier verifier;

    @PostConstruct
    private void init() {
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance()
        ).setAudience(List.of(googleClientId)).build();
    }

    @Override
    public GoogleIdToken.Payload verifyToken(String idTokenString) {
        GoogleIdToken idToken = verify(idTokenString);

        if (idToken == null) {
            throw new GoogleTokenInvalidException("Invalid Google ID token");
        }

        return idToken.getPayload();
    }

    private GoogleIdToken verify(String idTokenString) {
        try {
            return verifier.verify(idTokenString);
        } catch (GeneralSecurityException | IOException exception) {
            throw new GoogleTokenInvalidException("Google token verification failed", exception);
        }
    }
}