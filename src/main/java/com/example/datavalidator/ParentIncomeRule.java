package com.example.datavalidator;

import java.time.LocalDate;
import java.util.List;

import com.example.datavalidator.DataValidatorController.ValidationEntry;
import com.example.datavalidator.DataValidatorController.ValidationRequest;

public class ParentIncomeRule implements Rule {

    @Override
    public float ruleOrder() {
        return 6;
    }

    @Override
    public boolean validate(ValidationRequest request, List<ValidationEntry> entries, LocalDate schoolStart) {
        // Parent income only matters for dependent students.
        if ("dependent".equals(request.dependencyStatus())) {
            Long parentIncome = request.income().parentIncome();
            if (parentIncome == null) {
                entries.add(new ValidationEntry("Parent Income",
                        "Parent income is required for dependent students"));
                return false;
            }
            if (parentIncome < 0) {
                entries.add(new ValidationEntry("Parent Income",
                        "Parent income cannot be negative (%d, < 0)".formatted(parentIncome)));
                return false;
            }
        }
        entries.add(new ValidationEntry("Parent Income", "Valid"));
        return true;
    }
}
