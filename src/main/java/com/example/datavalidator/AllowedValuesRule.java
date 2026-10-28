package com.example.datavalidator;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.example.datavalidator.DataValidatorController.ValidationEntry;
import com.example.datavalidator.DataValidatorController.ValidationRequest;

public class AllowedValuesRule implements Rule {

    private static final Set<String> DEPENDENCY_STATUSES = Set.of("dependent", "independent");
    private static final Set<String> MARITAL_STATUSES = Set.of("single", "married");

    @Override
    public float ruleOrder() {
        return 0.1f;
    }

    @Override
    public boolean validate(ValidationRequest request, List<ValidationEntry> entries, LocalDate schoolStart) {
        boolean dependencyValid = check(entries, "Dependency Status", request.dependencyStatus(),
                DEPENDENCY_STATUSES, "dependent or independent");
        boolean maritalValid = check(entries, "Marital Status", request.maritalStatus(),
                MARITAL_STATUSES, "single or married");
        return dependencyValid && maritalValid;
    }

    private static boolean check(List<ValidationEntry> entries, String field, String value,
                                 Set<String> allowed, String expected) {
        if (!allowed.contains(value)) {
            entries.add(new ValidationEntry(field,
                    "Invalid value (%s), expected %s".formatted(value, expected)));
            return false;
        }
        entries.add(new ValidationEntry(field, "Valid"));
        return true;
    }
}
