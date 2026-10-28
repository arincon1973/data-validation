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

class ValidateHouseholdTests {

    private static final LocalDate SCHOOL_START = LocalDate.of(2027, 9, 1);

    private final ValidateHousehold rule = new ValidateHousehold();

    @Test
    void acceptsDependentWithTwoInHousehold() {
        List<ValidationEntry> entries = new ArrayList<>();

        assertTrue(rule.validate(request("dependent", 2, 1), entries, SCHOOL_START));
        assertEquals(List.of(new ValidationEntry("Household", "Valid", "N/A")), entries);
    }

    @Test
    void acceptsIndependentLivingAlone() {
        List<ValidationEntry> entries = new ArrayList<>();

        assertTrue(rule.validate(request("independent", 1, 1), entries, SCHOOL_START));
        assertEquals(List.of(new ValidationEntry("Household", "Valid", "N/A")), entries);
    }

    @Test
    void rejectsEmptyHousehold() {
        List<ValidationEntry> entries = new ArrayList<>();

        assertFalse(rule.validate(request("independent", 0, 1), entries, SCHOOL_START));
        assertEquals(List.of(new ValidationEntry("Household",
                "Number in household must be at least 1 (0, < 1)", "needs review")), entries);
    }

    @Test
    void rejectsDependentLivingAlone() {
        List<ValidationEntry> entries = new ArrayList<>();

        assertFalse(rule.validate(request("dependent", 1, 1), entries, SCHOOL_START));
        assertEquals(List.of(new ValidationEntry("Household",
                "Dependent student household must have at least 2 people (1, < 2)", "needs review")), entries);
    }

    @Test
    void rejectsNoneInCollege() {
        List<ValidationEntry> entries = new ArrayList<>();

        assertFalse(rule.validate(request("dependent", 4, 0), entries, SCHOOL_START));
        assertEquals(List.of(new ValidationEntry("Household",
                "Number in college must be at least 1 (0, < 1)", "needs review")), entries);
    }

    @Test
    void reportsEveryFailedCheck() {
        List<ValidationEntry> entries = new ArrayList<>();

        assertFalse(rule.validate(request("dependent", 1, 0), entries, SCHOOL_START));
        assertEquals(List.of(
                new ValidationEntry("Household",
                        "Dependent student household must have at least 2 people (1, < 2)", "needs review"),
                new ValidationEntry("Household",
                        "Number in college must be at least 1 (0, < 1)", "needs review")), entries);
    }

    private static ValidationRequest request(String dependencyStatus, int inHousehold, int inCollege) {
        return new ValidationRequest(
                new StudentInfo("Jane", "Smith", "123456789", LocalDate.of(2003, 5, 15)),
                dependencyStatus,
                "single",
                new Household(inHousehold, inCollege),
                new Income(5000L, 65000L),
                "CA",
                null);
    }
}
