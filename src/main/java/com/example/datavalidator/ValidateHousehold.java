package com.example.datavalidator;

import java.time.LocalDate;
import java.util.List;

import com.example.datavalidator.DataValidatorController.Household;
import com.example.datavalidator.DataValidatorController.ValidationEntry;
import com.example.datavalidator.DataValidatorController.ValidationRequest;

public class ValidateHousehold implements Rule {

    private static final int MIN_HOUSEHOLD = 1;
    private static final int MIN_COLLEGE = 1;
    private static final int MIN_DEPENDENT_HOUSEHOLD = 2;

    @Override
    public float ruleOrder() {
        return 5;
    }

    @Override
    public boolean validate(ValidationRequest request, List<ValidationEntry> entries, LocalDate schoolStart) {
        Household household = request.household();
        int inHousehold = household.numberInHousehold();
        int inCollege = household.numberInCollege();
        boolean passed = true;

        if (inHousehold < MIN_HOUSEHOLD) {
            entries.add(new ValidationEntry("Household",
                    "Number in household must be at least %d (%d, < %d)"
                            .formatted(MIN_HOUSEHOLD, inHousehold, MIN_HOUSEHOLD)));
            passed = false;
        } else if ("dependent".equals(request.dependencyStatus()) && inHousehold < MIN_DEPENDENT_HOUSEHOLD) {
            // A dependent student lives with at least one parent.
            entries.add(new ValidationEntry("Household",
                    "Dependent student household must have at least %d people (%d, < %d)"
                            .formatted(MIN_DEPENDENT_HOUSEHOLD, inHousehold, MIN_DEPENDENT_HOUSEHOLD)));
            passed = false;
        }
        if (inCollege < MIN_COLLEGE) {
            entries.add(new ValidationEntry("Household",
                    "Number in college must be at least %d (%d, < %d)"
                            .formatted(MIN_COLLEGE, inCollege, MIN_COLLEGE)));
            passed = false;
        }
        if (passed) {
            entries.add(new ValidationEntry("Household", "Valid"));
        }
        return passed;
    }
}
