package edu.brandeis.cosi103a.groupe.server;

import tools.jackson.databind.JacksonModule;
import tools.jackson.datatype.guava.GuavaModule;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi103a.groupe.player.AutoPlayer;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class AppConfig {
    @Bean
    public JacksonModule guavaModule() {
        return new GuavaModule();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public Player player() {
        return new AutoPlayer();
    }

    @Bean
    public ConcurrentHashMap<String, Player> playerMap() {
        return new ConcurrentHashMap<>();
    }
}
