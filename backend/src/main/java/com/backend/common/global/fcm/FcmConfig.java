package com.backend.common.global.fcm;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
@Profile("!test")
public class FcmConfig {

    @PostConstruct
    public void init() throws IOException{
        if(!FirebaseApp.getApps().isEmpty()) return;

        try(InputStream serviceAccount =
                new ClassPathResource("google/aibe6-fcm-notifications-firebase.json").getInputStream()){

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
        }
    }
}
