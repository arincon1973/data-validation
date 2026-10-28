package com.example.datavalidator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
public class DataValidatorController {

    private static final LocalDate SCHOOL_START = LocalDate.of(2027, 9, 1);

    private final List<Rule> rules;

    public DataValidatorController(List<Rule> rules) {
        this.rules = rules;
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

    public record ValidationEntry(String description, String status) {}

    public record ValidationResponse(List<ValidationEntry> results) {}

    @PostMapping("/validate")
    public ValidationResponse validate(@Valid @RequestBody ValidationRequest request) {
        List<ValidationEntry> entries = new ArrayList<>();
        boolean allPassed = true;
        for (Rule rule : rules) {
            if (!rule.validate(request, entries, SCHOOL_START)) {
                allPassed = false;
            }
        }
        entries.add(0, new ValidationEntry("Status", allPassed ? "Validation passed" : "Validation failed"));
        return new ValidationResponse(entries);
    }
}
