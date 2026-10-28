package com.example.datavalidator;

import java.time.LocalDate;
import java.util.List;

import com.example.datavalidator.DataValidatorController.ValidationEntry;
import com.example.datavalidator.DataValidatorController.ValidationRequest;

public class StudentIncomeRule implements Rule {

    @Override
    public boolean validate(ValidationRequest request, List<ValidationEntry> entries, LocalDate schoolStart) {
        long studentIncome = request.income().studentIncome();

        if (studentIncome < 0) {
            entries.add(new ValidationEntry("Student Income",
                    "Student income cannot be negative (%d, < 0)".formatted(studentIncome)));
            return false;
        }
        entries.add(new ValidationEntry("Student Income", "Valid"));
        return true;
    }
}
