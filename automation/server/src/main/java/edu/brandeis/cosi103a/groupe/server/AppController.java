package edu.brandeis.cosi103a.groupe.server;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppController {
    @PostMapping("/hello")
    public String play() {
        return "Hello, World!";
    }
}
