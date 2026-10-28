package com.example.datavalidator;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import com.example.datavalidator.DataValidatorController.ValidationEntry;
import com.example.datavalidator.DataValidatorController.ValidationRequest;

public class AgeRule implements Rule {

    private static final int MIN_AGE = 14;
    private static final int MAX_AGE = 120;

    @Override
    public float ruleOrder() {
        return 1;
    }

    @Override
    public boolean validate(ValidationRequest request, List<ValidationEntry> entries, LocalDate schoolStart) {
        LocalDate dateOfBirth = request.studentInfo().dateOfBirth();
        int age = Period.between(dateOfBirth, schoolStart).getYears();

        if (age < MIN_AGE) {
            entries.add(new ValidationEntry("Age",
                    "Student too young (%d years old, < %d)".formatted(age, MIN_AGE)));
            return false;
        }
        if (age > MAX_AGE) {
            entries.add(new ValidationEntry("Age",
                    "Student too old (%d years old, > %d)".formatted(age, MAX_AGE)));
            return false;
        }
        entries.add(new ValidationEntry("Age", "Valid"));
        return true;
    }
}
