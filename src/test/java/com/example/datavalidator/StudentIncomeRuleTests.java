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

class StudentIncomeRuleTests {

    private static final LocalDate SCHOOL_START = LocalDate.of(2027, 9, 1);

    private final StudentIncomeRule rule = new StudentIncomeRule();

    @Test
    void acceptsZeroIncome() {
        List<ValidationEntry> entries = new ArrayList<>();

        assertTrue(rule.validate(requestWithStudentIncome(0L), entries, SCHOOL_START));
        assertEquals(List.of(new ValidationEntry("Student Income", "Valid", "N/A")), entries);
    }

    @Test
    void acceptsPositiveIncome() {
        List<ValidationEntry> entries = new ArrayList<>();

        assertTrue(rule.validate(requestWithStudentIncome(5000L), entries, SCHOOL_START));
        assertEquals(List.of(new ValidationEntry("Student Income", "Valid", "N/A")), entries);
    }

    @Test
    void rejectsNegativeIncome() {
        List<ValidationEntry> entries = new ArrayList<>();

        assertFalse(rule.validate(requestWithStudentIncome(-1L), entries, SCHOOL_START));
        assertEquals(List.of(new ValidationEntry("Student Income",
                "Student income cannot be negative (-1, < 0)", "needs review")), entries);
    }

    private static ValidationRequest requestWithStudentIncome(long studentIncome) {
        return new ValidationRequest(
                new StudentInfo("Jane", "Smith", "123456789", LocalDate.of(2003, 5, 15)),
                "dependent",
                "single",
                new Household(4, 1),
                new Income(studentIncome, 65000L),
                "CA",
                null);
    }
}
