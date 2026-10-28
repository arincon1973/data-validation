package com.example.datavalidator;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.example.datavalidator.DataValidatorController.ValidationEntry;
import com.example.datavalidator.DataValidatorController.ValidationRequest;

public class StateOfResidenceRule implements Rule {

    // The 50 US states; DC and territories are not included.
    private static final Set<String> STATES = Set.of(
            "AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA",
            "HI", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME", "MD",
            "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ",
            "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC",
            "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI", "WY");

    @Override
    public float ruleOrder() {
        return 4;
    }

    @Override
    public boolean validate(ValidationRequest request, List<ValidationEntry> entries, LocalDate schoolStart) {
        String state = request.stateOfResidence();

        if (!STATES.contains(state)) {
            entries.add(new ValidationEntry("State of Residence",
                    "Invalid US state abbreviation (%s)".formatted(state)));
            return false;
        }
        entries.add(new ValidationEntry("State of Residence", "Valid"));
        return true;
    }
}
