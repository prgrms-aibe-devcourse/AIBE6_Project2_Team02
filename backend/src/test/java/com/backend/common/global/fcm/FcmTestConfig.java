package com.backend.common.global.fcm;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class FcmTestConfig {

    @Bean
    public FcmSender fcmSender() {
        return Mockito.mock(FcmSender.class);
    }
}
