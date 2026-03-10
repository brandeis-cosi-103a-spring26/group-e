package edu.brandeis.cosi103a.groupe.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.databind.Module;

@Configuration
public class AppConfig {
    @Bean
    public Module guavaModule() {
        return new GuavaModule();
    }
}
