package com.example.datavalidator;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
public class DataValidatorController {

    public record StudentInfo(
            @NotNull String firstName,
            @NotNull String lastName,
            @NotNull String ssn,
            @NotNull LocalDate dateOfBirth) {}

    public record Household(@NotNull Integer numberInHousehold, @NotNull Integer numberInCollege) {}

    public record Income(@NotNull Long studentIncome, @NotNull Long parentIncome) {}

    public record ValidationRequest(
            @NotNull @Valid StudentInfo studentInfo,
            @NotNull String dependencyStatus,
            @NotNull String maritalStatus,
            @NotNull @Valid Household household,
            @NotNull @Valid Income income,
            @NotNull String stateOfResidence) {}

    public record ValidationEntry(String description, String status) {}

    public record ValidationResponse(List<ValidationEntry> results) {}

    @PostMapping("/validate")
    public ValidationResponse validate(@Valid @RequestBody ValidationRequest request) {
        StudentInfo student = request.studentInfo();
        Household household = request.household();
        Income income = request.income();
        return new ValidationResponse(List.of(
                entry("studentInfo:firstName", student.firstName()),
                entry("studentInfo:lastName", student.lastName()),
                entry("studentInfo:ssn", student.ssn()),
                entry("studentInfo:dateOfBirth", student.dateOfBirth()),
                entry("dependencyStatus", request.dependencyStatus()),
                entry("maritalStatus", request.maritalStatus()),
                entry("household:numberInHousehold", household.numberInHousehold()),
                entry("household:numberInCollege", household.numberInCollege()),
                entry("income:studentIncome", income.studentIncome()),
                entry("income:parentIncome", income.parentIncome()),
                entry("stateOfResidence", request.stateOfResidence())));
    }

    private static ValidationEntry entry(String field, Object value) {
        return new ValidationEntry(field, String.valueOf(value));
    }
}
