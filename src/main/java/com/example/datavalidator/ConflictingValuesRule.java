package com.example.datavalidator;

import java.time.LocalDate;
import java.util.List;

import com.example.datavalidator.DataValidatorController.ValidationEntry;
import com.example.datavalidator.DataValidatorController.ValidationRequest;

public class ConflictingValuesRule implements Rule {

    @Override
    public float ruleOrder() {
        return 0.2f;
    }

    @Override
    public boolean validate(ValidationRequest request, List<ValidationEntry> entries, LocalDate schoolStart) {
        if ("dependent".equals(request.dependencyStatus()) && "married".equals(request.maritalStatus())) {
            entries.add(entry("Conflicting Values",
                    "Student cannot be both dependent and married"));
            return false;
        }
        entries.add(valid("Conflicting Values"));
        return true;
    }
}
