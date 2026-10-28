package com.example.datavalidator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Configuration
public class RulesConfig {

    // Created once at startup and shared by every request; rules run in ruleOrder() order.
    @Bean
    public List<Rule> rules() {
        return Stream.of(
                        new AllowedValuesRule(),
                        new ConflictingValuesRule(),
                        new AgeRule(),
                        new SsnRule(),
                        new StudentIncomeRule(),
                        new StateOfResidenceRule(),
                        new ValidateHousehold(),
                        new ParentIncomeRule(),
                        new SpouseInfoRule())
                .sorted(Comparator.comparingDouble(Rule::ruleOrder))
                .toList();
    }
}
