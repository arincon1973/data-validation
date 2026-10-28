package com.example.datavalidator;

import java.time.LocalDate;
import java.util.List;

import com.example.datavalidator.DataValidatorController.ValidationEntry;
import com.example.datavalidator.DataValidatorController.ValidationRequest;

public class SsnRule implements Rule {

    @Override
    public boolean validate(ValidationRequest request, List<ValidationEntry> entries, LocalDate schoolStart) {
        // The SSN itself is left out of the message so it doesn't end up in responses or logs.
        if (!SsnValidator.isValid(request.studentInfo().ssn())) {
            entries.add(new ValidationEntry("SSN", "Invalid SSN format (expected 9 digits or xxx-xx-xxxx)"));
            return false;
        }
        entries.add(new ValidationEntry("SSN", "Valid"));
        return true;
    }
}
