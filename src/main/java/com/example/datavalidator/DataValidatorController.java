package com.example.datavalidator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
public class DataValidatorController {

    private static final LocalDate SCHOOL_START = LocalDate.of(2027, 9, 1);

    private final List<Rule> rules;
    private final MeterRegistry meterRegistry;

    public DataValidatorController(List<Rule> rules, MeterRegistry meterRegistry) {
        this.rules = rules;
        this.meterRegistry = meterRegistry;
    }

    public record StudentInfo(
            @NotNull String firstName,
            @NotNull String lastName,
            @NotNull String ssn,
            @NotNull LocalDate dateOfBirth) {}

    public record Household(@NotNull Integer numberInHousehold, @NotNull Integer numberInCollege) {}

    public record Income(@NotNull Long studentIncome, Long parentIncome) {}

    public record SpouseInfo(String firstName, String lastName, String ssn) {}

    public record ValidationRequest(
            @NotNull @Valid StudentInfo studentInfo,
            @NotNull String dependencyStatus,
            @NotNull String maritalStatus,
            @NotNull @Valid Household household,
            @NotNull @Valid Income income,
            @NotNull String stateOfResidence,
            SpouseInfo spouseInfo) {}

    public record ValidationEntry(String description, String status, String severity) {}

    public record ValidationResponse(List<ValidationEntry> results) {}

    @PostMapping("/validate")
    public ValidationResponse validate(@Valid @RequestBody ValidationRequest request) {
        List<ValidationEntry> entries = new ArrayList<>();
        boolean allPassed = true;
        for (Rule rule : rules) {
            Timer.Sample sample = Timer.start(meterRegistry);
            boolean passed = rule.validate(request, entries, SCHOOL_START);
            sample.stop(meterRegistry.timer("validation.rule",
                    "rule", rule.getClass().getSimpleName(),
                    "result", passed ? "passed" : "failed"));
            if (!passed) {
                allPassed = false;
            }
        }
        entries.add(0, allPassed
                ? new ValidationEntry("Status", "Validation passed", "all valid")
                : new ValidationEntry("Status", "Validation failed", "invalid"));
        return new ValidationResponse(entries);
    }
}
