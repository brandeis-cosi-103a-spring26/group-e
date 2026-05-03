package edu.brandeis.cosi103a.groupe.player;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import edu.brandeis.cosi.atg.event.GameStartEvent;
import edu.brandeis.cosi103a.groupe.api.LogEventRequest;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.ImmutableList;
import edu.brandeis.cosi.atg.state.CardStacks;
import com.google.common.collect.ImmutableMap;

public class GenerateLogRequest {
    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new GuavaModule());
        mapper.findAndRegisterModules(); 
        
        GameStartEvent event = new GameStartEvent(
            ImmutableList.of("Alice", "Bob"),
            new CardStacks(ImmutableMap.of())
        );
        LogEventRequest req = new LogEventRequest(null, event, UUID.randomUUID().toString());
        
        System.out.println("JSON GENERATED:");
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(req));
    }
}
