package com.example.datavalidator;

import java.time.LocalDate;
import java.util.List;

import com.example.datavalidator.DataValidatorController.SpouseInfo;
import com.example.datavalidator.DataValidatorController.ValidationEntry;
import com.example.datavalidator.DataValidatorController.ValidationRequest;

public class SpouseInfoRule implements Rule {

    @Override
    public float ruleOrder() {
        return 7;
    }

    @Override
    public boolean validate(ValidationRequest request, List<ValidationEntry> entries, LocalDate schoolStart) {
        // Spouse info only matters for married students.
        if (!"married".equals(request.maritalStatus())) {
            entries.add(valid("Spouse"));
            return true;
        }

        SpouseInfo spouse = request.spouseInfo();
        if (spouse == null) {
            entries.add(entry("Spouse", "Spouse info is required for married students"));
            return false;
        }

        boolean passed = true;
        if (isMissing(spouse.firstName())) {
            entries.add(entry("Spouse", "Spouse first name is required for married students"));
            passed = false;
        }
        if (isMissing(spouse.lastName())) {
            entries.add(entry("Spouse", "Spouse last name is required for married students"));
            passed = false;
        }
        if (isMissing(spouse.ssn())) {
            entries.add(entry("Spouse", "Spouse SSN is required for married students"));
            passed = false;
        } else if (!SsnValidator.isValid(spouse.ssn())) {
            // The SSN itself is left out of the message so it doesn't end up in responses or logs.
            entries.add(entry("Spouse",
                    "Invalid spouse SSN format (expected 9 digits or xxx-xx-xxxx)"));
            passed = false;
        }
        if (passed) {
            entries.add(valid("Spouse"));
        }
        return passed;
    }

    private static boolean isMissing(String value) {
        return value == null || value.isBlank();
    }
}
