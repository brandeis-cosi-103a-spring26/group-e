package edu.brandeis.cosi103a.groupe.server;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi103a.groupe.player.AutoPlayer;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public Module guavaModule() {
        return new GuavaModule();
    }

    @Bean
    public Player player() {
        return new AutoPlayer();
    }

    @Bean
    public Module jdk8Module() {
        return new Jdk8Module();
    }

    @Bean
    public ConcurrentHashMap<String, Player> playerMap() {
        return new ConcurrentHashMap<>();
    }
}
