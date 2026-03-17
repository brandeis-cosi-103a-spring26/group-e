package edu.brandeis.cosi103a.groupe.server;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi103a.groupe.player.AutoPlayer;
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
}
