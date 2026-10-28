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

class ParentIncomeRuleTests {

    private static final LocalDate SCHOOL_START = LocalDate.of(2027, 9, 1);

    private final ParentIncomeRule rule = new ParentIncomeRule();

    @Test
    void acceptsDependentWithZeroParentIncome() {
        List<ValidationEntry> entries = new ArrayList<>();

        assertTrue(rule.validate(request("dependent", 0L), entries, SCHOOL_START));
        assertEquals(List.of(new ValidationEntry("Parent Income", "Valid", "N/A")), entries);
    }

    @Test
    void acceptsDependentWithPositiveParentIncome() {
        List<ValidationEntry> entries = new ArrayList<>();

        assertTrue(rule.validate(request("dependent", 65000L), entries, SCHOOL_START));
        assertEquals(List.of(new ValidationEntry("Parent Income", "Valid", "N/A")), entries);
    }

    @Test
    void rejectsDependentWithoutParentIncome() {
        List<ValidationEntry> entries = new ArrayList<>();

        assertFalse(rule.validate(request("dependent", null), entries, SCHOOL_START));
        assertEquals(List.of(new ValidationEntry("Parent Income",
                "Parent income is required for dependent students", "needs review")), entries);
    }

    @Test
    void rejectsDependentWithNegativeParentIncome() {
        List<ValidationEntry> entries = new ArrayList<>();

        assertFalse(rule.validate(request("dependent", -1L), entries, SCHOOL_START));
        assertEquals(List.of(new ValidationEntry("Parent Income",
                "Parent income cannot be negative (-1, < 0)", "needs review")), entries);
    }

    @Test
    void acceptsIndependentWithoutParentIncome() {
        List<ValidationEntry> entries = new ArrayList<>();

        assertTrue(rule.validate(request("independent", null), entries, SCHOOL_START));
        assertEquals(List.of(new ValidationEntry("Parent Income", "Valid", "N/A")), entries);
    }

    @Test
    void ignoresParentIncomeForIndependent() {
        List<ValidationEntry> entries = new ArrayList<>();

        assertTrue(rule.validate(request("independent", -1L), entries, SCHOOL_START));
        assertEquals(List.of(new ValidationEntry("Parent Income", "Valid", "N/A")), entries);
    }

    private static ValidationRequest request(String dependencyStatus, Long parentIncome) {
        return new ValidationRequest(
                new StudentInfo("Jane", "Smith", "123456789", LocalDate.of(2003, 5, 15)),
                dependencyStatus,
                "single",
                new Household(4, 1),
                new Income(5000L, parentIncome),
                "CA",
                null);
    }
}
