package edu.brandeis.cosi103a.groupe.server;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import tools.jackson.databind.JacksonModule;
import tools.jackson.datatype.guava.GuavaModule;
import edu.brandeis.cosi.atg.player.Player;
import edu.brandeis.cosi103a.groupe.player.AutoPlayer;
import org.junit.Test;

public class AppConfigTest {

    @Test
    public void guavaModule_ReturnsGuavaModule() {
        AppConfig config = new AppConfig();
        JacksonModule module = config.guavaModule();

        assertNotNull(module);
        assertTrue(module instanceof GuavaModule);
    }

    @Test
    public void player_ReturnsAutoPlayer() {
        AppConfig config = new AppConfig();
        Player player = config.player();

        assertNotNull(player);
        assertTrue(player instanceof AutoPlayer);
    }
}
