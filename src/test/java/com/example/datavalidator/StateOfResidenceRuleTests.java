package com.example.datavalidator;

import com.example.datavalidator.DataValidatorController.Household;
import com.example.datavalidator.DataValidatorController.Income;
import com.example.datavalidator.DataValidatorController.StudentInfo;
import com.example.datavalidator.DataValidatorController.ValidationEntry;
import com.example.datavalidator.DataValidatorController.ValidationRequest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StateOfResidenceRuleTests {

    private static final LocalDate SCHOOL_START = LocalDate.of(2027, 9, 1);

    private final StateOfResidenceRule rule = new StateOfResidenceRule();

    @ParameterizedTest
    @ValueSource(strings = {"CA", "NY", "TX", "AK", "HI", "WY"})
    void acceptsStateAbbreviations(String state) {
        List<ValidationEntry> entries = new ArrayList<>();

        assertTrue(rule.validate(requestFrom(state), entries, SCHOOL_START));
        assertEquals(List.of(new ValidationEntry("State of Residence", "Valid", "N/A")), entries);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "XX", "ca", "Calif", "California", "C", " CA", "DC", "PR"})
    void rejectsInvalidAbbreviations(String state) {
        List<ValidationEntry> entries = new ArrayList<>();

        assertFalse(rule.validate(requestFrom(state), entries, SCHOOL_START));
        assertEquals(List.of(new ValidationEntry("State of Residence",
                "Invalid US state abbreviation (" + state + ")", "needs review")), entries);
    }

    private static ValidationRequest requestFrom(String state) {
        return new ValidationRequest(
                new StudentInfo("Jane", "Smith", "123456789", LocalDate.of(2003, 5, 15)),
                "dependent",
                "single",
                new Household(4, 1),
                new Income(5000L, 65000L),
                state,
                null);
    }
}
