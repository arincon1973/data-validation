package com.example.datavalidator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class RulesConfig {

    // Created once at startup and shared by every request; rules run in list order.
    @Bean
    public List<Rule> rules() {
        return List.of(
                new AgeRule(),
                (request, entries, schoolStart) -> true, // placeholder rule 2
                (request, entries, schoolStart) -> true  // placeholder rule 3
        );
    }
}
