package edu.brandeis.cosi103a.groupe.server;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
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
    public Module guavaModule() {
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
