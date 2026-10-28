package com.example.datavalidator;

import com.example.datavalidator.DataValidatorController.Household;
import com.example.datavalidator.DataValidatorController.Income;
import com.example.datavalidator.DataValidatorController.StudentInfo;
import com.example.datavalidator.DataValidatorController.ValidationEntry;
import com.example.datavalidator.DataValidatorController.ValidationRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SsnRuleTests {

    private static final LocalDate SCHOOL_START = LocalDate.of(2027, 9, 1);

    private final SsnRule rule = new SsnRule();

    @Test
    void acceptsValidSsn() {
        List<ValidationEntry> entries = new ArrayList<>();

        assertTrue(rule.validate(requestWithSsn("123-45-6789"), entries, SCHOOL_START));
        assertEquals(List.of(new ValidationEntry("SSN", "Valid")), entries);
    }

    @Test
    void rejectsInvalidSsn() {
        List<ValidationEntry> entries = new ArrayList<>();

        assertFalse(rule.validate(requestWithSsn("12345"), entries, SCHOOL_START));
        assertEquals(List.of(new ValidationEntry("SSN",
                "Invalid SSN format (expected 9 digits or xxx-xx-xxxx)")), entries);
    }

    private static ValidationRequest requestWithSsn(String ssn) {
        return new ValidationRequest(
                new StudentInfo("Jane", "Smith", ssn, LocalDate.of(2003, 5, 15)),
                "dependent",
                "single",
                new Household(4, 1),
                new Income(5000L, 65000L),
                "CA",
                null);
    }
}
